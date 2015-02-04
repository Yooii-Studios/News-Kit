package com.yooiistudios.news.ui.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.Settings;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.panelmatrix.PanelMatrix;
import com.yooiistudios.news.model.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.news.ui.activity.StoreActivity;
import com.yooiistudios.news.model.language.LanguageUtils;
import com.yooiistudios.news.ui.adapter.SettingAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * SettingFragment
 *  세팅 화면의 세팅 탭에 쓰일 프레그먼트
 */
public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener,
        LanguageSelectDialog.OnActionListener, PanelMatrixSelectDialog.OnActionListener {

    public enum SettingItem {
        LANGUAGE(R.string.setting_language),
        KEEP_SCREEN_ON(R.string.setting_keep_screen_on),
        TUTORIAL(R.string.setting_tutorial),

        MAIN_SUB_HEADER(R.string.setting_main_sub_header),
        MAIN_AUTO_REFRESH_INTERVAL(R.string.setting_main_auto_refresh_offset),
        MAIN_AUTO_REFRESH_SPEED(R.string.setting_main_auto_refresh_speed),
        MAIN_PANEL_MATRIX(R.string.setting_main_panel_matrix);

        private int mTitleResId;
        private SettingItem(int titleResId) {
            mTitleResId = titleResId;
        }
        public int getTitleResId() {
            return mTitleResId;
        }
    }

    public static final String KEEP_SCREEN_ON_PREFS = "KEEP_SCREEN_ON_PREFS";
    public static final String KEEP_SCREEN_ON_KEY = "KEEP_SCREEN_ON_KEY";
    private static final String PANEL_MATRIX_KEY = "PANEL_MATRIX_KEY";

    @InjectView(R.id.setting_list_view) ListView mListView;
    @InjectView(R.id.setting_adView) AdView mAdView;

    private SettingAdapter mSettingAdapter;
    private int mPreviousPanelMatrixUniqueId = -1;

    public interface OnSettingChangedListener {
        public void onPanelMatrixSelect(boolean changed);
    }

    public SettingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mPreviousPanelMatrixUniqueId = PanelMatrixUtils.getCurrentPanelMatrix(getActivity()).getUniqueId();
        } else {
            mPreviousPanelMatrixUniqueId = savedInstanceState.getInt(PANEL_MATRIX_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PANEL_MATRIX_KEY, mPreviousPanelMatrixUniqueId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        if (rootView != null) {
            ButterKnife.inject(this, rootView);
            initListView();
            initAdView();
        }
        return rootView;
    }

    private void initAdView() {
        // NO_ADS 만 체크해도 풀버전까지 체크됨
        if (IabProducts.containsSku(getActivity().getApplicationContext(), IabProducts.SKU_NO_ADS)) {
            mAdView.setVisibility(View.GONE);
        } else {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void initListView() {
        mSettingAdapter = new SettingAdapter(getActivity());
        mListView.setAdapter(mSettingAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        SettingItem item = SettingItem.values()[position];
        switch (item) {
            case LANGUAGE:
                showLanguageSelectDialog();
                break;

//            case NEWS_FEED_AUTO_SCROLL_START_OFFSET:
//                showNewsFeedAutoScrollDialog();
//                break;

            case KEEP_SCREEN_ON:
                toggleKeepScreenOption(view);
                break;

            case MAIN_AUTO_REFRESH_INTERVAL:
                break;

            case MAIN_PANEL_MATRIX:
                showPanelMatrixSelectDialog();
                break;

            case TUTORIAL:
                break;
        }
    }

    @Override
    public void onSelectLanguage(int index) {
        LanguageUtils.setLanguageType(Language.valueOf(index), getActivity());
        mSettingAdapter.notifyDataSetChanged();
    }

    private void showLanguageSelectDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("language_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog
        DialogFragment newFragment = LanguageSelectDialog.newInstance(this);
        newFragment.show(ft, "language_dialog");
    }

    private void toggleKeepScreenOption(View view) {
        SharedPreferences preferences = getActivity().getSharedPreferences(
                KEEP_SCREEN_ON_PREFS, Context.MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean(KEEP_SCREEN_ON_KEY, false);
        preferences.edit().putBoolean(KEEP_SCREEN_ON_KEY, !isChecked).apply();

        SwitchCompat keepScreenSwitch = (SwitchCompat) view.findViewById(R.id.setting_item_switch);
        keepScreenSwitch.setChecked(!isChecked);
    }

    private void showPanelMatrixSelectDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("panel_matrix_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog
        DialogFragment newFragment = PanelMatrixSelectDialog.newInstance(this);
        newFragment.show(ft, "panel_matrix_dialog");

//                final NumberPicker numberPicker = new NumberPicker(getActivity());
//                String[] panelMatrixArr = MAIN_PANEL_MATRIX.getDisplayNameStringArr();
//                numberPicker.setDisplayedValues(panelMatrixArr);
//                numberPicker.setMinValue(0);
//                numberPicker.setMaxValue(panelMatrixArr.length - 1);
//                numberPicker.setValue(MAIN_PANEL_MATRIX.getIndexByUniqueKey(currentPanelMatrixKey));
//                numberPicker.setWrapSelectorWheel(false);
//                numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    @Override
    public void onSelectMatrix(int position) {
        PanelMatrix selectedPanelMatrix = PanelMatrix.getByUniqueKey(position);
        if (PanelMatrixUtils.isMatrixAvailable(getActivity(), selectedPanelMatrix)) {
            PanelMatrixUtils.setCurrentPanelMatrix(selectedPanelMatrix, getActivity());
            mSettingAdapter.notifyDataSetChanged();

            // 패널이 변경되었으면 메인에서 구조 변경을 할 수 있게 콜백으로 알림
            if (getActivity() instanceof OnSettingChangedListener) {
                ((OnSettingChangedListener)getActivity()).onPanelMatrixSelect(
                        selectedPanelMatrix.getUniqueId() != mPreviousPanelMatrixUniqueId);
            }
        } else {
            startActivity(new Intent(getActivity(), StoreActivity.class));
            Toast.makeText(getActivity(), R.string.store_buy_pro_version, Toast.LENGTH_SHORT).show();
        }
    }

    private void showNewsFeedAutoScrollDialog() {
        // 뉴스피드들의 타이틀을 CharSequence 로 변경
        ArrayList<String> list = new ArrayList<>();
        list.add(getString(R.string.on));
        list.add(getString(R.string.off));

        String[] booleanList = list.toArray(new String[list.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.setItems(booleanList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // archive selection
                if (i == 0) {
                    Settings.setNewsFeedAutoScroll(getActivity(), true);
                } else {
                    Settings.setNewsFeedAutoScroll(getActivity(), false);
                }
                initListView();
            }
        }).setTitle(R.string.setting_news_feed_auto_scroll_start_offset).create();
        alertDialog.show();
    }
}