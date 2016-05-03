package net.swmud.trog.dspam;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Settings {
    private static final String SETTINGS_FILE_NAME = "dspamsettings";
    private static final int SETTINGS_READ_SIZE = 1024;
    private static final String SETTING_SEPARATOR = "\n";

    private String host = "";
    private int port = 0;
    private String password = "";

    public Settings() {}

    public Settings(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public static String getAndroidId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    public static Settings loadSettings(Context context) {
        Settings settings = new Settings();
        settings.load(context);
        return settings;
    }

    public void load(Context context) {
        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(SETTINGS_FILE_NAME);
            byte buf[] = new byte[SETTINGS_READ_SIZE];
            int bytesRead = inputStream.read(buf);
            if (bytesRead > 0) {
                String settingsStr = new String(buf, 0, bytesRead, Constants.ENCODING);
                Log.e("LOAD", settingsStr);
                String setStr[] = settingsStr.split(SETTING_SEPARATOR);
                if (setStr != null && setStr.length > 2) {
                    host = setStr[0];
                    port = Integer.parseInt(setStr[1]);
                    password = Crypto.decrypt(getAndroidId(context), setStr[2]);
                }
            }
        } catch (FileNotFoundException e) {
            Log.d("SETT", e.getMessage());
        } catch (IOException e) {
            Log.d("SETT", e.getMessage());
        } catch (NumberFormatException e) {
            Log.d("SETT", e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.d("SETT", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d("SETT", e.getMessage());
        } catch (InvalidKeyException e) {
            Log.d("SETT", e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.d("SETT", e.getMessage());
        } catch (BadPaddingException e) {
            Log.d("SETT", e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void save(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(SETTING_SEPARATOR);
        sb.append(port);
        sb.append(SETTING_SEPARATOR);
        FileOutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(SETTINGS_FILE_NAME, Context.MODE_PRIVATE);
            sb.append(Crypto.encrypt(getAndroidId(context), password));
            outputStream.write(sb.toString().getBytes(Constants.ENCODING));
            outputStream.flush();
            Log.e("SAVE", sb.toString() + " flushed");
        } catch (FileNotFoundException e) {
            Log.d("SETT", e.getMessage());
        } catch (IOException e) {
            Log.d("SETT", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.d("SETT", e.getMessage());
        } catch (InvalidKeyException e) {
            Log.d("SETT", e.getMessage());
        } catch (NoSuchPaddingException e) {
            Log.d("SETT", e.getMessage());
        } catch (BadPaddingException e) {
            Log.d("SETT", e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.d("SETT", e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
