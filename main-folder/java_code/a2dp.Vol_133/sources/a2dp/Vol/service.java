package a2dp.Vol;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.UiModeManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothA2dp;
import android.bluetooth.IBluetoothA2dp.Stub;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.TransportMediator;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;

public class service extends Service implements OnAudioFocusChangeListener {
    private static final String A2DP_Vol = "A2DP_Vol";
    private static final int ALARM_STREAM = 2;
    static String DeviceToConnect = null;
    private static final String FIX_STREAM = "fix_stream";
    private static final int IN_CALL_STREAM = 1;
    private static final String LOG_TAG = "A2DP_Volume";
    private static final int MUSIC_STREAM = 0;
    private static final String OLD_PH_VOL = "old_phone_vol";
    private static final String OLD_VOLUME = "old_vol";
    private static Integer OldVol = Integer.valueOf(5);
    private static Integer OldVol2 = Integer.valueOf(5);
    private static Integer Oldsilent = null;
    public static final String PREFS_NAME = "btVol";
    static AudioManager am2 = ((AudioManager) null);
    private static MyApplication application;
    public static btDevice[] btdConn = new btDevice[5];
    public static Integer connects = Integer.valueOf(0);
    private static boolean hideVolUi = false;
    static IBluetoothA2dp ibta2;
    public static ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            service.mIsBound = true;
            service.ibta2 = Stub.asInterface(service);
            BluetoothDevice device = null;
            for (BluetoothDevice dev : BluetoothAdapter.getDefaultAdapter().getBondedDevices()) {
                if (dev.getAddress().equalsIgnoreCase(service.DeviceToConnect)) {
                    device = dev;
                }
            }
            if (device != null) {
                try {
                    service.ibta2.connect(device);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            service.mIsBound = false;
            service.doUnbind(service.application);
        }
    };
    static boolean mIsBound = false;
    public static boolean mTtsReady = false;
    private static boolean mvolsLeft = false;
    private static String notify_pref = "always";
    private static boolean pvolsLeft = false;
    private static boolean ramp_vol = false;
    public static boolean run = false;
    public static boolean talk = false;
    private DeviceDB DB;
    float MAX_ACC = 10.0f;
    private int MAX_MESSAGE_LENGTH = 350;
    long MAX_TIME = 20000;
    private long SMS_delay = 3000;
    private final BroadcastReceiver SMScatcher = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && service.this.tm.getCallState() == 0) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdusObj = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdusObj.length];
                    for (int i = 0; i < pdusObj.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (SmsMessage currentMessage : messages) {
                        sb.append(MessageFormat.format(service.this.getString(R.string.msgTemplate), new Object[]{service.this.GetName(currentMessage.getDisplayOriginatingAddress()), currentMessage.getDisplayMessageBody()})).append(' ');
                    }
                    service.this.TextReader(sb.toString().trim());
                }
            }
        }
    };
    private int SMSstream = 0;
    String a2dpDir = "";
    private boolean bluetoothWasOff = false;
    private final BroadcastReceiver btOFFReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            String mac = "";
            if (mBTA.getState() == 10 || mBTA.getState() == 13) {
                int j = 0;
                while (j < service.btdConn.length) {
                    if (service.btdConn[j] != null && service.btdConn[j].getMac().length() > 2) {
                        mac = service.btdConn[j].getMac();
                        service.btdConn[j] = null;
                    }
                    j++;
                }
                service.this.getConnects();
                if (mac != "") {
                    if (service.this.notify) {
                        service.this.updateNot(false, null);
                    }
                    if (!service.mvolsLeft) {
                        service.setVolume(service.OldVol2.intValue(), service.application);
                    }
                    if (!service.pvolsLeft) {
                        service.setPVolume(service.OldVol.intValue());
                    }
                    service.this.dowifi(service.this.oldwifistate);
                }
                if (service.mTtsReady) {
                    try {
                        if (!service.this.clearedTts) {
                            service.this.clearTts();
                        }
                        service.this.mTts.shutdown();
                        service.mTtsReady = false;
                        service.this.unregisterReceiver(service.this.SMScatcher);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent itent = new Intent();
                itent.setAction("a2dp.Vol.main.RELOAD_LIST");
                itent.putExtra("disconnect", mac);
                service.application.sendBroadcast(itent);
            }
        }
    };
    private boolean carMode = true;
    private boolean clearedTts = true;
    private int connectedIcon;
    private volatile boolean connecting = false;
    private volatile boolean disconnecting = false;
    private boolean enableGTalk = false;
    private boolean enableSMS = false;
    private final BroadcastReceiver headSetReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent intent) {
            int state = intent.getIntExtra("state", -1);
            try {
                btDevice bt2 = service.this.DB.getBTD("3");
                if (bt2 != null && "3".equalsIgnoreCase(bt2.getMac())) {
                    if (state == 0 && service.connects.intValue() > 0) {
                        service.this.disconnecting = true;
                        service.this.DoDisconnected(bt2);
                    } else if (state == 1) {
                        service.this.connecting = true;
                        service.this.DoConnected(bt2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private boolean headsetPlug = false;
    private boolean homeDock = false;
    public OnInitListener listenerStarted = new OnInitListener() {
        public void onInit(int status) {
            if (status == 0) {
                service.mTtsReady = true;
                service.this.mTts.setOnUtteranceProgressListener(service.this.ul);
            }
        }
    };
    boolean local;
    LocationManager locmanager;
    private NotificationManager mNotificationManager = null;
    private PackageManager mPackageManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!service.this.connecting) {
                BluetoothDevice bt;
                btDevice bt2;
                service.this.connecting = true;
                try {
                    bt = (BluetoothDevice) intent.getExtras().get("android.bluetooth.device.extra.DEVICE");
                } catch (Exception e1) {
                    bt = null;
                    e1.printStackTrace();
                }
                if (bt != null) {
                    try {
                        bt2 = service.this.DB.getBTD(bt.getAddress());
                    } catch (Exception e) {
                        bt2 = null;
                    }
                } else {
                    try {
                        if (intent.getAction().equalsIgnoreCase("android.app.action.ENTER_CAR_MODE")) {
                            bt2 = service.this.DB.getBTD("1");
                        } else if (intent.getAction().equalsIgnoreCase("android.app.action.ENTER_DESK_MODE")) {
                            bt2 = service.this.DB.getBTD("2");
                        } else if (intent.getAction().equalsIgnoreCase("android.intent.action.ACTION_POWER_CONNECTED")) {
                            bt2 = service.this.DB.getBTD("4");
                        } else {
                            bt2 = null;
                        }
                    } catch (Exception e2) {
                        bt2 = null;
                        Log.e(service.LOG_TAG, "Error" + e2.toString());
                    }
                }
                if (bt2 == null || bt2.getMac() == null) {
                    service.this.connecting = false;
                } else {
                    service.this.DoConnected(bt2);
                }
            }
        }
    };
    private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context2, Intent intent2) {
            if (!service.this.disconnecting) {
                BluetoothDevice bt;
                btDevice bt2;
                service.this.disconnecting = true;
                try {
                    bt = (BluetoothDevice) intent2.getExtras().get("android.bluetooth.device.extra.DEVICE");
                } catch (Exception e1) {
                    bt = null;
                    e1.printStackTrace();
                }
                if (bt != null) {
                    try {
                        bt2 = service.this.DB.getBTD(bt.getAddress());
                    } catch (Exception e) {
                        bt2 = null;
                        Log.e(service.LOG_TAG, "Error" + e.toString());
                    }
                } else {
                    try {
                        if (intent2.getAction().equalsIgnoreCase("android.app.action.EXIT_CAR_MODE")) {
                            bt2 = service.this.DB.getBTD("1");
                        } else if (intent2.getAction().equalsIgnoreCase("android.app.action.EXIT_DESK_MODE")) {
                            bt2 = service.this.DB.getBTD("2");
                        } else if (intent2.getAction().equalsIgnoreCase("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                            bt2 = service.this.DB.getBTD("4");
                        } else {
                            bt2 = null;
                        }
                    } catch (Exception e2) {
                        bt2 = null;
                        Log.e(service.LOG_TAG, e2.toString());
                    }
                }
                if (bt2 == null || bt2.getMac() == null) {
                    service.this.disconnecting = false;
                } else {
                    service.this.DoDisconnected(bt2);
                }
            }
        }
    };
    private TextToSpeech mTts;
    private final BroadcastReceiver messageClear = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            service.this.clearTts();
        }
    };
    private boolean musicWasPlaying = false;
    HashMap<String, String> myHash;
    private boolean notify = true;
    boolean oldgpsstate = true;
    boolean oldwifistate = true;
    Boolean permLocation = Boolean.valueOf(true);
    Boolean permPhone = Boolean.valueOf(true);
    Boolean permReadContacts = Boolean.valueOf(true);
    Boolean permSMS = Boolean.valueOf(true);
    Boolean permStorage = Boolean.valueOf(true);
    private boolean power = false;
    SharedPreferences preferences;
    public BroadcastReceiver sco_change = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getIntExtra("android.media.extra.SCO_AUDIO_STATE", 0) == 0 && !service.this.clearedTts) {
                if (!service.mTtsReady) {
                    service.this.mTts = new TextToSpeech(service.application, service.this.listenerStarted);
                }
                HashMap<String, String> myHash2 = new HashMap();
                myHash2.put("utteranceId", service.FIX_STREAM);
                service.am2.requestAudioFocus(service.this, 3, 2);
                myHash2.put("streamType", String.valueOf(3));
                if (service.mTtsReady) {
                    service.am2.abandonAudioFocus(service.this);
                    service.am2.setMode(0);
                } else {
                    service.am2.abandonAudioFocus(service.this);
                    service.am2.setMode(0);
                }
                if (service.this.musicWasPlaying) {
                    new CountDownTimer(1000, 6000) {
                        public void onFinish() {
                            Intent i = new Intent("com.android.music.musicservicecommand");
                            i.putExtra("command", "play");
                            service.this.sendBroadcast(i);
                        }

                        public void onTick(long millisUntilFinished) {
                            Intent downIntent2 = new Intent("android.intent.action.MEDIA_BUTTON", null);
                            downIntent2.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, TransportMediator.KEYCODE_MEDIA_PLAY));
                            service.this.sendOrderedBroadcast(downIntent2, null);
                        }
                    }.start();
                }
                service.this.clearedTts = true;
            }
        }
    };
    private boolean speakerPhoneWasOn = true;
    private TelephonyManager tm;
    private final BroadcastReceiver tmessage = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            service.this.TextReader(intent.getStringExtra("message"));
        }
    };
    boolean tmessageRegistered = false;
    private boolean toasts = true;
    public UtteranceProgressListener ul = new UtteranceProgressListener() {
        public void onDone(String uttId) {
            int result = 0;
            if (service.A2DP_Vol.equalsIgnoreCase(uttId)) {
                Intent c;
                switch (service.this.SMSstream) {
                    case 0:
                        result = service.am2.abandonAudioFocus(service.this);
                        break;
                    case 1:
                        if (!service.this.clearedTts) {
                            c = new Intent();
                            c.setAction("a2dp.vol.service.CLEAR");
                            service.application.sendBroadcast(c);
                        }
                        result = service.am2.abandonAudioFocus(service.this);
                        break;
                    case 2:
                        if (!service.this.clearedTts) {
                            c = new Intent();
                            c.setAction("a2dp.vol.service.CLEAR");
                            service.application.sendBroadcast(c);
                        }
                        result = service.am2.abandonAudioFocus(service.this);
                        break;
                }
                if (result == 0) {
                    result = service.am2.abandonAudioFocus(service.this);
                }
                service.am2.setMode(0);
            }
            if (service.FIX_STREAM.equalsIgnoreCase(uttId)) {
                result = service.am2.abandonAudioFocus(service.this);
            }
            service.am2.setMode(0);
        }

        public void onError(String utteranceId) {
        }

        public void onStart(String utteranceId) {
        }
    };
    private long vol_delay = 5000;
    WifiManager wifiManager;

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.tm = (TelephonyManager) getSystemService("phone");
        return 1;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        application = (MyApplication) getApplication();
        try {
            this.preferences = PreferenceManager.getDefaultSharedPreferences(application);
            this.carMode = this.preferences.getBoolean("car_mode", true);
            this.homeDock = this.preferences.getBoolean("home_dock", false);
            this.headsetPlug = this.preferences.getBoolean("headset", false);
            this.power = this.preferences.getBoolean("power", false);
            this.toasts = this.preferences.getBoolean("toasts", true);
            this.enableSMS = this.preferences.getBoolean("enableTTS", false);
            this.enableGTalk = this.preferences.getBoolean("enableGTalk", true);
            notify_pref = this.preferences.getString("notify_pref", "always");
            hideVolUi = this.preferences.getBoolean("hideVolUi", false);
            this.MAX_TIME = Long.valueOf(this.preferences.getString("gpsTime", "15000")).longValue();
            this.MAX_ACC = Float.valueOf(this.preferences.getString("gpsDistance", "10")).floatValue();
            this.local = this.preferences.getBoolean("useLocalStorage", false);
            if (this.local) {
                this.a2dpDir = getFilesDir().toString();
            } else {
                this.a2dpDir = Environment.getExternalStorageDirectory() + "/A2DPVol";
            }
            OldVol2 = Integer.valueOf(this.preferences.getInt(OLD_VOLUME, 10));
            OldVol = Integer.valueOf(this.preferences.getInt(OLD_PH_VOL, 5));
            Oldsilent = Integer.valueOf(this.preferences.getInt("oldsilent", 10));
        } catch (NumberFormatException e) {
            this.MAX_ACC = 10.0f;
            this.MAX_TIME = 15000;
            Toast.makeText(this, "prefs failed to load ", 1).show();
            e.printStackTrace();
            Log.e(LOG_TAG, "prefs failed to load " + e.getMessage());
        }
        registerRecievers();
        am2 = (AudioManager) getSystemService("audio");
        this.DB = new DeviceDB(application);
        this.wifiManager = (WifiManager) getBaseContext().getSystemService("wifi");
        this.locmanager = (LocationManager) getBaseContext().getSystemService("location");
        if (notify_pref.equalsIgnoreCase("always") || notify_pref.equalsIgnoreCase("connected_only")) {
            this.notify = true;
        } else {
            this.notify = false;
        }
        if (this.notify) {
            this.mNotificationManager = (NotificationManager) getSystemService("notification");
            Notification not = new Builder(application).setContentTitle(getResources().getString(R.string.app_name)).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, main.class), 0)).setSmallIcon(R.drawable.ic_launcher).setContentText(getResources().getString(R.string.ServRunning)).setPriority(-2).build();
            if (notify_pref.equalsIgnoreCase("always")) {
                this.mNotificationManager.notify(1, not);
                startForeground(1, not);
            }
        }
        run = true;
        if (this.toasts) {
            Toast.makeText(this, R.string.ServiceStarted, 1).show();
        }
        String IRun = "a2dp.vol.service.RUNNING";
        Intent i = new Intent();
        i.setAction("a2dp.vol.service.RUNNING");
        application.sendBroadcast(i);
        this.tm = (TelephonyManager) getSystemService("phone");
        this.mPackageManager = getPackageManager();
    }

    private void registerRecievers() {
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED");
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(this.btOFFReciever, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        registerReceiver(this.messageClear, new IntentFilter("a2dp.vol.service.CLEAR"));
        if (this.carMode) {
            filter2.addAction(UiModeManager.ACTION_EXIT_CAR_MODE);
            filter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
        }
        if (this.homeDock) {
            filter2.addAction(UiModeManager.ACTION_EXIT_DESK_MODE);
            filter.addAction(UiModeManager.ACTION_ENTER_DESK_MODE);
        }
        if (this.power) {
            filter2.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
            filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        }
        if (this.headsetPlug) {
            registerReceiver(this.headSetReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
        }
        registerReceiver(this.mReceiver, filter);
        registerReceiver(this.mReceiver2, filter2);
    }

    public void onDestroy() {
        run = false;
        stopService(new Intent(application, StoreLoc.class));
        try {
            unregisterReceiver(this.mReceiver);
            unregisterReceiver(this.mReceiver2);
            unregisterReceiver(this.btOFFReciever);
            if (this.headsetPlug) {
                unregisterReceiver(this.headSetReceiver);
            }
            if (mTtsReady) {
                try {
                    if (!this.clearedTts) {
                        clearTts();
                    }
                    this.mTts.shutdown();
                    mTtsReady = false;
                    unregisterReceiver(this.SMScatcher);
                    unregisterReceiver(this.sco_change);
                    unregisterReceiver(this.tmessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.DB.getDb().close();
            unregisterReceiver(this.messageClear);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        String IStop = "a2dp.vol.service.STOPPED_RUNNING";
        Intent i = new Intent();
        i.setAction("a2dp.vol.service.STOPPED_RUNNING");
        application.sendBroadcast(i);
        if (this.toasts) {
            Toast.makeText(this, R.string.ServiceStopped, 1).show();
        }
        if (mIsBound) {
            stopForeground(true);
        } else {
            stopForeground(true);
        }
    }

    public void onStart() {
        run = true;
        this.connecting = false;
        this.disconnecting = false;
        if (this.notify) {
            updateNot(false, null);
        }
    }

    protected void DoConnected(btDevice bt2) {
        boolean done = false;
        int l = 0;
        int k = 0;
        while (k < btdConn.length) {
            if (btdConn[k] != null && bt2.getMac().equalsIgnoreCase(btdConn[k].getMac())) {
                l = k;
                done = true;
            }
            k++;
        }
        if (!done) {
            do {
                if (btdConn[l] == null) {
                    btdConn[l] = bt2;
                    done = true;
                }
                l++;
                if (l >= btdConn.length) {
                    done = true;
                    continue;
                }
            } while (!done);
        }
        getConnects();
        if (connects.intValue() <= 1) {
            getOldvol();
            getOldPvol();
            this.oldwifistate = this.wifiManager.isWifiEnabled();
            this.oldgpsstate = this.locmanager.isProviderEnabled("gps");
        }
        this.connectedIcon = bt2.getIcon();
        this.SMSstream = bt2.getSmsstream();
        this.vol_delay = (long) (bt2.getVoldelay() * 1000);
        this.SMS_delay = (long) (bt2.getSmsdelay() * 1000);
        ramp_vol = bt2.isVolramp();
        if (bt2.wifi) {
            try {
                this.oldwifistate = this.wifiManager.isWifiEnabled();
                dowifi(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error " + e.getMessage());
            }
        }
        if (bt2.getBdevice() != null && bt2.getBdevice().length() == 17) {
            btDevice tempBT = bt2;
            DeviceToConnect = bt2.getBdevice();
            getIBluetoothA2dp(application);
        }
        if (this.notify) {
            updateNot(true, bt2.toString());
        }
        if (this.toasts) {
            Toast.makeText(application, bt2.toString(), 1).show();
        }
        if (bt2.hasIntent()) {
            if (this.tm == null) {
                runApp(bt2);
            } else if (this.tm.getCallState() == 0) {
                runApp(bt2);
            }
        }
        if (this.enableGTalk && bt2.isEnableTTS()) {
            this.mTts = new TextToSpeech(application, this.listenerStarted);
            application.registerReceiver(this.tmessage, new IntentFilter("a2dp.vol.service.MESSAGE"));
            this.tmessageRegistered = true;
            IntentFilter intentFilter = new IntentFilter("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
            registerReceiver(this.sco_change, intentFilter);
            talk = true;
        }
        if (bt2.isEnableTTS() && this.enableSMS) {
            application.registerReceiver(this.SMScatcher, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        }
        Intent itent = new Intent();
        itent.setAction("a2dp.Vol.main.RELOAD_LIST");
        itent.putExtra("connect", bt2.getMac());
        application.sendBroadcast(itent);
        this.connecting = false;
        application.sendBroadcast(new Intent("a2dp.Vol.Clear"));
        if (bt2.isSetpv()) {
            final int vol1 = bt2.getPhonev();
            new CountDownTimer(this.vol_delay + 500, this.vol_delay + 500) {
                public void onFinish() {
                    service.setPVolume(vol1);
                }

                public void onTick(long arg0) {
                }
            }.start();
        }
        if (bt2.isSilent()) {
            am2.setStreamVolume(5, 0, 0);
        }
        if (bt2.isSetV()) {
            final int vol = bt2.getDefVol();
            new CountDownTimer(this.vol_delay, this.vol_delay) {
                public void onFinish() {
                    service.setVolume(vol, service.application);
                }

                public void onTick(long arg0) {
                }
            }.start();
        }
        if (bt2.isCarmode()) {
            set_car_mode(true);
        }
    }

    protected void DoDisconnected(btDevice bt2) {
        int SavVol = am2.getStreamVolume(3);
        if (bt2.hasIntent()) {
            Intent intent;
            if (am2.isMusicActive()) {
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                sendBroadcast(i);
                intent = new Intent("android.intent.action.HEADSET_PLUG");
                intent.putExtra("state", 0);
                try {
                    sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent downIntent2 = new Intent("android.intent.action.MEDIA_BUTTON", null);
                downIntent2.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 86));
                sendOrderedBroadcast(downIntent2, null);
            }
            if (bt2.getPname().length() > 3 && bt2.isAppkill()) {
                intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME");
                intent.setFlags(268435456);
                startActivity(intent);
                final String kpackage = bt2.getPname();
                new CountDownTimer(3000, 3000) {
                    public void onFinish() {
                        if (service.am2.isMusicActive()) {
                            Intent i = new Intent("com.android.music.musicservicecommand");
                            i.putExtra("command", "pause");
                            service.this.sendBroadcast(i);
                            Intent j = new Intent("android.intent.action.HEADSET_PLUG");
                            j.putExtra("state", 0);
                            try {
                                service.this.sendBroadcast(j);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent downIntent2 = new Intent("android.intent.action.MEDIA_BUTTON", null);
                            downIntent2.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 86));
                            service.this.sendOrderedBroadcast(downIntent2, null);
                        }
                        try {
                            service.this.stopApp(kpackage);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            Log.e(service.LOG_TAG, "Error " + e2.getMessage());
                        }
                    }

                    public void onTick(long arg0) {
                        if (service.am2.isMusicActive()) {
                            Intent i = new Intent("com.android.music.musicservicecommand");
                            i.putExtra("command", "pause");
                            service.this.sendBroadcast(i);
                            Intent downIntent2 = new Intent("android.intent.action.MEDIA_BUTTON", null);
                            downIntent2.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 86));
                            service.this.sendOrderedBroadcast(downIntent2, null);
                        }
                        try {
                            service.this.stopApp(kpackage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(service.LOG_TAG, "Error " + e.getMessage());
                        }
                    }
                }.start();
            }
        }
        if (bt2 != null && bt2.isGetLoc()) {
            Intent dolock = new Intent(this, StoreLoc.class);
            dolock.putExtra("device", bt2.getMac());
            try {
                startService(dolock);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (bt2.wifi) {
            dowifi(this.oldwifistate);
        }
        int k = 0;
        while (k < btdConn.length) {
            if (btdConn[k] != null && bt2.getMac().equalsIgnoreCase(btdConn[k].getMac())) {
                btdConn[k] = null;
            }
            k++;
        }
        getConnects();
        if (((bt2 != null && bt2.isSetV()) || bt2 == null) && !mvolsLeft) {
            setVolume(OldVol2.intValue(), application);
        }
        if (((bt2 != null && bt2.isSetpv()) || bt2 == null) && !pvolsLeft) {
            setPVolume(OldVol.intValue());
        }
        if (this.notify && bt2.mac != null) {
            updateNot(false, null);
        }
        if (mTtsReady && (bt2.isEnableTTS() || this.enableGTalk || connects.intValue() < 1)) {
            try {
                if (!this.clearedTts) {
                    clearTts();
                }
                this.mTts.shutdown();
                mTtsReady = false;
                if (this.enableGTalk) {
                    unregisterReceiver(this.sco_change);
                    talk = false;
                }
                if (this.enableSMS) {
                    application.unregisterReceiver(this.SMScatcher);
                }
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
        if (this.tmessageRegistered) {
            try {
                application.unregisterReceiver(this.tmessage);
                this.tmessageRegistered = false;
            } catch (Exception e222) {
                e222.printStackTrace();
            }
        }
        if (bt2.isSilent()) {
            am2.setStreamVolume(5, Oldsilent.intValue(), 0);
        }
        if (bt2.getBdevice() != null && bt2.getBdevice().length() == 17) {
            BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();
            if (mBTA != null) {
                if (mBTA.isEnabled() && this.bluetoothWasOff) {
                    mBTA.disable();
                }
                doUnbind(application);
            }
        }
        if (bt2.isAutovol()) {
            bt2.setDefVol(SavVol);
            this.DB.update(bt2);
        }
        if (bt2.isCarmode()) {
            set_car_mode(false);
        }
        String Ireload = "a2dp.Vol.main.RELOAD_LIST";
        Intent itent = new Intent();
        itent.setAction("a2dp.Vol.main.RELOAD_LIST");
        itent.putExtra("disconnect", bt2.getMac());
        application.sendBroadcast(itent);
        this.disconnecting = false;
    }

    public static void setVolume(int inputVol, Context sender) {
        int curvol = am2.getStreamVolume(3);
        if (inputVol < 0) {
            inputVol = 0;
        }
        if (inputVol > am2.getStreamMaxVolume(3)) {
            inputVol = am2.getStreamMaxVolume(3);
        }
        if (!ramp_vol || inputVol <= curvol) {
            int ui;
            if (hideVolUi) {
                ui = 0;
            } else {
                ui = 1;
            }
            am2.setStreamVolume(3, inputVol, ui);
            return;
        }
        final int minputVol = inputVol;
        new CountDownTimer((long) ((inputVol - curvol) * 1000), 1000) {
            public void onFinish() {
                int ui;
                if (service.hideVolUi) {
                    ui = 0;
                } else {
                    ui = 1;
                }
                service.am2.setStreamVolume(3, minputVol, ui);
            }

            public void onTick(long millisUntilFinished) {
                int ui;
                if (service.hideVolUi) {
                    ui = 0;
                } else {
                    ui = 1;
                }
                int cvol = service.am2.getStreamVolume(3);
                int newvol = cvol;
                if (cvol + 1 < minputVol) {
                    newvol++;
                }
                service.am2.setStreamVolume(3, newvol, ui);
            }
        }.start();
    }

    private void getOldvol() {
        OldVol2 = Integer.valueOf(am2.getStreamVolume(3));
        Editor editor = this.preferences.edit();
        editor.putInt(OLD_VOLUME, OldVol2.intValue());
        editor.commit();
    }

    private void getOldPvol() {
        OldVol = Integer.valueOf(am2.getStreamVolume(0));
        Oldsilent = Integer.valueOf(am2.getStreamVolume(5));
        Editor editor = this.preferences.edit();
        editor.putInt(OLD_PH_VOL, OldVol.intValue());
        editor.putInt("oldsilent", Oldsilent.intValue());
        editor.commit();
    }

    public static int setPVolume(int inputVol) {
        if (inputVol < 0) {
            inputVol = 0;
        }
        if (inputVol > am2.getStreamMaxVolume(0)) {
            inputVol = am2.getStreamMaxVolume(0);
        }
        if (hideVolUi) {
            am2.setStreamVolume(0, inputVol, 1);
        } else {
            am2.setStreamVolume(0, inputVol, 0);
        }
        return am2.getStreamVolume(0);
    }

    private void updateNot(boolean connect, String car) {
        String temp = car;
        if (car != null) {
            temp = getResources().getString(R.string.connectedTo) + " " + car;
        } else if (connects.intValue() > 0) {
            String tmp = null;
            for (int k = 0; k < btdConn.length; k++) {
                if (btdConn[k] != null) {
                    tmp = btdConn[k].toString();
                }
            }
            temp = getResources().getString(R.string.connectedTo) + " " + tmp;
            connect = true;
        } else {
            temp = getResources().getString(R.string.ServRunning);
        }
        if (connect) {
            this.mNotificationManager.notify(1, new NotificationCompat.Builder(application).setContentTitle(getResources().getString(R.string.app_name)).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, main.class), 0)).setSmallIcon(this.connectedIcon).setContentText(temp).setPriority(-2).build());
            return;
        }
        this.mNotificationManager.cancel(1);
        if (notify_pref.equalsIgnoreCase("always")) {
            this.mNotificationManager.notify(1, new Builder(application).setContentTitle(getResources().getString(R.string.app_name)).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, main.class), 0)).setSmallIcon(R.drawable.ic_launcher).setContentText(temp).setPriority(-2).build());
        }
    }

    private boolean runApp(btDevice bt) {
        String pname = bt.getPname();
        String cAction = bt.getAppaction();
        String cData = bt.getAppdata();
        String cType = bt.getApptype();
        if (bt.isApprestart() && pname != null && pname.length() > 3) {
            try {
                ((ActivityManager) getSystemService("activity")).killBackgroundProcesses(pname);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (pname == null || pname.equals("")) {
            return false;
        }
        Intent i;
        if (cData.length() > 1) {
            try {
                i = Intent.getIntent(cData);
            } catch (URISyntaxException e2) {
                e2.printStackTrace();
                return false;
            }
        } else if (cAction.equals("")) {
            try {
                i = this.mPackageManager.getLaunchIntentForPackage(pname);
            } catch (Exception e3) {
                e3.printStackTrace();
                return false;
            }
        } else {
            i = new Intent();
            i.setAction(cAction);
            if (!cData.equals("")) {
                i.setData(Uri.parse(cData));
            }
            if (!cType.equals("")) {
                i.setType(cType);
            }
        }
        try {
            i.setFlags(268435456);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
            startActivity(i);
            return true;
        } catch (Exception e32) {
            Toast t = Toast.makeText(getApplicationContext(), R.string.app_not_found, 0);
            if (this.notify) {
                t.show();
            }
            e32.printStackTrace();
            return false;
        }
    }

    protected void stopApp(String packageName) {
        if (getPackageManager().getLaunchIntentForPackage(packageName) != null) {
            try {
                ActivityManager act1 = (ActivityManager) getSystemService("activity");
                act1.killBackgroundProcesses(packageName);
                for (RunningAppProcessInfo info : act1.getRunningAppProcesses()) {
                    for (String contains : info.pkgList) {
                        if (contains.contains(packageName)) {
                            Process.killProcess(info.pid);
                        }
                    }
                }
            } catch (ActivityNotFoundException err) {
                err.printStackTrace();
                Toast t = Toast.makeText(getApplicationContext(), R.string.app_not_found, 0);
                if (this.notify) {
                    t.show();
                }
            }
        }
    }

    public void getIBluetoothA2dp(Context context) {
        Intent i = new Intent(IBluetoothA2dp.class.getName());
        i.setPackage(getPackageManager().resolveService(i, 64).serviceInfo.packageName);
        if (!context.bindService(i, mConnection, 1)) {
            Toast.makeText(context, "start service connection failed", 0).show();
        }
    }

    static void doUnbind(Context context) {
        if (mIsBound) {
            context.unbindService(mConnection);
        }
    }

    private void dowifi(boolean s) {
        try {
            this.wifiManager.setWifiEnabled(s);
        } catch (Exception e) {
            Toast.makeText(application, "Unable to switch wifi: " + e.toString(), 1).show();
            e.printStackTrace();
        }
    }

    private void getConnects() {
        connects = Integer.valueOf(0);
        mvolsLeft = false;
        pvolsLeft = false;
        for (int i = 0; i < btdConn.length; i++) {
            if (btdConn[i] != null) {
                Integer num = connects;
                connects = Integer.valueOf(connects.intValue() + 1);
                if (btdConn[i].isSetV()) {
                    mvolsLeft = true;
                }
                if (btdConn[i].isSetpv()) {
                    pvolsLeft = true;
                }
            }
        }
    }

    public void TextReader(String rawinput) {
        if (mTtsReady) {
            this.myHash = new HashMap();
            if (rawinput == null) {
                Toast.makeText(application, "No input", 1).show();
                return;
            }
            String input = rawinput.replaceAll("http.*? ", ", URL, ");
            this.myHash.put("utteranceId", A2DP_Vol);
            if (input.length() > this.MAX_MESSAGE_LENGTH) {
                input = input.substring(0, this.MAX_MESSAGE_LENGTH) + " , , , message truncated";
            }
            this.musicWasPlaying = am2.isMusicActive();
            switch (this.SMSstream) {
                case 0:
                    am2.requestAudioFocus(this, 3, 2);
                    this.myHash.put("streamType", String.valueOf(3));
                    this.clearedTts = false;
                    break;
                case 1:
                    if (am2.isBluetoothScoAvailableOffCall()) {
                        am2.startBluetoothSco();
                    }
                    if (this.musicWasPlaying) {
                        Intent i = new Intent("com.android.music.musicservicecommand");
                        i.putExtra("command", "pause");
                        sendBroadcast(i);
                    }
                    am2.requestAudioFocus(this, 0, 1);
                    this.myHash.put("streamType", String.valueOf(0));
                    this.clearedTts = false;
                    break;
                case 2:
                    am2.requestAudioFocus(this, 4, 2);
                    this.myHash.put("streamType", String.valueOf(4));
                    this.clearedTts = false;
                    break;
            }
            final String str = input;
            if (this.toasts) {
                Toast.makeText(application, str, 1).show();
            }
            if (this.tm.getCallState() == 0) {
                new CountDownTimer(this.SMS_delay, this.SMS_delay / 2) {
                    public void onFinish() {
                        try {
                            service.this.mTts.speak(str, 1, service.this.myHash);
                        } catch (Exception e) {
                            Toast.makeText(service.application, R.string.TTSNotReady, 1).show();
                            e.printStackTrace();
                        }
                    }

                    public void onTick(long arg0) {
                    }
                }.start();
            }
        }
    }

    private void clearTts() {
        if (am2.isBluetoothScoAvailableOffCall()) {
            am2.stopBluetoothSco();
        }
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
        }
    }

    private String GetName(String number) {
        if (ContextCompat.checkSelfPermission(application, "android.permission.READ_CONTACTS") == 0) {
            this.permReadContacts = Boolean.valueOf(true);
        } else {
            this.permReadContacts = Boolean.valueOf(false);
        }
        if (this.permReadContacts.booleanValue()) {
            Cursor c = getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)), new String[]{"display_name"}, null, null, null);
            if (c.moveToFirst()) {
                return c.getString(c.getColumnIndex("display_name"));
            }
        }
        return number;
    }

    private void set_car_mode(boolean mode) {
        try {
            UiModeManager mm = (UiModeManager) getSystemService("uimode");
            if (mode) {
                mm.enableCarMode(1);
            } else {
                mm.disableCarMode(0);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), 1).show();
            e.printStackTrace();
        }
    }
}
