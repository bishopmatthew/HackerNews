package com.airlocksoftware.hackernews.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.airlocksoftware.hackernews.R;

public class AboutActivity extends SlideoutMenuActivity {

    private static final Uri WEBSITE = Uri.parse("http://www.airlocksoftware.com");
    private static final Uri GITHUB = Uri.parse("http://github.com/bishopmatthew/HackerNews");
    private static final Uri MARKET_URL = Uri.parse("market://details?id=com.airlocksoftware.hackernews");

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.act_about);

        getActionBarView().getController()
                .setTitleText(getString(R.string.about));
        setActiveMenuItem(R.id.about_button);

        findViewById(R.id.btn_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hn@airlocksoftware.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Hacker News 2");
                i.putExtra(Intent.EXTRA_TEXT, "Put your message here...");

                startActivity(Intent.createChooser(i, "Send mail..."));
            }
        });

        findViewById(R.id.btn_website).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(WEBSITE);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(GITHUB);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW, MARKET_URL);
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(marketIntent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
