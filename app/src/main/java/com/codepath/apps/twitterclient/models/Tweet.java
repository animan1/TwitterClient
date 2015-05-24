package com.codepath.apps.twitterclient.models;

import android.util.Log;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
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

  public Tweet() {
    super();
  }

  public Tweet(JSONObject tweetJson) {
    set(tweetJson);
  }

  private void set(JSONObject tweetJson) {
    try {
      this.remoteId = getRemoteId(tweetJson);
      this.user = User.fromJson(tweetJson.getJSONObject("user"));
      this.body = tweetJson.getString("text");
      this.createdDatetime = CREATED_AT_FORMAT.parse(tweetJson.getString("created_at"));
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

  public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
    ArrayList<Tweet> tweets = new ArrayList<>(jsonArray.length());

    for (int i=0; i < jsonArray.length(); i++) {
      Tweet tweet = fromJson(jsonArray.optJSONObject(i));
      tweets.add(tweet);
    }

    return tweets;
  }

  public static Tweet fromJson(JSONObject tweetJson) {
    try {
      String remoteId = getRemoteId(tweetJson);
      Tweet tweet = newOrLoad(remoteId);
      tweet.set(tweetJson);
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
