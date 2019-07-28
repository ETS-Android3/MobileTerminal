package edi.md.mobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetClienta;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetClient;


public class GetClient extends AppCompatActivity {
    EditText codeClient;
    Button btnOk, btnCancel;
    String ip_,port_,ClientID;
    ProgressDialog pgH;
    Boolean onedate =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.header_login_client_activity);
        setContentView(R.layout.activity_get_client);

        Toolbar toolbar = findViewById(R.id.toolbar_getClient);
        setSupportActionBar(toolbar);

        codeClient=findViewById(R.id.passwords_client);
        btnCancel=findViewById(R.id.btn_cancel_getClient);
        btnOk=findViewById(R.id.btn_ok_getClient);
        pgH =new ProgressDialog(GetClient.this);

        SharedPreferences Seting =getSharedPreferences("Settings", MODE_PRIVATE);
        ip_=Seting.getString("IP","");
        port_=Seting.getString("Port","");

        codeClient.requestFocus();

        Boolean showKB = Seting.getBoolean("ShowKeyBoard",false);
        if (showKB){
            //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            codeClient.requestFocus();
        }
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage("loading..");
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();
                ClientID = codeClient.getText().toString();
                URL getClient =GetClienta(ip_,port_,ClientID);
                new AsyncTask_GetClient().execute(getClient);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        codeClient.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                pgH.setMessage("loading..");
                pgH.setCancelable(false);
                pgH.setIndeterminate(true);
                pgH.show();

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ClientID = codeClient.getText().toString();
                    URL getClient =GetClienta(ip_,port_,ClientID);
                    new AsyncTask_GetClient().execute(getClient);
                }
                else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if (!onedate) {
                        ClientID = codeClient.getText().toString();
                        URL getClient =GetClienta(ip_,port_,ClientID);
                        new AsyncTask_GetClient().execute(getClient);
                        onedate = true;
                    }
                }
                return false;
            }

        });

    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }

        return super.dispatchTouchEvent(event);
    }

    class AsyncTask_GetClient extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String response ="";
            try {
                response = Response_from_GetClient(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),GetClient.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject response12 = new JSONObject(response);
                int errorCode = response12.getInt("ErrorCode");
                String Namexx= response12.getString("ClientName");
                if (errorCode==0 && !Namexx.equals("null")){
                    pgH.dismiss();
                    String CardID= response12.getString("ClientID");
                    String Name= response12.getString("ClientName");
                    Intent returns= new Intent();
                    returns.putExtra("ClientID",CardID);
                    returns.putExtra("ClientName",Name);
                    setResult(RESULT_OK,returns);
                    finish();
                }else{
                    pgH.dismiss();
                    onedate=false;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(GetClient.this);
                    dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                    dialog.setCancelable(false);
                    dialog.setMessage(getResources().getString(R.string.msg_client_not_found));
                    dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),GetClient.this);
            }

        }
    }
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            View mDecorView = getWindow().getDecorView();
//            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }
}
