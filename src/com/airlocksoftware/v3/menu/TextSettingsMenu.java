package com.airlocksoftware.v3.menu;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.airlocksoftware.hackernews.R;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.WindowManager.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.WRAP_CONTENT;

/**
 * Created by matthewbbishop on 12/28/13.
 */
public class TextSettingsMenu extends PopupWindow {

  private final Context mContext;

  public TextSettingsMenu(Context context) {
    super(context);
    mContext = context;

    /* Set content view */
    init();
    initViews();
  }

  private void init() {
    setWidth(MATCH_PARENT);
    setHeight(WRAP_CONTENT);
    setAnimationStyle(R.style.PopupAnimation_Bottom);
    Resources res = mContext.getResources();
    setBackgroundDrawable(res.getDrawable(R.drawable.bg_popup_text_settings));
    setOutsideTouchable(true);
    setFocusable(true);
  }

  private void initViews() {
    View content = LayoutInflater.from(mContext).inflate(R.layout.popup_text_settings, null);
    setContentView(content);
//    ButterKnife.inject(this, content);
  }

  public void show(View parent) {
    showAtLocation(parent, CENTER_HORIZONTAL | BOTTOM, 0, 0);
  }
}
