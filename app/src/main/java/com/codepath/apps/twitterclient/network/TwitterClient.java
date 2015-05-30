package com.codepath.apps.twitterclient.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.activeandroid.util.Log;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.Utils;
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

  public enum TIMELINE {
    HOME("home_timeline"),
    MENTIONS("mentions_timeline");

    private final String relativeEndpoint;

    TIMELINE(String relativeEndpoint) {
      this.relativeEndpoint = relativeEndpoint;
    }
  };

  public interface Handler<T> {
    void onSuccess(T value);
    void onFailure(int statusCode, String error);
  }

  public static final String LOGGED_IN_USER_ID = "logged_in_user_id";
  public static final String TWITTER_PREFERENCES = "twitter";
  public static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
  public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
  public static final String REST_URL ="https://api.twitter.com/1.1";
  public static final String REST_CALLBACK_URL = "oauth://codepathtweets";
  static final String TAG = TwitterResponseHandler.class.getSimpleName();

  private final SharedPreferences preferences;

  public TwitterClient(Context context) {
    super(context,
        REST_API_CLASS,
        REST_URL,
        context.getResources().getString(R.string.consumerKey),
        context.getResources().getString(R.string.consumerSecret),
        REST_CALLBACK_URL);
    this.preferences = this.context.getSharedPreferences(TWITTER_PREFERENCES, this.context.MODE_PRIVATE);
  }

  public TimelineRetriever getTimeline(TIMELINE timeline) {
    return new TimelineRetriever(timeline);
  }

  public class TimelineRetriever {
    private final String apiUrl;
    private final RequestParams params;

    public TimelineRetriever(TIMELINE timeline) {
      apiUrl = getApiUrl("statuses/" + timeline.relativeEndpoint + ".json");
      params = new RequestParams();
      params.put("count", 25);
    }

    public TimelineRetriever olderThan(String olderThanId) {
      params.put("max_id", olderThanId);
      return this;
    }

    public void submit(final Handler<ArrayList<Tweet>> handler) {
      get(apiUrl, params, new TwitterResponseHandler(handler) {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
          handler.onSuccess(Tweet.fromJson(response));
        }
      });
    }
  }

  public void getLoggedInUser(final Handler<User> handler) {
    User user = getCachedUser();
    if ((user != null) && (new Date().getTime() - user.updatedDateTime.getTime() < MILLIS_IN_DAY)) {
      handler.onSuccess(user);
      return;
    }

    String apiUrl = getApiUrl("account/verify_credentials.json");
    RequestParams params = new RequestParams();
    params.put("skip_status", 1);
  }

  public User getCachedUser() {
    long loggedInUserId = preferences.getLong(LOGGED_IN_USER_ID, -1);
    return loggedInUserId == -1 ? null : User.load(User.class, loggedInUserId);
  }

  public void postTweet(final String msg, final Handler<Tweet> handler) {
    String apiUrl = getApiUrl("statuses/update.json");
    RequestParams params = new RequestParams();
    params.put("status", msg);
    post(apiUrl, params, new TwitterResponseHandler(handler) {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        Tweet tweet = Tweet.fromJson(response);
        handler.onSuccess(tweet);
      }
    });
  }

  private void get(String apiUrl, RequestParams params, TwitterResponseHandler handler) {
    if (!Utils.isNetworkAvailable(context)) {
      handler.onFailure(0, null, null, (JSONObject) null);
      return;
    }
    getClient().get(apiUrl, params, handler);
  }

  private void post(String apiUrl, RequestParams params, TwitterResponseHandler handler) {
    if (!Utils.isNetworkAvailable(context)) {
      handler.onFailure(0, null, null, (JSONObject) null);
      return;
    }
    getClient().post(apiUrl, params, handler);
  }

  class TwitterResponseHandler extends JsonHttpResponseHandler {
    final Handler<?> activityHandler;

    TwitterResponseHandler(Handler<?> activityHandler) {
      this.activityHandler = activityHandler;
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
      Log.e(TAG, errorResponse + "", throwable);
      String msg = "An unknown error has occurred: " + statusCode;
      if (statusCode == 0) {
        msg = "Unable to reach Twitter";
      } else if (statusCode == 429) {
        msg = "Request limit exceeded";
      }
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
      activityHandler.onFailure(statusCode, msg);
    }
  }
}
