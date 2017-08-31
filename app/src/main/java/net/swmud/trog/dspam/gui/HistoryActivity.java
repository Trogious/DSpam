package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DspamHistory;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.RetrainResponse;

import java.util.List;

public class HistoryActivity extends Activity {
    int checkboxesVisible = View.GONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        processResponse();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processResponse();
    }

    private void processResponse() {
        String input = getIntent().getStringExtra("history");
        if (input != null) {
            DspamHistory history = new DspamHistory();
            List<DspamEntry> entries = null;
            try {
                entries = history.parse(input);
            } catch (JsonSyntaxException e) {
            }

            if (entries != null) {
                HistoryListAdapter adapter = new HistoryListAdapter(this, entries);
                final ListView listview = (ListView) findViewById(R.id.list);
                listview.setAdapter(adapter);
            }
        } else {
            input = getIntent().getStringExtra("retrained");
            if (input != null) {
                final ListView listview = (ListView) findViewById(R.id.list);
                HistoryListAdapter adapter = (HistoryListAdapter) listview.getAdapter();
                Gson gson = new Gson();
                RetrainResponse response = gson.fromJson(input, RetrainResponse.class);
                adapter.retrainEntries(response.result.ok);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
