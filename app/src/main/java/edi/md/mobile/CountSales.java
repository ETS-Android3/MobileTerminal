package edi.md.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import org.json.JSONException;
import org.json.JSONObject;

public class CountSales extends AppCompatActivity {

    TextView txt_barcode,txt_code,txt_articul,txt_stoc,txt_price,txt_name;
    TextView et_count;
    Button btn_add, btn_cancel;
    Boolean adauga_Count=false;
    String mName,mPrice,mID;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Activity activity=CountSales.this;
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
        setContentView(R.layout.activity_count_sales);

        Toolbar toolbar = findViewById(R.id.toolbar_count_sales);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_barcode = findViewById(R.id.txtBarcode_assortment_count_sales);
        txt_code = findViewById(R.id.txtcode_assortment_count_sales);
        txt_articul = findViewById(R.id.txtMarking_asortment_count_sales);
        txt_name = findViewById(R.id.txt_name_assortment_count_sales);
        txt_stoc = findViewById(R.id.txtStoc_asortment_count_sales);
        txt_price = findViewById(R.id.txtPrice_asortment_count_sales);
        et_count = findViewById(R.id.et_count_sales);
        btn_add = findViewById(R.id.btn_add_count_sales);
        btn_cancel = findViewById(R.id.btn_cancel_count_sales);

        final Intent sales = getIntent();
        txt_name.setText(sales.getStringExtra("Name"));
        txt_barcode.setText(sales.getStringExtra("BarCode"));

        String mMarking = sales.getStringExtra("Marking");
        String mCode = sales.getStringExtra("Code");

        if(mMarking.equals("null") || mMarking == null){
            txt_articul.setText("-");
        }
        else{
            txt_articul.setText(mMarking);
        }
        if(mCode.equals("null") || mCode == null){
            txt_code.setText("-");
        }
        else{
            txt_code.setText(mCode);
        }

        txt_stoc.setText(sales.getStringExtra("Remain"));
        txt_price.setText(sales.getStringExtra("Price"));

        mName = sales.getStringExtra("Name");
        mID = sales.getStringExtra("ID");
        mPrice = sales.getStringExtra("Price");

        et_count.requestFocus();

        SharedPreferences Sestting = getSharedPreferences("Settings", MODE_PRIVATE);

        boolean ShowCode = Sestting.getBoolean("ShowCode", false);
        boolean showKB = Sestting.getBoolean("ShowKeyBoard",false);
        if (!ShowCode) {
            txt_code.setVisibility(View.INVISIBLE);
        }
        if (showKB){
            et_count.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        final boolean checkStock = Sestting.getBoolean("CheckStockInput", false);
        final boolean stock = sales.getBooleanExtra("Stock",false);

        et_count.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_DONE) saveCountSales();
                else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) saveCountSales();
                return false;
            }
        });

        et_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (stock) {
                    if (checkStock) {
                        Double input = 0.0;
                        if (et_count.getText().toString().equals("")) {
                            input = 0.0;
                        } else {
                            input = Double.valueOf(et_count.getText().toString());
                        }
                        Double Stock = Double.valueOf(sales.getStringExtra("Remain"));
                        if (input > Stock) {
                            et_count.setError(getResources().getString(R.string.msg_count_greath_like_remain));
                            adauga_Count = false;
                        } else if (input == 0.0) {
                            et_count.setError(getResources().getString(R.string.msg_input_count_greath_nul));
                            adauga_Count = false;
                        } else {
                            adauga_Count = true;
                        }
                    } else {
                        adauga_Count = true;
                    }
                } else {
                    adauga_Count = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCountSales();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                et_count.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        et_count.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        et_count.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        et_count.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        et_count.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        et_count.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        et_count.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        et_count.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        et_count.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        et_count.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        et_count.append("0");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = et_count.getText().toString();
                        if(!et_count.getText().toString().equals("")) {
                            et_count.setText(test.substring(0, test.length() - 1));
                            et_count.requestFocus();
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
                Activity activity=CountSales.this;
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
    private void saveCountSales(){
        if (!et_count.getText().toString().equals("")) {
            JSONObject asl = new JSONObject();
            try {
                asl.put("AssortimentName", mName);
                asl.put("AssortimentUid",mID);
                asl.put("Count", et_count.getText().toString());
                asl.put("Price", mPrice);
            } catch (JSONException e) {
                ((Variables)getApplication()).appendLog(e.getMessage(),CountSales.this);
                e.printStackTrace();
            }
            Intent sales = new Intent();
            sales.putExtra("AssortmentSalesAdded", asl.toString());
            setResult(RESULT_OK, sales);
            finish();
        }else{
            Toast.makeText(CountSales.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
            et_count.requestFocus();
        }
    }
}
