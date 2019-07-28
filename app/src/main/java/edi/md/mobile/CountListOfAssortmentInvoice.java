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

public class CountListOfAssortmentInvoice extends AppCompatActivity {
    Button btn_accept,btn_cancel;
    TextView txtTotalinc,txtTotalsale,txtNames,txtProfit;
    EditText etCant,etPriceInc,etPriceSales;
    String Name,Uid , ip_,port_,UserId,name_asl,PriceSales,PriceIncoming;
    String IntegerSales;

    private boolean isDigits(String s) throws NumberFormatException {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortmentInvoice.this);
            return false;
        }
    }

    private boolean isDigitInteger(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            ((Variables)getApplication()).appendLog(e.getMessage(),CountListOfAssortmentInvoice.this);
            return false;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Intent countInc = new Intent();
                setResult(RESULT_CANCELED,countInc);
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
        setContentView(R.layout.activity_count_list_of_assortment_invoice);

        Toolbar toolbar = findViewById(R.id.toolbar_count_invoice_touch);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_accept=findViewById(R.id.btn_add_count_invoice_touch);
        btn_cancel=findViewById(R.id.btn_cancel_add_count_invoice_touch);
        txtNames=findViewById(R.id.txtName_assortment_count_invoice_touch);
        txtProfit=findViewById(R.id.txt_profit_coutn_invoice_touch);
        txtTotalinc=findViewById(R.id.txt_suma_inc_invoice_touch);
        txtTotalsale=findViewById(R.id.txtSumn_sales_count_invoice_touch);
        etCant=findViewById(R.id.et_count_invoice2);
        etPriceInc=findViewById(R.id.et_price_inc_invoice_touch);
        etPriceSales=findViewById(R.id.et_price_sales_invoice_touch);

        SharedPreferences sPref =getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences sPrefTouch = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);
        etCant.requestFocus();
        boolean showKB = sPref.getBoolean("ShowKeyBoard",false);
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            etCant.requestFocus();
        }
        Name = sPrefTouch.getString("Name_Assortiment", "");
        PriceSales = sPrefTouch.getString("Price_Assortiment", "");
        IntegerSales = sPrefTouch.getString("AllowNonIntegerSale", "false");
        Uid = sPrefTouch.getString("AssortimentID","");
        PriceIncoming=sPrefTouch.getString("IncomePrice","0");

        if(!PriceIncoming.contains("0")){
            etPriceInc.setText(PriceIncoming.replace(",","."));
        }else{
            etPriceInc.setText("0");
        }
        etPriceSales.setText(PriceSales.replace(",","."));
        txtNames.setText(Name);


        etPriceSales.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(etCant.getText().toString().equals("")) {
                        Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                    }else  if (etPriceInc.getText().toString().equals("")){
                        Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.msg_input_incoming_price_invoice), Toast.LENGTH_SHORT).show();
                    }else{

                    }
                }else if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    if(etCant.getText().toString().equals("")) {
                        Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                    }else  if (etPriceInc.getText().toString().equals("")){
                        Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.msg_input_incoming_price_invoice), Toast.LENGTH_SHORT).show();
                    }else{

                    }
                }
                return false;
            }
        });
        etCant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Double cant,PrInc;
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

                txtTotalinc.setText(String.format("%.2f",totalInc));
                txtTotalsale.setText(String.format("%.2f",totalSales));
                txtProfit.setText(String.format("%.2f",profit));
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

                txtProfit.setText(String.format("%.2f",profit).replace(",","."));
                txtTotalinc.setText(String.format("%.2f",totalInc).replace(",","."));
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

                txtProfit.setText(String.format("%.2f",profit).replace(",","."));
                txtTotalsale.setText(String.format("%.2f",totalSales).replace(",","."));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent countInc = new Intent();
                setResult(RESULT_CANCELED,countInc);
                finish();
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etCant.getText().toString().equals("")) {
                    Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                }else  if (etPriceInc.getText().toString().equals("")){
                    Toast.makeText(CountListOfAssortmentInvoice.this, getResources().getString(R.string.msg_input_incoming_price_invoice), Toast.LENGTH_SHORT).show();
                }else{
                    if (IntegerSales.equals("true")) {
                        if (isDigits(etCant.getText().toString())) {
                            Intent intent = new Intent();
                            intent.putExtra("count", etCant.getText().toString());
                            intent.putExtra("SalePrice", etPriceSales.getText().toString());
                            intent.putExtra("IncomePrice", etPriceInc.getText().toString());
                            intent.putExtra("Suma", txtTotalinc.getText().toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else {
                        if (isDigitInteger(etCant.getText().toString())) {
                            Intent intent = new Intent();
                            intent.putExtra("count", etCant.getText().toString());
                            intent.putExtra("SalePrice", etPriceSales.getText().toString());
                            intent.putExtra("IncomePrice", etPriceInc.getText().toString());
                            intent.putExtra("Suma", txtTotalinc.getText().toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }
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
}
