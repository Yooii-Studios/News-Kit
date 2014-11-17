package com.yooiistudios.news.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yooiistudios.news.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;

/**
 * Created by StevenKim in Morning Kit from Yooii Studios Co., LTD. on 2014. 1. 12.
 *
 * MNStoreGridViewAdapter
 */
public class StoreProductItemAdapter extends BaseAdapter {
    private static final String TAG = "StoreItemAdapter";
    private Context mContext;

    private StoreItemOnClickListener mListener;

    public interface StoreItemOnClickListener {
        public void onItemPriceButtonClicked(String sku);
    }

    private StoreProductItemAdapter(){}
    public StoreProductItemAdapter(Context context, StoreItemOnClickListener storeGridViewOnClickListener) {
        mContext = context;

        // debug
        mListener = storeGridViewOnClickListener;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.store_item, parent, false);
        if (convertView != null) {
            StoreItemViewHolder viewHolder = new StoreItemViewHolder(convertView);
            initLayout(viewHolder);
            switch (position) {
                case 0:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_banner_noads);
//                    viewHolder.getTitleTextView().setText();
//                    viewHolder.getDescriptionTextView().setText();
//                    viewHolder.getPriceButton().setTag("SKU");
                    break;
                case 1:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_banner_panels);
//                    viewHolder.getTitleTextView().setText();
//                    viewHolder.getDescriptionTextView().setText();
//                    viewHolder.getPriceButton().setTag("SKU");
                    break;
                case 2:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_banner_topic);
//                    viewHolder.getTitleTextView().setText();
//                    viewHolder.getDescriptionTextView().setText();
//                    viewHolder.getPriceButton().setTag("SKU");
                    break;
                case 3:
                    viewHolder.getImageView().setImageResource(R.drawable.store_icon_banner_rss);
//                    viewHolder.getTitleTextView().setText();
//                    viewHolder.getDescriptionTextView().setText();
//                    viewHolder.getPriceButton().setTag("SKU");
                    break;
            }
            initPriceViews(viewHolder);
            // debug
//            viewHolder.getLayout().setBackgroundColor(getToggledBackgroundColor(position));
            // release
            viewHolder.getTitleTextView().setBackgroundColor(Color.TRANSPARENT);
            viewHolder.getDescriptionTextView().setBackgroundColor(Color.TRANSPARENT);
//            viewHolder.getLayout().setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    // debug용으로 컬러를 얻어내기
    private int getToggledBackgroundColor(int index) {
        if (index % 2 == 0) {
            return Color.parseColor("#cc00cc");
        } else {
            return Color.parseColor("#ff66ff");
        }
    }

    private void initLayout(final StoreItemViewHolder viewHolder) {
        viewHolder.getLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemPriceButtonClicked((String) viewHolder.getPriceTextView().getTag());
            }
        });
    }

    private void initPriceViews(final StoreItemViewHolder viewHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float elevation =
                    mContext.getResources().getDimension(R.dimen.store_item_price_button_elevation);
            viewHolder.getPriceImageView().setElevation(elevation);
            viewHolder.getPriceTextView().setElevation(elevation);
        }
        viewHolder.getPriceTextView().setSelected(true);

        // price - check from inventory
        String sku = (String) viewHolder.getPriceTextView().getTag();

        /*
        if (MNStoreFragment.IS_STORE_FOR_NAVER) {
            // Naver
            boolean hasDetails = false;
            if (naverIabInventoryItemList != null) {
                for (NaverIabInventoryItem naverIabInventoryItem : naverIabInventoryItemList) {
                    if (naverIabInventoryItem.getKey().equals(
                            NaverIabProductUtils.naverSkuMap.get(sku))) {
                        hasDetails = true;

                        if (naverIabInventoryItem.isAvailable()) {
                            viewHolder.getPriceTextView().setText(R.string.store_purchased);
                        } else {
                            viewHolder.getPriceTextView().setText(
                                    "₩" + MNDecimalFormatUtils.makeStringComma(naverIabInventoryItem.getPrice()));
                        }
                    }
                }
                if (!hasDetails) {
                    viewHolder.getPriceTextView().setText(R.string.loading);
                }
            } else {
                viewHolder.getPriceTextView().setText(R.string.loading);
            }
        } else {
            // Google
            if (inventory != null) {
                if (inventory.hasDetails(sku)) {
                    if (inventory.hasPurchase(sku)) {
                        viewHolder.getPriceTextView().setText(R.string.store_purchased);
                    } else {
                        viewHolder.getPriceTextView().setText(inventory.getSkuDetails(sku).getPrice());
                    }
                } else {
                    viewHolder.getPriceTextView().setText(R.string.loading);
                }
            } else {
                viewHolder.getPriceTextView().setText(R.string.loading);
            }
        }
        */

        /*
        // price - purchase check from ownedSkus - 풀버전, 언락은 ownedSkus에서 체크
        if (ownedSkus != null && ownedSkus.contains(sku)) {
            viewHolder.getPriceTextView().setText(R.string.store_purchased);
        } else {
            if (!MNStoreDebugChecker.isUsingStore(mContext)) {
                viewHolder.getPriceTextView().setText("$0.99");
            }
        }
        */

        // onClick
        viewHolder.getPriceTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemPriceButtonClicked((String) v.getTag());
            }
        });

        // set clickable
        /*
        if (viewHolder.getPriceTextView().getText().toString().equals(mContext.getResources().getText(R.string.store_purchased))) {
            viewHolder.getPriceTextView().setClickable(false);
            viewHolder.getInnerLayout().setFocusable(false);
            viewHolder.getInnerLayout().setClickable(false);
            viewHolder.getInnerLayout().setBackgroundResource(
                    R.drawable.shape_rounded_view_classic_gray_normal);
        } else {
            viewHolder.getPriceTextView().setClickable(true);
            viewHolder.getInnerLayout().setFocusable(true);
            viewHolder.getInnerLayout().setClickable(true);
            viewHolder.getInnerLayout().setBackgroundResource(
                    R.drawable.shape_rounded_view_classic_gray);
        }
        */
    }

    /**
     * ViewHolder
     */
    static class StoreItemViewHolder {
        @Getter @InjectView(R.id.store_item_layout) RelativeLayout layout;
        @Getter @InjectView(R.id.store_item_image_view) ImageView imageView;
        @Getter @InjectView(R.id.store_item_title_text_view) TextView titleTextView;
        @Getter @InjectView(R.id.store_item_description_text_view) TextView descriptionTextView;
        @Getter @InjectView(R.id.store_item_price_image_view) ImageView priceImageView;
        @Getter @InjectView(R.id.store_item_price_text_view) TextView priceTextView;

        public StoreItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
