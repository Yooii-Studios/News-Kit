package com.yooiistudios.newsflow.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newsflow.R;
import com.yooiistudios.newsflow.model.Settings;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 4. 13.
 *
 * NotificationAskUtils
 *  앱의 첫 실행시 노티를 받을 것인지 물어보는 유틸
 */
public class NotificationAskUtils {
    public static void showAskNotificationDialog(final Context context) {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.app_name)
                .content(R.string.notification_ask_description)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Settings.setNotification(context, true);
                    }
                })
                .build();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
