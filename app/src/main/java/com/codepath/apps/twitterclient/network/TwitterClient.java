package com.codepath.apps.twitterclient.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
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
import java.util.LinkedList;

public class TwitterClient extends OAuthBaseClient {

  public enum TIMELINE {
    HOME("home_timeline"),
    MENTIONS("mentions_timeline"),
    USER("user_timeline");

    private final String relativeUrl;

    TIMELINE(String jsonName) {
      this.relativeUrl = "statuses/" + jsonName + ".json";
    }
  };

  public interface Handler<T> {
    void onSuccess(T value);
    void onFailure(int statusCode, String error);
  }

  public static class HandlerAdapter<T> implements Handler<T> {
    @Override
    public void onSuccess(T value) {}
    @Override
    public void onFailure(int statusCode, String error) {}
  }

  public static final String LOGGED_IN_USER_ID = "logged_in_user_id";
  public static final String TWITTER_PREFERENCES = "twitter";
  public static final long MILLIS_IN_SECOND = 100;
  public static final long MILLIS_IN_DAY = MILLIS_IN_SECOND * 60 * 60 * 24;
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

  public class Requester<E> {
    final String apiUrl;
    final RequestParams params;
    final TwitterResponseHandler<E> responseHandler;

    public Requester(String relativeUrl, TwitterResponseHandler responseHandler) {
      this.apiUrl = getApiUrl(relativeUrl);
      this.params = new RequestParams();
      this.responseHandler = responseHandler;
    }

    void get(Handler<E> handler) {
      responseHandler.handlers.add(handler);
      if (!Utils.isNetworkAvailable(context)) {
        responseHandler.onFailure(0, null, null, (JSONObject) null);
        return;
      }
      getClient().get(apiUrl, params, responseHandler);
    }

    void post(Handler<E> handler) {
      responseHandler.handlers.add(handler);
      if (!Utils.isNetworkAvailable(context)) {
        responseHandler.onFailure(0, null, null, (JSONObject) null);
        return;
      }
      getClient().post(apiUrl, params, responseHandler);
    }
  }

  public TimelineRetriever getTimeline(TIMELINE timeline) {
    return new TimelineRetriever(timeline);
  }

  public class TimelineRetriever extends Requester<ArrayList<Tweet>> {

    public TimelineRetriever(final TIMELINE timeline) {
      super(timeline.relativeUrl, new TwitterResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, final JSONArray response) {
          new AsyncTask<JSONArray, Void, ArrayList<Tweet>>() {
            @Override
            protected ArrayList<Tweet> doInBackground(JSONArray ... params) {
              JSONArray response = params[0];
              return Tweet.fromJson(response, timeline);
            }

            @Override
            protected void onPostExecute(ArrayList<Tweet> tweets) {
              onSuccess(tweets);
            }
          }.execute(response);
        }
      });
      params.put("count", 25);
    }

    public TimelineRetriever olderThan(String olderThanId) {
      params.put("max_id", olderThanId);
      return this;
    }

    public void submit(final Handler<ArrayList<Tweet>> handler) {
      get(handler);
    }
  }

  public UserRetriever getLoggedInUser() {
    long loggedInUserId = preferences.getLong(LOGGED_IN_USER_ID, -1);
    return new UserRetriever("account/verify_credentials.json", loggedInUserId).extraHandler(new HandlerAdapter<User>() {
      @Override
      public void onSuccess(User user) {
        preferences.edit().putLong(LOGGED_IN_USER_ID, user.getId()).apply();
      }
    });
  }

  public User getCachedUser(long id) {
    return id == -1 ? null : User.load(User.class, id);
  }

  public class UserRetriever extends Requester {
    private final long userId;
    private long cacheTTL = MILLIS_IN_DAY;

    public UserRetriever(String relativeUrl, final long userId) {
      super(relativeUrl, new TwitterResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
          User user = User.fromJson(response);
          onSuccess(user);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
          User user = getCachedUser(userId);
          if (user == null) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
          } else {
            onSuccess(user);
          }
        }
      });
      params.put("skip_status", 1);
      this.userId = userId;
    }

    public UserRetriever extraHandler(Handler<User> handler) {
      this.responseHandler.handlers.add(handler);
      return this;
    }

    public UserRetriever cacheTTL(long ttl) {
      this.cacheTTL = ttl;
      return this;
    }

    public void submit(final Handler<User> handler) {
      User user = getCachedUser(userId);
      if ((user != null) && (new Date().getTime() - user.updatedDateTime.getTime() < cacheTTL)) {
        handler.onSuccess(user);
        return;
      }

      get(handler);
    }
  }

  public TweetSubmitter postTweet() {
    return new TweetSubmitter();
  }

  public class TweetSubmitter extends Requester<Tweet> {
    public TweetSubmitter() {
      super("statuses/update.json", new TwitterResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
          onSuccess(Tweet.fromJson(response, TIMELINE.HOME, TIMELINE.USER));
        }
      });
    }

    public TweetSubmitter message(String msg) {
      params.put("status", msg);
      return this;
    }

    public void submit(final Handler<Tweet> handler) {
      post(handler);
    }
  }

  class TwitterResponseHandler<E> extends JsonHttpResponseHandler {
    final LinkedList<Handler<E>> handlers = new LinkedList<>();

    public TwitterResponseHandler(Handler<E> ... handlers) {
      if (handlers != null) {
        for (Handler<E> handler : handlers) {
          this.handlers.add(handler);
        }
      }
    }

    public void onSuccess(E value) {
      for (Handler<E> handler : handlers) {
        handler.onSuccess(value);
      }
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
      for (Handler<E> handler : handlers) {
        handler.onFailure(statusCode, msg);
      }
    }
  }
}
