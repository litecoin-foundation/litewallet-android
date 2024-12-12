package com.litewallet.tools.security
//
//import com.breadwallet.presenter.activities.intro.WriteDownActivity
//import com.breadwallet.tools.threads.BRExecutor
//import com.breadwallet.tools.util.BRConstants
//import com.litewallet.example.DependencyOne
//import com.litewallet.example.DependencyTwo
//import com.litewallet.example.SystemUnderTest
//import com.platform.entities.TxMetaData
//import com.platform.interfaces.KVStoreAdaptor
//import com.platform.kvstore.CompletionObject
//import com.platform.kvstore.ReplicatedKVStore
//import com.platform.sqlite.KVItem
//import com.platform.sqlite.PlatformSqliteHelper
//import com.platform.tools.KVStoreManager
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.ClassRule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.Arrays
//import java.util.concurrent.atomic.AtomicInteger
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
//
class BRKeyStoreTest {

}

//  from com.litewallet.security;
//package com.litewallet.security;
//
//import android.security.keystore.UserNotAuthenticatedException;
//import androidx.test.rule.ActivityTestRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.breadwallet.presenter.activities.settings.TestActivity;
//import com.breadwallet.tools.security.BRKeyStore;
//import com.breadwallet.tools.threads.BRExecutor;
//
//import org.junit.Assert;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.File;
//
//import static com.breadwallet.tools.security.BRKeyStore.aliasObjectMap;
//
//@RunWith(AndroidJUnit4.class)
//        public class KeyStoreTests {
//    public static final String TAG = KeyStoreTests.class.getName();
//
//    @Rule
//    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);
//
//    @Test
//    public void setGetPhrase() {
//        //set get phrase
//        byte[] phrase = "axis husband project any sea patch drip tip spirit tide bring belt".getBytes();
//        try {
//            BRKeyStore.putPhrase(phrase, mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        assertFilesExist(BRKeyStore.PHRASE_ALIAS);
//
//        byte[] freshGet = new byte[0];
//        try {
//            freshGet = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertArrayEquals(freshGet, phrase);
//
//        //set get Japaneese phrase
//        byte[] japPhrase = "こせき　ぎじにってい　けっこん　せつぞく　うんどう　ふこう　にっすう　こせい　きさま　なまみ　たきび　はかい".getBytes();
//        try {
//            BRKeyStore.putPhrase(japPhrase, mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        assertFilesExist(BRKeyStore.PHRASE_ALIAS);
//        byte[] freshJapGet = new byte[0];
//        try {
//            freshJapGet = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertArrayEquals(freshJapGet, japPhrase);
//
//    }
//
//    @Test
//    public void setGetCanary() {
//        String canary = "canary";
//        try {
//            BRKeyStore.putCanary(canary, mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        assertFilesExist(BRKeyStore.CANARY_ALIAS);
//        String freshGet = "";
//        try {
//            freshGet = BRKeyStore.getCanary(mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(freshGet, canary);
//    }
//
//    @Test
//    public void setGetMultiple() {
//        final String canary = "canary";
//        for (int i = 0; i < 100; i++) {
//            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        boolean b = BRKeyStore.putCanary(canary, mActivityRule.getActivity(), 0);
//                        Assert.assertTrue(b);
//                    } catch (UserNotAuthenticatedException e) {
//                        e.printStackTrace();
//                        Assert.fail();
//                    }
//                    try {
//                        String b = BRKeyStore.getCanary(mActivityRule.getActivity(), 0);
//                        Assert.assertEquals(b, canary);
//                    } catch (UserNotAuthenticatedException e) {
//                        e.printStackTrace();
//                        Assert.fail();
//                    }
//                }
//            });
//
//        }
//
//        assertFilesExist(BRKeyStore.CANARY_ALIAS);
//
//
//        for (int i = 0; i < 100; i++) {
//            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
//                @Override
//                public void run() {
//                    String freshGet = "";
//                    try {
//                        freshGet = BRKeyStore.getCanary(mActivityRule.getActivity(), 0);
//                    } catch (UserNotAuthenticatedException e) {
//                        e.printStackTrace();
//                    }
//                    Assert.assertEquals(freshGet, canary);
//                }
//            });
//
//        }
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @Test
//    public void setGetMasterPubKey() {
//        byte[] pubKey = "26wZYDdvpmCrYZeUcxgqd1KquN4o6wXwLomBW5SjnwUqG".getBytes();
//        BRKeyStore.putMasterPublicKey(pubKey, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PUB_KEY_ALIAS);
//        byte[] freshGet;
//        freshGet = BRKeyStore.getMasterPublicKey(mActivityRule.getActivity());
//        Assert.assertArrayEquals(freshGet, freshGet);
//    }
//
//
//    @Test
//    public void setGetAuthKey() {
//        byte[] authKey = "26wZYDdvpmCrYZeUcxgqd1KquN4o6wXwLomBW5SjnwUqG".getBytes();
//        BRKeyStore.putAuthKey(authKey, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.AUTH_KEY_ALIAS);
//        byte[] freshGet;
//        freshGet = BRKeyStore.getAuthKey(mActivityRule.getActivity());
//        Assert.assertArrayEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetWalletCreationTime() {
//        int time = 1479686841;
//        BRKeyStore.putWalletCreationTime(time, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.WALLET_CREATION_TIME_ALIAS);
//        int freshGet;
//        freshGet = BRKeyStore.getWalletCreationTime(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetPassCode() {
//        String passCode = "0124";
//        BRKeyStore.putPinCode(passCode, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PASS_CODE_ALIAS);
//        String freshGet;
//        freshGet = BRKeyStore.getPinCode(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//
//        passCode = "0000";
//        BRKeyStore.putPinCode(passCode, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PASS_CODE_ALIAS);
//        freshGet = BRKeyStore.getPinCode(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//
//        passCode = "9999";
//        BRKeyStore.putPinCode(passCode, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PASS_CODE_ALIAS);
//        freshGet = BRKeyStore.getPinCode(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//
//        passCode = "9876";
//        BRKeyStore.putPinCode(passCode, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PASS_CODE_ALIAS);
//        freshGet = BRKeyStore.getPinCode(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetFailCount() {
//        int failCount = 2;
//        BRKeyStore.putFailCount(failCount, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.FAIL_COUNT_ALIAS);
//        int freshGet;
//        freshGet = BRKeyStore.getFailCount(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetSpendLimit() {
//        long spendLimit = 100000;
//        BRKeyStore.putSpendLimit(spendLimit, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.SPEND_LIMIT_ALIAS);
//        long freshGet;
//        freshGet = BRKeyStore.getSpendLimit(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetSFailTimeStamp() {
//        long failTime = 1479686841;
//        BRKeyStore.putFailTimeStamp(failTime, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.FAIL_TIMESTAMP_ALIAS);
//        long freshGet;
//        freshGet = BRKeyStore.getFailTimeStamp(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void setGetLastPasscodeUsedTime() {
//        long time = 1479686841;
//        BRKeyStore.putLastPinUsedTime(time, mActivityRule.getActivity());
//        assertFilesExist(BRKeyStore.PASS_TIME_ALIAS);
//        long freshGet;
//        freshGet = BRKeyStore.getLastPinUsedTime(mActivityRule.getActivity());
//        Assert.assertEquals(freshGet, freshGet);
//    }
//
//    @Test
//    public void testClearKeyStore() {
//        try {
//            BRKeyStore.putPhrase("axis husband project any sea patch drip tip spirit tide bring belt".getBytes(), mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            BRKeyStore.putCanary("canary", mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        BRKeyStore.putMasterPublicKey("26wZYDdvpmCrYZeUcxgqd1KquN4o6wXwLomBW5SjnwUqG".getBytes(), mActivityRule.getActivity());
//        BRKeyStore.putAuthKey("26wZYDdvpmCrYZeUcxgqd1KquN4o6wXwLomBW5SjnwUqG".getBytes(), mActivityRule.getActivity());
//        BRKeyStore.putToken("26wZYDdvpmCrYZeUcxgqd1KquN4o6wXwLomBW5SjnwUqG".getBytes(), mActivityRule.getActivity());
//        BRKeyStore.putWalletCreationTime(1479686841, mActivityRule.getActivity());
//        BRKeyStore.putPinCode("0123", mActivityRule.getActivity());
//        BRKeyStore.putFailCount(3, mActivityRule.getActivity());
//        BRKeyStore.putFailTimeStamp(1479686841, mActivityRule.getActivity());
//        BRKeyStore.putSpendLimit(10000000, mActivityRule.getActivity());
//        BRKeyStore.putLastPinUsedTime(1479686841, mActivityRule.getActivity());
//        BRKeyStore.putTotalLimit(1479686841, mActivityRule.getActivity());
//
//        for (String a : aliasObjectMap.keySet()) {
//            assertFilesExist(a);
//        }
//
//        BRKeyStore.resetWalletKeyStore(mActivityRule.getActivity());
//
//        for (String a : aliasObjectMap.keySet()) {
//            assertFilesDontExist(a);
//        }
//
//
//        byte[] phrase = "some".getBytes();
//        try {
//            phrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//        }
//
//        String canary = "some";
//
//        try {
//            canary = BRKeyStore.getCanary(mActivityRule.getActivity(), 0);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//        }
//
//        Assert.assertNull(phrase);
//        Assert.assertEquals(null, canary);
//        Assert.assertEquals(null, BRKeyStore.getMasterPublicKey(mActivityRule.getActivity()));
//        Assert.assertEquals(null, BRKeyStore.getAuthKey(mActivityRule.getActivity()));
//        Assert.assertEquals(null, BRKeyStore.getToken(mActivityRule.getActivity()));
//        Assert.assertEquals(0, BRKeyStore.getWalletCreationTime(mActivityRule.getActivity()));
//        Assert.assertEquals("", BRKeyStore.getPinCode(mActivityRule.getActivity()));
//        Assert.assertEquals(0, BRKeyStore.getFailCount(mActivityRule.getActivity()));
//        Assert.assertEquals(0, BRKeyStore.getFailTimeStamp(mActivityRule.getActivity()));
//        Assert.assertEquals(0, BRKeyStore.getSpendLimit(mActivityRule.getActivity()));
//        Assert.assertEquals(0, BRKeyStore.getLastPinUsedTime(mActivityRule.getActivity()));
//
//    }
//
//    @Test
//    public void testKeyStoreAuthTime() {
//        Assert.assertEquals(BRKeyStore.AUTH_DURATION_SEC, 300);
//    }
//
//    @Test
//    public void testKeyStoreAliasMap() {
//        Assert.assertNotNull(aliasObjectMap);
//        Assert.assertEquals(aliasObjectMap.size(), 12);
//    }
//
//    public void assertFilesExist(String alias) {
//        Assert.assertTrue(new File(BRKeyStore.getFilePath(aliasObjectMap.get(alias).datafileName, mActivityRule.getActivity())).exists());
//        Assert.assertTrue(new File(BRKeyStore.getFilePath(aliasObjectMap.get(alias).ivFileName, mActivityRule.getActivity())).exists());
//    }
//
//    public void assertFilesDontExist(String alias) {
//        Assert.assertFalse(new File(BRKeyStore.getFilePath(aliasObjectMap.get(alias).datafileName, mActivityRule.getActivity())).exists());
//        Assert.assertFalse(new File(BRKeyStore.getFilePath(aliasObjectMap.get(alias).ivFileName, mActivityRule.getActivity())).exists());
//    }
//
//}


//TODO: Transcode and add Kotlin tests.  Copied from legacy Java BRKeyStoreTest
////@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
////@LargeTest
//fun `add invoked with valid mocked values, should return correct value as expected`() {
//    val dependencyOne = mockk<DependencyOne>()
//    val dependencyTwo = mockk<DependencyTwo>()
//
//    every { dependencyOne.value } returns 10
//    every { dependencyTwo.value } returns 3
//
//    val system = SystemUnderTest(dependencyOne, dependencyTwo)
//
//    val expected = 13
//    val result = system.add()
//    assertEquals(expected, result)
//    verify {
//        dependencyOne.value
//        dependencyTwo.value
//    }
//}
//
//
//    class MockUpAdapter : KVStoreAdaptor {
//        var remoteKVs: MutableMap<String, KVItem> = HashMap()
//
//        override fun ver(key: String): CompletionObject {
//            val result = remoteKVs[key]
//            return if (result == null) CompletionObject(CompletionObject.RemoteKVStoreError.notFound) else CompletionObject(
//                result.version,
//                result.time,
//                if (result.deleted == 0) null else CompletionObject.RemoteKVStoreError.tombstone
//            )
//        }
//
//        override fun put(key: String, value: ByteArray, version: Long): CompletionObject {
//            val result = remoteKVs[key]
//            if (result == null) {
//                if (version != 1L) return CompletionObject(CompletionObject.RemoteKVStoreError.notFound)
//                val newObj = KVItem(1, -1, key, value, System.currentTimeMillis(), 0)
//                remoteKVs[key] = newObj
//                return CompletionObject(1, newObj.time, null)
//            }
//            if (version != result.version) return CompletionObject(CompletionObject.RemoteKVStoreError.conflict)
//            val newObj = KVItem(result.version + 1, -1, key, value, System.currentTimeMillis(), 0)
//            remoteKVs[newObj.key] = newObj
//            return CompletionObject(newObj.version, newObj.time, null)
//        }
//
//        override fun del(key: String, version: Long): CompletionObject {
//            val result = remoteKVs[key]
//                ?: return CompletionObject(CompletionObject.RemoteKVStoreError.notFound)
//            if (result.version != version) return CompletionObject(CompletionObject.RemoteKVStoreError.conflict)
//            val newObj = KVItem(result.version + 1, -1, result.key, result.value, result.time, 1)
//            remoteKVs[newObj.key] = newObj
//            return CompletionObject(newObj.version, newObj.time, null)
//        }
//
//        override fun get(key: String, version: Long): CompletionObject {
//            val result = remoteKVs[key]
//                ?: return CompletionObject(CompletionObject.RemoteKVStoreError.notFound)
//            if (version != result.version) return CompletionObject(
//                0,
//                System.currentTimeMillis(),
//                CompletionObject.RemoteKVStoreError.conflict
//            )
//            return CompletionObject(
//                result.version,
//                result.time,
//                result.value,
//                if (result.deleted == 0) null else CompletionObject.RemoteKVStoreError.tombstone
//            )
//        }
//
//        override fun keys(): CompletionObject {
//            val result: MutableList<KVItem> = ArrayList()
//            for (kv in remoteKVs.values) {
//                if (kv.deleted != 0) kv.err = CompletionObject.RemoteKVStoreError.tombstone
//                result.add(kv)
//            }
//            return CompletionObject(result)
//        }
//
//        fun putKv(kv: KVItem) {
//            remoteKVs[kv.key] = kv
//        }
//    }
//
//    @Before
//    fun setUp() {
//        BRConstants.PLATFORM_ON = false
//        (remote as MockUpAdapter).remoteKVs.clear()
//        remote.putKv(
//            KVItem(
//                1,
//                1,
//                "hello",
//                ReplicatedKVStore.encrypt("hello".toByteArray(), mActivityRule.getActivity()),
//                System.currentTimeMillis(),
//                0
//            )
//        )
//        remote.putKv(
//            KVItem(
//                1,
//                1,
//                "removed",
//                ReplicatedKVStore.encrypt("removed".toByteArray(), mActivityRule.getActivity()),
//                System.currentTimeMillis(),
//                1
//            )
//        )
//        for (i in 0..19) {
//            remote.putKv(
//                KVItem(
//                    1,
//                    1,
//                    "testkey$i",
//                    ReplicatedKVStore.encrypt(
//                        ("testkey$i").toByteArray(),
//                        mActivityRule.getActivity()
//                    ),
//                    System.currentTimeMillis(),
//                    0
//                )
//            )
//        }
//
//        store = ReplicatedKVStore.getInstance(mActivityRule.getActivity(), remote)
//        store.deleteAllKVs()
//        Assert.assertEquals(22, remote.remoteKVs.size.toLong())
//    }
//
//    @After
//    fun tearDown() {
//        store!!.deleteAllKVs()
//        mActivityRule.getActivity().deleteDatabase(PlatformSqliteHelper.DATABASE_NAME)
//        (remote as MockUpAdapter).remoteKVs.clear()
//    }
//
//    fun assertDatabasesAreSynced() {
//        val remoteKV: MutableMap<String, ByteArray> = LinkedHashMap()
//        val kvs: List<KVItem> = ArrayList((remote as MockUpAdapter).remoteKVs.values)
//        for (kv in kvs) {
//            if (kv.deleted == 0) remoteKV[kv.key] = kv.value
//        }
//
//        val allLocalKeys = store!!.rawKVs
//        val localKV: MutableMap<String, ByteArray> = LinkedHashMap()
//
//        for (kv in allLocalKeys) {
//            if (kv.deleted == 0) {
//                val `object` = store!![kv.key, kv.version]
//
//                //                KVItem tmpKv = object.kv;
//                localKV[kv.key] = `object`.kv.value
//            }
//        }
//
//        Assert.assertEquals(remoteKV.size.toLong(), localKV.size.toLong())
//
//        var it: Iterator<*> = remoteKV.entries.iterator()
//        while (it.hasNext()) {
//            val pair = it.next() as Map.Entry<*, *>
//            val `val` =
//                ReplicatedKVStore.decrypt(pair.value as ByteArray?, mActivityRule.getActivity())
//            val valToAssert = localKV[pair.key as String]
//            val valStr = String(`val`!!)
//            val valToAssertStr = String(valToAssert!!)
//            Assert.assertArrayEquals(`val`, valToAssert)
//        }
//
//        it = localKV.entries.iterator()
//        while (it.hasNext()) {
//            val pair = it.next() as Map.Entry<*, *>
//            val `val` = pair.value as ByteArray
//            val valToAssert =
//                ReplicatedKVStore.decrypt(remoteKV[pair.key as String], mActivityRule.getActivity())
//            Assert.assertArrayEquals(`val`, valToAssert)
//        }
//    }
//
//
//    @Test
//    fun testSetLocal() {
//        val obj = store!!.set(0, 1, "Key1", "Key1".toByteArray(), System.currentTimeMillis(), 0)
//        Assert.assertNull(obj.err)
//        store!![0, 1, "Key1", "Key1".toByteArray(), System.currentTimeMillis()] =
//            0
//        store!!.set(KVItem(0, 1, "Key2", "Key2".toByteArray(), System.currentTimeMillis(), 2))
//        store!!.set(
//            arrayOf(
//                KVItem(0, 4, "Key3", "Key3".toByteArray(), System.currentTimeMillis(), 2),
//                KVItem(0, 2, "Key4", "Key4".toByteArray(), System.currentTimeMillis(), 0)
//            )
//        )
//        store!!.set(
//            Arrays.asList(
//                *arrayOf(
//                    KVItem(0, 4, "Key5", "Key5".toByteArray(), System.currentTimeMillis(), 1),
//                    KVItem(0, 5, "Key6", "Key6".toByteArray(), System.currentTimeMillis(), 5)
//                )
//            )
//        )
//        Assert.assertEquals(6, store!!.rawKVs.size.toLong())
//    }
//
//    @Test
//    fun testDeleteAll() {
//        val obj = store!!.set(0, 1, "Key1", "Key1".toByteArray(), System.currentTimeMillis(), 0)
//        store!![0, 1, "Key1", "Key1".toByteArray(), System.currentTimeMillis()] =
//            0
//        store!!.set(KVItem(0, 1, "Key2", "Key2".toByteArray(), System.currentTimeMillis(), 2))
//        store!!.set(
//            arrayOf(
//                KVItem(0, 4, "Key3", "Key3".toByteArray(), System.currentTimeMillis(), 2),
//                KVItem(0, 2, "Key4", "Key4".toByteArray(), System.currentTimeMillis(), 0)
//            )
//        )
//        store!!.set(
//            Arrays.asList(
//                *arrayOf(
//                    KVItem(0, 4, "Key5", "Key5".toByteArray(), System.currentTimeMillis(), 1),
//                    KVItem(0, 5, "Key6", "Key6".toByteArray(), System.currentTimeMillis(), 5)
//                )
//            )
//        )
//        var kvs = store!!.rawKVs
//        Assert.assertEquals(6, kvs.size.toLong())
//        store!!.deleteAllKVs()
//        kvs = store!!.rawKVs
//        Assert.assertEquals(0, kvs.size.toLong())
//    }
//
//    @Test
//    fun testSetLocalIncrementsVersion() {
//        store!!.deleteAllKVs()
//        val obj = store!!.set(0, 0, "Key1", "Key1".toByteArray(), System.currentTimeMillis(), 0)
//        Assert.assertNull(obj.err)
//        val test = store!!.rawKVs
//        Assert.assertEquals(1, test.size.toLong())
//        Assert.assertEquals(1, store!!.localVersion("Key1").version)
//    }
//
//    @Test
//    fun testMultithreadedInserts() {
//        val count = AtomicInteger()
//        for (i in 0..999) {
//            val finalI = i
//            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
//                val obj = store!!.set(
//                    0, 0,
//                    "Key$finalI", "Key1".toByteArray(), System.currentTimeMillis(), 0
//                )
//                Assert.assertNull(obj.err)
//                Assert.assertEquals(obj.version, 1)
//                count.incrementAndGet()
//                val remObj = store!!.delete(
//                    "Key$finalI", store!!.localVersion(
//                        "Key$finalI"
//                    ).version
//                )
//                Assert.assertNull(remObj.err)
//                count.decrementAndGet()
//            }
//        }
//        try {
//            Thread.sleep(10000)
//            Assert.assertEquals(count.get().toLong(), 0)
//            val items = store!!.rawKVs
//            Assert.assertEquals(items.size.toLong(), 1000)
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//    }
//
//    @Test
//    fun testSetThenGet() {
//        val value = "hello".toByteArray()
//        val setObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        Assert.assertNull(setObj.err)
//        val v1 = setObj.version
//        val t1 = setObj.time
//        var obj = store!!["hello", 0]
//        val kvNoVersion = obj.kv
//        obj = store!!["hello", 1]
//        val kvWithVersion = obj.kv
//        Assert.assertArrayEquals(value, kvNoVersion.value)
//        Assert.assertEquals(v1, kvNoVersion.version)
//        Assert.assertEquals(t1.toDouble(), kvNoVersion.time.toDouble(), 0.001)
//
//        Assert.assertNotNull(kvWithVersion)
//        Assert.assertArrayEquals(value, kvWithVersion.value)
//        Assert.assertEquals(v1, kvWithVersion.version)
//        Assert.assertEquals(t1.toDouble(), kvWithVersion.time.toDouble(), 0.001)
//    }
//
//    @Test
//    fun testSetThenSetIncrementsVersion() {
//        val value = "hello".toByteArray()
//        val value2 = "hello2".toByteArray()
//        val setObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        val setObj2 = store!!.set(setObj.version, 0, "hello", value2, System.currentTimeMillis(), 0)
//        Assert.assertEquals(setObj2.version, setObj.version + 1)
//    }
//
//    @Test
//    fun testSetThenDel() {
//        val value = "hello".toByteArray()
//        val setObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        val delObj = store!!.delete("hello", setObj.version)
//        Assert.assertNull(setObj.err)
//        Assert.assertNull(delObj.err)
//
//        Assert.assertEquals(delObj.version, setObj.version + 1)
//    }
//
//    @Test
//    fun testSetThenDelThenGet() {
//        val value = "hello".toByteArray()
//        val setObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        val delObj = store!!.delete("hello", setObj.version)
//        Assert.assertNull(setObj.err)
//        Assert.assertNull(delObj.err)
//
//        val `object` = store!!["hello", 0]
//        val getKv = `object`.kv
//
//        Assert.assertEquals(delObj.version, setObj.version + 1)
//        Assert.assertEquals(getKv.version, setObj.version + 1)
//    }
//
//    @Test
//    fun testSetWithIncorrectFirstVersionFails() {
//        val value = "hello".toByteArray()
//        val setObj = store!!.set(1, 0, "hello", value, System.currentTimeMillis(), 0)
//        Assert.assertNotNull(setObj.err)
//    }
//
//    @Test
//    fun testSetWithStaleVersionFails() {
//        val value = "hello".toByteArray()
//        val setObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        val setStaleObj = store!!.set(0, 0, "hello", value, System.currentTimeMillis(), 0)
//        Assert.assertNull(setObj.err)
//        Assert.assertEquals(CompletionObject.RemoteKVStoreError.conflict, setStaleObj.err)
//    }
//
//    @Test
//    fun testGetNonExistentKeyFails() {
//        val `object` = store!!["hello", 0]
//        val getKv = `object`.kv
//        Assert.assertNull(getKv)
//    }
//
//    @Test
//    fun testGetNonExistentKeyVersionFails() {
//        val `object` = store!!["hello", 1]
//        val getKv = `object`.kv
//
//        Assert.assertNull(getKv)
//    }
//
//    @Test
//    fun testGetAllKeys() {
//        val value = "hello".toByteArray()
//        val time = System.currentTimeMillis()
//        val setObj = store!!.set(0, 0, "hello", value, time, 0)
//        val list = store!!.rawKVs
//
//        Assert.assertNotNull(list)
//        Assert.assertEquals(1, list.size.toLong())
//        Assert.assertEquals("hello", list[0].key)
//        Assert.assertEquals(setObj.version, list[0].version)
//        Assert.assertEquals(setObj.time.toDouble(), list[0].time.toDouble(), 0.001)
//        Assert.assertEquals(0, list[0].remoteVersion)
//        Assert.assertEquals(0, list[0].deleted.toLong())
//    }
//
//    @Test
//    fun testSetRemoteVersion() {
//        val value = "hello".toByteArray()
//        val time = System.currentTimeMillis()
//        val setObj = store!!.set(0, 0, "hello", value, time, 0)
//        val setRemoteVersionObj = store!!.setRemoteVersion("hello", setObj.version, 1)
//        Assert.assertEquals(setRemoteVersionObj.version, setObj.version + 1)
//        val remoteVer = store!!.remoteVersion("hello")
//        Assert.assertEquals(1, remoteVer)
//    }
//
//    @Test
//    fun testBasicSyncGetAllObjects() {
//        val success = store!!.syncAllKeys()
//        Assert.assertEquals(true, success)
//
//        val localKeys = store!!.rawKVs
//        Assert.assertEquals(
//            ((remote as MockUpAdapter).remoteKVs.size - 1).toLong(),
//            localKeys.size.toLong()
//        )
//        assertDatabasesAreSynced()
//    }
//
//    @Test
//    fun testSyncTenTimes() {
//        var n = 10
//        while (n > 0) {
//            val success = store!!.syncAllKeys()
//            Assert.assertTrue(success)
//            n--
//        }
//        assertDatabasesAreSynced()
//    }
//
//    @Test
//    fun testSyncAddsLocalKeysToRemote() {
//        val setObj =
//            store!!.set(KVItem(0, -1, "derp", "derp".toByteArray(), System.currentTimeMillis(), 0))
//        Assert.assertNull(setObj.err)
//        val success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        val obj = remote["derp", 1]
//        Assert.assertArrayEquals(
//            ReplicatedKVStore.decrypt(obj.value, mActivityRule.getActivity()),
//            "derp".toByteArray()
//        )
//    }
//
//    @Test
//    fun testSyncSavesRemoteVersion() {
//        val success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        val ver = store!!.remoteVersion("hello")
//        Assert.assertEquals((remote as MockUpAdapter).remoteKVs["hello"]!!.version, 1)
//        Assert.assertEquals(remote.remoteKVs["hello"]!!.version, ver)
//        assertDatabasesAreSynced()
//    }
//
//    @Test
//    fun testSyncPreventsAnotherConcurrentSync() {
////        boolean success = store.syncAllKeys();
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////                boolean success = store.syncAllKeys();
////                Assert.assertTrue(success);
////                Assert.assertFalse(success);
////            }
////        }).start();
////        Assert.assertTrue(success);
//    }
//
//    @Test
//    fun testLocalDeleteReplicates() {
//        val setObj = store!!.set(
//            KVItem(
//                0,
//                0,
//                "goodbye_cruel_world",
//                "goodbye_cruel_world".toByteArray(),
//                System.currentTimeMillis(),
//                0
//            )
//        )
//        Assert.assertNull(setObj.err)
//        var success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        val delObj = store!!.delete(
//            "goodbye_cruel_world",
//            store!!.localVersion("goodbye_cruel_world").version
//        )
//        Assert.assertNull(delObj.err)
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        val kv = (remote as MockUpAdapter).remoteKVs["goodbye_cruel_world"]
//        Assert.assertTrue(kv!!.deleted > 0)
//    }
//
//    @Test
//    fun testLocalUpdateReplicates() {
//        var setObj = store!!.set(
//            KVItem(
//                0,
//                -1,
//                "goodbye_cruel_world",
//                "goodbye_cruel_world".toByteArray(),
//                System.currentTimeMillis(),
//                0
//            )
//        )
//        var success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        setObj = store!!.set(
//            KVItem(
//                store!!.localVersion("goodbye_cruel_world").version,
//                -1,
//                "goodbye_cruel_world",
//                "goodbye_cruel_world with some new info".toByteArray(),
//                System.currentTimeMillis(),
//                0
//            )
//        )
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        Assert.assertArrayEquals(
//            "goodbye_cruel_world with some new info".toByteArray(), ReplicatedKVStore.decrypt(
//                remote["goodbye_cruel_world", store!!.remoteVersion("goodbye_cruel_world")].value,
//                mActivityRule.getActivity()
//            )
//        )
//    }
//
//    @Test
//    fun testRemoteDeleteReplicates() {
//        var success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        val kv = (remote as MockUpAdapter).remoteKVs["hello"]
//        remote.remoteKVs["hello"] =
//            KVItem(kv!!.version + 1, -1, kv.key, kv.value, System.currentTimeMillis(), 1)
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//        val getObj = store!!["hello", 0]
//        Assert.assertNull(getObj.err)
//        Assert.assertTrue(getObj.kv.deleted > 0)
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//    }
//
//    @Test
//    fun testRemoteUpdateReplicates() {
//        var success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//
//        val kv = (remote as MockUpAdapter).remoteKVs["hello"]
//        remote.remoteKVs["hello"] =
//            KVItem(
//                kv!!.version + 1,
//                -1,
//                kv.key,
//                ReplicatedKVStore.encrypt("newVal".toByteArray(), mActivityRule.getActivity()),
//                System.currentTimeMillis(),
//                0
//            )
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//
//        val getObj = store!!["hello", 0]
//        Assert.assertNull(getObj.err)
//        Assert.assertArrayEquals(getObj.kv.value, "newVal".toByteArray())
//        success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        assertDatabasesAreSynced()
//    }
//
//    @Test
//    fun testEnableEncryptedReplication() {
//        (remote as MockUpAdapter).remoteKVs.clear()
//        val setObj =
//            store!!.set(KVItem(0, 0, "derp", "derp".toByteArray(), System.currentTimeMillis(), 0))
//        Assert.assertNull(setObj.err)
//        val success = store!!.syncAllKeys()
//        Assert.assertTrue(success)
//        val obj = remote["derp", 1]
//        Assert.assertArrayEquals(
//            ReplicatedKVStore.decrypt(obj.value, mActivityRule.getActivity()),
//            "derp".toByteArray()
//        )
//    }
//
//    @Test
//    fun testGetAllMds() {
//        store!![0, 1, "Key1", "Key1".toByteArray(), System.currentTimeMillis()] =
//            0
//        store!!.set(KVItem(0, 1, "Key2", "Key2".toByteArray(), System.currentTimeMillis(), 2))
//        store!!.set(
//            arrayOf(
//                KVItem(0, 4, "fdf-gsd34534", "second".toByteArray(), System.currentTimeMillis(), 2),
//                KVItem(
//                    0,
//                    2,
//                    "fsdtxn2-fdslkjf34",
//                    "ignore".toByteArray(),
//                    System.currentTimeMillis(),
//                    0
//                )
//            )
//        )
//        store!!.set(
//            Arrays.asList(
//                *arrayOf(
//                    KVItem(0, 4, "Key5", "Key5".toByteArray(), System.currentTimeMillis(), 1),
//                    KVItem(0, 5, "Key6", "Key6".toByteArray(), System.currentTimeMillis(), 5)
//                )
//            )
//        )
//        val kvs = store!!.rawKVs
//        Assert.assertEquals(kvs.size.toLong(), 6)
//
//        val tx = TxMetaData()
//        val theHash = byteArrayOf(3, 5, 64, 2, 4, 5, 63, 7, 0, 56, 34)
//        tx.blockHeight = 123
//        tx.classVersion = 3
//        tx.comment = "hehey !"
//        tx.creationTime = 21324
//        tx.deviceId = "someDevice2324"
//        tx.fee = 234
//        tx.txSize = 23423
//        tx.exchangeCurrency = "curr"
//        tx.exchangeRate = 23.4343
//        KVStoreManager.getInstance().putTxMetaData(mActivityRule.getActivity(), tx, theHash)
//        val items = store!!.rawKVs
//        Assert.assertEquals(7, items.size.toLong())
//
//        val mds = KVStoreManager.getInstance().getAllTxMD(mActivityRule.getActivity())
//        Assert.assertEquals(mds.size.toLong(), 1)
//
//        //        Assert.assertEquals(mds.(0).blockHeight, 123);
////        Assert.assertEquals(mds.get(0).classVersion, 3);
////        Assert.assertEquals(mds.get(0).comment, "hehey !");
////        Assert.assertEquals(mds.get(0).creationTime, 21324);
////        Assert.assertEquals(mds.get(0).deviceId, "someDevice2324");
////        Assert.assertEquals(mds.get(0).fee, 234);
////        Assert.assertEquals(mds.get(0).txSize, 23423);
////        Assert.assertEquals(mds.get(0).exchangeCurrency, "curr");
////        Assert.assertEquals(mds.get(0).exchangeRate, 23.4343, 0);
//    }
//
//    @Test
//    fun testEncryptDecrypt() {
//        val data =
//            "Ladies and Gentlemen of the class of '99: If I could offer you only one tip for the future, " +
//                    "sunscreen would be it."
//        val encryptedData =
//            ReplicatedKVStore.encrypt(data.toByteArray(), mActivityRule.getActivity())
//
//        Assert.assertTrue(encryptedData != null && encryptedData.size > 0)
//
//        val decryptedData = ReplicatedKVStore.decrypt(encryptedData, mActivityRule.getActivity())
//
//        Assert.assertNotEquals(encryptedData, decryptedData)
//
//        Assert.assertArrayEquals(decryptedData, data.toByteArray())
//        Assert.assertEquals(String(decryptedData!!), data)
//    }
//
//    @Test
//    fun testKVManager() {
//        val tx = TxMetaData()
//        val theHash = byteArrayOf(3, 5, 64, 2, 4, 5, 63, 7, 0, 56, 34)
//        tx.blockHeight = 123
//        tx.classVersion = 3
//        tx.comment = "hehey !"
//        tx.creationTime = 21324
//        tx.deviceId = "someDevice2324"
//        tx.fee = 234
//        tx.txSize = 23423
//        tx.exchangeCurrency = "curr"
//        tx.exchangeRate = 23.4343
//        KVStoreManager.getInstance().putTxMetaData(mActivityRule.getActivity(), tx, theHash)
//        val items = store!!.rawKVs
//        Assert.assertEquals(1, items.size.toLong())
//
//        val newTx = KVStoreManager.getInstance().getTxMetaData(mActivityRule.getActivity(), theHash)
//        Assert.assertEquals(newTx.blockHeight.toLong(), 123)
//        Assert.assertEquals(newTx.classVersion.toLong(), 3)
//        Assert.assertEquals(newTx.comment, "hehey !")
//        Assert.assertEquals(newTx.creationTime.toLong(), 21324)
//        Assert.assertEquals(newTx.deviceId, "someDevice2324")
//        Assert.assertEquals(newTx.fee, 234)
//        Assert.assertEquals(newTx.txSize.toLong(), 23423)
//        Assert.assertEquals(newTx.exchangeCurrency, "curr")
//        Assert.assertEquals(newTx.exchangeRate, 23.4343, 0.0)
//    } //((MockUpAdapter) remote).remoteKVs.size()
//
//    companion object {
//        val TAG: String = KVStoreTests::class.java.name
//
//        @ClassRule
//        var mActivityRule: ActivityTestRule<WriteDownActivity> =
//            ActivityTestRule<WriteDownActivity>(
//                WriteDownActivity::class.java
//            )
//        private val remote: KVStoreAdaptor = MockUpAdapter()
//        private var store: ReplicatedKVStore? = null
//    }
//}
//
//}