/*
 * Copyright (c) 2021-2023 by k3b.
 *
 * This file is part of CSVDroid (https://github.com/k3b/CSVDroid)
 *  and of AndroidGeo2ArticlesMap https://github.com/k3b/AndroidGeo2ArticlesMap .
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package de.k3b.android.csvdroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * Handles all permission releated stuff
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_ID_FILEIO = 24;

    // TODO change to WRITE_EXTERNAL_STORAGE once modifying csv is implemented
    private static final String PERMISSION_FILEIO = Manifest.permission.READ_EXTERNAL_STORAGE;

    private static final int RESULT_NO_PERMISSIONS = -22;

    private Bundle lastSavedInstanceState = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions(savedInstanceState);

        // call onCreateEx() when permissions are granted
    }

    private void checkPermissions(Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(this, PERMISSION_FILEIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(savedInstanceState, PERMISSION_FILEIO, PERMISSION_REQUEST_ID_FILEIO);
            return;
        }

        onCreateEx(savedInstanceState);
    }

    private void requestPermission(Bundle savedInstanceState, final String permission, final int requestCode) {
        lastSavedInstanceState = savedInstanceState;
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private boolean isGrantSuccess(int[] grantResults) {
        return (grantResults != null)
                && (grantResults.length > 0)
                && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID_FILEIO) {
            onRequestPermissionsResult(grantResults);
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onRequestPermissionsResult(int[] grantResults) {
        if (isGrantSuccess(grantResults)) {
            checkPermissions(lastSavedInstanceState);
        } else {
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            setResult(RESULT_NO_PERMISSIONS, null);
            finish();
        }
    }

    /** executed after {@link #onCreate(Bundle)} after all permsiions are granted */
    protected void onCreateEx(Bundle savedInstanceState) {
        this.lastSavedInstanceState = null;
    }
}
