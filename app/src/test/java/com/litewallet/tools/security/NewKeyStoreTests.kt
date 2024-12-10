package com.litewallet.tools.security
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
class NewKeyStoreTests {
}

//package com.litewallet.security;
//
//import android.app.Activity;
//import android.security.keystore.UserNotAuthenticatedException;
//import androidx.test.rule.ActivityTestRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.breadwallet.presenter.activities.settings.TestActivity;
//import com.breadwallet.tools.security.BRKeyStore;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static com.breadwallet.tools.security.BRKeyStore.PHRASE_ALIAS;
//import static com.breadwallet.tools.security.BRKeyStore.aliasObjectMap;
//
//@RunWith(AndroidJUnit4.class)
//        public class NewKeyStoreTests {
//    public static final String TAG = NewKeyStoreTests.class.getName();
//
//    @Rule
//    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(TestActivity.class);
//
//    @Before
//    public void setup() {
//        BRKeyStore.resetWalletKeyStore(mActivityRule.getActivity());
//    }
//
//    @Test
//    public void testBase64() {
//        Activity app = mActivityRule.getActivity();
//        String temp = "here is some data to encrypt! @#$%^&*";
//        byte[] phrase = temp.getBytes();
//        BRKeyStore.storeEncryptedData(app, phrase, "phrase");
//        byte[] retrievedPhrase = BRKeyStore.retrieveEncryptedData(app, "phrase");
//        Assert.assertNotNull(retrievedPhrase);
//        Assert.assertArrayEquals("Oh no", phrase, retrievedPhrase);
//        String newTemp = new String(retrievedPhrase);
//        Assert.assertEquals(temp, newTemp);
//
//    }
//
//    @Test
//    public void setNewGetNew() {
//        //set get phrase
//        byte[] phrase = "axis husband project any sea patch drip tip spirit tide bring belt".getBytes();
//        try {
//            boolean b = BRKeyStore.putPhrase(phrase, mActivityRule.getActivity(), 0);
//            Assert.assertEquals(true, b);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//            Assert.assertNotNull(getPhrase);
//            Assert.assertEquals(new String(getPhrase), new String(phrase));
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void setOldSetNew() {
//        byte[] phrase = "axis husband project any sea patch drip tip spirit tide bring belt".getBytes();
//        BRKeyStore.AliasObject obj = aliasObjectMap.get(PHRASE_ALIAS);
//        try {
//            boolean b = BRKeyStore._setOldData(mActivityRule.getActivity(), phrase, obj.alias, obj.datafileName, obj.ivFileName, 0, true);
//            Assert.assertEquals(true, b);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore._getOldData(mActivityRule.getActivity(), obj.alias, obj.datafileName, obj.ivFileName, 0);
//            Assert.assertNotNull(getPhrase);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//        try {
//            boolean b = BRKeyStore.putPhrase(phrase, mActivityRule.getActivity(), 0);
//            Assert.assertEquals(true, b);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//        try {
//            byte[] getPhrase = BRKeyStore._getOldData(mActivityRule.getActivity(), obj.alias, obj.datafileName, obj.ivFileName, 0);
//            Assert.assertNull(getPhrase);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//        try {
//            byte[] getPhrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//            Assert.assertNotNull(getPhrase);
//            Assert.assertEquals(new String(getPhrase), new String(phrase));
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void setOldGetNew() {
//
//        //set get phrase
//        byte[] phrase = "axis husband project any sea patch drip tip spirit tide bring belt".getBytes();
//        BRKeyStore.AliasObject obj = aliasObjectMap.get(PHRASE_ALIAS);
//        try {
//            boolean s = BRKeyStore._setOldData(mActivityRule.getActivity(), phrase, obj.alias, obj.datafileName, obj.ivFileName, 0, true);
//            Assert.assertEquals(true, s);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//            Assert.assertNotNull(getPhrase);
//            Assert.assertEquals(new String(getPhrase), new String(phrase));
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//            Assert.assertNotNull(getPhrase);
//            Assert.assertEquals(new String(getPhrase), new String(phrase));
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore._getOldData(mActivityRule.getActivity(), obj.alias, obj.datafileName, obj.ivFileName, 0);
//            Assert.assertNull(getPhrase);
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//        try {
//            byte[] getPhrase = BRKeyStore.getPhrase(mActivityRule.getActivity(), 0);
//            Assert.assertNotNull(getPhrase);
//            Assert.assertEquals(new String(getPhrase), new String(phrase));
//        } catch (UserNotAuthenticatedException e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//
//    }
//
//    @Test
//    public void allMultiThreading() {
//        for (int i = 0; i < 10; i++) {
//            setup();
//            setNewGetNew();
//            setup();
//            setOldGetNew();
//            setup();
//            setOldSetNew();
//            setup();
//
//        }
//
//    }
//
//}
