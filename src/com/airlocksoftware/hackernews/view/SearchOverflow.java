package com.airlocksoftware.hackernews.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.SearchActivity;
import com.airlocksoftware.hackernews.interfaces.RestartableActivity;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.type.FontEdit;

/**
 * Displays search box in the Overflow menu, and starts SearchActivity whenever a search is performed. *
 */
public class SearchOverflow extends RelativeLayout {

    Activity mActivity;
    FontEdit mSearchBox;
    IconView mSearchBtn;

    private static final int LAYOUT = R.layout.vw_overflow_searchbox;

    private OnClickListener mSearchBtnListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            performSearch();
        }
    };

    private OnEditorActionListener mSearchKeyboardListener = new OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            } else return false;
        }
    };

    public SearchOverflow(Activity activity, RestartableActivity restart) {
        super(activity, null);
        mActivity = activity;

        // inflate layout
        activity.getLayoutInflater()
                .inflate(LAYOUT, this);

        // set layout params
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);

        // find views
        mSearchBox = (FontEdit) findViewById(R.id.edit_search);
        mSearchBox.setOnEditorActionListener(mSearchKeyboardListener);
        mSearchBtn = (IconView) findViewById(R.id.icv_search);
        mSearchBtn.setOnClickListener(mSearchBtnListener);

    }

    private void performSearch() {
        // hide keyboard
        InputMethodManager inputManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Intent intent = new Intent(mActivity, SearchActivity.class);
        intent.putExtra(SearchActivity.QUERY, mSearchBox.getText()
                .toString());
        mActivity.startActivity(intent);
    }

}
