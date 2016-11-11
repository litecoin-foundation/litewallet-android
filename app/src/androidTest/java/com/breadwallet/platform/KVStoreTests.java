package com.breadwallet.platform;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.breadwallet.presenter.activities.MainActivity;
import com.platform.sqlite.KVEntity;
import com.platform.sqlite.PlatformSqliteHelper;
import com.platform.sqlite.PlatformSqliteManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 9/30/16.
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

@RunWith(AndroidJUnit4.class)
public class KVStoreTests {
    public static final String TAG = KVStoreTests.class.getName();
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    PlatformSqliteManager sqliteManager;
    List<KVEntity> kvs;

    private static final String KEY = "S6c56bnXQiBjk9mqSYE7ykVQ7NzrRy";

    @Before
    public void setUp() {
        sqliteManager = PlatformSqliteManager.getInstance(mActivityRule.getActivity());
        kvs = new LinkedList<>();
        kvs.add(new KVEntity(1, 2, "hello", "hello".getBytes(), System.currentTimeMillis(), 0));
        kvs.add(new KVEntity(2, 2, "removed", "removed".getBytes(), System.currentTimeMillis(), 1));
        for (int i = 0; i < 20; i++) {
            kvs.add(new KVEntity(1, 1, "testkey" + i, ("testkey" + i).getBytes(), System.currentTimeMillis(), 0));
        }
        sqliteManager.setKv(kvs);
        Log.e(TAG, "setUp: size: " + sqliteManager.getKVs().size());
    }

    @After
    public void tearDown() {
        mActivityRule.getActivity().deleteDatabase(PlatformSqliteHelper.DATABASE_NAME);
        Log.e(TAG, "tearDown: " + sqliteManager.getKVs().size());
        sqliteManager = null;
        kvs.clear();
    }

    @Test
    public void testDatabasesAreSynced() {
        Map<String, byte[]> remoteKV = new LinkedHashMap<>();

        for (KVEntity kv : kvs) {
            if (kv.getDeleted() == 0)
                remoteKV.put(kv.getKey(), kv.getValue());
        }

        List<KVEntity> allLocalKeys = sqliteManager.getKVs();
        Map<String, byte[]> localKV = new LinkedHashMap<>();
        for (KVEntity kv : allLocalKeys) {
            if (kv.getDeleted() == 0) {
                localKV.put(kv.getKey(), sqliteManager.getKv(kv.getKey(), kv.getVersion()).getValue());
            }
        }

        Iterator it = remoteKV.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            byte[] val = (byte[]) pair.getValue();
            byte[] valToAssert = localKV.get(pair.getKey());

            Assert.assertArrayEquals(val, valToAssert);
        }

        it = localKV.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            byte[] val = (byte[]) pair.getValue();
            byte[] valToAssert = remoteKV.get(pair.getKey());
            Assert.assertArrayEquals(val, valToAssert);
        }

    }

    @Test
    public void testSetLocal() {
        sqliteManager.deleteKVs();
        sqliteManager.setKv(0, 1, "Key1", "Key1".getBytes(), System.currentTimeMillis(), 0);
        sqliteManager.setKv(new KVEntity(2, 1, "Key2", "Key2".getBytes(), System.currentTimeMillis(), 2));
        sqliteManager.setKv(new KVEntity[]{
                new KVEntity(2, 4, "Key3", "Key3".getBytes(), System.currentTimeMillis(), 2),
                new KVEntity(2, 2, "Key4", "Key4".getBytes(), System.currentTimeMillis(), 0)});
        sqliteManager.setKv(Arrays.asList(new KVEntity[]{
                new KVEntity(1, 4, "Key5", "Key5".getBytes(), System.currentTimeMillis(), 1),
                new KVEntity(0, 5, "Key6", "Key6".getBytes(), System.currentTimeMillis(), 5)}));
        Assert.assertEquals(6, sqliteManager.getKVs().size());
    }

}
