package com.codepath.apps.twitterclient.models;

import android.util.Log;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

@Table(name = "Tweets")
public class Tweet extends Model {
  @Column(name = "userId")
  public String userId;
  @Column(name = "userHandle")
  public String userHandle;
  @Column(name = "timestamp")
  public String timestamp;
  @Column(name = "body")
  public String body;
  @Column(name = "profileImageUrl")
  public String profileImageUrl;

  public Tweet() {
    super();
  }

  public Tweet(JSONObject tweetJson) {
    this();

    try {
      JSONObject userObject = tweetJson.getJSONObject("user");
      this.userId = userObject.getString("id_str");
      this.userHandle = userObject.getString("screen_name");
      this.profileImageUrl = userObject.getString("profile_image_url_https");

      this.body = tweetJson.getString("text");
    } catch (JSONException e) {
      Log.i(null, tweetJson + "");
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return body;
  }

  public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
    ArrayList<Tweet> tweets = new ArrayList<>(jsonArray.length());

    for (int i=0; i < jsonArray.length(); i++) {
      try {
        JSONObject tweetJson = jsonArray.getJSONObject(i);

        Tweet tweet = new Tweet(tweetJson);
        tweet.save();
        tweets.add(tweet);
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }
    }

    return tweets;
  }
}
