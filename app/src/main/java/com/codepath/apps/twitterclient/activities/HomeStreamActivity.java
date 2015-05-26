package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.adapters.TweetAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.network.TwitterClient;

import java.util.ArrayList;

public class HomeStreamActivity extends ActionBarActivity {
  private static final int COMPOSE_REQUEST_CODE = 100;

  TwitterClient client;
  private ListView tweetListView;
  private TweetAdapter tweetAdapter;
  private SwipeRefreshLayout timelineSwipeContainer;

  EndlessScrollListener scrollListener = new EndlessScrollListener(25) {
    @Override
    public void onLoadMore(int page, int totalItemsCount) {
      String lastId = tweetAdapter.getItem(totalItemsCount - 1).remoteId;
      client.getHomeTimeline(lastId, new TwitterClient.Handler<ArrayList<Tweet>>() {
        @Override
        public void onSuccess(ArrayList<Tweet> tweets) {
          tweetAdapter.addAll(tweets);
        }

        @Override
        public void onFailure(int statusCode, String error) {
          loading = false;
        }
      });
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_stream);
    client = TwitterApplication.getTwitterClient();
    tweetListView = (ListView) findViewById(R.id.tweetListView);
    tweetAdapter = new TweetAdapter(this, new ArrayList<Tweet>());
    timelineSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.timelineSwipeContainer);
    initTweetListView();
    initSwipeContainer();
  }

  private void initSwipeContainer() {
    timelineSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        refresh();
      }
    });
  }

  public void initTweetListView() {
    tweetAdapter.addAll(Tweet.getRecentCached(50));
    tweetListView.setAdapter(tweetAdapter);
    refresh();
  }

  private void refresh() {
    // don't allow paging while we're fetching new results
    tweetListView.setOnScrollListener(null);
    client.getHomeTimeline(new TwitterClient.Handler<ArrayList<Tweet>>() {
      @Override
      public void onSuccess(ArrayList<Tweet> tweetList) {
        tweetAdapter.clear();
        tweetAdapter.addAll(tweetList);
        tweetAdapter.notifyDataSetChanged();
        tweetListView.setOnScrollListener(scrollListener);
        timelineSwipeContainer.setRefreshing(false);
      }

      @Override
      public void onFailure(int statusCode, String error) {
        timelineSwipeContainer.setRefreshing(false);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_home_stream, menu);
    MenuItem composeMenuItem = menu.findItem(R.id.action_compose);
    composeMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        onCompose();
        return false;
      }
    });
    return super.onCreateOptionsMenu(menu);
  }

  private void onCompose() {
    Intent i = new Intent(this, ComposeActivity.class);
    startActivityForResult(i, COMPOSE_REQUEST_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
      long tweetId = data.getLongExtra(ComposeActivity.TWEET_ID, -1);
      Tweet tweet = Tweet.load(Tweet.class, tweetId);
      tweetAdapter.insert(tweet, 0);
      tweetListView.smoothScrollToPosition(0);
    }
  }
}
