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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import de.k3b.android.csvdroid.model.CsvItem;
import de.k3b.util.csv.CsvUtil;

public class CSVTableActivity extends AppCompatActivity {

    private static final String TAG = CSVTableActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_table);

        TableView mTableView = findViewById(R.id.tableview);

        CsvItem header = null;
        List<CsvItem> pojos = null;

        Uri sourceUri = getSourceUri(getIntent());
        if (sourceUri != null) {
            String csv = readAll(sourceUri);

            if (csv != null) {
                try (CSVReader csvReader = CsvUtil.openCsv(csv)) {
                    header = CsvUtil.getNext(csvReader, null);
                    pojos = CsvUtil.getAll(csvReader, header);
                } catch (Exception exception) {
                    showError(exception, "Cannot read from " + sourceUri);
                }
            }
        }

        if (header == null) {
            // no source file on app start. Use constant demo data instead
            header = TestData.createSampleHeader(8);
            pojos = TestData.createSampleData(header, 25);
            showError(null, "No Input CSV. Usining demo data instead.");
        }

        initializeTableView(mTableView, header, pojos);
    }


    @Nullable private String readAll(@Nullable Uri inUri) {
        if (inUri != null) {
            try (InputStream is = getContentResolver().openInputStream(inUri)) {
                return IOUtils.toString(is, Charset.defaultCharset());
            } catch (IOException exception) {
                showError(exception, "Cannot read from " + inUri);
            }
        }
        return null;
    }

    private void showError(Exception exception, String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Log.e(TAG, text, exception);
    }

    protected Uri getSourceUri(Intent intent) {
        if (intent == null) return null;

        return intent.getData();
    }

    private void initializeTableView(TableView mTableView, @NonNull CsvItem header,@NonNull List<CsvItem> pojos) {
        List<ColumnDefinition<CsvItem>> columnDefinitions = TestData.createColumnDefinitions(header);
        // Create TableView View model class  to group view models of TableView
        TableViewModel<CsvItem> tableViewModel = new TableViewModel<>(columnDefinitions, pojos);

        // Create TableView Adapter
        TableViewAdapter<CsvItem> tableViewAdapter = new TableViewAdapter<>();
        mTableView.setAdapter(tableViewAdapter);
        tableViewAdapter.setAllItems(tableViewModel);

        // mTableView.setTableViewListener(new TableViewListener(mTableView));

    }

}