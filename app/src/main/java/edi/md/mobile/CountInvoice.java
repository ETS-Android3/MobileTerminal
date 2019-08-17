package edi.md.mobile;

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

import edi.md.mobile.Utils.AssortmentInActivity;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;

public class CountInvoice extends AppCompatActivity {

    Button btn_accept,btn_cancel;
    TextView txtTotalinc,txtTotalsale,txtNames,txtProfit;
    EditText etCant,etPriceInc,etPriceSales;
    String mNameAssortment,mPriceAssortment,mIncomePrice,mIDAssortment;
    boolean mAllowNotIntegerSales;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_invoice);

        Toolbar toolbar = findViewById(R.id.toolbar_count_invoice);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_accept=findViewById(R.id.btn_add_count_invoice);
        btn_cancel=findViewById(R.id.btn_cancel_add_count_invoice);
        txtNames=findViewById(R.id.txtName_assortment_count_invoice);
        txtProfit=findViewById(R.id.txt_profit_coutn_invoice);
        txtTotalinc=findViewById(R.id.txt_suma_inc_invoice);
        txtTotalsale=findViewById(R.id.txtSumn_sales_count_invoice);
        etCant=findViewById(R.id.et_count_invoice);
        etPriceInc=findViewById(R.id.et_price_inc_invoice);
        etPriceSales=findViewById(R.id.et_price_sales_invoice);

        SharedPreferences Sestting = getSharedPreferences("Settings", MODE_PRIVATE);

        final Intent Invoice = getIntent();
        AssortmentInActivity assortment = Invoice.getParcelableExtra(AssortimentClickentSendIntent);
        mNameAssortment = assortment.getName();
        mPriceAssortment = assortment.getPrice();
        mIncomePrice = assortment.getIncomePrice();
        mIDAssortment = assortment.getAssortimentID();
        mAllowNotIntegerSales =Boolean.parseBoolean(assortment.getAllowNonIntegerSale());

        txtNames.setText(mNameAssortment);
        etPriceInc.setText(mIncomePrice);
        etPriceSales.setText(mPriceAssortment);



        etCant.requestFocus();
        boolean showKB = Sestting.getBoolean("ShowKeyBoard",false);
        if (showKB){
            etCant.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        etPriceSales.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) saveCount(mNameAssortment,mIDAssortment);
                else if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) saveCount(mNameAssortment,mIDAssortment);
                return false;
            }
        });

        etCant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double cant,PrInc;

                if (mAllowNotIntegerSales) {
                    if (!isDouble(String.valueOf(s))) etCant.setError(getResources().getString(R.string.msg_format_number_incorect));
                    else{
                        if (etCant.getText().toString().equals("")){
                            cant = Double.valueOf("0.0");
                        }else{
                            cant = Double.valueOf(etCant.getText().toString());
                        }
                        if (etPriceInc.getText().toString().equals("")){
                            PrInc = Double.valueOf("0.0");
                        }else{
                            PrInc = Double.valueOf(etPriceInc.getText().toString());
                        }
                        Double PrSales=Double.valueOf(etPriceSales.getText().toString());

                        Double totalInc = cant * PrInc;
                        Double totalSales = cant * PrSales;
                        Double profit =((totalSales/totalInc)-1 )/ 0.01;

                        String total_incom = String.format("%.2f",totalInc);
                        total_incom= total_incom.replace(",",".");
                        String total_sales = String.format("%.2f",totalSales);
                        total_sales= total_sales.replace(",",".");
                        txtTotalinc.setText(total_incom);
                        txtTotalsale.setText(total_sales);
                        txtProfit.setText(String.format("%.2f",profit));
                    }
                }
                else {
                    if (!isInteger(etCant.getText().toString()))
                        etCant.setError(getResources().getString(R.string.msg_only_number_integer));
                    else{
                        if (etCant.getText().toString().equals("")){
                            cant = Double.valueOf("0.0");
                        }else{
                            cant = Double.valueOf(etCant.getText().toString());
                        }
                        if (etPriceInc.getText().toString().equals("")){
                            PrInc = Double.valueOf("0.0");
                        }else{
                            PrInc = Double.valueOf(etPriceInc.getText().toString());
                        }
                        Double PrSales=Double.valueOf(etPriceSales.getText().toString());

                        Double totalInc = cant * PrInc;
                        Double totalSales = cant * PrSales;
                        Double profit =((totalSales/totalInc)-1 )/ 0.01;

                        String total_incom = String.format("%.2f",totalInc);
                        total_incom= total_incom.replace(",",".");
                        String total_sales = String.format("%.2f",totalSales);
                        total_sales= total_sales.replace(",",".");
                        txtTotalinc.setText(total_incom);
                        txtTotalsale.setText(total_sales);
                        txtProfit.setText(String.format("%.2f",profit));
                    }
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPriceInc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double cant,PrInc,PrSales;
                if (etCant.getText().toString().equals("")){
                    cant = Double.valueOf("0.0");
                }else{
                    cant = Double.valueOf(etCant.getText().toString());
                }
                if (etPriceInc.getText().toString().equals("")){
                    PrInc = Double.valueOf("0.0");
                }else{
                    PrInc = Double.valueOf(etPriceInc.getText().toString());
                }
                if (etPriceSales.getText().toString().equals("")){
                    PrSales = Double.valueOf("0.0");
                }else{
                    PrSales = Double.valueOf(etPriceSales.getText().toString());
                }
                Double totalSales = cant * PrSales;
                Double totalInc = cant * PrInc;
                Double profit =((totalSales/totalInc)-1 )/ 0.01;

                String total_incom = String.format("%.2f",totalInc);
                total_incom= total_incom.replace(",",".");
                txtProfit.setText(String.format("%.2f",profit));
                txtTotalinc.setText(total_incom);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPriceSales.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double cant,PrSales,PrInc;
                if (etCant.getText().toString().equals("")){
                    cant = Double.valueOf("1.0");
                }else{
                    cant = Double.valueOf(etCant.getText().toString());
                }
                if (etPriceSales.getText().toString().equals("")){
                    PrSales = Double.valueOf("0.0");
                }else{
                    PrSales = Double.valueOf(etPriceSales.getText().toString());
                }
                if (etPriceInc.getText().toString().equals("")){
                    PrInc = Double.valueOf("0.0");
                }else{
                    PrInc = Double.valueOf(etPriceInc.getText().toString());
                }
                Double totalSales = cant * PrSales;
                Double totalInc = cant * PrInc;

                Double profit =((totalSales/totalInc)-1 )/ 0.01;

                txtProfit.setText(String.format("%.2f",profit));

                String total_sales = String.format("%.2f",totalSales);
                total_sales= total_sales.replace(",",".");
                txtTotalsale.setText(total_sales);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCount(mNameAssortment,mIDAssortment);
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
    private void saveCount(String name,String id){
        if(mAllowNotIntegerSales){
            if (!isDouble(etCant.getText().toString()))
                etCant.setError(getResources().getString(R.string.msg_format_number_incorect));
            else {
                if(etCant.getText().toString().equals("")) {
                    Toast.makeText(CountInvoice.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                }else  if (etPriceInc.getText().toString().equals("")){
                    Toast.makeText(CountInvoice.this, getResources().getString(R.string.msg_input_incoming_price_invoice), Toast.LENGTH_SHORT).show();
                }else{
                    JSONObject asl = new JSONObject();
                    try {
                        asl.put("AssortimentName", name);
                        asl.put("AssortimentUid", id);
                        asl.put("SalePrice", etPriceSales.getText().toString());
                        asl.put("IncomeSum", etPriceInc.getText().toString());
                        asl.put("Suma", txtTotalinc.getText().toString());
                        asl.put("Count", etCant.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),CountInvoice.this);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("AssortmentInvoiceAdded", asl.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }else {
            if (!isInteger(etCant.getText().toString()))
                etCant.setError(getResources().getString(R.string.msg_only_number_integer));
            else {
                if (etCant.getText().toString().equals("")) {
                    Toast.makeText(CountInvoice.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                } else if (etPriceInc.getText().toString().equals("")) {
                    Toast.makeText(CountInvoice.this, getResources().getString(R.string.msg_input_incoming_price_invoice), Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject asl = new JSONObject();
                    try {
                        asl.put("AssortimentName", name);
                        asl.put("AssortimentUid", id);
                        asl.put("SalePrice", etPriceSales.getText().toString());
                        asl.put("IncomeSum", etPriceInc.getText().toString());
                        asl.put("Suma", txtTotalinc.getText().toString());
                        asl.put("Count", etCant.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables) getApplication()).appendLog(e.getMessage(), CountInvoice.this);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("AssortmentInvoiceAdded", asl.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                etCant.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        etCant.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        etCant.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        etCant.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        etCant.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        etCant.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        etCant.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        etCant.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        etCant.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        etCant.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        etCant.append("0");
                    }break;
                    case KeyEvent.KEYCODE_STAR : {
                        etCant.append(".");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = etCant.getText().toString();
                        if(!etCant.getText().toString().equals("")) {
                            etCant.setText(test.substring(0, test.length() - 1));
                            etCant.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isDouble(String s) throws NumberFormatException {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {

            ((Variables)getApplication()).appendLog(e.getMessage(),CountInvoice.this);
            return false;
        }
    }
    private boolean isInteger(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            ((Variables)getApplication()).appendLog(e.getMessage(),CountInvoice.this);
            return false;
        }
    }
}
