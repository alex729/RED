/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FileFormat;

import com.google.common.annotations.VisibleForTesting;

public class TokenSeparatorBuilder {

    private static final Pattern PIPE_SEPARATOR_BEGIN = Pattern.compile("(^[|]\\s+)|(^[|]$)");

    private final FileFormat format;

    public TokenSeparatorBuilder(final FileFormat fileFormat) {
        this.format = fileFormat;
    }

    public ALineSeparator createSeparator(final int lineNumber, final String line) {
        ALineSeparator thisLineSeparator = null;

        if (format == FileFormat.TXT_OR_ROBOT) {
            thisLineSeparator = new WhitespaceSeparator(lineNumber, line);
            if (isPipeSeparated(line)) {
                thisLineSeparator = new PipeSeparator(lineNumber, line);
            }
        } else if (format == FileFormat.TSV) {
            thisLineSeparator = new StrictTsvTabulatorSeparator(lineNumber, line);
        }

        return thisLineSeparator;
    }

    @VisibleForTesting
    public boolean isPipeSeparated(final String line) {
        boolean result = false;
        if (format == FileFormat.TXT_OR_ROBOT) {
            result = PIPE_SEPARATOR_BEGIN.matcher(line).find();
        }

        return result;
    }
}
