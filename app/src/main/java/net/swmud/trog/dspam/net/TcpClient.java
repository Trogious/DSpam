package net.swmud.trog.dspam.net;

import android.util.Log;

import net.swmud.trog.dspam.core.Constants;
import net.swmud.trog.dspam.core.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class TcpClient implements Runnable {
    private static final int READ_SIZE = 4096;
    private static final int CONNECT_TIMEOUT = 10000;
    private String host;
    private int port;
    private boolean useClientCertLogin;
    private Socket socket = new Socket();
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private boolean running = false;
    private Listener msgListener;
    private Listener errListener;

    public TcpClient(String host, int port, boolean useClientCertLogin, Listener<String> msgListener, Listener<String> errListener) {
        this.host = host;
        this.port = port;
        this.useClientCertLogin = useClientCertLogin;
        this.msgListener = msgListener;
        this.errListener = errListener;
    }

    @Override
    public void run() {
        running = true;
        Log.d("Tcp", "TcpClient.run()");

        SocketAddress sockAddr = new InetSocketAddress(host, port);
        try {
            Log.d("Tcp", "try");
            socket.connect(sockAddr, CONNECT_TIMEOUT);
            Log.d("Tcp", "socket created");

            SSLSocket sock = createSslSocket(socket);
            sock.startHandshake();
//            examineSslSocket(sock);

            synchronized (sock) {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                sock.setSoTimeout(0);
            }
        } catch (UnknownHostException e) {
            handleException(e);
            return;
        } catch (IOException e) {
            handleException(e);
            return;
        } catch (NoSuchAlgorithmException e) {
            handleException(e);
            return;
        } catch (KeyManagementException e) {
            handleException(e);
            return;
        } catch (KeyStoreException e) {
            handleException(e);
            return;
        } catch (UnrecoverableKeyException e) {
            handleException(e);
            return;
        }
        errListener.onMessage("connected");

        ResponseParser responseParser = new ResponseParser();
        while (running) {
            Log.d("Tcp", "while");
            char buf[] = new char[READ_SIZE];
            int bytesRead;
            try {
                bytesRead = mBufferIn.read(buf, 0, READ_SIZE);
                Log.d("bytesRead", "" + bytesRead);
                responseParser.parse(buf, bytesRead);
            } catch (SocketTimeoutException e) {
                errListener.onMessage("socket timed out ok");
                continue;
            } catch (IOException e) {
                errListener.onMessage(e.getMessage());
                break;
            }

            if (bytesRead < 1) {
                Log.d("Tcp", "connection closed by peer");
                errListener.onMessage("connection closed by peer");
                break;
            } else {
                if (responseParser.isRequestComplete()) {
                    String msg = responseParser.getBody();
                    Log.d("message", msg);
                    Log.d("messageLen", "" + msg.length());
                    msgListener.onMessage(msg);
                    responseParser = new ResponseParser();
                }
            }
        }

        Log.d("Tcp", "finished");
        finish();
    }

    private void handleException(Exception e) {
        Log.d("E", e.getMessage());
        errListener.onMessage(e.getMessage());
        finish();
    }

    private void closeSocket() {
        if (socket != null) {
            synchronized (socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void finish() {
        running = false;
        closeSocket();
        Log.d("E", "finished");
    }

    public boolean isRunning() {
        return running;
    }

    public void sendMessage(String msg) {
        if (mBufferOut != null) {
            synchronized (mBufferOut) {
                mBufferOut.print(msg);
                mBufferOut.flush();
            }
        }
    }

    public interface Listener<T> {
        void onMessage(T msg);
    }

    private SSLSocket createSslSocket(Socket socket) throws NoSuchAlgorithmException, KeyManagementException, IOException, KeyStoreException, UnrecoverableKeyException {
        SSLSocketFactory sf = new ExtendedSslFactory(useClientCertLogin);//(SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sock = (SSLSocket) sf.createSocket(socket, host, port, true);
        sock.setEnabledProtocols(new String[]{Constants.SECURE_PROTOCOL});

        return sock;
    }

    private static void examineX509Cert(X509Certificate cert) {
        Log.d("x509", "not before: " + DateFormatter.format(cert.getNotBefore()));
        Log.d("x509", "not after: " + DateFormatter.format(cert.getNotAfter()));
        Log.d("x509", "issuer DN: " + cert.getIssuerDN().getName());
        Log.d("x509", "issuer principal x500: " + cert.getIssuerX500Principal().getName());
    }

    private static void examineX509Cert(javax.security.cert.X509Certificate cert) {
        Log.d("x509", "not before: " + DateFormatter.format(cert.getNotBefore()));
        Log.d("x509", "not after: " + DateFormatter.format(cert.getNotAfter()));
        Log.d("x509", "issuer DN: " + cert.getIssuerDN().getName());
        Log.d("x509", "subject DN: " + cert.getSubjectDN().getName());
    }

    private static void examineSslSocket(SSLSocket sock) {
        Log.d("SSL", "want client auth: " + sock.getWantClientAuth());
        Log.d("SSL", "need client auth: " + sock.getNeedClientAuth());
        SSLSession sess = sock.getSession();
        Log.d("SSL", "protocol used: " + sess.getProtocol());
        Log.d("SSL", "peer certificates:");
        try {
            Certificate[] certs = sess.getPeerCertificates();
            if (certs != null) {
                for (Certificate cert : certs) {
                    if (cert instanceof X509Certificate) {
                        X509Certificate x5 = (X509Certificate) cert;
                        examineX509Cert(x5);
                    } else {
                        Log.d("SSL", "NOT x509");
                    }
                }
            }
        } catch (SSLPeerUnverifiedException e) {
            Log.d("SSL", e.getMessage());
        }

        Log.d("SSL", "local certificates:");
        try {
            Certificate[] certs = sess.getLocalCertificates();
            if (certs != null) {
                for (Certificate cert : certs) {
                    if (cert instanceof X509Certificate) {
                        X509Certificate x5 = (X509Certificate) cert;
                        examineX509Cert(x5);
                    } else {
                        Log.d("SSL", "NOT x509");
                    }
                }
            }
        } catch (Exception e) {
            Log.d("SSL", e.getMessage());
        }

        Log.d("SSL", "peer certificate chain:");
        try {
            javax.security.cert.X509Certificate[] x509certs = sess.getPeerCertificateChain();
            if (x509certs != null) {
                for (javax.security.cert.X509Certificate cert : x509certs) {
                    examineX509Cert(cert);
                }
            }
        } catch (SSLPeerUnverifiedException e) {
            Log.d("SSL", e.getMessage());
        }

        Log.d("SSL", "value names:");
        for (String s : sess.getValueNames()) {
            Log.d("SSL", " " + s);
        }
        Log.d("SSL", "enabled protocols:");
        for (String s : sock.getEnabledProtocols()) {
            Log.d("SSL", " " + s);
        }
        Log.d("SSL", "enabled cipher suites:");
        for (String s : sock.getEnabledCipherSuites()) {
            Log.d("SSL", " " + s);
        }
    }
}
