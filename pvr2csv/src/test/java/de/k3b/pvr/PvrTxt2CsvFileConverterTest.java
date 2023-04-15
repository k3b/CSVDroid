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

public class PvrTxt2CsvFileConverterTest {

    @Test
    public void toDate() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("2007-10-21", PvrTxt2CsvFileConverter.toDate("71021"));
        assertEquals("2007-10-21", PvrTxt2CsvFileConverter.toDate("20071021"));

        assertEquals("2004-08-14", PvrTxt2CsvFileConverter.toDate("2004-08-14 22:51"));

        assertEquals("2007-12-31", PvrTxt2CsvFileConverter.toDate("31.12.2007"));
        assertEquals("2007-01-01", PvrTxt2CsvFileConverter.toDate("1.1.2007"));

        ValueConverter.onErrorReturnOriginal = false;
        assertEquals("2004-08-14", PvrTxt2CsvFileConverter.toDate("2004-08-14"));
    }
}