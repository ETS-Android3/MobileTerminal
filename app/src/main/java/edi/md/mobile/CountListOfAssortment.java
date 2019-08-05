package edi.md.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveRevisionLine;

public class CountListOfAssortment extends AppCompatActivity {
    final Context context = this;
    String name_asl,price_asl,code,barcode,ip_,port_,UserId,IntegerSales;

    EditText Count_enter;
    ImageButton btn_plus,btn_del;
    TextView name_forasl,price_forasl,ViewCom,btn_save,btn_cancel,txtCode,txtBarCode;
    int h,k,g=1;
    int i = 0;
    JSONObject sendRevision;
    ArrayList com_lists;
    ProgressDialog pgH;
    private Toolbar mToolbar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Activity activity=CountListOfAssortment.this;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                finish();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_list_of_assortment);

        mToolbar = (Toolbar) findViewById(R.id.toolbar_count_list_asl);
        setSupportActionBar(mToolbar);

        Count_enter = findViewById(R.id.edt_cnt_list_asl);
        btn_plus = findViewById(R.id.image_btn_add_list_asl);
        btn_del = findViewById(R.id.image_btn_del_list_asl);
        btn_save = findViewById(R.id.btn_add_count_list_asl);
        btn_cancel = findViewById(R.id.btn_cancel_count_list_asl);
        name_forasl = findViewById(R.id.txt_name_assortment_count_list_asl);
        price_forasl = findViewById(R.id.txtPrice_asortment_count_list_asl);
        txtCode = findViewById(R.id.txtcode_assortment_count_list_asl);
        txtBarCode = findViewById(R.id.txtBarcode_assortment_count_list_asl);
        pgH = new ProgressDialog(CountListOfAssortment.this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        SharedPreferences sPref = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);

        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            Count_enter.requestFocus();
        }
        final String uid_save = sPref.getString("Guid_Assortiment", "");
        name_asl = sPref.getString("Name_Assortiment", "");
        price_asl = sPref.getString("Price_Assortiment", "");
        IntegerSales = sPref.getString("AllowNonIntegerSale", "false");
        code = sPref.getString("Code_Assortiment", "");
        barcode = sPref.getString("BarCode_Assortiment", "");

        name_forasl.setText(name_asl);
        price_forasl.setText(price_asl);
        txtCode.setText(code);
        txtBarCode.setText(barcode);

        Count_enter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (IntegerSales.equals("true")) {
                    if (isDigits(String.valueOf(charSequence))) {
                    } else {
                        Count_enter.setError(getResources().getString(R.string.msg_format_number_incorect));
                    }
                } else {
                    if (isDigitInteger(Count_enter.getText().toString())) {
                    } else {
                        Count_enter.setError(getResources().getString(R.string.msg_only_number_integer));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Count_enter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Intent getIntetn = getIntent();
                    int type = getIntetn.getIntExtra("NonAutoCount",777);
                    if(type==717){
                        SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                        String revisions =Revision.getString("RevisionID","");
                        if (IntegerSales.equals("true")) {
                            if (isDigits(Count_enter.getText().toString())) {
                                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                                pgH.setIndeterminate(true);
                                pgH.setCancelable(false);
                                pgH.show();
                                sendRevision = new JSONObject();
                                try {
                                    sendRevision.put("Assortiment", uid_save);
                                    sendRevision.put("Quantity", "1");
                                    sendRevision.put("RevisionID", revisions);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                                }
                                URL generateSaveLine = SaveRevisionLine(ip_, port_);
                                new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                            }
                        }else {
                            if (isDigitInteger(Count_enter.getText().toString())) {
                                pgH.setMessage("loading..");
                                pgH.setIndeterminate(true);
                                pgH.setCancelable(false);
                                pgH.show();
                                sendRevision = new JSONObject();
                                try {
                                    sendRevision.put("Assortiment", uid_save);
                                    sendRevision.put("Quantity", "1");
                                    sendRevision.put("RevisionID", revisions);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                                }
                                URL generateSaveLine = SaveRevisionLine(ip_, port_);
                                new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                            }
                        }
                    }else{
                        if (IntegerSales.equals("true")) {
                            if (isDigits(Count_enter.getText().toString())) {
                                Intent intent = new Intent();
                                intent.putExtra("Name", name_asl);
                                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            if (isDigitInteger(Count_enter.getText().toString())) {
                                Intent intent = new Intent();
                                intent.putExtra("Name", name_asl);
                                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                    }
                }
                else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    Intent getIntetn = getIntent();
                    int type = getIntetn.getIntExtra("NonAutoCount",777);
                    if(type==717){
                        SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                        String revisions =Revision.getString("RevisionID","");
                        if (IntegerSales.equals("true")) {
                            if (isDigits(Count_enter.getText().toString())) {
                                pgH.setMessage("loading..");
                                pgH.setIndeterminate(true);
                                pgH.setCancelable(false);
                                pgH.show();
                                sendRevision = new JSONObject();
                                try {
                                    sendRevision.put("Assortiment", uid_save);
                                    sendRevision.put("Quantity", Count_enter.getText().toString());
                                    sendRevision.put("RevisionID", revisions);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                                }
                                URL generateSaveLine = SaveRevisionLine(ip_, port_);
                                new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                            }
                        }else {
                            if (isDigitInteger(Count_enter.getText().toString())) {
                                pgH.setMessage("loading..");
                                pgH.setIndeterminate(true);
                                pgH.setCancelable(false);
                                pgH.show();
                                sendRevision = new JSONObject();
                                try {
                                    sendRevision.put("Assortiment", uid_save);
                                    sendRevision.put("Quantity", Count_enter.getText().toString());
                                    sendRevision.put("RevisionID", revisions);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                                }
                                URL generateSaveLine = SaveRevisionLine(ip_, port_);
                                new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                            }
                        }
                    }else{
                        if (IntegerSales.equals("true")) {
                            if (isDigits(Count_enter.getText().toString())) {
                                Intent intent = new Intent();
                                intent.putExtra("Name", name_asl);
                                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            if (isDigitInteger(Count_enter.getText().toString())) {
                                Intent intent = new Intent();
                                intent.putExtra("Name", name_asl);
                                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                    }
                }
                return false;
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntetn = getIntent();
                int type = getIntetn.getIntExtra("NonAutoCount",777);
                if(type==717){
                    SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                    String revisions =Revision.getString("RevisionID","");
                    if (IntegerSales.equals("true")) {
                        if (isDigits(Count_enter.getText().toString())) {
                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                            pgH.setIndeterminate(true);
                            pgH.setCancelable(false);
                            pgH.show();
                            sendRevision = new JSONObject();
                            try {
                                sendRevision.put("Assortiment", uid_save);
                                sendRevision.put("Quantity", Count_enter.getText().toString());
                                sendRevision.put("RevisionID", revisions);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                            }
                            URL generateSaveLine = SaveRevisionLine(ip_, port_);
                            new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                        }
                    }else {
                        if (isDigitInteger(Count_enter.getText().toString())) {
                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                            pgH.setIndeterminate(true);
                            pgH.setCancelable(false);
                            pgH.show();
                            sendRevision = new JSONObject();
                            try {
                                sendRevision.put("Assortiment", uid_save);
                                sendRevision.put("Quantity", Count_enter.getText().toString());
                                sendRevision.put("RevisionID", revisions);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
                            }
                            URL generateSaveLine = SaveRevisionLine(ip_, port_);
                            new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                        }
                    }
                }else{
                    if (IntegerSales.equals("true")) {
                        if (isDigits(Count_enter.getText().toString())) {
                            Intent intent = new Intent();
                            intent.putExtra("Name", name_asl);
                            intent.putExtra("count", String.valueOf(Count_enter.getText()));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        if (isDigitInteger(Count_enter.getText().toString())) {
                            Intent intent = new Intent();
                            intent.putExtra("Name", name_asl);
                            intent.putExtra("count", String.valueOf(Count_enter.getText()));
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_cancel = new Intent();
                setResult(RESULT_CANCELED, intent_cancel);
                finish();
            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IntegerSales.equals("true")) {
                    if (isDigits(Count_enter.getText().toString())) {
                        Double curr = Double.valueOf(String.valueOf(Count_enter.getText()));
                        curr += 1;
                        Count_enter.setText(String.valueOf(curr));
                    }
                } else {
                    if (isDigitInteger(Count_enter.getText().toString())) {
                        Integer curr = Integer.valueOf(Count_enter.getText().toString());
                        curr += 1;
                        Count_enter.setText(String.valueOf(curr));
                    }
                }
            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IntegerSales.equals("true")) {
                    if (isDigits(Count_enter.getText().toString())) {
                        Double curr = Double.valueOf(String.valueOf(Count_enter.getText()));
                        if (curr - 1 <= 0) {

                        } else {
                            curr -= 1;
                        }
                        Count_enter.setText(String.valueOf(curr));
                    }
                } else {
                    if (isDigitInteger(Count_enter.getText().toString())) {
                        Integer curr = Integer.valueOf(Count_enter.getText().toString());
                        if (curr - 1 <= 0) {

                        } else {
                            curr -= 1;
                        }
                        Count_enter.setText(String.valueOf(curr));
                    }
                }
            }
        });
    }

    private boolean isDigits(String s) throws NumberFormatException {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {

            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
            return false;
        }
    }

    private boolean isDigitInteger(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
            return false;
        }
    }

    public String getResponseFromURLSaveRevisionLineAdd(URL send_bills) throws IOException {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(4000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());
            wr.writeBytes(String.valueOf(sendRevision));
            wr.flush();
            wr.close();

            InputStream in = send_bill_Connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            int inputStreamData = inputStreamReader.read();

            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                data += current;
            }

        } catch (Exception e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data;
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

    class AsyncTask_SaveRevisionLine extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response = "";
            try {
                response = getResponseFromURLSaveRevisionLineAdd(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables) getApplication()).appendLog(e.getMessage(), CountListOfAssortment.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if (!response.equals("")) {
                try {
                    JSONObject responseAssortiment = new JSONObject(response);
                    Integer ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        Intent intent_cancel = new Intent();
                        intent_cancel.putExtra("count", String.valueOf(Count_enter.getText()));
                        setResult(RESULT_OK, intent_cancel);
                        finish();
                    } else {
                        Intent intent_cancel = new Intent();
                        setResult(RESULT_CANCELED, intent_cancel);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), CountListOfAssortment.this);
                }
            } else {
                Intent intent_cancel = new Intent();
                setResult(RESULT_CANCELED, intent_cancel);
                finish();
            }


        }
    }
}
