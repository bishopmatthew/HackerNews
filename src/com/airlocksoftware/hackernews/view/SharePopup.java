package com.airlocksoftware.hackernews.view;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.ShareItem;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.holo.anim.AnimationParams;
import com.airlocksoftware.holo.anim.AnimationParams.Exclusivity;
import com.airlocksoftware.holo.anim.AnimationParams.FillType;
import com.airlocksoftware.holo.anim.OverlayManager;
import com.airlocksoftware.holo.checkable.CheckableView;
import com.airlocksoftware.holo.checkable.CheckableViewManager;
import com.airlocksoftware.holo.checkable.CheckableViewManager.OnCheckedViewChangedListener;
import com.airlocksoftware.holo.picker.share.ShareList;
import com.airlocksoftware.holo.type.FontText;
import com.airlocksoftware.holo.utils.Utils;

/**
 * Uses HoloTheme class ShareList to display a popup with a list of sharing options. Also allows multiple options for
 * what should be shared.
 */
public class SharePopup extends RelativeLayout {

    Context mContext;

    OverlayManager mOverlay;
    ShareList mList;
    FontText mTitle;
    CheckableViewManager mCheckManager;
    LinearLayout mCheckContainer;
    private View mCancelButton;

    private int mChildCount = 1;

    OnCheckedViewChangedListener checkListener = new OnCheckedViewChangedListener() {
        @Override
        public void onCheckedViewChanged(CheckableViewManager manager, int newIndex, int oldIndex) {
            ShareItem holder = (ShareItem) manager.getChildAt(newIndex)
                    .getTag();
            updateIntent(holder.subject, holder.extraText);
        }
    };

    OnClickListener cancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            close();
        }
    };

    private static final int LAYOUT_RES = R.layout.vw_share_popup;

    // CONSTRUCTORS
    public SharePopup(Context context, AttributeSet attrs, OverlayManager overlayManager) {
        super(context, attrs);
        if (context == null || overlayManager == null) throw new NullPointerException(
                "Passed a null context or overlayManager");
        mOverlay = overlayManager;
        mContext = context;

        setId(R.id.share_popup);

        // inflate & find views
        LayoutInflater.from(context)
                .inflate(LAYOUT_RES, this);
        mList = (ShareList) findViewById(R.id.sharelist);
        mCheckContainer = (LinearLayout) findViewById(R.id.cnt_btn);
        mTitle = (FontText) findViewById(R.id.txt_share);
        mCancelButton = findViewById(R.id.btn_cancel);

        mCancelButton.setOnClickListener(cancelListener);

        // setup intent type of ShareList (empty, to be filled in later)
        mList.setIntentType(createShareIntent("", ""));

        // setup check manager
        mCheckManager = new CheckableViewManager();
        mCheckManager.setOnCheckedChangedListener(checkListener);

        // setup animation in the overlay

        // get screen size (if < 3 inches tall, allow SharePopup to clip the ActionBar
        PointF size = Utils.pixelsToInches(mContext, Utils.getScreenSize(mContext));
        FillType fillType = size.y > 3 ? FillType.CLIP_CONTENT : FillType.FILL_SCREEN;

        AnimationParams animParams = new AnimationParams(fillType).exclusivity(Exclusivity.EXCLUDE_ALL);
        animParams.inAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_in_up));
        animParams.outAnimation(AnimationUtils.loadAnimation(context, R.anim.scale_out_down));
        mOverlay.addView(this, animParams);

        // set layout params & gravity

        // get screen size
        int maxWidth = Utils.getScreenSize(mContext).x - 40; // max width is screen width - 40
        int shareWidth = Math.min(Utils.dpToPixels(mContext, 320), maxWidth); // 320dp is ideal size
        int topMargin = Utils.dpToPixels(mContext, 30); // leave 30dp margin at top

        int grav = Gravity.BOTTOM;
        grav |= Gravity.RIGHT;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(shareWidth, LayoutParams.WRAP_CONTENT, grav);
        layoutParams.topMargin = topMargin;
        setLayoutParams(layoutParams);

        // set background
        TypedValue tv = new TypedValue();
        mContext.getTheme()
                .resolveAttribute(R.attr.bgSharePopup, tv, true);
        int background = tv.resourceId;
        setBackgroundResource(background);
    }

    // PUBLIC METHODS
    public void shareComment(Comment comment) {
        title("Share comment");

        String subject = "A comment by " + comment.username;
        String extraText = ConnectionManager.itemIdToUrl(comment.commentId);
        clearButtons();
        addItem("link", subject, extraText);
        addItem("text", subject, comment.generateSpannedHtml()
                .toString());

        updateIntent(subject, extraText);
        open();
    }

    /**
     * Updates the SharePopup to share the specified story. Sets the title, buttons, and intent. *
     */
    public void shareStory(Story story) {
        title("Share story");

        String extraText = ConnectionManager.itemIdToUrl(story.storyId);
        clearButtons();
        if (StringUtils.isNotBlank(story.url)) addItem("article", story.title, story.url);
        addItem("hn page", story.title, extraText);

        updateIntent(story.title, story.url);
        open();
    }

    // PRIVATE METHODS

    /**
     * Creates a sharing intent from a title and body text. (can be URL, String, etc) *
     */
    private static Intent createShareIntent(String title, String extraText) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, extraText);
        return intent;
    }

    /**
     * Updates the intent that will be used when an item in the ShareList is clicked. *
     */
    private void updateIntent(String title, String extraText) {
        mList.setIntent(createShareIntent(title, extraText));
    }

    private void addAll(Collection<ShareItem> items) {
        clearButtons();
        for (ShareItem item : items) {
            addItem(item);
        }
    }

    /**
     * Adds button to the checkable buttons at the top of the SharePopup (i.e. an intent) *
     */
    private void addItem(String label, String subject, String extraText) {
        ShareItem holder = new ShareItem();
        holder.label = label;
        holder.subject = subject;
        holder.extraText = extraText;
        addItem(holder);
    }

    /**
     * Adds button to the checkable buttons at the top of the SharePopup (i.e. an intent) *
     */
    private void addItem(ShareItem item) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        CheckableView chk = (CheckableView) inflater.inflate(R.layout.vw_btn_sharepopup, mCheckContainer, false);

        FontText txt = (FontText) chk.findViewById(R.id.txt);
        txt.setText(item.label);

        chk.setId(mChildCount++); // first child will always have id of 1

        chk.setTag(item);

        mCheckContainer.addView(chk);
        mCheckManager.register(chk);

        // this means it's the first one added to the container
        if (mChildCount == 2) {
            mCheckManager.check(chk.getId());
            chk.setChecked(true);
        }
    }

    /**
     * Removes all buttons from the check container *
     */
    private void clearButtons() {
        mCheckContainer.removeAllViews();
        mCheckManager.deregisterAll();
        mChildCount = 1;
    }

    /**
     * Sets the title of the SharePopup *
     */
    private void title(String title) {
        mTitle.setText(title);
    }

    /**
     * Display SharePopup (should only be called after the ShareList is populated). *
     */
    private void open() {
        mOverlay.showViewById(getId());
    }

    /**
     * Close the SharePopup. Alternatively, clicking outside of the SharePopup will do the same thing. *
     */
    private void close() {
        mOverlay.hideAllViews();
    }

}
