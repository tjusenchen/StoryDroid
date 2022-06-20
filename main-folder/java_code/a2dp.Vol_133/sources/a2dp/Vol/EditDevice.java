package a2dp.Vol;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class EditDevice extends Activity {
    private static final int ACTION_ADD_PACKAGE = 17;
    private static final int ACTION_CHOOSE_APP = 2;
    private static final int ACTION_CHOOSE_APP_CUSTOM = 16;
    private static final int ACTION_CHOOSE_FROM_PROVIDER = 11;
    private static final int ACTION_CREATE_HOME_SCREEN_SHORTCUT = 14;
    private static final int ACTION_CUSTOM_INTENT = 6;
    private static final int ALARM_STREAM = 2;
    private static final String[] APP_TYPE_OPTIONS = new String[]{"Choose App", "Create Shortcut", "Home Screen Shortcut", "Pandora Radio Station", "Custom Intent", "Clear App Selection"};
    private static final int DIALOG_BITLY = 6;
    private static final int DIALOG_PICK_APP_TYPE = 3;
    private static final int DIALOG_WARN_STOP_APP = 5;
    private static final int FETCH_HOME_SCREEN_SHORTCUT = 15;
    private static final int IN_CALL_STREAM = 1;
    private static final int MUSIC_STREAM = 0;
    private boolean TTsEnabled;
    private String appaction;
    private String appdata;
    private boolean appkill;
    private MyApplication application;
    private boolean apprestart;
    private String apptype;
    public String btd;
    private Button cb;
    private Button connbt;
    private btDevice device;
    private boolean enablegps;
    private EditText fapp;
    private CheckBox fappkill;
    private CheckBox fapprestart;
    private CheckBox fautoVol;
    private EditText fbt;
    private CheckBox fcarmodeBox;
    private EditText fdesc2;
    private CheckBox fenableGPS;
    private CheckBox fenableTTS;
    private CheckBox fgloc;
    private SeekBar fphonev;
    private CheckBox frampVol;
    private CheckBox fsetpv;
    private CheckBox fsetvol;
    private CheckBox fsilent;
    private CheckBox fsleepBox;
    private SeekBar fsmsdelaybar;
    private TextView fsmsdelaybox;
    private SeekBar fvol;
    private SeekBar fvoldelaybar;
    private TextView fvoldelaybox;
    private CheckBox fwifi;
    private RadioGroup icongroup;
    private RadioButton iconradio0;
    private RadioButton iconradio1;
    private RadioButton iconradio2;
    private RadioButton iconradio3;
    private RadioButton iconradio4;
    private LinearLayout l1;
    private LinearLayout l2;
    private OnClickListener mAppTypeDialogOnClick = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            Intent i;
            switch (which) {
                case 0:
                    EditDevice.this.startActivityForResult(new Intent(EditDevice.this.getBaseContext(), AppChooser.class), 2);
                    return;
                case 1:
                    i = new Intent("android.intent.action.PICK_ACTIVITY");
                    i.putExtra("android.intent.extra.INTENT", new Intent("android.intent.action.CREATE_SHORTCUT"));
                    i.putExtra("android.intent.extra.TITLE", "Create a Shortcut");
                    EditDevice.this.startActivityForResult(i, 14);
                    return;
                case 2:
                    i = new Intent(EditDevice.this.getBaseContext(), ProviderList.class);
                    i.putExtra(ProviderList.EXTRA_PROVIDER, ProviderList.PROVIDER_HOMESCREEN);
                    EditDevice.this.startActivityForResult(i, 11);
                    return;
                case 3:
                    i = new Intent(EditDevice.this.getBaseContext(), ProviderList.class);
                    i.putExtra(ProviderList.EXTRA_PROVIDER, ProviderList.PROVIDER_PANDORA);
                    EditDevice.this.startActivityForResult(i, 11);
                    return;
                case 4:
                    i = new Intent(EditDevice.this.getBaseContext(), CustomIntentMaker.class);
                    i.putExtra("alarm_custom_action", EditDevice.this.appaction);
                    i.putExtra("alarm_custom_data", EditDevice.this.appdata);
                    i.putExtra("alarm_custom_type", EditDevice.this.apptype);
                    i.putExtra("alarm_package_name", EditDevice.this.pname);
                    EditDevice.this.startActivityForResult(i, 6);
                    return;
                case 5:
                    EditDevice.this.pname = "";
                    EditDevice.this.appaction = "";
                    EditDevice.this.appdata = "";
                    EditDevice.this.apptype = "";
                    EditDevice.this.vUpdateApp();
                    return;
                default:
                    return;
            }
        }
    };
    private TextView mediadelay;
    private DeviceDB myDB;
    private String pname;
    SharedPreferences preferences;
    private Button sb;
    OnSeekBarChangeListener smsdelaySeekBarProgress = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
            EditDevice.this.fsmsdelaybox.setText(progress + "s");
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onStopTrackingTouch(SeekBar arg0) {
        }
    };
    private Button startapp;
    private RadioGroup streamgroup;
    private RadioButton streamradio0;
    private RadioButton streamradio1;
    private RadioButton streamradio2;
    private TextView ttsdelay;
    private TextView tv2;
    private TextView tvincallVol;
    private TextView tvmediavol;
    private TextView tvstream;
    OnSeekBarChangeListener voldelaySeekBarProgress = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
            EditDevice.this.fvoldelaybox.setText(progress + "s");
        }

        public void onStartTrackingTouch(SeekBar arg0) {
        }

        public void onStopTrackingTouch(SeekBar arg0) {
        }
    };

    public void onBackPressed() {
        Save();
        closedb();
        finish();
        super.onBackPressed();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);
        this.application = (MyApplication) getApplication();
        this.myDB = new DeviceDB(this.application);
        AudioManager am = (AudioManager) getSystemService("audio");
        this.sb = (Button) findViewById(R.id.EditDevSavebutton);
        this.cb = (Button) findViewById(R.id.EditDevCancelbutton);
        this.startapp = (Button) findViewById(R.id.chooseAppButton);
        this.connbt = (Button) findViewById(R.id.chooseBTbutton);
        this.fdesc2 = (EditText) findViewById(R.id.editDesc2);
        this.fgloc = (CheckBox) findViewById(R.id.checkCaptLoc);
        this.fsetvol = (CheckBox) findViewById(R.id.checkSetVol);
        this.fvol = (SeekBar) findViewById(R.id.seekBarVol);
        this.fapp = (EditText) findViewById(R.id.editApp);
        this.fapprestart = (CheckBox) findViewById(R.id.appRestartCheckbox);
        this.fappkill = (CheckBox) findViewById(R.id.appKillCheckbox);
        this.fbt = (EditText) findViewById(R.id.editBtConnect);
        this.fwifi = (CheckBox) findViewById(R.id.checkwifi);
        this.fenableTTS = (CheckBox) findViewById(R.id.enableTTSBox);
        this.fsetpv = (CheckBox) findViewById(R.id.checkSetpv);
        this.fsilent = (CheckBox) findViewById(R.id.silentBox);
        this.fphonev = (SeekBar) findViewById(R.id.seekPhoneVol);
        this.fsmsdelaybar = (SeekBar) findViewById(R.id.SMSdelayseekBar);
        this.fsmsdelaybox = (TextView) findViewById(R.id.SMSdelaytextView);
        this.fvoldelaybar = (SeekBar) findViewById(R.id.VolDelaySeekBar);
        this.fvoldelaybox = (TextView) findViewById(R.id.VolDelayTextView);
        this.tv2 = (TextView) findViewById(R.id.textView2);
        this.frampVol = (CheckBox) findViewById(R.id.rampBox);
        this.fautoVol = (CheckBox) findViewById(R.id.autoVolcheckBox);
        this.icongroup = (RadioGroup) findViewById(R.id.radioGroupIcon);
        this.iconradio0 = (RadioButton) findViewById(R.id.iconradio0);
        this.iconradio1 = (RadioButton) findViewById(R.id.iconradio1);
        this.iconradio2 = (RadioButton) findViewById(R.id.iconradio2);
        this.iconradio3 = (RadioButton) findViewById(R.id.iconradio3);
        this.iconradio4 = (RadioButton) findViewById(R.id.iconradio4);
        this.streamgroup = (RadioGroup) findViewById(R.id.radioGroupStream);
        this.streamradio0 = (RadioButton) findViewById(R.id.streamradio0);
        this.streamradio1 = (RadioButton) findViewById(R.id.streamradio1);
        this.streamradio2 = (RadioButton) findViewById(R.id.streamradio2);
        this.l1 = (LinearLayout) findViewById(R.id.LinearLayout1);
        this.l2 = (LinearLayout) findViewById(R.id.LinearLayout2);
        this.ttsdelay = (TextView) findViewById(R.id.textViewTTSDelay);
        this.mediadelay = (TextView) findViewById(R.id.textViewMediaDelay);
        this.tvstream = (TextView) findViewById(R.id.textViewStream);
        this.tvmediavol = (TextView) findViewById(R.id.textViewMediaVolume);
        this.tvincallVol = (TextView) findViewById(R.id.textViewInCallVol);
        this.fsleepBox = (CheckBox) findViewById(R.id.checkBoxSleep);
        this.fcarmodeBox = (CheckBox) findViewById(R.id.checkBoxLaunchCar);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.application);
        this.TTsEnabled = this.preferences.getBoolean("enableTTS", false);
        this.btd = getIntent().getStringExtra("btd");
        this.device = this.myDB.getBTD(this.btd);
        this.fdesc2.setText(this.device.desc2);
        this.fgloc.setChecked(this.device.isGetLoc());
        this.fsetvol.setChecked(this.device.isSetV());
        this.fvol.setMax(am.getStreamMaxVolume(3));
        this.fvol.setProgress(this.device.defVol);
        this.fapp.setText(this.device.getPname());
        this.fbt.setText(this.device.getBdevice());
        this.fwifi.setChecked(this.device.isWifi());
        if (this.device == null) {
            this.connbt.setEnabled(false);
        }
        this.pname = this.device.getPname();
        this.appaction = this.device.getAppaction();
        this.appdata = this.device.getAppdata();
        this.apptype = this.device.getApptype();
        this.apprestart = this.device.isApprestart();
        this.appkill = this.device.isAppkill();
        this.fapprestart.setChecked(this.apprestart);
        this.fappkill.setChecked(this.appkill);
        this.fenableTTS.setChecked(this.device.isEnableTTS());
        this.fsetpv.setChecked(this.device.isSetpv());
        this.fphonev.setMax(am.getStreamMaxVolume(0));
        this.fphonev.setProgress(this.device.getPhonev());
        this.fsmsdelaybar.setMax(20);
        this.fsmsdelaybar.setOnSeekBarChangeListener(this.smsdelaySeekBarProgress);
        this.fsmsdelaybox.setText(this.device.smsdelay + "s");
        this.fsmsdelaybar.setProgress(this.device.getSmsdelay());
        this.fvoldelaybar.setMax(20);
        this.fvoldelaybar.setOnSeekBarChangeListener(this.voldelaySeekBarProgress);
        this.fvoldelaybox.setText(this.device.voldelay + "s");
        this.fvoldelaybar.setProgress(this.device.getVoldelay());
        this.frampVol.setChecked(this.device.isVolramp());
        this.fautoVol.setChecked(this.device.isAutovol());
        this.fsilent.setChecked(this.device.isSilent());
        this.fsleepBox.setChecked(this.device.isSleep());
        this.fcarmodeBox.setChecked(this.device.isCarmode());
        switch (this.device.getIcon()) {
            case R.drawable.car2:
                this.iconradio0.setChecked(true);
                break;
            case R.drawable.headset:
                this.iconradio1.setChecked(true);
                break;
            case R.drawable.icon5:
                this.iconradio4.setChecked(true);
                break;
            case R.drawable.jack:
                this.iconradio2.setChecked(true);
                break;
            case R.drawable.usb:
                this.iconradio3.setChecked(true);
                break;
            default:
                this.iconradio0.setChecked(true);
                break;
        }
        switch (this.device.getSmsstream()) {
            case 0:
                this.streamradio0.setChecked(true);
                break;
            case 1:
                this.streamradio1.setChecked(true);
                break;
            case 2:
                this.streamradio2.setChecked(true);
                break;
            default:
                this.streamradio0.setChecked(true);
                break;
        }
        setTTSVisibility();
        this.fenableTTS.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                EditDevice.this.setTTSVisibility();
            }
        });
        setMediaVisibility();
        this.fsetvol.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditDevice.this.setMediaVisibility();
            }
        });
        setInCallVisibility();
        this.fsetpv.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                EditDevice.this.setInCallVisibility();
            }
        });
        setAppVisibility();
        this.tv2.requestFocus();
        vUpdateApp();
        this.sb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditDevice.this.Save();
                EditDevice.this.closedb();
                EditDevice.this.finish();
            }
        });
        this.cb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                EditDevice.this.closedb();
                EditDevice.this.finish();
            }
        });
        this.startapp.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                PackageManager pm = EditDevice.this.getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(0);
                String[] lstring = new String[packages.size()];
                int i = 0;
                int n = 0;
                while (n < packages.size()) {
                    Intent itent = pm.getLaunchIntentForPackage(((ApplicationInfo) packages.get(n)).packageName);
                    if (((ApplicationInfo) packages.get(n)).icon > 0 && ((ApplicationInfo) packages.get(n)).enabled && itent != null) {
                        lstring[i] = ((ApplicationInfo) packages.get(n)).packageName;
                        i++;
                    }
                    n++;
                }
                final String[] ls2 = new String[i];
                for (int j = 0; j < i; j++) {
                    ls2[j] = lstring[j];
                }
                Arrays.sort(ls2);
                Builder builder = new Builder(EditDevice.this);
                builder.setTitle("Pick a package");
                builder.setItems(ls2, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        EditDevice.this.fapp.setText(ls2[item]);
                    }
                });
                builder.create().show();
                return false;
            }
        });
        this.startapp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Builder adb2 = new Builder(EditDevice.this);
                adb2.setTitle(R.string.ea_ti_app);
                adb2.setItems(EditDevice.APP_TYPE_OPTIONS, EditDevice.this.mAppTypeDialogOnClick);
                adb2.create();
                adb2.show();
            }
        });
        this.connbt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!(EditDevice.this.myDB.getDb().isOpen() || EditDevice.this.myDB.getDb().isDbLockedByCurrentThread())) {
                    EditDevice.this.myDB = new DeviceDB(EditDevice.this.application);
                }
                final Vector<btDevice> vec = EditDevice.this.myDB.selectAlldb();
                int j = vec.size();
                int i = 0;
                while (i < j) {
                    if (((btDevice) vec.get(i)).mac.length() < 17) {
                        vec.remove(i);
                        j--;
                        i--;
                    }
                    i++;
                }
                vec.trimToSize();
                String[] lstring = new String[(vec.size() + 1)];
                for (i = 0; i < vec.size(); i++) {
                    lstring[i] = ((btDevice) vec.get(i)).desc2;
                }
                lstring[vec.size()] = "none";
                Builder builder = new Builder(EditDevice.this);
                builder.setTitle("Bluetooth Device");
                builder.setItems(lstring, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item < vec.size()) {
                            EditDevice.this.fbt.setText(((btDevice) vec.get(item)).mac);
                        } else {
                            EditDevice.this.fbt.setText("");
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    private void setMediaVisibility() {
        if (this.fsetvol.isChecked()) {
            this.tvmediavol.setVisibility(0);
            this.fvol.setVisibility(0);
            this.fautoVol.setVisibility(0);
            this.frampVol.setVisibility(0);
            this.l2.setVisibility(0);
            this.mediadelay.setVisibility(0);
            return;
        }
        this.tvmediavol.setVisibility(8);
        this.fvol.setVisibility(8);
        this.fautoVol.setVisibility(8);
        this.frampVol.setVisibility(8);
        this.l2.setVisibility(8);
        this.mediadelay.setVisibility(8);
    }

    private void setTTSVisibility() {
        if (this.fenableTTS.isChecked()) {
            this.l1.setVisibility(0);
            this.ttsdelay.setVisibility(0);
            this.tvstream.setVisibility(0);
            this.streamgroup.setVisibility(0);
            return;
        }
        this.l1.setVisibility(8);
        this.ttsdelay.setVisibility(8);
        this.tvstream.setVisibility(8);
        this.streamgroup.setVisibility(8);
    }

    private void setInCallVisibility() {
        if (this.fsetpv.isChecked()) {
            this.tvincallVol.setVisibility(0);
            this.fphonev.setVisibility(0);
            return;
        }
        this.tvincallVol.setVisibility(8);
        this.fphonev.setVisibility(8);
    }

    private void setAppVisibility() {
        if (this.fapp.getText().length() > 0) {
            this.fapp.setVisibility(0);
            this.fapprestart.setVisibility(0);
            this.fappkill.setVisibility(0);
            this.fsleepBox.setVisibility(8);
            return;
        }
        this.fapp.setVisibility(8);
        this.fapprestart.setVisibility(8);
        this.fappkill.setVisibility(8);
        this.fsleepBox.setVisibility(8);
    }

    private void Save() {
        if (this.fdesc2.length() < 1) {
            this.device.setDesc2(this.device.desc1);
        } else {
            this.device.setDesc2(this.fdesc2.getText().toString());
        }
        this.device.setSetV(this.fsetvol.isChecked());
        this.device.setDefVol(this.fvol.getProgress());
        this.device.setGetLoc(this.fgloc.isChecked());
        this.device.setPname(this.pname);
        this.device.setBdevice(this.fbt.getText().toString());
        this.device.setWifi(this.fwifi.isChecked());
        this.device.setAppaction(this.appaction);
        this.device.setAppdata(this.appdata);
        this.device.setApptype(this.apptype);
        this.apprestart = this.fapprestart.isChecked();
        this.device.setApprestart(this.apprestart);
        this.appkill = this.fappkill.isChecked();
        this.device.setAppkill(this.appkill);
        this.enablegps = this.fenableTTS.isChecked();
        this.device.setEnableTTS(this.enablegps);
        this.device.setSetpv(this.fsetpv.isChecked());
        this.device.setPhonev(this.fphonev.getProgress());
        this.device.setSmsdelay(this.fsmsdelaybar.getProgress());
        this.device.setVoldelay(this.fvoldelaybar.getProgress());
        this.device.setVolramp(this.frampVol.isChecked());
        this.device.setAutovol(this.fautoVol.isChecked());
        this.device.setSilent(this.fsilent.isChecked());
        this.device.setSleep(this.fsleepBox.isChecked());
        this.device.setCarmode(this.fcarmodeBox.isChecked());
        switch (this.icongroup.getCheckedRadioButtonId()) {
            case R.id.iconradio0:
                this.device.setIcon(R.drawable.car2);
                break;
            case R.id.iconradio1:
                this.device.setIcon(R.drawable.headset);
                break;
            case R.id.iconradio2:
                this.device.setIcon(R.drawable.jack);
                break;
            case R.id.iconradio3:
                this.device.setIcon(R.drawable.usb);
                break;
            case R.id.iconradio4:
                this.device.setIcon(R.drawable.icon5);
                break;
        }
        switch (this.streamgroup.getCheckedRadioButtonId()) {
            case R.id.streamradio0:
                this.device.setSmsstream(0);
                break;
            case R.id.streamradio1:
                this.device.setSmsstream(1);
                break;
            case R.id.streamradio2:
                this.device.setSmsstream(2);
                break;
        }
        if (!this.TTsEnabled && this.fenableTTS.isChecked()) {
            Editor editor = this.preferences.edit();
            editor.putBoolean("enableTTS", true);
            editor.commit();
        }
        this.sb.setText("Saving");
        try {
            this.myDB.update(this.device);
            String Ireload = "a2dp.Vol.main.RELOAD_LIST";
            Intent itent = new Intent();
            itent.setAction("a2dp.Vol.main.RELOAD_LIST");
            itent.putExtra("device", "");
            this.application.sendBroadcast(itent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closedb() {
        this.myDB.getDb().close();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 2:
                    this.pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
                    this.appaction = "";
                    this.apptype = "";
                    this.appdata = "";
                    vUpdateApp();
                    break;
                case 6:
                    this.pname = "";
                    this.appaction = data.getStringExtra("alarm_custom_action");
                    this.appdata = data.getStringExtra("alarm_custom_data");
                    this.apptype = data.getStringExtra("alarm_custom_type");
                    if (this.appdata.length() > 3) {
                        try {
                            this.pname = Intent.getIntent(this.pname).getComponent().getPackageName();
                        } catch (URISyntaxException e) {
                            this.pname = "custom";
                            e.printStackTrace();
                        }
                    }
                    if (this.pname.equals("")) {
                        this.pname = "custom";
                    }
                    vUpdateApp();
                    break;
                case 11:
                    processShortcut(data);
                    break;
                case 14:
                    startActivityForResult(data, 15);
                    break;
                case 15:
                    processShortcut(data);
                    if (this.pname.length() < 3 || this.pname.equalsIgnoreCase("Custom")) {
                        showDialog(5);
                        break;
                    }
                case 16:
                    this.pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
                    vUpdateApp();
                    break;
                case 17:
                    this.pname = data.getStringExtra(AppChooser.EXTRA_PACKAGE_NAME);
                    vUpdateApp();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void vUpdateApp() {
        this.device.setAppaction(this.appaction);
        this.device.setAppdata(this.appdata);
        this.device.setApptype(this.apptype);
        this.device.setPname(this.pname);
        if (!this.device.hasIntent()) {
            this.fapp.setText("");
        } else if (this.pname != null && this.pname.length() > 3) {
            this.fapp.setText(this.pname);
        } else if (this.appdata != null) {
            this.fapp.setText(this.appdata);
        } else if (this.appaction != null) {
            this.fapp.setText(this.appaction);
        } else {
            this.fapp.setText("Custom");
        }
        setAppVisibility();
    }

    private void processShortcut(Intent data) {
        Intent i = (Intent) data.getParcelableExtra("android.intent.extra.shortcut.INTENT");
        this.appdata = getIntentUri(i);
        if (data.hasExtra(ProviderList.EXTRA_PACKAGE_NAME)) {
            this.pname = data.getStringExtra(ProviderList.EXTRA_PACKAGE_NAME);
        } else {
            try {
                this.pname = i.getComponent().getPackageName();
            } catch (Exception e) {
                this.pname = "";
                e.printStackTrace();
            }
        }
        if (this.pname.length() < 3) {
            this.pname = "custom";
        }
        this.appaction = data.getStringExtra("android.intent.extra.shortcut.NAME");
        this.apptype = "";
        vUpdateApp();
    }

    public static String getIntentUri(Intent i) {
        String rtr = "";
        try {
            return (String) Intent.class.getMethod("toUri", new Class[]{Integer.TYPE}).invoke(i, new Object[]{Integer.valueOf(Intent.class.getField("URI_INTENT_SCHEME").getInt(null))});
        } catch (Exception e) {
            return i.toURI();
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 3:
                Builder adb2 = new Builder(this);
                adb2.setTitle(R.string.ea_ti_app);
                adb2.setItems(APP_TYPE_OPTIONS, this.mAppTypeDialogOnClick);
                return adb2.create();
            case 5:
                Builder adb4 = new Builder(this);
                adb4.setTitle(R.string.ae_stop_app_warning_title);
                adb4.setMessage(R.string.ae_stop_app_warning_message);
                adb4.setCancelable(false);
                adb4.setPositiveButton("Select App", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditDevice.this.startActivityForResult(new Intent(EditDevice.this.getBaseContext(), AppChooser.class), 17);
                    }
                });
                adb4.setNegativeButton("Ignore", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                return adb4.create();
            case 6:
                Dialog pd = new ProgressDialog(this);
                pd.setIndeterminate(true);
                pd.setMessage("Shortenting Url with Bit.ly...");
                pd.setCancelable(false);
                return pd;
            default:
                return super.onCreateDialog(id);
        }
    }
}
