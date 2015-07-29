package luckynine.youtell.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by Weiliang on 7/11/2015.
 */
public class UserStatus {

    static final String PREF_USER_LOGIN_STATUS = "login_status";
    static final String PREF_USER_ID = "user_id";
    static final String PREF_USER_FIRSTNAME = "firstname";
    static final String PREF_USER_LASTNAME = "lastname";
    static final String PREF_USER_ACCESS_TOKEN = "access_token";
    static final String PREF_USER_PROFILE_PICTURE_URI = "profile_pic_uri";

    public static void setUserLoggedInStatus(Context context, boolean status)
    {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_USER_LOGIN_STATUS, status);
        editor.commit();
    }

    public static boolean getUserLoggedInStatus(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_USER_LOGIN_STATUS, false);
    }

    public static void clearUserInfo(Context context)
    {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(PREF_USER_LOGIN_STATUS, false);
        editor.remove(PREF_USER_ID);
        editor.remove(PREF_USER_FIRSTNAME);
        editor.remove(PREF_USER_LASTNAME);
        editor.remove(PREF_USER_ACCESS_TOKEN);
        editor.remove(PREF_USER_PROFILE_PICTURE_URI);
        editor.commit();
    }

    public static void setAccessToken(Context context, String accessToken)
    {
        setUserData(context, PREF_USER_ACCESS_TOKEN, accessToken);
    }

    public static String getAccessToken(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_ACCESS_TOKEN, null);
    }

    public static void setUserId(Context context, String userId)
    {
        setUserData(context, PREF_USER_ID, userId);
    }

    public static String getUserId(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_ID, null);
    }

    public static void setFirstName(Context context, String firstname)
    {
        setUserData(context, PREF_USER_FIRSTNAME, firstname);
    }

    public static String getFirstName(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_FIRSTNAME, null);
    }

    public static void setLastName(Context context, String lastname)
    {
        setUserData(context, PREF_USER_LASTNAME, lastname);
    }

    public static String getLastName(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_LASTNAME, null);
    }

    public static void setProfilePictureUri(Context context, Uri profilePictureUri){
        setUserData(context, PREF_USER_PROFILE_PICTURE_URI, profilePictureUri.toString());
    }

    public static String getProfilePictureUri(Context context, Uri profilePictureUri){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_PROFILE_PICTURE_URI, null);
    }

    private static void setUserData(Context context, String key, String value){
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.commit();
    }
}
