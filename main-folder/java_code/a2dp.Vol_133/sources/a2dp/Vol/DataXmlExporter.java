package a2dp.Vol;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DataXmlExporter {
    private static final String DATASUBDIRECTORY = (Environment.getExternalStorageDirectory() + "/A2DPVol");
    private SQLiteDatabase db;
    private XmlBuilder xmlBuilder;

    class XmlBuilder {
        private static final String CLOSE_WITH_TICK = "'>";
        private static final String COL_CLOSE = "</col>";
        private static final String COL_OPEN = "<col name='";
        private static final String DB_CLOSE = "</database>";
        private static final String DB_OPEN = "<database name='";
        private static final String OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        private static final String ROW_CLOSE = "</row>";
        private static final String ROW_OPEN = "<row>";
        private static final String TABLE_CLOSE = "</table>";
        private static final String TABLE_OPEN = "<table name='";
        private final StringBuilder sb = new StringBuilder();

        void start(String dbName) {
            this.sb.append(OPEN_XML_STANZA);
            this.sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK);
        }

        String end() throws IOException {
            this.sb.append(DB_CLOSE);
            return this.sb.toString();
        }

        void openTable(String tableName) {
            this.sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK);
        }

        void closeTable() {
            this.sb.append(TABLE_CLOSE);
        }

        void openRow() {
            this.sb.append(ROW_OPEN);
        }

        void closeRow() {
            this.sb.append(ROW_CLOSE);
        }

        void addColumn(String name, String val) throws IOException {
            this.sb.append(COL_OPEN + name + CLOSE_WITH_TICK + val + COL_CLOSE);
        }
    }

    public DataXmlExporter(SQLiteDatabase db) {
        this.db = db;
    }

    public void export(String dbName, String exportFileNamePrefix) throws IOException {
        this.xmlBuilder = new XmlBuilder();
        this.xmlBuilder.start(dbName);
        Cursor c = this.db.rawQuery("select * from sqlite_master", new String[0]);
        if (c.moveToFirst()) {
            do {
                String tableName = c.getString(c.getColumnIndex("name"));
                if (!(tableName.equals("android_metadata") || tableName.equals("sqlite_sequence") || tableName.startsWith("uidx") || tableName.startsWith("sqlite"))) {
                    exportTable(tableName);
                }
            } while (c.moveToNext());
        }
        try {
            writeToFile(this.xmlBuilder.end(), exportFileNamePrefix + ".xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportTable(String tableName) throws IOException {
        this.xmlBuilder.openTable(tableName);
        Cursor c = this.db.rawQuery("select * from " + tableName, new String[0]);
        if (c.moveToFirst()) {
            int cols = c.getColumnCount();
            do {
                this.xmlBuilder.openRow();
                for (int i = 0; i < cols; i++) {
                    this.xmlBuilder.addColumn(c.getColumnName(i), c.getString(i));
                }
                this.xmlBuilder.closeRow();
            } while (c.moveToNext());
        }
        c.close();
        this.xmlBuilder.closeTable();
    }

    private void writeToFile(String xmlString, String exportFileName) throws IOException {
        File dir = new File(DATASUBDIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, exportFileName);
        file.createNewFile();
        ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
        FileChannel channel = new FileOutputStream(file).getChannel();
        try {
            channel.write(buff);
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
}
