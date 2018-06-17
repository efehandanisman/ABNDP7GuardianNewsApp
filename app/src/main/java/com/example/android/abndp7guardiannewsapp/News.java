package com.example.android.abndp7guardiannewsapp;

/**
 * Created by Efehan on 11.6.2018.
 */

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class News extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<NewsClass>> {
    private static final String GUARDIAN_REQUEST_URL = "https://content.guardianapis.com/search?q=facebook&api-key=00f4f78d-4618-44f1-87f0-82d18cd0ce33&show-tags=contributor";
    private NewsAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private static final int NEWS_LOADER_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        ListView newsList = findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsList.setEmptyView(mEmptyStateTextView);

        mAdapter = new NewsAdapter(this, new ArrayList<NewsClass>());
        newsList.setAdapter(mAdapter);

        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                NewsClass currentNews = mAdapter.getItem(position);
                // NewsClass currentTags = mAdapter.getItem(position);
                Uri newsUri = Uri.parse(currentNews.getUrl());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                startActivity(websiteIntent);
            }
        });

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.progress);
            loadingIndicator.setVisibility(View.GONE);
            // Update empty st ate with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

    }


    @Override
    public Loader<List<NewsClass>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );
        if (!section.equals(getString(R.string.settings_section_default))) {
            uriBuilder.appendQueryParameter("section", section);
        }

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<NewsClass>> loader, List<NewsClass> newsList) {
        View progress = findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.no_news);
        mAdapter.clear();

        if (newsList != null && !newsList.isEmpty()) {
            mAdapter.addAll(newsList);

        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsClass>> loader) {
        mAdapter.clear();
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onCreateLoader(NEWS_LOADER_ID, null);

    }


}
