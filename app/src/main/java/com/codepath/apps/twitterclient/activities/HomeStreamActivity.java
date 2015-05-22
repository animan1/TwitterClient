package com.codepath.apps.twitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.ListView;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.network.TwitterClient;

import java.util.ArrayList;

public class HomeStreamActivity extends ActionBarActivity {
  TwitterClient client;
  private ListView tweetListView;
  private TweetAdapter tweetAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_stream);
    client = TwitterApplication.getTwitterClient();
    tweetListView = (ListView) findViewById(R.id.tweetListView);
    tweetAdapter = new TweetAdapter(this, new ArrayList<Tweet>());
    tweetListView.setAdapter(tweetAdapter);
    initTweetListView();
  }

  public void initTweetListView() {
    client.getHomeTimeline(new TwitterClient.TweetListHandler() {
      @Override
      public void onSuccess(ArrayList<Tweet> tweetList) {
        tweetAdapter.addAll(tweetList);
        tweetAdapter.notifyDataSetChanged();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_home_stream, menu);
    return true;
  }
}
