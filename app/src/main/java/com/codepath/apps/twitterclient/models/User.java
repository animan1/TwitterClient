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
      this.remoteId = getRemoteId(userObject);
      this.username = userObject.getString("screen_name");
      this.displayName = userObject.getString("name");
      this.profileImageUrl = userObject.getString("profile_image_url_https");
      this.updatedDateTime = new Date();
      this.bannerImageUrl = userObject.getString("profile_background_image_url_https");
      this.tagLine = userObject.getString("description");
      this.numTweets = userObject.getInt("statuses_count");
      this.numFollowing = userObject.getInt("friends_count");
      this.numFollowers = userObject.getInt("followers_count");
    } catch (JSONException e) {
      Log.i(null, userObject + "");
      e.printStackTrace();
    }
  }

  static String getRemoteId(JSONObject userObject) throws JSONException {
    return userObject.getString("id_str");
  }

  public static User fromJson(JSONObject userObject) {
    try {
      List<User> userList = new Select().from(User.class).where("remoteId = ?", getRemoteId(userObject)).execute();
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
