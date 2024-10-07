package com.breadwallet.tools.manager;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import timber.log.Timber;

public class BRClipboardManager {

    @SuppressLint("NewApi")
    public static void putClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("message", text);
            clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @SuppressLint("NewApi")
    public static String getClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = context.getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String text = null;
            String title = null;

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            // Tries to get the item's contents as a URI pointing to a note
            Uri uri = item.getUri();

            // If the contents of the clipboard wasn't a reference to a
            // note, then
            // this converts whatever it is to text.
            text = coerceToText(item).toString();

            return text;
        }
        return "";
    }

    @SuppressLint("NewApi")
    private static CharSequence coerceToText(ClipData.Item item) {
        // If this Item has an explicit textual value, simply return that.
        CharSequence text = item.getText();
        if (text != null) {
            return text;
        } else {
            return "no text";
        }

    }

}