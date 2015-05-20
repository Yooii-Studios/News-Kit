package com.yooiistudios.newskit.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yooiistudios.newskit.R;

import java.util.ArrayList;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;

/**
 * Created by StevenKim in MNSettingActivityProject from Yooii Studios Co., LTD. on 2014. 1. 8.
 *
 * MNCreditListAdapter
 */
public class CreditListAdapter extends BaseAdapter {
    private Context context;

    @SuppressWarnings("UnusedDeclaration")
    private CreditListAdapter() {}
    public CreditListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 9;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.info_credit_item, parent, false);
        }
        final MNSettingInfoCreditItemViewHolder viewHolder = new MNSettingInfoCreditItemViewHolder(convertView);

        viewHolder.getOuterLayout().setOnClickListener(null);
        viewHolder.getTitleTextView().clearAnimation();
        viewHolder.getNameTextView().clearAnimation();
        switch (position) {
            case 0:
                viewHolder.getTitleTextView().setText("Executive Producer");
                viewHolder.getNameTextView().setText("Robert Song");
                break;
            case 1:
                viewHolder.getTitleTextView().setText("Software Engineer");
                viewHolder.getNameTextView().setText(
                        "Wooseong Kim\n" +
                        "Chris Jeong");
                viewHolder.getOuterLayout().setOnClickListener(new View.OnClickListener() {
                    private int mClickCount = 0;
                    private ArrayList<Integer> mSpanned = new ArrayList<>();
                    private ArrayList<Integer> mNonSpanned;

                    @Override
                    public void onClick(View v) {
                        if (++mClickCount > 10) {
                            CharSequence sequence = viewHolder.getNameTextView().getText();
                            if (sequence != null) {
                                String content = sequence.toString();

                                if (mNonSpanned == null) {
                                    mNonSpanned = new ArrayList<>();
                                    for (int i = 0; i < content.length(); i++) {
                                        mNonSpanned.add(i);
                                    }
                                }
                                if (mSpanned.size() < content.length()) {
                                    int randomIdx =
                                            Math.abs(new Random(System.currentTimeMillis()).nextInt())
                                                    % mNonSpanned.size();

                                    mSpanned.add(mNonSpanned.remove(randomIdx));

                                    SpannableStringBuilder sp = new SpannableStringBuilder(content);
                                    for (Integer spanIdx : mSpanned) {
                                        sp.setSpan(new ForegroundColorSpan(Color.WHITE),
                                                spanIdx, spanIdx + 1,
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    }
                                    viewHolder.getNameTextView().setText(sp);
                                }
                            }
                        }

                        if (mClickCount == 5) {
                            TranslateAnimation anim = new TranslateAnimation(
                                    Animation.RELATIVE_TO_SELF,  0,
                                    Animation.RELATIVE_TO_SELF, 0.01f,
                                    Animation.RELATIVE_TO_SELF,  0,
                                    Animation.RELATIVE_TO_SELF,  0
                            );
                            anim.setDuration(50);
                            anim.setRepeatCount(Animation.INFINITE);
                            anim.setRepeatMode(Animation.REVERSE);
                            anim.setInterpolator(new AccelerateDecelerateInterpolator());
                            viewHolder.getTitleTextView().startAnimation(anim);

                            anim = new TranslateAnimation(
                                    Animation.RELATIVE_TO_SELF,  0,
                                    Animation.RELATIVE_TO_SELF, 0.01f,
                                    Animation.RELATIVE_TO_SELF,  0,
                                    Animation.RELATIVE_TO_SELF,  0
                            );
                            anim.setDuration(50);
                            anim.setStartOffset(10);
                            anim.setRepeatCount(Animation.INFINITE);
                            anim.setRepeatMode(Animation.REVERSE);
                            anim.setInterpolator(new AccelerateDecelerateInterpolator());
                            viewHolder.getNameTextView().startAnimation(anim);
                        }
                    }
                });
                break;
            case 2:
                viewHolder.getTitleTextView().setText("Main Artist");
                viewHolder.getNameTextView().setText("Ted");
                break;
            case 3:
                viewHolder.getTitleTextView().setText("Associate Producer");
                viewHolder.getNameTextView().setText("Jasmine Oh");
                break;
            case 4:
                viewHolder.getTitleTextView().setText("Development Manager");
                viewHolder.getNameTextView().setText("Jeff Jeong");
                break;
            case 5:
                viewHolder.getTitleTextView().setText("QA");
                viewHolder.getNameTextView().setText("Yooii Studios Members");
                break;
            case 6:
                viewHolder.getTitleTextView().setText("Development Consulting by");
                viewHolder.getNameTextView().setText("PlayFluent");
                break;
            case 7:
                viewHolder.getTitleTextView().setText("Localization");
                viewHolder.getNameTextView().setText(
                        "Angela Choi\n" +
                        "Boris Yang\n" +
                        "Moritz Will\n" +
                        "Taft Love\n" +
                        "Yu Wang\n" +
                        "Yuki Endo\n" +
                        "Peter Van Dyke\n" +
                        "Van Anh Nguyen Thi");
                break;
            case 8:
                viewHolder.getTitleTextView().setText("Special Thanks to");
                viewHolder.getNameTextView().setText("Andrew Ryu\n" +
                        "Aotter\n" +
                        "HyoSang Lim\n" +
                        "JongHwa Kim\n" +
                        "KwanSoo Choi\n" +
                        "Lou Hsin\n" +
                        "Osamu Takahashi\n" +
                        "The Great Frog Party\n" +
                        "Thomas Moon");
                break;
        }
        return convertView;
    }

    /**
     * ViewHolder
     */
    static class MNSettingInfoCreditItemViewHolder {
        @Getter
        @InjectView(R.id.setting_info_credit_item_outer_layout)
        RelativeLayout outerLayout;
        @Getter
        @InjectView(R.id.setting_info_credit_item_inner_layout)
        RelativeLayout innerLayout;
        @Getter
        @InjectView(R.id.setting_info_credit_item_title_textview)
        TextView titleTextView;
        @Getter
        @InjectView(R.id.setting_info_credit_item_name_textview)
        TextView nameTextView;

        public MNSettingInfoCreditItemViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
