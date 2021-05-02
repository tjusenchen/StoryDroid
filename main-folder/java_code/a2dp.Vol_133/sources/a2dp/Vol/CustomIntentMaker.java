package a2dp.Vol;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import java.net.URISyntaxException;

public class CustomIntentMaker extends Activity {
    private Button mBtnCancel;
    private OnClickListener mBtnCancelOnClick = new OnClickListener() {
        public void onClick(View v) {
            CustomIntentMaker.this.setResult(0);
            CustomIntentMaker.this.finish();
        }
    };
    private Button mBtnOk;
    private OnClickListener mBtnOkOnClick = new OnClickListener() {
        public void onClick(View v) {
            CustomIntentMaker.this.setResult(-1, new Intent());
            CustomIntentMaker.this.finish();
        }
    };
    private Button mBtnTest;
    private OnClickListener mBtnTestOnClick = new OnClickListener() {
        public void onClick(View v) {
            String action = CustomIntentMaker.this.mEtAction.getText().toString();
            String data = CustomIntentMaker.this.mEtData.getText().toString();
            String type = CustomIntentMaker.this.mEtType.getText().toString();
            if (action.length() >= 3 || data.length() >= 3 || type.length() >= 3) {
                Intent i;
                if (CustomIntentMaker.isShortcutIntent(data)) {
                    try {
                        i = Intent.getIntent(data);
                    } catch (URISyntaxException e) {
                        i = new Intent();
                        e.printStackTrace();
                    }
                } else {
                    i = new Intent();
                    if (!(action == null || action.equals(""))) {
                        i.setAction(action);
                    }
                    if (!data.equals("")) {
                        try {
                            i.setData(Uri.parse(data));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            return;
                        }
                    }
                    if (!type.equals("")) {
                        i.setType(type);
                    }
                }
                if ("android.intent.action.CALL".equals(i.getAction())) {
                    AudioManager am = (AudioManager) CustomIntentMaker.this.getBaseContext().getSystemService("audio");
                    am.setMode(2);
                    am.setSpeakerphoneOn(true);
                    am.setStreamVolume(0, am.getStreamMaxVolume(0), 1);
                }
                try {
                    CustomIntentMaker.this.startActivity(i);
                    CustomIntentMaker.this.mBtnOk.setEnabled(true);
                } catch (Exception e22) {
                    e22.printStackTrace();
                }
            }
        }
    };
    private EditText mEtAction;
    private EditText mEtData;
    private EditText mEtType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_intent);
        setTitle("Custom Intent...");
        initViews();
        assignListeners();
        this.mBtnOk.setEnabled(false);
    }

    private void initViews() {
        this.mEtAction = (EditText) findViewById(R.id.ci_et_action);
        this.mEtData = (EditText) findViewById(R.id.ci_et_data);
        this.mEtType = (EditText) findViewById(R.id.ci_et_type);
        this.mBtnOk = (Button) findViewById(R.id.ci_btn_ok);
        this.mBtnCancel = (Button) findViewById(R.id.ci_btn_cancel);
        this.mBtnTest = (Button) findViewById(R.id.ci_btn_test);
    }

    private void assignListeners() {
        this.mBtnOk.setOnClickListener(this.mBtnOkOnClick);
        this.mBtnCancel.setOnClickListener(this.mBtnCancelOnClick);
        this.mBtnTest.setOnClickListener(this.mBtnTestOnClick);
    }

    public static boolean isShortcutIntent(String data) {
        String lcase = data.toLowerCase();
        return lcase.startsWith("intent:") || lcase.contains("#intent");
    }
}
