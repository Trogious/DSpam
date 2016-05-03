package net.swmud.trog.dspam;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class ExtendedSslFactory extends SSLSocketFactory {
    protected static final SSLSocketFactory SOCKET_FACTORY = (SSLSocketFactory) SSLSocketFactory.getDefault();
    protected SSLContext sslContext = SSLContext.getInstance(Constants.SECURE_PROTOCOL);

    public ExtendedSslFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        sslContext.init(new KeyManager[]{new ExtendedKeyManager()}, new TrustManager[]{new ExtendedTrustManager()}, null);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return SOCKET_FACTORY.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return SOCKET_FACTORY.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return sslContext.getSocketFactory().createSocket(s, i);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress localAddress, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(s, i, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress localAddress, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(inetAddress, i, localAddress, localPort);
    }
}
