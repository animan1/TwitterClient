package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.codepath.apps.twitterclient.models.Tweet;

import java.util.List;

public class TweetAdapter extends ArrayAdapter<Tweet> {
  public TweetAdapter(Context context, List<Tweet> objects) {
    super(context, android.R.layout.simple_list_item_1, objects);
  }
}
