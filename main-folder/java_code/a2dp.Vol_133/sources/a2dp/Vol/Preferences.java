package a2dp.Vol;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    public static final String PREFS_NAME = "btVol";
    private MyApplication application;

    public void onContentChanged() {
        stopService(new Intent(this, service.class));
        super.onContentChanged();
    }

    protected void onDestroy() {
        getSharedPreferences("btVol", 0).edit().commit();
        startService(new Intent(this, service.class));
        this.application = (MyApplication) getApplication();
        String IRun = "a2dp.vol.preferences.UPDATED";
        Intent i = new Intent();
        i.setAction("a2dp.vol.preferences.UPDATED");
        this.application.sendBroadcast(i);
        super.onDestroy();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
