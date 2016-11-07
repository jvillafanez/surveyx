package es.academy.solidgear.surveyx.utils;

import android.util.Log;

import java.net.URLEncoder;

/**
 * Created by oscar on 7/11/16.
 */

public class RequestUtils {
    public static String encode(String s){

    try {
            return URLEncoder.encode(s,"utf-8");
        } catch (Exception e) {
            Log.e("UserLoginRequest","Unsupported encoding error", e);
            return "";
        }
    }
}
