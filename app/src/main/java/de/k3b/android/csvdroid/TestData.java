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

package de.k3b.android.csvdroid;

import com.evrencoskun.tableview.model.ColumnDefinition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.k3b.util.csv.CsvItem;

/**
 * This is where all Testdata for the demo app comes from.
 *
 * Created by k3b on 2023-03-25
 */

public class TestData {
    @NotNull public static List<CsvItem> createSampleData(@NotNull String[] header, int rowCount) {
        List<CsvItem> sampleData = new ArrayList<>();
        for(int itemNumber = 0; itemNumber < rowCount; itemNumber++) {
            sampleData.add(createSampleItem(header, itemNumber));
        }
        return sampleData;
    }

    @NotNull public static String[]  createSampleHeader(int columnCount) {
        String[] columns = new String[columnCount];
        for(int i = 0; i < columnCount; i++) {
            columns[i] = "Col " + i;
        }
        return columns;
    }

    @NotNull private static CsvItem createSampleItem(@NotNull String[] header, int itemNumber) {
        int colCount = header.length;
        String[] columns = new String[colCount];
        for(int i = 0; i < colCount; i++) {
            columns[i] = "Item " + itemNumber +
                    " " + i;
        }
        return new CsvItem(header, itemNumber, columns, new String[]{"Comment for Item " + itemNumber});
    }

    @NotNull public static List<ColumnDefinition<CsvItem>> createColumnDefinitions(@NotNull String[] header) {
        List<ColumnDefinition<CsvItem>> definitions = new ArrayList<>();
        int colCount = header.length;
        for(int i = 0; i < colCount; i++) {
            final int colNo = i;
            definitions.add(new ColumnDefinition<>(header[i], r -> max(r.getColumn(colNo, ""),30)));
        }
        definitions.add(new ColumnDefinition<>("Comments", r -> max(r.getComments(),25)));
        return definitions;
    }

    @NotNull private static String max(@NotNull String text, int maxLen) {
        if (text.length() > maxLen) text = text.substring(0,maxLen - 2) + " …";
        return text;
    }
}
