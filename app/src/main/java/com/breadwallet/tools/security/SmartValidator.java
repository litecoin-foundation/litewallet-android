package com.breadwallet.tools.security;

import android.app.Activity;
import android.content.Context;

import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.Bip39Reader;
import com.breadwallet.tools.util.TypesConverter;
import com.breadwallet.wallet.BRWalletManager;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class SmartValidator {

    private static List<String> list;

    public static boolean isPaperKeyValid(Context ctx, String paperKey) {
        String languageCode = Locale.getDefault().getLanguage();
        if (!isValid(ctx, paperKey, languageCode)) {
            //try all langs
            for (String lang : Bip39Reader.LANGS) {
                if (isValid(ctx, paperKey, lang)) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    private static boolean isValid(Context ctx, String paperKey, String lang) {
        List<String> list = Bip39Reader.bip39List(ctx, lang);
        String[] words = list.toArray(new String[list.size()]);
        if (words.length % Bip39Reader.WORD_LIST_SIZE != 0) {
            IllegalArgumentException ex = new IllegalArgumentException("words.length is not dividable by " + Bip39Reader.WORD_LIST_SIZE);
            Timber.e(ex);
            throw ex;
        }
        return BRWalletManager.getInstance().validateRecoveryPhrase(words, paperKey);
    }

    public static boolean isPaperKeyCorrect(String insertedPhrase, Context activity) {
        String normalizedPhrase = Normalizer.normalize(insertedPhrase.trim(), Normalizer.Form.NFKD);
        if (!SmartValidator.isPaperKeyValid(activity, normalizedPhrase))
            return false;
        BRWalletManager m = BRWalletManager.getInstance();
        byte[] rawPhrase = normalizedPhrase.getBytes();
        byte[] bytePhrase = TypesConverter.getNullTerminatedPhrase(rawPhrase);
        byte[] pubKey = m.getMasterPubKey(bytePhrase);
        byte[] pubKeyFromKeyStore = new byte[0];
        try {
            pubKeyFromKeyStore = BRKeyStore.getMasterPublicKey(activity);
        } catch (Exception e) {
            Timber.e(e);
        }
        Arrays.fill(bytePhrase, (byte) 0);
        return Arrays.equals(pubKey, pubKeyFromKeyStore);
    }

    public static boolean checkFirstAddress(Activity app, byte[] mpk) {
        String addressFromPrefs = BRSharedPrefs.getFirstAddress(app);
        String generatedAddress = BRWalletManager.getFirstAddress(mpk);
        if (!addressFromPrefs.equalsIgnoreCase(generatedAddress) && addressFromPrefs.length() != 0 && generatedAddress.length() != 0) {
            Timber.d("timber: checkFirstAddress: WARNING, addresses don't match: Prefs:" + addressFromPrefs + ", gen:" + generatedAddress);
        }
        return addressFromPrefs.equals(generatedAddress);
    }

    public static String cleanPaperKey(Context activity, String phraseToCheck) {
        return Normalizer.normalize(phraseToCheck.replace("　", " ").replace("\n", " ").trim().replaceAll(" +", " "), Normalizer.Form.NFKD);
    }

    public static boolean isWordValid(Context ctx, String word) {
        Timber.d("timber: isWordValid: word:" + word + ":" + word.length());
        if (list == null) list = Bip39Reader.bip39List(ctx, null);
        String cleanWord = Bip39Reader.cleanWord(word);
        Timber.d("timber: isWordValid: cleanWord:" + cleanWord + ":" + cleanWord.length());
        return list.contains(cleanWord);
    }
}
