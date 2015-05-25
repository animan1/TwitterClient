package com.codepath.apps.twitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
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
  private TextView countTextView;

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
    this.tweetEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {
        int len = s.length();
        int remaining = 140 - len;
        tweetMenuItem.setEnabled((len > 0) && (remaining >= 0));
        countTextView.setText("" + remaining);
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

      @Override
      public void onFailure(int statusCode, String error) {}
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_compose, menu);
    tweetMenuItem = menu.findItem(R.id.action_tweet);
    MenuItem countMenuItem = menu.findItem(R.id.action_count);
    this.countTextView = (TextView) countMenuItem.getActionView().findViewById(R.id.textView);
    initTweetMenuItem();
    initCountTextView();
    return super.onCreateOptionsMenu(menu);
  }

  private void initCountTextView() {
    this.countTextView.setText("140");
  }

  private void initTweetMenuItem() {
    tweetMenuItem.setEnabled(false);
    tweetMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        onTweet();
        return false;
      }
    });
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

      @Override
      public void onFailure(int statusCode, String error) {}
    });
  }
}
