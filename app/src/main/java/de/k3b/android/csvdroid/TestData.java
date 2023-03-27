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

import androidx.annotation.NonNull;

import com.evrencoskun.tableview.model.ColumnDefinition;

import java.util.ArrayList;
import java.util.List;

import de.k3b.android.csvdroid.model.CsvItem;

/**
 * This is where all Testdata for the demo app comes from.
 *
 * Created by k3b on 2023-03-25
 */

public class TestData {
    public static List<CsvItem> createSampleData(@NonNull CsvItem header, int rowCount) {
        List<CsvItem> sampleData = new ArrayList<>();
        for(int itemNumber = 0; itemNumber < rowCount; itemNumber++) {
            sampleData.add(createSampleItem(header, itemNumber));
        }
        return sampleData;
    }

    public static CsvItem createSampleHeader(int columnCount) {
        String[] columns = new String[columnCount];
        for(int i = 0; i < columnCount; i++) {
            columns[i] = "Col " + i;
        }
        return new CsvItem(null, -1, columns, null);
    }

    @NonNull private static CsvItem createSampleItem(@NonNull CsvItem header, int itemNumber) {
        int colCount = header.getColumnCount();
        String[] columns = new String[colCount];
        for(int i = 0; i < colCount; i++) {
            columns[i] = "Item " + itemNumber +
                    " " + i;
        }
        return new CsvItem(header, itemNumber, columns, new String[]{"Comment for Item " + itemNumber});
    }

    public static List<ColumnDefinition<CsvItem>> createColumnDefinitions(@NonNull CsvItem header) {
        List<ColumnDefinition<CsvItem>> definitions = new ArrayList<>();
        int colCount = header.getColumnCount();
        for(int i = 0; i < colCount; i++) {
            final int colNo = i;
            definitions.add(new ColumnDefinition<>(header.getColumn(i, ""), r -> r.getColumn(colNo, "")));
        }
        definitions.add(new ColumnDefinition<>("Comments", CsvItem::getComments));
        return definitions;
    }
}
