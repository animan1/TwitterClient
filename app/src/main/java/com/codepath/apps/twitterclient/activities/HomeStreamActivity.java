package com.codepath.apps.twitterclient.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.fragments.HomeStreamFragment;

public class HomeStreamActivity extends ActionBarActivity {
  private static final String HOME_TAG = "home";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_stream);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.tabContainer, new HomeStreamFragment(), HOME_TAG)
        .commit();
  }
}
