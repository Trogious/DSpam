package net.swmud.trog.dspam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private final SettingsActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Button saveButton = (Button) findViewById(R.id.buttonSveSettings);
        final TextView hostView = (TextView) findViewById(R.id.editHost);
        final TextView portView = (TextView) findViewById(R.id.editPort);
        final TextView passwordView = (TextView) findViewById(R.id.editPassword);

        Settings settings = Settings.loadSettings(this);
        hostView.setText(settings.getHost());
        portView.setText(""+settings.getPort());
        passwordView.setText(passwordView.getText());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = hostView.getText().toString();
                String portStr = portView.getText().toString();
                int port = 0;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                }
                String password = passwordView.getText().toString();

                new Settings(host, port, password).save(self);
                Toast.makeText(self, "Settings saved.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
