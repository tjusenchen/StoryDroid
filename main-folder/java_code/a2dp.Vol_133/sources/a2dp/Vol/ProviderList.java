package a2dp.Vol;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class ProviderList extends ListActivity {
    public static String EXTRA_PACKAGE_NAME = "extra_package_name";
    public static String EXTRA_PROVIDER = "extra_provider";
    public static final String KEY_ID = "_id";
    public static final int MI_TYPE_APPEND_VIEW = 2;
    public static final int MI_TYPE_CUSTOM = 3;
    public static final int MI_TYPE_STANDARD = 1;
    public static int PROVIDER_GOOGLE_LISTEN = 3;
    public static int PROVIDER_HOMESCREEN = 1;
    public static int PROVIDER_HOMESCREEN2 = 2;
    public static int PROVIDER_HTC_SENSE = 4;
    public static int PROVIDER_PANDORA = 0;
    public static final String[] P_CUSTOM_DATA_STRINGS = new String[]{null, null, null, "http://listen.googlelabs.com/listen?id=@@", null};
    public static final String[] P_DATA_KEYS = new String[]{"stationToken", "intent", "intent", "guid", "intent"};
    public static final String[] P_EMPTY_LIST_MSGS = new String[]{"It looks like you don't have any Pandora Radio stations set up. This usually means either Pandora is not installed or you haven't logged into it yet. Please try starting Pandora manually and make sure your stations show up, then try again.", "It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed).", "It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed).", "It looks like you don't have any subscriptions set up in Google's Listen. Please close AppAlarm and make sure your subscriptions show up in Listen.", "It looks like there was an error reading the shortcuts from your home screen (or you don't have any installed)."};
    public static final int[] P_MI_TYPES = new int[]{2, 1, 1, 3, 1};
    public static final String[] P_PACKAGE_NAMES = new String[]{"com.pandora.android", "com.android.launcher", "com.android.launcher2", "com.google.android.apps.listen", "com.htc.launcher"};
    public static final String[] P_TITLE_KEYS = new String[]{"stationName", "title", "title", "title", "title"};
    public static final String[] P_URI_STRINGS = new String[]{"content://com.pandora.provider/stations", "content://com.android.launcher.settings/favorites", "content://com.android.launcher2.settings/favorites", "content://com.google.android.apps.listen.PodcastProvider/item", "content://com.htc.launcher.settings/favorites"};
    public static final String[] P_WHERE_KEYS = new String[]{null, "intent!=\"\"", "intent!=\"\"", null, "intent!=\"\""};
    public static final String[] P_WINDOW_TITLES = new String[]{"Select a Pandora Favorite...", "Select a Shortcut from your Home Screen...", "Select a Shortcut from your Home Screen...", "Select a Feed from Google's Listen", "Select a Shortcut from your Home Screen..."};
    private CursorAdapter mListAdapter;
    private int mProvider;

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.pandora_station_list);
        super.onCreate(savedInstanceState);
        this.mProvider = getIntent().getIntExtra(EXTRA_PROVIDER, 0);
        setTitle(P_WINDOW_TITLES[this.mProvider]);
        ((TextView) getListView().getEmptyView()).setText(P_EMPTY_LIST_MSGS[this.mProvider]);
        loadList();
    }

    private void loadList() {
        Exception e;
        Cursor c;
        try {
            c = managedQuery(Uri.parse(P_URI_STRINGS[this.mProvider]), new String[]{KEY_ID, P_TITLE_KEYS[this.mProvider]}, P_WHERE_KEYS[this.mProvider], null, P_TITLE_KEYS[this.mProvider]);
            if (c == null) {
                try {
                    if (this.mProvider == PROVIDER_HOMESCREEN) {
                        Log.d("AppAlarm", "Error reading from Launcher1, trying Launcher2");
                        this.mProvider = PROVIDER_HOMESCREEN2;
                        loadList();
                        return;
                    } else if (this.mProvider == PROVIDER_HOMESCREEN2) {
                        Log.d("AppAlarm", "Error reading from Launcher2, trying Sense");
                        this.mProvider = PROVIDER_HTC_SENSE;
                        loadList();
                        return;
                    } else {
                        return;
                    }
                } catch (Exception e2) {
                    e = e2;
                }
            } else {
                this.mListAdapter = new SimpleCursorAdapter(this, R.layout.pandora_station_item, c, new String[]{P_TITLE_KEYS[this.mProvider]}, new int[]{R.id.psi_tv_station_name});
                setListAdapter(this.mListAdapter);
                return;
            }
        } catch (Exception e3) {
            e = e3;
            c = null;
        }
        e.printStackTrace();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setContentView(R.layout.pandora_station_list);
        super.onConfigurationChanged(newConfig);
        try {
            setListAdapter(this.mListAdapter);
        } catch (Exception e) {
        }
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor c = getContentResolver().query(Uri.parse(P_URI_STRINGS[this.mProvider]), new String[]{P_TITLE_KEYS[this.mProvider], P_DATA_KEYS[this.mProvider]}, "_id=" + id, null, null);
        c.moveToFirst();
        String title = c.getString(c.getColumnIndexOrThrow(P_TITLE_KEYS[this.mProvider]));
        String data = c.getString(c.getColumnIndexOrThrow(P_DATA_KEYS[this.mProvider]));
        c.close();
        Intent rtrIntent = new Intent();
        rtrIntent.putExtra("android.intent.extra.shortcut.NAME", title);
        if (!(this.mProvider == PROVIDER_HOMESCREEN || this.mProvider == PROVIDER_HOMESCREEN2 || this.mProvider == PROVIDER_HTC_SENSE)) {
            rtrIntent.putExtra(EXTRA_PACKAGE_NAME, P_PACKAGE_NAMES[this.mProvider]);
        }
        rtrIntent.putExtra("android.intent.extra.shortcut.INTENT", getSelectedIntent(data));
        setResult(-1, rtrIntent);
        finish();
    }

    private Intent getSelectedIntent(String data) {
        Intent intent = null;
        Intent nI;
        switch (P_MI_TYPES[this.mProvider]) {
            case 1:
                try {
                    return Intent.getIntent(data);
                } catch (URISyntaxException e) {
                    return intent;
                }
            case 2:
                nI = new Intent("android.intent.action.VIEW");
                nI.setData(Uri.withAppendedPath(Uri.parse(P_URI_STRINGS[this.mProvider]), data));
                return nI;
            case 3:
                nI = new Intent("android.intent.action.VIEW");
                String newData = null;
                try {
                    newData = P_CUSTOM_DATA_STRINGS[this.mProvider].replaceAll("@@", URLEncoder.encode(data, "UTF-8"));
                } catch (UnsupportedEncodingException e2) {
                    e2.printStackTrace();
                }
                Toast.makeText(getBaseContext(), newData, 1).show();
                nI.setData(Uri.parse(newData));
                return nI;
            default:
                return intent;
        }
    }
}
