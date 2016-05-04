package net.swmud.trog.dspam;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

public class ExtendedKeyManager implements X509KeyManager {

    private X509KeyManager defaultKeyManager;
    private Properties serverMap = new Properties();
    private KeyStore ks = null;

    public ExtendedKeyManager() {
        try {
            Log.e("KM", "ks type: " + KeyStore.getDefaultType());
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(LaunchActivity.getInputStream(), "dupa.12".toCharArray());
            Enumeration<String> aliases = ks.aliases();
            Log.e("KM", "keystore loaded");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
        Log.e("KM", "chooseClientAlias");
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        String hostName = ((InetSocketAddress) socketAddress).getHostName().toUpperCase();
        Log.e("KM", "hostname: " + hostName);
        String alias = null;
        if (serverMap.containsKey(hostName)) {
            alias = serverMap.getProperty(hostName.toUpperCase());
            if (alias != null && alias.length() == 0) {
                alias = null;
            }
        } else {
            alias = null;//defaultKeyManager.chooseClientAlias(keyType, issuers, socket);
        }
        return "swmud.net";
    }

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        Log.e("KM", "chooseServerAlias");
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        Log.e("KM", "getCertificateChain");
        if (ks != null) {
            Log.d("KM", "ks NOT null");
            try {
                Certificate[] chain = ks.getCertificateChain(alias);
                List<X509Certificate> certList = new ArrayList<>();
                for (Certificate c : chain) {
                    certList.add((X509Certificate) c);
                    Log.d("KM", "chain cert: " + c.toString());
                }
                return certList.toArray(new X509Certificate[certList.size()]);
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
        return new X509Certificate[0];
    }

    @Override
    public String[] getClientAliases(String alias, Principal[] principals) {
        Log.e("KM", "getClientAliases");
        return new String[0];
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        Log.e("KM", "getServerAliases");
        return new String[0];
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        Log.e("KM", "getPrivateKey1");
        if (ks != null) {
            Log.d("KM", "ks NOT null");
            try {
                Enumeration<String> as = ks.aliases();
                Log.d("KM", "has swmud: " + ks.containsAlias("swmud.net"));
                Certificate cert = ks.getCertificate(alias);
                if (cert != null) {
/*
                    Log.d("KM", "swmud cert: " + cert.toString());
                    Key pubKey = cert.getPublicKey();
                    Log.d("KM", "swmud publickey: " + pubKey.toString());
*/
                }
                Log.d("KM", "aliases:");
                while (as.hasMoreElements()) {
                    String a = as.nextElement();
                    Log.e("KM", " " + a);
                }
                Key key = (PrivateKey) ks.getKey(alias, "dupa.12".toCharArray());
                Log.e("KM", "privateKey: " + key.toString());
                return (PrivateKey) key;
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static class KeyWrapper implements PrivateKey {
        private Key key;

        KeyWrapper(Key key) {
            this.key = key;
        }

        @Override
        public String getAlgorithm() {
            return key.getAlgorithm();
        }

        @Override
        public String getFormat() {
            return key.getFormat();
        }

        @Override
        public byte[] getEncoded() {
            return key.getEncoded();
        }
    }
}
