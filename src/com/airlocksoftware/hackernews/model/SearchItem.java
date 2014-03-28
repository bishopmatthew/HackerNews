package com.airlocksoftware.hackernews.model;

import org.apache.commons.lang3.StringUtils;

public class SearchItem {

//	public String _id;
//	public boolean _noindex;
//	public long _update_ts;
//	public String cache_ts;
//	public String create_ts;
//	public String domain;
//	public int id;
//	public int num_comments;
//	public int parent_id;
//	public String parent_sigid;
//	public int points;
//	public String text;
//	public String title;
//	public String type;
//	public String url;
//	public String username;
//	public Discussion discussion;

	public String objectID;
	public String title;
	public String author;
	public String created_at;
	public long created_at_i;
	public long points;
	public long num_comments;

	public String url;
	public String story_text;

	public String story_id;
	public String parent_id;
	public String comment_text;

	public String story_title;
	public String story_url;

	@Override
	public String toString() {
		return "SearchItem [points=" + points
				+ ", story_text=" + story_text
				+ ", title=" + title
				+ ", isTextPost=" + isTextPost()
				+ ", url=" + url
				+ ", story_title=" + url
				+ ", story_url=" + url
				+ ", author=" + author + "]";
	}

	public SearchItem() {
	}

	public boolean isArticlePost() {
		// It's an article post if it has a URL
		return StringUtils.isNotBlank(url);
	}

	public boolean isTextPost() {
		// It's a text post if it doesn't have a URL or comment_text
		return StringUtils.isNotBlank(story_text);
	}

	public boolean isComment() {
		// It's a comment if it has comment_text
		return StringUtils.isNotBlank(comment_text);
	}

	public String domain() {
		if (url != null) {
			return url.replaceAll("https?://(www.)?", "").replaceAll("/.*", "");
		} else {
			return "";
		}
	}

}
