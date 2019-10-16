package com.example.e_float;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
* <h1>Custom Pager Adapter</h1>
* This class handles the different fragments for displaying.
*	
* @author David Fitzsimmons
* @version 1.0
* @since 2019-10-5
*/

public class CustomPagerAdapter extends FragmentPagerAdapter {
	//number of fragments present to display
    private static int NUM_ITEMS = 2;

	/**
	* Default constructor
	* 
	* @param fragementManager is a instance of the fragment manager to override methods from
	*/
    public CustomPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

	/**
	* This methods returns a copy of the fragment with its pageNum and pageTitle
	*
	* @param position, the position of the fragment that is to be returned
	* @return Fragment, a copy of the fragment containing the pageNum and pageTitle
	*/
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

	/**
	* This method sets the title of the pages and their numbers on the adapter slide
	*
	* @param position, the position of the title within the adapter slide
	* @return CharSequence, the title of the fragment in the relative position
	*/
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

	/**
	* This methods returns the number of fragments that are present 
	*
	* @return, this is the number of fragments present
	*/
    @Override
    public int getCount() {
        // Show 2 total pages.
        return NUM_ITEMS;
    }
}