package com.yooiistudios.newskit.model.debug;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.yooiistudios.newskit.R;

/**
 * Created by Wooseong Kim in News-Android-L from Yooii Studios Co., LTD. on 14. 11. 7.
 *
 * DebugSettingDialogs
 *  디버그 관련 세팅을 할 수 있는 다이얼로그를 생성
 */
public class DebugAnimationSettingDialogFactory {
    private DebugAnimationSettingDialogFactory() { throw new AssertionError("You can't create this class!"); }

    public interface DebugSettingListener {
        public void autoScrollSettingSaved();
    }

    public static void showAutoScrollSettingDialog(final Context context,
                                                   final DebugSettingListener listener) {

        @SuppressLint("InflateParams")
        final View dialogContent = LayoutInflater.from(context).inflate(R.layout
                .dialog_auto_scroll_setting, null);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Auto Scroll Setting")
                .setView(dialogContent)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        EditText startDelayEditText =
                                (EditText) dialogContent.findViewById(R.id.auto_scroll_start_delay_edit_text);
                        EditText durationEditText =
                                (EditText) dialogContent.findViewById(R.id.auto_scroll_duration_edit_text);
                        EditText midDelayEditText =
                                (EditText) dialogContent.findViewById(R.id.auto_scroll_mid_delay_edit_text);

                        int startDelay =
                                Integer.valueOf((startDelayEditText).getText().toString());
                        int durationForEachItem =
                                Integer.valueOf(durationEditText.getText().toString());
                        int midDelay =
                                Integer.valueOf(midDelayEditText.getText().toString());

                        DebugAnimationSettings.setStartDelay(context, startDelay);
                        DebugAnimationSettings.setDurationForEachItem(context, durationForEachItem);
                        DebugAnimationSettings.setMidDelay(context, midDelay);
                        if (listener != null) {
                            listener.autoScrollSettingSaved();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                EditText startDelayEditText =
                        (EditText) dialogContent.findViewById(R.id.auto_scroll_start_delay_edit_text);
                EditText durationEditText =
                        (EditText) dialogContent.findViewById(R.id.auto_scroll_duration_edit_text);
                EditText midDelayEditText =
                        (EditText) dialogContent.findViewById(R.id.auto_scroll_mid_delay_edit_text);

                startDelayEditText.setText(String.valueOf(DebugAnimationSettings.getStartDelay(context)));
                durationEditText.setText(String.valueOf(DebugAnimationSettings.getDurationForEachItem(context)));
                midDelayEditText.setText(String.valueOf(DebugAnimationSettings.getMidDelay(context)));
            }
        });
        dialog.show();
    }
}
