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

import android.os.Bundle;
import android.view.View;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.model.ColumnDefinition;
import com.evrencoskun.tableviewutil.TableViewAdapter;
import com.evrencoskun.tableviewutil.TableViewModel;

import java.util.List;

import de.k3b.android.csvdroid.model.CsvItem;

public class CSVTableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv_table);

        TableView mTableView = findViewById(R.id.tableview);

        initializeTableView(mTableView);
        
    }

    private void initializeTableView(TableView mTableView) {
        CsvItem header = TestData.createSampleHeader(8);
        List<ColumnDefinition<CsvItem>> columnDefinitions = TestData.createColumnDefinitions(header);
        List<CsvItem> pojos = TestData.createSampleData(header, 25);
        // Create TableView View model class  to group view models of TableView
        TableViewModel<CsvItem> tableViewModel = new TableViewModel<>(columnDefinitions, pojos);

        // Create TableView Adapter
        TableViewAdapter<CsvItem> tableViewAdapter = new TableViewAdapter<>();
        tableViewAdapter.setAllItems(tableViewModel);

        mTableView.setAdapter(tableViewAdapter);
        // mTableView.setTableViewListener(new TableViewListener(mTableView));

    }
}