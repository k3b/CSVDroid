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
package de.k3b.pvr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.k3b.util.ValueConverter;

/**
 * Extracts csv-data from extracted-pvr-txt files.
 * *.txt -> csv .
 *
 * film-txt-fileformat see PvrTxt.md
 */
public class PvrTxt2CsvFileConverter  implements AutoCloseable {
    public static final Charset FILM_TXT_ENCODING = Charset.forName("windows-1252");

    /**  "71224" -> "200"+"71224" */
    private static final String YEAR_INT8_PREFIX = "200";
    private  static final int YEAR_INT8LEN_MAX = "20071224".length();
    private  static final int YEAR_INT8LEN_MIN = YEAR_INT8LEN_MAX - YEAR_INT8_PREFIX.length(); // 5;

    private static final DateFormat DATE_INT8 = new SimpleDateFormat("yyyyMMdd", Locale.US);

    private static final DateFormat DATE_GERMAN = new SimpleDateFormat("d.M.y", Locale.US);

    private static final DateFormat[] DATE_FORMATS = {ValueConverter.DATE_RFC3339_SHORT, ValueConverter.DATE_RFC3339, DATE_GERMAN};

    private final PvrWriter csvWriter;

    // private final File csvOutFile;

    public PvrTxt2CsvFileConverter(File csvOutFile) throws IOException {
        // this.csvOutFile = csvOutFile;
        this.csvWriter = new PvrWriter(csvOutFile);
    }

    public void listFiles(File dir, String relPath) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            String lowerCase = fileName.toLowerCase(Locale.ROOT);
            if (file.isFile() && lowerCase.endsWith(".txt") && !lowerCase.contains(".sub.")) {
                writeFile(file, relPath);
            }
            if (file.isDirectory()) listFiles(file, relPath + fileName + "/");
        }
    }

    private void writeFile(File file, String relPath) {
        // System.out.println(relPath + file.getName());

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file), FILM_TXT_ENCODING))) {
            String dateLastModified = ValueConverter.getLastModified(file);

            String dvd = readLine(in, null);

            String[] columns = readColumns(in, "");
            String source = columns.length > 0 ? columns[0] : null;
            String dateRecorded = columns.length > 1 ? columns[1] : null;

            columns = readColumns(in, "");
            String title = columns.length > 0 ? columns[0] : null;
            String minutes = columns.length > 1 ? columns[1] : null;

            StringBuilder info = new StringBuilder();
            String infoLine = readLine(in, null);
            while (infoLine != null && !infoLine.isEmpty()) {
                if (info.length() != 0) info.append('\n');
                info.append(infoLine);
                infoLine = readLine(in, null);
            }

            StringBuilder description = new StringBuilder();
            String descriptionLine = in.readLine();
            while (descriptionLine != null) {
                if (description.length() != 0) description.append('\n');
                description.append(descriptionLine);
                descriptionLine = in.readLine();
            }

            writeRow(relPath, file.getName(),
                    ValueConverter.toUpper(dvd), ValueConverter.toUpper(source), toDate(dateRecorded),
                    title, ValueConverter.toMinutes(minutes), info.toString(),
                    description.toString(), dateLastModified);
            /*
            new ToPvrTxtFileConverter(csvOutFile).writeTxtFile(relPath + file.getName(),
                    toUpper(source), toDate(dateRecorded),
                    title, toMinutes(minutes), info.toString(),
                    description.toString(), file.lastModified());
             */
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected static String toDate(String value) {
        Date result = null;

        // 71021 -> 20071021 -> 2007-10-21
        try {
            String srcValue = "" + Integer.parseInt(value);
            int intLen = srcValue.length();
            if (intLen >= YEAR_INT8LEN_MIN && intLen < YEAR_INT8LEN_MAX) {
                srcValue = YEAR_INT8_PREFIX.substring(0, YEAR_INT8LEN_MAX - intLen) + srcValue;
            }
            result = DATE_INT8.parse(srcValue);
        } catch (NumberFormatException | ParseException e) {
            result = null;
        }

        if (result == null) {
            return ValueConverter.toDate(value, DATE_FORMATS);
        } else {
            return ValueConverter.DATE_RFC3339_SHORT.format(result);
        }
    }

    public void writeHeader() {
        writeRow("relPath", "file",
                "dvd", "source", "dateRecorded", "title", " minutes",
                "info", "description", "dateLastModified");
    }

    private String[] readColumns(BufferedReader in, String notFoundValue) throws IOException {
        return readLine(in, notFoundValue).split(";");
    }

    private String readLine(BufferedReader in, String notFoundValue) throws IOException {
        // return in.readLine().trim();
        String line = in.readLine();
        return line == null ? notFoundValue : line;
    }

    public void writeRow(String... columns) {
        csvWriter.writeRow(columns);
    }

    @Override
    public void close() throws Exception {
        csvWriter.close();
    }
}
