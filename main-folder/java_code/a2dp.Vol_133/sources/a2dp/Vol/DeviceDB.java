package a2dp.Vol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class DeviceDB {
    private static final String DATABASE_NAME = "btdevices.db";
    private static final int DATABASE_VERSION = 13;
    private static final String INSERT = "insert into devices(desc1, desc2, mac, maxv, setv, getl, pname, bdevice, wifi, appaction, appdata, apptype, apprestart, tts, setpv, phonev, appkill, enablegps, icon, smsdelay, smsstream, voldelay, volramp, autovol, silent, sleep, carmode) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String TABLE_NAME = "devices";
    private static Context context;
    private SQLiteDatabase db = new OpenHelper(context).getWritableDatabase();
    private SQLiteStatement insertStmt = this.db.compileStatement(INSERT);

    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DeviceDB.DATABASE_NAME, null, 13);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE devices(desc1 TEXT, desc2 TEXT, mac TEXT PRIMARY KEY, maxv INTEGER, setv INTEGER DEFAULT 1, getl INTEGER DEFAULT 1, pname TEXT, bdevice TEXT, wifi INTEGER DEFAULT 0, appaction TEXT, appdata TEXT, apptype TEXT, apprestart INTEGER DEFAULT 0, tts INTEGER DEFAULT 0, setpv INTEGER DEFAULT 0, phonev INTEGER DEFAULT 10, appkill INTEGER DEFAULT 1, enablegps INTEGER DEFAULT 0, icon INTEGER, smsdelay DEFAULT 3, smsstream DEFAULT 1, voldelay DEFAULT 5, volramp DEFAULT 0, autovol DEFAULT 1, silent DEFAULT 0, sleep DEFAULT 0, carmode DEFAULT 0)");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(DeviceDB.context, "Upgrading database....", 1).show();
            if ((newVersion < 4 && oldVersion < 4) || oldVersion > 13 || newVersion > 13) {
                db.execSQL("DROP TABLE IF EXISTS devices");
                onCreate(db);
                Toast.makeText(DeviceDB.context, "Database replaced", 1).show();
            } else if (newVersion >= 5) {
                try {
                    List<String> columns = GetColumns(db);
                    db.execSQL("ALTER table devices RENAME TO 'temp_devices'");
                    onCreate(db);
                    columns.retainAll(GetColumns(db));
                    String cols = join(columns);
                    db.execSQL(String.format("INSERT INTO %s (%s) SELECT %s from temp_%s", new Object[]{DeviceDB.TABLE_NAME, cols, cols, DeviceDB.TABLE_NAME}));
                    db.execSQL("DROP table 'temp_devices'");
                    Toast.makeText(DeviceDB.context, "Database upgraded succesfully", 1).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    db.execSQL("DROP TABLE IF EXISTS devices");
                    Toast.makeText(DeviceDB.context, "Upgrade failed, replaced database", 1).show();
                    onCreate(db);
                }
            }
        }

        public static List<String> GetColumns(SQLiteDatabase db) {
            List<String> ar = null;
            Cursor c = null;
            try {
                c = db.rawQuery("select * from devices limit 1", null);
                if (c != null) {
                    ar = new ArrayList(Arrays.asList(c.getColumnNames()));
                }
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                Log.v(DeviceDB.TABLE_NAME, e.getMessage(), e);
                e.printStackTrace();
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
            return ar;
        }

        public static String join(List<String> list) {
            StringBuilder buf = new StringBuilder();
            int num = list.size();
            for (int i = 0; i < num; i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append((String) list.get(i));
            }
            return buf.toString();
        }
    }

    public DeviceDB(Context context) {
        context = context;
    }

    public void update(btDevice bt) {
        ContentValues vals = new ContentValues();
        vals.put("desc2", bt.getDesc2());
        vals.put("maxv", Long.valueOf((long) bt.getDefVol()));
        vals.put("setv", Long.valueOf(bt.islSetV()));
        vals.put("getl", Long.valueOf(bt.islGetLoc()));
        vals.put("pname", bt.getPname());
        vals.put("bdevice", bt.getBdevice());
        vals.put("wifi", Long.valueOf(bt.islWifi()));
        vals.put("appaction", bt.getAppaction());
        vals.put("appdata", bt.getAppdata());
        vals.put("apptype", bt.getApptype());
        vals.put("apprestart", Long.valueOf(bt.lApprestart()));
        vals.put("tts", Long.valueOf(bt.islEnableTTS()));
        vals.put("setpv", Long.valueOf(bt.islSetpv()));
        vals.put("phonev", Long.valueOf((long) bt.getPhonev()));
        vals.put("appkill", Long.valueOf(bt.lAppkill()));
        vals.put("enablegps", Long.valueOf(bt.lenablegps()));
        vals.put("icon", Long.valueOf((long) bt.getIcon()));
        vals.put("smsdelay", Long.valueOf((long) bt.getSmsdelay()));
        vals.put("smsstream", Long.valueOf((long) bt.getSmsstream()));
        vals.put("voldelay", Long.valueOf((long) bt.getVoldelay()));
        vals.put("volramp", Long.valueOf(bt.lVolramp()));
        vals.put("autovol", Long.valueOf(bt.lautovol()));
        vals.put("silent", Long.valueOf(bt.lsilent()));
        vals.put("sleep", Boolean.valueOf(bt.isSleep()));
        vals.put("carmode", Boolean.valueOf(bt.isCarmode()));
        this.db.update(TABLE_NAME, vals, "mac='" + bt.mac + "'", null);
    }

    public void delete(btDevice bt) {
        this.db.delete(TABLE_NAME, "mac='" + bt.mac + "'", null);
    }

    public long insert(btDevice btd) {
        String temp1 = btd.desc1;
        if (temp1 == null) {
            temp1 = "Unknown Device";
        }
        this.insertStmt.bindString(1, temp1);
        String temp2 = btd.desc2;
        if (temp2 == null) {
            temp2 = temp1;
        }
        this.insertStmt.bindString(2, temp2);
        if (btd.mac == null) {
            return -1;
        }
        this.insertStmt.bindString(3, btd.mac);
        this.insertStmt.bindLong(4, (long) btd.getDefVol());
        this.insertStmt.bindLong(5, btd.islSetV());
        this.insertStmt.bindLong(6, btd.islGetLoc());
        this.insertStmt.bindString(7, btd.getPname());
        this.insertStmt.bindString(8, btd.getBdevice());
        this.insertStmt.bindLong(9, btd.islWifi());
        this.insertStmt.bindString(10, btd.getAppaction());
        this.insertStmt.bindString(11, btd.getAppdata());
        this.insertStmt.bindString(12, btd.getApptype());
        this.insertStmt.bindLong(13, btd.lApprestart());
        this.insertStmt.bindLong(14, btd.islEnableTTS());
        this.insertStmt.bindLong(15, btd.islSetpv());
        this.insertStmt.bindLong(16, (long) btd.getPhonev());
        this.insertStmt.bindLong(17, btd.lAppkill());
        this.insertStmt.bindLong(18, btd.lenablegps());
        this.insertStmt.bindLong(19, (long) btd.getIcon());
        this.insertStmt.bindLong(20, (long) btd.getSmsdelay());
        this.insertStmt.bindLong(21, (long) btd.getSmsstream());
        this.insertStmt.bindLong(22, (long) btd.getVoldelay());
        this.insertStmt.bindLong(23, btd.lVolramp());
        this.insertStmt.bindLong(24, btd.lautovol());
        this.insertStmt.bindLong(25, btd.lsilent());
        this.insertStmt.bindLong(26, btd.lsleep());
        this.insertStmt.bindLong(27, btd.lcarmode());
        try {
            return this.insertStmt.executeInsert();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public btDevice getBTD(String imac) {
        btDevice bt = new btDevice();
        Cursor cs = this.db.query(TABLE_NAME, null, "mac = ?", new String[]{imac}, null, null, null, null);
        try {
            if (cs.moveToFirst()) {
                bt.setDesc1(cs.getString(0));
                bt.setDesc2(cs.getString(1));
                bt.setMac(cs.getString(2));
                bt.setDefVol(cs.getInt(3));
                bt.setSetV(cs.getInt(4));
                bt.setGetLoc(cs.getInt(5));
                bt.setPname(cs.getString(6));
                bt.setBdevice(cs.getString(7));
                bt.setWifi(cs.getInt(8));
                bt.setAppaction(cs.getString(9));
                bt.setAppdata(cs.getString(10));
                bt.setApptype(cs.getString(11));
                bt.setApprestart(cs.getInt(12));
                bt.setEnableTTS(cs.getInt(13));
                bt.setSetpv(cs.getInt(14));
                bt.setPhonev(cs.getInt(15));
                bt.setAppkill(cs.getInt(16));
                bt.setEnablegps(cs.getInt(17));
                bt.setIcon(cs.getInt(18));
                bt.setSmsdelay(cs.getInt(19));
                bt.setSmsstream(cs.getInt(20));
                bt.setVoldelay(cs.getInt(21));
                bt.setVolramp(cs.getInt(22));
                bt.setAutovol(cs.getInt(23));
                bt.setSilent(cs.getInt(24));
                bt.setSleep(cs.getInt(25));
                bt.setCarmode(cs.getInt(26));
            }
        } catch (Exception e) {
            bt.mac = null;
        }
        if (!(cs == null || cs.isClosed())) {
            cs.close();
        }
        return bt;
    }

    public void deleteAll() {
        this.db.delete(TABLE_NAME, null, null);
    }

    public SQLiteDatabase getDb() {
        return this.db;
    }

    public int getLength() {
        return selectAll().size();
    }

    public List<String> selectAll() {
        List<String> list = new ArrayList();
        if (!this.db.isOpen()) {
            return null;
        }
        Cursor cursor = this.db.query(TABLE_NAME, new String[]{"desc1", "desc2"}, null, null, null, null, "desc2");
        try {
            if (cursor.moveToFirst()) {
                do {
                    String t = cursor.getString(1);
                    if (t.length() < 2) {
                        list.add(cursor.getString(0));
                    } else {
                        list.add(t);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(context, "Database corrupt, delete and recreate database", 1).show();
            e.printStackTrace();
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return list;
    }

    public Vector<btDevice> selectAlldb() {
        Vector<btDevice> list = new Vector();
        Cursor cursor = this.db.query(TABLE_NAME, new String[]{"desc1", "desc2", "mac", "maxv", "setv", "getl", "pname", "bdevice", "wifi", "appaction", "appdata", "apptype", "apprestart", "tts", "setpv", "phonev", "appkill", "enablegps", "icon", "smsdelay", "smsstream", "voldelay", "volramp", "autovol", "silent", "sleep", "carmode"}, null, null, null, null, "desc2");
        if (cursor.moveToFirst()) {
            do {
                btDevice bt = new btDevice();
                bt.setDesc1(cursor.getString(0));
                bt.setDesc2(cursor.getString(1));
                bt.setMac(cursor.getString(2));
                bt.setSetV(cursor.getInt(4));
                bt.setDefVol(cursor.getInt(3));
                bt.setGetLoc(cursor.getInt(5));
                bt.setPname(cursor.getString(6));
                bt.setBdevice(cursor.getString(7));
                bt.setWifi(cursor.getInt(8));
                bt.setAppaction(cursor.getString(9));
                bt.setAppdata(cursor.getString(10));
                bt.setApptype(cursor.getString(11));
                bt.setApprestart(cursor.getInt(12));
                bt.setEnableTTS(cursor.getInt(13));
                bt.setSetpv(cursor.getInt(14));
                bt.setPhonev(cursor.getInt(15));
                bt.setAppkill(cursor.getInt(16));
                bt.setEnablegps(cursor.getInt(17));
                bt.setIcon(cursor.getInt(18));
                bt.setSmsdelay(cursor.getInt(19));
                bt.setSmsstream(cursor.getInt(20));
                bt.setVoldelay(cursor.getInt(21));
                bt.setVolramp(cursor.getInt(22));
                bt.setAutovol(cursor.getInt(23));
                bt.setSilent(cursor.getInt(24));
                bt.setSleep(cursor.getInt(25));
                bt.setCarmode(cursor.getInt(26));
                list.add(bt);
            } while (cursor.moveToNext());
        }
        if (!(cursor == null || cursor.isClosed())) {
            cursor.close();
        }
        return list;
    }
}
