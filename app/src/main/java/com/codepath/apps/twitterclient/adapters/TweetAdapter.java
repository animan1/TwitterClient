package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class TweetAdapter extends ArrayAdapter<Tweet> {
  private static final int TIMESTAMP_FLAGS = DateUtils.FORMAT_ABBREV_ALL;
  private static final long MIN_RESOLUTION = DateUtils.MINUTE_IN_MILLIS;

  class ViewHolder {
    ImageView profileImageView;
    TextView authorTextView;
    TextView usernameTextView;
    TextView bodyTextView;
    TextView timestampTextView;
  }

  public TweetAdapter(Context context, List<Tweet> objects) {
    super(context, R.layout.tweet_item, objects);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      LayoutInflater inflater = LayoutInflater.from(getContext());
      convertView = inflater.inflate(R.layout.tweet_item, parent, false);
      viewHolder.profileImageView = (ImageView) convertView.findViewById(R.id.profileImageView);
      viewHolder.authorTextView = (TextView) convertView.findViewById(R.id.authorTextView);
      viewHolder.usernameTextView = (TextView) convertView.findViewById(R.id.usernameTextView);
      viewHolder.timestampTextView = (TextView) convertView.findViewById(R.id.timestampTextView);
      viewHolder.bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    Tweet tweet = getItem(position);

    if (tweet.user.profileImageUrl != null) {
      Picasso.with(getContext()).load(tweet.user.profileImageUrl).placeholder(R.drawable.profile).into(viewHolder.profileImageView);
    }
    else {
      viewHolder.profileImageView.setImageDrawable(getDrawable(R.drawable.profile));
    }

    long now = new Date().getTime();
    String timestampStr = DateUtils.getRelativeTimeSpanString(
        tweet.createdDatetime.getTime(), now, MIN_RESOLUTION, TIMESTAMP_FLAGS).toString();
    viewHolder.timestampTextView.setText(timestampStr);

    viewHolder.authorTextView.setText(tweet.user.displayName);
    viewHolder.usernameTextView.setText("@" + tweet.user.username);
    viewHolder.bodyTextView.setText(tweet.body);

    return convertView;
  }

  private Drawable getDrawable(int id) {
    return getContext().getResources().getDrawable(id);
  }
}
