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
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import de.k3b.util.csv.CsvItem;
import de.k3b.util.csv.CsvItemRepository;

public class CSVTableActivity extends BaseActivity {
    private static final String TAG = CSVTableActivity.class.getSimpleName();
    private static final int PICK_CSV_FILE = 2;
    public static final String KEY_CSV_FILE_URI = "csvFileUri";
    public static final String[] SUPPORTED_MIME_TYPES = {
            "text/csv", "text/comma-separated-values"};

    private final Handler delayTimerForSearch = new Handler();

    private TableViewAdapter<CsvItem> tableViewAdapter;
    private List<ColumnDefinition<CsvItem>> columnDefinitions;
    private CsvItemRepository repository;

    // persisted through InstanceState[KEY_CSV_FILE_URI] and SharedPreferences[KEY_CSV_FILE_URI]
    private Uri currentCsvFileUri = null;

    /**
     * This is the permission granted replacement for {@link #onCreate(Bundle)}
     */
    @Override
    protected void onCreateEx(Bundle savedInstanceState) {
        super.onCreateEx(savedInstanceState);
        setContentView(R.layout.activity_csv_table);

        repository = null;
        if (savedInstanceState == null) {
            // initial app start: uri comes either (1) from intent
            // or (2) from last used
            // or (3) ask user

            // (1)
            currentCsvFileUri = CsvItemRepositoryAndroid.getSourceUriOrNull(getIntent());

            // (2)
            if (currentCsvFileUri == null) currentCsvFileUri = getLastUsedUri();

            try {
                repository = CsvItemRepositoryAndroid.createOrThrow(this, currentCsvFileUri);
            } catch (Exception e) {
                // ignore error: csvFileUri==null or cannot read/parse file
            }

            // (3)
            if (repository == null) openCsvFilePicker();
        } else {
            // ie after screen rotation
            currentCsvFileUri = savedInstanceState.getParcelable(KEY_CSV_FILE_URI);
            repository = CsvItemRepositoryAndroid.createOrNull(this, currentCsvFileUri);
        }

        initializeTableView(findViewById(R.id.tableview), currentCsvFileUri);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_CSV_FILE_URI, currentCsvFileUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tableview, menu);

        MenuItem i =  menu.findItem(R.id.cmd_search_bar);
        SearchView searchView = (SearchView) i.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchTerm) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchTerm) {
                delayTimerForSearch.removeCallbacksAndMessages(null);
                delayTimerForSearch.postDelayed(() -> setTableFilter(searchTerm), 300);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.cmd_file_open) {
            openCsvFilePicker();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == PICK_CSV_FILE) {
            if (resultCode == Activity.RESULT_OK
                    && resultData != null
                    && resultData.getData() != null) {
                // from https://developer.android.com/training/data-storage/shared/documents-files#java
                onOpenCsvFilePickerResult(resultData.getData());
            } else {
                // PICK_CSV_FILE canceled. Use demo data instead
                loadDemoData();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void openCsvFilePicker() {
        // from https://developer.android.com/training/data-storage/shared/documents-files#java
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT) // .ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, getString(R.string.title_open_file))
                .setType("text/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_MIME_TYPES) // since api 19: multible .setType()
                ;

        /* does not work with material files :-(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            String lastUsed = PreferenceManager
                    .getDefaultSharedPreferences(this.getApplicationContext())
                            .getString(KEY_CSV_FILE_URI, null);
            if (lastUsed != null) {
                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(lastUsed));
            }
        }
         */

        Log.d(TAG,intent.toString() + " Extras " + intent.getExtras());

        // to replace the deprecated simple solution see
        // https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        //noinspection deprecation
        startActivityForResult(intent, PICK_CSV_FILE);
    }

    private void onOpenCsvFilePickerResult(Uri data) {
        if (data != null) {
            getContentResolver().takePersistableUriPermission(data, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            currentCsvFileUri = data;
            saveLastUsedUri(data);
        }
        repository = CsvItemRepositoryAndroid.createOrNull(this, data);
        initializeTableView(findViewById(R.id.tableview), data);
    }

    private void saveLastUsedUri(Uri data) {
        PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext())
                .edit()
                .putString(KEY_CSV_FILE_URI, data.toString())
                .apply();
    }

    private Uri getLastUsedUri() {
        String lastUsed = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext())
                .getString(KEY_CSV_FILE_URI, null);
        return (lastUsed != null) ? Uri.parse(lastUsed) : null;
    }

    private void loadDemoData() {
        CsvItem header = TestData.createSampleHeader(8);
        List<CsvItem> pojos = TestData.createSampleData(header, 25);

        repository = new CsvItemRepository(header, pojos);
        CsvItemRepositoryAndroid.showError(this,null, "No Input CSV. Using demo data instead.");
        initializeTableView(findViewById(R.id.tableview), null);
    }

    private void initializeTableView(TableView tableView, Uri fileName) {

        // Create TableView Adapter
        tableViewAdapter = new TableViewAdapter<>();
        tableView.setAdapter(tableViewAdapter);

        if (repository != null) {
            columnDefinitions = TestData.createColumnDefinitions(repository.getHeader());
            setTableFilter(null);
        }

        tableView.setTableViewListener(new TableViewListener(tableView));

        String path = "demo";
        if (fileName != null) {
            path = urlDecode(urlDecode(fileName.toString()));
            int pos = path.lastIndexOf('/') + 1;
            if (pos > 1) path = path.substring(pos);
        }
        setTitle(getString(R.string.app_name) + " " + path);
    }

    private String urlDecode(String fileName) {
        try {
            return URLDecoder.decode(fileName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return fileName;
        }
    }

    private void setTableFilter(String searchTerm) {
        TableViewModel<CsvItem> tableViewModel = new TableViewModel<>(columnDefinitions, repository.getPojos(searchTerm));
        tableViewAdapter.setAllItems(tableViewModel.getColumnHeaderList(), tableViewModel
                .getRowHeaderList(), tableViewModel.getCellList());
    }
}