package com.unimelb.gof.wesnap.stories;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.unimelb.gof.wesnap.models.OfficialStory;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by qideng on 15/10/16.
 */
public class GuardianImporter extends AsyncTask<String, Void, String> { // use JSON instead?
    private static final String TAG = "GuardianImporter";
    public static final String SOURCE_NAME = "Guardian";

    private static final String BASE_API_URL = "http://content.guardianapis.com/search";
    private static final String API_KEY = "95a63xev9azvpch7wraqfsqf";

    private static final String USER_AGENT = "Mozilla/5.0";
    public static final int MILLISECONDS_IN_ONE_DAY = 24 * 60 * 60 * 1000;
    public static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    private int nArticles;
    private String requestUrl;
    private String startDate;
    private String endDate;

    public GuardianImporter() {
        this.startDate = DATE_FORMATTER.format(new Date(
                System.currentTimeMillis() - GuardianImporter.MILLISECONDS_IN_ONE_DAY));
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
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//            while ((inputLine = in.readLine()) != null) {
//                Log.d(TAG, "response:"+inputLine);
//                response.append(inputLine);
//            }
//            in.close();
//            return response.toString();

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
            for (Object item : results) {
                Log.d(TAG, "onPostExecute:result=" + item.toString());
                Map<String, Object> itemMapObject = (Map) item;

                // create new story using each item
                OfficialStory newStory = new OfficialStory(
                        SOURCE_NAME,
                        itemMapObject.get("sectionId").toString(),
                        itemMapObject.get("webTitle").toString(),
                        itemMapObject.get("webUrl").toString(),
                        ((Map) itemMapObject.get("fields")).get("thumbnail").toString(),
                        itemMapObject.get("webPublicationDate").toString(),
                        ((Map) itemMapObject.get("fields")).get("headline").toString()
                );

                FirebaseUtil.getOfficialStoriesDatabase().push()
                        .setValue(newStory);

                nArticles++;
            }
        }

        Log.d(TAG, "onPostExecute:resultsSize=" + nArticles);
    }

}
