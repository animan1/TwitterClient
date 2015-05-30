package com.codepath.apps.twitterclient.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.twitterclient.fragments.StreamFragment;
import com.codepath.apps.twitterclient.network.TwitterClient;

public class StreamPagerAdapter extends FragmentPagerAdapter {
  private final Tab[] TABS = {
      new Tab("Home", new StreamFragment(), TwitterClient.TIMELINE.HOME),
      new Tab("Mentions", new StreamFragment(), TwitterClient.TIMELINE.MENTIONS)
  };

  public StreamPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {
    return TABS[position].fragment;
  }

  @Override
  public int getCount() {
    return TABS.length;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return TABS[position].name;
  }

  class Tab {
    String name;
    Fragment fragment;

    public Tab(String name, StreamFragment fragment, TwitterClient.TIMELINE timeline) {
      this.name = name;
      this.fragment = fragment;
      Bundle args = new Bundle();
      args.putInt(StreamFragment.TIMELINE_INDEX, timeline.ordinal());
      fragment.setArguments(args);
    }
  }
}
