package com.vitalsync.vitalsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class StatsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // ربط كلاس الجافا بملف التصميم fragment_stats.xml
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }
}
