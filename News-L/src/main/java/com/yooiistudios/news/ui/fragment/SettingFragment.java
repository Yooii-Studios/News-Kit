package com.yooiistudios.news.ui.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.Settings;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;
import com.yooiistudios.news.ui.adapter.SettingAdapter;
import com.yooiistudios.news.ui.widget.MainBottomContainerLayout;
import com.yooiistudios.news.util.RecommendUtils;
import com.yooiistudios.news.util.ReviewUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * SettingFragment
 *  세팅 화면의 세팅 탭에 쓰일 프레그먼트
 */
public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener {

    public enum SettingItem {
        LANGUAGE(R.string.setting_language),
        NEWSFEED_AUTO_SCROLL(R.string.setting_newsfeed_auto_scroll),
        KEEP_SCREEN_ON(R.string.setting_keep_screen_on),
        PANEL_COUNT(R.string.setting_panel_count),
        SHARE_APP(R.string.setting_share_this_app),
        RATE(R.string.setting_rate_this_app),
        TUTORIAL(R.string.setting_tutorial),
        CREDIT(R.string.setting_credit),
        LIKE_ON_FACEBOOK(R.string.setting_like_facebook);

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

    private static final String LINK_APP_PREFIX = "fb://profile/";
    private static final String FB_YOOII_ID = "652380814790935";

    @InjectView(R.id.setting_list_view) ListView mListView;
    @InjectView(R.id.setting_adView) AdView mAdView;
    private SettingAdapter mSettingAdapter;

    public SettingFragment() {
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        SettingItem item = SettingItem.values()[position];
        final Context context = getActivity().getApplicationContext();
        SharedPreferences preferences;
        switch (item) {
            case LANGUAGE:
                showLanguageDialog();
                break;

            case NEWSFEED_AUTO_SCROLL:
                showNewsFeedAutoScrollDialog();
                break;

            case KEEP_SCREEN_ON:
                preferences = context.getSharedPreferences(
                        KEEP_SCREEN_ON_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                boolean isChecked = preferences.getBoolean(KEEP_SCREEN_ON_KEY, false);
                preferences.edit().putBoolean(KEEP_SCREEN_ON_KEY, !isChecked).apply();

                mSettingAdapter.notifyDataSetChanged();
                break;

            case PANEL_COUNT:
                preferences = context.getSharedPreferences(
                        MainBottomContainerLayout.PANEL_COUNT_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                int currentPanelCount = preferences.getInt(MainBottomContainerLayout.PANEL_COUNT_KEY,
                                MainBottomContainerLayout.PANEL_COUNT_VALUE);

                final NumberPicker numberPicker = new NumberPicker(getActivity());
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(6);
                numberPicker.setValue(currentPanelCount);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.setting_panel_count)
                        .setView(numberPicker)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences preferences = context.getSharedPreferences(
                                        MainBottomContainerLayout.PANEL_COUNT_SHARED_PREFERENCES,
                                        Context.MODE_PRIVATE);
                                preferences.edit()
                                        .putInt(MainBottomContainerLayout.PANEL_COUNT_KEY,
                                                numberPicker.getValue())
                                        .apply();

                                mSettingAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {}
                        })
                        .show();
                break;

            case SHARE_APP:
                RecommendUtils.showRecommendDialog(getActivity());
                break;

            case RATE:
                ReviewUtils.showReviewActivity(getActivity());
                break;

            case TUTORIAL:
                break;

            case CREDIT:
                break;

            case LIKE_ON_FACEBOOK:
                try {
                    PackageManager packageManager = getActivity().getPackageManager();
                    if (packageManager != null) {
                        packageManager.getPackageInfo("com.facebook.katana", 0);
                    }
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(LINK_APP_PREFIX + FB_YOOII_ID)));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/YooiiMooii")));
                }
                break;
            default:
        }
    }

    private void showLanguageDialog() {
        // 뉴스피드들의 타이틀을 CharSequence 로 변경
        ArrayList<String> languageList = new ArrayList<String>();
        for (int i = 0; i < LanguageType.values().length; i++) {
            languageList.add(LanguageType.toTranselatedString(i, getActivity()));
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
                initListView();
            }
        }).setTitle(R.string.setting_language).create();
        alertDialog.show();
    }

    private void showNewsFeedAutoScrollDialog() {
        // 뉴스피드들의 타이틀을 CharSequence 로 변경
        ArrayList<String> list = new ArrayList<String>();
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
        }).setTitle(R.string.setting_newsfeed_auto_scroll).create();
        alertDialog.show();
    }
}