package com.codepath.apps.twitterclient.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.activities.ComposeActivity;
import com.codepath.apps.twitterclient.activities.EndlessScrollListener;
import com.codepath.apps.twitterclient.activities.ProfileActivity;
import com.codepath.apps.twitterclient.adapters.TweetAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterClient;

import java.util.ArrayList;
import java.util.List;

public class StreamFragment extends Fragment {
  private static final int COMPOSE_REQUEST_CODE = 100;
  public static final String TIMELINE_INDEX = "timeline_index";

  TwitterClient client;
  TwitterClient.TIMELINE timeline;

  private ListView tweetListView;
  private TweetAdapter tweetAdapter;
  private SwipeRefreshLayout timelineSwipeContainer;

  EndlessScrollListener scrollListener = new EndlessScrollListener() {
    @Override
    public void onLoadMore(int page, int totalItemsCount) {
      String lastId = tweetAdapter.getItem(totalItemsCount - 1).remoteId;
      client.getTimeline(timeline).olderThan(lastId).submit(new TwitterClient.Handler<ArrayList<Tweet>>() {
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    tweetAdapter = new TweetAdapter(getActivity(), new ArrayList<Tweet>());
    client = TwitterApplication.getTwitterClient();
    int timeline_index = getArguments().getInt(TIMELINE_INDEX);
    this.timeline = TwitterClient.TIMELINE.values()[timeline_index];
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_stream, container, false);

    tweetListView = (ListView) view.findViewById(R.id.tweetListView);
    timelineSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.timelineSwipeContainer);
    initTweetListView();
    initSwipeContainer();

    return view;
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
    List<Tweet> cached = Tweet.getCached(timeline).limit(50).execute();
    tweetAdapter.addAll(cached);
    tweetListView.setAdapter(tweetAdapter);
    tweetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        onUserClicked(tweetAdapter.getItem(i).user);
      }
    });
    refresh();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.menu_stream, menu);
    MenuItem composeMenuItem = menu.findItem(R.id.action_compose);
    composeMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        onCompose();
        return false;
      }
    });
  }

  private void onUserClicked(User user) {
    Intent i = new Intent(getActivity(), ProfileActivity.class);
    i.putExtra(ProfileActivity.USER_ID, user.getId());
    startActivity(i);
  }

  private void onCompose() {
    Intent i = new Intent(getActivity(), ComposeActivity.class);
    startActivityForResult(i, COMPOSE_REQUEST_CODE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == FragmentActivity.RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
      long tweetId = data.getLongExtra(ComposeActivity.TWEET_ID, -1);
      Tweet tweet = Tweet.load(Tweet.class, tweetId);
      tweetAdapter.insert(tweet, 0);
      tweetListView.smoothScrollToPosition(0);
    }
  }

  private void refresh() {
    // don't allow paging while we're fetching new results
    tweetListView.setOnScrollListener(null);
    client.getTimeline(timeline).submit(new TwitterClient.Handler<ArrayList<Tweet>>() {
      @Override
      public void onSuccess(ArrayList<Tweet> tweetList) {
        tweetAdapter.clear();
        tweetAdapter.addAll(tweetList);
        tweetListView.setOnScrollListener(scrollListener);
        timelineSwipeContainer.setRefreshing(false);
      }

      @Override
      public void onFailure(int statusCode, String error) {
        timelineSwipeContainer.setRefreshing(false);
      }
    });
  }
}
