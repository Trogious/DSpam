package net.swmud.trog.dspam;

import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class ExtendedTrustManager implements X509TrustManager {
    protected X509Certificate acceptedIssuer;
    protected List<X509TrustManager> trustManagers = new ArrayList<X509TrustManager>();

    protected ExtendedTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        final ArrayList<TrustManagerFactory> factories = new ArrayList<>();

        // The default Trustmanager with default keystore
        final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        original.init((KeyStore) null);
        factories.add(original);
/*
            for( KeyStore keyStore : additionalkeyStores ) {
                final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                additionalCerts.init(keyStore);
                factories.add(additionalCerts);
            }
*/

        for (TrustManagerFactory trustManagerFactory : factories) {
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager) {
                    trustManagers.add((X509TrustManager) trustManager);
                }
            }
        }

        if (trustManagers.size() < 1)
            throw new RuntimeException("No X509TrustManagers available."); //TODO: use more meaningful exception class
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        Log.e("AUTH", "checkClientTrusted");
        throw new CertificateException("This trust manager validates only server certificates");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (chain == null || chain.length < 1) {
            throw new CertificateException("x509 cert chain empty");
        }
        acceptedIssuer = chain[0];

        Log.e("AUTH", "_authType: " + authType);

        for (X509TrustManager trustManager : trustManagers) {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
            }
        }

        Log.e("AUTH", "Server certificate not trusted: " + chain[0].getIssuerDN().getName());
//        throw new CertificateException("Server certificate not trusted.");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        Log.e("AUTH", "getAcceptedIssuers");
        return new X509Certificate[]{acceptedIssuer};
    }
}
