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

import org.apache.commons.lang3.ArrayUtils;

public class StringUtils {
    /**
     * Modified version of org.apache.commons.lang3.StringUtils#indexOfAny.
     * <p>
     * Same as {@link org.apache.commons.lang3.StringUtils#indexOfAny(CharSequence, char...)
     * where you can specify the search intervall}
     * License of this function is Apache2
     */
    public static int indexOfAny(final CharSequence cs, int csFirst, int csLen, final char... searchChars) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return org.apache.commons.lang3.StringUtils.INDEX_NOT_FOUND;
        }
        final int csLast = csLen - 1;
        final int searchLen = searchChars.length;
        final int searchLast = searchLen - 1;
        for (int i = csFirst; i < csLen; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLen; j++) {
                if (searchChars[j] == ch) {
                    if (i < csLast && j < searchLast && Character.isHighSurrogate(ch)) {
                        // ch is a supplementary character
                        if (searchChars[j + 1] == cs.charAt(i + 1)) {
                            return i;
                        }
                    } else {
                        return i;
                    }
                }
            }
        }
        return org.apache.commons.lang3.StringUtils.INDEX_NOT_FOUND;
    }
}