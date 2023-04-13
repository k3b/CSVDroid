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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import de.k3b.util.FileUtils;

/**
 * Generates pvr-txt files.
 *
 * film-txt-fileformat see PvrTxt.md
 */
public class ToPvrTxtFileConverter {
    public static final Charset FILM_TXT_ENCODING = Charset.forName("windows-1252");
    private final File txtOutDir;

    public ToPvrTxtFileConverter(File csvOutFile)  {
        this.txtOutDir = new File(csvOutFile.getAbsoluteFile().getParentFile(), "generated");
        this.txtOutDir.mkdirs();
    }

    public void writeTxtFile(String filename, String source,
                             String dateRecorded, String title, String minutes,
                             String info, String description, long dateLastModified)
            throws IOException
    {
        String outTxtName = FileUtils.fixFileName(filename);
        File outFile = new File(txtOutDir, outTxtName + ".txt");
        File outFileBak = new File(txtOutDir, outTxtName + ".txt.bak");
        File outFileBakDel = new File(txtOutDir, outTxtName + ".txt.bak.del");

        boolean success = true;
        if (success && outFileBakDel.exists()) success = outFileBakDel.delete();
        if (success && outFileBak.exists()) success = outFileBak.renameTo(outFileBakDel);
        if (success && outFile.exists()) success = outFile.renameTo(outFileBak);

        if (success) {
            try (PrintStream out = new PrintStream(outFile, FILM_TXT_ENCODING.name())) {
                out.println("?? " + outFile.getName()); // dvd
                out.print(source); // i.e. ZDF
                out.print(";");
                out.println(dateRecorded);
                out.print(title);
                out.print(";");
                out.println(minutes); // i.e. 2'04
                if (info != null && info.length() > 0) out.println(info);
                out.println(); // empty before description
                out.println(description);
            } catch (IOException ex) {
                success = false;
            }
            if (dateLastModified != 0) {
                outFile.setLastModified(dateLastModified);
            }
            if (success && outFileBakDel.exists()) success = outFileBakDel.delete();
        }
        if (!success) {
            // rollback
            if (outFile.exists()) outFile.delete();
            if (outFileBak.exists()) outFileBak.renameTo(outFile);
            if (outFileBakDel.exists()) outFileBakDel.renameTo(outFileBak);
        }
    }
}
