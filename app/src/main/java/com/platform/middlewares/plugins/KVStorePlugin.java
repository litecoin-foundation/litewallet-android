package com.platform.middlewares.plugins;

import android.util.Log;

import com.breadwallet.presenter.activities.MainActivity;
import com.platform.APIClient;
import com.platform.interfaces.Plugin;
import com.platform.kvstore.CompletionObject;
import com.platform.kvstore.RemoteKVStore;
import com.platform.kvstore.ReplicatedKVStore;
import com.platform.sqlite.KVEntity;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 11/1/16.
 * Copyright (c) 2016 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class KVStorePlugin implements Plugin {
    public static final String TAG = KVStorePlugin.class.getName();

    @Override
    public boolean handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {

        if (target.startsWith("/_kv/")) {
            String key = target.replace("/_kv/", "");
            MainActivity app = MainActivity.app;
            if (app == null) {
                try {
                    response.sendError(500, "context is null");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            if (key.isEmpty()) {
                Log.e(TAG, "handle: missing key argument");
                try {
                    response.sendError(400);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            RemoteKVStore remote = RemoteKVStore.getInstance(APIClient.getInstance(app));
            ReplicatedKVStore store = new ReplicatedKVStore(app, remote);
            switch (request.getMethod()) {
                case "GET":
                    CompletionObject getObj = store.get(key, 0);
                    KVEntity kv = getObj.kv;
                    if (kv == null) {
                        Log.e(TAG, "handle: missing key argument");
                        try {
                            response.sendError(400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("v", new String(kv.getValue()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "handle: WARNING failed to create JSON");
                        try {
                            response.sendError(500);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return false;
                    }
                    response.setHeader("ETag", String.valueOf(kv.getVersion()));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss:SSS Z", Locale.getDefault());
                    String date = dateFormat.format(kv.getTime());
                    response.setHeader("Last-Modified", date);

                    if (kv.getDeleted() > 0) {
                        try {
                            response.sendError(410, "Gone");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    response.setHeader("Content-Type", "application/json");
                    response.setStatus(200);
                    break;
                case "PUT":
                    // Read from request
                    String rawData = null;
                    try {
                        InputStream body = request.getInputStream();
                        rawData = IOUtils.toString(body);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (rawData == null) {
                        Log.e(TAG, "handle: missing request body");
                        try {
                            response.sendError(400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    if ("gzip".equalsIgnoreCase(request.getHeader("content-encoding"))) {
                        Log.e(TAG, "sendRequest: the content is gzip! UNZIPPING");
                        byte[] decompressed = APIClient.extractGZIP(rawData.getBytes());
                        assert (decompressed != null);
                        rawData = new String(decompressed);
                    }

                    String strVersion = request.getHeader("if-none-match");
                    if (strVersion == null) {
                        Log.e(TAG, "handle: missing If-None-Match header, set to `0` if creating a new key");
                        try {
                            response.sendError(400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    String ct = request.getHeader("content-type");
                    if (ct == null || !ct.equalsIgnoreCase("application/json")) {
                        Log.e(TAG, "handle: can only set application/json request bodies");
                        try {
                            response.sendError(400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }

                    long version = Long.valueOf(strVersion);

                    JSONObject jVal = null;
                    try {
                        jVal = new JSONObject(rawData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jVal == null) return false;


                    try {
                        byte[] value = jVal.getString("v").getBytes();
                        CompletionObject setObj = store.set(new KVEntity(version, 0, key, value, System.currentTimeMillis(), 0));
                        if (setObj.err != null) {
                            int errCode = transformErrorToResponseCode(setObj.err);
                            try {
                                response.sendError(errCode);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        response.setHeader("ETag", String.valueOf(setObj.version));
                        dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss:SSS Z", Locale.getDefault());
                        date = dateFormat.format(setObj.time);
                        response.setHeader("Last-Modified", date);
                        response.setStatus(204);
                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        response.sendError(500);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                case "DELETE":
                    strVersion = request.getHeader("if-none-match");
                    if (strVersion == null) {
                        Log.e(TAG, "handle: missing If-None-Match header");
                        try {
                            response.sendError(400);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    CompletionObject delObj = null;
                    try {
                        delObj = store.delete(key, Long.parseLong(strVersion));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (delObj == null || delObj.err != null) {
                        int err = 500;
                        if (delObj != null)
                            err = transformErrorToResponseCode(delObj.err);
                        try {
                            response.sendError(err);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        return false;
                    }
                    response.setHeader("ETag", String.valueOf(delObj.version));
                    dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss:SSS Z", Locale.getDefault());
                    date = dateFormat.format(delObj.time);
                    response.setHeader("Last-Modified", date);
                    response.setStatus(204);
                    break;

            }
        }

        return false;
    }

    private int transformErrorToResponseCode(CompletionObject.RemoteKVStoreError err) {
        switch (err) {
            case notFound:
                return 404;
            case conflict:
                return 409;
            default:
                Log.e(TAG, "transformErrorToResponseCode: unexpected error: " + err.name());
                return 500;
        }
    }
}
