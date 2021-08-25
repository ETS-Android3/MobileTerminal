package md.intelectsoft.stockmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.concurrent.TimeUnit;

//import md.intelectsoft.stockmanager.NetworkUtils.Services.CommandService;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.ResponseSimple;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static md.intelectsoft.stockmanager.ListAssortment.AssortimentClickentSendIntent;

public class SetAssortmentBarcode extends AppCompatActivity {

    Button btn_save, btn_cancel;
    TextView txtName, txtMarking,txtCode,txtPrice, txtUnit;
    EditText etBarcodeInput;

    String url, userId, assortmentId;
    ProgressDialog pgH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_assortment_barcode);

        Toolbar toolbar = findViewById(R.id.toolbar_set_barcode);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtName = findViewById(R.id.txt_name_assortment_set_barcode);
        txtCode = findViewById(R.id.txtcode_assortment_set_barcode);
        txtMarking = findViewById(R.id.txtMarking_asortment_set_barcode);
        txtPrice = findViewById(R.id.txtPrice_asortment_set_barcode);
        etBarcodeInput = findViewById(R.id.txt_input_barcode_for_assortment);
        btn_save = findViewById(R.id.btn_save_barcode);
        btn_cancel = findViewById(R.id.btn_cancel_set_barcode);
        txtUnit = findViewById(R.id.txt_unit_barcode);
        pgH = new ProgressDialog(SetAssortmentBarcode.this);

        userId =  getSharedPreferences("User", MODE_PRIVATE).getString("UserID",null);
        url = SPFHelp.getInstance().getString("URI","0.0.0.0:1111");

        Intent sales = getIntent();
        AssortmentParcelable assortment = sales.getParcelableExtra(AssortimentClickentSendIntent);

        assortmentId = assortment.getAssortimentID();
        txtUnit.setText("/" + assortment.getUnit());
        txtName.setText(assortment.getName());
        txtPrice.setText(assortment.getPrice());
        txtCode.setText(assortment.getCode());
        txtMarking.setText(assortment.getMarking());

        etBarcodeInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!etBarcodeInput.getText().toString().equals("")){
                        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();

                        saveBarcode(etBarcodeInput.getText().toString());
                    }
                    else
                        Toast.makeText(SetAssortmentBarcode.this, getResources().getString(R.string.enter_barcode_), Toast.LENGTH_SHORT).show();
                }
                else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) {
                    if(!etBarcodeInput.getText().toString().equals("")){
                        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();

                        saveBarcode(etBarcodeInput.getText().toString());
                    }
                    else
                        Toast.makeText(SetAssortmentBarcode.this, getResources().getString(R.string.enter_barcode_), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        btn_cancel.setOnClickListener(v-> {
            finish();
        });

        btn_save.setOnClickListener(v ->  {
            if(!etBarcodeInput.getText().toString().equals("")){
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();

                saveBarcode(etBarcodeInput.getText().toString());
            }
            else
                Toast.makeText(this, getResources().getString(R.string.enter_barcode_), Toast.LENGTH_SHORT).show();

        });
    }

    private void saveBarcode ( String barcode){
        Thread saveBar = new Thread(() -> {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES)
                    .readTimeout(4, TimeUnit.MINUTES)
                    .writeTimeout(2, TimeUnit.MINUTES)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            TerminalAPI saveBarcodeService = TerminalRetrofitClient.getApiTerminalService(url);
            Call<ResponseSimple> call = saveBarcodeService.saveBarcodeForAssortment(userId,barcode,assortmentId);

            call.enqueue(new Callback<ResponseSimple>() {
                @Override
                public void onResponse(Call<ResponseSimple> call, Response<ResponseSimple> response) {

                    ResponseSimple responseBarcode = response.body();
                    if (responseBarcode != null) {
                        int errorCode = responseBarcode.getErrorCode();

                        if(errorCode == 0)
                            handlers.obtainMessage(10).sendToTarget();
                        else {
                            String msg = ((BaseApp)getApplication()).getErrorMessage(errorCode);
                            handlers.obtainMessage(101, msg).sendToTarget();
                        }
                    }
                    else{
                        handlers.obtainMessage(101, getResources().getString(R.string.responseNothing)).sendToTarget();
                    }

                }

                @Override
                public void onFailure(Call<ResponseSimple> call, Throwable t) {
                    pgH.dismiss();
                    handlers.obtainMessage(101, t.getMessage()).sendToTarget();
                }
            });

        });
        saveBar.start();


    }

   private Handler handlers  = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if(msg.what == 10) {
                if (SetAssortmentBarcode.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                else{
                    pgH.dismiss();
                    Intent setBarcode = new Intent();
                    setBarcode.putExtra("Barcode", etBarcodeInput.getText().toString());
                    setResult(RESULT_OK,setBarcode);
                    finish();
                }
            }
            else if (msg.what == 101){
                String error = msg.obj.toString();
                pgH.dismiss();
                Toast.makeText(SetAssortmentBarcode.this, error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Activity activity = SetAssortmentBarcode.this;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                etBarcodeInput.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        etBarcodeInput.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        etBarcodeInput.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        etBarcodeInput.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        etBarcodeInput.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        etBarcodeInput.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        etBarcodeInput.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        etBarcodeInput.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        etBarcodeInput.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        etBarcodeInput.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        etBarcodeInput.append("0");
                    }break;
                    case KeyEvent.KEYCODE_STAR : {
                        etBarcodeInput.append(".");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = etBarcodeInput.getText().toString();
                        if(!etBarcodeInput.getText().toString().equals("")) {
                            etBarcodeInput.setText(test.substring(0, test.length() - 1));
                            etBarcodeInput.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                Activity activity = SetAssortmentBarcode.this;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        return super.dispatchTouchEvent(event);
    }
}
