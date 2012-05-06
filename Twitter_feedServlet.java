package com.gae.aravinth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.*;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Frequency based word count sort utility class for Twitter tweets
 * @author  Aravinth Bheemaraj
 * @version 1.00, 05/05/12
 */

@SuppressWarnings("serial")
public class Twitter_feedServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    resp.setContentType("text/html");

    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey("################")
        .setOAuthConsumerSecret("###########################")
        .setOAuthAccessToken("###########################")
        .setOAuthAccessTokenSecret("#####################");
    TwitterFactory tf = new TwitterFactory(cb.build());
    Twitter twitter = tf.getInstance();

    try {
      User user = twitter.verifyCredentials();
      String friend = req.getParameter("user");
      String regexPattern = "[^a-zA-Z@# ]";
      HashMap<String, Integer> tweetWordCount = new HashMap<String, Integer>();
      List<Status> result = new ArrayList<Status>();
      int pages = Math.max(user.getStatusesCount()/20 + 1, 50);
			
      for (int i = 1; i <= pages; i++) {
        result.addAll(twitter.getUserTimeline(friend, new Paging(i)));
      }

      resp.getWriter().println(
          "<h3> Showing @" + friend + "'s word list, sorted based on frequency. </h3>");
      resp.getWriter().println(
          "<p> Indexed " + result.size() + " tweets </p>");
      resp.getWriter().println("Click " +
          "<a href=\"//twitter.com/" + friend + "\">" + friend + "</a>" +
          " to view the tweets");

      for (Status status : result) {
        String[] indivWords = status.getText()
            .replaceAll(regexPattern, "").split(" ");
        for (String word : indivWords) {
          word = word.trim().toLowerCase();
          if (!word.equals("")) {
            if (tweetWordCount.containsKey(word)) {
              tweetWordCount.put(word, tweetWordCount.get(word).intValue() + 1);
            } else {
              tweetWordCount.put(word, 1);
            }
          }
        }
      }

      ValueComparator comp = new ValueComparator(tweetWordCount);
      TreeMap<String, Integer> freq_sorted_map = new TreeMap<String, Integer>(comp);
      freq_sorted_map.putAll(tweetWordCount);

      resp.getWriter().println(
          "<table border=\"1\"> " +
	  "<tr> <th> Word </th> <th> Count </th> </tr>");

      for (String key : freq_sorted_map.keySet()) {
        resp.getWriter().println(
            " <tr> <td> " + key + " </td> " + " <td> " +
            tweetWordCount.get(key) + " </td> " +
            " </tr> ");
       }

       resp.getWriter().println("</table>");

    } catch (TwitterException te) {
      resp.getWriter().println(te.getMessage());
    }

    resp.getWriter().println(
        "<hr>Click <a href=\"/twitter.html\">here</a> to go back");
  }

  static class ValueComparator implements Comparator<Object> {
    Map<String, Integer> base;

    public ValueComparator(Map<String, Integer> base) {
      this.base = base;
    }

    public int compare(Object a, Object b) {
      if ((Integer) base.get(a) < (Integer) base.get(b)) {
        return 1;
      } else {
        return -1;
      }
    }
  }

}

