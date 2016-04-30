package net.swmud.trog.dspam;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LaunchActivity extends AppCompatActivity {
    private final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    private TcpClient tcpClient;
    private final LaunchActivity self = this;
    private TextView bottomtext;
    private StringBuilder sending = new StringBuilder("sending request");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        bottomtext = (TextView) findViewById(R.id.bottomtext);

        boolean networkOk = false;
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i("Debug", "network OK");
            networkOk = true;
        } else {
            Log.i("Debug", "network FAILED");
        }

        if (networkOk) {
            startTcpClient();
        }

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tcpClient == null || !tcpClient.isRunning()) {
                    startTcpClient();
                } else {
                    sending.append(".");
                    bottomtext.setText(sending.toString());
                    tcpClient.sendMessage("{\"dspam\":0}");
                    Log.i("Debug", "onButtonSend2");
                }
            }
        });

    }

    private void startTcpClient() {
        Log.i("Debug", "startTcpClient");
        if (tcpClient != null) {
            tcpClient.finish();
        }
        tcpClient = new TcpClient(new TcpClient.Listener<String>() {
            @Override
            public void onMessage(final String msg) {
                Intent intent = new Intent(self, HistoryActivity.class);
                intent.putExtra("history", msg);
                startActivity(intent);
            }
        }, new TcpClient.Listener<String>() {
            @Override
            public void onMessage(final String msg) {
                bottomtext.post(new Runnable() {
                    @Override
                    public void run() {
                        bottomtext.setText(msg);
                    }
                });
            }
        });
        if (!tcpClient.isRunning()) {
            backgroundExecutor.execute(tcpClient);
        }
        sending = new StringBuilder("sending request");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}
