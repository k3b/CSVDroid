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
package de.k3b.util.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.k3b.util.IRepository;

/**
 * Manage the csv-file content:
 * * {@link #getHeader()}
 * * {@link #getPojos()}
 * * {@link #getPojos(String)} for searchfilter
 */
public class CsvItemRepository implements IRepository<CsvItem> {
    @NotNull private final String[] header;
    @NotNull private final List<CsvItem> pojos;

    public CsvItemRepository(@NotNull String csv) throws CsvValidationException, IOException {
        try (CSVReader csvReader = CsvUtil.openCsvOrThrow(csv)) {
            CsvItem csvHeader = CsvUtil.getNext(csvReader, null);
            String[] header = csvHeader == null ? null : csvHeader.getColumns();
            if (header == null || header.length == 0) {
                throw new CsvValidationException("Empty/no Header");
            }
            this.header = header;
            this.pojos = CsvUtil.getAll(csvReader, this.header);
        }
    }

    public CsvItemRepository(@NotNull String[] header, @NotNull List<CsvItem> pojos) {
        this.header = header;
        this.pojos = pojos;
    }

    @NotNull
    public String[] getHeader() {
        return header;
    }

    @NotNull public List<CsvItem> getPojos() {
        return pojos;
    }

    @NotNull public List<CsvItem> getPojos(@Nullable String searchTerm) {
        if (searchTerm == null) return pojos;
        String searchTermLC = searchTerm.trim().toLowerCase(Locale.ROOT);
        if (searchTermLC.isEmpty()) return pojos;

        List<CsvItem> result = new ArrayList<>();
        for (CsvItem p : pojos) {
            if (p != null && p.contains(searchTermLC)) result.add(p);
        }
        return result;
    }
}
