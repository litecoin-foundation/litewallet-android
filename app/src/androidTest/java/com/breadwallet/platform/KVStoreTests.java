package com.breadwallet.platform;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.breadwallet.presenter.activities.MainActivity;
import com.platform.APIClient;
import com.platform.interfaces.KVStoreAdaptor;
import com.platform.kvstore.CompletionObject;
import com.platform.kvstore.RemoteKVStore;
import com.platform.kvstore.ReplicatedKVStore;
import com.platform.sqlite.KVEntity;
import com.platform.sqlite.PlatformSqliteHelper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
@LargeTest

public class KVStoreTests {
    public static final String TAG = KVStoreTests.class.getName();

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private KVStoreAdaptor remote;
    private ReplicatedKVStore store;

    public class MockUpAdapter implements KVStoreAdaptor {
        private Map<String, KVEntity> remoteKVs = new HashMap<>();

        @Override
        public CompletionObject ver(String key) {
            KVEntity result = remoteKVs.get(key);
            return result == null ? new CompletionObject(CompletionObject.RemoteKVStoreError.notFound) : new CompletionObject(result.getVersion(), result.getTime(), null);
        }

        @Override
        public CompletionObject put(String key, byte[] value, long version) {
            KVEntity result = remoteKVs.get(key);
            if (result == null) {
                if (version != 0)
                    return new CompletionObject(CompletionObject.RemoteKVStoreError.notFound);
                KVEntity newObj = new KVEntity(1, 1, key, value, System.currentTimeMillis(), 0);
                remoteKVs.put(key, newObj);
                 return new CompletionObject(1, newObj.getTime(), null);
            }
            if (version != result.getRemoteVersion())
                return new CompletionObject(CompletionObject.RemoteKVStoreError.conflict);
            KVEntity newObj = new KVEntity(result.getVersion() + 1, +result.getRemoteVersion() + 1, key, value, System.currentTimeMillis(), 0);
            remoteKVs.put(newObj.getKey(), newObj);
            return new CompletionObject(newObj.getRemoteVersion(), newObj.getTime(), null);
        }

        @Override
        public CompletionObject del(String key, long version) {
            KVEntity result = remoteKVs.get(key);
            if (result == null)
                return new CompletionObject(CompletionObject.RemoteKVStoreError.notFound);
            if (result.getRemoteVersion() != version)
                return new CompletionObject(CompletionObject.RemoteKVStoreError.conflict);
            KVEntity newObj = new KVEntity(result.getVersion() + 1, result.getRemoteVersion() + 1, result.getKey(), result.getValue(), result.getTime(), 1);
            remoteKVs.put(newObj.getKey(), newObj);
            return new CompletionObject(newObj.getRemoteVersion(), newObj.getTime(), null);
        }

        @Override
        public CompletionObject get(String key, long version) {
            KVEntity result = remoteKVs.get(key);
            if (result == null)
                return new CompletionObject(CompletionObject.RemoteKVStoreError.notFound);
            if (version != result.getRemoteVersion())
                return new CompletionObject(CompletionObject.RemoteKVStoreError.conflict);
            return new CompletionObject(result, result.getDeleted() > 0 ? CompletionObject.RemoteKVStoreError.tombstone : null);
        }

        @Override
        public CompletionObject keys() {
            return new CompletionObject(new ArrayList<>(remoteKVs.values()), null);
        }

        public void putKv(KVEntity kv){
            remoteKVs.put(kv.getKey(), kv);
        }

    }

    @Before
    public void setUp() {
        remote = new MockUpAdapter();
        store = new ReplicatedKVStore(mActivityRule.getActivity(), remote);
        ((MockUpAdapter)remote).putKv(new KVEntity(0, 0, "hello", "hello".getBytes(), System.currentTimeMillis(), 0));
        ((MockUpAdapter)remote).putKv(new KVEntity(0, 0, "removed", "removed".getBytes(), System.currentTimeMillis(), 1));
        for (int i = 0; i < 20; i++) {
            ((MockUpAdapter)remote).putKv(new KVEntity(0, 0, "testkey" + i, ("testkey" + i).getBytes(), System.currentTimeMillis(), 0));
        }
        store.set(remote.keys().kvs);
        List<KVEntity> fetchedKvs = store.getAllKVs();
        int freshSize = fetchedKvs.size();
        Assert.assertEquals(22, remote.keys().kvs.size());
        if (freshSize != 22) {
            Log.e(TAG, "setUp: ");
        }
        Assert.assertEquals(22, freshSize);
    }

    @After
    public void tearDown() {
        mActivityRule.getActivity().deleteDatabase(PlatformSqliteHelper.DATABASE_NAME);
    }

    @Test
    public void testDatabasesAreSynced() {
        Map<String, byte[]> remoteKV = new LinkedHashMap<>();
        List<KVEntity> kvs = remote.keys().kvs;
        for (KVEntity kv : kvs) {
            if (kv.getDeleted() == 0)
                remoteKV.put(kv.getKey(), kv.getValue());
        }
        Assert.assertEquals(remoteKV.size(), 21);

        List<KVEntity> allLocalKeys = store.getAllKVs();
        Assert.assertEquals(allLocalKeys.size(), 22);
        Map<String, byte[]> localKV = new LinkedHashMap<>();
        for (KVEntity kv : allLocalKeys) {
            if (kv.getDeleted() == 0) {
                CompletionObject object = store.get(kv.getKey(), kv.getVersion());
                KVEntity tmpKv = object.kv;
                localKV.put(kv.getKey(), tmpKv.getValue());
            }
        }

        Assert.assertEquals(localKV.size(), 21);

        Iterator it = remoteKV.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            byte[] val = (byte[]) pair.getValue();
            byte[] valToAssert = localKV.get((String) pair.getKey());

            Assert.assertArrayEquals(val, valToAssert);
        }

        it = localKV.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            byte[] val = (byte[]) pair.getValue();
            byte[] valToAssert = remoteKV.get((String) pair.getKey());
            Assert.assertArrayEquals(val, valToAssert);
        }

    }

    @Test
    public void testSetLocal() {
        store.set(0, 1, "Key1", "Key1".getBytes(), System.currentTimeMillis(), 0);
        store.set(0, 1, "Key1", "Key1".getBytes(), System.currentTimeMillis(), 0);
        store.set(new KVEntity(0, 1, "Key2", "Key2".getBytes(), System.currentTimeMillis(), 2));
        store.set(new KVEntity[]{
                new KVEntity(0, 4, "Key3", "Key3".getBytes(), System.currentTimeMillis(), 2),
                new KVEntity(0, 2, "Key4", "Key4".getBytes(), System.currentTimeMillis(), 0)});
        store.set(Arrays.asList(new KVEntity[]{
                new KVEntity(0, 4, "Key5", "Key5".getBytes(), System.currentTimeMillis(), 1),
                new KVEntity(0, 5, "Key6", "Key6".getBytes(), System.currentTimeMillis(), 5)}));
        Assert.assertEquals(28, store.getAllKVs().size());
    }

    @Test
    public void testSetLocalIncrementsVersion() {
        store.deleteAllKVs();
        CompletionObject obj = store.set(0, 0, "Key1", "Key1".getBytes(), System.currentTimeMillis(), 0);
        List<KVEntity> test = store.getAllKVs();
        Assert.assertEquals(test.size(), 1);
        Assert.assertNull(obj.err);
        Assert.assertEquals(1, store.localVersion("Key1"));
    }

    @Test
    public void testSetThenGet() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        CompletionObject setObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);
        Assert.assertNull(setObj.err);
        long v1 = setObj.version;
        long t1 = setObj.time;
        CompletionObject obj = store.get("hello", 0);
        KVEntity kvNoVersion = obj.kv;
        obj = store.get("hello", 1);
        KVEntity kvWithVersion = obj.kv;
        Assert.assertArrayEquals(value, kvNoVersion.getValue());
        Assert.assertEquals(v1, kvNoVersion.getVersion());
        Assert.assertEquals(t1, kvNoVersion.getTime(), 0.001);

        Assert.assertArrayEquals(value, kvWithVersion.getValue());
        Assert.assertEquals(v1, kvWithVersion.getVersion());
        Assert.assertEquals(t1, kvWithVersion.getTime(), 0.001);

    }

    @Test
    public void testSetThenSetIncrementsVersion() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        byte[] value2 = "hello2".getBytes();
        CompletionObject setObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);
        CompletionObject setObj2 = store.set(setObj.version, 0, "hello", value2, System.currentTimeMillis(), 0);
        Assert.assertEquals(setObj2.version, setObj.version + 1);
    }

    @Test
    public void testSetThenDel() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        CompletionObject setObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);
        CompletionObject delObj = store.delete("hello", setObj.version);
        Assert.assertNull(setObj.err);
        Assert.assertNull(delObj.err);

        Assert.assertEquals(delObj.version, setObj.version + 1);
    }

    @Test
    public void testSetThenDelThenGet() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        CompletionObject setObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);
        CompletionObject delObj = store.delete("hello", setObj.version);
        Assert.assertNull(setObj.err);
        Assert.assertNull(delObj.err);

        CompletionObject object = store.get("hello", 0);
        KVEntity getKv = object.kv;

        Assert.assertEquals(delObj.version, setObj.version + 1);
        Assert.assertEquals(getKv.getVersion(), setObj.version + 1);
    }

    @Test
    public void testSetWithIncorrectFirstVersionFails() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        CompletionObject setObj = store.set(1, 0, "hello", value, System.currentTimeMillis(), 0);
        Assert.assertNotNull(setObj.err);
    }

    @Test
    public void testSetWithStaleVersionFails() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        CompletionObject setObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);
        CompletionObject setStaleObj = store.set(0, 0, "hello", value, System.currentTimeMillis(), 0);

        Assert.assertNull(setObj.err);
        Assert.assertNotNull(setStaleObj.err);
    }

    @Test
    public void testGetNonExistentKeyFails() {
        store.deleteAllKVs();
        CompletionObject object = store.get("hello", 0);
        KVEntity getKv = object.kv;

        Assert.assertNull(getKv);
    }

    @Test
    public void testGetNonExistentKeyVersionFails() {
        store.deleteAllKVs();
        CompletionObject object = store.get("hello", 1);
        KVEntity getKv = object.kv;

        Assert.assertNull(getKv);
    }

    @Test
    public void testGetAllKeys() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        long time = System.currentTimeMillis();
        CompletionObject setObj = store.set(0, 0, "hello", value, time, 0);
        List<KVEntity> list = store.getAllKVs();

        Assert.assertNotNull(list);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("hello", list.get(0).getKey());
        Assert.assertEquals(setObj.version, list.get(0).getVersion());
        Assert.assertEquals(setObj.time, list.get(0).getTime(), 0.001);
        Assert.assertEquals(0, list.get(0).getRemoteVersion());
        Assert.assertEquals(0, list.get(0).getDeleted());
    }

    @Test
    public void testSetRemoteVersion() {
        store.deleteAllKVs();
        byte[] value = "hello".getBytes();
        long time = System.currentTimeMillis();
        CompletionObject setObj = store.set(0, 0, "hello", value, time, 0);
        CompletionObject setRemoteVersionObj = store.setRemoteVersion("hello", setObj.version, 1);
        Assert.assertEquals(setRemoteVersionObj.version, setObj.version + 1);
        long remoteVer = store.remoteVersion("hello");
        Assert.assertEquals(1, remoteVer);

    }

    @Test
    public void testBasicSyncGetAllObjects() {
//        CompletionObject putObj2 = remote.put("veryverybigkey", "veryverybigkey".getBytes(),1);

        CompletionObject obj = remote.keys();

        boolean success = store.syncAllKeys();
        Assert.assertEquals(true, success);

        List<KVEntity> localKeys = store.getAllKVs();
        obj = remote.keys();
        List<KVEntity> keys = obj.kvs;
        Assert.assertEquals(keys.size() - 1, localKeys.size());
        testDatabasesAreSynced();
    }

    @Test
    public void testSyncTenTimes() {
        int n = 10;
    }
}
