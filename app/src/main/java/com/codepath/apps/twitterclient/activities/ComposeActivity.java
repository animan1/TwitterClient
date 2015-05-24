package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterClient;
import com.squareup.picasso.Picasso;

public class ComposeActivity extends ActionBarActivity {

  public static final String TWEET_ID = "tweet_id";

  private TwitterClient client;
  private ImageView profileImageView;
  private TextView usernameTextView;
  private TextView authorTextView;
  private EditText tweetEditText;
  private MenuItem tweetMenuItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_compose);
    this.client = TwitterApplication.getTwitterClient();
    this.profileImageView = (ImageView) findViewById(R.id.profileImageView);
    this.usernameTextView = (TextView) findViewById(R.id.usernameTextView);
    this.authorTextView = (TextView) findViewById(R.id.authorTextView);
    this.tweetEditText = (EditText) findViewById(R.id.tweetEditText);
    initUserInfo();
    initTweetEdit();
  }

  private void initTweetEdit() {
    this.tweetEditText.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        tweetMenuItem.setEnabled(tweetEditText.getText().length() > 0);
        return false;
      }
    });
  }

  private void initUserInfo() {
    this.client.getLoggedInUser(new TwitterClient.Handler<User>() {
      @Override
      public void onSuccess(User user) {
        Picasso.with(ComposeActivity.this).load(user.profileImageUrl).placeholder(R.drawable.profile).into(profileImageView);
        usernameTextView.setText("@" + user.username);
        authorTextView.setText(user.displayName);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_compose, menu);
    tweetMenuItem = menu.findItem(R.id.action_tweet);
    tweetMenuItem.setEnabled(false);
    tweetMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        onTweet();
        return false;
      }
    });
    return super.onCreateOptionsMenu(menu);
  }

  private void onTweet() {
    this.client.postTweet(this.tweetEditText.getText().toString(), new TwitterClient.Handler<Tweet>() {
      @Override
      public void onSuccess(Tweet tweet) {
        Intent data = new Intent();
        data.putExtra(TWEET_ID, tweet.getId());
        setResult(RESULT_OK, data);
        ComposeActivity.this.finish();
      }
    });
  }
}
