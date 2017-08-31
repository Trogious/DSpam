package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.BackgroundExecutor;
import net.swmud.trog.dspam.core.Global;
import net.swmud.trog.dspam.core.KeyStores;
import net.swmud.trog.dspam.core.PasswordProvider;
import net.swmud.trog.dspam.core.Settings;
import net.swmud.trog.dspam.json.JsonResponse;
import net.swmud.trog.dspam.json.JsonRpc;
import net.swmud.trog.dspam.net.TcpClient;

import java.util.HashMap;
import java.util.Map;

public class LaunchActivity extends AppCompatActivity {
    private static final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    static TcpClient tcpClient;
    private final LaunchActivity self = this;
    private TextView bottomtext;
    private StringBuilder sending = new StringBuilder("sending request");
    private Settings settings = null;
    private static final Map<Long, Pair<String, Class<? extends Activity>>> responseRoutes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        settings = Settings.loadSettings(this);
        Global.keyStores = new KeyStores(Global.getKeyStoresLocation(), new PasswordProvider() {
            @Override
            public String getPassword() {
                return settings.getPassword();
            }
        });
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
                    JsonRpc.JsonRequest request = JsonRpc.getRequest("get_entries", null);
                    sendMessage(request, "history", HistoryActivity.class);
                    Log.d("LA", "onButtonSend2");
                }
            }
        });
    }

    static void sendMessage(final JsonRpc.JsonRequest request, final String key, final Class<? extends Activity> clazz) {
        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (responseRoutes) {
                    responseRoutes.put(request.getId(), new Pair<String, Class<? extends Activity>>(key, clazz));
                }
                tcpClient.sendMessage(request.toString());
            }
        });
    }

    private void startTcpClient() {
        Log.d("LA", "startTcpClient");
        if (tcpClient != null) {
            tcpClient.finish();
        }
        tcpClient = new TcpClient(settings.getHost(), settings.getPort(), settings.isLoginWithCertificate(), settings.getPreferredCertificateAlias(),
                new TcpClient.Listener<String>() {
                    @Override
                    public void onMessage(final String msg) {
                        JsonResponse response = new Gson().fromJson(msg, JsonResponse.class);
                        Pair<String, Class<? extends Activity>> keyClass = responseRoutes.get(response.getId());
                        Intent intent = new Intent(self, keyClass.second);
                        intent.putExtra(keyClass.first, msg);
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
            case R.id.action_disconnect:
                if (tcpClient != null) {
                    tcpClient.finish();
                }
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
