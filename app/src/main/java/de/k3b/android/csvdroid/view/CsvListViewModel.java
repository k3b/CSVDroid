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
package de.k3b.android.csvdroid.view;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.k3b.android.csvdroid.CsvDroidApp;
import de.k3b.util.IRepository;
import de.k3b.util.csv.CsvItemRepository;
import de.k3b.android.csvdroid.CsvItemRepositoryAndroid;
import de.k3b.android.csvdroid.R;
import de.k3b.util.csv.CsvItem;

public class CsvListViewModel extends AndroidViewModel {
    private static final String TAG = CsvListViewModel.class.getSimpleName();

    public static final String KEY_CSV_FILE_URI = "csvFileUri";
    private static final String[] EMPTY_HEADER = new String[0];
    private static final List<CsvItem> EMPTY_ITEMS_LIST = new ArrayList<>();

    /// to find out if uri has changed
    private Uri lastUsedUri = null;
    @Nullable private IRepository<CsvItem> itemRepository = null;
    private final MutableLiveData<String> searchTerm = new MutableLiveData<>();

    /// to find out if uri has searchTerm
    private String lastExecutedSearchTerm = null;

    private final MutableLiveData<List<CsvItem>> csvList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> status = new MutableLiveData<>();

    public CsvListViewModel(@NonNull Application application) {
        super(application);
    }

    public void saveLastUsedUri(Uri data) {
        PreferenceManager
                .getDefaultSharedPreferences(getApplication())
                .edit()
                .putString(KEY_CSV_FILE_URI, data.toString())
                .apply();
    }

    public Uri loadLastUsedUri() {
        String lastUsed = PreferenceManager
                .getDefaultSharedPreferences(getApplication())
                .getString(KEY_CSV_FILE_URI, null);
        return (lastUsed != null) ? Uri.parse(lastUsed) : null;
    }

    @NotNull
    public String[] getHeader() {
        if (itemRepository == null) return EMPTY_HEADER;
        return itemRepository.getHeader();
    }

    /**
     * loads {@link #itemRepository} from uri or updates {@link #getStatus()} if there was an error.
     **/
    public void load(Uri uri) {
        if (uri != null && !uri.equals(lastUsedUri)) {
            lastUsedUri = uri;
            saveLastUsedUri(uri);
            Context app = getApplication();
            try {
                setStatus(app.getString(R.string.progress_loading_csv, uri));
                resetSearchTerm();
                itemRepository = CsvItemRepositoryAndroid.createOrThrow(app, uri);
                getCsvList().postValue(itemRepository.getPojos());
            } catch (Exception e) {
                onError(app.getString(R.string.error_load, uri), e);
                itemRepository = null;
                getCsvList().postValue(EMPTY_ITEMS_LIST);
            }
        }
    }

    public void load(String[] header, List<CsvItem> pojos) {
        resetSearchTerm();
        itemRepository = new CsvItemRepository(header, pojos);
        CsvItemRepositoryAndroid.showError(getApplication(),null, "No Input CSV. Using demo data instead.");
        getCsvList().postValue(itemRepository.getPojos());
    }

    private void resetSearchTerm() {
        lastExecutedSearchTerm = null;
        getSearchTerm().postValue(null);
    }

    /** triggers a new repository search */
    public void executeSearch(String newSearchTerm) {
        getSearchTerm().postValue(newSearchTerm);

        // prevent expensive search if newSearchTerm has not changed
        if (!Objects.equals(lastExecutedSearchTerm,newSearchTerm)) {
            lastExecutedSearchTerm = newSearchTerm;
            CsvDroidApp.executor.execute(this::onSearchFilterChanged);
        }
    }

    public MutableLiveData<String> getSearchTerm() {
        return searchTerm;
    }

    public MutableLiveData<List<CsvItem>> getCsvList() {
        return csvList;
    }

    public void setStatus(String msg) {
        getStatus().postValue(msg);
    }

    public MutableLiveData<String> getStatus() {
        return status;
    }

    @NonNull
    public String getFilename(@Nullable Object fullPath) {
        String path = "demo";
        if (fullPath != null) {
            path = urlDecode(urlDecode(fullPath.toString()));
            int pos = path.lastIndexOf('/') + 1;
            if (pos > 1) path = path.substring(pos);
        }
        return path;
    }

    private String urlDecode(String fileName) {
        try {
            return URLDecoder.decode(fileName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return fileName;
        }
    }

    private void onSearchFilterChanged() {
        List<CsvItem> csvItems;
        Context app = getApplication();

        if (itemRepository == null) {
            setStatus(app.getString(R.string.result_empty));
            csvItems = new ArrayList<>();
        } else {
            String searchTerm = getSearchTerm().getValue();
            setStatus(app.getString(R.string.progress_filtering, searchTerm));
            csvItems = itemRepository.getPojos(searchTerm);
            if (csvItems.size() == 0) {
                setStatus(app.getString(R.string.result_search_empty, searchTerm));
                return; // data not changed
            }
        }
        getCsvList().postValue(csvItems);
    }

    private void onError(String cause, Exception exception) {
        String msg = "Error " + cause + ": " + exception.getMessage();
        setStatus(msg);
        Log.e(TAG, msg, exception);
    }
}
