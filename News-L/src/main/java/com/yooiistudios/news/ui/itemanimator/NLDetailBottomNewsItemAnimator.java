package com.yooiistudios.news.ui.itemanimator;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * NLDetailBottomNewsItemAnimator
 *  디테일 뉴스의 애니메이션을 커스터마이징
 */
public class NLDetailBottomNewsItemAnimator extends BaseItemAnimator {
    private View parent;

    public NLDetailBottomNewsItemAnimator(View parent) {
        this.parent = parent;
    }

    @Override
    public PendingAnimator.Add onAdd(RecyclerView.ViewHolder viewHolder) {
        final View v = viewHolder.itemView;
        v.setTranslationY(parent.getHeight());

        return new PendingAnimator.Add(viewHolder) {
            @Override
            void animate(OnAnimatorEnd callback) {
                v.animate().setDuration(getAddDuration() * 4).translationY(0)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(callback);
            }

            @Override
            void cancel() {
                v.animate().cancel();
                v.setTranslationY(0);
            }
        };
    }

    @Override
    public PendingAnimator.Remove onRemove(RecyclerView.ViewHolder viewHolder) {
        return null;
    }

    @Override
    public PendingAnimator.Move onMove(RecyclerView.ViewHolder viewHolder, int fromX, int fromY, int toX, int toY) {
        return null;
    }
}
