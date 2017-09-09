package net.swmud.trog.dspam;

import android.util.Base64;
import android.util.Log;

import com.google.common.io.BaseEncoding;

import net.swmud.trog.dspam.core.Crypto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, Base64.class})
@PowerMockIgnore({"javax.crypto.*"})
public class CryptoTest {
    {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(Base64.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getMethod().getName() == "encodeToString") {
                    return BaseEncoding.base64().encode(invocation.getArgumentAt(0, byte[].class));
                } else if (invocation.getMethod().getName() == "decode") {
                    return BaseEncoding.base64().decode(invocation.getArgumentAt(0, String.class));
                }

                return null;
            }
        });
    }

    @Test
    public void encryptDecryptTest() throws Exception {
        String key = "7f9de17b67d5338b";
        String text = "foobarwenttoofoo";
        String enc = Crypto.encrypt(key, text);
        String dec = Crypto.decrypt(key, enc);
        assertEquals("cryptDecrypt", text, dec);
    }

    @Test
    public void differentPasswordsTest() throws Exception {
        String key = "7f9de17b67d5338b";
        String text = "foobarwenttoofoo";
        String enc = Crypto.encrypt(key, text);
        String dec = Crypto.decrypt(key, enc);
        assertEquals("differentPasswords1", text, dec);

        text = "a";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentPasswords2", text, dec);

        text = "asdai43";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentPasswords3", text, dec);

        text = "lopskj,12";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentPasswords4", text, dec);

        text = "tjsgsalkafuybnrff-x";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentPasswords5", text, dec);
    }

    @Test
    public void differentKeysTest() throws Exception {
        String key = "7f9de17b67d5338b";
        String text = "foobarwenttoofoo";
        String enc = Crypto.encrypt(key, text);
        String dec = Crypto.decrypt(key, enc);
        assertEquals("differentKeys1", text, dec);

        key = "7f9de17b67d__38b";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentKeys2", text, dec);

        key = "7f9cc17b67d__38b";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentKeys3", text, dec);

        key = "7f9ss17b67d__38b";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentKeys4", text, dec);

        key = "7f9de17b67d__58c";
        enc = Crypto.encrypt(key, text);
        dec = Crypto.decrypt(key, enc);
        assertEquals("differentKeys5", text, dec);
    }
}