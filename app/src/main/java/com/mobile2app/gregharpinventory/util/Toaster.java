package com.mobile2app.gregharpinventory.util;

// I created this class because I was tired of Toasts queuing up if I clicked
// something like "Login" a bunch of times when the password was incorrect.
// This code clears out any queue of toasts and is generalized to use across
// several different activities

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public final class Toaster {
    // the current toast (so we can cancel any previous toast)
    private static Toast current;

    // handler tied to main UI thread
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // default constructor
    private Toaster() {
        // nothing to do here
    }

    // method to show a Toast using a string resource ID
    public static void show(Context context, int toastMessage) {
        // call the CharSequence method
        show(context, context.getString(toastMessage));
    }

    // method to show a Toast using a raw CharSequence
    public static void show(Context context, CharSequence message) {
        // runnable that performs the Toast work on the main thread
        Runnable r = () -> {
            // if there is already a Toast visible, cancel it
            if (current != null)
                current.cancel();

            // create the new Toast
            current = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);

            // display the Toast
            current.show();
        };

        // if we're already on the main thread, run immediately - otherwise post to main
        if (Looper.myLooper() == Looper.getMainLooper())
            r.run();
        else
            MAIN.post(r);
    }

    // method to cancel any active Toast and clear references
    public static void cancel() {
        // runnable that safely cancels the toast in the main thread
        Runnable r = () -> {
            // verify that we're not trying to cancel a non-existent Toast
            if (current != null) {
                // cancel the current Toast
                current.cancel();

                // clear the reference
                current = null;
            }
        };

        // if on the main thread, run now - otherwise post to main
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        }
        else {
            MAIN.post(r);
        }
    }
}
