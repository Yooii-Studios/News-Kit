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
import com.yooiistudios.news.util.RecommendUtils;
import com.yooiistudios.news.util.ReviewUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_KEY;
import static com.yooiistudios.news.ui.widget.MainBottomContainerLayout.PANEL_MATRIX_SHARED_PREFERENCES;


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

    private static final String SI_PANEL_MATRIX_KEY = "SI_PANEL_MATRIX_KEY";

    @InjectView(R.id.setting_list_view) ListView mListView;
    @InjectView(R.id.setting_adView) AdView mAdView;
    private SettingAdapter mSettingAdapter;

    private int mPreviousPanelMatrixKey = -1;

    public interface OnSettingChangedListener {
        public void onPanelMatrixChanged(boolean changed);
    }

    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(
                    PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            mPreviousPanelMatrixKey = preferences.getInt(PANEL_MATRIX_KEY,
                    PANEL_MATRIX.getDefault().uniqueKey);
        } else {
            mPreviousPanelMatrixKey = savedInstanceState.getInt(SI_PANEL_MATRIX_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SI_PANEL_MATRIX_KEY, mPreviousPanelMatrixKey);
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
                        PANEL_MATRIX_SHARED_PREFERENCES, Context.MODE_PRIVATE);
                int currentPanelMatrixKey = preferences.getInt(PANEL_MATRIX_KEY,
                        PANEL_MATRIX.getDefault().uniqueKey);

                final NumberPicker numberPicker = new NumberPicker(getActivity());
                String[] panelMatrixArr = PANEL_MATRIX.getDisplayNameStringArr();
                numberPicker.setDisplayedValues(panelMatrixArr);
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(panelMatrixArr.length - 1);
                numberPicker.setValue(PANEL_MATRIX.getIndexByUniqueKey(currentPanelMatrixKey));
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.setting_panel_count)
                        .setView(numberPicker)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int idx = numberPicker.getValue();
                                PANEL_MATRIX selectedPanelMatrix = PANEL_MATRIX.values()[idx];

                                SharedPreferences preferences = context.getSharedPreferences(
                                        PANEL_MATRIX_SHARED_PREFERENCES,
                                        Context.MODE_PRIVATE);
                                preferences.edit()
                                        .putInt(PANEL_MATRIX_KEY, selectedPanelMatrix.uniqueKey)
                                        .apply();

                                mSettingAdapter.notifyDataSetChanged();

                                if (getActivity() instanceof OnSettingChangedListener) {
                                    ((OnSettingChangedListener)getActivity()).onPanelMatrixChanged(
                                            selectedPanelMatrix.uniqueKey != mPreviousPanelMatrixKey
                                    );
                                }
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