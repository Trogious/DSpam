package net.swmud.trog.dspam;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String ENCODING = "UTF-8";


    public static String encrypt(String key, String clearText) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        SecretKey ks = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Log.e("SK", "bits:" + ks.getEncoded().length);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, ks);
        byte[] encryptedText = c.doFinal(clearText.getBytes(ENCODING));

        return Base64.encodeToString(encryptedText, Base64.DEFAULT);
    }

    public static String decrypt(String key, String encryptedText) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        SecretKey ks = new SecretKeySpec(key.getBytes(), ALGORITHM);
        Log.e("SK2", "bits:" + ks.getEncoded().length);
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, ks);
        byte[] clearText = c.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));

        return new String(clearText, ENCODING);
    }
}