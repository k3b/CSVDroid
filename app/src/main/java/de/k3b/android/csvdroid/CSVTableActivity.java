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

import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;
import java.util.List;

import de.k3b.util.csv.CsvItem;
import de.k3b.util.csv.CsvItemRepository;

public class CSVTableActivity extends AppCompatActivity {
    private final Handler delayTimerForSearch = new Handler();

    private TableViewAdapter<CsvItem> tableViewAdapter;
    private List<ColumnDefinition<CsvItem>> columnDefinitions;
    private CsvItemRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_table);


        TableView tableView = findViewById(R.id.tableview);

        repository = CsvItemRepositoryAndroid.create (this);


        if (repository == null) {
            // no source file on app start. Use constant demo data instead
            CsvItem header = TestData.createSampleHeader(8);
            List<CsvItem> pojos = TestData.createSampleData(header, 25);

            repository = new CsvItemRepository(header, pojos);
            CsvItemRepositoryAndroid.showError(this,null, "No Input CSV. Usining demo data instead.");
        }


        initializeTableView(tableView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tableview, menu);

        MenuItem i =  menu.findItem(R.id.search_bar);
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