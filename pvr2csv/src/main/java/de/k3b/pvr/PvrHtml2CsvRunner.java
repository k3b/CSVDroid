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

import java.io.File;

/**
 * Extracts csv-data from pvr web gui.
 */
public class PvrHtml2CsvRunner {
    // Dir where downloaded htm(l) files are loaded from
    static String root = "C:/Users/eve/StudioProjects/github/CSVDroid/pvr2csv/downloads/hd51-download/";

    public static void main(String[] args) throws Exception {
        File dir = new File(root);

        try(PvrHtml2CsvFileConverter converter = new PvrHtml2CsvFileConverter(new File(dir, "../PvrHtm.csv"))) {
            converter.writeHeader();
            converter.listFiles(dir, "");
        }
    }


}
