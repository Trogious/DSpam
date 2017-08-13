package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.gson.JsonSyntaxException;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DspamHistory;
import net.swmud.trog.dspam.json.DspamEntry;

import java.util.List;

public class HistoryActivity extends Activity {
    int checkboxesVisible = View.GONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        DspamHistory history = new DspamHistory();
        List<DspamEntry> entries = null;
        try {
            entries = history.parse(getIntent().getStringExtra("history"));
        } catch (JsonSyntaxException e) {
        }

        if (entries != null) {
            HistoryListAdapter adapter = new HistoryListAdapter(this, entries);
            final ListView listview = (ListView) findViewById(R.id.list);
            listview.setAdapter(adapter);
        }
    }
}
