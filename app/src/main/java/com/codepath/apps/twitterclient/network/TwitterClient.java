package com.codepath.apps.twitterclient.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.activeandroid.util.Log;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import java.util.ArrayList;
import java.util.Date;

public class TwitterClient extends OAuthBaseClient {

  public interface Handler<T> {
    void onSuccess(T value);
  }

  public static final String LOGGED_IN_USER_ID = "logged_in_user_id";
  public static final String TWITTER_PREFERENCES = "twitter";
  public static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
  public static final String REST_URL ="https://api.twitter.com/1.1";
  public static final String REST_CALLBACK_URL = "oauth://codepathtweets";
  static final String TAG = TwitterResponseHandler.class.getSimpleName();

  public TwitterClient(Context context) {
    super(context,
        REST_API_CLASS,
        REST_URL,
        context.getResources().getString(R.string.consumerKey),
        context.getResources().getString(R.string.consumerSecret),
        REST_CALLBACK_URL);
  }

  public void getHomeTimeline(final Handler<ArrayList<Tweet>> handler) {
    getHomeTimeline(null, handler);
  }

  public void getHomeTimeline(String olderThanId, final Handler<ArrayList<Tweet>> handler) {
    String apiUrl = getApiUrl("statuses/home_timeline.json");
    RequestParams params = new RequestParams();
    if (olderThanId != null) {
      params.put("max_id", olderThanId);
    }
    params.put("count", 25);
    getClient().get(apiUrl, params, new TwitterResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
          handler.onSuccess(Tweet.fromJson(response));
      }
    });
  }

  public void getLoggedInUser(final Handler<User> handler) {
    final SharedPreferences preferences = this.context.getSharedPreferences(TWITTER_PREFERENCES, this.context.MODE_PRIVATE);
    long loggedInUserId = preferences.getLong(LOGGED_IN_USER_ID, -1);
    if (loggedInUserId != -1) {
      User user = User.load(User.class, loggedInUserId);
      if (new Date().getTime() - user.updatedDateTime.getTime() < MILLIS_IN_DAY) {
        handler.onSuccess(user);
        return;
      }
    }

    String apiUrl = getApiUrl("account/verify_credentials.json");
    RequestParams params = new RequestParams();
    params.put("skip_status", 1);
    getClient().get(apiUrl, params, new TwitterResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        User user = User.fromJson(response);
        preferences.edit().putLong(LOGGED_IN_USER_ID, user.getId()).apply();
        handler.onSuccess(user);
      }
    });
  }

  public void postTweet(final String msg, final Handler<Tweet> handler) {
    String apiUrl = getApiUrl("statuses/update.json");
    RequestParams params = new RequestParams();
    params.put("status", msg);
    getClient().post(apiUrl, params, new TwitterResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        Tweet tweet = Tweet.fromJson(response);
        handler.onSuccess(tweet);
      }
    });
  }

  class TwitterResponseHandler extends JsonHttpResponseHandler {
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
      Log.e(TAG, errorResponse + "", throwable);
      String msg = "An unknown error has occurred.";
      if (statusCode == 0) {
        msg = "Unable to reach Twitter";
      }
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
  }
}
