package com.codepath.apps.twitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

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

    initBannerLayout();
  }

  private void initBannerLayout() {
    TwitterApplication.getTwitterClient().getLoggedInUser().cacheTTL(TwitterClient.MILLIS_IN_SECOND).submit(new TwitterClient.HandlerAdapter<User>() {
      @Override
      public void onSuccess(User user) {
        TargetLinearLayout bannerLayout = (TargetLinearLayout) findViewById(R.id.bannerLayout);
        Picasso.with(ProfileActivity.this).load(user.bannerImageUrl).into(bannerLayout);
      }
    });
  }
}
