package com.yooiistudios.newskit.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yooiistudios.newskit.R;
import com.yooiistudios.newskit.model.Settings;

/**
 * Created by Wooseong Kim in News Kit from Yooii Studios Co., LTD. on 15. 4. 13.
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

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
