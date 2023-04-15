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

public class ValueConverterTest {

    @Test
    public void toMinutes() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("74", ValueConverter.toMinutes("1'14"));
        assertEquals("12", ValueConverter.toMinutes("12:59 min"));

        // errorhandling
        assertEquals("error pass through", "hallo world", ValueConverter.toMinutes("hallo world"));

        ValueConverter.onErrorReturnOriginal = false;
        assertEquals("error hide", "", ValueConverter.toMinutes("hallo world"));

        assertEquals("", ValueConverter.toMinutes("-1:59 min."));
    }

    @Test
    public void toKilobytes() {
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("1", ValueConverter.toKilobytes("1"));
        assertEquals("10", ValueConverter.toKilobytes("10 kb"));
        assertEquals("1024", ValueConverter.toKilobytes("1 mb"));
        assertEquals("" + (1024 * 1024), ValueConverter.toKilobytes("1 gb"));

        assertEquals("" + (int) (1024 * 1.5), ValueConverter.toKilobytes("1.5 mb"));

        assertEquals("17", ValueConverter.toKilobytes("17 hallo world"));

        // errorhandling
        assertEquals("error pass through", "hallo world", ValueConverter.toKilobytes("hallo world"));

        ValueConverter.onErrorReturnOriginal = false;
        assertEquals("error hide", "", ValueConverter.toKilobytes("hallo world"));

        assertEquals("to small", "", ValueConverter.toKilobytes("1"));
    }

    @Test
    public void toDate() {
        ValueConverter.onErrorReturnOriginal = false;
        assertEquals("Can parse date", "2008-12-29 09:21", ValueConverter.toDate("2008-12-29 09:21"));

        // errorhandling
        ValueConverter.onErrorReturnOriginal = true;
        assertEquals("error pass through", "hallo world", ValueConverter.toDate("hallo world"));

        ValueConverter.onErrorReturnOriginal = false;
        assertEquals("error hide", "", ValueConverter.toDate("hallo world"));

    }
}