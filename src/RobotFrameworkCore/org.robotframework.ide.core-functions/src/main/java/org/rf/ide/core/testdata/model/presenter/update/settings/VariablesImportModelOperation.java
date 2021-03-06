/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariablesImportModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_VARIABLES_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.VARIABLES_IMPORT_SETTING);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment) {
        final VariablesImport newVariablesImport = isValidTableIndex(settingsTable, tableIndex)
                ? settingsTable.newVariablesImport(tableIndex) : settingsTable.newVariablesImport();
        if (!args.isEmpty()) {
            newVariablesImport.setPathOrName(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newVariablesImport.addArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newVariablesImport.setComment(comment);
        }
        return newVariablesImport;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final VariablesImport variablesImport = (VariablesImport) modelElement;
        if (index == 0) {
            variablesImport.setPathOrName(value != null ? value : "");
        } else if (index > 0) {
            if (value != null) {
                variablesImport.setArguments(index - 1, value);
            } else {
                variablesImport.removeElementToken(index - 1);
            }
        }
    }
    
    @Override
    public void insert(final SettingTable settingsTable, final int index, final AModelElement<?> modelElement) {
        settingsTable.addImported((AImported) modelElement, index);
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElement) {
        settingsTable.removeImported((AImported) modelElement);
    }
    
    private boolean isValidTableIndex(final SettingTable settingsTable, final int tableIndex) {
        return tableIndex >= 0 && tableIndex < settingsTable.getImports().size();
    }
}
