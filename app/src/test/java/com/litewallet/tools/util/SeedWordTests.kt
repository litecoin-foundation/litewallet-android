package com.litewallet.tools.util


class SeedWordTests {
}

//
//
//package com.litewallet;
//
//import android.util.Log;
//
//import com.breadwallet.tools.util.Bip39Reader;
//import com.breadwallet.wallet.BRWalletManager;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.notNullValue;
//import static org.junit.Assert.assertThat;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.filters.LargeTest;
//
//@RunWith(AndroidJUnit4.class)
//    @LargeTest
//
//    public class PaperKeyTests {
//        private static final String TAG = PaperKeyTests.class.getName();
//        public static final List<String> PAPER_KEY_ENG =  Arrays.asList("stick","sword","keen","afraid","smile","sting","huge","relax","nominee","arena","area","gift");
//
//        @Test
//        public void testWordsValid() {
//            List<String> wordList = getAllWords();
//            Assert.assertEquals(wordList.size(), 2048);
//            Assert.assertSame(PAPER_KEY_ENG.containsAll(wordList),true);
//        }
//        @Test
//        public void testPaperKeyValidation() {
//            List<String> list = getAllWords();
//            assertThat(list.size(), is(2048));
//        }
//
//        private List<String> getAllWords() {
//            List<String> result = new ArrayList<>();
//            List<String> names = new ArrayList<>();
//            names.add("en-BIP39Words.txt");
//
//            for (String fileName : names) {
//                InputStream in = null;
//                try {
//                    in = getClass().getResourceAsStream(fileName);
//                    String str = IOUtils.toString(in);
//                    String lines[] = str.split("\\r?\\n");
//                    result.addAll(Arrays.asList(lines));
//                } catch (IOException e) {
//                    Log.e(TAG, "getAllWords: " + fileName + ", ", e);
//                } finally {
//                    if (in != null) try {
//                        in.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            List<String> cleanList = new ArrayList<>();
//            for (String s : result) {
//                String cleanWord = Bip39Reader.cleanWord(s);
//                cleanList.add(cleanWord);
//            }
//            assertThat(cleanList.size(), is(2048));
//            return cleanList;
//        }
//
//        private boolean isValid(String phrase, List<String> words) {
//
//            return BRWalletManager.getInstance().validateRecoveryPhrase((String[]) words.toArray(), phrase);
//        }
//
//    }
