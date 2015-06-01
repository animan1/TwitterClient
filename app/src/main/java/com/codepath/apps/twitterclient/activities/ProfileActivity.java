package com.codepath.apps.twitterclient.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.TargetLinearLayout;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.fragments.StreamFragment;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.network.TwitterClient;
import com.codepath.apps.twitterclient.utils.DimensionsHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;


public class ProfileActivity extends ActionBarActivity {

  public static final String USER_ID = "user_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    TwitterClient client = TwitterApplication.getTwitterClient();
    long userId = getIntent().getLongExtra(USER_ID, client.getLoggedInUserId());
    TwitterClient.UserRetriever userRetriever = client.getUserWithId(userId);
    userRetriever.cacheTTL(TwitterClient.MILLIS_IN_SECOND).submit(new TwitterClient.HandlerAdapter<User>() {
      @Override
      public void onSuccess(User user) {
        initUser(user);
      }
    });

    StreamFragment streamFragment = new StreamFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(StreamFragment.TIMELINE_INDEX, TwitterClient.TIMELINE.USER.ordinal());
    bundle.putLong(StreamFragment.USER_ID, userId);
    streamFragment.setArguments(bundle);
    getSupportFragmentManager().beginTransaction().replace(R.id.streamHolder, streamFragment).commit();
  }

  public void initUser(User user) {
    initBannerLayout(user);
    initStats(user);
  }

  private void initBannerLayout(User user) {
    TargetLinearLayout bannerLayout = (TargetLinearLayout) findViewById(R.id.bannerLayout);
    final int height = bannerLayout.getHeight();
    final int width = bannerLayout.getWidth();
    Picasso.with(ProfileActivity.this).load(user.bannerImageUrl).transform(new Transformation() {
      @Override
      public Bitmap transform(Bitmap source) {
        Bitmap updated = DimensionsHelper.scaleToFitHeight(source, height, true);
        return DimensionsHelper.cropToFitWidth(updated, width, true);
      }

      @Override
      public String key() {
        return "centerCrop h=" + height + " w=" + width;
      }
    }).into(bannerLayout);

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
