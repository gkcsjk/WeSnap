package com.unimelb.gof.wesnap.stories;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.AppParams;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * GuardianImporter
 * Fetch news data via Guardian API
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class GuardianImporter
        extends AsyncTask<String, Void, String> {
    private static final String TAG = "GuardianImporter";
    public static final String SOURCE_NAME = "Guardian";

    private static final String BASE_API_URL = "http://content.guardianapis.com/search";
    private static final String API_KEY = "95a63xev9azvpch7wraqfsqf";

    private static final String USER_AGENT = "Mozilla/5.0";
    public static final int MILLISECONDS_IN_ONE_DAY = 24 * 60 * 60 * 1000;
    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat PUBDATE_FORMATTER_GUARDIAN =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);

    private int nArticles;
    private String requestUrl;
    private String startDate;
    private String endDate;
    private String lastImport;

    public GuardianImporter(String lastImport) {
        /* relevant dates */
        // yesterday
        Date yesterday = new Date(
                System.currentTimeMillis() - GuardianImporter.MILLISECONDS_IN_ONE_DAY);
        // last import time
        if (lastImport == null) {
            this.lastImport = AppParams.PUBDATE_FORMATTER.format(yesterday);
        } else {
            this.lastImport = lastImport;
        }
        // query start date
        try {
            this.startDate = DATE_FORMATTER.format(
                    AppParams.PUBDATE_FORMATTER.parse(this.lastImport));
        } catch (ParseException e) {
            e.printStackTrace();
            this.startDate = DATE_FORMATTER.format(yesterday);
        }
        // query end date
        this.endDate = DATE_FORMATTER.format(new Date());

        // request_url for article search on specified dates
        this.requestUrl = BASE_API_URL + "?format=json"+
                "&from-date=" + this.startDate +
                "&to-date=" + this.endDate +
                "&show-fields=headline,thumbnail"+
                "&api-key=" + API_KEY;
        this.nArticles = 0;
    }

    @Override
    protected String doInBackground(String[] params) {
        URL urlObject;
        HttpURLConnection con;

        try {
            urlObject = new URL(this.requestUrl);

            con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            Log.d(TAG, "sendRequest:GET:url=" + this.requestUrl);
            Log.d(TAG, "sendRequest:GET:responseCode=" + con.getResponseCode());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));

            String inputLine = in.readLine();
            Log.d(TAG, "response:"+inputLine);
            return inputLine;

        } catch (Exception e) {
            Log.e(TAG, "sendRequest:error", e);
            return "error";
        }
    }

    @Override
    protected void onPostExecute(String message) {
        // parse the return value
        Map<String, Object> rootMapObject = new Gson().fromJson(message, Map.class);
        List results = (List) ((Map) rootMapObject.get("response")).get("results");

        if (results == null) {
            Log.d(TAG, "onPostExecute:results=null");
        } else {
            String mostRecentDate = null;

            for (Object item : results) {
                Log.d(TAG, "onPostExecute:result=" + item.toString());
                Map<String, Object> itemMapObject = (Map) item;

                // reformat the date
                String pubDate = itemMapObject.get("webPublicationDate").toString();
                try {
                    pubDate = AppParams.PUBDATE_FORMATTER.format(
                            PUBDATE_FORMATTER_GUARDIAN.parse(pubDate)
                    );
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }

                // filter old items
                if (pubDate.compareTo(this.lastImport) <= 0) {
                    // pubDate is before lastImport
                    // this item should have been imported already
                    continue;
                }

                // filter non-articles
                if (!itemMapObject.get("type").toString().equals("article")) {
                    continue;
                }

                // record the most recent publication date
                if (mostRecentDate == null) {
                    mostRecentDate = pubDate;
                } else if (mostRecentDate.compareTo(pubDate) < 0) {
                    // mostRecentDate is before pubDate
                    mostRecentDate = pubDate;
                }

                // create new story using the data fields
                String keyword = itemMapObject.get("sectionId").toString();
                OfficialStory newStory = new OfficialStory(
                        SOURCE_NAME,
                        keyword,
                        itemMapObject.get("webTitle").toString(),
                        itemMapObject.get("webUrl").toString(),
                        ((Map) itemMapObject.get("fields")).get("thumbnail").toString(),
                        pubDate,
                        ((Map) itemMapObject.get("fields")).get("headline").toString()
                );

                // save to Database
                String newStoryId = FirebaseUtil.getOfficialStoriesDatabase().push().getKey();
                FirebaseUtil.getOfficialStoriesDatabase().child(newStoryId).setValue(newStory);
                FirebaseUtil.getKeywordsDatabase().child(keyword).child(newStoryId).setValue(pubDate);
                nArticles++;
            } // finish looping all results!

            // save the most recent publication date
            if (mostRecentDate != null) {
                FirebaseUtil.getLastImportTimeRef().setValue(mostRecentDate);
            }
        }

        Log.d(TAG, "onPostExecute:resultsSize=" + nArticles);
    }

}
