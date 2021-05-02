package a2dp.Vol;

import android.bluetooth.BluetoothDevice;

public class btDevice {
    public String appaction;
    public String appdata;
    public boolean appkill;
    public boolean apprestart;
    public String apptype;
    public boolean autovol;
    public String bdevice;
    public boolean carmode;
    public int defVol;
    public String desc1;
    public String desc2;
    public boolean enableTTS;
    public boolean enablegps;
    public boolean getLoc;
    public int icon;
    public String mac;
    public int phonev;
    public String pname;
    public boolean setV;
    public boolean setpv;
    public boolean silent;
    public boolean sleep;
    public int smsdelay;
    public int smsstream;
    public int voldelay;
    public boolean volramp;
    public boolean wifi;

    public boolean isSetpv() {
        return this.setpv;
    }

    public long islSetpv() {
        if (isSetpv()) {
            return 1;
        }
        return 0;
    }

    public void setSetpv(boolean setpv) {
        this.setpv = setpv;
    }

    public void setSetpv(int sV) {
        if (sV > 0) {
            this.setpv = true;
        } else {
            this.setpv = false;
        }
    }

    public int getPhonev() {
        return this.phonev;
    }

    public void setPhonev(int phonev) {
        this.phonev = phonev;
    }

    public String getPname() {
        return this.pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getAppaction() {
        return this.appaction;
    }

    public void setAppaction(String appaction) {
        this.appaction = appaction;
    }

    public String getAppdata() {
        return this.appdata;
    }

    public void setAppdata(String appdata) {
        this.appdata = appdata;
    }

    public String getApptype() {
        return this.apptype;
    }

    public void setApptype(String apptype) {
        this.apptype = apptype;
    }

    public boolean isApprestart() {
        return this.apprestart;
    }

    public void setApprestart(boolean apprestart) {
        this.apprestart = apprestart;
    }

    public long lApprestart() {
        if (this.apprestart) {
            return 1;
        }
        return 0;
    }

    public void setApprestart(int apprestart) {
        if (apprestart > 0) {
            this.apprestart = true;
        } else {
            this.apprestart = false;
        }
    }

    public boolean isAppkill() {
        return this.appkill;
    }

    public void setAppkill(boolean appkill) {
        this.appkill = appkill;
    }

    public long lAppkill() {
        if (this.appkill) {
            return 1;
        }
        return 0;
    }

    public void setAppkill(int appkill) {
        if (appkill > 0) {
            this.appkill = true;
        } else {
            this.appkill = false;
        }
    }

    public long lenablegps() {
        if (this.enablegps) {
            return 1;
        }
        return 0;
    }

    public boolean isEnablegps() {
        return this.enablegps;
    }

    public void setEnablegps(boolean enablegps) {
        this.enablegps = enablegps;
    }

    public void setEnablegps(int enablegps1) {
        if (enablegps1 > 0) {
            this.enablegps = true;
        } else {
            this.enablegps = false;
        }
    }

    public boolean isEnableTTS() {
        return this.enableTTS;
    }

    public long islEnableTTS() {
        if (this.enableTTS) {
            return 1;
        }
        return 0;
    }

    public void setEnableTTS(boolean enableTTS) {
        this.enableTTS = enableTTS;
    }

    public void setEnableTTS(int value) {
        if (value > 0) {
            this.enableTTS = true;
        } else {
            this.enableTTS = false;
        }
    }

    public String getBdevice() {
        return this.bdevice;
    }

    public void setBdevice(String bdevice) {
        this.bdevice = bdevice;
    }

    public boolean isWifi() {
        return this.wifi;
    }

    public long islWifi() {
        if (this.wifi) {
            return 1;
        }
        return 0;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public void setWifi(int swifi) {
        if (swifi > 0) {
            this.wifi = true;
        } else {
            this.wifi = false;
        }
    }

    public boolean isGetLoc() {
        return this.getLoc;
    }

    public long islGetLoc() {
        if (this.getLoc) {
            return 1;
        }
        return 0;
    }

    public void setGetLoc(boolean getLoc) {
        this.getLoc = getLoc;
    }

    public void setGetLoc(int g) {
        if (g >= 1) {
            this.getLoc = true;
        } else {
            this.getLoc = false;
        }
    }

    public String toString() {
        if (this.desc2 == null) {
            return this.desc1;
        }
        return this.desc2;
    }

    public String getDesc1() {
        return this.desc1;
    }

    public void setDesc1(String desc1) {
        this.desc1 = desc1;
    }

    public String getDesc2() {
        return this.desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = FileNameCleaner.cleanFileName(desc2);
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public boolean isSetV() {
        return this.setV;
    }

    public long islSetV() {
        if (isSetV()) {
            return 1;
        }
        return 0;
    }

    public void setSetV(boolean setV) {
        this.setV = setV;
    }

    public void setSetV(int sV) {
        if (sV > 0) {
            this.setV = true;
        } else {
            this.setV = false;
        }
    }

    public int getDefVol() {
        return this.defVol;
    }

    public void setDefVol(int defVol) {
        this.defVol = defVol;
    }

    public int getIcon() {
        return this.icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getSmsdelay() {
        return this.smsdelay;
    }

    public void setSmsdelay(int smsdelay) {
        this.smsdelay = smsdelay;
    }

    public int getSmsstream() {
        return this.smsstream;
    }

    public void setSmsstream(int smsstream) {
        this.smsstream = smsstream;
    }

    public int getVoldelay() {
        return this.voldelay;
    }

    public void setVoldelay(int voldelay) {
        this.voldelay = voldelay;
    }

    public boolean isVolramp() {
        return this.volramp;
    }

    public void setVolramp(boolean volramp) {
        this.volramp = volramp;
    }

    public long lVolramp() {
        if (isVolramp()) {
            return 1;
        }
        return 0;
    }

    public void setVolramp(int ramp) {
        if (ramp > 0) {
            this.volramp = true;
        } else {
            this.volramp = false;
        }
    }

    public boolean isAutovol() {
        return this.autovol;
    }

    public void setAutovol(boolean autovol) {
        this.autovol = autovol;
    }

    public void setAutovol(int autovol) {
        if (autovol > 0) {
            this.autovol = true;
        } else {
            this.autovol = false;
        }
    }

    public long lautovol() {
        if (this.autovol) {
            return 1;
        }
        return 0;
    }

    public boolean isSilent() {
        return this.silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setSilent(int silent) {
        if (silent > 0) {
            this.silent = true;
        } else {
            this.silent = false;
        }
    }

    public long lsilent() {
        if (this.silent) {
            return 1;
        }
        return 0;
    }

    public void setBluetoothDevice(BluetoothDevice btd, String name, int vol) {
        this.desc1 = btd.getName();
        this.desc2 = FileNameCleaner.cleanFileName(name);
        this.mac = btd.getAddress();
        this.setV = true;
        this.defVol = vol;
        this.getLoc = true;
        this.pname = "";
        this.bdevice = "";
        this.wifi = false;
        this.appaction = "";
        this.appdata = "";
        this.apptype = "";
        this.apprestart = false;
        this.appkill = true;
        this.enableTTS = false;
        this.phonev = 10;
        this.setpv = false;
        setIcon(R.drawable.car2);
        this.autovol = false;
        this.smsdelay = 6;
        this.volramp = false;
        this.voldelay = 6;
        this.silent = false;
        this.carmode = false;
        this.sleep = false;
    }

    public void setBluetoothDevice(String s1, String s2, String mac, int vol) {
        this.desc1 = s1;
        this.desc2 = FileNameCleaner.cleanFileName(s2);
        this.mac = mac;
        this.setV = true;
        this.defVol = vol;
        this.getLoc = true;
        this.pname = "";
        this.bdevice = "";
        this.wifi = false;
        this.appaction = "";
        this.appdata = "";
        this.apptype = "";
        this.apprestart = false;
        this.appkill = true;
        this.enableTTS = false;
        this.phonev = 10;
        this.setpv = false;
        setIcon(R.drawable.car2);
        this.autovol = false;
        this.smsdelay = 6;
        this.volramp = false;
        this.voldelay = 6;
        this.silent = false;
        this.carmode = false;
        this.sleep = false;
    }

    public boolean hasIntent() {
        if (this.pname == null || this.appdata == null) {
            return false;
        }
        if (this.pname.length() < 3 && this.appdata.length() < 3) {
            return false;
        }
        if (!this.pname.equalsIgnoreCase("Custom") || this.appdata.length() >= 3) {
            return true;
        }
        return false;
    }

    public boolean isSleep() {
        return this.sleep;
    }

    public long lsleep() {
        if (this.sleep) {
            return 1;
        }
        return 0;
    }

    public void setSleep(boolean sleep) {
        this.sleep = sleep;
    }

    public void setSleep(int sleep1) {
        if (sleep1 > 0) {
            this.sleep = true;
        } else {
            this.sleep = false;
        }
    }

    public boolean isCarmode() {
        return this.carmode;
    }

    public long lcarmode() {
        if (this.carmode) {
            return 1;
        }
        return 0;
    }

    public void setCarmode(boolean carmode) {
        this.carmode = carmode;
    }

    public void setCarmode(int cm) {
        if (cm > 0) {
            this.carmode = true;
        } else {
            this.carmode = false;
        }
    }
}
