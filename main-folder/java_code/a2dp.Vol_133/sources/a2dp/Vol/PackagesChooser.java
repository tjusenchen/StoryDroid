package a2dp.Vol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackagesChooser extends Activity {
    private MyApplication application;
    private List<AppInfoCache> mAppList;
    private Runnable mFinishLoadAndSortTask = new Runnable() {
        public void run() {
            PackagesChooser.this.initAssignListenersAndAdapter();
            PackagesChooser.this.pb.setVisibility(8);
        }
    };
    private List<AppInfoCache> mFullAppList;
    private final Handler mHandler = new Handler();
    private PackageListAdapter mListAdapter;
    private ListView mListView;
    private Runnable mLoadAppLoadAndSortAppList = new Runnable() {
        public void run() {
            PackagesChooser.this.mAppList = new ArrayList();
            for (ApplicationInfo appInfo : PackagesChooser.this.pm.getInstalledApplications(0)) {
                PackagesChooser.this.mAppList.add(new AppInfoCache(appInfo.loadLabel(PackagesChooser.this.pm).toString(), appInfo.packageName, appInfo.className));
            }
            Collections.sort(PackagesChooser.this.mAppList, new AlphaComparator());
            PackagesChooser.this.mFullAppList = new ArrayList();
            int i = 0;
            for (AppInfoCache appInfo2 : PackagesChooser.this.mAppList) {
                appInfo2.setPosition(i);
                appInfo2.setChecked(Arrays.asList(PackagesChooser.this.packages).contains(appInfo2.getPackageName()));
                PackagesChooser.this.mFullAppList.add(appInfo2);
                i++;
            }
            PackagesChooser.this.mListAdapter = new PackageListAdapter(PackagesChooser.this.getBaseContext());
            PackagesChooser.this.mHandler.post(PackagesChooser.this.mFinishLoadAndSortTask);
        }
    };
    String packagelist;
    private String[] packages;
    private ProgressBar pb;
    private PackageManager pm;
    SharedPreferences preferences;

    class AlphaComparator implements Comparator<AppInfoCache> {
        private final Collator sCollator = Collator.getInstance();

        AlphaComparator() {
        }

        public final int compare(AppInfoCache a, AppInfoCache b) {
            return this.sCollator.compare(a.getAppName(), b.getAppName());
        }
    }

    class AppInfoCache {
        private String app_name;
        private boolean checked;
        private String class_name;
        private String package_name;
        private int position = -1;

        public AppInfoCache(String aName, String pName, String cName) {
            this.app_name = aName;
            this.package_name = pName;
            this.class_name = cName;
            setChecked(false);
        }

        public Drawable getIcon() {
            try {
                return PackagesChooser.this.pm.getApplicationIcon(this.package_name);
            } catch (NameNotFoundException e) {
                return null;
            }
        }

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int pos) {
            this.position = pos;
        }

        public String getAppName() {
            return this.app_name;
        }

        public String getPackageName() {
            return this.package_name;
        }

        public String getClassName() {
            return this.class_name;
        }

        public String toString() {
            return this.app_name;
        }

        public boolean isChecked() {
            return this.checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }

    public class PackageListAdapter extends ArrayAdapter<AppInfoCache> {
        Context c;

        public PackageListAdapter(Context context) {
            super(context, R.layout.activity_packages_chooser, PackagesChooser.this.mAppList);
            this.c = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(this.c).inflate(R.layout.package_list_item, parent, false);
            TextView tv_name = (TextView) v.findViewById(R.id.pi_tv_name);
            final AppInfoCache ai = (AppInfoCache) getItem(position);
            ((ImageView) v.findViewById(R.id.pi_iv_icon)).setImageDrawable(ai.getIcon());
            tv_name.setText(ai.getAppName());
            final CheckBox box = (CheckBox) v.findViewById(R.id.checkBox1);
            box.setChecked(ai.isChecked());
            box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ai.setChecked(box.isChecked());
                }
            });
            return v;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packages_chooser);
        setupActionBar();
        this.application = (MyApplication) getApplication();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.application);
        this.packagelist = this.preferences.getString("packages", "com.google.android.talk,com.android.email,com.android.calendar");
        this.packages = this.packagelist.split(",");
        this.pm = getPackageManager();
        this.pb = (ProgressBar) findViewById(R.id.progressBar1);
        this.pb.setIndeterminate(true);
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.packages_chooser, menu);
        this.mListView = (ListView) findViewById(R.id.PackagelistView1);
        new Thread(this.mLoadAppLoadAndSortAppList).start();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                cleanup();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initAssignListenersAndAdapter() {
        this.mListView.setAdapter(this.mListAdapter);
    }

    private void cleanup() {
        this.packagelist = "";
        int i = 0;
        if (this.mFullAppList == null) {
            this.packagelist = "";
        } else if (this.mFullAppList.isEmpty()) {
            this.packagelist = "";
        } else {
            for (AppInfoCache info : this.mFullAppList) {
                if (info.isChecked()) {
                    if (i > 0) {
                        this.packagelist += ",";
                    }
                    this.packagelist += info.getPackageName();
                }
                i++;
            }
        }
        Editor editor = this.preferences.edit();
        editor.putString("packages", this.packagelist);
        editor.commit();
        Intent intent = new Intent();
        intent.setAction("a2dp.vol.Reload");
        this.application.sendBroadcast(intent);
    }

    public void onBackPressed() {
        cleanup();
        super.onBackPressed();
    }
}
