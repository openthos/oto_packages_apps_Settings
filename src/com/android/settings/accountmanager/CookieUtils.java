package com.android.settings.accountmanager;

import android.util.Log;
import android.view.KeyEvent;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;

public class CookieUtils {
    public static String getCookieskey(HttpResponse hc) {
        Header[] header = hc.getHeaders("Set-Cookie");
        return header[0].getValue();
    }

    public static HttpPost putCookieskeyPost(HttpPost post , String cookie) {
        post.addHeader("Cookie", cookie);
        post.addHeader("Referer", "https://dev.openthos.org/accounts/register/");
        return post;
    }

    public static HttpGet putCookieskeyGet(HttpGet get , String cookie) {
        get.addHeader("Cookie", cookie);
        return get;
    }
}
