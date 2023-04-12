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
 * Konvertiert Video-Beschreibungsdateien *.txt -> csv .
 *
 * Aufbau der Quelldatei
 *
 * * dvd
 * * source;date
 * * title;minutes
 * * [info]
 * * [leerzeile]
 * * description...
 *
 * Example
 * * dvd_divx_Filme_01
 * * ZDF;7.4.2006
 * * James Bond - 007 jagt Dr. No;2'04
 * * Spielfilm Großbritannien 1985
 * *
 * * Superagent James Bond hat eine...
 */
public class PvrTxt2CsvRunner {
    static String root = "C:/Users/eve/StudioProjects/github/CSVDroid/pvr2csv/downloads/cds_dvds";

    public static void main(String[] args) throws Exception {
        // System.out.println("hello world");

        File dir = new File(root);
        // System.out.println("Lade " + dir.getAbsolutePath());
        try(PvrTxt2CsvFileConverter converter = new PvrTxt2CsvFileConverter(new File(dir, "../PvrTxt.csv"))) {

            converter.writeHeader();
            converter.listFiles(dir, "");
        }
    }
}
