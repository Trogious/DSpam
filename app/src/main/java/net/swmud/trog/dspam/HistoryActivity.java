package net.swmud.trog.dspam;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.google.gson.JsonSyntaxException;

public class HistoryActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        DspamLogs l = new DspamLogs();
        Dspam d = null;
        try {
            d = l.parse(getIntent().getStringExtra("history"));
        } catch (JsonSyntaxException e) {
        }

        if (d != null) {
            HistoryListAdapter adapter = new HistoryListAdapter(this, d);
            final ListView listview = (ListView) findViewById(R.id.list);
            listview.setAdapter(adapter);
        }
    }
}
