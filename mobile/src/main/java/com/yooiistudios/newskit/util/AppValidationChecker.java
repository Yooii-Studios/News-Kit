package com.yooiistudios.newskit.util;

import android.app.Activity;
import android.widget.Toast;

import java.util.Calendar;

// 윤승용 제작. 김우성 감수 (특정 날짜를 지정해놓고 메인 액티비티의 onCreate 에서 사용하면 날짜에 따라 알아서 죽음)
public class AppValidationChecker {
    private static final int VALIDATE_YEAR = 2014;
    private static final int VALIDATE_MONTH = Calendar.DECEMBER;
    private static final int VALIDATE_DAY = 30;

    private AppValidationChecker() { throw new AssertionError(); } // You must not create instance

    public static void validationCheck(Activity c) {
        Calendar calendarForCheck = Calendar.getInstance();
        
        boolean isValidate = true;
        
        if (calendarForCheck.get(Calendar.YEAR) >= VALIDATE_YEAR) {
            if (calendarForCheck.get(Calendar.MONTH) > VALIDATE_MONTH) {
                isValidate = false;
            } else if (calendarForCheck.get(Calendar.MONTH) == VALIDATE_MONTH) {
                if (calendarForCheck.get(Calendar.DAY_OF_MONTH) > VALIDATE_DAY) {
                    isValidate = false;
                }
            }
        }
        
        if (!isValidate) {
            Toast.makeText(c.getApplicationContext(), "News L is over the validation date. Please request to the developer.", Toast.LENGTH_SHORT).show();
            c.finish();
        }
    }
}
