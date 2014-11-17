package com.yooiistudios.news.ui.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.adapter.StoreProductItemAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class StoreActivity extends ActionBarActivity implements StoreProductItemAdapter.StoreItemOnClickListener {
    private static final String TAG = "StoreActivity";

    @InjectView(R.id.store_toolbar) Toolbar mToolbar;

    @InjectView(R.id.store_title_text_view_1) TextView mTitleTextView1;
    @InjectView(R.id.store_title_text_view_2) TextView mTitleTextView2;
    @InjectView(R.id.store_description_text_view_1) TextView mDescriptionTextView1;
    @InjectView(R.id.store_description_text_view_2) TextView mDescriptionTextView2;

    @InjectView(R.id.store_icon_banner_rss_image_view) ImageView mRssImageView;
    @InjectView(R.id.store_icon_banner_more_panels_image_view) ImageView mMorePanelsImageView;
    @InjectView(R.id.store_icon_banner_topic_image_view) ImageView mTopicImageView;
    @InjectView(R.id.store_icon_banner_no_ads_image_view) ImageView mNoAdsImageView;
    @InjectView(R.id.store_icon_banner_discount_image_view) ImageView mDiscountImageView;


    @InjectView(R.id.store_discounted_price_text_view) TextView mDiscountedPriceTextView;
    @InjectView(R.id.store_original_price_text_view) TextView mOriginalPriceTextView;
    @InjectView(R.id.store_product_list_view) ListView mProductListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        ButterKnife.inject(this);
        initToolbar();
        initBannerLayout();
        initItemListView();
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mToolbar.setElevation(getResources().getDimension(R.dimen.store_toolbar_elevation));
        }
    }

    private void initBannerLayout() {
        setPointColoredTextView(mDescriptionTextView2, getString(R.string.store_description_text_2),
                getString(R.string.store_description_text_2_highlight));
        mOriginalPriceTextView.setPaintFlags(mOriginalPriceTextView.getPaintFlags() |
                Paint.STRIKE_THRU_TEXT_FLAG);

        // release
        /*
        int[] attrs = new int[] { android.R.attr.selectableItemBackground };

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = obtainStyledAttributes(attrs);

        // Now get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
        Drawable drawableFromTheme = ta.getDrawable(0);

        // Finally free resources used by TypedArray
        ta.recycle();

        // setBackground(Drawable) requires API LEVEL 16,
        // otherwise you have to use deprecated setBackgroundDrawable(Drawable) method.
        mTitleTextView1.setBackground(drawableFromTheme);
        */

//        mTitleTextView1.setBackgroundColor(Color.TRANSPARENT);
        mTitleTextView2.setBackgroundColor(Color.TRANSPARENT);
        mDescriptionTextView1.setBackgroundColor(Color.TRANSPARENT);
        mDescriptionTextView2.setBackgroundColor(Color.TRANSPARENT);
        mDiscountedPriceTextView.setBackgroundColor(Color.TRANSPARENT);
        mOriginalPriceTextView.setBackgroundColor(Color.TRANSPARENT);

        mRssImageView.setBackgroundColor(Color.TRANSPARENT);
        mMorePanelsImageView.setBackgroundColor(Color.TRANSPARENT);
        mTopicImageView.setBackgroundColor(Color.TRANSPARENT);
        mNoAdsImageView.setBackgroundColor(Color.TRANSPARENT);
        mDiscountImageView.setBackgroundColor(Color.TRANSPARENT);

        /*
        TextView tv = mOriginalPriceTextView;
        String s = getString(R.string.store_full_version_original_price);
        StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();
        tv.setText(s, TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) tv.getText();
        spannable.setSpan(STRIKE_THROUGH_SPAN, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        */
    }

    private void initItemListView() {
        mProductListView.setAdapter(new StoreProductItemAdapter(this, this));
        mProductListView.setBackgroundColor(Color.WHITE);
    }

    private void setPointColoredTextView(TextView textView, String descriptionString, String pointedString) {
        if (textView != null) {
            SpannableString spannableString = new SpannableString(descriptionString);

            int pointedStringIndex = descriptionString.indexOf(pointedString);
            if (pointedStringIndex != -1) {
                spannableString.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.store_no_ads_color)),
                        pointedStringIndex, pointedStringIndex + pointedString.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(spannableString);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Log.i("Store", "id: " + item.getItemId());
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBannerClicked(View view) {
        Log.i(TAG, "onBannerClicked");
    }

    public void onBuyFullVersionButtonClicked(View view) {
        Log.i(TAG, "onBuyFullVersionButtonClicked");
    }

    @Override
    public void onItemPriceButtonClicked(String sku) {
        Log.i(TAG, "onItemPriceButtonClicked: " + sku);
    }
}
