package a2dp.Vol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class ManageData extends Activity {
    String a2dpDir;
    private MyApplication application;
    private Button exportDbToSdButton;
    private Button exportDbXmlToSdButton;
    private Button exportLoc;
    private Button importDB;
    private TextView output = ((TextView) null);
    private TextView path = ((TextView) null);
    private String pathstr;

    private class ExportDataAsXmlTask extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog;

        private ExportDataAsXmlTask() {
            this.dialog = new ProgressDialog(ManageData.this);
        }

        /* synthetic */ ExportDataAsXmlTask(ManageData x0, AnonymousClass1 x1) {
            this();
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database as XML...");
            this.dialog.show();
        }

        protected String doInBackground(String... args) {
            DataXmlExporter dm = new DataXmlExporter(ManageData.this.application.getDeviceDB().getDb());
            try {
                String dbName = args[0];
                String exportFileName = args[1];
                dm.export(dbName, exportFileName);
                ManageData.this.pathstr = ManageData.this.a2dpDir + "/" + exportFileName + ".xml";
                return null;
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return e.getMessage();
            }
        }

        protected void onPostExecute(String errMsg) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (errMsg == null) {
                Toast.makeText(ManageData.this, "Export successful!", 0).show();
                ManageData.this.path.setText("Exported to: " + ManageData.this.pathstr);
                return;
            }
            Toast.makeText(ManageData.this, "Export failed - " + errMsg, 0).show();
            ManageData.this.path.setText("Export Failed");
        }
    }

    private class ExportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        private ExportDatabaseFileTask() {
            this.dialog = new ProgressDialog(ManageData.this);
        }

        /* synthetic */ ExportDatabaseFileTask(ManageData x0, AnonymousClass1 x1) {
            this();
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting database...");
            this.dialog.show();
        }

        protected Boolean doInBackground(String... args) {
            File dbFile = new File(ManageData.this.application.getDeviceDB().getDb().getPath());
            File exportDir = new File(ManageData.this.a2dpDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());
            ManageData.this.pathstr = file.getPath();
            try {
                file.createNewFile();
                copyFile(dbFile, file);
                return Boolean.valueOf(true);
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success.booleanValue()) {
                Toast.makeText(ManageData.this, "Export successful!", 0).show();
                ManageData.this.path.setText("Exported to: " + ManageData.this.pathstr);
                return;
            }
            Toast.makeText(ManageData.this, "Export failed", 0).show();
            ManageData.this.path.setText("Export Failed");
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            }
        }
    }

    private class ExportLocationTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        private ExportLocationTask() {
            this.dialog = new ProgressDialog(ManageData.this);
        }

        /* synthetic */ ExportLocationTask(ManageData x0, AnonymousClass1 x1) {
            this();
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Exporting location data...");
            this.dialog.show();
        }

        protected Boolean doInBackground(String... args) {
            File LocFile = ManageData.this.application.getFileStreamPath(args[0]);
            File exportDir = new File(ManageData.this.a2dpDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, LocFile.getName() + ".txt");
            ManageData.this.pathstr = file.getPath();
            try {
                file.createNewFile();
                copyFile(LocFile, file);
                return Boolean.valueOf(true);
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success.booleanValue()) {
                ManageData.this.path.setText("Exported to: " + ManageData.this.pathstr);
                Toast.makeText(ManageData.this, "Location data exported", 1).show();
                return;
            }
            Toast.makeText(ManageData.this, "Export failed", 0).show();
            ManageData.this.path.setText("Export Failed");
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            }
        }
    }

    private class ImportDatabaseFileTask extends AsyncTask<String, Void, Boolean> {
        private final ProgressDialog dialog;

        private ImportDatabaseFileTask() {
            this.dialog = new ProgressDialog(ManageData.this);
        }

        /* synthetic */ ImportDatabaseFileTask(ManageData x0, AnonymousClass1 x1) {
            this();
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Importing database...");
            this.dialog.show();
        }

        protected Boolean doInBackground(String... args) {
            File dbFile = new File(ManageData.this.application.getDeviceDB().getDb().getPath());
            File exportDir = new File(ManageData.this.a2dpDir);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, dbFile.getName());
            ManageData.this.pathstr = file.getPath();
            try {
                file.createNewFile();
                copyFile(file, dbFile);
                return Boolean.valueOf(true);
            } catch (IOException e) {
                Log.e(MyApplication.APP_NAME, e.getMessage(), e);
                return Boolean.valueOf(false);
            }
        }

        protected void onPostExecute(Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            if (success.booleanValue()) {
                ManageData.this.path.setText("Imported from: " + ManageData.this.pathstr);
                String Ireload = "a2dp.vol.Main.RELOAD_LIST";
                Intent itent = new Intent();
                itent.setAction("a2dp.vol.Main.RELOAD_LIST");
                itent.putExtra("device", "");
                ManageData.this.application.sendBroadcast(itent);
                Toast.makeText(ManageData.this, R.string.ImportCompletedText, 0).show();
                return;
            }
            Toast.makeText(ManageData.this, "Import failed", 0).show();
            ManageData.this.path.setText("Import Failed");
        }

        void copyFile(File src, File dst) throws IOException {
            FileChannel inChannel = new FileInputStream(src).getChannel();
            FileChannel outChannel = new FileOutputStream(dst).getChannel();
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
                if (outChannel != null) {
                    outChannel.close();
                }
            }
        }
    }

    private class SelectDataTask extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog;

        private SelectDataTask() {
            this.dialog = new ProgressDialog(ManageData.this);
        }

        /* synthetic */ SelectDataTask(ManageData x0, AnonymousClass1 x1) {
            this();
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Selecting data...");
            this.dialog.show();
        }

        protected String doInBackground(String... args) {
            List<String> names = ManageData.this.application.getDeviceDB().selectAll();
            StringBuilder sb = new StringBuilder();
            for (String name : names) {
                sb.append(name + "\n");
            }
            return sb.toString();
        }

        protected void onPostExecute(String result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            ManageData.this.output.setText(result);
        }
    }

    public void finish() {
        setResult(-1, new Intent());
        super.finish();
    }

    protected void onDestroy() {
        setResult(-1, new Intent());
        super.onDestroy();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.application = (MyApplication) getApplication();
        this.a2dpDir = Environment.getExternalStorageDirectory() + "/A2DPVol";
        setContentView(R.layout.managedata);
        this.output = (TextView) findViewById(R.id.Output);
        this.path = (TextView) findViewById(R.id.Path);
        new SelectDataTask().execute(new String[0]);
        this.exportDbToSdButton = (Button) findViewById(R.id.exportdbtosdbutton);
        this.exportDbToSdButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportDatabaseFileTask(ManageData.this, null).execute(new String[0]);
                } else {
                    Toast.makeText(ManageData.this, "External storage is not available, unable to export data.", 0).show();
                }
            }
        });
        this.exportDbXmlToSdButton = (Button) findViewById(R.id.exportdbxmltosdbutton);
        this.exportDbXmlToSdButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportDataAsXmlTask(ManageData.this, null).execute(new String[]{"devices", "A2DPDevices"});
                    return;
                }
                Toast.makeText(ManageData.this, "External storage is not available, unable to export data.", 0).show();
            }
        });
        this.importDB = (Button) findViewById(R.id.ImportDBButton);
        this.importDB.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ImportDatabaseFileTask(ManageData.this, null).execute(new String[]{"devices", ManageData.this.a2dpDir});
                    return;
                }
                Toast.makeText(ManageData.this, "External storage is not available, unable to import data.", 0).show();
            }
        });
        this.exportLoc = (Button) findViewById(R.id.ExportLoc);
        this.exportLoc.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ManageData.this.isExternalStorageAvail()) {
                    new ExportLocationTask(ManageData.this, null).execute(new String[]{"My_Last_Location", ManageData.this.a2dpDir});
                    return;
                }
                Toast.makeText(ManageData.this, "External storage is not available, unable to export data.", 0).show();
            }
        });
    }

    public void onPause() {
        super.onPause();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    private boolean isExternalStorageAvail() {
        return Environment.getExternalStorageState().equals("mounted");
    }
}
