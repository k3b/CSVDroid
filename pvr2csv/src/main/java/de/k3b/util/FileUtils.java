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
package de.k3b.util;

public class FileUtils {
    public static String fixFileName(String filename) {
        String outTxtName = filename;
        String illegalChars = "/\\:. ,;@|><\n\r\t";
        int len = illegalChars.length();
        for (int i = 0; i < len; i++) {
            outTxtName = outTxtName.replace(illegalChars.charAt(i), '_');
        }
        outTxtName = outTxtName
                .replace("ä","ae")
                .replace("Ä","Ae")
                .replace("ö","oe")
                .replace("Ö","Oe")
                .replace("ü","ue")
                .replace("Ü","Ue")
                .replace("ß","ss")
                .replace("__","_")
        ;
        return outTxtName;
    }


}
