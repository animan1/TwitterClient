package com.codepath.apps.twitterclient.models;

import android.util.Log;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

@Table(name = "User")
public class User extends Model {
  private static final String TAG = User.class.getSimpleName();
  @Column(name = "remoteId", unique = true, notNull = true)
  public String remoteId;
  @Column(name = "username", notNull = true)
  public String username;
  @Column(name = "displayName", notNull = true)
  public String displayName;
  @Column(name = "profileImageUrl", notNull = true)
  public String profileImageUrl;
  @Column(name = "updatedDateTime", notNull = true)
  public Date updatedDateTime;
  @Column(name = "bannerImageUrl")
  public String bannerImageUrl;
  @Column(name = "tagLine")
  public String tagLine;
  @Column(name = "numTweets")
  public int numTweets;
  @Column(name = "numFollowing")
  public int numFollowing;
  @Column(name = "numFollowers")
  public int numFollowers;

  public User() {}

  public User(JSONObject userObject) {
    set(userObject);
  }

  private void set(JSONObject userObject) {
    try {
      this.remoteId = optRemoteId(userObject, this.remoteId);
      this.username = userObject.optString("screen_name", this.username);
      this.displayName = userObject.optString("name", this.displayName);
      this.profileImageUrl = userObject.optString("profile_image_url_https", this.profileImageUrl);
      this.updatedDateTime = new Date();
      this.bannerImageUrl = userObject.optString("profile_banner_url", this.bannerImageUrl);
      this.tagLine = userObject.optString("description", this.tagLine);
      this.numTweets = userObject.optInt("statuses_count", this.numTweets);
      this.numFollowing = userObject.optInt("friends_count", this.numFollowing);
      this.numFollowers = userObject.optInt("followers_count", this.numFollowers);
    } catch (JSONException e) {
      Log.i(TAG, userObject + "");
      e.printStackTrace();
    }
  }

  static String optRemoteId(JSONObject userObject, String defaultValue) throws JSONException {
    return userObject.optString("id_str", defaultValue);
  }

  public static User fromJson(JSONObject userObject) {
    try {
      List<User> userList = new Select().from(User.class).where("remoteId = ?", optRemoteId(userObject, null)).execute();
      User user = userList.isEmpty() ? new User() : userList.get(0);
      user.set(userObject);
      user.save();
      return user;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
