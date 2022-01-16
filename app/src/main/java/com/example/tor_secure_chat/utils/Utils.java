package com.example.tor_secure_chat.utils;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static Context currentContext;

    public static void setCurrentAppContext(Context currentContext) {
        Utils.currentContext = currentContext;
    }

    public static Context getCurrentAppContext() {
        return currentContext;
    }

    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat timeFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat = new SimpleDateFormat("HH:mm:ss");
    }

    public static String formatDate(long date) {
        return dateFormat.format(new Date(date));
    }

    public static String formatTime(long date) {
        return timeFormat.format(new Date(date));
    }

    public static void alert(String title, String message) {
        alert(currentContext, title, message, "Ok");
    }

    public static void alert(Context context, String title, String message) {
        alert(context, title, message, "Ok");
    }

    public static void alert(String title, String message, String buttonName) {
        alert(currentContext, title, message, buttonName);
    }

    public static void alert(Context context, String title, String message, String buttonName) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(buttonName, (dialog, which) -> { }).show();
    }

    public static ProgressDialog loadingAlert(String title, String message) {
        return loadingAlert(currentContext, title, message);
    }

    public static ProgressDialog loadingAlert(Context context, String title, String message) {
        ProgressDialog progress = new ProgressDialog(context);
        progress.setTitle(title);
        progress.setMessage(message);
        progress.setCancelable(false);
        progress.show();
        return progress;
    }

}
