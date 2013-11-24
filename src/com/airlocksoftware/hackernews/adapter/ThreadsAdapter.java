package com.airlocksoftware.hackernews.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.CommentThread;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.view.SharePopup;

/**
 * Adapter for ThreadsFragment. Extends CommentsAdapter to support Threads (which is just a List<Comment> + a Story
 * they're children of.
 */
public class ThreadsAdapter extends CommentsAdapter {

    // map from comment to story
    Map<Comment, Story> mStories = new HashMap<Comment, Story>();

    public ThreadsAdapter(Context context, ListView list, SharePopup share) {
        super(context, list, share);
        // no implementation neccessary
    }

    @Override
    public View getView(Comment comment, View convertView, ViewGroup parent) {
        return super.getCommentView(mStories.get(comment), comment, convertView, parent);
    }

    public void addThreads(Collection<CommentThread> threads) {
        for (CommentThread thread : threads) {
            addThread(thread);
        }
    }

    public void addThread(CommentThread thread) {
        if (thread.comments != null && thread.comments.size() > 0 && thread.story != null) {
            mStories.put(thread.comments.get(0), thread.story);
        }
        super.addAll(thread.comments);
    }

    public void clear() {
        super.clear();
        mStories.clear();
    }
}
