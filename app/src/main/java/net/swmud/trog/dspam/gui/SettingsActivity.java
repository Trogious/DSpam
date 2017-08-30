package net.swmud.trog.dspam.gui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.swmud.trog.dspam.R;
import net.swmud.trog.dspam.core.Global;
import net.swmud.trog.dspam.core.Settings;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
        final TextView preferredCertLabel = (TextView) findViewById(R.id.labelPreferredCert);
        final Spinner preferredCertSpinner = (Spinner) findViewById(R.id.spinnerPreferredCert);

        final Settings settings = Settings.loadSettings(this);
        hostView.setText(settings.getHost());
        final int port = settings.getPort();
        portView.setText(port < 1 ? "" : "" + port);
        passwordView.setText(settings.getPassword());
        loginCertificate.setChecked(settings.isLoginWithCertificate());
        loginPassword.setChecked(!settings.isLoginWithCertificate());
        final List<String> aliases = getCertAliases();
        int preferredCertAliasIndex = aliases.indexOf(settings.getPreferredCertificateAlias());
        if (preferredCertAliasIndex < 0) {
            preferredCertAliasIndex = 0;
        } else {
            ++preferredCertAliasIndex;
        }
        aliases.add(0, "");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aliases);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preferredCertSpinner.setAdapter(dataAdapter);
        preferredCertSpinner.setSelection(preferredCertAliasIndex);

        if (settings.isLoginWithCertificate()) {
            passwordLabel.setText("Key store & private key password:");
            keyStoreLocationView.setVisibility(View.VISIBLE);
            keyStoreLocationLabel.setVisibility(View.VISIBLE);
            keyStoreLocationView.setText(Environment.getExternalStorageDirectory().getPath());
            if (aliases.size() > 0) {
                preferredCertLabel.setVisibility(View.VISIBLE);
                preferredCertSpinner.setVisibility(View.VISIBLE);
            } else {
                preferredCertLabel.setVisibility(View.GONE);
                preferredCertSpinner.setVisibility(View.GONE);
            }
        } else {
            passwordLabel.setText("Password:");
            keyStoreLocationView.setVisibility(View.GONE);
            keyStoreLocationLabel.setVisibility(View.GONE);
            preferredCertLabel.setVisibility(View.GONE);
            preferredCertSpinner.setVisibility(View.GONE);
        }

        loginPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    passwordLabel.setText("Password:");
                    keyStoreLocationView.setVisibility(View.GONE);
                    keyStoreLocationLabel.setVisibility(View.GONE);
                    preferredCertLabel.setVisibility(View.GONE);
                    preferredCertSpinner.setVisibility(View.GONE);
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
                    if (aliases.size() > 0) {
                        preferredCertLabel.setVisibility(View.VISIBLE);
                        preferredCertSpinner.setVisibility(View.VISIBLE);
                    } else {
                        preferredCertLabel.setVisibility(View.GONE);
                        preferredCertSpinner.setVisibility(View.GONE);
                    }
                }
            }
        });

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
                String preferredCertAlias = (String)preferredCertSpinner.getSelectedItem();
                Settings.set(host, port, password, loginCertificate.isChecked(), preferredCertAlias).save(self);
                Toast.makeText(self, "Settings saved.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private List<String> getCertAliases() {
        List<String> aliasesList = new LinkedList<>();

        Enumeration<String> aliases = null;
        try {
            KeyStore keyStore = Global.keyStores.getClientKeyStore();
            if (null != keyStore) {
                aliases = keyStore.aliases();
            }
        } catch (KeyStoreException e) {
            Log.d("SA", "getCertAliases: " + e.getMessage());
        }

        if (null != aliases) {
            while (aliases.hasMoreElements()) {
                aliasesList.add(aliases.nextElement());
            }
            Collections.sort(aliasesList);
        }

        aliases = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            if (null != keyStore) {
                keyStore.load(null);
                aliases = keyStore.aliases();
            }
        } catch (IOException e) {
            Log.d("SA", "getCertAliases: " + e.getMessage());
        } catch (CertificateException e) {
            Log.d("SA", "getCertAliases: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d("SA", "getCertAliases: " + e.getMessage());
        } catch (KeyStoreException e) {
            Log.d("SA", "getCertAliases: " + e.getMessage());
        }

        if (null != aliases) {
            while (aliases.hasMoreElements()) {
                aliasesList.add(aliases.nextElement());
            }
        }

        return aliasesList;
    }
}
