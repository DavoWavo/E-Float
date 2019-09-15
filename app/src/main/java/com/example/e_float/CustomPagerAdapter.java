package com.example.e_float;

import android.content.Context;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class CustomPagerAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 1;

    public CustomPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        //new code
        switch (position) {
            case 0:
                return DeviceScanningFragment.newInstance(0, "Scan Device");
            //case 1:

            //case 2:

            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return ""; //near
            //case 1:
            //    return ""; //beacn
            //case 2:
            //    return ""; //rlay
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