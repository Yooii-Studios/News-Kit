package com.yooiistudios.newsflow.core.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

/**
 * Created by Wooseong Kim in News Flow from Yooii Studios Co., LTD. on 15. 2. 19.
 *
 * Device
 *  안드로이드 버전을 체크할 수 있는 클래스
 */
public class Device {
    private Device() { throw new AssertionError("You MUST not create this class!"); }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static class Profile {
        public static String getUserNameOrDefault(Context context) {
            String userName;
            try {
                userName = getUserName(context);
            } catch(SecurityException e) {
                userName = "default user name";
            }

            return userName;
        }
        private static String getUserName(Context context) throws SecurityException {
            final ContentResolver content = context.getContentResolver();
            final Cursor cursor = content.query(
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                    ProfileQuery.PROJECTION,
                    ContactsContract.Contacts.Data.MIMETYPE + "=?",
                    new String[]{ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE},
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
            );

            String deviceUserName = null;

            while (cursor.moveToNext()) {
                if (cursor.getString(ProfileQuery.GIVEN_NAME) == null && cursor.getString(ProfileQuery.FAMILY_NAME) == null) {
                    continue;
                }
                else {
                    String givenName = cursor.getString(ProfileQuery.GIVEN_NAME);

                    String familyName = cursor.getString(ProfileQuery.FAMILY_NAME);

                    deviceUserName = (givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName);

                    break;
                }
            }

            cursor.close();

            if (deviceUserName == null || deviceUserName.trim().isEmpty()) {
                return "Unknown";
            }
            else {
                return deviceUserName.trim();
            }
        }

        private interface ProfileQuery{
            /**
             * The set of columns to extract from the profile query results
             */
            String[] PROJECTION = {
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            };

            /**
             * Column index for the family name in the profile query results
             */
            int FAMILY_NAME = 0;
            /**
             * Column index for the given name in the profile query results
             */
            int GIVEN_NAME = 1;
        }
    }
}
