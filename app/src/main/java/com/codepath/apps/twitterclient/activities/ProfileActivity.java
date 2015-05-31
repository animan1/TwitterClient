package com.codepath.apps.twitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.TargetLinearLayout;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterClient;
import com.squareup.picasso.Picasso;


public class ProfileActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    TwitterApplication.getTwitterClient().getLoggedInUser().cacheTTL(TwitterClient.MILLIS_IN_SECOND).submit(new TwitterClient.HandlerAdapter<User>() {
      @Override
      public void onSuccess(User user) {
        initUser(user);
      }
    });
  }

  public void initUser(User user) {
    initBannerLayout(user);
    initStats(user);
  }

  private void initBannerLayout(User user) {
    TargetLinearLayout bannerLayout = (TargetLinearLayout) findViewById(R.id.bannerLayout);
    Picasso.with(ProfileActivity.this).load(user.bannerImageUrl).into(bannerLayout);

    ImageView profileImageView = (ImageView) findViewById(R.id.profileImageView);
    Picasso.with(ProfileActivity.this).load(user.profileImageUrl).into(profileImageView);

    TextView authorTextView = (TextView) findViewById(R.id.authorTextView);
    authorTextView.setText(user.displayName);

    TextView usernameTextView = (TextView) findViewById(R.id.usernameTextView);
    usernameTextView.setText("@" + user.username);
  }

  private void initStats(User user) {
    TextView tweetCountTextView = (TextView) findViewById(R.id.tweetCountTextView);
    tweetCountTextView.setText(user.numTweets + "");

    TextView followingCountTextView = (TextView) findViewById(R.id.followingCountTextView);
    followingCountTextView.setText(user.numFollowing + "");

    TextView followersCountTextView = (TextView) findViewById(R.id.followersCountTextView);
    followersCountTextView.setText(user.numFollowers + "");
  }
}
