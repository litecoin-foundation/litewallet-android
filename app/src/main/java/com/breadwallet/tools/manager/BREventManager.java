package com.breadwallet.tools.manager;

import android.content.Context;

import com.breadwallet.BreadApp;
import com.breadwallet.BreadApp_java;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public class BREventManager implements BreadApp.OnAppBackgrounded {
    private static BREventManager instance;
    private String sessionId;
    private List<Event> events = new ArrayList<>();

    private BREventManager() {
        sessionId = UUID.randomUUID().toString();
        BreadApp.Companion.addOnBackgroundedListener(this::onBackgrounded);
    }

    public static BREventManager getInstance() {
        if (instance == null) instance = new BREventManager();
        return instance;
    }

    public void pushEvent(String eventName, Map<String, String> attributes) {
        Timber.d("timber: pushEvent: %s", eventName);
        Event event = new Event(sessionId, System.currentTimeMillis() * 1000, eventName, attributes);
        events.add(event);
    }

    public void pushEvent(String eventName) {
        Timber.d("timber: pushEvent: %s", eventName);
        Event event = new Event(sessionId, System.currentTimeMillis() * 1000, eventName, null);
        events.add(event);
    }

    @Override
    public void onBackgrounded() {
        Timber.d("timber: onBackgrounded: ");
    }

    //returns the list of JSONArray which consist of Event arrays
    private static List<JSONArray> getEventsFromDisk(Context context) {
        List<JSONArray> result = new ArrayList<>();
        File dir = new File(context.getFilesDir().getAbsolutePath() + "/events/");
        if (dir.listFiles() == null) return result;
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                Timber.d("timber: getEventsFromDisk: name:%s", name);
                try {
                    JSONArray arr = new JSONArray(readFile(name));
                    result.add(arr);
                } catch (JSONException e) {
                    Timber.e(e);
                }
            } else {
                Timber.i("timber: getEventsFromDisk: Unexpected directory where file is expected: %s", f.getName());
            }
        }
        return result;
    }

    private static String readFile(String fileName) {
        try {
            File f = new File(fileName);
            //check whether file exists
            FileInputStream is = new FileInputStream(f);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (IOException e) {
            Timber.e(e, "timber:Error in Reading");
            return null;
        }
    }

    public class Event {
        public String sessionId;
        public long time;
        public String eventName;
        public Map<String, String> attributes;

        public Event(String sessionId, long time, String eventName, Map<String, String> attributes) {
            this.sessionId = sessionId;
            this.time = time;
            this.eventName = eventName;
            this.attributes = attributes;
        }
    }
}
