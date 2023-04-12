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

import java.util.Date;

public class VideoDTO {
    private final String dvd;
    private final String source;
    private final Date date;
    private final String title;
    private final String info;
    private final int minutes;
    private final String description;
    private final int megabytes;

    public VideoDTO(String dvd, String source, Date date, String title, int minutes, String info, String description, int megabytes) {
        this.dvd = dvd;
        this.source = source;
        this.date = date;
        this.title = title;
        this.info = info;
        this.minutes = minutes;
        this.description = description;
        this.megabytes = megabytes;
    }

    public String getDvd() {
        return dvd;
    }

    public String getSource() {
        return source;
    }

    public Date getDate() {
        return date;
    }

    public String getTilte() {
        return title;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getInfo() {
        return info;
    }

    public String getDescription() {
        return description;
    }

    public int getMegabytes() {
        return megabytes;
    }

}
