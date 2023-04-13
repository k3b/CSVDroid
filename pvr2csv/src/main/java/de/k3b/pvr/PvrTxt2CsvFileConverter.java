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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Extracts csv-data from extracted-pvr-txt files.
 * *.txt -> csv .
 *
 * film-txt-fileformat see PvrTxt.md
 */
public class PvrTxt2CsvFileConverter  implements AutoCloseable {
    public static final Charset FILM_TXT_ENCODING = Charset.forName("windows-1252");
    private final PvrWriter csvWriter;

    private final SimpleDateFormat DATE_RFC3339 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
            String dateLastModified = getLastModified(file);

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
                    toUpper(dvd), toUpper(source), toDate(dateRecorded),
                    title, toMinutes(minutes), info.toString(),
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

    public void writeHeader() {
        writeRow("relPath", "file",
                "dvd", "source", "dateRecorded", "title", " minutes",
                "info", "description", "dateLastModified");
    }

    private String toUpper(String source) {
        if (source == null) return "";
        return source.toUpperCase(Locale.ROOT);
    }

    private String toDate(String date) {
        if (date == null) return "";
        return date;
    }

    private String toMinutes(String minutes) {
        if (minutes == null) return "";
        String[] split = minutes.trim().split("[:']");

        int minuten = Integer.parseInt(split[split.length - 1]);
        if (split.length > 1) {
            minuten += 60 * Integer.parseInt(split[0]);
        }
        return "" + minuten;
    }

    private String getLastModified(File file) {
        long lastModified = file.lastModified();
        if (lastModified == 0) return "";
        return DATE_RFC3339.format(new Date(lastModified));
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
