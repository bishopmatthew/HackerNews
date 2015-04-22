package com.airlocksoftware.hackernews.parser;

import android.content.Context;
import android.util.Log;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.*;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentsParser {

	public static final String THREAD_TIMESTAMP_ID = "Thread";
	public static final String COMMENT_TIMESTAMP_ID = "Comment";

	private static final String TAG = CommentsParser.class.getSimpleName();
	private static final Pattern ID_PATTERN = Pattern.compile("(?<=id=)[0-9]+");

	/** Object used to encapsulate the results of parsing a comments page. * */
	public static class CommentsResponse {

		public CommentsResponse() {
			// empty constructor
		}

		public CommentsResponse(Result result) {
			this.result = result;
		}

		public Story story;
		public List<Comment> comments;
		public CommentsTimestamp timestamp;
		public Result result;
	}

	/** Object that encapsulates the results of parsing a user's Threads page. * */
	public static class ThreadsResponse {
		public StoryTimestamp timestamp;
		public List<CommentThread> threads;
		public Result result;
	}

	/** Parses comment groups from a users "Threads" page * */
	public static ThreadsResponse parseThreadsPage(Context context, String username) {
		return parseThreadsPage(context, username, null);
	}

	/** Parses comment groups from a users "Threads" page * */
	public static ThreadsResponse parseThreadsPage(Context context, String username, String moreFnid) {
		ThreadsResponse response = new ThreadsResponse();
		try {
			UserPrefs data = new UserPrefs(context);
			Document doc = getThreadsDocument(data, moreFnid, username);
			response.threads = parseCommentsThreads(doc, data.isLoggedIn());
			response.timestamp = getNewThreadsTimestamp(doc, username);
			response.result = moreFnid != null ? Result.MORE : Result.SUCCESS;
		} catch (Exception e) {
			response.result = Result.FAILURE;
		}
		return response;
	}

	/** Parses a list of CommentThreads from the given document. * */
	private static List<CommentThread> parseCommentsThreads(Document doc, boolean isLoggedIn) {
		Elements commentsContainer = doc.select("td.default");
		ListIterator<Element> commentRows = commentsContainer.listIterator();
		ArrayList<CommentThread> threads = new ArrayList<CommentThread>();

		CommentThread currentThread = null;
		while (commentRows.hasNext()) {
			Element commentRow = commentRows.next();
			CommentThread newThread = parseThread(commentRow);
			if (newThread != null) {
				threads.add(newThread);
				currentThread = newThread;
			}
			Comment comment = parseComment(commentRow, isLoggedIn);
			currentThread.comments.add(comment);
		}
		return threads;
	}

	/**
	 * Checks whether a comment element contains the start of a new thread of comments (i.e. a link to a story)
	 * If so, it parses and returns it. Else it returns null.
	 */
	private static CommentThread parseThread(Element comment) {
		Element comhead = comment.select("span.comhead")
				.first();
		boolean isNewThread = comhead.children()
				.size() >= 4; // size doesn't include text nodes

		CommentThread thread = null;
		if (isNewThread) {
			thread = new CommentThread();
			thread.comments = new ArrayList<Comment>();
			thread.story = new Story();

			Element titleLink = comhead.select("a")
					.last();
			thread.story.title = titleLink.text();

			// get id
			Matcher m = ID_PATTERN.matcher(titleLink.attr("href"));
			if (m.find()) thread.story.storyId = Long.parseLong(m.group());
		}
		return thread;
	}

	/** Parses a story from the comments page identified by storyId * */
	public static CommentsResponse parseCommentsPage(Context context, long storyId) {
		CommentsResponse response = new CommentsResponse();

		try {
			UserPrefs data = new UserPrefs(context);
			Document doc = getCommentsDocument(data, storyId);

			// parse story
			Elements storyRows = getStoryRows(doc);
			Element line1 = storyRows.first();
			Element line2 = doc.select("td.subtext").first();
			response.story = StoryParser.parseStory(line1, line2, data.isLoggedIn());
			response.story.storyId = storyId;
			response.timestamp = getNewCommentsTimestamp(storyId);

			// setup replyFnid, isArchived, & selfText
			Element replyInput = storyRows.select("form[action=comment]").first();
			if (replyInput != null) {
				response.timestamp.parent = replyInput.select("input[name=parent]").first().attr("value");
				response.timestamp.go_to = replyInput.select("input[name=goto]").first().attr("value");
				response.timestamp.hmac = replyInput.select("input[name=hmac]").first().attr("value");
				response.story.selfText = getSelfText(storyRows);
				response.story.isArchived = false;
			} else {
				// it's an archived story
				response.story.isArchived = true;
				response.story.selfText = getArchivedSelfText(storyRows);
			}

			response.comments = parseComments(doc, storyId, data.isLoggedIn());
			response.result = Result.SUCCESS;
		} catch (Exception e) {
			response.result = Result.FAILURE;
			response.comments = new ArrayList<Comment>();
			Log.e(TAG, "Error parsing comments", e);
		}

		return response;
	}

	/** Parses a list of comments from the given document. * */
	private static List<Comment> parseComments(Document doc, long storyId, boolean isLoggedIn) {
		List<Comment> comments = new ArrayList<Comment>();
		ListIterator<Element> commentRows = doc.select("td.default").listIterator();
		while (commentRows.hasNext()) {
			Element commentContainer = commentRows.next();
			Comment comment = parseComment(commentContainer, isLoggedIn);
			comment.storyId = storyId;
			comments.add(comment);
		}
		return comments;
	}

	/** Parses a comment from the Element containing it * */
	private static Comment parseComment(Element commentContainer, boolean isLoggedIn) {
		Comment comment = new Comment();
		comment.depth = getDepth(commentContainer);
		comment.html = getHtml(commentContainer);

		// setup default values
		comment.username = "";
		comment.ago = "";
		comment.commentId = -1;
		comment.auth = "";
		comment.whence = "";
		comment.replyUrl = "";
		comment.isUpvoted = false;

		// if it's a deleted comment, return early
		if (comment.html.equals("")) {
			comment.username = "deleted";
			comment.ago = "deleted";
			comment.html = "deleted";
			return comment;
		}

		Element comhead = getComhead(commentContainer);

		comment.replyUrl = getReplyUrl(commentContainer);
		comment.username = getUsername(comhead);
		comment.ago = getAgo(comhead);
		comment.commentId = getCommentId(comhead);

		Element voteAnchor = commentContainer.parent()
				.select("a[href^=vote")
				.first();
		comment.isUpvoted = voteAnchor == null;
		if (isLoggedIn && !comment.isUpvoted) {
			String[] voteHref = voteAnchor.attr("href").split("[=&]");
			comment.whence = voteHref[voteHref.length - 1];
			comment.auth = voteHref[7];
		}
		return comment;
	}

	private static long getCommentId(Element comhead) {
		String linkHref = comhead.select("a[href^=item]").attr("href");
		Matcher matcher = ID_PATTERN.matcher(linkHref);
		if(matcher.find()) {
			return Long.parseLong(matcher.group());
		} else {
			throw new IllegalStateException("Couldn't parse comment id from commentHeader");
		}
	}

	private static String getAgo(Element comhead) {
		Element agoLink = comhead.select("a").get(1);
		return agoLink.text().replace("|", "").trim();
	}

	private static String getUsername(Element comhead) {
		return comhead.select("a").first().text();
	}

	private static Element getComhead(Element commentContainer) {
		return commentContainer.select("span.comhead").first();
	}

	private static String getReplyUrl(Element commentContainer) {
		return commentContainer.select("span.comment a:containsOwn(reply)").attr("href");
	}

	private static String getHtml(Element commentContainer) {
		Elements comment = commentContainer.select("span.comment > :not(p:has(font[size]))");
		String html = comment.outerHtml();
		// delete font tags from Html
		html = html.replaceAll("[<](/)?font[^>]*[>]", "");
		return html;
	}

	private static int getDepth(Element commentContainer) {
		// CHANGED FROM
		// Element upvoteImg = commentContainer.parent()
		// .select("img[src^=http://ycombinator.com/images/]")
		// .first();
		// IN RESPONSE TO CHANGE IN HTML FROM news.ycombinator.com

		Element upvoteImg = commentContainer.parent().select("img[src^=s.gif]").first();
		return Integer.parseInt(upvoteImg.attr("width")) / 40;
	}

	private static String getArchivedSelfText(Elements storyRows) {
		// check if it has selfText
		if (storyRows.size() <= 2) return null;

		return storyRows.get(3).children().last().text();

	}

	private static String getSelfText(Elements storyRows) {
		// check if it has selfText
		if (storyRows.size() <= 4) return null;

		// TODO switched this to 2 from 3 because it stopped working... need to figure out a more resilient way
		// to do this
		return storyRows.get(2).children().last().text();
	}

	private static Elements getStoryRows(Document doc) {
		return doc.select("td.subtext").first().parent().siblingElements();
	}

	private static CommentsTimestamp getNewCommentsTimestamp(long storyId) {
		CommentsTimestamp timestamp = new CommentsTimestamp();
		timestamp.time = System.currentTimeMillis();
		timestamp.primaryId = COMMENT_TIMESTAMP_ID;
		timestamp.secondaryId = Long.toString(storyId);
		return timestamp;
	}

	private static Document getCommentsDocument(UserPrefs data, long storyId) throws IOException {
		Connection con;
		if (data.isLoggedIn()) {
			con = ConnectionManager.authConnect(ConnectionManager.ITEMS_URL + Long.toString(storyId), data.getUserCookie());
		} else {
			con = ConnectionManager.anonConnect(ConnectionManager.ITEMS_URL + Long.toString(storyId));
		}
		return con.get();
	}

	private static StoryTimestamp getNewThreadsTimestamp(Document doc, String username) {
		Element more = doc.select("td.title a")
				.first();
		if (more == null) return null;

		StoryTimestamp timestamp = new StoryTimestamp();
		timestamp.fnid = more.attr("href");
		timestamp.time = System.currentTimeMillis();
		timestamp.primaryId = THREAD_TIMESTAMP_ID;
		timestamp.secondaryId = username;
		return timestamp;
	}

	/** GETs the document specified by the parameters. * */
	private static Document getThreadsDocument(UserPrefs data, String moreFnid, String username) throws IOException {
		Connection con;
		if (data.isLoggedIn()) {
			if (moreFnid != null) con = ConnectionManager.authConnect(moreFnid, data.getUserCookie());
			else con = ConnectionManager.authConnect(ConnectionManager.THREADS_URL + username, data.getUserCookie());

		} else {
			if (moreFnid != null) con = ConnectionManager.anonConnect(moreFnid);
			else con = ConnectionManager.anonConnect(ConnectionManager.THREADS_URL + username);
		}
		return con.get();
	}

}
