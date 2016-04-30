package net.swmud.trog.dspam;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;


public class MyActivity extends AppCompatActivity {
    private final BackgroundExecutor backgroundExecutor = new BackgroundExecutor();
    private TcpClient tcpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final MyActivity self = this;

        final TextView text2 = (TextView) findViewById(R.id.textView2);
        final TextView text3 = (TextView) findViewById(R.id.textView3);

        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        addTab(host, R.string.tab1, R.id.tab1);
        addTab(host, R.string.tab2, R.id.tab2);
        addTab(host, R.string.tab3, R.id.tab3);

        final ListView listview = (ListView) findViewById(R.id.list);

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
            tcpClient = new TcpClient(new TcpClient.Listener<String>() {
                @Override
                public void onMessage(final String msg) {
                    text2.post(new Runnable() {
                        @Override
                        public void run() {
                            DspamLogs l = new DspamLogs();
                            Dspam d = l.parse(msg);
/*
                            final List<String> list = new ArrayList<String>();
                            for (int i = 0; i < d.dspam.size(); ++i) {
                                DspamEntry e = d.dspam.get(i);
                                Log.e("Debug", e.getFrom());
                                list.add(e.getFrom());
                            }
*/
                            HistoryListAdapter adapter = new HistoryListAdapter(self, d);
                            listview.setAdapter(adapter);
                        }
                    });
                }
            }, new TcpClient.Listener<String>() {
                @Override
                public void onMessage(final String msg) {
                    text3.post(new Runnable() {
                        @Override
                        public void run() {
                            text3.setText(msg);
                        }
                    });
                }
            });

            findViewById(R.id.buttonConnect).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("Debug", "onButtonConnect");
                    if (!tcpClient.isRunning()) {
                        backgroundExecutor.execute(tcpClient);
                    }
                }
            });

            findViewById(R.id.buttonSend).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("Debug", "onButtonSend");
                    if (tcpClient != null) {
                        tcpClient.sendMessage("{\"dspam\":0}");
                        Log.i("Debug", "onButtonSend2");
                    }
                }
            });
        }


        Log.i("Debug", " onCreate");
    }

    private void addTab(TabHost host, int titleId, int resId) {
        String title = getString(titleId);
        TabHost.TabSpec spec = host.newTabSpec(title);
        spec.setContent(resId);
        spec.setIndicator(title);
        host.addTab(spec);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_exit) {
            if (tcpClient != null) {
                backgroundExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        tcpClient.finish();
                    }
                });
            }
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }
}