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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static edi.md.mobile.NetworkUtils.NetworkUtils.Autentificare;

public class Login extends AppCompatActivity {
    Button ok_btn, cancel_btn;
    EditText password;
    JSONObject sendCodeObj;
    ProgressDialog pgH;
    String ip_,port_,PinCode;
    Boolean onedate =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.header_login_user_activity);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);

        cancel_btn=findViewById(R.id.btn_cancel_login);
        ok_btn=findViewById(R.id.btn_ok_login);
        password=findViewById(R.id.passwords_login);

        final SharedPreferences Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");
        pgH=new ProgressDialog(Login.this);

        PinCode=Settings.getString("PinCode","");

        password.requestFocus();

        boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        if (showKB){
            //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
//            password.setInputType(InputType.TYPE_CLASS_NUMBER);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            password.requestFocus();
        }

        Intent getActivity = getIntent();
        final int id_ = getActivity.getIntExtra("Activity", 101);

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Variables)getApplication()).setLoginVariable(false);
                finish();
            }
        });
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setCancelable(false);
                pgH.setIndeterminate(true);
                pgH.show();

                if(id_==8) {
                    String code = password.getText().toString();
                    PinCode=Settings.getString("PinCod","");
                    String PinCodAdmin = Settings.getString("PinCodAdmin","");
                    if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                        pgH.dismiss();
                        SharedPreferences.Editor inpNew = Settings.edit();
                        inpNew.putBoolean("SetPin",false);
                        inpNew.apply();
                        Intent Seting= new Intent(".MenuWorkPlace");
                        startActivity(Seting);
                        finish();
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                    }
                }else if (id_==9){
                    String code = password.getText().toString();
                    PinCode=Settings.getString("PinCod","");
                    String PinCodAdmin = Settings.getString("PinCodAdmin","");
                    if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                        pgH.dismiss();
                        SharedPreferences.Editor inpNew = Settings.edit();
                        inpNew.putBoolean("SetPin",false);
                        inpNew.apply();
                        Intent Seting= new Intent(".MenuPrinters");
                        startActivity(Seting);
                        finish();
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                    }
                } else if (id_==10){
                    String code = password.getText().toString();
                    PinCode=Settings.getString("PinCod","");
                    String PinCodAdmin = Settings.getString("PinCodAdmin","");
                    if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                        pgH.dismiss();
                        SharedPreferences.Editor inpNew = Settings.edit();
                        inpNew.putBoolean("SetPin",false);
                        inpNew.apply();
                        Intent Seting= new Intent(".MenuSecuritate");
                        startActivity(Seting);
                        finish();
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                    }
                }else{
                    {
                        sendCodeObj = new JSONObject();
                        try {
                            sendCodeObj.put("Code", password.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), Login.this);
                        }
                        URL Url_Autentificare = Autentificare(ip_, port_);
                        new AsyncTask_Autentificare().execute(Url_Autentificare);
                    }
                }
            }
        });
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                pgH.setMessage("loading..");
                pgH.setCancelable(false);
                pgH.setIndeterminate(true);
                pgH.show();

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(id_==8) {
                        String code = password.getText().toString();
                        PinCode=Settings.getString("PinCod","");
                        String PinCodAdmin = Settings.getString("PinCodAdmin","");
                        if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                            pgH.dismiss();
                            SharedPreferences.Editor inpNew = Settings.edit();
                            inpNew.putBoolean("SetPin",false);
                            inpNew.apply();
                            Intent Seting= new Intent(".MenuWorkPlace");
                            startActivity(Seting);
                            finish();
                        }else{
                            pgH.dismiss();
                            Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                        }
                    }else if (id_==9){
                        String code = password.getText().toString();
                        PinCode=Settings.getString("PinCod","");
                        String PinCodAdmin = Settings.getString("PinCodAdmin","");
                        if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                            pgH.dismiss();
                            SharedPreferences.Editor inpNew = Settings.edit();
                            inpNew.putBoolean("SetPin",false);
                            inpNew.apply();
                            Intent Seting= new Intent(".MenuPrinters");
                            startActivity(Seting);
                            finish();
                        }else{
                            pgH.dismiss();
                            Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                        }
                    } else if (id_==10){
                        String code = password.getText().toString();
                        PinCode=Settings.getString("PinCod","");
                        String PinCodAdmin = Settings.getString("PinCodAdmin","");
                        if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                            pgH.dismiss();
                            SharedPreferences.Editor inpNew = Settings.edit();
                            inpNew.putBoolean("SetPin",false);
                            inpNew.apply();
                            Intent Seting= new Intent(".MenuSecuritate");
                            startActivity(Seting);
                            finish();
                        }else{
                            pgH.dismiss();
                            Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        {
                            sendCodeObj = new JSONObject();
                            try {
                                sendCodeObj.put("Code", password.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), Login.this);
                            }
                            URL Url_Autentificare = Autentificare(ip_, port_);
                            new AsyncTask_Autentificare().execute(Url_Autentificare);

                            return true;
                        }
                    }
                 }
                 else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if (!onedate) {
                        if(id_==8) {
                            String code = password.getText().toString();
                            PinCode=Settings.getString("PinCod","");
                            String PinCodAdmin = Settings.getString("PinCodAdmin","");
                            if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                                pgH.dismiss();
                                SharedPreferences.Editor inpNew = Settings.edit();
                                inpNew.putBoolean("SetPin",false);
                                inpNew.apply();
                                Intent Seting= new Intent(".MenuWorkPlace");
                                startActivity(Seting);
                                finish();
                            }else{
                                pgH.dismiss();
                                Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                            }
                        }else if (id_==9){
                            String code = password.getText().toString();
                            PinCode=Settings.getString("PinCod","");
                            String PinCodAdmin = Settings.getString("PinCodAdmin","");
                            if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                                pgH.dismiss();
                                SharedPreferences.Editor inpNew = Settings.edit();
                                inpNew.putBoolean("SetPin",false);
                                inpNew.apply();
                                Intent Seting= new Intent(".MenuPrinters");
                                startActivity(Seting);
                                finish();
                            }else{
                                pgH.dismiss();
                                Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                            }
                        } else if (id_==10){
                            String code = password.getText().toString();
                            PinCode=Settings.getString("PinCod","");
                            String PinCodAdmin = Settings.getString("PinCodAdmin","");
                            if(TestInputPin(code,PinCode) || TestInputPin(code,PinCodAdmin)){
                                pgH.dismiss();
                                SharedPreferences.Editor inpNew = Settings.edit();
                                inpNew.putBoolean("SetPin",false);
                                inpNew.apply();
                                Intent Seting= new Intent(".MenuSecuritate");
                                startActivity(Seting);
                                finish();
                            }else{
                                pgH.dismiss();
                                Toast.makeText(Login.this,getResources().getString(R.string.msg_login_cod_incorect),Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            {
                                sendCodeObj = new JSONObject();
                                try {
                                    sendCodeObj.put("Code", password.getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(), Login.this);
                                }
                                URL Url_Autentificare = Autentificare(ip_, port_);
                                new AsyncTask_Autentificare().execute(Url_Autentificare);

                                return true;
                            }
                        }
                    }
                }
                return false;
            }

        });

    }

    public String getResponse_from_Autenficare(URL send_bills) {
        StringBuilder data = new StringBuilder();
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(3000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());
            wr.writeBytes(String.valueOf(sendCodeObj));
            wr.flush();
            wr.close();

            InputStream in = send_bill_Connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            int inputStreamData = inputStreamReader.read();

            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                data.append(current);
            }


        } catch (Exception e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(), Login.this);
        } finally {
            assert send_bill_Connection != null;
            send_bill_Connection.disconnect();
        }
        return data.toString();
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    public boolean TestInputPin (String key,String entern_key){
        return key.equals(entern_key);
    }

    class AsyncTask_Autentificare extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            return getResponse_from_Autenficare(urls[0]);
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if (response==null) {
                onedate=false;
                final AlertDialog.Builder eroare = new AlertDialog.Builder(Login.this);
                eroare.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                eroare.setMessage(getResources().getString(R.string.msg_nu_raspuns_server));
                eroare.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                eroare.show();
                password.requestFocus();
            } else {
                try {
                    JSONObject response_from_Autentificare = new JSONObject(response);
                    int errore_code = response_from_Autentificare.getInt("ErrorCode");
                    if (errore_code == 0) {
                        String Name = response_from_Autentificare.getString("Name");
                        String UserID = response_from_Autentificare.getString("UserID");

                        ((Variables) getApplication()).setLoginVariable(true);
                        SharedPreferences LogIn = getSharedPreferences("User", MODE_PRIVATE);
                        SharedPreferences.Editor input_LogIn = LogIn.edit();
                        input_LogIn.putString("Name", Name);
                        input_LogIn.putString("UserID", UserID);
                        input_LogIn.apply();

                        Intent getActivity = getIntent();
                        final int id = getActivity.getIntExtra("Activity", 101);
                        switch (id) {
                            case 1: {
                                Intent checkprice = new Intent(".CheckPriceMobile");
                                startActivity(checkprice);
                                finish();
                            }
                            break;
                            case 2: {
                                Intent sales = new Intent(".SalesMobile");
                                startActivity(sales);
                                finish();
                            }
                            break;
                            case 3: {
                                Intent invoice = new Intent(".InvoiceMobile");
                                startActivity(invoice);
                                finish();
                            }
                            break;
                            case 4: {
                                Intent Tranfer = new Intent(".TransferMobile");
                                startActivity(Tranfer);
                                finish();
                            }
                            break;
                            case 5: {
                                Intent StockInventory = new Intent(".RevisionMobile");
                                startActivity(StockInventory);
                                finish();
                            }
                            break;
                            case 6: {
                                Intent workPlace = new Intent();
                                setResult(RESULT_OK,workPlace);
                                finish();
                            }
                            break;
                            case 7: {
                                Intent StockAsortiment = new Intent(".StockAssortmentMobile");
                                startActivity(StockAsortiment);
                                finish();
                            }
                            break;
                        }
                    } else {
                        onedate=false;
                        final AlertDialog.Builder eroare = new AlertDialog.Builder(Login.this);
                        eroare.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        eroare.setMessage(getResources().getString(R.string.msg_error_code) + errore_code);
                        eroare.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        eroare.show();
                        password.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), Login.this);
                }

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
