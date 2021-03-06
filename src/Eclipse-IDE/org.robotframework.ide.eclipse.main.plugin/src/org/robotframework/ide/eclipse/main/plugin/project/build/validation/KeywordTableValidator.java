/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.VersionDependentValidators;

import com.google.common.collect.ImmutableMap;

class KeywordTableValidator implements ModelUnitValidator {

    private final Optional<RobotKeywordsSection> keywordSection;

    private final ValidationReportingStrategy reporter;

    private final FileValidationContext validationContext;

    private final VersionDependentValidators versionDependentValidators;

    KeywordTableValidator(final FileValidationContext validationContext,
            final Optional<RobotKeywordsSection> keywordSection, final ValidationReportingStrategy reporter) {
        this.validationContext = validationContext;
        this.keywordSection = keywordSection;
        this.reporter = reporter;
        this.versionDependentValidators = new VersionDependentValidators(validationContext, reporter);
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        if (!keywordSection.isPresent()) {
            return;
        }
        final RobotKeywordsSection robotKeywordsSection = keywordSection.get();
        final KeywordTable keywordTable = robotKeywordsSection.getLinkedElement();
        final List<UserKeyword> keywords = keywordTable.getKeywords();

        reportVersionSpecificProblems(keywordTable);

        reportDuplicatedKeywords(keywords);

        validateKeywords(keywords);
    }

    private void reportVersionSpecificProblems(final KeywordTable table)
            throws CoreException {
        versionDependentValidators.getKeywordTableValidators(table).forEach(ModelUnitValidator::validate);
    }

    private void reportDuplicatedKeywords(final List<UserKeyword> keywords) {
        final Set<String> duplicatedNames = newHashSet();

        for (final UserKeyword kw1 : keywords) {
            for (final UserKeyword kw2 : keywords) {
                if (kw1 != kw2) {
                    final String kw1Name = QualifiedKeywordName.unifyDefinition(kw1.getName().getText());
                    final String kw2Name = QualifiedKeywordName.unifyDefinition(kw2.getName().getText());

                    if (kw1Name.equals(kw2Name)) {
                        duplicatedNames.add(kw1Name);
                    }
                }
            }
        }

        for (final UserKeyword keyword : keywords) {
            final RobotToken keywordName = keyword.getName();
            final String name = keywordName.getText();

            if (duplicatedNames.contains(QualifiedKeywordName.unifyDefinition(name.toLowerCase()))) {
                final RobotProblem problem = RobotProblem.causedBy(KeywordsProblem.DUPLICATED_KEYWORD)
                        .formatMessageWith(name);
                final Map<String, Object> additionalArguments = ImmutableMap.of(AdditionalMarkerAttributes.NAME, name);
                reporter.handleProblem(problem, validationContext.getFile(), keywordName, additionalArguments);
            }
        }
    }

    private void validateKeywords(final List<UserKeyword> keywords) {
        for (final UserKeyword keyword : keywords) {
            new KeywordValidator(validationContext, keyword, reporter).validate();
        }
    }
}
