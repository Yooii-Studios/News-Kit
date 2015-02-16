package com.yooiistudios.news.util;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.EditText;

import com.yooiistudios.news.R;
import com.yooiistudios.news.ui.animation.CubicBezierInterpolator;

/**
 * Created by Dongheyon Jeong on in News-Android-L from Yooii Studios Co., LTD. on 2014. 10. 11.
 * 
 * DialogFactory
 *  다이얼로그를 만드는 팩토리
 */
public class InterpolatorHelper {
    public static final String IMG_SCALE_CP_1X = "IMG_SCALE_CP_1X";
    public static final String IMG_SCALE_CP_1Y = "IMG_SCALE_CP_1Y";
    public static final String IMG_SCALE_CP_2X = "IMG_SCALE_CP_2X";
    public static final String IMG_SCALE_CP_2Y = "IMG_SCALE_CP_2Y";

    public static final String TRANSITION_CP_1X = "TRANSITION_CP_1X";
    public static final String TRANSITION_CP_1Y = "TRANSITION_CP_1Y";
    public static final String TRANSITION_CP_2X = "TRANSITION_CP_2X";
    public static final String TRANSITION_CP_2Y = "TRANSITION_CP_2Y";

    public static final String ROOT_BOUND_WIDTH_CP_1X = "ROOT_BOUND_WIDTH_CP_1X";
    public static final String ROOT_BOUND_WIDTH_CP_1Y = "ROOT_BOUND_WIDTH_CP_1Y";
    public static final String ROOT_BOUND_WIDTH_CP_2X = "ROOT_BOUND_WIDTH_CP_2X";
    public static final String ROOT_BOUND_WIDTH_CP_2Y = "ROOT_BOUND_WIDTH_CP_2Y";

    public static final String ROOT_BOUND_HEIGHT_CP_1X = "ROOT_BOUND_HEIGHT_CP_1X";
    public static final String ROOT_BOUND_HEIGHT_CP_1Y = "ROOT_BOUND_HEIGHT_CP_1Y";
    public static final String ROOT_BOUND_HEIGHT_CP_2X = "ROOT_BOUND_HEIGHT_CP_2X";
    public static final String ROOT_BOUND_HEIGHT_CP_2Y = "ROOT_BOUND_HEIGHT_CP_2Y";
    
    private InterpolatorHelper() {
        throw new AssertionError("You MUST NOT create this class's instance!!");
    }
    
    public static void showDialog(final Context context) {
        @SuppressLint("InflateParams")
        final View dialogContent = LayoutInflater.from(context).inflate(R.layout
                .dialog_bezier, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Edit Bezier")
                .setView(dialogContent)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = context.getSharedPreferences
                                ("bezier", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        
                        editor.putFloat(IMG_SCALE_CP_1X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.image_scale_cp_1x)).getText().toString()));
                        editor.putFloat(IMG_SCALE_CP_1Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.image_scale_cp_1y)).getText().toString()));
                        editor.putFloat(IMG_SCALE_CP_2X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.image_scale_cp_2x)).getText().toString()));
                        editor.putFloat(IMG_SCALE_CP_2Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.image_scale_cp_2y)).getText().toString()));

                        editor.putFloat(TRANSITION_CP_1X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.transition_cp_1x)).getText().toString()));
                        editor.putFloat(TRANSITION_CP_1Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.transition_cp_1y)).getText().toString()));
                        editor.putFloat(TRANSITION_CP_2X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.transition_cp_2x)).getText().toString()));
                        editor.putFloat(TRANSITION_CP_2Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.transition_cp_2y)).getText().toString()));

                        editor.putFloat(ROOT_BOUND_WIDTH_CP_1X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_1x)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_WIDTH_CP_1Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_1y)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_WIDTH_CP_2X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_2x)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_WIDTH_CP_2Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_2y)).getText().toString()));

                        editor.putFloat(ROOT_BOUND_HEIGHT_CP_1X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_1x)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_HEIGHT_CP_1Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_1y)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_HEIGHT_CP_2X,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_2x)).getText().toString()));
                        editor.putFloat(ROOT_BOUND_HEIGHT_CP_2Y,
                                Float.valueOf(((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_2y)).getText().toString()));
                        
                        editor.apply();
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("bezier", Context.MODE_PRIVATE);

                ((EditText)dialogContent.findViewById(R.id.image_scale_cp_1x)).setText(
                        String.valueOf(sharedPreferences.getFloat(IMG_SCALE_CP_1X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.image_scale_cp_1y)).setText(
                        String.valueOf(sharedPreferences.getFloat(IMG_SCALE_CP_1Y, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.image_scale_cp_2x)).setText(
                        String.valueOf(sharedPreferences.getFloat(IMG_SCALE_CP_2X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.image_scale_cp_2y)).setText(
                        String.valueOf(sharedPreferences.getFloat(IMG_SCALE_CP_2Y, -1.f)));

                ((EditText)dialogContent.findViewById(R.id.transition_cp_1x)).setText(
                        String.valueOf(sharedPreferences.getFloat(TRANSITION_CP_1X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.transition_cp_1y)).setText(
                        String.valueOf(sharedPreferences.getFloat(TRANSITION_CP_1Y, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.transition_cp_2x)).setText(
                        String.valueOf(sharedPreferences.getFloat(TRANSITION_CP_2X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.transition_cp_2y)).setText(
                        String.valueOf(sharedPreferences.getFloat(TRANSITION_CP_2Y, -1.f)));

                ((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_1x)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_WIDTH_CP_1X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_1y)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_WIDTH_CP_1Y, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_2x)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_WIDTH_CP_2X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_width_cp_2y)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_WIDTH_CP_2Y, -1.f)));

                ((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_1x)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_HEIGHT_CP_1X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_1y)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_HEIGHT_CP_1Y, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_2x)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_HEIGHT_CP_2X, -1.f)));
                ((EditText)dialogContent.findViewById(R.id.root_bound_height_cp_2y)).setText(
                        String.valueOf(sharedPreferences.getFloat(ROOT_BOUND_HEIGHT_CP_2Y, -1.f)));
            }
        });
        dialog.show();
    }

    public static void saveDefaultSetting(Context context) {
        makeImageAndRootTransitionInterpolator(context);
        makeImageScaleInterpolator(context);
        makeRootHeightScaleInterpolator(context);
        makeRootWidthScaleInterpolator(context);
    }

    public static TimeInterpolator makeImageAndRootTransitionInterpolator(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bezier", Context.MODE_PRIVATE);
        float oneX = sharedPreferences.getFloat(InterpolatorHelper.TRANSITION_CP_1X, -1);
        float oneY = sharedPreferences.getFloat(InterpolatorHelper.TRANSITION_CP_1Y, -1);
        float twoX = sharedPreferences.getFloat(InterpolatorHelper.TRANSITION_CP_2X, -1);
        float twoY = sharedPreferences.getFloat(InterpolatorHelper.TRANSITION_CP_2Y, -1);

        if (oneX < 0 || oneY < 0 || twoX < 0 || twoY < 0) {
//            oneX = .68f;
//            oneY = .92f;
//            twoX = .33f;
//            twoY = .96f;
            oneX = .42f;
            oneY = .10f;
            twoX = .58f;
            twoY = .99f;
            sharedPreferences.edit()
                    .putFloat(InterpolatorHelper.TRANSITION_CP_1X, oneX)
                    .putFloat(InterpolatorHelper.TRANSITION_CP_1Y, oneY)
                    .putFloat(InterpolatorHelper.TRANSITION_CP_2X, twoX)
                    .putFloat(InterpolatorHelper.TRANSITION_CP_2Y, twoY)
                    .apply();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(oneX, oneY, twoX, twoY);
        } else {
            return new CubicBezierInterpolator(oneX, oneY, twoX, twoY);
        }
    }

    public static TimeInterpolator makeImageScaleInterpolator(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bezier", Context.MODE_PRIVATE);
        float oneX = sharedPreferences.getFloat(InterpolatorHelper.IMG_SCALE_CP_1X, -1);
        float oneY = sharedPreferences.getFloat(InterpolatorHelper.IMG_SCALE_CP_1Y, -1);
        float twoX = sharedPreferences.getFloat(InterpolatorHelper.IMG_SCALE_CP_2X, -1);
        float twoY = sharedPreferences.getFloat(InterpolatorHelper.IMG_SCALE_CP_2Y, -1);

        if (oneX < 0 || oneY < 0 || twoX < 0 || twoY < 0) {
//            oneX = .33f;
//            oneY = .12f;
//            twoX = .04f;
//            twoY = 1.f;
            oneX = .36f;
            oneY = .12f;
            twoX = .04f;
            twoY = 1.f;
            sharedPreferences.edit()
                    .putFloat(InterpolatorHelper.IMG_SCALE_CP_1X, oneX)
                    .putFloat(InterpolatorHelper.IMG_SCALE_CP_1Y, oneY)
                    .putFloat(InterpolatorHelper.IMG_SCALE_CP_2X, twoX)
                    .putFloat(InterpolatorHelper.IMG_SCALE_CP_2Y, twoY)
                    .apply();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(oneX, oneY, twoX, twoY);
        } else {
            return new CubicBezierInterpolator(oneX, oneY, twoX, twoY);
        }
    }

    public static TimeInterpolator makeRootWidthScaleInterpolator(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bezier", Context.MODE_PRIVATE);
        float oneX = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_1X, -1);
        float oneY = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_1Y, -1);
        float twoX = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_2X, -1);
        float twoY = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_2Y, -1);

        if (oneX < 0 || oneY < 0 || twoX < 0 || twoY < 0) {
            oneX = .38f;
            oneY = .12f;
            twoX = .04f;
            twoY = 1.f;
            sharedPreferences.edit()
                    .putFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_1X, oneX)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_1Y, oneY)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_2X, twoX)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_WIDTH_CP_2Y, twoY)
                    .apply();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(oneX, oneY, twoX, twoY);
        } else {
            return new CubicBezierInterpolator(oneX, oneY, twoX, twoY);
        }
    }

    public static TimeInterpolator makeRootHeightScaleInterpolator(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bezier", Context.MODE_PRIVATE);
        float oneX = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_1X, -1);
        float oneY = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_1Y, -1);
        float twoX = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_2X, -1);
        float twoY = sharedPreferences.getFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_2Y, -1);

        if (oneX < 0 || oneY < 0 || twoX < 0 || twoY < 0) {
//            oneX = .75f;
//            oneY = 0.f;
//            twoX = .25f;
//            twoY = 1.f;
            oneX = .56f;
            oneY = .08f;
            twoX = .02f;
            twoY = .99f;
            sharedPreferences.edit()
                    .putFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_1X, oneX)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_1Y, oneY)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_2X, twoX)
                    .putFloat(InterpolatorHelper.ROOT_BOUND_HEIGHT_CP_2Y, twoY)
                    .apply();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(oneX, oneY, twoX, twoY);
        } else {
            return new CubicBezierInterpolator(oneX, oneY, twoX, twoY);
        }
    }
}
