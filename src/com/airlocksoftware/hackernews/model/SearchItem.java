package com.airlocksoftware.hackernews.model;

public class SearchItem {

  public String _id;

  public boolean _noindex;

  public long _update_ts;

  public String cache_ts;

  public String create_ts;

  public String domain;

  public int id;

  public int num_comments;

  public int parent_id;

  public String parent_sigid;

  public int points;

  public String text;

  public String title;

  public String type;

  public String url;

  public String username;

  public Discussion discussion;

  @Override
  public String toString() {
    return "SearchItem [points=" + points + ", text=" + text + ", title=" + title + ", type=" + type + ", url=" + url
            + ", username=" + username + "]";
  }

  public SearchItem() {
  }

  public static class Discussion {

    public int id;

    public String sigid;

    public String title;

    @Override
    public String toString() {
      return "Discussion [id=" + id + ", sigid=" + sigid + ", title=" + title + "]";
    }
  }

}
