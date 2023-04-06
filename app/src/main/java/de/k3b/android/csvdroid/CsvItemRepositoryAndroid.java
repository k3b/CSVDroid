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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import de.k3b.util.csv.CsvItem;
import de.k3b.util.csv.CsvItemRepository;

/**
 * Add android specific code to {@link CsvItemRepository}
 */
public class CsvItemRepositoryAndroid extends CsvItemRepository {
    private static final String TAG = CSVTableActivity.class.getSimpleName();

    private CsvItemRepositoryAndroid(@NotNull CsvItem header, @NotNull List<CsvItem> pojos) {
        super(header, pojos);
    }

    @Nullable
    public static CsvItemRepository createOrNull(@NotNull Activity activity) {
        return createOrNull(activity, getSourceUriOrNull(activity.getIntent()));
    }

    @Nullable
    public static CsvItemRepository createOrNull(@NotNull Context ctx, @Nullable Uri sourceUri) {
        if (sourceUri != null) {
            String csv = readAllOrNull(ctx, sourceUri);

            if (csv != null) {
                try {
                    return create(csv);
                } catch (Exception exception) {
                    showError(ctx, exception, "Cannot read from " + sourceUri);
                }
            }
        }
        return null;
    }

    @Nullable
    public static CsvItemRepository createOrThrow(@NotNull Context ctx, @Nullable Uri sourceUri) throws CsvValidationException, IOException {
        return create(readAllOrThrow(ctx, sourceUri));
    }

    @Nullable
    private static String readAllOrNull(Context ctx, @Nullable Uri inUri) {
        if (inUri != null) {
            try (InputStream is = ctx.getContentResolver().openInputStream(inUri)) {
                return readAllOrThrow(is);
            } catch (IOException exception) {
                showError(ctx, exception, "Cannot read from " + inUri);
            }
        }
        return null;
    }

    @NotNull private static String readAllOrThrow(Context ctx, @Nullable Uri inUri) throws IOException {
        return readAllOrThrow(ctx.getContentResolver().openInputStream(inUri));
    }

    @NotNull private static String readAllOrThrow(InputStream is) throws IOException {
        return IOUtils.toString(is, Charset.defaultCharset());
    }

    public static void showError(Context ctx, Exception exception, String text) {
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
        Log.e(TAG, text, exception);
    }

    protected static Uri getSourceUriOrNull(Intent intent) {
        Uri result = null; // not found
        if (intent != null) {
            // used by VIEW or EDIT
            result = intent.getData();

            // used by send(to)
            if (result == null) result = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
        return result;
    }

}
