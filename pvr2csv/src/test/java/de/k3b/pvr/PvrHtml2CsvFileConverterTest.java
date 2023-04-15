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

import static org.junit.Assert.*;

import org.junit.Test;

import de.k3b.util.ValueConverter;

public class PvrHtml2CsvFileConverterTest {
    @Test
    public void toDate() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("2008-12-29 09:21", PvrHtml2CsvFileConverter.toDate("29.12.2008, 09:21"));
        assertEquals("2008-12-29 09:21", PvrHtml2CsvFileConverter.toDate("Mo 29.12.2008, 09:21"));

        assertEquals("2019-01-02 04:37", PvrHtml2CsvFileConverter.toDate("Mo 2.1.2019, 4:37"));
        // error
        assertEquals("hallo world", PvrHtml2CsvFileConverter.toDate("hallo world"));
    }
}