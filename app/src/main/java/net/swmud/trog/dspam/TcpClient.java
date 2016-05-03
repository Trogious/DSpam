package net.swmud.trog.dspam;

import android.util.Log;

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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class TcpClient implements Runnable {
    private static final String PROTOCOL = "TLSv1.2";
    private String host;
    private int port;
    private Socket socket = new Socket();
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private boolean running = false;
    private Listener msgListener;
    private Listener errListener;

    TcpClient(String host, int port, Listener<String> msgListener, Listener<String> errListener) {
        this.host = host;
        this.port = port;
        this.msgListener = msgListener;
        this.errListener = errListener;
    }

    @Override
    public void run() {
        running = true;
        Log.i("Debug", "TcpClient.run()");


        try {
            Log.d("D", "try");
            SocketAddress sockAddr = new InetSocketAddress(host, port);
            socket.connect(sockAddr, 10000);
            Log.d("D", "socket created");

/*
            SSLSocket sock = createSslSocket(socket, SERVER_IP, SERVER_PORT);
            sock.startHandshake();
*/

            synchronized (socket) {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socket.setSoTimeout(0);
            }
        } catch (UnknownHostException e) {
            Log.e("E", e.getMessage());
            errListener.onMessage(e.getMessage());
            finish();
            return;
        } catch (IOException e) {
            Log.e("E", e.getMessage());
            errListener.onMessage(e.getMessage());
            finish();
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

    private SSLSocket createSslSocket(Socket socket) throws IOException {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sock = (SSLSocket) sf.createSocket(socket, host, port, true);
        sock.setEnabledProtocols(new String[] { PROTOCOL });

        return sock;
    }
}
