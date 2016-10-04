package com.phonepe.merchantsdk.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * @author Sharath Pandeshwar
 * @since 28/09/16.
 */
public class CacheUtils {
    private static final String TAG = "CacheUtils";

    /* Reference to shared preference */
    private SharedPreferences mSharedPreferences;
    private static CacheUtils sCacheUtils;

    /**
     * Keys
     */
    private static String sUserIdKey = "key_user_id";
    private static String sAmountKey = "key_amount";


    private CacheUtils(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static CacheUtils getInstance(Context context) {
        if (sCacheUtils == null) {
            synchronized (CacheUtils.class) {
                sCacheUtils = new CacheUtils(context);
            }
        }
        return sCacheUtils;
    }

    //*********************************************************************
    // APIs
    //*********************************************************************


    public void saveUserId(String userId) {
        mSharedPreferences.edit().putString(sUserIdKey, userId).apply();
    }

    public String getUserId() {
        return mSharedPreferences.getString(sUserIdKey, "unknown143");
    }


    public void saveAmountForTransaction(Long amount) {
        mSharedPreferences.edit().putLong(sAmountKey, amount).apply();
    }

    public Long getAmountForTransaction() {
        return mSharedPreferences.getLong(sAmountKey, 2L);
    }

    //*********************************************************************
    // End of the class
    //*********************************************************************

}
