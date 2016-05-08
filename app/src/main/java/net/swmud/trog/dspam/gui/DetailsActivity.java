package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.DateFormatter;
import net.swmud.trog.dspam.json.DspamEntry;
import net.swmud.trog.dspam.json.RetrainRequest;

public class DetailsActivity extends Activity {
    private final DetailsActivity self = this;

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

        final DspamEntry entry = (DspamEntry) getIntent().getSerializableExtra("entry");

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
                Gson gson = new Gson();
                String jsonStr = gson.toJson(retrainRequest);
                LaunchActivity.sendMessage(jsonStr);
                Toast.makeText(self, jsonStr, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
