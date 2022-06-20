package a2dp.Vol;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Vector;

public class main extends Activity {
    static final int CHECK_TTS = 3;
    static final int EDITED_DATA = 4;
    static final int ENABLE_BLUETOOTH = 1;
    private static final String LOG_TAG = "A2DP_Volume";
    public static final String PREFS_NAME = "btVol";
    static final int RELOAD = 2;
    static AudioManager am = ((AudioManager) null);
    private static int resourceID = 17367043;
    static Button serv;
    boolean TTSignore = false;
    private String a2dpDir = "";
    String activebt = null;
    private MyApplication application;
    boolean carMode = false;
    int connects;
    boolean enableTTS = false;
    boolean headsetPlug = false;
    boolean homeDock = false;
    ArrayAdapter<String> ladapt;
    String[] lstring = null;
    ListView lvl = null;
    private final BroadcastReceiver mReceiver5 = new BroadcastReceiver() {
        public void onReceive(Context context2, Intent intent2) {
            main.this.getConnects();
            main.this.refreshList(main.this.loadFromDB());
        }
    };
    private final BroadcastReceiver mReceiver6 = new BroadcastReceiver() {
        public void onReceive(Context context2, Intent intent2) {
            boolean carModeOld = main.this.carMode;
            boolean homeDockOld = main.this.homeDock;
            boolean headsetPlugOld = main.this.headsetPlug;
            boolean powerOld = main.this.power;
            try {
                main.this.carMode = main.this.preferences.getBoolean("car_mode", false);
                main.this.homeDock = main.this.preferences.getBoolean("home_dock", false);
                main.this.headsetPlug = main.this.preferences.getBoolean("headset", false);
                main.this.power = main.this.preferences.getBoolean("power", false);
                main.this.enableTTS = main.this.preferences.getBoolean("enableTTS", false);
                if (main.this.preferences.getBoolean("useLocalStorage", false)) {
                    main.this.a2dpDir = main.this.getFilesDir().toString();
                } else {
                    main.this.a2dpDir = Environment.getExternalStorageDirectory() + "/A2DPVol";
                }
                File exportDir = new File(main.this.a2dpDir);
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(main.LOG_TAG, "error" + e2.getMessage());
            }
            if ((!carModeOld && main.this.carMode) || ((!homeDockOld && main.this.homeDock) || ((!headsetPlugOld && main.this.headsetPlug) || (!powerOld && main.this.power)))) {
                main.this.getBtDevices(0);
            }
            if (main.this.enableTTS) {
                try {
                    Intent checkIntent = new Intent();
                    checkIntent.setAction("android.speech.tts.engine.CHECK_TTS_DATA");
                    main.this.startActivityForResult(checkIntent, 3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Set<String> list = NotificationManagerCompat.getEnabledListenerPackages(main.this.getBaseContext());
                Boolean listenerEnabled = Boolean.valueOf(false);
                for (String item : list) {
                    if (item.equalsIgnoreCase(BuildConfig.APPLICATION_ID)) {
                        listenerEnabled = Boolean.valueOf(true);
                    }
                }
                if (main.this.preferences.getBoolean("enableGTalk", false) && !listenerEnabled.booleanValue()) {
                    main.this.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                }
            }
        }
    };
    private DeviceDB myDB;
    boolean power = false;
    SharedPreferences preferences;
    Resources res;
    private final BroadcastReceiver sRunning = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            try {
                if (service.run) {
                    main.this.servrun = true;
                    main.serv.setText(R.string.StopService);
                    main.this.getConnects();
                } else {
                    main.this.servrun = false;
                    main.serv.setText(R.string.StartService);
                    main.this.connects = 0;
                }
            } catch (Exception x) {
                x.printStackTrace();
                main.this.servrun = false;
                main.serv.setText(R.string.StartService);
                main.this.connects = 0;
                Log.e(main.LOG_TAG, "error" + x.getMessage());
            }
            main.this.refreshList(main.this.loadFromDB());
        }
    };
    boolean servrun = false;
    boolean toasts = true;
    Vector<btDevice> vec = new Vector();

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.prefs:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.DelData:
                Builder builder = new Builder(this);
                builder.setMessage(R.string.DeleteDataMsg).setCancelable(false).setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        main.this.myDB.deleteAll();
                        main.this.refreshList(main.this.loadFromDB());
                    }
                }).setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
                return true;
            case R.id.Manage_data:
                this.myDB.getDb().close();
                startActivityForResult(new Intent(getBaseContext(), ManageData.class), 2);
                return true;
            case R.id.Exit:
                stopService(new Intent(this, service.class));
                finish();
                return true;
            case R.id.help:
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/jroal/a2dpvolume/wiki")));
                return true;
            case R.id.packages:
                startActivity(new Intent(this, PackagesChooser.class));
                return true;
            default:
                return false;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        this.res = getResources();
        setContentView(R.layout.main);
        String ver = null;
        try {
            ver = getPackageManager().getPackageInfo(new ComponentName(BuildConfig.APPLICATION_ID, "main").getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(LOG_TAG, "error" + e.getMessage());
        }
        setTitle(this.res.getString(R.string.app_name) + " Version: " + ver);
        this.application = (MyApplication) getApplication();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.application);
        try {
            if (this.preferences.getBoolean("useLocalStorage", false)) {
                this.a2dpDir = getFilesDir().toString();
            } else {
                this.a2dpDir = Environment.getExternalStorageDirectory() + "/A2DPVol";
            }
            File exportDir = new File(this.a2dpDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            this.carMode = this.preferences.getBoolean("car_mode", true);
            this.homeDock = this.preferences.getBoolean("home_dock", false);
            this.headsetPlug = this.preferences.getBoolean("headset", false);
            this.power = this.preferences.getBoolean("power", false);
            this.enableTTS = this.preferences.getBoolean("enableTTS", false);
            this.toasts = this.preferences.getBoolean("toasts", true);
            this.TTSignore = this.preferences.getBoolean("TTSignore", false);
        } catch (Exception e2) {
            Log.e(LOG_TAG, "error" + e2.getMessage());
        }
        this.connects = 0;
        am = (AudioManager) getSystemService("audio");
        Button btn = (Button) findViewById(R.id.Button01);
        Button locbtn = (Button) findViewById(R.id.Locationbtn);
        serv = (Button) findViewById(R.id.ServButton);
        IntentFilter intentFilter = new IntentFilter("a2dp.vol.service.RUNNING");
        try {
            registerReceiver(this.sRunning, intentFilter);
        } catch (Exception e22) {
            e22.printStackTrace();
        }
        intentFilter = new IntentFilter("a2dp.vol.service.STOPPED_RUNNING");
        try {
            registerReceiver(this.sRunning, intentFilter);
        } catch (Exception e222) {
            e222.printStackTrace();
        }
        intentFilter = new IntentFilter("a2dp.Vol.main.RELOAD_LIST");
        registerReceiver(this.mReceiver5, intentFilter);
        intentFilter = new IntentFilter("a2dp.vol.preferences.UPDATED");
        registerReceiver(this.mReceiver6, intentFilter);
        this.lstring = new String[]{this.res.getString(R.string.NoData)};
        this.myDB = new DeviceDB(this.application);
        if (savedInstanceState == null) {
            int devicemin = 1;
            if (this.carMode) {
                devicemin = 1 + 1;
            }
            if (this.homeDock) {
                devicemin++;
            }
            try {
                if (this.myDB.getLength() < devicemin) {
                    getBtDevices(1);
                }
            } catch (Exception e1) {
                Log.e(LOG_TAG, "error" + e1.getMessage());
            }
            serv.setText(R.string.StartService);
            startService(new Intent(this, service.class));
            if (this.enableTTS) {
                try {
                    Intent checkIntent = new Intent();
                    checkIntent.setAction("android.speech.tts.engine.CHECK_TTS_DATA");
                    startActivityForResult(checkIntent, 3);
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            startService(new Intent(this, NotificationCatcher.class));
        }
        this.ladapt = new ArrayAdapter(this.application, resourceID, this.lstring);
        this.lvl = (ListView) findViewById(R.id.ListView01);
        this.lvl.setAdapter(this.ladapt);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                main.this.getBtDevices(1);
            }
        });
        this.lvl.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (main.this.vec.isEmpty()) {
                    return false;
                }
                String mesg;
                BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
                btDevice bt = new btDevice();
                bt = (btDevice) main.this.vec.get(position);
                BluetoothDevice btd = null;
                if (mBTA != null) {
                    for (BluetoothDevice device : mBTA.getBondedDevices()) {
                        if (device.getAddress().equalsIgnoreCase(bt.mac)) {
                            btd = device;
                        }
                    }
                }
                Builder builder = new Builder(main.this);
                builder.setTitle(bt.toString());
                final String car = bt.toString();
                if (btd != null) {
                    mesg = bt.desc1 + "\n" + bt.mac + "\n" + main.this.res.getString(R.string.Bonded);
                    switch (btd.getBondState()) {
                        case ExploreByTouchHelper.INVALID_ID /*-2147483648*/:
                            mesg = mesg + " = " + main.this.res.getString(R.string.Error);
                            break;
                        case 10:
                            mesg = mesg + " = " + main.this.res.getString(R.string.NotBonded);
                            break;
                        case 11:
                            mesg = mesg + " = " + main.this.res.getString(R.string.Bonding);
                            break;
                        case MotionEventCompat.AXIS_RX /*12*/:
                            mesg = mesg + " = " + main.this.res.getString(R.string.Bonded);
                            break;
                    }
                    mesg = ((mesg + "\n" + main.this.res.getString(R.string.Class) + " = " + main.this.getBTClassDev(btd)) + "\nMajor " + main.this.res.getString(R.string.Class) + " = " + main.this.getBTClassDevMaj(btd)) + "\nService " + main.this.res.getString(R.string.Class) + " = " + main.this.getBTClassServ(btd);
                } else {
                    mesg = (String) main.this.getText(R.string.btNotOn);
                }
                builder.setMessage(mesg);
                builder.setPositiveButton("OK", null);
                builder.setNeutralButton(R.string.LocationString, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        File exportDir = new File(main.this.a2dpDir);
                        if (exportDir.exists()) {
                            Uri uri = Uri.parse(new String("file:///" + exportDir.getPath() + "/" + car.replaceAll(" ", "_") + ".html").trim());
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.setDataAndType(uri, "text/html");
                            try {
                                PackageInfo pi = main.this.getPackageManager().getPackageInfo("com.android.chrome", 0);
                                intent.setClassName("com.android.chrome", "com.google.android.apps.chrome.Main");
                            } catch (NameNotFoundException e1) {
                                intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
                                e1.printStackTrace();
                            }
                            try {
                                main.this.startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(main.this.application, e.toString(), 1).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
        this.lvl.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!main.this.vec.isEmpty()) {
                    final btDevice bt = (btDevice) main.this.vec.get(position);
                    final btDevice bt2 = main.this.myDB.getBTD(bt.mac);
                    Builder builder = new Builder(main.this);
                    builder.setTitle(bt.toString());
                    builder.setMessage(bt2.desc1 + "\n" + bt2.desc2 + "\n" + bt2.mac);
                    builder.setPositiveButton(17039370, null);
                    builder.setNegativeButton(R.string.Delete, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            main.this.myDB.delete(bt2);
                            main.this.refreshList(main.this.loadFromDB());
                        }
                    });
                    builder.setNeutralButton(R.string.Edit, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(main.this, EditDevice.class);
                            i.putExtra("btd", bt.mac);
                            main.this.startActivityForResult(i, 4);
                        }
                    });
                    builder.show();
                }
            }
        });
        locbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                main.this.Locationbtn();
            }
        });
        locbtn.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                try {
                    byte[] buff = new byte[250];
                    FileInputStream fs = main.this.openFileInput("My_Last_Location2");
                    fs.read(buff);
                    fs.close();
                    String st = new String(buff).trim();
                    Toast.makeText(main.this, st, 1).show();
                    main.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(st)));
                } catch (FileNotFoundException e) {
                    Toast.makeText(main.this, R.string.NoData, 1).show();
                    Log.e(main.LOG_TAG, "error" + e.getMessage());
                } catch (IOException e2) {
                    Toast.makeText(main.this, "Some IO issue", 1).show();
                    Log.e(main.LOG_TAG, "error" + e2.getMessage());
                }
                return false;
            }
        });
        serv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (main.this.servrun) {
                    main.this.stopService(new Intent(main.this, service.class));
                } else {
                    main.this.startService(new Intent(main.this, service.class));
                }
            }
        });
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (service.run) {
                        main.this.servrun = true;
                        main.serv.setText(R.string.StopService);
                        return;
                    }
                    main.this.servrun = false;
                    main.serv.setText(R.string.StartService);
                } catch (Exception x) {
                    main.this.servrun = false;
                    main.serv.setText(R.string.StartService);
                    Log.e(main.LOG_TAG, "error" + x.getMessage());
                }
            }

            public void onFinish() {
                try {
                    if (service.run) {
                        main.this.servrun = true;
                        main.serv.setText(R.string.StopService);
                        main.this.getConnects();
                        main.this.refreshList(main.this.loadFromDB());
                        return;
                    }
                    main.this.servrun = false;
                    main.serv.setText(R.string.StartService);
                } catch (Exception x) {
                    main.this.servrun = false;
                    main.serv.setText(R.string.StartService);
                    Log.e(main.LOG_TAG, "error" + x.getMessage());
                }
            }
        }.start();
        getConnects();
        refreshList(loadFromDB());
        super.onCreate(savedInstanceState);
    }

    private void getConnects() {
        if (this.servrun) {
            this.connects = service.connects.intValue();
        } else {
            this.connects = 0;
        }
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        try {
            unregisterReceiver(this.sRunning);
            unregisterReceiver(this.mReceiver5);
            unregisterReceiver(this.mReceiver6);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.myDB.getDb().close();
        super.onDestroy();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        getConnects();
        refreshList(loadFromDB());
        super.onResume();
    }

    protected void onRestart() {
        super.onRestart();
    }

    public void Locationbtn() {
        try {
            byte[] buff = new byte[250];
            FileInputStream fs = openFileInput("My_Last_Location");
            fs.read(buff);
            fs.close();
            String st = new String(buff).trim();
            Intent i = new Intent("android.intent.action.VIEW");
            i.setData(Uri.parse(st));
            startActivity(i);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, R.string.NoData, 1).show();
            Log.e(LOG_TAG, "error" + e.getMessage());
        } catch (IOException e2) {
            Toast.makeText(this, "Some IO issue", 1).show();
            Log.e(LOG_TAG, "error" + e2.getMessage());
        }
    }

    private int getBtDevices(int mode) {
        btDevice fbt;
        String str;
        btDevice fbt2;
        int i = 0;
        this.vec.clear();
        if (this.carMode) {
            fbt = new btDevice();
            str = getString(R.string.carDockName);
            fbt.setBluetoothDevice(str, str, "1", am.getStreamMaxVolume(3));
            fbt2 = this.myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setIcon(R.drawable.car2);
                this.myDB.insert(fbt);
                this.vec.add(fbt);
            } else {
                this.vec.add(fbt2);
            }
            refreshList(loadFromDB());
        }
        if (this.homeDock) {
            fbt = new btDevice();
            str = getString(R.string.homeDockName);
            fbt.setBluetoothDevice(str, str, "2", am.getStreamMaxVolume(3));
            fbt2 = this.myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.usb);
                this.myDB.insert(fbt);
                this.vec.add(fbt);
            } else {
                this.vec.add(fbt2);
            }
            refreshList(loadFromDB());
        }
        if (this.headsetPlug) {
            fbt = new btDevice();
            str = getString(R.string.audioJackName);
            fbt.setBluetoothDevice(str, str, "3", am.getStreamMaxVolume(3));
            fbt2 = this.myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.jack);
                this.myDB.insert(fbt);
                this.vec.add(fbt);
            } else {
                this.vec.add(fbt2);
            }
            refreshList(loadFromDB());
        }
        if (this.power) {
            fbt = new btDevice();
            str = getString(R.string.powerPlugName);
            fbt.setBluetoothDevice(str, str, "4", am.getStreamMaxVolume(3));
            fbt2 = this.myDB.getBTD(fbt.mac);
            if (fbt2.mac == null) {
                fbt.setGetLoc(false);
                fbt.setIcon(R.drawable.usb);
                this.myDB.insert(fbt);
                this.vec.add(fbt);
            } else {
                this.vec.add(fbt2);
            }
            refreshList(loadFromDB());
        }
        if (mode >= 1) {
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            if (mBTA == null) {
                Toast.makeText(this.application, R.string.NobtSupport, 1).show();
                return 0;
            } else if (mBTA.isEnabled()) {
                if (mBTA != null) {
                    Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        IBluetooth ibta = getIBluetooth();
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getAddress() != null) {
                                String name;
                                btDevice bt = new btDevice();
                                i++;
                                if (VERSION.SDK_INT < 14 || VERSION.SDK_INT > 16) {
                                    name = device.getName();
                                } else {
                                    try {
                                        name = ibta.getRemoteAlias(device.getAddress());
                                    } catch (RemoteException e) {
                                        name = device.getName();
                                        e.printStackTrace();
                                    }
                                    if (name == null) {
                                        name = device.getName();
                                    }
                                }
                                bt.setBluetoothDevice(device, name, am.getStreamMaxVolume(3));
                                if (VERSION.SDK_INT > 15) {
                                    bt.setSetV(false);
                                }
                                btDevice bt2 = this.myDB.getBTD(bt.mac);
                                if (bt2.mac == null) {
                                    this.myDB.insert(bt);
                                    this.vec.add(bt);
                                } else {
                                    this.vec.add(bt2);
                                }
                            }
                        }
                    }
                }
                refreshList(loadFromDB());
                Toast.makeText(this.application, "Found " + i + " Bluetooth Devices", 1).show();
            } else {
                try {
                    startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return 0;
            }
        }
        return i;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 1:
                    if (resultCode != 0) {
                        int test = getBtDevices(1);
                        if (test > 0) {
                            this.lstring = new String[test];
                            for (int i = 0; i < test; i++) {
                                this.lstring[i] = ((btDevice) this.vec.get(i)).toString();
                            }
                            refreshList(loadFromDB());
                            break;
                        }
                    }
                    Toast.makeText(this.application, R.string.btEnableFail, 1).show();
                    refreshList(loadFromDB());
                    break;
                    break;
                case 2:
                    refreshList(loadFromDB());
                    break;
            }
        }
        if (requestCode == 4) {
            this.enableTTS = this.preferences.getBoolean("enableTTS", false);
            if (this.enableTTS) {
                try {
                    Intent checkIntent = new Intent();
                    checkIntent.setAction("android.speech.tts.engine.CHECK_TTS_DATA");
                    startActivityForResult(checkIntent, 3);
                } catch (Exception e) {
                    Toast.makeText(this.application, "TTS missing fault", 1).show();
                }
            }
        }
        if (requestCode == 3) {
            switch (resultCode) {
                case -3:
                    if (this.toasts) {
                        Toast.makeText(this.application, "TTS Missing Volume", 0).show();
                        return;
                    }
                    return;
                case -2:
                    if (!this.TTSignore) {
                        Builder builder = new Builder(this);
                        builder.setTitle(getString(R.string.app_name));
                        builder.setPositiveButton(R.string.Yes, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent installIntent = new Intent();
                                installIntent.setAction("android.speech.tts.engine.INSTALL_TTS_DATA");
                                main.this.startActivityForResult(installIntent, 3);
                            }
                        });
                        builder.setNegativeButton(R.string.No, null);
                        builder.setNeutralButton(R.string.ignoreTTSMissing, setIgnore());
                        builder.setMessage(R.string.needTTS);
                        builder.show();
                        return;
                    }
                    return;
                case -1:
                    if (this.toasts) {
                        Toast.makeText(this.application, "TTS Bad Data", 0).show();
                        return;
                    }
                    return;
                case 0:
                    if (this.toasts) {
                        Toast.makeText(this.application, "TTS Voice data fail", 0).show();
                        return;
                    }
                    return;
                case 1:
                    if (this.toasts) {
                        Toast.makeText(this.application, R.string.TTSready, 0).show();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private OnClickListener setIgnore() {
        Editor editor = this.preferences.edit();
        this.TTSignore = true;
        editor.putBoolean("TTSignore", true);
        editor.commit();
        return null;
    }

    private void refreshList(int test) {
        if (test > 0) {
            this.lstring = new String[test];
            int i = 0;
            while (i < test) {
                this.lstring[i] = ((btDevice) this.vec.get(i)).toString();
                if (this.connects > 0 && this.servrun) {
                    int j = 0;
                    while (j < service.btdConn.length) {
                        if (service.btdConn[j] != null && ((btDevice) this.vec.get(i)).getMac().equalsIgnoreCase(service.btdConn[j].getMac())) {
                            StringBuilder stringBuilder = new StringBuilder();
                            String[] strArr = this.lstring;
                            strArr[i] = stringBuilder.append(strArr[i]).append(" **").toString();
                        }
                        j++;
                    }
                }
                i++;
            }
        } else {
            this.lstring = new String[]{"no data"};
        }
        this.ladapt = new ArrayAdapter(this.application, resourceID, this.lstring);
        this.lvl.setAdapter(this.ladapt);
        this.ladapt.notifyDataSetChanged();
        this.lvl.invalidateViews();
        this.lvl.forceLayout();
    }

    private int loadFromDB() {
        this.myDB.getDb().close();
        if (!this.myDB.getDb().isOpen()) {
            try {
                this.myDB = new DeviceDB(this.application);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        try {
            this.vec = this.myDB.selectAlldb();
            if (this.vec.isEmpty() || this.vec == null) {
                return 0;
            }
            return this.vec.size();
        } catch (Exception e2) {
            e2.printStackTrace();
            return 0;
        }
    }

    private String getBTClassServ(BluetoothDevice btd) {
        String temp = "";
        if (btd == null) {
            return temp;
        }
        if (btd.getBluetoothClass().hasService(2097152)) {
            temp = "Audio, ";
        }
        if (btd.getBluetoothClass().hasService(4194304)) {
            temp = temp + "Telophony, ";
        }
        if (btd.getBluetoothClass().hasService(GravityCompat.RELATIVE_LAYOUT_DIRECTION)) {
            temp = temp + "Information, ";
        }
        if (btd.getBluetoothClass().hasService(8192)) {
            temp = temp + "Limited Discoverability, ";
        }
        if (btd.getBluetoothClass().hasService(131072)) {
            temp = temp + "Networking, ";
        }
        if (btd.getBluetoothClass().hasService(1048576)) {
            temp = temp + "Object Transfer, ";
        }
        if (btd.getBluetoothClass().hasService(65536)) {
            temp = temp + "Positioning, ";
        }
        if (btd.getBluetoothClass().hasService(262144)) {
            temp = temp + "Render, ";
        }
        if (btd.getBluetoothClass().hasService(524288)) {
            temp = temp + "Capture, ";
        }
        if (temp.length() > 5) {
            temp = temp.substring(0, temp.length() - 2);
        }
        return temp;
    }

    private String getBTClassDev(BluetoothDevice btd) {
        String temp = "";
        if (btd == null) {
            return temp;
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1056) {
            temp = "Car Audio, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1032) {
            temp = temp + "Handsfree, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1048) {
            temp = temp + "Headphones, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1064) {
            temp = temp + "HiFi Audio, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1044) {
            temp = temp + "Loudspeaker, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1052) {
            temp = temp + "Portable Audio, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1076) {
            temp = temp + "Camcorder, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1060) {
            temp = temp + "Set Top Box, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1084) {
            temp = temp + "A/V Display/Speaker, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1080) {
            temp = temp + "Video Monitor, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1068) {
            temp = temp + "VCR, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 516) {
            temp = temp + "Cellular Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 524) {
            temp = temp + "Smart Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 520) {
            temp = temp + "Cordless Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 532) {
            temp = temp + "ISDN Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 528) {
            temp = temp + "Phone Modem/Gateway, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 512) {
            temp = temp + "Other Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1028) {
            temp = temp + "Wearable Headset, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 1024) {
            temp = temp + "Uncategorized A/V, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 512) {
            temp = temp + "Uncategorized Phone, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 2048) {
            temp = temp + "Incategorized Toy, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 260) {
            temp = temp + "Desktop PC, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 272) {
            temp = temp + "Handheld PC, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 268) {
            temp = temp + "Laptop PC, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 276) {
            temp = temp + "Palm Sized PC/PDA, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 280) {
            temp = temp + "Wearable PC, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 264) {
            temp = temp + "Server PC, ";
        }
        if (btd.getBluetoothClass().getDeviceClass() == 256) {
            temp = temp + "Computer, ";
        }
        if (temp.length() > 3) {
            temp = temp.substring(0, temp.length() - 2);
        } else {
            temp = "other";
        }
        return temp;
    }

    private String getBTClassDevMaj(BluetoothDevice btd) {
        String temp = "";
        if (btd == null) {
            return temp;
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 1024) {
            temp = "Audio Video, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 256) {
            temp = temp + "Computer, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 2304) {
            temp = temp + "Health, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 0) {
            temp = temp + "Misc, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 768) {
            temp = temp + "Networking, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 1280) {
            temp = temp + "Peripheral, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 512) {
            temp = temp + "Phone, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 7936) {
            temp = temp + "Uncategorized, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 1792) {
            temp = temp + "Wearable, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 2048) {
            temp = temp + "Toy, ";
        }
        if (btd.getBluetoothClass().getMajorDeviceClass() == 1536) {
            temp = temp + "Imaging, ";
        }
        if (temp.length() >= 3) {
            temp = temp.substring(0, temp.length() - 2);
        } else {
            temp = "other";
        }
        return temp;
    }

    private IBluetooth getIBluetooth() {
        IBluetooth ibta = null;
        try {
            Log.d(LOG_TAG, "Test2: " + ((IBinder) Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class}).invoke(null, new Object[]{"bluetooth"})).getInterfaceDescriptor());
            Method m = Class.forName("android.bluetooth.IBluetooth").getDeclaredClasses()[0].getDeclaredMethod("asInterface", new Class[]{IBinder.class});
            m.setAccessible(true);
            return (IBluetooth) m.invoke(null, new Object[]{b});
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error " + e.getMessage());
            return ibta;
        }
    }
}
