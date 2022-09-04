package io.github.herrherklotz.chameleon;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import io.github.herrherklotz.chameleon.helper.jackson.MyObjectMapper;


public class Chameleon extends Application {
    private static MyObjectMapper sObjectMapper = new MyObjectMapper();
    public final static String sTAG = "Chameleon";
    public final static String sAppName = "LabDroid";
    public final static DateFormat sTIMEFORMAT_MS = new SimpleDateFormat("HH:mm:ss.SSS");
    public final static DateFormat sTIMEFORMAT_MSS = new SimpleDateFormat("HH:mm:ss.SSSS");
    public static Path sPATH;


    public static void LOG_D(Object pObject, String pMethod) {
        LOG_D(pObject, pMethod, "");
    }


    public static void LOG_D(Object pObject, String pMethod, String pMessage) {
        if (BuildConfig.DEBUG)
            Log.d(sTAG, pObject.getClass().getSimpleName() + "." + pMethod + " " + pMessage);
    }


    public static void LOG_D(String pMessage) {
        if (BuildConfig.DEBUG)
            Log.d(sTAG, pMessage);
    }


    public static void LOG_D(String pClass, String pMethod) {
        LOG_D(pClass, pMethod, "");
    }


    public static void LOG_D(String pClass, String pMethod, String pMessage) {
        if (BuildConfig.DEBUG)
            Log.d(sTAG, pClass + "." + pMethod + " " + pMessage);
    }


    private void checkFirstRun() {
        final String PREFS_NAME = "ChameleonPreferences";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;
        // Get current version code
        int lCurrentVersionCode = BuildConfig.VERSION_CODE;
        // Get saved version code
        SharedPreferences lSharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lSavedVersionCode = lSharedPreferences.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (lCurrentVersionCode == lSavedVersionCode) // RERUN
            return;
        else if (lSavedVersionCode == DOESNT_EXIST) {
            // FIRST RUN
			/* TODO copy assets folder into getFilesDir()
			new File(getApplicationContext().getFilesDir(), "/hw/").mkdir();
			new File(getApplicationContext().getFilesDir(), "/com/").mkdir();
			new File(getApplicationContext().getFilesDir(), "/hw/sensor/").mkdir();
			new File(getApplicationContext().getFilesDir(), "/com/server/websocket/").mkdir();
			
			try {
				new File(getApplicationContext().getFilesDir(), "/hw/sensor/accelerometer.json").createNewFile();
				new File(getApplicationContext().getFilesDir(), "/hw/vibrator.json").createNewFile();
				new File(getApplicationContext().getFilesDir(), "/com/server/websocket/websocket.json").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			*/
        } else if (lCurrentVersionCode > lSavedVersionCode) {
            // UPDATE
            /* TODO
             * 1. backup system.json assets folder into getFilesDir()
             * 2. clear getFilesDir()
             * 3. copy assets folder into getFilesDir()
             * 4. restore system.json
             */
        }

        // Update the shared preferences with the current version code
        lSharedPreferences.edit().putInt(PREF_VERSION_CODE_KEY, lCurrentVersionCode).apply();
    }


    public void onCreate() {
        super.onCreate();
        sPATH = Paths.get(getApplicationContext().getFilesDir().getAbsolutePath());
        this.checkFirstRun();
    }


    public static boolean save(Path path, Object value) {
        Path directory = path.getParent();

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            if (value instanceof String)
                Files.write(path, ((String) value).getBytes());
            else
                Files.write(path, sObjectMapper.writeValueAsBytes(value));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}