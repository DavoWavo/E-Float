package com.example.e_float;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class CustomPagerAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 2;

    public CustomPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return DeviceScanningFragment.newInstance(0, "Find my Float");
            case 1:
                return BeaconFragment.newInstance(1, "Paired Float");
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                Log.d("debugAdapter", "page 1");
                return "Find my Float";
            case 1:
                Log.d("debugAdapter", "page 2");
                return "Paired Float";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return NUM_ITEMS;
    }


}