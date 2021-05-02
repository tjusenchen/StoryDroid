package a2dp.Vol;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ALauncher extends Service {
    public void onCreate() {
        try {
            byte[] buff = new byte[250];
            FileInputStream fs = openFileInput("My_Last_Location");
            fs.read(buff);
            fs.close();
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(new String(buff).trim()));
            intent.addFlags(268435456);
            startActivity(intent);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "No data", 1).show();
            e.printStackTrace();
        } catch (IOException e2) {
            Toast.makeText(this, "Some IO issue", 1).show();
            e2.printStackTrace();
        }
        super.onCreate();
        stopSelf();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
