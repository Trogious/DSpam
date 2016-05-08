package net.swmud.trog.dspam.gui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.BackgroundExecutor;
import net.swmud.trog.dspam.core.Global;
import net.swmud.trog.dspam.core.KeyStores;
import net.swmud.trog.dspam.core.Settings;
import net.swmud.trog.dspam.net.TcpClient;

public class LaunchActivity extends AppCompatActivity {
    private static final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    static TcpClient tcpClient;
    private final LaunchActivity self = this;
    private TextView bottomtext;
    private StringBuilder sending = new StringBuilder("sending request");

//    public static InputStream getInputStream() {
//        return SELF.getResources().openRawResource(R.raw.mystore);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Global.keyStores = new KeyStores(Environment.getExternalStorageDirectory().getPath());
        bottomtext = (TextView) findViewById(R.id.bottomtext);

        startTcpClient();

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tcpClient == null || !tcpClient.isRunning()) {
                    startTcpClient();
                } else {
                    sending.append(".");
                    bottomtext.setText(sending.toString());
                    sendMessage("{\"dspam\":0}");
                    Log.i("Debug", "onButtonSend2");
                }
            }
        });
    }

    static void sendMessage(final String msg) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                tcpClient.sendMessage(msg);
            }
        });
    }

    private void startTcpClient() {
        Log.i("Debug", "startTcpClient");
        if (tcpClient != null) {
            tcpClient.finish();
        }
        final Settings settings = Settings.loadSettings(this);
        tcpClient = new TcpClient(settings.getHost(), settings.getPort(),
                new TcpClient.Listener<String>() {
                    @Override
                    public void onMessage(final String msg) {
                        Intent intent = new Intent(self, HistoryActivity.class);
                        intent.putExtra("history", msg);
                        startActivity(intent);
                    }
                },
                new TcpClient.Listener<String>() {
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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        if (tcpClient != null) {
            tcpClient.finish();
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}
