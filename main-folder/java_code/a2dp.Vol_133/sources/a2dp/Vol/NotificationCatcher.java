package a2dp.Vol;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import java.util.ArrayList;
import java.util.List;

public class NotificationCatcher extends NotificationListenerService {
    private static String[] packages;
    private MyApplication application;
    List<String> apps1 = new ArrayList();
    private final BroadcastReceiver clear = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            NotificationCatcher.this.notList.clear();
        }
    };
    List<notItem> notList = new ArrayList();
    private String packagelist;
    SharedPreferences preferences;
    private final BroadcastReceiver reloadprefs = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            NotificationCatcher.this.LoadPrefs();
        }
    };

    private class Readit extends AsyncTask<StatusBarNotification, Integer, Long> {
        private Readit() {
        }

        /* synthetic */ Readit(NotificationCatcher x0, AnonymousClass1 x1) {
            this();
        }

        protected Long doInBackground(StatusBarNotification... params) {
            int connected = 0;
            try {
                connected = service.connects.intValue();
                if (connected < 1) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            StatusBarNotification sbn = params[0];
            boolean test = false;
            for (String p : NotificationCatcher.packages) {
                if (p.equalsIgnoreCase(sbn.getPackageName())) {
                    test = true;
                }
            }
            if (test) {
                ApplicationInfo appInfo;
                CharSequence applicationLabel;
                String str = "";
                PackageManager pm = NotificationCatcher.this.getPackageManager();
                String pack = sbn.getPackageName();
                try {
                    appInfo = pm.getApplicationInfo(pack, 0);
                } catch (NameNotFoundException e2) {
                    appInfo = null;
                }
                if (appInfo != null) {
                    applicationLabel = pm.getApplicationLabel(appInfo);
                } else {
                    Object applicationLabel2 = pack;
                }
                String appName = (String) applicationLabel2;
                Notification notification = sbn.getNotification();
                if (notification == null) {
                    return null;
                }
                Long when = Long.valueOf(notification.when);
                notItem item = new notItem(pack, when);
                Boolean found = Boolean.valueOf(false);
                for (notItem element : NotificationCatcher.this.notList) {
                    if (element.getNot().equals(pack)) {
                        if (element.getNottime().longValue() + 1000 >= when.longValue()) {
                            return null;
                        }
                        NotificationCatcher.this.notList.set(NotificationCatcher.this.notList.indexOf(element), item);
                        found = Boolean.valueOf(true);
                    }
                }
                if (!found.booleanValue()) {
                    NotificationCatcher.this.notList.add(item);
                }
                str = str + appName + ", ";
                String ticker = "";
                if (notification.tickerText != null) {
                    ticker = sbn.getNotification().tickerText.toString();
                }
                String temp = "";
                if (!NotificationCatcher.this.apps1.contains(pack)) {
                    Bundle bun = notification.extras;
                    if (!bun.isEmpty()) {
                        CharSequence[] lines = bun.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES);
                        if (lines != null && lines.length > 0) {
                            for (CharSequence line : lines) {
                                if (line != null && line.length() > 1) {
                                    temp = line.toString();
                                }
                            }
                        }
                    }
                    String text = "";
                    if (!(bun.getString(NotificationCompat.EXTRA_TEXT) == null || bun.getString(NotificationCompat.EXTRA_TEXT).isEmpty())) {
                        text = bun.getString(NotificationCompat.EXTRA_TEXT).toString();
                    }
                    if (ticker.length() > 1) {
                        if (ticker.equalsIgnoreCase(temp) || temp.length() < 1) {
                            str = str + ticker;
                        } else {
                            str = str + ticker + ", " + temp;
                        }
                    } else if (!text.isEmpty()) {
                        if (text.equalsIgnoreCase(temp) || temp.isEmpty()) {
                            str = str + text;
                        } else {
                            str = str + text + ", " + temp;
                        }
                    }
                    if (temp.isEmpty() && ticker.isEmpty() && text.isEmpty()) {
                        return null;
                    }
                } else if (ticker == null) {
                    return null;
                } else {
                    str = str + ticker;
                }
                if (connected > 0 && str.length() > 0) {
                    String IRun = "a2dp.vol.service.MESSAGE";
                    Intent intent = new Intent();
                    intent.setAction("a2dp.vol.service.MESSAGE");
                    intent.putExtra("message", str);
                    NotificationCatcher.this.application.sendBroadcast(intent);
                }
            }
            return null;
        }
    }

    private class notItem {
        String not;
        Long nottime;

        public notItem(String not, Long nottime) {
            this.not = not;
            this.nottime = nottime;
        }

        public Long getNottime() {
            return this.nottime;
        }

        public void setNottime(Long nottime) {
            this.nottime = nottime;
        }

        public String getNot() {
            return this.not;
        }

        public void setNot(String not) {
            this.not = not;
        }
    }

    public void onCreate() {
        this.application = (MyApplication) getApplication();
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.application);
        registerReceiver(this.reloadprefs, new IntentFilter("a2dp.vol.Reload"));
        registerReceiver(this.clear, new IntentFilter("a2dp.Vol.Clear"));
        LoadPrefs();
        super.onCreate();
    }

    public void onDestroy() {
        unregisterReceiver(this.reloadprefs);
        unregisterReceiver(this.clear);
        super.onDestroy();
    }

    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        super.onNotificationPosted(sbn, rankingMap);
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        new Readit().execute(new StatusBarNotification[]{sbn});
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    public void LoadPrefs() {
        this.packagelist = this.preferences.getString("packages", "com.google.android.talk,com.android.email,com.android.calendar");
        packages = this.packagelist.split(",");
        this.apps1.add("com.google.android.talk");
        this.apps1.add("com.skype.raider");
    }
}
