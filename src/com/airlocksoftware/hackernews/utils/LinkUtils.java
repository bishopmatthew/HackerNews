package com.airlocksoftware.hackernews.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import com.airlocksoftware.hackernews.data.UserPrefs;

/**
 * Created by matthewbbishop on 11/24/13.
 */
public class LinkUtils {

    public static void handleUrlIntent(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());

        /* Potential fix to allow things to be opened in a new tab */
        UserPrefs prefs = new UserPrefs(context);
        boolean openInNewTab = prefs.getOpenInNewTab();
        if(openInNewTab) intent.putExtra("new_window", true);

        context.startActivity(intent);
    }

}
