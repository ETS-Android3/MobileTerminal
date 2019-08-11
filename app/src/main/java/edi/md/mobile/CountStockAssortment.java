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

public class CountStockAssortment extends AppCompatActivity {
    TextView txt_barcode,txt_code,txt_articul,txt_stoc,txt_price,txt_name;
    TextView et_count;
    Button btn_add, btn_cancel;
    Boolean adauga_Count=false;
    String Unit,UnitPrice,UnitInPackage, Uid;

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Activity activity = CountStockAssortment.this;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = activity.getCurrentFocus();
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_stock_assortment);

        Toolbar toolbar = findViewById(R.id.toolbar_count_stock_Asl);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_barcode = findViewById(R.id.txtBarcode_assortment_count_stock_assortment);
        txt_code = findViewById(R.id.txtcode_assortment_count_stock_assortment);
        txt_articul = findViewById(R.id.txtMarking_asortment_count_stock_assortment);
        txt_name = findViewById(R.id.txt_name_assortment_count_stock_assortment);
        txt_stoc = findViewById(R.id.txtStoc_asortment_count_stock_assortment);
        txt_price = findViewById(R.id.txtPrice_asortment_count_stock_assortment);
        et_count = findViewById(R.id.et_count_stock_assortment);
        btn_add = findViewById(R.id.btn_add_count_stock_assortment);
        btn_cancel = findViewById(R.id.btn_cancel_count_stock_assortment);

        final Intent sales = getIntent();
        txt_name.setText(sales.getStringExtra("Name"));
        txt_barcode.setText(sales.getStringExtra("BarCode"));
        txt_code.setText(sales.getStringExtra("Code"));
        txt_articul.setText(sales.getStringExtra("Marking"));
        txt_stoc.setText(sales.getStringExtra("Remain"));
        txt_price.setText(sales.getStringExtra("Price"));

        Unit = sales.getStringExtra("Unit");
        UnitInPackage  = sales.getStringExtra("UnitInPackage");
        UnitPrice  = sales.getStringExtra("UnitPrice");
        Uid = sales.getStringExtra("ID");

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

        et_count.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {
                if (keyCode == EditorInfo.IME_ACTION_DONE) saveCount(sales.getStringExtra("Name"),sales.getStringExtra("Code"),sales.getStringExtra("Price"),sales.getStringExtra("BarCode"));
                else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) saveCount(sales.getStringExtra("Name"),sales.getStringExtra("Code"),sales.getStringExtra("Price"),sales.getStringExtra("BarCode"));
                return false;
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCount(sales.getStringExtra("Name"),sales.getStringExtra("Code"),sales.getStringExtra("Price"),sales.getStringExtra("BarCode"));
            }
        });

        et_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double input ;
                if(et_count.getText().toString().equals("")){
                    input =0.0;
                }else{
                    input = Double.valueOf(et_count.getText().toString());
                }
                if (input==0.0){
                    et_count.setError(getResources().getString(R.string.msg_input_count_greath_nul));
                    adauga_Count=false;
                }else{
                    adauga_Count=true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    @Override
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
    private void saveCount(String name,String code,String price,String barcode){
        if(adauga_Count && !et_count.getText().toString().equals("")) {
            JSONObject asl = new JSONObject();
            try {
                asl.put("AssortimentName", name);
                asl.put("AssortimentUid", Uid);
                asl.put("Barcode",barcode);
                asl.put("Price",price);
                asl.put("Unit",Unit);
                asl.put("Code",code);
                asl.put("UnitInPackage",UnitInPackage);
                asl.put("UnitPrice",UnitPrice);
                asl.put("Count", et_count.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),CountStockAssortment.this);
            }
            Intent stock_asl = new Intent();
            stock_asl.putExtra("AssortmentStockAdded", asl.toString());
            setResult(RESULT_OK, stock_asl);
            finish();
        }else {
            Toast.makeText(CountStockAssortment.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
            et_count.requestFocus();
        }
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
                    case KeyEvent.KEYCODE_STAR : {
                        et_count.append(".");
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
}
