package com.airlocksoftware.hackernews.model;

import java.util.List;

/**
 * Represents a thread of comments on a user's Threads page *
 */
public class CommentThread {

    public List<Comment> comments;
    public Story story;

    public CommentThread() {
    }

}
