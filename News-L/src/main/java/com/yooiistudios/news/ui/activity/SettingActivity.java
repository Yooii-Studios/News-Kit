package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yooiistudios.news.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        @InjectView(R.id.setting_list_view) ListView mListView;
        ArrayList<String> mSettingList;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
            ButterKnife.inject(this, rootView);

            // List View
            mSettingList = new ArrayList<String>();
            mSettingList.add("Language");
            mSettingList.add("Share this app");
            mSettingList.add("Rate this app");
            mSettingList.add("Tutorial");
            mSettingList.add("Credit");
            mSettingList.add("Like us on facebook");
            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, mSettingList);

            mListView.setAdapter(adapter);

            return rootView;
        }
    }
}
