package a2dp.Vol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppChooser extends Activity {
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    private List<AppInfoCache> mAppList;
    private Button mBtnClear;
    private Button mBtnSearch;
    public OnClickListener mClearBtnListenerListner = new OnClickListener() {
        public void onClick(View v) {
            AppChooser.this.mEtFilter.setText("");
            AppChooser.this.doListFilter();
        }
    };
    private EditText mEtFilter;
    private String mFilterText;
    private Runnable mFinishLoadAndSortTask = new Runnable() {
        public void run() {
            AppChooser.this.initAssignListenersAndAdapter();
            AppChooser.this.mLoadingDialog.dismiss();
        }
    };
    private List<AppInfoCache> mFullAppList;
    private final Handler mHandler = new Handler();
    private PackageListAdapter mListAdapter;
    public OnItemClickListener mListItemClickAdapter = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent i = new Intent();
            i.putExtra(AppChooser.EXTRA_PACKAGE_NAME, ((AppInfoCache) AppChooser.this.mAppList.get(position)).getPackageName());
            AppChooser.this.setResult(-1, i);
            AppChooser.this.finish();
        }
    };
    private ListView mListView;
    private Runnable mLoadAppLoadAndSortAppList = new Runnable() {
        public void run() {
            AppChooser.this.mAppList = new ArrayList();
            for (ApplicationInfo appInfo : AppChooser.this.pm.getInstalledApplications(0)) {
                AppChooser.this.mAppList.add(new AppInfoCache(appInfo.loadLabel(AppChooser.this.pm).toString(), appInfo.packageName, appInfo.className));
            }
            Collections.sort(AppChooser.this.mAppList, new AlphaComparator());
            AppChooser.this.mFullAppList = new ArrayList();
            int i = 0;
            for (AppInfoCache appInfo2 : AppChooser.this.mAppList) {
                appInfo2.setPosition(i);
                AppChooser.this.mFullAppList.add(appInfo2);
                i++;
            }
            AppChooser.this.mListAdapter = new PackageListAdapter(AppChooser.this.getBaseContext());
            AppChooser.this.mHandler.post(AppChooser.this.mFinishLoadAndSortTask);
        }
    };
    private ProgressDialog mLoadingDialog;
    public OnEditorActionListener mSearchActionListener = new OnEditorActionListener() {
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            AppChooser.this.doListFilter();
            return false;
        }
    };
    public OnKeyListener mSearchBoxKeyListener = new OnKeyListener() {
        public boolean onKey(View arg0, int keycode, KeyEvent arg2) {
            if (keycode != 66) {
                return false;
            }
            AppChooser.this.doListFilter();
            return true;
        }
    };
    public OnClickListener mSearchBtnListenerListner = new OnClickListener() {
        public void onClick(View v) {
            AppChooser.this.doListFilter();
        }
    };
    private PackageManager pm;

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
        private String class_name;
        private String package_name;
        private int position = -1;

        public AppInfoCache(String aName, String pName, String cName) {
            this.app_name = aName;
            this.package_name = pName;
            this.class_name = cName;
        }

        public Drawable getIcon() {
            try {
                return AppChooser.this.pm.getApplicationIcon(this.package_name);
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
    }

    public class PackageListAdapter extends ArrayAdapter<AppInfoCache> {
        Context c;

        public PackageListAdapter(Context context) {
            super(context, R.layout.app_list_item, AppChooser.this.mAppList);
            this.c = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(this.c).inflate(R.layout.app_list_item, parent, false);
            TextView tv_name = (TextView) v.findViewById(R.id.pi_tv_name);
            AppInfoCache ai = (AppInfoCache) getItem(position);
            ((ImageView) v.findViewById(R.id.pi_iv_icon)).setImageDrawable(ai.getIcon());
            tv_name.setText(ai.getAppName());
            return v;
        }
    }

    public void doListFilter() {
        this.mFilterText = this.mEtFilter.getText().toString().toLowerCase();
        this.mAppList.clear();
        if (this.mFilterText.contentEquals("")) {
            for (AppInfoCache appInfo : this.mFullAppList) {
                this.mAppList.add(appInfo);
            }
        } else {
            for (AppInfoCache appInfo2 : this.mFullAppList) {
                if (appInfo2.getAppName().toLowerCase().contains(this.mFilterText)) {
                    this.mAppList.add(appInfo2);
                }
            }
        }
        this.mListAdapter.notifyDataSetChanged();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);
        setTitle("Select an app...");
        initAttachViewsToVars();
        this.pm = getPackageManager();
        this.mLoadingDialog = new ProgressDialog(this);
        this.mLoadingDialog.setIndeterminate(true);
        this.mLoadingDialog.setMessage("Loading App List...");
        this.mLoadingDialog.setCancelable(false);
        this.mLoadingDialog.show();
        new Thread(this.mLoadAppLoadAndSortAppList).start();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setContentView(R.layout.app_list);
        initAttachViewsToVars();
        initAssignListenersAndAdapter();
        super.onConfigurationChanged(newConfig);
    }

    private void initAttachViewsToVars() {
        this.mListView = (ListView) findViewById(R.id.m_lv_packages);
        this.mEtFilter = (EditText) findViewById(R.id.m_et_search);
        this.mBtnSearch = (Button) findViewById(R.id.m_btn_search);
        this.mBtnClear = (Button) findViewById(R.id.m_btn_clear);
    }

    private void initAssignListenersAndAdapter() {
        this.mEtFilter.setText(this.mFilterText);
        this.mEtFilter.setOnEditorActionListener(this.mSearchActionListener);
        this.mEtFilter.setOnKeyListener(this.mSearchBoxKeyListener);
        this.mListView.setAdapter(this.mListAdapter);
        this.mListView.setOnItemClickListener(this.mListItemClickAdapter);
        this.mBtnSearch.setOnClickListener(this.mSearchBtnListenerListner);
        this.mBtnClear.setOnClickListener(this.mClearBtnListenerListner);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
    }
}
