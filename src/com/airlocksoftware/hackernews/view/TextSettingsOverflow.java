package com.airlocksoftware.hackernews.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.data.UserPrefs.Theme;
import com.airlocksoftware.hackernews.interfaces.RestartableActivity;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.type.FontFactory;

/**
 * Light/Dark theme & text size chooser that's added to the overflow menu. If a setting is changed, the activity needs
 * to restart.
 */
public class TextSettingsOverflow extends RelativeLayout {

    Context mContext;
    RestartableActivity mRestart;

    UserPrefs mUserData;

    IconView mDay;
    IconView mNight;
    IconView mBigger;
    IconView mSmaller;

    // CONSTANTS
    private static final float MAX_SCALE_FACTOR = 1.375f;
    private static final float MIN_SCALE_FACTOR = 0.75f;
    private static final float SCALE_INCREMENT = 0.125f;
    private static final int LAYOUT = R.layout.vw_overflow_textsettings;

    private OnClickListener themeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            UserPrefs data = new UserPrefs(mContext);
            if (v.getId() == R.id.icv_day_mode) data.setTheme(Theme.LIGHT);
            else if (v.getId() == R.id.icv_night_mode) data.setTheme(Theme.DARK);

            mRestart.restartActivity();
        }
    };

    private OnClickListener textSizeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            float sf = FontFactory.getTextScaleFactor(mContext);

            if (v.getId() == R.id.icv_text_bigger && sf < MAX_SCALE_FACTOR) {
                FontFactory.saveTextScaleFactor(mContext, sf += SCALE_INCREMENT);
                ((RestartableActivity) mContext).restartActivity();
            } else if (v.getId() == R.id.icv_text_smaller && sf > MIN_SCALE_FACTOR) {
                FontFactory.saveTextScaleFactor(mContext, sf -= SCALE_INCREMENT);
                mRestart.restartActivity();
            }

        }
    };

    public TextSettingsOverflow(Context context, RestartableActivity restart) {
        super(context, null);
        mContext = context;
        mRestart = restart;

        // inflate layout
        LayoutInflater.from(mContext)
                .inflate(LAYOUT, this);

        // set layout params
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        // setup text settings
        mUserData = new UserPrefs(mContext);
        mDay = (IconView) findViewById(R.id.icv_day_mode);
        mNight = (IconView) findViewById(R.id.icv_night_mode);
        mBigger = (IconView) findViewById(R.id.icv_text_bigger);
        mSmaller = (IconView) findViewById(R.id.icv_text_smaller);

        mDay.setOnClickListener(themeListener);
        mNight.setOnClickListener(themeListener);
        mBigger.setOnClickListener(textSizeListener);
        mSmaller.setOnClickListener(textSizeListener);
    }

    public void refreshVisibility() {
        float sf = FontFactory.getTextScaleFactor(mContext);
        if (sf <= MIN_SCALE_FACTOR) {
            mSmaller.setVisibility(View.GONE);
            mBigger.setVisibility(View.VISIBLE);
        } else if (sf >= MAX_SCALE_FACTOR) {
            mSmaller.setVisibility(View.VISIBLE);
            mBigger.setVisibility(View.GONE);
        } else {
            mSmaller.setVisibility(View.VISIBLE);
            mBigger.setVisibility(View.VISIBLE);
        }

        if (mUserData.getTheme() == Theme.DARK) {
            mNight.setVisibility(View.GONE);
            mDay.setVisibility(View.VISIBLE);
        } else if (mUserData.getTheme() == Theme.LIGHT) {
            mNight.setVisibility(View.VISIBLE);
            mDay.setVisibility(View.GONE);
        }

    }

}
