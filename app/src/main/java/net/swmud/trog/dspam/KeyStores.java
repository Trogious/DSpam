package net.swmud.trog.dspam;

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
    private static final char[] KEY_STORE_PASSWORD = "dspamstorepass".toCharArray();
    private static final String KEY_STORE_CLIENT = "dspamstoreclient.bks";
    private static final String KEY_STORE_TRUST = "dspamstoretrust.bks";
    private static final String KEY_STORE_ISSUER = "dspamstoretrust.bks";
    private String dataDirectory;

    public KeyStores(String dataDirectory) {
        this.dataDirectory = dataDirectory;
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

    @Nullable
    private KeyStore loadKeyStore(String keyStoreFileName) {
        File path = new File(new File(dataDirectory, KEY_DATA_DIR), keyStoreFileName);
        try {
            FileInputStream ksInputStream = new FileInputStream(path);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(ksInputStream, KEY_STORE_PASSWORD);
            Log.e("KSs", "loaded key store: " + path.getAbsolutePath());
            Enumeration<String> as = ks.aliases();
            while (as.hasMoreElements()) {
                String a = as.nextElement();
                Log.e("KSs", " " + a);
            }
            return ks;
        } catch (KeyStoreException e) {
            Log.e("KSs", e.getMessage());
        } catch (CertificateException e) {
            Log.e("KSs", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KSs", e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e("KSs", e.getMessage());
        } catch (IOException e) {
            Log.e("KSs", e.getMessage());
        }

        return null;
    }
}
