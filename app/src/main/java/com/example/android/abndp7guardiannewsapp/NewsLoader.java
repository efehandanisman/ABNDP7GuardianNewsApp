package com.example.android.abndp7guardiannewsapp;

/**
 * Created by Efehan on 11.6.2018.
 */

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Efehan on 23.4.2018.
 */

public class NewsLoader extends AsyncTaskLoader<List<NewsClass>> {
    private static final String LOG_TAG = NewsLoader.class.getName();
    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<NewsClass> loadInBackground() {
        Log.d(LOG_TAG, "loadInBackground()");
        if (mUrl == null) {
            return null;
        }

        List<NewsClass> newsList = Query.fetchNewsData(mUrl);
       /* if (newsList.size() == 0) {
            Log.d(LOG_TAG, "news list is empty");
        } else {
            Log.d(LOG_TAG, newsList.get(0).getTitle());
        }*/

        return newsList;
    }
}

