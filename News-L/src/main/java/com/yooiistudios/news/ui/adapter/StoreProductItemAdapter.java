package com.yooiistudios.news.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yooiistudios.news.R;
import com.yooiistudios.news.iab.IabProducts;
import com.yooiistudios.news.iab.util.Inventory;
import com.yooiistudios.news.util.StoreDebugCheckUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 12.
 *
 * MNStoreGridViewAdapter
 */
public class StoreProductItemAdapter extends BaseAdapter {
    public static final int NUM_OF_PRODUCT = 4;
    private Context mContext;
    private List<String> mOwnedSkus;
    private Inventory mInventory;

    private StoreItemOnClickListener mListener;

    public interface StoreItemOnClickListener {
        public void onItemPriceButtonClicked(String sku);
    }

    @SuppressWarnings("UnusedDeclaration")
    private StoreProductItemAdapter(){}
    public StoreProductItemAdapter(Context context, Inventory inventory,
                                   StoreItemOnClickListener storeGridViewOnClickListener) {
        mContext = context;
        mOwnedSkus = IabProducts.loadOwnedIabProducts(context);
        mInventory = inventory;

        // debug
        mListener = storeGridViewOnClickListener;
    }

    public void updateOnPurchase() {
        mOwnedSkus = IabProducts.loadOwnedIabProducts(mContext);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return NUM_OF_PRODUCT;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.store_item, null, true);
        if (convertView != null) {
            StoreItemViewHolder viewHolder = new StoreItemViewHolder(convertView);
            initLayout(viewHolder);
            switch (position) {
                case 0:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_list_noads);
                    viewHolder.getTitleTextView().setText(R.string.store_no_ads_title);
                    viewHolder.getDescriptionTextView().setText(R.string.store_no_ads_description);
                    viewHolder.getPriceButton().setTag(IabProducts.SKU_NO_ADS);
                    break;
                case 1:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_list_panels);
                    viewHolder.getTitleTextView().setText(R.string.store_more_panel_title);
                    viewHolder.getDescriptionTextView().setText(R.string.store_more_panel_description);
                    viewHolder.getPriceButton().setTag(IabProducts.SKU_MORE_PANELS);
                    break;
                case 2:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_list_topic);
                    viewHolder.getTitleTextView().setText(R.string.store_topic_select_title);
                    viewHolder.getDescriptionTextView().setText(R.string.store_topic_select_description);
                    viewHolder.getPriceButton().setTag(IabProducts.SKU_TOPIC_SELECT);
                    break;
                case 3:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_list_rss);
                    viewHolder.getTitleTextView().setText(R.string.store_custom_rss_feed_title);
                    viewHolder.getDescriptionTextView().setText(R.string.store_custom_rss_feed_description);
                    viewHolder.getPriceButton().setTag(IabProducts.SKU_CUSTOM_RSS_URL);
                    break;
                // test
                default:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_list_rss);
                    viewHolder.getTitleTextView().setText(R.string.store_custom_rss_feed_title);
                    viewHolder.getDescriptionTextView().setText(R.string.store_custom_rss_feed_description);
                    viewHolder.getPriceButton().setTag(IabProducts.SKU_CUSTOM_RSS_URL);
                    break;
            }
            initPriceViews(viewHolder);

            // debug
//            viewHolder.getLayout().setBackgroundColor(getToggledBackgroundColor(position));
            // release
            viewHolder.getTitleTextView().setBackgroundColor(Color.TRANSPARENT);
            viewHolder.getDescriptionTextView().setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    /*
    // debug용으로 컬러를 얻어내기
    private int getToggledBackgroundColor(int index) {
        if (index % 2 == 0) {
            return Color.parseColor("#cc00cc");
        } else {
            return Color.parseColor("#ff66ff");
        }
    }
    */

    private void initLayout(final StoreItemViewHolder viewHolder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.getLayout().setBackgroundResource(R.drawable.store_item_layout_background);
        }
        viewHolder.getLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemPriceButtonClicked((String) viewHolder.getPriceButton().getTag());
            }
        });
    }

    private void initPriceViews(final StoreItemViewHolder viewHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float elevation =
                    mContext.getResources().getDimension(R.dimen.store_item_price_button_elevation);
            viewHolder.getPriceButton().setElevation(elevation);
        }
        viewHolder.getPriceButton().setSelected(true);

        // price - check from inventory
        String sku = (String) viewHolder.getPriceButton().getTag();

        // Google
        if (mInventory != null) {
            if (mInventory.hasDetails(sku)) {
                if (mInventory.hasPurchase(sku)) {
                    viewHolder.getPriceButton().setText(R.string.store_purchased);
                    viewHolder.getPriceButton().setBackgroundResource(R.drawable.store_btn_raised_disable_drawable);
                } else {
                    viewHolder.getPriceButton().setText(mInventory.getSkuDetails(sku).getPrice());
                }
            }
        }

        // price - purchase check from ownedSkus - 풀버전, 언락은 ownedSkus 에서 체크
        if (mOwnedSkus != null && mOwnedSkus.contains(sku)) {
            viewHolder.getPriceButton().setText(R.string.store_purchased);
            viewHolder.getPriceButton().setBackgroundResource(R.drawable.store_btn_raised_disable_drawable);
        } else {
            if (!StoreDebugCheckUtils.isUsingStore(mContext)) {
                if (sku.equals(IabProducts.SKU_NO_ADS)) {
                    viewHolder.getPriceButton().setText("$1.99");
                } else {
                    viewHolder.getPriceButton().setText("$0.99");
                }
            }
        }

        // onClick
        viewHolder.getPriceButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemPriceButtonClicked((String) v.getTag());
            }
        });

        // set clickable
        if (viewHolder.getPriceButton().getText().toString().equals(
                mContext.getResources().getText(R.string.store_purchased))) {
            viewHolder.getPriceButton().setClickable(false);
            viewHolder.getPriceButton().setBackgroundResource(R.drawable.store_btn_raised_disable_drawable);
            viewHolder.getLayout().setClickable(false);
            viewHolder.getLayout().setBackgroundColor(Color.WHITE);
        } else {
            viewHolder.getPriceButton().setClickable(true);
            viewHolder.getLayout().setFocusable(true);
        }
    }

    /**
     * ViewHolder
     */
    static class StoreItemViewHolder {
        @Getter @InjectView(R.id.store_item_layout) RelativeLayout layout;
        @Getter @InjectView(R.id.store_item_image_view) ImageView imageView;
        @Getter @InjectView(R.id.store_item_title_text_view) TextView titleTextView;
        @Getter @InjectView(R.id.store_item_description_text_view) TextView descriptionTextView;
        @Getter @InjectView(R.id.store_item_price_button) Button priceButton;

        public StoreItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
