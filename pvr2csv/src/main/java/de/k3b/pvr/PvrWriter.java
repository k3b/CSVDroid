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

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Encapsulation of opencsv */
public class PvrWriter implements AutoCloseable {
    private final ICSVWriter writer;
    public PvrWriter(File outFile) throws IOException {
        this.writer = new CSVWriterBuilder(new FileWriter(outFile))
                .withSeparator(',')
                .withQuoteChar('"')
                .withLineEnd("\n")
                .build();
    }

    public void writeRow(String... columns) {
        writer.writeNext(columns, false);
    }

    @Override
    public void close() throws Exception {
        writer.flushQuietly();
        writer.close();
    }
}
