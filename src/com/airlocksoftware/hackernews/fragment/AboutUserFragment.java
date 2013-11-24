package com.airlocksoftware.hackernews.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.loader.AboutUserLoader;
import com.airlocksoftware.hackernews.model.User;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarClient;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarController;
import com.airlocksoftware.holo.type.FontText;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Fragment that displays the user's name, karma, and about summary. *
 */
public class AboutUserFragment extends Fragment implements ActionBarClient, LoaderManager.LoaderCallbacks<User> {

    // State
    String mUsername;

    FontText mUsernameText, mCreatedText, mTotalText, mAvgText, mDescriptionText;
    View mError, mLoading, mAboutUserContainer;

    // Constants
    public static final String USERNAME = AboutUserFragment.class.getSimpleName() + ".username";

    public AboutUserFragment() {
        // default no-arg constructor
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        Bundle args = getArguments();
        if (args != null) mUsername = args.getString(USERNAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frg_aboutuser, container, false);
        ViewUtils.fixBackgroundRepeat(root);
        findViews(root);
        showLoading();
        return root;
    }

    private void findViews(View root) {
        mAboutUserContainer = root.findViewById(R.id.cnt_about_user);
        mUsernameText = (FontText) root.findViewById(R.id.txt_username);
        mCreatedText = (FontText) root.findViewById(R.id.txt_created);
        mTotalText = (FontText) root.findViewById(R.id.txt_karma_total);
        mAvgText = (FontText) root.findViewById(R.id.txt_karma_avg);
        mDescriptionText = (FontText) root.findViewById(R.id.txt_description);

        mError = root.findViewById(R.id.cnt_error);
        mLoading = root.findViewById(R.id.cnt_loading);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // restoring saved state
        if (savedInstanceState != null) {
            mUsername = savedInstanceState.getString(USERNAME);
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(USERNAME, mUsername);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new AboutUserLoader(getActivity(), mUsername);
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User data) {
        if (data != null) {
            bindData(data);
            showContent();
        } else {
            showError();
        }
    }

    private void bindData(User data) {
        mUsernameText.setText(data.username);
        mCreatedText.setText(data.created);
        mTotalText.setText(Integer.toString(data.karma));
        if (data.avg != -1.0f) mAvgText.setText(Float.toString(data.avg));
        mDescriptionText.setText(Html.fromHtml(data.aboutHtml));
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {
        // do nothing
    }

    @Override
    public void setupActionBar(Context context, ActionBarController ab) {
        ab.setTitleText(context.getString(R.string.about_user));
    }

    @Override
    public void cleanupActionBar(Context context, ActionBarController ab) {
        ab.setTitleText(null);
    }

    private void showLoading() {
        mError.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
        mAboutUserContainer.setVisibility(View.GONE);
    }

    private void showError() {
        mAboutUserContainer.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    private void showContent() {
        mAboutUserContainer.setVisibility(View.VISIBLE);
        mError.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
    }

}
