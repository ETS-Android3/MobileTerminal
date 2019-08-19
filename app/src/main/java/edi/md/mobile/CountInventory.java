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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import edi.md.mobile.Utils.AssortmentInActivity;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;
import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveRevisionLine;

public class CountInventory extends AppCompatActivity {
    Button btn_ok,btn_cancel;
    TextView txtBarCode,txtArticol,txtStock,txtName,txtCode,txtTotalScan,txtRemainScan,txtSurplusScan,txtPrice;
    EditText inpCount;
    Switch cant_final;

    ProgressDialog pgH;
    int WeightPrefix;
    boolean mAllowNotIntegerSales;
    JSONObject sendAssortiment;

    String mNameAssortment,mMarkingAssortment,mIDAssortment , ip_,port_,UserId,mRemainAssortment,RevisionID,mCodeAssortment,mBarcodeAssortment,mPriceAssortment,mUnitAssortment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar_count_inventory);
        setSupportActionBar(toolbar);

        initializareElement();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent countInv= new Intent();
                setResult(RESULT_CANCELED,countInv);
                finish();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCount();
            }
        });

        inpCount.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveCount();
                }else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    saveCount();
                }
                return false;
            }
        });
    }

    public String getResponseFromURLSaveRevisionLine(URL send_bills) throws IOException {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(4000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());
            wr.writeBytes(String.valueOf(sendAssortiment));
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
            ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        inpCount.append("1");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        inpCount.append("2");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        inpCount.append("3");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        inpCount.append("4");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        inpCount.append("5");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        inpCount.append("6");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        inpCount.append("7");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        inpCount.append("8");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        inpCount.append("9");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        inpCount.append("0");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_STAR : {
                        inpCount.append(".");
                        inpCount.requestFocus();
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = inpCount.getText().toString();
                        if(!inpCount.getText().toString().equals("")) {
                            inpCount.setText(test.substring(0, test.length() - 1));
                            inpCount.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
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
    class AsyncTask_SaveRevisionLine extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="";
            try {
                response = getResponseFromURLSaveRevisionLine(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
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
                        //Sumez cantitatea introdusa noua cu cea existenta si o salvez
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SharedPreferences SaveCountName = getSharedPreferences("SaveNameInventory", MODE_PRIVATE);
                        SharedPreferences.Editor add_count = SaveCount.edit();
                        SharedPreferences.Editor add_name = SaveCountName.edit();

                        String ExistingCount = SaveCount.getString(mIDAssortment,"0");
                        ExistingCount = ExistingCount.replace(",",".");

                        Double countExist = Double.valueOf(ExistingCount);
                        Double new_count  = Double.valueOf(inpCount.getText().toString());
                        Double total_count = countExist + new_count;

                        if (cant_final.isChecked()){
                            total_count = Double.valueOf(inpCount.getText().toString());
                        }
                        add_count.putString(mIDAssortment,String.format("%.2f",total_count));
                        add_name.putString(mIDAssortment,mNameAssortment);
                        add_name.apply();
                        add_count.apply();

                        Activity activity=CountInventory.this;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        View view = activity.getCurrentFocus();
                        //If no view currently has focus, create a new one, just so we can grab a window token from it
                        if (view == null) {
                            view = new View(activity);
                        }
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        Intent countInv= new Intent();
                        countInv.putExtra("Count",inpCount.getText().toString());
                        setResult(RESULT_OK,countInv);
                        finish();
                    } else {
                        Activity activity=CountInventory.this;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        View view = activity.getCurrentFocus();
                        //If no view currently has focus, create a new one, just so we can grab a window token from it
                        if (view == null) {
                            view = new View(activity);
                        }
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        Toast.makeText(CountInventory.this,getResources().getString(R.string.msg_error_code)+ErrorCode , Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
                }
            }else{
                Activity activity=CountInventory.this;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                Toast.makeText(CountInventory.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }


        }
    }
    private void saveCount(){
        if(!inpCount.getText().toString().equals("")) {
            pgH.setMessage("loading..");
            pgH.setIndeterminate(true);
            pgH.setCancelable(false);
            pgH.show();
            sendAssortiment = new JSONObject();
            try {
                sendAssortiment.put("Assortiment", mIDAssortment);
                sendAssortiment.put("FinalQuantity", cant_final.isChecked());
                sendAssortiment.put("Quantity", inpCount.getText().toString());
                sendAssortiment.put("RevisionID", RevisionID);
            } catch (JSONException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
            }
            URL generateSaveLine = SaveRevisionLine(ip_, port_);
            new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
        }else if (inpCount.getText().toString().equals("")){
            inpCount.setError(getResources().getString(R.string.txt_header_inp_count));
        }
    }
    private void initializareElement(){
        btn_cancel=findViewById(R.id.btn_cancel_count_inventory);
        btn_ok=findViewById(R.id.btn_add_count_inventory);
        txtArticol= findViewById(R.id.txtMarking_asortment_count_inventory);
        txtBarCode=findViewById(R.id.txtbarcode_count_inventory);
        txtName=findViewById(R.id.txtName_assortment_inv_count);
        txtStock=findViewById(R.id.txtStoc_count_inventory);
        txtCode=findViewById(R.id.txtcode_assortment_count_inventory);
        inpCount=findViewById(R.id.et_count_inventory);
        cant_final = findViewById(R.id.switch_final_count_inventory);
        txtRemainScan=findViewById(R.id.txt_ramas_inventory);
        txtTotalScan = findViewById(R.id.txtTotalScanat_count_inventory);
        txtSurplusScan=findViewById(R.id.txt_surplus_inventory);
        txtPrice=findViewById(R.id.txtPrice_asortment_count_inventory);
        pgH =new ProgressDialog(CountInventory.this);

        final SharedPreferences getRevisions = getSharedPreferences("Revision", MODE_PRIVATE);
        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);

        final Intent sales = getIntent();
        AssortmentInActivity assortment = sales.getParcelableExtra(AssortimentClickentSendIntent);
        mNameAssortment = assortment.getName();
        mPriceAssortment = assortment.getPrice();
        mMarkingAssortment = assortment.getMarking();
        mCodeAssortment = assortment.getCode();
        mBarcodeAssortment = assortment.getBarCode();
        mRemainAssortment = assortment.getRemain();
        mIDAssortment = assortment.getAssortimentID();
        mUnitAssortment = assortment.getUnit();
        mAllowNotIntegerSales =Boolean.parseBoolean(assortment.getAllowNonIntegerSale());
        WeightPrefix= sales.getIntExtra("WeightPrefix",0);

        boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        boolean ShowCode = Settings.getBoolean("ShowCode",false);
        UserId = User.getString("UserID","Non");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");
        RevisionID=getRevisions.getString("RevisionID","");
        String prefic = mBarcodeAssortment.substring(0,2);
        int toInt = Integer.valueOf(prefic);

        String ExistingCount = SaveCount.getString(mIDAssortment,"0");
        ExistingCount = ExistingCount.replace(",",".");
        txtTotalScan.setText(ExistingCount + " " + mUnitAssortment);
        txtName.setText(mNameAssortment);
        txtCode.setText(mCodeAssortment);
        txtBarCode.setText(mBarcodeAssortment);
        txtStock.setText(mRemainAssortment + " " + mUnitAssortment);

        if (!ShowCode){
            txtCode.setVisibility(View.INVISIBLE);
        }
        inpCount.requestFocus();
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            inpCount.requestFocus();
        }
        double countAdded = Double.valueOf(ExistingCount);
        double Remain_in_program = 0.00;
        if(mRemainAssortment!= null){
            Remain_in_program =Double.valueOf(mRemainAssortment);
        }
        if (Remain_in_program!=0.0){
            double remain_scan=  Remain_in_program - countAdded;
            if (remain_scan<=0) {
                txtRemainScan.setText("0 " + mUnitAssortment);
                double surplus_scan = Math.abs(remain_scan);
                txtSurplusScan.setText(String.valueOf(surplus_scan) + " " + mUnitAssortment);
            }else {
                txtRemainScan.setText(String.valueOf(remain_scan) + " " + mUnitAssortment);
                txtSurplusScan.setText("0 " + mUnitAssortment);
            }
        }
        else{
            txtRemainScan.setText("0 " + mUnitAssortment);
            double surplus_scan = Math.abs(Remain_in_program - countAdded);
            txtSurplusScan.setText(String.valueOf(surplus_scan) + " " + mUnitAssortment);
        }
        if(toInt==WeightPrefix){
            String afterseven = mBarcodeAssortment.substring(7,12);
            String afetrCut = afterseven.substring(0,2) + "." + afterseven.substring(2);
            Double tet = Double.valueOf(afetrCut);
            inpCount.setText(String.valueOf(tet));
        }
        if (mMarkingAssortment != null){
            txtArticol.setText(mMarkingAssortment);
        }
        else{
            txtArticol.setText("");
        }
        if (mPriceAssortment==null){
            txtPrice.setText("-");
        }
        else {
            txtPrice.setText(mPriceAssortment);
        }
    }
}
