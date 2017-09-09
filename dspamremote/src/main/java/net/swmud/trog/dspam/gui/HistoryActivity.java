package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DspamHistory;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.RetrainResponse;

import java.util.List;

public class HistoryActivity extends Activity {
    static final String ARG_HISTORY = "history";
    static final String ARG_RETRAINED = "retrained";
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
        String input = getIntent().getStringExtra(ARG_HISTORY);
        if (input != null) {
            DspamHistory history = new DspamHistory();
            List<DspamEntry> entries = null;
            try {
                entries = history.parse(input);
            } catch (JsonSyntaxException e) {
                Toast.makeText(this, getString(R.string.parsing_argument_failed, ARG_HISTORY, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
            }

            if (entries != null) {
                HistoryListAdapter adapter = new HistoryListAdapter(this, entries);
                final ListView listview = (ListView) findViewById(R.id.list);
                listview.setAdapter(adapter);
            }
        } else {
            input = getIntent().getStringExtra(ARG_RETRAINED);
            if (input != null) {
                final ListView listview = (ListView) findViewById(R.id.list);
                HistoryListAdapter adapter = (HistoryListAdapter) listview.getAdapter();
                RetrainResponse response = null;
                try {
                    response = new Gson().fromJson(input, RetrainResponse.class);
                } catch (JsonSyntaxException e) {
                    Toast.makeText(this, getString(R.string.parsing_argument_failed, ARG_RETRAINED, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
                }
                if (response != null) {
                    adapter.retrainEntries(response.result.ok);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
