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

import static org.junit.Assert.*;

import org.junit.Test;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import de.k3b.pvr.PvrHtml2CsvFileConverter;

public class ValueConverterTest {

    @Test
    public void toMinutes() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("74", ValueConverter.toMinutes("1'14"));
        assertEquals("12", ValueConverter.toMinutes("12:59 min"));

        // errors
        assertEquals("-1:59 min.", ValueConverter.toMinutes("-1:59 min."));
        assertEquals("hallo world", ValueConverter.toMinutes("hallo world"));
    }

    @Test
    public void toKilobytes() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("1", ValueConverter.toKilobytes("1"));
        assertEquals("10", ValueConverter.toKilobytes("10 kb"));
        assertEquals("1024", ValueConverter.toKilobytes("1 mb"));
        assertEquals("" + (1024*1024), ValueConverter.toKilobytes("1 gb"));

        assertEquals("1536", ValueConverter.toKilobytes("1.5 mb"));

        assertEquals("17", ValueConverter.toKilobytes("17 hallo world"));


        // errors
        assertEquals("hallo world", ValueConverter.toKilobytes("hallo world"));
    }

    @Test
    public void toDate() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("2008-12-29 09:21", ValueConverter.toDate("2008-12-29 09:21"));

        String d = PvrHtml2CsvFileConverter.DATE_PVR_WITH_YEAR.format(new Date(2008,12 - 1,29,9,21));

        assertEquals("2008-12-29 09:21", PvrHtml2CsvFileConverter.toDate("29.12.2008, 09:21"));
        assertEquals("2008-12-29 09:21", PvrHtml2CsvFileConverter.toDate("Mo 29.12.2008, 09:21"));

        assertEquals("2019-01-02 04:37", PvrHtml2CsvFileConverter.toDate("Mo 2.1.2019, 4:37"));
        // error
        assertEquals("hallo world", PvrHtml2CsvFileConverter.toDate("hallo world"));
    }
}