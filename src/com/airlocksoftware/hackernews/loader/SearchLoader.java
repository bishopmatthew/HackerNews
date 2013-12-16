package com.airlocksoftware.hackernews.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.airlocksoftware.hackernews.activity.SearchActivity.SearchType;
import com.airlocksoftware.hackernews.activity.SearchActivity.SortType;
import com.airlocksoftware.hackernews.loader.SearchLoader.SearchResult;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.SearchItem;

import com.google.gson.Gson;

public class SearchLoader extends AsyncTaskLoader<SearchResult> {

  Request mRequest;

  String mQuery;

  SortType mSort;

  SearchType mType;

  /**
   * Which position to request search results for. Use 0 for a new search, and other positive integers as offsets from
   * 0.
   */
  int mStart = 0;

  // Constants
  @SuppressWarnings("unused")
  private static final String TAG = SearchLoader.class.getSimpleName();

  private static final String START_QUERY = "http://api.thriftdb.com/api.hnsearch.com/items/_search";

  private static final String WEIGHTS
          = "&weights[title]=1.1&weights[text]=0.7&weights[domain]=2.0&weights[username]=0.1&weights[type]=0.0&boosts[fields][points]=0.15&boosts[fields][num_comments]=0.15&boosts[functions][pow(2,div(div(ms(create_ts,NOW),3600000),72))]=200.0";

  public static class SearchResult {

    public SearchResult(Result result) {
      this.result = result;
    }

    public SearchResult() {
      // default empty constructor
    }

    public Result result;

    public List<SearchItem> items;

    public int hits = -1;

    public int start = -1;
  }

  public SearchLoader(Context context, Request request, String query, SortType sort, SearchType type, int start) {
    super(context);

    mRequest = request;
    mQuery = query;
    mSort = sort;
    mType = type;
    mStart = start;
  }

  @Override
  public SearchResult loadInBackground() {
    if (mQuery == null || mSort == null || mType == null || mRequest == Request.EMPTY) {
      return new SearchResult(Result.EMPTY);
    }

    String response = performSearch();

    int hits = -1;
    List<SearchItem> items = new ArrayList<SearchItem>();
    SearchResult search = new SearchResult();

    try {
      Gson gson = new Gson();
      JSONObject obj = new JSONArray(response).getJSONObject(0);
      hits = obj.getInt("hits");
      JSONArray results = obj.getJSONArray("results");

      for (int i = 0; i < results.length(); i++) {
        SearchItem searchItem = gson.fromJson(results.getJSONObject(i)
                .getString("item"), SearchItem.class);
        items.add(searchItem);
      }

    } catch (Exception e) {
      search.result = Result.FAILURE;
      e.printStackTrace();
    }

    search.items = items;
    search.hits = hits;
    search.start = mStart;
    if (search.result == null) {
      search.result = Result.SUCCESS;
    }
    // search.result = (items != null && hits != -1) ? Result.SUCCESS : Result.EMPTY;

    return search;
  }

  /**
   * Builds a search url from instance variables, then performs a get of the url. Returns the result as a string. *
   */
  private String performSearch() {
    String searchUrl = buildSearchUrl();

    StringBuilder builder = new StringBuilder();
    HttpClient client = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(searchUrl);
    try {
      HttpResponse response = client.execute(httpGet);
      StatusLine statusLine = response.getStatusLine();
      int statusCode = statusLine.getStatusCode();
      if (statusCode == 200) {
        HttpEntity entity = response.getEntity();
        InputStream content = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
      } else {
        Log.e(searchUrl, "Failed to download file");
        // notify user of search failure
      }
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String response = builder.toString();
    response = "[" + response + "]";
    return response;
  }

  private String buildSearchUrl() {

    StringBuilder builder = new StringBuilder();

    try {
      builder.append(START_QUERY)
              .append("?q=" + URLEncoder.encode(mQuery, "UTF-8"))
              .append(WEIGHTS);
    } catch (UnsupportedEncodingException e) {
      // tell user the query was invalid?
      e.printStackTrace();
    }

    String sortBy;

    switch (mSort) {
      case RELEVANCE:
        sortBy = "score";
        break;
      case DATE:
        sortBy = "create_ts";
        break;
      case POINTS:
        sortBy = "points";
        break;
      default:
        throw new RuntimeException("Error: didn't recieve a valid SortType" + mSort.toString());
    }

    builder.append("&sortby=" + sortBy + "+desc");

    switch (mType) {
      case ALL:
        // don't append filter
        break;
      case STORIES:
        builder.append("&filter[fields][type]=" + "submission");
        break;
      case COMMENTS:
        builder.append("&filter[fields][type]=" + "comment");
        break;
      case USERS:
        throw new RuntimeException("The parent activity should have started UserActivity & not started this loader");
      default:
        throw new RuntimeException("Error: didn't recieve a valid SearchType" + mType.toString());
    }

    builder.append("&start=" + Integer.toString(mStart));

    return builder.toString();
  }

  /**
   * Handles a request to start the Loader.
   */
  @Override
  protected void onStartLoading() {
    forceLoad();
  }
}