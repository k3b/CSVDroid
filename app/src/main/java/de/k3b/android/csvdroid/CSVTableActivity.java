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
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.k3b.android.csvdroid.view.CsvListViewModel;
import de.k3b.util.csv.CsvItem;

public class CSVTableActivity extends BaseActivity {
    /** used for logging */
    private static final String TAG = CSVTableActivity.class.getSimpleName();
    private static final int REQUEST_ID_PICK_CSV_FILE = 2;
    private static final String[] SUPPORTED_MIME_TYPES = {
            // standard
            "text/csv",
            // used by android
            "text/comma-separated-values"
    };
    private static final int SEARCH_DELAY_MILLIS = 800;
    private TableView tableView;

    private CsvListViewModel viewModel;
    private final Handler delayTimerForSearch = new Handler();

    private TableViewAdapter<CsvItem> tableViewAdapter;
    private List<ColumnDefinition<CsvItem>> columnDefinitions;

    /**
     * This is the permission granted replacement for {@link #onCreate(Bundle)}
     */
    @Override
    protected void onCreateEx(Bundle savedInstanceState) {
        super.onCreateEx(savedInstanceState);
        setContentView(R.layout.activity_csv_table);
        tableView = findViewById(R.id.tableview);

        viewModel = new ViewModelProvider(this)
                .get(CsvListViewModel.class);

        List<CsvItem> itemList = viewModel.getCsvList().getValue();
        if (itemList == null || itemList.isEmpty()) {
            // initial app start: uri comes either
            // (1) from intent
            // or (2) from last used
            // or (3) ask user

            // (1)
            Uri currentCsvFileUri = CsvItemRepositoryAndroid.getSourceUriOrNull(getIntent());

            // (2)
            if (currentCsvFileUri == null) currentCsvFileUri = viewModel.loadLastUsedUri();

            if (currentCsvFileUri != null) {
                executeLoadCsvFile(currentCsvFileUri);
            } else {
                // (3) ask user
                openCsvFilePicker();
            }
        }

        viewModel.getCsvList().observe(this, this::initializeTableView);
        viewModel.getStatus().observe(this, this::setStatus);
    }

    private void setStatus(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tableview, menu);

        MenuItem i =  menu.findItem(R.id.cmd_search_bar);
        SearchView searchView = (SearchView) i.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setQuery(viewModel.getSearchTerm().getValue(), false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchTerm) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newSearchTerm) {
                delayTimerForSearch.removeCallbacksAndMessages(null);
                delayTimerForSearch.postDelayed(() -> viewModel.executeSearch(newSearchTerm), SEARCH_DELAY_MILLIS);
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
        if (requestCode == REQUEST_ID_PICK_CSV_FILE) {
            onOpenCsvFilePickerResult(resultCode,resultData);
            return;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Ask the user for a CSV-file to be loaded
     */
    private void openCsvFilePicker() {
        // from https://developer.android.com/training/data-storage/shared/documents-files#java
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT) // .ACTION_GET_CONTENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_TITLE, getString(R.string.title_open_file))
                .setType("text/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_MIME_TYPES) // since api 19: multiple .setType()
                ;

        Log.d(TAG,"openCsvFilePicker " + intent.toString() + " Extras " + intent.getExtras());

        // to replace the deprecated simple solution see
        // https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        //noinspection deprecation
        startActivityForResult(intent, REQUEST_ID_PICK_CSV_FILE);
        // continues at onOpenCsvFilePickerResult() ...
    }

    /**
     * process the response from {@link #openCsvFilePicker()}
     */
    private void onOpenCsvFilePickerResult(int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK
                && resultData != null
                && resultData.getData() != null) {
            // from https://developer.android.com/training/data-storage/shared/documents-files#java
            executeLoadCsvFile(resultData.getData());
        } else {
            // PICK_CSV_FILE canceled. Use demo data instead
            loadDemoData();
        }

    }

    private void loadDemoData() {
        Log.d(TAG,"loadDemoData()");

        String[] header = TestData.createSampleHeader(8);
        List<CsvItem> csvItemList = TestData.createSampleData(header, 25);

        viewModel.load(header, csvItemList);
        showFilename(null);
    }

    private void executeLoadCsvFile(@NonNull final Uri data) {
        Log.d(TAG,"executeLoadCsvFile: Loading csv from " + data);

        CsvDroidApp.executor.execute(() -> viewModel.load(data));
        showFilename(data.toString());
    }

    private void showFilename(@Nullable String fileName) {
        setTitle(getString(R.string.app_name) + " " + viewModel.getFilename(fileName));
    }


    private void initializeTableView(@NonNull List<CsvItem> csvItemList) {

        // Create TableView Adapter
        tableViewAdapter = new TableViewAdapter<>();
        tableView.setAdapter(tableViewAdapter);

        if (viewModel != null) {
            columnDefinitions = TestData.createColumnDefinitions(viewModel.getHeader());
            setTableItems(csvItemList);
        }

        tableView.setTableViewListener(new TableViewListener(tableView));
    }

    private void setTableItems(@NonNull List<CsvItem> csvItemList) {
        TableViewModel<CsvItem> tableViewModel = new TableViewModel<>(columnDefinitions, csvItemList);
        tableViewAdapter.setAllItems(tableViewModel.getColumnHeaderList(), tableViewModel
                .getRowHeaderList(), tableViewModel.getCellList());
    }
}