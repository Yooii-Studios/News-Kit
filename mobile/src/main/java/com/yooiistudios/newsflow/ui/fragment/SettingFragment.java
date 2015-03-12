package com.yooiistudios.newsflow.ui.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
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
import com.yooiistudios.newsflow.NewsApplication;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.core.connector.Connector;
import com.yooiistudios.newsflow.core.connector.ConnectorResult;
import com.yooiistudios.newsflow.core.connector.UploadRequest;
import com.yooiistudios.newsflow.core.connector.UploadResult;
import com.yooiistudios.newsflow.core.language.Language;
import com.yooiistudios.newsflow.core.language.LanguageUtils;
import com.yooiistudios.newsflow.core.news.NewsFeedDefaultUrlProvider;
import com.yooiistudios.newsflow.core.news.NewsTopic;
import com.yooiistudios.newsflow.core.news.util.RssFetchableConverter;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrix;
import com.yooiistudios.newsflow.core.panelmatrix.PanelMatrixUtils;
import com.yooiistudios.newsflow.core.util.NLLog;
import com.yooiistudios.newsflow.iab.IabProducts;
import com.yooiistudios.newsflow.model.Settings;
import com.yooiistudios.newsflow.ui.activity.StoreActivity;
import com.yooiistudios.newsflow.ui.adapter.SettingAdapter;
import com.yooiistudios.newsflow.util.AnalyticsUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 3.
 *
 * SettingFragment
 *  세팅 화면의 세팅 탭에 쓰일 프레그먼트
 */
public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener,
        LanguageSelectDialog.OnActionListener, PanelMatrixSelectDialog.OnActionListener,
        AutoRefreshIntervalDialogFragment.OnActionListener, PairTvDialog.OnActionListener {
    public enum SettingItem {
        LANGUAGE(R.string.setting_language),
        KEEP_SCREEN_ON(R.string.setting_keep_screen_on),
        TUTORIAL(R.string.setting_tutorial),

        MAIN_SUB_HEADER(R.string.setting_main_sub_header),
        MAIN_AUTO_REFRESH_INTERVAL(R.string.setting_main_auto_refresh_interval),
        MAIN_AUTO_REFRESH_SPEED(R.string.setting_main_auto_refresh_speed),
        MAIN_PANEL_MATRIX(R.string.setting_main_panel_matrix),

        PAIR_TV(R.string.setting_pair_tv);

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

            case KEEP_SCREEN_ON:
                toggleKeepScreenOption(view);
                break;

            case MAIN_AUTO_REFRESH_INTERVAL:
                showAutoRefreshInterval();
                break;

            case MAIN_PANEL_MATRIX:
                showPanelMatrixSelectDialog();
                break;

            case PAIR_TV:
                showPairTVDialogFragment();
                break;

            case TUTORIAL:
                showTutorial();
                break;
        }
    }

    @Override
    public void onSelectLanguage(int index) {
        LanguageUtils.setLanguageType(Language.valueOf(index), getActivity());
        // TODO: 언어의 선택 통계 Google Analytics 나중에 구현할 것
        mSettingAdapter.notifyDataSetChanged();
    }

    private void showLanguageSelectDialog() {
        showDialogFragment("language_dialog", LanguageSelectDialog.newInstance(this));
    }

    private void toggleKeepScreenOption(View view) {
        SharedPreferences preferences = getActivity().getSharedPreferences(
                KEEP_SCREEN_ON_PREFS, Context.MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean(KEEP_SCREEN_ON_KEY, false);
        preferences.edit().putBoolean(KEEP_SCREEN_ON_KEY, !isChecked).apply();

        SwitchCompat keepScreenSwitch = (SwitchCompat) view.findViewById(R.id.setting_item_switch);
        keepScreenSwitch.setChecked(!isChecked);
    }

    private void showTutorial() {
        // TODO 나중에 튜토리얼이 개발된 뒤 boolean 값으로 메인에서 볼 수 있게 해 주자
        Toast.makeText(getActivity(), "In developing...", Toast.LENGTH_SHORT).show();
    }

    private void showAutoRefreshInterval() {
        showDialogFragment("auto_refresh_interval", AutoRefreshIntervalDialogFragment.newInstance(this));
    }

    @Override
    public void onTypeAutoRefreshInterval(int interval) {
        Settings.setAutoRefreshIntervalProgress(getActivity(), interval);
        mSettingAdapter.notifyDataSetChanged();
    }

    private void showPanelMatrixSelectDialog() {
        showDialogFragment("panel_matrix_dialog", PanelMatrixSelectDialog.newInstance(this));
    }

    private void showPairTVDialogFragment() {
        showDialogFragment("pair_tv", PairTvDialog.newInstance(this));
    }

    @Override
    public void onSelectMatrix(int position) {
        PanelMatrix selectedPanelMatrix = PanelMatrix.getByUniqueKey(position);
        if (IabProducts.isMatrixAvailable(getActivity(), selectedPanelMatrix)) {
            PanelMatrixUtils.setCurrentPanelMatrix(selectedPanelMatrix, getActivity());
            mSettingAdapter.notifyDataSetChanged();

            // 패널이 변경되었으면 메인에서 구조 변경을 할 수 있게 콜백으로 알림
            if (getActivity() instanceof OnSettingChangedListener) {
                ((OnSettingChangedListener)getActivity()).onPanelMatrixSelect(
                        selectedPanelMatrix.getUniqueId() != mPreviousPanelMatrixUniqueId);
            }

            // Google Analytics
            AnalyticsUtils.trackNewsPanelMatrixSelection(
                    (NewsApplication) getActivity().getApplication(), "Settings",
                    selectedPanelMatrix.toString());
        } else {
            startActivity(new Intent(getActivity(), StoreActivity.class));
            Toast.makeText(getActivity(), R.string.store_buy_pro_version, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfirmPairing(String token) {
        NLLog.now("onConfirmPairing");
        Toast.makeText(getActivity(), "Token: " + token, Toast.LENGTH_SHORT).show();

        uploadData(token);
    }

    private void uploadData(String token) {
        try {
            NewsTopic topic = NewsFeedDefaultUrlProvider.getInstance(getActivity()).getTopNewsTopic();
            String data = RssFetchableConverter.toBase64String(topic);
//            NewsTopic decodedTopic = (NewsTopic)RssFetchableConverter.toRssFetchable(data);

            UploadRequest uploadRequest = new UploadRequest();
            uploadRequest.context = getActivity().getApplicationContext();
            uploadRequest.token = token;
            uploadRequest.data = data;
            uploadRequest.listener = new UploadRequest.ResultListener<UploadResult>() {

                @Override
                public void onGetResult(UploadResult result) {
                    NLLog.now("Upload succeed.");
                }

                @Override
                public void onFail(ConnectorResult result) {
                    NLLog.now("Upload failed(onFail).");
                }
            };
            Connector.upload(uploadRequest);
        } catch(RssFetchableConverter.RssFetchableConvertException e) {
            NLLog.now("Error occurred while converting data to bytes.");
        }
    }

    private void showDialogFragment(String tag, DialogFragment dialogFragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog
        dialogFragment.show(ft, tag);
    }
}