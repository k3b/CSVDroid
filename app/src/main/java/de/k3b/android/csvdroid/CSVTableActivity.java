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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.Menu;
import android.view.MenuItem;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.k3b.util.csv.CsvItem;
import de.k3b.util.csv.CsvItemRepository;

public class CSVTableActivity extends BaseActivity {
    private static final String TAG = CSVTableActivity.class.getSimpleName();
    private static final int VIEW_CSV_FILE = 2;

    private final Handler delayTimerForSearch = new Handler();

    private TableViewAdapter<CsvItem> tableViewAdapter;
    private List<ColumnDefinition<CsvItem>> columnDefinitions;
    private CsvItemRepository repository;

    /**
     * This is the permission granted replacement for {@link #onCreate(Bundle)}
     */
    @Override
    protected void onCreateEx(Bundle savedInstanceState) {
        super.onCreateEx(savedInstanceState);
        setContentView(R.layout.activity_csv_table);

        repository = CsvItemRepositoryAndroid.create (this);

        if (repository == null) {
            // no source file on app start. Use constant demo data instead
            CsvItem header = TestData.createSampleHeader(8);
            List<CsvItem> pojos = TestData.createSampleData(header, 25);

            repository = new CsvItemRepository(header, pojos);
            CsvItemRepositoryAndroid.showError(this,null, "No Input CSV. Using demo data instead.");
        }

        initializeTableView(findViewById(R.id.tableview));
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
                delayTimerForSearch.postDelayed(() -> filterItems(searchTerm), 300);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.cmd_file_open) {
            openFile(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == VIEW_CSV_FILE
                && resultCode == Activity.RESULT_OK
                && resultData != null
                && resultData.getData() != null
        ) {
            // from https://developer.android.com/training/data-storage/shared/documents-files#java
            onOpenFileResult(resultData.getData());
            return;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    private void openFile(Uri pickerInitialUri) {
        // from https://developer.android.com/training/data-storage/shared/documents-files#java
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/csv")
                // .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_TITLE, getString(R.string.title_open_file))
                ;

        /*
            final int takeFlags = intent.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            getContentResolver().takePersistableUriPermission(uri, takeFlags);

         */
        if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        }

        startActivityForResult(intent, VIEW_CSV_FILE);
    }

    private void onOpenFileResult(Uri data) {
        repository = CsvItemRepositoryAndroid.create (this, data);
        initializeTableView(findViewById(R.id.tableview));
    }

    private void initializeTableView(TableView tableView) {
        columnDefinitions = TestData.createColumnDefinitions(repository.getHeader());

        // Create TableView Adapter
        tableViewAdapter = new TableViewAdapter<>();
        tableView.setAdapter(tableViewAdapter);

        filterItems(null);

        tableView.setTableViewListener(new TableViewListener(tableView));
    }

    private void filterItems(String searchTerm) {
        TableViewModel<CsvItem> tableViewModel = new TableViewModel<>(columnDefinitions, repository.getPojos(searchTerm));
        tableViewAdapter.setAllItems(tableViewModel.getColumnHeaderList(), tableViewModel
                .getRowHeaderList(), tableViewModel.getCellList());
    }
}