package com.yooiistudios.newsflow.ui.animation;

/**
 * Created by Dongheyon Jeong in News-Android-L from Yooii Studios Co., LTD. on 15. 2. 26.
 *
 * PointEvaluator
 *  Point 객체를 사용하는 TypeEvaluator. 구글 내부 코드에서 PointFEvaluator 내용을 복사해옴.
 */


import android.animation.TypeEvaluator;
import android.graphics.Point;

public class PointEvaluator implements TypeEvaluator<Point> {

    /**
     * When null, a new Point is returned on every evaluate call. When non-null,
     * mPoint will be modified and returned on every evaluate.
     */
    private Point mPoint;

    /**
     * Construct a PointEvaluator that returns a new Point on every evaluate call.
     * To avoid creating an object for each evaluate call,
     * {@link PointEvaluator#PointEvaluator(android.graphics.Point)} should be used
     * whenever possible.
     */
    public PointEvaluator() {
    }

    /**
     * Constructs a PointEvaluator that modifies and returns <code>reuse</code>
     * in {@link #evaluate(float, android.graphics.Point, android.graphics.Point)} calls.
     * The value returned from
     * {@link #evaluate(float, android.graphics.Point, android.graphics.Point)} should
     * not be cached because it will change over time as the object is reused on each
     * call.
     *
     * @param reuse A Point to be modified and returned by evaluate.
     */
    public PointEvaluator(Point reuse) {
        mPoint = reuse;
    }

    /**
     * This function returns the result of linearly interpolating the start and
     * end Point values, with <code>fraction</code> representing the proportion
     * between the start and end values. The calculation is a simple parametric
     * calculation on each of the separate components in the Point objects
     * (x, y).
     *
     * <p>If {@link #PointEvaluator(android.graphics.Point)} was used to construct
     * this PointEvaluator, the object returned will be the <code>reuse</code>
     * passed into the constructor.</p>
     *
     * @param fraction   The fraction from the starting to the ending values
     * @param startValue The start Point
     * @param endValue   The end Point
     * @return A linear interpolation between the start and end values, given the
     *         <code>fraction</code> parameter.
     */
    @Override
    public Point evaluate(float fraction, Point startValue, Point endValue) {
        int x = (int)(startValue.x + (fraction * (endValue.x - startValue.x)));
        int y = (int)(startValue.y + (fraction * (endValue.y - startValue.y)));

        if (mPoint != null) {
            mPoint.set(x, y);
            return mPoint;
        } else {
            return new Point(x, y);
        }
    }
}

