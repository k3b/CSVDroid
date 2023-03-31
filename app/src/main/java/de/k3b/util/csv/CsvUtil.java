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

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.k3b.android.csvdroid.model.CsvItem;

/**
 * Replacement for CSVReaderBuilder...build() that infers the csv parameters
 * <p>
 * Implementation detail for csv support. This file should be not have dependencies to
 * android or app
 */
public class CsvUtil {
    public static final int BUFFER_SIZE = 8096;
    public static final String[] EMPTY = new String[0];

    @Nullable
    public static CSVReader openCsv(@Nullable String csv) throws IOException {
        BufferedReader bufferedReader = null;
        try  {
            if (csv != null) {
                // parser cannot handle empty lines if they are not "\r\n"
                bufferedReader = new BufferedReader(new StringReader(csv.replace("\n", "\r\n")), BUFFER_SIZE);
                CsvConfig csvConfig = inferCsvConfiguration(bufferedReader);

                ICSVParser parser = new CSVParserBuilder()
                        .withSeparator(csvConfig.getFieldDelimiterChar())
                        .withQuoteChar(csvConfig.getQuoteChar())
                        .build();

                // reader will be closed by caller
                return new CSVReaderBuilder(bufferedReader)
                        .withSkipLines(0)
                        .withCSVParser(parser)
                        .withKeepCarriageReturn(true)
                        .build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            bufferedReader.close();
        }

        return null;
    }

    @NotNull private static CsvConfig inferCsvConfiguration(@NotNull BufferedReader bufferedReader) throws IOException {
        // remember where we started.
        bufferedReader.mark(BUFFER_SIZE);
        try {
            String line;
            while (null != (line = bufferedReader.readLine())) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    return CsvConfig.infer(line);
                }
            }
            return CsvConfig.DEFAULT;
        } finally {
            // go back to start of csv
            bufferedReader.reset();
        }
    }

    @Nullable
    public static CsvItem getNext(@NotNull CSVReader csvReader, @Nullable CsvItem header) throws CsvValidationException, IOException {
        List<String> comments = new ArrayList<>();
        String[] columns = csvReader.readNext();
        if (columns == null) return null;

        int lineNo = (int) csvReader.getLinesRead();
        while (columns != null && isComment(columns)) {
            comments.add(columns[0]);
            columns = csvReader.readNext();
        }

        return new CsvItem(header, lineNo, columns == null ? EMPTY : columns, toArray(comments));
    }

    @NotNull
    public static List<CsvItem> getAll(@NotNull CSVReader csvReader, @Nullable CsvItem header) throws CsvValidationException, IOException {
        List<CsvItem> result = new ArrayList<>();
        CsvItem line;
        while ((line = getNext(csvReader, header)) != null)
        {
            result.add(line);
        }
        return result;
    }

    @Nullable private static String[] toArray(@Nullable List<String> comments) {
        if (comments == null || comments.size() == 0) return null;
        return comments.toArray(EMPTY);
    }

    public static boolean isComment(@NotNull String[] columns) {
        // empty line without content
        if (columns.length == 0) return true;

        // exactly one column that only contains blanks
        if (columns.length == 1 && columns[0].trim().length() == 0) return true;

        // comments start with "#" char
        return columns[0].startsWith("#");
    }
}
