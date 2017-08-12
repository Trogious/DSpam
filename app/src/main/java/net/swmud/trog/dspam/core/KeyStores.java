package net.swmud.trog.dspam.core;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class KeyStores {
    /* bcprov-jdk15on-154.jar */
    private static final String KEY_DATA_DIR = "DSpam";
    private static final char[] KEY_STORE_PASSWORD = "dupa.12".toCharArray();
    private static final String KEY_STORE_CLIENT = "dspamstoreclient.bks";
    private static final String KEY_STORE_TRUST = "dspamstoretrust.bks";
    private static final String KEY_STORE_ISSUER = KEY_STORE_TRUST;
    private final String dataDirectory;
    private final PasswordProvider privateKeyPasswordProvider;

    public KeyStores(String dataDirectory, PasswordProvider passwordProvider) {
        this.dataDirectory = dataDirectory;
        this.privateKeyPasswordProvider = passwordProvider;
    }

    @Nullable
    public KeyStore getClientKeyStore() {
        return loadKeyStore(KEY_STORE_CLIENT);
    }

    @Nullable
    public KeyStore getTrustKeyStore() {
        return loadKeyStore(KEY_STORE_TRUST);
    }

    @Nullable
    public KeyStore getIssuerKeyStore() {
        return loadKeyStore(KEY_STORE_ISSUER);
    }


    public char[] getPrivateKeyPassword() {
        return privateKeyPasswordProvider.getPassword().toCharArray();
    }

    @Nullable
    private KeyStore loadKeyStore(String keyStoreFileName) {
        File path = new File(new File(dataDirectory, KEY_DATA_DIR), keyStoreFileName);
        try {
            FileInputStream ksInputStream = new FileInputStream(path);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(ksInputStream, KEY_STORE_PASSWORD);
            Log.d("KSs", "loaded key store: " + path.getAbsolutePath());
            Enumeration<String> as = ks.aliases();
            while (as.hasMoreElements()) {
                String a = as.nextElement();
                Log.d("KSs", " " + a);
            }
            return ks;
        } catch (KeyStoreException e) {
            Log.d("KSs", e.getMessage());
        } catch (CertificateException e) {
            Log.d("KSs", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d("KSs", e.getMessage());
        } catch (FileNotFoundException e) {
            Log.d("KSs", e.getMessage());
        } catch (IOException e) {
            Log.d("KSs", e.getMessage());
        }

        return null;
    }
}
