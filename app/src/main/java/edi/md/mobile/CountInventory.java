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

import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveRevisionLine;

public class CountInventory extends AppCompatActivity {
    Button btn_ok,btn_cancel;
    TextView txtBarCode,txtArticol,txtStock,txtName,txtCode,txtTotalScan,txtRemainScan,txtSurplusScan,txtPrice;
    EditText inpCount;
    Switch cant_final;

    ProgressDialog pgH;
    int WeightPrefix;
    char decSeparator;
    JSONObject sendAssortiment;

    String Name,Marking,UidAsortiment , ip_,port_,UserId,Remain,RevisionID,Code,Barcode,Price;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar_count_inventory);
        setSupportActionBar(toolbar);

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
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        final SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);

        Boolean ShowCode = Settings.getBoolean("ShowCode",false);
        if (!ShowCode){
            txtCode.setVisibility(View.INVISIBLE);
        }
        Boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        inpCount.requestFocus();
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            inpCount.requestFocus();
        }
        UserId = User.getString("UserID","Non");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        RevisionID=getRevisions.getString("RevisionID","");

        NumberFormat nf = NumberFormat.getInstance();
        if (nf instanceof DecimalFormat) {
            DecimalFormatSymbols sym = ((DecimalFormat) nf).getDecimalFormatSymbols();
            sym.setDecimalSeparator('.');
            decSeparator = sym.getDecimalSeparator();
        }

        Intent countInv= getIntent();

        Name = countInv.getStringExtra("Name");
        Marking = countInv.getStringExtra("mMarkingAssortment");
        Remain = countInv.getStringExtra("Remain");
        UidAsortiment = countInv.getStringExtra("Uid");
        Barcode= countInv.getStringExtra("BarCode");
        Price= countInv.getStringExtra("mPriceAssortment");
        Code =countInv.getStringExtra("Code");
        String Unit = countInv.getStringExtra("mUnitAssortment");
        WeightPrefix= countInv.getIntExtra("WeightPrefix",0);


        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
        String ExistingCount = SaveCount.getString(UidAsortiment,"0");
        ExistingCount = ExistingCount.replace(",",".");
        txtTotalScan.setText(ExistingCount + " " + Unit);

        double countAdded = Double.valueOf(ExistingCount);
        double Remain_in_program =Double.valueOf(Remain);


        if (Remain_in_program!=0.0){
            double remain_scan=  Remain_in_program - countAdded;
            if (remain_scan<=0) {
                txtRemainScan.setText("0 " + Unit);
                double surplus_scan = Math.abs(remain_scan);
                txtSurplusScan.setText(String.valueOf(surplus_scan) + " " + Unit);
            }else {
                txtRemainScan.setText(String.valueOf(remain_scan) + " " + Unit);
                txtSurplusScan.setText("0 " + Unit);
            }
        }else{
            txtRemainScan.setText("0 " + Unit);
            double surplus_scan = Math.abs(Remain_in_program - countAdded);
            txtSurplusScan.setText(String.valueOf(surplus_scan) + " " + Unit);
        }

        String prefic = Barcode.substring(0,2);
        Integer toInt = Integer.valueOf(prefic);

        if(toInt==WeightPrefix){
            String afterseven = Barcode.substring(7,12);
            String afetrCut = afterseven.substring(0,2) + decSeparator + afterseven.substring(2);
            Double tet = Double.valueOf(afetrCut);
            inpCount.setText(String.valueOf(tet));
        }
        txtName.setText(Name);
        txtCode.setText(Code);
        if (!Marking.equals("null")){
            txtArticol.setText(Marking);
        }else{
            txtArticol.setText("");
        }

        txtBarCode.setText(Barcode);
        txtStock.setText(Remain + " " + Unit);
        if (Price.equals("0") || Price==null){
            txtPrice.setText("-");
        }else {
            txtPrice.setText(Price);
        }

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
                if(!inpCount.getText().toString().equals("")) {
                    pgH.setMessage("loading..");
                    pgH.setIndeterminate(true);
                    pgH.setCancelable(false);
                    pgH.show();
                    sendAssortiment = new JSONObject();
                    try {
                        sendAssortiment.put("Assortiment", UidAsortiment);
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
        });

        inpCount.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!inpCount.getText().toString().equals("")) {
                        pgH.setMessage("loading..");
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();
                        sendAssortiment = new JSONObject();
                        try {
                            sendAssortiment.put("Assortiment", UidAsortiment);
                            sendAssortiment.put("Quantity", inpCount.getText().toString());
                            sendAssortiment.put("RevisionID", RevisionID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
                        }

                        URL generateSaveLine = SaveRevisionLine(ip_, port_);
                        new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                    }else {
                        Toast.makeText(CountInventory.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                        inpCount.requestFocus();
                    }
                }else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    if (!inpCount.getText().toString().equals("")) {
                        pgH.setMessage("loading..");
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();
                        sendAssortiment = new JSONObject();
                        try {
                            sendAssortiment.put("Assortiment", UidAsortiment);
                            sendAssortiment.put("Quantity", inpCount.getText().toString());
                            sendAssortiment.put("RevisionID", RevisionID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),CountInventory.this);
                        }

                        URL generateSaveLine = SaveRevisionLine(ip_, port_);
                        new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                    }else {
                        Toast.makeText(CountInventory.this, getResources().getString(R.string.txt_header_inp_count), Toast.LENGTH_SHORT).show();
                        inpCount.requestFocus();
                    }
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
                    Integer ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        //Sumez cantitatea introdusa noua cu cea existenta si o salvez
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SharedPreferences.Editor add_count = SaveCount.edit();

                        String ExistingCount = SaveCount.getString(UidAsortiment,"0");
                        ExistingCount = ExistingCount.replace(",",".");

                        Double countExist = Double.valueOf(ExistingCount);
                        Double new_count  = Double.valueOf(inpCount.getText().toString());
                        Double total_count = countExist + new_count;

                        if (cant_final.isChecked()){
                            total_count = Double.valueOf(inpCount.getText().toString());
                        }
                        add_count.putString(UidAsortiment,String.format("%.2f",total_count));
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
}
