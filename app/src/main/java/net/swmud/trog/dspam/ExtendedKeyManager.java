package net.swmud.trog.dspam;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.X509KeyManager;

public class ExtendedKeyManager implements X509KeyManager {

    private X509KeyManager defaultKeyManager;
    private Properties serverMap = new Properties();

    public ExtendedKeyManager() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(LaunchActivity.inputStream, "x".toCharArray());
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
            if (alias != null &&
                    alias.length() == 0) {
                alias = null;
            }
        } else {
            alias = null;//defaultKeyManager.chooseClientAlias(keyType, issuers, socket);
        }
        return alias;
    }

    @Override
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        Log.e("KM", "chooseServerAlias");
        return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String s) {
        Log.e("KM", "getCertificateChain");
        return new X509Certificate[0];
    }

    @Override
    public String[] getClientAliases(String s, Principal[] principals) {
        Log.e("KM", "getClientAliases");
        return new String[0];
    }

    @Override
    public String[] getServerAliases(String s, Principal[] principals) {
        Log.e("KM", "getServerAliases");
        return new String[0];
    }

    @Override
    public PrivateKey getPrivateKey(String s) {
        Log.e("KM", "getPrivateKey");

        return null;
    }
}
