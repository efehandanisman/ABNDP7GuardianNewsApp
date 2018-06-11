package com.example.android.abndp7guardiannewsapp;

/**
 * Created by Efehan on 11.6.2018.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Efehan on 23.4.2018.
 */

public final class Query {
    private static final String LOG_TAG = Query.class.getSimpleName();

    private Query() {

    }


    public static List<NewsClass> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = "";
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {

            Log.e(LOG_TAG, "It was not possible to connect to the server", e);
        }
        List<NewsClass> newsList = extractFeatureFromJson(jsonResponse);
        return newsList;

    }



    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    private static List<NewsClass> extractFeatureFromJson(String newJSON) {
        if (TextUtils.isEmpty(newJSON)) {
            return null;
        }

        List<NewsClass> newsList = new ArrayList<>();

        try {

            JSONObject baseJsonResponse = new JSONObject(newJSON);
            JSONObject response = baseJsonResponse.getJSONObject("response");

            JSONArray NewsArray = response.getJSONArray("results");

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (int i = 0; i < NewsArray.length(); i++) {

                // Get a single earthquake at position i within the list of earthquakes
                JSONObject currentNews = NewsArray.getJSONObject(i);

                String sectionName = currentNews.optString("sectionName");

                String webTitle = currentNews.optString("webTitle");

                String date = currentNews.optString("webPublicationDate");
                date = formatDate(date);
                String url = currentNews.optString("webUrl");
                JSONArray tags = currentNews.getJSONArray("tags");

                String articleAuthor;
                if(tags.length()!=0) {
                    JSONObject tagsObject = tags.getJSONObject(0);
                    articleAuthor = tagsObject.optString("webTitle");
                } else articleAuthor = "No author, this is just a news";
                // Create a new {@link Earthquake} object with the magnitude, location, time,
                // and url from the JSON response.
                NewsClass newEntry = new NewsClass(webTitle, sectionName, articleAuthor, url, date);
                newsList.add(newEntry);
            }

        } catch (JSONException e) {
            Log.e("Query", "Problem to parse results", e);
        }
        return newsList;
    }

    // Taken from https://github.com/laramartin/android_newsfeed/blob/master/app/src/main/java/eu/laramartin/newsfeed/QueryUtils.java

    private static String formatDate(String rawDate) {
        String jsonDatePattern = "yyyy-MM-dd";
        SimpleDateFormat jsonFormatter = new SimpleDateFormat(jsonDatePattern);
        try {
            Date parsedJsonDate = jsonFormatter.parse(rawDate);
            String finalDatePattern = "yyyy-MM-dd";
            SimpleDateFormat finalDateFormatter = new SimpleDateFormat(finalDatePattern);
            return finalDateFormatter.format(parsedJsonDate);
        } catch (ParseException e) {
            Log.e("QueryUtils", "Error parsing JSON date: ", e);
            return "";
        }
    }
}