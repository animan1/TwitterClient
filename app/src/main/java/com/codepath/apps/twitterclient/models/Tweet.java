package com.codepath.apps.twitterclient.models;

import android.util.Log;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.codepath.apps.twitterclient.network.TwitterClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "Tweets")
public class Tweet extends Model {
  final static DateFormat CREATED_AT_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZ yyyy");
  @Column(name = "remoteId", unique = true, notNull = true)
  public String remoteId;
  @Column(name = "body", notNull = true)
  public String body;
  @Column(name = "createdDatetime", notNull = true)
  public Date createdDatetime;
  @Column(name = "user", notNull = true)
  public User user;
  @Column(name = "isHome", notNull = true)
  public boolean isHome;
  @Column(name = "isMention", notNull = true)
  public boolean isMention;
  @Column(name = "isUser", notNull = true)
  public boolean isUser;


  public Tweet() {
    super();
  }

  public Tweet(JSONObject tweetJson, TwitterClient.TIMELINE ... timelines) {
    set(tweetJson, timelines);
  }

  private void set(JSONObject tweetJson, TwitterClient.TIMELINE ... timelines) {
    try {
      this.remoteId = getRemoteId(tweetJson);
      this.user = User.fromJson(tweetJson.getJSONObject("user"));
      this.body = tweetJson.getString("text");
      this.createdDatetime = CREATED_AT_FORMAT.parse(tweetJson.getString("created_at"));
      for (TwitterClient.TIMELINE timeline : timelines) {
        switch (timeline) {
          case HOME:
            this.isHome = true;
            break;
          case MENTIONS:
            this.isMention = true;
            break;
          case USER:
            this.isUser = true;
            break;
          default:
            throw new UnsupportedOperationException("New timeline type not supported");
        }
      }
    } catch (JSONException | ParseException e) {
      Log.i(null, tweetJson + "");
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return body;
  }

  private static String getRemoteId(JSONObject tweetJson) throws JSONException {
    return tweetJson.getString("id_str");
  }

  public static From getCached(TwitterClient.TIMELINE timeline) {
    From results = new Select().from(Tweet.class).orderBy("-remoteId");
    switch (timeline) {
      case HOME:
        return results.where("isHome = ?", true);
      case MENTIONS:
        return results.where("isMention = ?", true);
      case USER:
        return results.where("isUser = ?", true);
      default:
        throw new UnsupportedOperationException("New timeline type not supported");
    }
  }

  public static ArrayList<Tweet> fromJson(JSONArray jsonArray, TwitterClient.TIMELINE ... timelines) {
    ArrayList<Tweet> tweets = new ArrayList<>(jsonArray.length());

    for (int i=0; i < jsonArray.length(); i++) {
      Tweet tweet = fromJson(jsonArray.optJSONObject(i), timelines);
      tweets.add(tweet);
    }

    return tweets;
  }

  public static Tweet fromJson(JSONObject tweetJson, TwitterClient.TIMELINE ... timelines) {
    try {
      String remoteId = getRemoteId(tweetJson);
      Tweet tweet = newOrLoad(remoteId);
      tweet.set(tweetJson, timelines);
      tweet.save();
      return tweet;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Tweet newOrLoad(String remoteId) {
    List<Tweet> tweetList = new Select().from(Tweet.class).where("remoteId = ?", remoteId).execute();
    return tweetList.isEmpty() ? new Tweet() : tweetList.get(0);
  }
}
