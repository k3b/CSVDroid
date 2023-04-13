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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Extracts csv-data from pvr web gui.
 */
public class PvrHtml2CsvFileConverter  implements AutoCloseable {
    private static final Document.OutputSettings NO_REFORMATTING = new Document.OutputSettings().prettyPrint(false);
    private static final Safelist REMOVE_ALL_HTML = Safelist.none();
    public static final String[] EMPTY = new String[0];
    public static final String URL_PARAM_FILE = "file=";
    private final PvrWriter writer;

    private final SimpleDateFormat DATE_RFC3339 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private final Set<String> titles = new HashSet<>();
    private final ToPvrTxtFileConverter toPvrTxtFileConverter;

    public PvrHtml2CsvFileConverter(File outFile) throws IOException {
        this.writer = new PvrWriter(outFile);
        toPvrTxtFileConverter = new ToPvrTxtFileConverter(outFile);
    }


    public void listFiles(File dir, String relPath) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            String lowerCase = fileName.toLowerCase(Locale.ROOT);
            if (file.isFile() && (lowerCase.endsWith(".htm") || lowerCase.endsWith(".html"))) {
                writeFile(file, relPath);
            }
            if (file.isDirectory()) listFiles(file, relPath + fileName + "/");
        }
    }

    private void writeFile(File file, String relPath) {
        try {
            String dateLastModified = getLastModified(file);
            // new Simple
            Document doc = Jsoup.parse(file, "UTF-8").outputSettings(NO_REFORMATTING);
            Elements movies = doc.select("#movies"); // #id
            Elements rows = doc.select(".tm_row"); // .css-class

            for (Element row : rows) {
                // <div class="tm_row ..." id="0" data-title="James Bond 007 - Der Hauch des Todes" data-start="1564003920">
                // <span class="tm_title">James Bond 007 - Der Hauch des Todes</span><br> NITRO / Wed 24.7, 23:32<br> 122:38 min. / 3.05 GB
                // <span class="tm_desc">James Bond hilft dem KGB-General Georgi ...
                String title = row.attr("data-title");
                String description = getText(row.select(".tm_desc"));

                String[] parameters = getSiblingSplit(row, ".tm_title", "[\\n/]");
                String source = get(parameters, 0);
                String dateRecorded = get(parameters, 1);
                String minutes = get(parameters, 2);
                String kilobytes = get(parameters, 3);

                String url = getUrl(row);

                title = fixDiscription(description, title);
                writeRow(relPath, file.getName(),
                        // toUpper(dvd),
                        source, dateRecorded,
                        title,
                        minutes, kilobytes,
                        description,dateLastModified, url);

                toPvrTxtFileConverter.writeTxtFile(relPath + file.getName() + "_" + title,
                        source, dateRecorded,
                        title, minutes, null,
                        description, file.lastModified());
            }

            // System.out.println("done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add first line of description to title if
     * * title is not unique or
     * * title ends with a digit
     */
    private String fixDiscription(String description, String title) {
        if (description != null && description.length() > 0 && title.length() > 1 &&
                (titles.contains(title) || (Character.isDigit(title.charAt(title.length() - 1))))) {
            // if title is not unique append episode (first line from discription)
            String[] lines = description.split("\n");
            title += " - " + lines[0];
        } else {
            titles.add(title);
        }
        return title;
    }

    public void writeHeader() {
        writeRow("relPath", "file",
                // "dvd",
                "source", "dateRecorded",
                "title",
                " minutes", "kilobytes",
                "description","dateLastModified","url");
    }

    private String getUrl(Element row) {
        Element a = row.selectFirst("a[href*=download]");
        if (a == null) return "";
        String url = a.attr("href");
        int pos = url.indexOf(URL_PARAM_FILE);
        if (pos >= 0) return url.substring(pos + URL_PARAM_FILE.length());
        return url;
    }

    private String get(String[] parameters, int index) {
        if (parameters != null && index >= 0 && index < parameters.length) return parameters[index].trim();
        return "";
    }

    private String getLastModified(File file) {
        long lastModified = file.lastModified();
        if (lastModified == 0) return "";
        return DATE_RFC3339.format(new Date(lastModified));
    }
    private String[] getSiblingSplit(Element row, String cssQuery, String splitRexExp) {
        Element titleElement = row.selectFirst(cssQuery);
        if (titleElement != null) {
            int offset = titleElement.text().trim().length();
            String textAfterElement = getText(titleElement.parent().html()).trim().substring(offset);
            return textAfterElement.split(splitRexExp);
        }
        return EMPTY;
    }

    private String getText(Elements elements) {
        if (elements == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            if (sb.length() != 0)
                sb.append("\n");
            sb.append(getText(element.html()));
        }

        // https://stackoverflow.com/questions/5640334/how-do-i-preserve-line-breaks-when-using-jsoup-to-convert-html-to-plain-text
        return sb.toString();
    }

    private String getText(String html) {
        return Jsoup.clean(html, "", REMOVE_ALL_HTML, NO_REFORMATTING);
    }

    public void writeRow(String... columns) {
        writer.writeRow(columns);
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }
}
