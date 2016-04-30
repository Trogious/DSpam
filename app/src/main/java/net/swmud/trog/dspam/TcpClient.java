package net.swmud.trog.dspam;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class TcpClient implements Runnable {
    private static final String PROTOCOL = "TLSv1.2";
    private static final String SERVER_IP = "192.168.1.1";
    private static final int SERVER_PORT = 3000;
    private Socket socket;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private boolean running = false;
    private Listener msgListener;
    private Listener errListener;

    TcpClient(Listener<String> msgListener, Listener<String> errListener) {
        this.msgListener = msgListener;
        this.errListener = errListener;
    }

    @Override
    public void run() {
        running = true;
        Log.i("Debug", "TcpClient.run()");


        InetAddress serverAddr = null;
        try {
            serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVER_PORT);

/*
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sock = (SSLSocket) sf.createSocket(serverAddr, SERVER_PORT);
            sock.setEnabledProtocols(new String[] { PROTOCOL });
*/

            synchronized (socket) {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socket.setSoTimeout(0);
            }
        } catch (UnknownHostException e) {
            errListener.onMessage(e.getMessage());
            finish();
            return;
        } catch (IOException e) {
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
                finish();
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

        running = false;
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
    }

    public boolean isRunning() {
        return running;
    }

    public void sendMessage(String msg) {
        mBufferOut.print(msg);
        mBufferOut.flush();
    }

    public interface Listener<T> {
        void onMessage(T msg);
    }
}
