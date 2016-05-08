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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class TcpClient implements Runnable {
    private String host;
    private int port;
    private Socket socket = new Socket();
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private boolean running = false;
    private Listener msgListener;
    private Listener errListener;

    public TcpClient(String host, int port, Listener<String> msgListener, Listener<String> errListener) {
        this.host = host;
        this.port = port;
        this.msgListener = msgListener;
        this.errListener = errListener;
    }

    @Override
    public void run() {
        running = true;
        Log.i("Debug", "TcpClient.run()");

        SocketAddress sockAddr = new InetSocketAddress(host, port);
        try {
            Log.d("D", "try");
            socket.connect(sockAddr, 10000);
            Log.d("D", "socket created");

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
        }
        errListener.onMessage("connected");

        ResponseParser rp = new ResponseParser();
        while (running) {
            Log.i("Debug", "while");
            final int SIZE = 4096;
            char buf[] = new char[SIZE];
            int bytesRead = 0;
            try {
                bytesRead = mBufferIn.read(buf, 0, SIZE);
                Log.e("bytesRead", "" + bytesRead);
                rp.parse(buf, bytesRead);
            } catch (SocketTimeoutException e) {
                errListener.onMessage("socket timed out ok");
                continue;
            } catch (IOException e) {
                errListener.onMessage(e.getMessage());
                break;
            }

            if (bytesRead < 1) {
                Log.i("Debug", "connection closed by peer");
                errListener.onMessage("connection closed by peer");
                break;
            } else {
                if (rp.isRequestComplete()) {
                    String msg = rp.getBody();
                    Log.e("message", msg);
                    Log.e("messageLen", "" + msg.length());
                    msgListener.onMessage(msg);
                    rp = new ResponseParser();
                }
            }
        }

        Log.i("Debug", "finished");
        finish();
    }

    private void handleException(Exception e) {
        Log.e("E", e.getMessage());
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
        Log.e("E", "finished");
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

    private SSLSocket createSslSocket(Socket socket) throws NoSuchAlgorithmException, KeyManagementException, IOException, KeyStoreException {
        SSLSocketFactory sf = new ExtendedSslFactory();//(SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sock = (SSLSocket) sf.createSocket(socket, host, port, true);
        sock.setEnabledProtocols(new String[]{Constants.SECURE_PROTOCOL});

        return sock;
    }

    private static void examineX509Cert(X509Certificate cert) {
        Log.e("x509", "not before: " + DateFormatter.format(cert.getNotBefore()));
        Log.e("x509", "not after: " + DateFormatter.format(cert.getNotAfter()));
        Log.e("x509", "issuer DN: " + cert.getIssuerDN().getName());
        Log.e("x509", "issuer principal x500: " + cert.getIssuerX500Principal().getName());
    }

    private static void examineX509Cert(javax.security.cert.X509Certificate cert) {
        Log.e("x509", "not before: " + DateFormatter.format(cert.getNotBefore()));
        Log.e("x509", "not after: " + DateFormatter.format(cert.getNotAfter()));
        Log.e("x509", "issuer DN: " + cert.getIssuerDN().getName());
        Log.e("x509", "subject DN: " + cert.getSubjectDN().getName());
    }

    private static void examineSslSocket(SSLSocket sock) {
        Log.e("SSL", "want client auth: " + sock.getWantClientAuth());
        Log.e("SSL", "need client auth: " + sock.getNeedClientAuth());
        SSLSession sess = sock.getSession();
        Log.e("SSL", "protocol used: " + sess.getProtocol());
        Log.d("SSL", "peer certificates:");
        try {
            Certificate[] certs = sess.getPeerCertificates();
            if (certs != null) {
                for (Certificate cert : certs) {
                    if (cert instanceof X509Certificate) {
                        X509Certificate x5 = (X509Certificate) cert;
                        examineX509Cert(x5);
                    } else {
                        Log.e("SSL", "NOT x509");
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
                        Log.e("SSL", "NOT x509");
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
