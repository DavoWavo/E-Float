package com.example.e_float;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 3;
    private final Context mContext;

    public CustomPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        //new code
        switch (position) {
            case 0:
                return DeviceScanningFragment.newInstance(0, "Scan Device");
            case 1:
                return BeaconFragment.newInstance(1, "Beacon");
            case 2:
                return RelayFragment.newInstance(2, "Relay");
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Near";
            case 1:
                return "Beacon";
            case 2:
                return "Relay";
            default:
                return ""; //better than returning null... avoid potential crashing
        }
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return NUM_ITEMS;
    }
}