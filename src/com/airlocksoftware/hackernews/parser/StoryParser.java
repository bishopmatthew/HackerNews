package com.airlocksoftware.hackernews.parser;

import android.content.Context;
import android.util.Log;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.*;
import org.apache.commons.lang3.StringUtils;
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

public class StoryParser {

	private static final String TAG = StoryParser.class.getSimpleName();
	private static final int NO_POSITION = -1;

	// num comments / points
	private static final Pattern NUM_COMMENTS_PATTERN = Pattern.compile("\\d+");

	/** Parse stories from Front Page, Ask, Best, or New * */
	public static StoryResponse parseStoryList(Context context, Page page, Request request, String moreFnid) {
		String urlExtension = generateUrlExtension(request, page, moreFnid);
		StoryResponse response = parseStories(context, page, urlExtension);
		// parseStories() doesn't know about MORE, so potentially set it here
		if (response.result == Result.SUCCESS && moreFnid != null && request == Request.MORE) {
			response.result = Result.MORE;
		}
		return response;
	}

	/** Generate the extension that we're trying to load (goes on the end of ConnectionManager.BASE_URL) * */
	private static String generateUrlExtension(Request request, Page page, String moreFnid) {
		String urlExtension = "/";
		if (moreFnid != null && request == Request.MORE) urlExtension += moreFnid;
		switch (page) {
			case ASK:
				urlExtension += "ask";
				break;
			case BEST:
				urlExtension += "best";
				break;
			case NEW:
				urlExtension += "newest";
				break;
			case ACTIVE:
				urlExtension += "active";
				break;
			default:
				break;
		}
		return urlExtension;
	}

	/** Parse stories from the user's submissions page * */
	public static StoryResponse parseUserSubmissions(Context context, String username, String moreFnid) {
		if (StringUtils.isBlank(username)) {
			throw new RuntimeException("StoryParser.parseUserSubmissions received a blank username");
		}
		String urlExtension = StringUtils.isNotBlank(moreFnid) ? "/" + moreFnid : "/submitted?id=" + username;
		StoryResponse response = parseStories(context, Page.USER, urlExtension);
		if (StringUtils.isNotBlank(moreFnid) && response.result == Result.SUCCESS) {
			// switch result to MORE
			response.result = Result.MORE;
		}
		return response;
	}

	private static StoryResponse parseStories(Context context, Page page, String urlExtension) {
		StoryResponse response = new StoryResponse();
		response.stories = new ArrayList<Story>();
		response.result = Result.SUCCESS; // success unless error state is tripped
		try {
			UserPrefs data = new UserPrefs(context);
			String userCookie = data.getUserCookie();
			Document doc = getDocument(urlExtension, userCookie);

			// check for expired fnid
			Element body = doc.body();
			String bodyText = body.text();
			if (bodyText.equals("Unknown or expired link.")) {
				response.result = Result.FNID_EXPIRED;
				return response;
			}

			Elements titles = doc.select("span.rank"); // html changed, story rank numbers now have this class
			Elements subtexts = doc.select("td.subtext");
			ListIterator<Element> titlesIterator = titles.listIterator();
			ListIterator<Element> subtextIterator = subtexts.listIterator();

			while (titlesIterator.hasNext() && subtextIterator.hasNext()) {
				Element child = titlesIterator.next();
				Element titleElement = child.parent().parent();
				Element subtextElement = subtextIterator.next();
				Story story = parseStory(titleElement, subtextElement, userCookie != null);
				story.page = page;
				response.stories.add(story);
			}
			response.timestamp = getNewTimestamp(doc);

		} catch (IOException e) {
			response.result = Result.FAILURE;
		} catch (NumberFormatException e) {
			response.result = Result.FAILURE;
		} catch (NullPointerException e) {
			response.result = Result.FAILURE;
		}

		if (response.stories == null || response.stories.size() < 1) {
			response.result = Result.FAILURE;
		}

		return response;
	}

	private static Document getDocument(String urlExtension, String userCookie) throws IOException {
		Connection con;
		if (userCookie != null) con = ConnectionManager.authConnect(urlExtension, userCookie);
		else con = ConnectionManager.anonConnect(urlExtension);
		return con.get();
	}

	/** Creates a new timestamp if the more element exists on the page, else returns null. * */
	private static StoryTimestamp getNewTimestamp(Document doc) {
		// get new moreFnid & Timestamp
		Element more = doc.select("td.title a:matchesOwn(^More$)")
				.first();
		if (more == null) return null;

		String fnid = more.attr("href");
		// strip leading slash (/) since it's added by the urlExtension code above
		if (fnid.startsWith("/")) fnid = fnid.substring(1);

		StoryTimestamp timestamp = new StoryTimestamp();
		timestamp.fnid = fnid;
		timestamp.time = System.currentTimeMillis();
		return timestamp;
	}

	public static class StoryResponse {

		// NULL_RESPONSE :: A response with all fields set to `null`
		public static final StoryResponse NULL_RESPONSE = new StoryResponse();

		public Result result = null;
		public List<Story> stories = null;
		public StoryTimestamp timestamp = null;

		public boolean isNull() {
			return (this.equals(NULL_RESPONSE));
		}

		@Override
		public boolean equals(Object other) {
			if (other == null) return false;
			if (other == this) return true;
			if (!(other instanceof StoryResponse)) return false;

			StoryResponse o = (StoryResponse) other;

			return (result == o.result && stories == o.stories && timestamp == o.timestamp);
		}
	}

	/**
	 * Parses a story from the two tags we can reach with "td.title:containsOwn(.)" and
	 * "td.subtext"
	 * TODO figure out a better way of parsing than try / catching exceptions
	 */
	public static Story parseStory(Element title, Element subtext, boolean loggedIn) {
		Story story = new Story();
		story.position = parsePosition(title);

		String potentialJobsUrl = null;
		try {
			Element titleLink = title.select("td.title > a")
					.first();
			story.title = titleLink.text();

			// try to get url & domain, if it fails you're on a self post
			try {
				story.url = titleLink.attr("href");
				// if url starts with item?id, it's a self post & may potentially be a url for a jobs post
				if (story.url.startsWith("item?id=")) potentialJobsUrl = ConnectionManager.BASE_URL + "/" + story.url;
				story.domain = parseDomain(title);
			} catch (NullPointerException e) {
				story.url = null;
				story.domain = null;
			}

			story.ago = parseAgo(subtext);
			story.storyId = parseStoryId(subtext);

			// if the user is logged in, get isUpvoted, go_to, and auth
			if (loggedIn) {

				story.isUpvoted = true;
				story.whence = null;
				story.auth = null;

				Element voteAnchor = title.select("a[href^=vote]")
						.first();

				if (voteAnchor != null) {
					String[] voteHref = voteAnchor.attr("href")
							.split("[=&]");

					story.isUpvoted = false;
					story.whence = voteHref[voteHref.length - 1];
					story.auth = voteHref[7];
				}
			}

			story.numPoints = parseNumPoints(subtext);
			story.username = (subtext.select("a[href^=user]").text());
			story.numComments = parseNumComments(subtext);

		} catch (Exception e) {
			// this means it's a YCombinator jobs post
			story.storyId = 0;
			story.whence = null;
			story.numPoints = 0;
			story.username = null;
			story.numComments = 0;
			if (potentialJobsUrl != null) story.url = potentialJobsUrl;
		}
		return story;
	}

	/** try to get number of comments. If it fails there are 0 comments. * */
	private static int parseNumComments(Element subtext) {
		// last child is <a href="item?id=9029159">20 comments</a>
		try {
			int lastIndex = subtext.children().size() - 1;
			Element numComments = subtext.child(lastIndex);
			Matcher matcher = NUM_COMMENTS_PATTERN.matcher(numComments.text());
			if (matcher.find()) return Integer.parseInt(matcher.group());
		} catch (NumberFormatException e) {
			Log.i(TAG, "Error parsing number of comments from: ", e);// + numComments.text());
		} catch (Throwable t) {
			Log.i(TAG, "Other error", t);
		}
		return 0;
	}

	private static int parseNumPoints(Element subtext) {
		return Integer.parseInt(subtext.select("span.score").first().text().split("\\s")[0]);
	}

//	private static boolean parseHasUpvoteButton(Element voteAnchor) {
//		Elements voteButtons = voteAnchor.select("img[src=http://ycombinator.com/images/grayarrow.gif]");
//		return voteButtons.size() == 1;
//	}

	private static long parseStoryId(Element subtext) {
		return Long.parseLong(subtext.select("a[href^=item]")
				.attr("href")
				.split("=")[1]);
	}

	private static String parseAgo(Element subtext) {
		Element agoLink = subtext.select("a").get(1);
		return agoLink.text().replace("|", "").trim();
	}

	private static String parseDomain(Element title) {
		String domain = title.select("span.comhead")
				.first()
				.text()
				.trim();
		// trim parens from domain;
		domain = domain.substring(1, domain.length() - 1);
		return domain;
	}

	/** Get the stories position (i.e. 1st, 2nd, 3rd, etc) on the page. * */
	private static int parsePosition(Element title) {
		try {
			String position = title.child(0)
					.text()
					.replace(".", "");
			return Integer.parseInt(position);
		} catch (Exception e) { // TODO fix exception catch'em all!
			// this means we're on the comments page
			return NO_POSITION;
		}
	}
}
