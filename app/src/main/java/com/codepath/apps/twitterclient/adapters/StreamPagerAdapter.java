package com.codepath.apps.twitterclient.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.twitterclient.fragments.StreamFragment;

public class StreamPagerAdapter extends FragmentPagerAdapter {
  public StreamPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  private final Tab[] TABS = {
    new Tab("Home", new StreamFragment())
  };

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

    public Tab(String name, Fragment fragment) {
      this.name = name;
      this.fragment = fragment;
    }
  }
}
