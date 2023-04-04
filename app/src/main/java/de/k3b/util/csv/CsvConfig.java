/*
Copyright (C) 2023 by k3b

This file is part of CSVDroid (https://github.com/k3b/CSVDroid)

This program is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.util.csv;

import org.jetbrains.annotations.NotNull;

import de.k3b.util.StringUtils;

/**
 * Configuration for CSV file format.
 * <p>
 * Implementation detail for csv support. This file should be not have dependencies to
 * android or app
 */
public class CsvConfig {
    public static final CsvConfig DEFAULT = new CsvConfig(',', '"');

    private static CsvConfig lastUsedConfig = DEFAULT;
    public static final char[] CSV_DELIMITER_CANDIDATES = {DEFAULT.getFieldDelimiterChar(), ';', '\t', ':', '|'};
    public static final char[] CSV_QUOTE_CANDIDATES = {DEFAULT.getQuoteChar(), '\''};
    private final char m_fieldDelimiterChar;
    private final char m_quoteChar;

    public CsvConfig(char fieldDelimiterChar, char quoteChar) {
        m_fieldDelimiterChar = fieldDelimiterChar;
        m_quoteChar = quoteChar;
    }

    public static CsvConfig infer(String line) {
        char csvFieldDelimiterChar = findChar(line, CSV_DELIMITER_CANDIDATES);
        char csvQuoteChar = findChar(line, CSV_QUOTE_CANDIDATES);

        lastUsedConfig = new CsvConfig(csvFieldDelimiterChar, csvQuoteChar);
        return lastUsedConfig;
    }

    private static char findChar(String line, char... candidates) {
        int pos = StringUtils.indexOfAny(line, 0, line.length(), candidates);
        return pos == -1 ? candidates[0] : line.charAt(pos);
    }

    @NotNull public static CsvConfig getLastUsedConfig() {
        return lastUsedConfig;
    }

    public char getFieldDelimiterChar() {
        return m_fieldDelimiterChar;
    }

    public char getQuoteChar() {
        return m_quoteChar;
    }

}
