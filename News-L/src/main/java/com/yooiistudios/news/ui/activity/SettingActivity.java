package com.yooiistudios.news.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.model.language.Language;
import com.yooiistudios.news.model.language.LanguageType;
import com.yooiistudios.news.util.RecommendUtils;
import com.yooiistudios.news.util.ReviewUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Wooseong Kim on in News-Android-L from Yooii Studios Co., LTD. on 2014. 9. 9.
 *
 * SettingActivity
 *  설정화면 임시 구현 액티비티. 나중에 UI 작업이 필요
 */
public class SettingActivity extends Activity {
    private static final String LINK_APP_PREFIX = "fb://profile/";
    private static final String FB_YOOII_ID = "652380814790935";

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
    public static class PlaceholderFragment extends Fragment implements AdapterView.OnItemClickListener {

        @InjectView(R.id.setting_list_view) ListView mListView;
        @InjectView(R.id.setting_adView) AdView mAdView;
        private View mFooterView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
            if (rootView != null) {
                ButterKnife.inject(this, rootView);
                // setAdapter 전에 호출 필요
                mFooterView = LayoutInflater.from(getActivity().getApplicationContext())
                        .inflate(R.layout.list_footer_view, container, false);
                mListView.addFooterView(mFooterView);
                initListView();
                initAdView();
            }
            return rootView;
        }

        private void initAdView() {
            List<String> ownedSkus = IabProducts.loadOwnedIabProducts(getActivity().getApplicationContext());
            // NO_ADS 만 체크해도 풀버전까지 체크됨
            if (ownedSkus.contains(IabProducts.SKU_NO_ADS)) {
                mAdView.setVisibility(View.GONE);
                if (mFooterView != null) {
                    mListView.removeFooterView(mFooterView);
                }
            } else {
                mAdView.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder()
                        .build();
                mAdView.loadAd(adRequest);
            }
        }

        private void initListView() {
            ArrayList<String> mSettingList = new ArrayList<String>();
            mSettingList.add(getString(R.string.setting_language));
            mSettingList.add(getString(R.string.setting_share_this_app));
            mSettingList.add(getString(R.string.setting_rate_this_app));
            mSettingList.add(getString(R.string.setting_tutorial));
            mSettingList.add(getString(R.string.setting_credit));
            mSettingList.add(getString(R.string.setting_like_facebook));
            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, mSettingList);

            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            switch (i) {
                case 0:
                    showLanguageDialog();
                    break;
                case 1:
                    RecommendUtils.showRecommendDialog(getActivity());
                    break;
                case 2:
                    ReviewUtils.showReviewActivity(getActivity());
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
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
    }
}
