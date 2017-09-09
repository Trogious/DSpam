package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DateFormatter;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.JsonRpc;
import net.swmud.trog.dspam.json.RetrainRequest;
import net.swmud.trog.dspam.json.RetrainResponse;

public class DetailsActivity extends Activity {
    private static final String ARG_RETRAINED1 = "retrained1";
    private final DetailsActivity self = this;
    private DspamEntry entry;
    static final String ARG_ENTRY = "entry";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final TextView from = (TextView) findViewById(R.id.from);
        final TextView date = (TextView) findViewById(R.id.date);
        final TextView signature = (TextView) findViewById(R.id.signature);
        final TextView signatureCount = (TextView) findViewById(R.id.signatureCount);
        final TextView spamStatus = (TextView) findViewById(R.id.spamStatus);
        final TextView deliveryStatus = (TextView) findViewById(R.id.deliveryStatus);
        final TextView subject = (TextView) findViewById(R.id.subject);
        final TextView messageId = (TextView) findViewById(R.id.messageId);
        final Button retrain = (Button) findViewById(R.id.retrainButton);

        entry = (DspamEntry) getIntent().getSerializableExtra(ARG_ENTRY);

        from.setText(entry.getFrom());
        date.setText(DateFormatter.format(entry.getDate()));
        signature.setText(entry.getSignature());
        signatureCount.setText(""+entry.getSignatureCount());
        spamStatus.setText(entry.getSpamStatus().toString());
        deliveryStatus.setText(entry.getDeliveryStatus());
        subject.setText(entry.getSubject());
        messageId.setText(entry.getMsgId());

        retrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrainRequest retrainRequest = new RetrainRequest(entry);
                JsonRpc.JsonRequest request = retrainRequest.getJsonRpcRequest();
                LaunchActivity.sendMessage(request, ARG_RETRAINED1, DetailsActivity.class);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (entry != null) {
            final String input = getIntent().getStringExtra(ARG_RETRAINED1);
            RetrainResponse response = null;
            try {
                response = new Gson().fromJson(input, RetrainResponse.class);
            } catch (JsonSyntaxException e) {
                Toast.makeText(this, getString(R.string.parsing_argument_failed, ARG_RETRAINED1, e.getLocalizedMessage()), Toast.LENGTH_LONG).show();
            }

            if (response != null && response.result.ok.size() > 0) {
                if (response.result.ok.getFirst().equals(entry.getSignature())) {
                    final TextView spamStatus = (TextView) findViewById(R.id.spamStatus);
                    spamStatus.setText("RETRAINED");
                }
            }
        }
    }
}
