package com.example.e_float;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RelayFragment extends Fragment {
    private String pageTitle;
    private int pageNum;

    public RelayFragment() {
        //Required empty public constructor
    }

    public static RelayFragment newInstance(int pageNum, String pageTitle) {
        RelayFragment relayFragment = new RelayFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNumber", pageNum);
        bundle.putString("pageTitle", pageTitle);
        relayFragment.setArguments(bundle);
        return relayFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.pageNum = getArguments().getInt("pageNumber", 0);
        this.pageTitle = getArguments().getString("pageTitle");
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relay, container, false);

        return view;
    }
}