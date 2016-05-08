package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.gson.JsonSyntaxException;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DspamLogs;
import net.swmud.trog.dspam.json.Dspam;

public class HistoryActivity extends Activity {
    int checkboxesVisible = View.GONE;

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
