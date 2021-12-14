package md.intelectsoft.stockmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;

import static md.intelectsoft.stockmanager.ListAssortment.AssortimentClickentSendIntent;

public class CountSales extends AppCompatActivity {

    TextView txt_barcode,txt_code,txt_articul,txt_stoc,txt_price,txt_name;
    TextView et_count,txt_unit;
    Button btn_add, btn_cancel;
    Boolean mAllowNotIntegerSales;
    String mNameAssortment,mPriceAssortment,mIDAssortment,mRemainAssortment,mMarkingAssortment,mCodeAssortment,mBarcodeAssortment,WareHouse,WareName;
    Integer WeightPrefix;

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
        txt_unit = findViewById(R.id.txt_unit);

        SPFHelp sharedPrefsInstance = SPFHelp.getInstance();
//        SharedPreferences Sestting = getSharedPreferences("Settings", MODE_PRIVATE);

        final SharedPreferences getRevisions = getSharedPreferences("Revision", MODE_PRIVATE);
        WeightPrefix = getRevisions.getInt("WeightPrefix",0);

        boolean ShowCode = sharedPrefsInstance.getBoolean("ShowCode", false);
        boolean showKB = sharedPrefsInstance.getBoolean("ShowKeyBoard",false);
        final boolean mCheckStock = sharedPrefsInstance.getBoolean("CheckStockInput", false);

        Intent sales = getIntent();
        WareHouse = sales.getStringExtra("WhareHouse");
        WareName = sales.getStringExtra("WhareNames");

        AssortmentParcelable assortment = sales.getParcelableExtra(AssortimentClickentSendIntent);
        mBarcodeAssortment = assortment.getBarCode();
        mCodeAssortment = assortment.getCode();
        mNameAssortment = assortment.getName();
        mPriceAssortment = assortment.getPrice();
        mMarkingAssortment = assortment.getMarking();
        mRemainAssortment = assortment.getRemain();
        mIDAssortment = assortment.getAssortimentID();
        mAllowNotIntegerSales =Boolean.parseBoolean(assortment.getAllowNonIntegerSale());


        txt_name.setText(mNameAssortment);
        txt_barcode.setText(mBarcodeAssortment);
        txt_stoc.setText(mRemainAssortment);
        txt_price.setText(mPriceAssortment);
        txt_unit.setText("/" + assortment.getUnit());

        String weightCode = mBarcodeAssortment.substring(7, 12);
        String weightKg = weightCode.substring(0,2);
        String weightGrams =weightCode.substring(2,5);
        String quantity = weightKg + "." + weightGrams;

        Double quantityDouble = Double.parseDouble(quantity);

        et_count.setText(quantityDouble.toString());
        et_count.requestFocus();


        if(mMarkingAssortment == null || mMarkingAssortment.equals("null")){
            txt_articul.setText("-");
        }
        else{
            txt_articul.setText(mMarkingAssortment);
        }
        if(mCodeAssortment == null || mCodeAssortment.equals("null")){
            txt_code.setText("-");
        }
        else{
            txt_code.setText(mCodeAssortment);
        }

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
                if (keyCode == EditorInfo.IME_ACTION_DONE) saveCountSales(mCheckStock);
                else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) saveCountSales(mCheckStock);
                return false;
            }
        });

        et_count.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (mCheckStock) {
                        Double input = 0.0;
                        if (et_count.getText().toString().equals("")) {
                            input = 0.0;
                        } else {
                            try{
                                input = Double.valueOf(et_count.getText().toString().replace(",","."));
                            }catch (Exception e){
                                input = Double.valueOf(et_count.getText().toString().replace(".",","));
                            }
                        }
                        Double Stock = 0.00;
                        try{
                            Stock = Double.valueOf(mRemainAssortment);
                        }catch (Exception e){
                            Stock = Double.valueOf(mRemainAssortment.replace(",","."));
                        }

                        if (input > Stock) {
                            et_count.setError(getResources().getString(R.string.msg_count_greath_like_remain));
                        } else if (input == 0.0) {
                            et_count.setError(getResources().getString(R.string.msg_input_count_greath_nul));
                        }
                    }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCountSales(mCheckStock);
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
    private void saveCountSales(boolean checkStock){
        if (checkStock) {
            Double input = 0.0;
            if (et_count.getText().toString().equals("")) {
                input = 0.0;
            } else {
                try{
                    input = Double.valueOf(et_count.getText().toString().replace(",","."));
                }catch (Exception e){
                    input = Double.valueOf(et_count.getText().toString().replace(".",","));
                }
            }
            Double Stock = 0.00;
            try{
                Stock = Double.valueOf(mRemainAssortment);
            }catch (Exception e){
                Stock = Double.valueOf(mRemainAssortment.replace(",","."));
            }

            if (input > Stock) {
                et_count.setError(getResources().getString(R.string.msg_count_greath_like_remain));
            } else if (input == 0.0) {
                et_count.setError(getResources().getString(R.string.msg_input_count_greath_nul));
            } else {
                if(!mAllowNotIntegerSales){
                    if (isInteger(et_count.getText().toString())) {
                        JSONObject asl = new JSONObject();
                        try {
                            double count = 0;
                            try{
                                count = Double.valueOf(et_count.getText().toString());
                            }catch (Exception e){
                                count = Double.valueOf(et_count.getText().toString().replace(",","."));
                            }
                            asl.put("AssortimentName", mNameAssortment);
                            asl.put("AssortimentUid",mIDAssortment);
                            asl.put("Count", count);
                            asl.put("Price", mPriceAssortment);
                            asl.put("Warehouse",WareHouse);
                            asl.put("WareName",WareName);
                        } catch (JSONException e) {
                            ((BaseApp)getApplication()).appendLog(e.getMessage(),CountSales.this);
                            e.printStackTrace();
                        }
                        Intent sales = new Intent();
                        sales.putExtra("AssortmentSalesAdded", asl.toString());
                        setResult(RESULT_OK, sales);
                        finish();
                    }else{
                        et_count.setError(getResources().getString(R.string.sales_count_only_integer));
                        et_count.requestFocus();
                    }
                }else{
                    JSONObject asl = new JSONObject();
                    try {
                        double count = 0;
                        try{
                            count = Double.valueOf(et_count.getText().toString());
                        }catch (Exception e){
                            count = Double.valueOf(et_count.getText().toString().replace(",","."));
                        }

                        asl.put("AssortimentName", mNameAssortment);
                        asl.put("AssortimentUid",mIDAssortment);
                        asl.put("Count", count);
                        asl.put("Price", mPriceAssortment);
                        asl.put("Warehouse",WareHouse);
                        asl.put("WareName",WareName);
                    } catch (JSONException e) {
                        ((BaseApp)getApplication()).appendLog(e.getMessage(),CountSales.this);
                        e.printStackTrace();
                    }
                    Intent sales = new Intent();
                    sales.putExtra("AssortmentSalesAdded", asl.toString());
                    setResult(RESULT_OK, sales);
                    finish();
                }
            }
        }
        else{
            String prefixString = txt_barcode.getText().toString().substring(0, 2);
            if (!mAllowNotIntegerSales || prefixString == WeightPrefix.toString()) {
                String prefix = WeightPrefix.toString();

                if (prefixString.equals(prefix)){
                    JSONObject asl = new JSONObject();
                    try {
                        double count = 0;
                        try {
                            count = Double.valueOf(et_count.getText().toString());
                        } catch (Exception e) {
                            count = Double.valueOf(et_count.getText().toString().replace(",", "."));
                        }

                        asl.put("AssortimentName", mNameAssortment);
                        asl.put("AssortimentUid", mIDAssortment);
                        asl.put("Count", count);
                        asl.put("Price", mPriceAssortment);
                        asl.put("Warehouse", WareHouse);
                        asl.put("WareName", WareName);
                    } catch (JSONException e) {
                        ((BaseApp) getApplication()).appendLog(e.getMessage(), CountSales.this);
                        e.printStackTrace();
                    }
                    Intent sales = new Intent();
                    sales.putExtra("AssortmentSalesAdded", asl.toString());
                    setResult(RESULT_OK, sales);
                    finish();
                }
                else if (isInteger(et_count.getText().toString())) {
                    JSONObject asl = new JSONObject();
                    try {
                        double count = 0;
                        try {
                            count = Double.valueOf(et_count.getText().toString());
                        } catch (Exception e) {
                            count = Double.valueOf(et_count.getText().toString().replace(",", "."));
                        }

                        asl.put("AssortimentName", mNameAssortment);
                        asl.put("AssortimentUid", mIDAssortment);
                        asl.put("Count", count);
                        asl.put("Price", mPriceAssortment);
                        asl.put("Warehouse", WareHouse);
                        asl.put("WareName", WareName);
                    } catch (JSONException e) {
                        ((BaseApp) getApplication()).appendLog(e.getMessage(), CountSales.this);
                        e.printStackTrace();
                    }
                    Intent sales = new Intent();
                    sales.putExtra("AssortmentSalesAdded", asl.toString());
                    setResult(RESULT_OK, sales);
                    finish();
                } else {
                    et_count.setError(getResources().getString(R.string.sales_count_only_integer));
                    et_count.requestFocus();
                }
            } else {
                JSONObject asl = new JSONObject();
                try {
                    double count = 0;
                    try {
                        count = Double.valueOf(et_count.getText().toString());
                    } catch (Exception e) {
                        count = Double.valueOf(et_count.getText().toString().replace(",", "."));
                    }
                    asl.put("AssortimentName", mNameAssortment);
                    asl.put("AssortimentUid", mIDAssortment);
                    asl.put("Count", count);
                    asl.put("Price", mPriceAssortment);
                    asl.put("Warehouse", WareHouse);
                    asl.put("WareName", WareName);
                } catch (JSONException e) {
                    ((BaseApp) getApplication()).appendLog(e.getMessage(), CountSales.this);
                    e.printStackTrace();
                }
                Intent sales = new Intent();
                sales.putExtra("AssortmentSalesAdded", asl.toString());
                setResult(RESULT_OK, sales);
                finish();
            }
        }
    }
    private boolean isInteger(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            ((BaseApp)getApplication()).appendLog(e.getMessage(),CountSales.this);
            return false;
        }
    }
}
