package com.airlocksoftware.hackernews.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import android.content.Context;

import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.CommentThread;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.model.Timestamp;
import com.airlocksoftware.v3.api.Api;

public class CommentsParser {

  public static final String THREAD_TIMESTAMP_ID = "Thread";

  public static final String COMMENT_TIMESTAMP_ID = "Comment";

  private static final String TAG = CommentsParser.class.getSimpleName();

  private static final Pattern ID_PATTERN = Pattern.compile("(?<=id=)[0-9]+");

  /**
   * Object used to encapsulate the results of parsing a comments page. *
   */
  public static class CommentsResponse {

    public CommentsResponse() {
      // empty constructor
    }

    public CommentsResponse(Result result) {
      this.result = result;
    }

    public Story story;

    public List<Comment> comments;

    public Timestamp timestamp;

    public Result result;
  }

  /**
   * Object that encapsulates the results of parsing a user's Threads page. *
   */
  public static class ThreadsResponse {

    public Timestamp timestamp;

    public List<CommentThread> threads;

    public Result result;
  }

  /**
   * Parses comment groups from a users "Threads" page *
   */
  public static ThreadsResponse parseThreadsPage(Context context, String username) {
    return parseThreadsPage(context, username, null);
  }

  /**
   * Parses comment groups from a users "Threads" page *
   */
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

  /**
   * Parses a list of CommentThreads from the given document. *
   */
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
   * Checks whether a comment element contains the start of a new thread of comments (i.e. a link to a story) If so, it
   * parses and returns it. Else it returns null.
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
      if (m.find()) {
        thread.story.storyId = Long.parseLong(m.group());
      }
    }
    return thread;
  }

  /**
   * Parses a story from the comments page identified by storyId *
   */
  public static CommentsResponse parseCommentsPage(Context context, long storyId) {
    CommentsResponse response = new CommentsResponse();

    try {
      UserPrefs data = new UserPrefs(context);
      Document doc = getCommentsDocument(data, storyId);

      // parse story
      Elements storyRows = getStoryRows(doc);
      Element line1 = storyRows.first();
      Element line2 = doc.select("td.subtext")
              .first();
      response.story = StoryParser.parseStory(line1, line2, data.isLoggedIn());
      response.story.storyId = storyId;
      response.timestamp = getNewCommentsTimestamp(storyId);

      // setup replyFnid, isArchived, & selfText
      Element replyInput = storyRows.select("input[name=fnid]")
              .first();
      if (replyInput != null) {
        response.timestamp.fnid = replyInput.attr("value");
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
    }

    return response;
  }

  /**
   * Parses a list of comments from the given document. *
   */
  private static List<Comment> parseComments(Document doc, long storyId, boolean isLoggedIn) {
    List<Comment> comments = new ArrayList<Comment>();
    ListIterator<Element> commentRows = doc.select("td.default")
            .listIterator();
    while (commentRows.hasNext()) {
      Element commentContainer = commentRows.next();
      Comment comment = parseComment(commentContainer, isLoggedIn);
      comment.storyId = storyId;
      comments.add(comment);
    }
    return comments;
  }

  /**
   * Parses a comment from the Element containing it *
   */
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
      String[] voteHref = voteAnchor.attr("href")
              .split("[=&]");
      comment.whence = voteHref[voteHref.length - 1];
      comment.auth = voteHref[7];
    }
    return comment;
  }

  private static long getCommentId(Element comhead) {
    return Long.parseLong(comhead.select("a")
            .last()
            .attr("href")
            .split("=")[1]);
  }

  private static String getAgo(Element comhead) {
    // if this is the users' own comment, the text " by " will be at index 1 and "3 days ago |" will
    // be at index 3 otherwise "3 days ago |" will be at index 1
    String ago = ((TextNode) comhead.childNode(1)).text();
    if (ago.equals(" by ")) {
      ago = ((TextNode) comhead.childNode(3)).text();
    }
    return ago.replace("|", "")
            .trim();
  }

  private static String getUsername(Element comhead) {
    return comhead.select("a")
            .first()
            .text();
  }

  private static Element getComhead(Element commentContainer) {
    return commentContainer.select("span.comhead")
            .first();
  }

  private static String getReplyUrl(Element commentContainer) {
    return commentContainer.select("span.comment a:containsOwn(reply)")
            .attr("href");
  }

  private static String getHtml(Element commentContainer) {
    String html = commentContainer.select("span.comment > :not(p:has(font[size]))")
            .toString();
    // delete font tags from Html
    // TODO should somehow transform these so they show faded text (but in the appropriate color for the theme)
    html = html.replace("<font color=\"#000000\">", "");
    html = html.replace("</font>", "");
    return html;
  }

  private static int getDepth(Element commentContainer) {
    // CHANGED FROM
    // Element upvoteImg = commentContainer.parent()
    // .select("img[src^=http://ycombinator.com/images/]")
    // .first();
    // IN RESPONSE TO CHANGE IN HTML FROM news.ycombinator.com

    Element upvoteImg = commentContainer.parent()
            .select("img[src^=s.gif]")
            .first();
    return Integer.parseInt(upvoteImg.attr("width")) / 40;
  }

  private static String getArchivedSelfText(Elements storyRows) {
    // check if it has selfText
    if (storyRows.size() <= 2) {
      return null;
    }

    return storyRows.get(3)
            .children()
            .last()
            .text();

  }

  private static String getSelfText(Elements storyRows) {
    // check if it has selfText
    if (storyRows.size() <= 4) {
      return null;
    }

    // TODO switched this to 2 from 3 because it stopped working... need to figure out a more resilient way
    // to do this
    return storyRows.get(2)
            .children()
            .last()
            .text();
  }

  private static Elements getStoryRows(Document doc) {
    return doc.select("td.subtext")
            .first()
            .parent()
            .siblingElements();
  }

  private static Timestamp getNewCommentsTimestamp(long storyId) {
    Timestamp timestamp = new Timestamp();
    timestamp.time = System.currentTimeMillis();
    timestamp.primaryId = COMMENT_TIMESTAMP_ID;
    timestamp.secondaryId = Long.toString(storyId);
    return timestamp;
  }

  private static Document getCommentsDocument(UserPrefs data, long storyId) throws IOException {
    Connection con;
    if (data.isLoggedIn()) {
      con = ConnectionManager.authConnect(Api.ITEMS_URL + Long.toString(storyId), data.getUserCookie());
    } else {
      con = ConnectionManager.anonConnect(Api.ITEMS_URL + Long.toString(storyId));
    }
    return con.get();
  }

  private static Timestamp getNewThreadsTimestamp(Document doc, String username) {
    Element more = doc.select("td.title a")
            .first();
    if (more == null) {
      return null;
    }

    Timestamp timestamp = new Timestamp();
    timestamp.fnid = more.attr("href");
    timestamp.time = System.currentTimeMillis();
    timestamp.primaryId = THREAD_TIMESTAMP_ID;
    timestamp.secondaryId = username;
    return timestamp;
  }

  /**
   * GETs the document specified by the parameters. *
   */
  private static Document getThreadsDocument(UserPrefs data, String moreFnid, String username) throws IOException {
    Connection con;
    if (data.isLoggedIn()) {
      if (moreFnid != null) {
        con = ConnectionManager.authConnect(moreFnid, data.getUserCookie());
      } else {
        con = ConnectionManager.authConnect(Api.THREADS_URL + username, data.getUserCookie());
      }

    } else {
      if (moreFnid != null) {
        con = ConnectionManager.anonConnect(moreFnid);
      } else {
        con = ConnectionManager.anonConnect(Api.THREADS_URL + username);
      }
    }
    return con.get();
  }

}
