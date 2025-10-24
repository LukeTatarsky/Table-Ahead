package com.example.tableahead.search;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tableahead.R;

/**
 * Fragment that shows loading circle, used while loading data
 * from database in progress in CategoryScreen
 * @author Ethan Rody
 */
public class ProgressBarFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress_bar, container, false);
    }
}