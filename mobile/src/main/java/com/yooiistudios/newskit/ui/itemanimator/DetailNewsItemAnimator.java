package com.yooiistudios.newskit.ui.itemanimator;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 2014. 8. 23.
 *
 * NLDetailBottomNewsItemAnimator
 *  디테일 뉴스의 애니메이션을 커스터마이징
 */
public class DetailNewsItemAnimator extends BaseItemAnimator {
    private View parent;

    public DetailNewsItemAnimator(View parent) {
        this.parent = parent;
    }

    @Override
    public PendingAnimator.Add onAdd(RecyclerView.ViewHolder viewHolder) {
        final View v = viewHolder.itemView;
        v.setTranslationY(parent.getHeight() / 2);
        v.setAlpha(0f);

        return new PendingAnimator.Add(viewHolder) {
            @Override
            void animate(OnAnimatorEnd callback) {
                v.animate()
                        .setStartDelay(300)
                        .setDuration(getAddDuration() * 7)
                        .translationY(0)
                        .alpha(1f)
//                        .setInterpolator(new PathInterpolator(0.4f, 0, 1, 1))
                        .setInterpolator(new DecelerateInterpolator())
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

    @Override
    public boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4) {
        return false;
    }
}
