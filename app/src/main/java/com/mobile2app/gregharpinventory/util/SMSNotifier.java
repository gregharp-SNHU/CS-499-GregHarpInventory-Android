package com.mobile2app.gregharpinventory.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.content.ContextCompat;

import com.mobile2app.gregharpinventory.R;

public final class SMSNotifier {
    // request code used when asking the user for SMS permission
    public static final int REQ_SEND_SMS = 4221;

    // set up for SharedPreferences
    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_SMS_ON = "sms_enabled";
    private static final String KEY_PHONE = "phone";

    // default constructor
    private SMSNotifier() {
        // nothing to do here
    }

    // method to trigger an SMS when an item transitions to "out of stock"
    public static void notifyOutOfStock(Context context, String itemName) {
        // read the SMS settings from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // check if SMS alerts are enabled
        boolean enabled = prefs.getBoolean(KEY_SMS_ON, false);

        // get the configured destination phone number
        String phone = prefs.getString(KEY_PHONE, "");

        // if SMS is disabled or the phone number is missing, abort
        if (!enabled || phone.isEmpty()) {
            // notify user SMS is disabled
            Toaster.show(context, R.string.sms_disabled_or_no_phone);
            return;
        }

        // ensure this device actually supports telephony/SMS
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            // notify the user that SMS isn't supported on this device
            Toaster.show(context, R.string.sms_no_telephony);
            return;
        }

        // check if SEND_SMS permission has been granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toaster.show(context, R.string.permission_denied);
            return;
        }

        // build the SMS message body from a string resource with the item name
        String body = context.getString(R.string.sms_out_of_stock, itemName);

        // we have permission: send the SMS
        try {
            // use the default SmsManager to send a simple text message
            SmsManager.getDefault().sendTextMessage(phone, null, body, null, null);
            // notify the user that the SMS was sent successfully
            Toaster.show(context, context.getString(R.string.sms_sent, body));
        }
        catch (Exception e) {
            // if sending fails, notify the user with a generic failure message
            Toaster.show(context, R.string.sms_failed_to_send);
        }
    }
}
