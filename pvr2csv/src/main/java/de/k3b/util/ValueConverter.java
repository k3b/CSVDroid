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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** convert values from parsed source */
public class ValueConverter {
    public static boolean onErrorReturnOriginal = true;
    public static final SimpleDateFormat DATE_RFC3339 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
    public static final SimpleDateFormat DATE_RFC3339_SHORT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    /** translate unit to kilobytes */
    private static final int MEGA = 1024;
    private static final int GIGA = 1024 * MEGA;

    /** kilobytes must be between KILOBYTES_MIN and KILOBYTES_MAX. Else error */
    private static final int KILOBYTES_MIN = 10;
    private static final int KILOBYTES_MAX = 10 * MEGA * MEGA;

    /** minutes must be between MINUTES_MIN and MINUTES_MAX. Else error */
    private static final int MINUTES_MIN = 5;
    private static final int MINUTES_MAX = 600;

    /** date.year must be between YEAR_MIN and YEAR_MAX. Else error */
    private static final int YEAR_MIN = 1980;
    private static final int YEAR_MAX = 2080;

    public static String toUpper(String value) {
        if (value == null) return "";
        return value.toUpperCase(Locale.ROOT);
    }

    /**
     * Tries all formatters until a formatter matches.
     * @param formatters null or empty means use DATE_RFC3339 instead
     * @return value reformatted with first formatter.
     */
    public static String toDate(String value, DateFormat... formatters) {
        if (value == null || value.isEmpty()) return "";

        if (formatters == null || formatters.length == 0) {
            return toDate(value, DATE_RFC3339);
        }

        Date result = null;
        // TODO
        for (DateFormat formatter : formatters) {
            try {
                result = formatter.parse(value);
                break;
            } catch (ParseException e) {
                // ignore error
            }
        }
        if (result != null && getYear(result) >= YEAR_MIN && getYear(result) <= YEAR_MAX) {
            return formatters[0].format(result);
        }
        if (onErrorReturnOriginal) return value;
        return "";
    }

    private static int getYear(Date result) {
        // getYear(1980) returns "80"
        // getYear(2001) returns "101"
        return result.getYear() + 1900;
    }

    public static String toKilobytes(String value) {
        if (value == null) return "";
        double result = 0;
        try {
            String[] split = value.trim().split("\\s");
            result = Double.parseDouble(split[0]);
            if (split.length > 1) {
                String unit = split[1].toUpperCase(Locale.ROOT);
                if (unit.equals("MB")) result *= MEGA;
                else if (unit.equals("GB")) result *= GIGA;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result >= KILOBYTES_MIN && result <= KILOBYTES_MAX) {
            return "" + ((int) result);
        }
        if (onErrorReturnOriginal) return value;
        return "";
    }

    public static String toMinutes(String value) {
        String src = value;
        // ie "1'14" one hour, 14 minutes
        // ie "12:59 min" 12 minutes
        if (src == null) return "";

        int minuten = 0;
        try {
            int posSuffix = src.indexOf("min");
            if (posSuffix >= 0) {
                src = src.substring(0, posSuffix).trim();
                posSuffix = src.indexOf(':');
                if (posSuffix >= 0) {
                    minuten = Integer.parseInt(src.substring(0, posSuffix).trim());
                }
            } else {
                // ie 1:25 = 1 hour and 25 minutes
                String[] split = src.trim().split("[:']");

                minuten = Integer.parseInt(split[split.length - 1]);
                if (split.length > 1) {
                    minuten += 60 * Integer.parseInt(split[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (minuten >= MINUTES_MIN && minuten <= MINUTES_MAX) {
            return "" + minuten;
        }
        if (onErrorReturnOriginal) return value;
        return "";
    }

    public static String getLastModified(File file) {
        long lastModified = file.lastModified();
        if (lastModified == 0) return "";
        return DATE_RFC3339.format(new Date(lastModified));
    }

    public static String getAt(String[] parameters, int index) {
        if (parameters != null && index >= 0 && index < parameters.length) return parameters[index].trim();
        return "";
    }

}
