package net.swmud.trog.dspam;

import net.swmud.trog.dspam.core.Crypto;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class CryptoTest {
    @Test
    public void encryptDecryptTest() throws Exception {
        String key = "7f9de17b67d5338b";
        String text = "foobarwenttoofoo";
        String enc = Crypto.encrypt(key, text);
        String dec = Crypto.decrypt(key, enc);
        assertTrue("cryptDecrypt", text.equals(dec));
    }
}