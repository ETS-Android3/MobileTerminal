package edi.md.mobile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import edi.md.mobile.NetworkUtils.ApiRetrofit;
import edi.md.mobile.NetworkUtils.RetrofitResults.GetAssortmentRemainResults;
import edi.md.mobile.NetworkUtils.RetrofitResults.Remain;
import edi.md.mobile.NetworkUtils.Services.CommandService;
import edi.md.mobile.Utils.AssortmentParcelable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;

public class CountListOfAssortment extends AppCompatActivity {
    final Context context = this;
    String ip_,port_,mNameAssortment,WareUid;
    boolean mIntegerSales;
    EditText Count_enter;
    ImageButton btn_plus,btn_del;
    TextView name_forasl,price_forasl,btn_save,btn_cancel,txtCode,txtBarCode,txtMarking,txtRemain, txtUnit;
    JSONObject sendRevision;
    ProgressDialog pgH;
    ProgressBar pgBarStock;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Activity activity=CountListOfAssortment.this;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
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

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_count_list_asl);
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
        txtMarking = findViewById(R.id.txtMarking_asortment_count_list_asl);
        txtRemain = findViewById(R.id.txtStoc_asortment_count_list_asl);
        txtUnit = findViewById(R.id.txt_unit_list_assortment);
        pgH = new ProgressDialog(CountListOfAssortment.this);
        pgBarStock = findViewById(R.id.progressBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences sPref = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);

        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            Count_enter.requestFocus();
        }
        boolean ShowCode = Settings.getBoolean("ShowCode", false);

        Intent sales = getIntent();
        AssortmentParcelable assortment = sales.getParcelableExtra(AssortimentClickentSendIntent);
        mNameAssortment = assortment.getName();
        final String mPriceAssortment = assortment.getPrice();
        final String mMarkingAssortment = assortment.getMarking();
        final String mCodeAssortment = assortment.getCode();
        final String mBarcodeAssortment = assortment.getBarCode();
        final String mRemainAssortment = assortment.getRemain();
        final String mIDAssortment = assortment.getAssortimentID();
        final boolean mAllowNotIntegerSales =Boolean.parseBoolean(assortment.getAllowNonIntegerSale());
        txtUnit.setText(assortment.getUnit());

        if(mAllowNotIntegerSales)
            mIntegerSales = true;
        else
            mIntegerSales = false;

        WareUid = sales.getStringExtra("WareUid");

        boolean verifyStock = Settings.getBoolean("CheckStockToServer", false);

        if(verifyStock){
            checkStock(mIDAssortment,WareUid);
        }

        name_forasl.setText(mNameAssortment);
        price_forasl.setText(mPriceAssortment);


        if(mCodeAssortment==null || mCodeAssortment.equals("") || mCodeAssortment.equals("null") ){
            txtCode.setText("-");
        }else{
            if (!ShowCode)
                txtCode.setText("--------");
            else
                txtCode.setText(mCodeAssortment);
        }
        if(mMarkingAssortment==null || mMarkingAssortment.equals("") || mMarkingAssortment.equals("null") ){
            txtMarking.setText("-");
        }else{
            txtMarking.setText(mMarkingAssortment);
        }
        if(mRemainAssortment==null || mRemainAssortment.equals("") || mRemainAssortment.equals("null") ){
            txtRemain.setText("-");
        }else{
            txtRemain.setText(mRemainAssortment);
        }
        if(mBarcodeAssortment==null || mBarcodeAssortment.equals("") || mBarcodeAssortment.equals("null")){
            txtBarCode.setText("-");
        }else{
            txtBarCode.setText(mBarcodeAssortment);
        }

        Count_enter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (mIntegerSales) {
                    if (!isDouble(Count_enter.getText().toString())) Count_enter.setError(getResources().getString(R.string.msg_format_number_incorect));
                }
                else {
                    if (!isInteger(Count_enter.getText().toString())) Count_enter.setError(getResources().getString(R.string.msg_only_number_integer));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Count_enter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) saveCount();
                else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER) saveCount();
                return false;
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCount();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIntegerSales) {
                    if (isDouble(Count_enter.getText().toString())) {
                        Double curr = Double.valueOf(String.valueOf(Count_enter.getText()));
                        curr += 1;
                        Count_enter.setText(String.valueOf(curr));
                    }
                }
                else {
                    if (isInteger(Count_enter.getText().toString())) {
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
                if (mIntegerSales) {
                    if (isDouble(Count_enter.getText().toString())) {
                        Double curr = Double.valueOf(String.valueOf(Count_enter.getText()));
                        if (curr - 1 <= 0) {

                        } else {
                            curr -= 1;
                        }
                        Count_enter.setText(String.valueOf(curr));
                    }
                }
                else {
                    if (isInteger(Count_enter.getText().toString())) {
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

    private void checkStock(String mIDAssortment, String wareUid) {
        txtRemain.setVisibility(View.INVISIBLE);
        pgBarStock.setVisibility(View.VISIBLE);
        CommandService commandService = ApiRetrofit.getCommandService(CountListOfAssortment.this);
        Call<GetAssortmentRemainResults> call = commandService.getAssortimentRemains(mIDAssortment,wareUid);

        call.enqueue(new Callback<GetAssortmentRemainResults>() {
            @Override
            public void onResponse(Call<GetAssortmentRemainResults> call, Response<GetAssortmentRemainResults> response) {
                if(response.isSuccessful()){
                    GetAssortmentRemainResults remain = response.body();
                    if(remain != null && remain.getErrorCode() == 0){
                        List<Remain> remains = remain.getRemains();
                        if(remains.size() > 0){

                            for(Remain remain1 : remains){
                                String remaiId = remain1.getWarehouseID();
                                if(remaiId.equals(wareUid)){
                                    txtRemain.setText(String.format("%.2f",remain1.getRemain()));
                                    txtRemain.setVisibility(View.VISIBLE);
                                    pgBarStock.setVisibility(View.INVISIBLE);
                                    break;
                                }
                            }
                        }
                        else{
                            txtRemain.setVisibility(View.VISIBLE);
                            pgBarStock.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        txtRemain.setVisibility(View.VISIBLE);
                        pgBarStock.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    txtRemain.setVisibility(View.VISIBLE);
                    pgBarStock.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GetAssortmentRemainResults> call, Throwable t) {
                txtRemain.setVisibility(View.VISIBLE);
                pgBarStock.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void saveCount(){
        if (mIntegerSales) {
            if (isDouble(Count_enter.getText().toString())) {
                Intent intent = new Intent();
                intent.putExtra("Name", mNameAssortment);
                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            if (isInteger(Count_enter.getText().toString())) {
                Intent intent = new Intent();
                intent.putExtra("Name",mNameAssortment);
                intent.putExtra("count", String.valueOf(Count_enter.getText()));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
    private boolean isDouble(String s) throws NumberFormatException {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {

            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortment.this);
            return false;
        }
    }
    private boolean isInteger(String s) throws NumberFormatException {
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
                    int ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        Intent intent = new Intent();
                        intent.putExtra("count", String.valueOf(Count_enter.getText()));
                        setResult(RESULT_OK, intent);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                Count_enter.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        Count_enter.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        Count_enter.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        Count_enter.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        Count_enter.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        Count_enter.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        Count_enter.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        Count_enter.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        Count_enter.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        Count_enter.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        Count_enter.append("0");
                    }break;
                    case KeyEvent.KEYCODE_STAR : {
                        Count_enter.append(".");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = Count_enter.getText().toString();
                        if(!Count_enter.getText().toString().equals("")) {
                            Count_enter.setText(test.substring(0, test.length() - 1));
                            Count_enter.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
