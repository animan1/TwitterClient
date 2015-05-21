package com.codepath.apps.twitterclient.network;

import android.content.Context;
import com.codepath.apps.twitterclient.R;
import com.codepath.oauth.OAuthBaseClient;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

public class TwitterClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL ="http://api.twitter.com";
	public static final String REST_CALLBACK_URL = "oauth://codepathtweets";

	public TwitterClient(Context context) {
		super(context,
        REST_API_CLASS,
        REST_URL,
        context.getResources().getString(R.string.consumerKey),
        context.getResources().getString(R.string.consumerSecret),
        REST_CALLBACK_URL);
	}
}