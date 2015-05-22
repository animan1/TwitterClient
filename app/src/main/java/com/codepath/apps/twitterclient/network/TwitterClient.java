package com.codepath.apps.twitterclient.network;

import android.content.Context;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.apache.http.Header;
import org.json.JSONArray;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import java.util.ArrayList;

public class TwitterClient extends OAuthBaseClient {
  public interface TweetListHandler {
    public void onSuccess(ArrayList<Tweet> tweetList);
  }
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL ="https://api.twitter.com/1.1";
	public static final String REST_CALLBACK_URL = "oauth://codepathtweets";

	public TwitterClient(Context context) {
		super(context,
        REST_API_CLASS,
        REST_URL,
        context.getResources().getString(R.string.consumerKey),
        context.getResources().getString(R.string.consumerSecret),
        REST_CALLBACK_URL);
	}

  public void getHomeTimeline(final TweetListHandler handler) {
    String apiUrl = getApiUrl("statuses/home_timeline.json");
    RequestParams params = new RequestParams();
    params.put("count", 25);
    getClient().get(apiUrl, params, new JsonHttpResponseHandler() {
      @Override
      public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        handler.onSuccess(Tweet.fromJson(response));
      }
    });
  }
}