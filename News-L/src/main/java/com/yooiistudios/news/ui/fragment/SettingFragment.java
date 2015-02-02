package com.yooiistudios.news.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.Settings;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;
import com.yooiistudios.news.ui.adapter.PanelMatrixSelectAdapter;
import com.yooiistudios.news.ui.adapter.SettingAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_KEY;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_SHARED_PREFERENCES;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PanelMatrixType;


/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * SettingFragment
 *  세팅 화면의 세팅 탭에 쓰일 프레그먼트
 */
public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener {
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

    public static final String KEEP_SCREEN_ON_SHARED_PREFERENCES = "KEEP_SCREEN_ON_SHARED_PREFERENCES";
    public static final String KEEP_SCREEN_ON_KEY = "KEEP_SCREEN_ON_KEY";

    private static final String SI_PANEL_MATRIX_KEY = "SI_PANEL_MATRIX_KEY";

    @InjectView(R.id.setting_list_view) ListView mListView;
    @InjectView(R.id.setting_adView) AdView mAdView;
    private SettingAdapter mSettingAdapter;

    private int mPreviousPanelMatrixKey = -1;

    public interface OnSettingChangedListener {
        public void onPanelMatrixSelect(boolean changed);
    }

    public SettingFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mPreviousPanelMatrixKey = PanelMatrixType.getCurrentPanelMatrixIndex(getActivity().getApplicationContext());
        } else {
            mPreviousPanelMatrixKey = savedInstanceState.getInt(SI_PANEL_MATRIX_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SI_PANEL_MATRIX_KEY, mPreviousPanelMatrixKey);

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
        mSettingAdapter = new SettingAdapter(getActivity().getApplicationContext());
        mListView.setAdapter(mSettingAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        SettingItem item = SettingItem.values()[position];
        SharedPreferences preferences;
        switch (item) {
            case LANGUAGE:
                showLanguageDialog();
                break;

//            case NEWS_FEED_AUTO_SCROLL_START_OFFSET:
//                showNewsFeedAutoScrollDialog();
//                break;

            case KEEP_SCREEN_ON:
                preferences = getActivity().getSharedPreferences(
                        KEEP_SCREEN_ON_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                boolean isChecked = preferences.getBoolean(KEEP_SCREEN_ON_KEY, false);
                preferences.edit().putBoolean(KEEP_SCREEN_ON_KEY, !isChecked).apply();

                Switch keepScreenSwitch = (Switch) view.findViewById(R.id.setting_item_switch);
                keepScreenSwitch.setChecked(!isChecked);
                break;

            case MAIN_AUTO_REFRESH_INTERVAL:
                break;

            case MAIN_PANEL_MATRIX:
                PanelMatrixType currentPanelMatrix =
                        PanelMatrixType.getCurrentPanelMatrix(getActivity().getApplicationContext());

//                final NumberPicker numberPicker = new NumberPicker(getActivity());
//                String[] panelMatrixArr = MAIN_PANEL_MATRIX.getDisplayNameStringArr();
//                numberPicker.setDisplayedValues(panelMatrixArr);
//                numberPicker.setMinValue(0);
//                numberPicker.setMaxValue(panelMatrixArr.length - 1);
//                numberPicker.setValue(MAIN_PANEL_MATRIX.getIndexByUniqueKey(currentPanelMatrixKey));
//                numberPicker.setWrapSelectorWheel(false);
//                numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                ListView panelMatrixListView = new ListView(getActivity());
                panelMatrixListView.setDivider(new ColorDrawable(android.R.color.transparent));
                panelMatrixListView.setDividerHeight(0);
                panelMatrixListView.setAdapter(new PanelMatrixSelectAdapter(getActivity(), currentPanelMatrix));

                final AlertDialog panelMatrixSelectDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.setting_main_panel_matrix)
                        .setView(panelMatrixListView)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {}
                        })
                        .create();
                panelMatrixSelectDialog.show();

                panelMatrixListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        PanelMatrixType selectedPanelMatrix = PanelMatrixType.values()[position];
                        if (!selectedPanelMatrix.isUsable(getActivity())) {
                            return;
                        }

                        SharedPreferences preferences = getActivity().getSharedPreferences(
                                PANEL_MATRIX_SHARED_PREFERENCES,
                                Context.MODE_PRIVATE);
                        preferences.edit()
                                .putInt(PANEL_MATRIX_KEY, selectedPanelMatrix.uniqueKey)
                                .apply();

                        mSettingAdapter.notifyDataSetChanged();

                        if (getActivity() instanceof OnSettingChangedListener) {
                            ((OnSettingChangedListener)getActivity()).onPanelMatrixSelect(
                                    selectedPanelMatrix.uniqueKey != mPreviousPanelMatrixKey
                            );
                        }

                        panelMatrixSelectDialog.dismiss();
                    }
                });
                break;

            case TUTORIAL:
                break;
        }
    }

    private void showLanguageDialog() {
        // 뉴스피드들의 타이틀을 CharSequence 로 변경
        ArrayList<String> languageList = new ArrayList<>();
        for (int i = 0; i < LanguageType.values().length; i++) {
            languageList.add(getString(LanguageType.valueOf(i).getLocalNotationStringId()));
        }

        String[] languages = languageList.toArray(new String[languageList.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                // archive selection
                Language.setLanguageType(LanguageType.valueOf(i), getActivity());

//                    getActivity().finish();
                mSettingAdapter.notifyDataSetChanged();
            }
        }).setTitle(R.string.setting_language).create();
        alertDialog.show();
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