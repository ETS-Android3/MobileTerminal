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

public class CountTransfer extends AppCompatActivity {
    TextView txt_barcode,txt_code,txt_articul,txt_stoc,txt_price,txt_name;
    TextView et_count;
    Button btn_add, btn_cancel;
    Boolean adauga_Count=false;


    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Activity activity=CountTransfer.this;
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
        setContentView(R.layout.activity_count_transfer);

        Toolbar toolbar = findViewById(R.id.toolbar_count_transfer);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_barcode = findViewById(R.id.txtBarcode_assortment_count_transfer);
        txt_code = findViewById(R.id.txtcode_assortment_count_transfer);
        txt_articul = findViewById(R.id.txtMarking_asortment_count_transfer);
        txt_name = findViewById(R.id.txt_name_assortment_count_transfer);
        txt_stoc = findViewById(R.id.txtStoc_asortment_count_transfer);
        txt_price = findViewById(R.id.txtPrice_asortment_count_transfer);
        et_count = findViewById(R.id.et_count_transfer);
        btn_add = findViewById(R.id.btn_add_count_transfer);
        btn_cancel = findViewById(R.id.btn_cancel_count_transfer);

        final Intent sales = getIntent();
        txt_name.setText(sales.getStringExtra("Names"));
        txt_barcode.setText(sales.getStringExtra("BarCode"));
        txt_code.setText(sales.getStringExtra("Codes"));
        txt_articul.setText(sales.getStringExtra("Marking"));
        txt_stoc.setText(sales.getStringExtra("Remain"));
        txt_price.setText(sales.getStringExtra("Price"));

        et_count.requestFocus();
        SharedPreferences Sestting = getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences AddedAssortment = getSharedPreferences("Transfer", MODE_PRIVATE);
        final SharedPreferences.Editor inpASL = AddedAssortment.edit();

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

        et_count.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int keyCode,
                                          KeyEvent event) {
                boolean od =false;
                if (keyCode == EditorInfo.IME_ACTION_DONE) {
                    if (!et_count.getText().toString().equals("")) {
                        JSONObject asl = new JSONObject();
                        try {
                            asl.put("AssortimentName", sales.getStringExtra("Names"));
                            asl.put("AssortimentUid", sales.getStringExtra("ID"));
                            asl.put("Count", et_count.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),CountTransfer.this);
                        }
                        Intent added = new Intent();
                        inpASL.putString("AssortmentTransferAdded", asl.toString());
                        inpASL.apply();
                        setResult(RESULT_OK, added);
                        finish();
                    }else{
                        Toast.makeText(CountTransfer.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                        et_count.requestFocus();
                    }
                }else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) {
                    if (!et_count.getText().toString().equals("")) {
                        JSONObject asl = new JSONObject();
                        try {
                            asl.put("AssortimentName", sales.getStringExtra("Names"));
                            asl.put("AssortimentUid", sales.getStringExtra("ID"));
                            asl.put("Count", et_count.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),CountTransfer.this);
                        }
                        Intent added = new Intent();
                        inpASL.putString("AssortmentTransferAdded", asl.toString());
                        inpASL.apply();
                        setResult(RESULT_OK, added);
                        finish();
                    }else{
                        Toast.makeText(CountTransfer.this,getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                        et_count.requestFocus();
                    }
                }
                return false;
            }
        });

        et_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (checkStock){
                    Double input = 0.0;
                    if(et_count.getText().toString().equals("")){
                        input =0.0;
                    }else{
                        input = Double.valueOf(et_count.getText().toString());
                    }
                    Double Stock = Double.valueOf(sales.getStringExtra("Remain"));
                    if(input>Stock){
                        et_count.setError(getResources().getString(R.string.msg_count_greath_like_remain));
                        adauga_Count=false;
                    }else if (input==0.0){
                        et_count.setError(getResources().getString(R.string.msg_input_count_greath_nul));
                        adauga_Count=false;
                    }else{
                        adauga_Count=true;
                    }
                }else{
                    adauga_Count=true;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adauga_Count && !et_count.getText().toString().equals("")) {
                    JSONObject asl = new JSONObject();
                    try {
                        asl.put("AssortimentName", sales.getStringExtra("Names"));
                        asl.put("AssortimentUid", sales.getStringExtra("ID"));
                        asl.put("Count", et_count.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),CountTransfer.this);
                    }
                    inpASL.putString("AssortmentTransferAdded", asl.toString());
                    inpASL.apply();
                    Intent countInc = new Intent();
                    setResult(RESULT_OK, countInc);
                    finish();
                }else{
                    Toast.makeText(CountTransfer.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                    et_count.requestFocus();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inpASL.putString("AssortmentTransferAdded", "{}");
                inpASL.apply();
                Intent finIntent = new Intent();
                setResult(RESULT_CANCELED, finIntent);
                finish();
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
}
