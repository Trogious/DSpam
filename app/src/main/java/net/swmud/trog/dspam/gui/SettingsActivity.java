package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.Global;
import net.swmud.trog.dspam.core.Settings;

public class SettingsActivity extends Activity {
    private final SettingsActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Button saveButton = (Button) findViewById(R.id.buttonSveSettings);
        final TextView hostView = (TextView) findViewById(R.id.editHost);
        final TextView portView = (TextView) findViewById(R.id.editPort);
        final RadioButton loginCertificate = (RadioButton) findViewById(R.id.radioCertificate);
        final RadioButton loginPassword = (RadioButton) findViewById(R.id.radioPassword);
        final TextView passwordLabel = (TextView) findViewById(R.id.labelPassword);
        final TextView passwordView = (TextView) findViewById(R.id.editPassword);
        final TextView keyStoreLocationView = (TextView) findViewById(R.id.keystoreLocationView);
        final TextView keyStoreLocationLabel = (TextView) findViewById(R.id.labelKeyStore);

        Settings settings = Settings.loadSettings(this);
        hostView.setText(settings.getHost());
        int port = settings.getPort();
        portView.setText(port < 1 ? "" : ""+port);
        passwordView.setText(settings.getPassword());
        loginCertificate.setChecked(settings.isLoginWithCertificate());
        loginPassword.setChecked(!settings.isLoginWithCertificate());
        if (settings.isLoginWithCertificate()) {
            passwordLabel.setText("Key store & private key password:");
            keyStoreLocationView.setVisibility(View.VISIBLE);
            keyStoreLocationLabel.setVisibility(View.VISIBLE);
            keyStoreLocationView.setText(Environment.getExternalStorageDirectory().getPath());
        } else {
            passwordLabel.setText("Password:");
            keyStoreLocationView.setVisibility(View.GONE);
            keyStoreLocationLabel.setVisibility(View.GONE);
        }

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
                Log.d("SA", "pass: " + password);
                Settings.set(host, port, password, loginCertificate.isChecked()).save(self);
                Toast.makeText(self, "Settings saved.", Toast.LENGTH_SHORT).show();
            }
        });

        loginPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    passwordLabel.setText("Password:");
                    keyStoreLocationView.setVisibility(View.GONE);
                    keyStoreLocationLabel.setVisibility(View.GONE);
                }
            }
        });

        loginCertificate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    passwordLabel.setText("Key store & private key password:");
                    keyStoreLocationView.setVisibility(View.VISIBLE);
                    keyStoreLocationLabel.setVisibility(View.VISIBLE);
                    keyStoreLocationView.setText(Global.getKeyStoresLocation());
                }
            }
        });
    }
}
