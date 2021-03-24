package md.intelectsoft.stockmanager;

import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;

import static md.intelectsoft.stockmanager.ListAssortment.AssortimentClickentSendIntent;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.CreateRevision;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.GetAssortiment;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.GetWareHouseList;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.Ping;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.Response_from_CreateRevision;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.Response_from_Ping;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.SaveRevisionLine;

public class Inventory extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    TextView txtArticol,txtCountInit,txtCount,txtTotal,txtNames,txtInpBarcode,txtCode,inpBarcode;
    TextView textLastProductScanned, textLastProductScannedCount, lastScannedTitle;
    ImageButton btn_showKeyboard,btn_addTouch,btn_open_list;
    ToggleButton auto_input_count;
    Button btn_ok;
    ListView list_added_touch;

    Menu menu;
    TimerTask timerTaskSync;
    Timer sync;
    JSONArray json_array,array_added_manual;
    JSONObject sendAssortiment,sendRevision;

    ProgressDialog pgH;
    ProgressBar pgB;
    Integer WeightPrefix;
    Double countInRevision;
    final boolean[] show_keyboard = {false};
    String Name,Price,Marking,Uid , UserId,ip_,port_,Remain,RevisionNumber,RevisionName,RevisionID, barcode,WareID;

    AlertDialog.Builder builderType;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list_added = new ArrayList<>();
    SimpleAdapter simpleAdapterASL;

    Boolean onCreat = false;

    SharedPreferences revisionHistory;
    
    int REQUEST_CODE_OPEN_LIST_ASSORTMENT = 202,REQUEST_FROM_COUNT_INV = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar_inventory);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_inventory);
        NavigationView navigationView = findViewById(R.id.nav_view_inventory);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        txtArticol=findViewById(R.id.txtMarking_asortment_inventory);

        txtCount=findViewById(R.id.txtCant_added_inventory);
        txtCountInit=findViewById(R.id.txtcnt_init_inventory);
        txtTotal=findViewById(R.id.txt_total_cant_inventory);

        textLastProductScanned = findViewById(R.id.textLastProductScanned);
        textLastProductScannedCount = findViewById(R.id.textLastProductScannedCount);
        lastScannedTitle = findViewById(R.id.textView56);

        txtNames=findViewById(R.id.txtName_assortment_inventory);
        txtCode=findViewById(R.id.txtcode_assortment_inventory);
        inpBarcode=findViewById(R.id.txt_input_barcode_inventory);
        txtInpBarcode=findViewById(R.id.txtBarcode_assortment_inventory);

        pgH =new ProgressDialog(Inventory.this);
        btn_ok=findViewById(R.id.btn_accept_inventory);
        pgB=findViewById(R.id.progressBar_inventory);
        btn_showKeyboard=findViewById(R.id.btn_write_manual_inventory);
        btn_addTouch=findViewById(R.id.btn_touch_open_asl_inventory);
        auto_input_count=findViewById(R.id.btn_auto_cant_inventory);
        list_added_touch = findViewById(R.id.LW_asl_added_inventory);
        btn_open_list = findViewById(R.id.btn_touch_open_asl_inventory_listAdded);

        final SharedPreferences getRevisions = getSharedPreferences("Revision", MODE_PRIVATE);
        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        final SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);

        simpleAdapterASL = new SimpleAdapter(this, asl_list_added,R.layout.show_asl_inventory, new String[]{"Name","Cant"}, new int[]{R.id.textName,R.id.textCantitate});

        RevisionName = getRevisions.getString("RevisionName","");
        RevisionNumber = getRevisions.getString("RevisionNumber","");
        RevisionID = getRevisions.getString("RevisionID","");
        int type = getRevisions.getInt("Type",2);
        WeightPrefix = getRevisions.getInt("WeightPrefix",0);

        revisionHistory = getSharedPreferences(RevisionID, MODE_PRIVATE);
        String nameLastProduct = revisionHistory.getString("name","");
        String countLastProduct = revisionHistory.getString("count","");

        textLastProductScanned.setText(nameLastProduct);
        textLastProductScannedCount.setText(countLastProduct);

        setTitle(RevisionNumber);

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        switch (type){
            case 1: {
                WareID = WorkPlace.getString("Uid", "0");
                onCreat = true;
                if(WareID.equals("0") || WareID.equals("")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
                    dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                    dialog.setCancelable(false);
                    dialog.setMessage(getResources().getString(R.string.txt_inventory_not_selected_workplace));
                    dialog.setPositiveButton(getResources().getString(R.string.msg_btn_create_new_revision_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                            pgH.setIndeterminate(true);
                            pgH.setCancelable(false);
                            pgH.show();
                            getWareHouse();
                        }
                    });
                    dialog.setNegativeButton(getResources().getString(R.string.msg_dialog_inventroy_select_workplace), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onCreat=false;
                            Intent Logins = new Intent(".MenuWorkPlace");
                            startActivity(Logins);
                        }
                    });
                    dialog.setNeutralButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.show();
                }
            }break;
            case 0 :{
                WareID = WareHouse.getString("WareUid", "");
            }break;
        }

        boolean ShowCode = Settings.getBoolean("ShowCode",false);
        if (!ShowCode){
            txtCode.setVisibility(View.INVISIBLE);
        }
        UserId = User.getString("UserID","Non");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");
        
        json_array=new JSONArray();
        array_added_manual = new JSONArray();

        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,2000,2000);

        inpBarcode.requestFocus();
        inpBarcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                asl_list_added.clear();
                list_added_touch.setAdapter(simpleAdapterASL);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    getAssortment(User.getString("UserID", ""));
                }else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER) {
                    getAssortment(User.getString("UserID", ""));
                }
                return false;
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog_accept = new AlertDialog.Builder(Inventory.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
                dialog_accept.setTitle("Ati finisat inventarierea cumva?");
                dialog_accept.setMessage("Datele au fost salvate pe server.\nDoriti sa salvati datele si pe terminal?\nInchide - nu va salva datele.");
                dialog_accept.setPositiveButton("Salveaza", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                dialog_accept.setNeutralButton("Inchide", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SharedPreferences SaveCountName = getSharedPreferences("SaveNameInventory", MODE_PRIVATE);
                        SaveCount.edit().clear().apply();
                        SaveCountName.edit().clear().apply();
                        finish();
                    }
                });
                dialog_accept.setNegativeButton("Anulare", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog_accept.create().show();

            }
        });

        btn_showKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asl_list_added.clear();
                list_added_touch.setAdapter(simpleAdapterASL);
                if(!show_keyboard[0]) {
                    show_keyboard[0] = true;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(inpBarcode, InputMethodManager.SHOW_IMPLICIT);
                    inpBarcode.setInputType(InputType.TYPE_CLASS_TEXT);
                }else{
                    show_keyboard[0] = false;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inpBarcode.getWindowToken(), 0);
                }
            }
        });
        btn_addTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asl_list_added.clear();
                list_added_touch.setAdapter(simpleAdapterASL);
                Intent AddingASL = new Intent(".AssortmentMobile");
                AddingASL.putExtra("ActivityCount", 171);
                AddingASL.putExtra("AutoCount", auto_input_count.isChecked());
                AddingASL.putExtra("WeightPrefix",WeightPrefix);
                AddingASL.putExtra("WareID",WareID);
                startActivityForResult(AddingASL, REQUEST_CODE_OPEN_LIST_ASSORTMENT);
            }
        });

        btn_open_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent list_add = new Intent(Inventory.this,ListAddedAssortmentInventory.class);
                startActivity(list_add);
            }
        });
    }
    private void getAssortment(String user){
        show_keyboard[0] = false;
        pgB.setVisibility(View.VISIBLE);
        barcode = inpBarcode.getText().toString();
        int toInt = 201;
        if(barcode.length() >= 7){
            String aftercur = barcode.substring(0,2);
            try{
                toInt = Integer.valueOf(aftercur);
            }
            catch (Exception e){
                toInt = 202;
            }

            sendAssortiment = new JSONObject();
            try {
                if(toInt == WeightPrefix) {
                    sendAssortiment.put("AssortmentIdentifier", barcode.substring(0,7));
                }else{
                    sendAssortiment.put("AssortmentIdentifier", inpBarcode.getText().toString());
                }
                sendAssortiment.put("ShowStocks", true);
                sendAssortiment.put("UserID", user);
                sendAssortiment.put("WarehouseID",WareID );
                sendAssortiment.put("RevisionID", RevisionID);
            } catch (JSONException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
            }
            txtInpBarcode.setText(inpBarcode.getText().toString());
            inpBarcode.setText("");
            inpBarcode.requestFocus();
            URL getASL = GetAssortiment(ip_, port_);
            new AsyncTask_GetAssortiment().execute(getASL);
        }
        else{
            pgB.setVisibility(View.INVISIBLE);
            txtNames.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
            txtArticol.setText("--");
            txtCount.setText("0");
            txtCode.setText("-");
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        getMenuInflater().inflate(R.menu.menu_inventory, menus);
        this.menu = menus;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_close_inventory : {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                dialog.setCancelable(false);
                dialog.setMessage(getResources().getString(R.string.msg_dialog_inchide_revizia));
                dialog.setPositiveButton(getResources().getString(R.string.msg_dialog_close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SaveCount.edit().clear().apply();
                        finish();
                    }
                });
                dialog.setNegativeButton(getResources().getString(R.string.msg_dialog_close_ramine), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getResponse_from_GetAssortiment(URL send_bills) {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(1500);
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
            ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
        } finally {
            assert send_bill_Connection != null;
            send_bill_Connection.disconnect();
        }
        return data;
    }

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                 Inventory.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        URL generatedURL =  Ping(ip_, port_);
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

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
            ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
        } finally {
            assert send_bill_Connection != null;
            send_bill_Connection.disconnect();
        }
        return data;
    }
    class AsyncTask_Ping extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String pings="";
            try {
                pings=Response_from_Ping(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return pings;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.equals("true")) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable( Inventory.this, R.drawable.signal_wi_fi_48));
            }else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(Inventory.this, R.drawable.no_signal_wi_fi_48));
            }
        }
    }
    class AsyncTask_GetAssortiment extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="";
            response = getResponse_from_GetAssortiment(urls[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            pgB.setVisibility(View.INVISIBLE);

            if (!response.equals("")) {
                try {
                    JSONObject responseAssortiment = new JSONObject(response);
                    Integer ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        Name = responseAssortiment.getString("Name");
                        Marking = responseAssortiment.getString("Marking");
                        Uid = responseAssortiment.getString("AssortimentID");
                        Remain = responseAssortiment.getString("Remain");
                        Price = responseAssortiment.getString("Price");
                        String Code = responseAssortiment.getString("Code");
                        String Unit = responseAssortiment.getString("Unit");
                        String revisionCount = responseAssortiment.getString("RevisionCount");
                        boolean allowInteger = responseAssortiment.getBoolean("AllowNonIntegerSale");

                        countInRevision = Double.valueOf(revisionCount.replace(",", "."));

                        if (!Marking.equals("null")) {
                            txtArticol.setText(Marking);
                        } else {
                            txtArticol.setText("-");
                        }
                        txtNames.setText(Name);
                        txtCode.setText(Code);
                        if(!auto_input_count.isChecked() && barcode.length() > 5) {
                            Assortment assortment = new Assortment();
                            assortment.setBarCode(barcode);
                            assortment.setCode(Code);
                            assortment.setName(Name);
                            assortment.setPrice(Price);
                            assortment.setMarking(Marking);
                            assortment.setRemain(Remain);
                            assortment.setAssortimentID(Uid);
                            assortment.setAllowNonIntegerSale(String.valueOf(allowInteger));
                            assortment.setUnit(Unit);
                            final AssortmentParcelable assortmentParcelable = new AssortmentParcelable(assortment);

                            Intent sales = new Intent(".CountInventorytMobile");
                            sales.putExtra("WeightPrefix",WeightPrefix);
                            sales.putExtra("countScanned", countInRevision);
                            sales.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
                            startActivityForResult(sales, REQUEST_FROM_COUNT_INV);

                            inpBarcode.setText("");
                            inpBarcode.requestFocus();
                        }else{
                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                            pgH.setIndeterminate(true);
                            pgH.setCancelable(false);
                            pgH.show();
                            sendRevision=new JSONObject();
                            try {
                                sendRevision.put("Assortiment",Uid);
                                sendRevision.put("Quantity","1");
                                sendRevision.put("RevisionID",RevisionID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(),Inventory.this);
                            }
                            txtCount.setText("1");

                            URL generateSaveLine = SaveRevisionLine(ip_,port_);
                            new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                        }
                    } else {
                        txtNames.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                        txtArticol.setText("--");
                        txtCount.setText("0");
                        txtCode.setText("-");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Inventory.this);
                }
            }else{
                txtNames.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                txtArticol.setText("--");
                txtCount.setText("0");
                txtCode.setText("-");
            }
        }
    }
    class AsyncTask_SaveRevisionLine extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {

            String response="";
            try {
                response = getResponseFromURLSaveRevisionLine(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
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
                    String errorMsg = responseAssortiment.getString("ErrorMessage");
                    if (ErrorCode == 0) {
                        inpBarcode.setText("");
                        inpBarcode.requestFocus();

                        SharedPreferences SaveCountName = getSharedPreferences("SaveNameInventory", MODE_PRIVATE);
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SharedPreferences.Editor add_count = SaveCount.edit();
                        SharedPreferences.Editor add_name = SaveCountName.edit();

                        String ExistingCount = SaveCount.getString(Uid,"0");

                        ExistingCount = ExistingCount.replace(",",".");
                        Double countExist = Double.valueOf(ExistingCount);
                        Double new_count  = Double.valueOf("1");
                        Double total_count = countExist + new_count;

                        add_name.putString(Uid,Name);
                        add_name.apply();
                        add_count.putString(Uid,String.format("%.2f",total_count));
                        add_count.apply();

                        String count_initial = String.format("%.2f",countInRevision).replace(",",".");
                        String count_total= String.format("%.2f",countInRevision + 1).replace(",",".");

                        txtCountInit.setText(count_initial);
                        txtTotal.setText(count_total);
                        Toast.makeText(Inventory.this, getResources().getString(R.string.msg_count_added_inventory), Toast.LENGTH_SHORT).show();
                    } else {
                        inpBarcode.setText("");
                        inpBarcode.requestFocus();

                        AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie) + "Eroarea: " + ErrorCode);
                        dialog.setCancelable(false);
                        dialog.setMessage("Assortimentul nu a fost salvat! Mesajul erorii: " + errorMsg);
                        dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        Toast.makeText(Inventory.this,getResources().getString(R.string.msg_error_code)   + ErrorCode , Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Inventory.this);
                }
            }else{
                inpBarcode.setText("");
                inpBarcode.requestFocus();
                Toast.makeText(Inventory.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_FROM_COUNT_INV){
            if (resultCode==RESULT_CANCELED){
                inpBarcode.setText("");
                inpBarcode.requestFocus();
            }else if (resultCode==RESULT_OK){
                inpBarcode.setText("");
                inpBarcode.requestFocus();
                Toast.makeText(Inventory.this,getResources().getString(R.string.msg_count_added_inventory), Toast.LENGTH_SHORT).show();
                assert data != null;
                String count = data.getStringExtra("Count");


                txtCount.setText(count);
                revisionHistory.edit().putString("count", count).putString("name", Name).apply();
                textLastProductScanned.setText(Name);
                textLastProductScannedCount.setText(count);

                double countAdded = countInRevision + Double.parseDouble(count.replace(",","."));

                txtTotal.setText(String.format("%.2f",countAdded).replace(",","."));
                txtCountInit.setText(String.format("%.2f",countInRevision).replace(",","."));
            }
        }else if (requestCode==REQUEST_CODE_OPEN_LIST_ASSORTMENT){
            if (resultCode==RESULT_CANCELED){
                inpBarcode.setText("");
                inpBarcode.requestFocus();
            }else if (resultCode==RESULT_OK) {
                inpBarcode.setText("");
                inpBarcode.requestFocus();
                try {
                    JSONArray esult_added_touch = new JSONArray(data.getStringExtra("AssortmentInventoryAddedArray"));
                    for(int k = 0; k < esult_added_touch.length(); k++){
                        JSONObject json= esult_added_touch.getJSONObject(k);
                        String Name = json.getString("AssortimentName");
                        String Cant = json.getString("Quantity");

                        HashMap<String, Object> asl_ = new HashMap<>();
                        asl_.put("Name",Name);
                        asl_.put("Cant",Cant);
                        asl_list_added.add(asl_);
                    }

                    list_added_touch.setAdapter(simpleAdapterASL);

                    textLastProductScanned.setText(revisionHistory.getString("name", ""));
                    textLastProductScannedCount.setText(revisionHistory.getString("count", ""));

                    txtCountInit.setText("-");
                    txtTotal.setText("-");
                    txtCount.setText("-");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        asl_list_added.clear();
        list_added_touch.setAdapter(simpleAdapterASL);
        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                inpBarcode.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        inpBarcode.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        inpBarcode.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        inpBarcode.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        inpBarcode.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        inpBarcode.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        inpBarcode.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        inpBarcode.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        inpBarcode.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        inpBarcode.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        inpBarcode.append("0");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = inpBarcode.getText().toString();
                        if(!inpBarcode.getText().toString().equals("")) {
                            inpBarcode.setText(test.substring(0, test.length() - 1));
                            inpBarcode.requestFocus();
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_inventory);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
            dialog.setCancelable(false);
            dialog.setMessage(getResources().getString(R.string.msg_dialog_inchide_revizia));
            dialog.setPositiveButton(getResources().getString(R.string.msg_dialog_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                    SaveCount.edit().clear().apply();
                    finish();
                }
            });
            dialog.setNegativeButton(getResources().getString(R.string.msg_dialog_close_ramine), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_conect) {
            Intent MenuConnect = new Intent(".MenuConnect");
            startActivity(MenuConnect);
        } else if (id == R.id.menu_workplace) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 8);
            startActivity(Logins);
        } else if (id == R.id.menu_printers) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 9);
            startActivity(Logins);
        } else if (id == R.id.menu_securitate) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 10);
            startActivity(Logins);
        } else if (id == R.id.menu_about) {
            Intent MenuConnect = new Intent(".MenuAbout");
            startActivity(MenuConnect);
        } else if (id == R.id.menu_exit) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
            dialog.setCancelable(false);
            dialog.setMessage(getResources().getString(R.string.msg_dialog_inchide_revizia));
            dialog.setPositiveButton(getResources().getString(R.string.msg_dialog_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                    SaveCount.edit().clear().apply();
                    finish();
                }
            });
            dialog.setNegativeButton(getResources().getString(R.string.msg_dialog_close_ramine), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_inventory);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        String WorkPlaceID = WorkPlace.getString("Uid","0");
        String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if(!WorkPlaceID.equals("0") && !WorkPlaceID.equals("")){
           WareID = WorkPlaceID;
        }else{
            if(!onCreat) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Inventory.this);
                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                dialog.setCancelable(false);
                dialog.setMessage(getResources().getString(R.string.txt_inventory_not_selected_workplace));
                dialog.setPositiveButton(getResources().getString(R.string.msg_btn_create_new_revision_dialog), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                        pgH.setIndeterminate(true);
                        pgH.setCancelable(false);
                        pgH.show();
                        getWareHouse();
                    }
                });
                dialog.setNegativeButton(getResources().getString(R.string.msg_dialog_inventroy_select_workplace), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onCreat=false;
                        Intent Logins = new Intent(".MenuWorkPlace");
                        startActivity(Logins);
                    }
                });
                dialog.setNeutralButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.show();
            }
        }
    }

    public void show_WareHouse(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Inventory.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Inventory.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                dialogInterface.dismiss();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String WareGUid= String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareName= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

//                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
//                SharedPreferences.Editor addWareHouse = WareHouse.edit();
//                addWareHouse.putString("WareName",WareName);
//                addWareHouse.putString("WareUid",WareGUid);
//                addWareHouse.putString("WareCode",WareCode);
//                addWareHouse.apply();

                stock_List_array.clear();
                pgH.setTitle(getResources().getString(R.string.msg_create_new_revision));
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();
                URL generateCreateRevision = CreateRevision(ip_,port_,UserId,WareGUid);
                new AsyncTask_CreateRevison().execute(generateCreateRevision);
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void getWareHouse(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouse().execute(getWareHouse);
    }
    class AsyncTask_WareHouse extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Inventory.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (!response.equals("false")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            JSONArray ListWare = responseWareHouse.getJSONArray("Warehouses");
                            for (int i = 0; i < ListWare.length(); i++) {
                                JSONObject object = ListWare.getJSONObject(i);
                                String WareCode = object.getString("Code");
                                String WareName = object.getString("Name");
                                String WareUid = object.getString("WarehouseID");
                                HashMap<String, Object> WareHouse = new HashMap<>();
                                WareHouse.put("Name", WareName);
                                WareHouse.put("Code", WareCode);
                                WareHouse.put("Uid", WareUid);
                                stock_List_array.add(WareHouse);
                            }
                            show_WareHouse();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables) getApplication()).appendLog(e.getMessage(), Inventory.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Inventory.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Inventory.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Inventory.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_CreateRevison extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="";
            try {
                response = Response_from_CreateRevision(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if(!response.equals("")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            JSONObject Revisons = responseWareHouse.getJSONObject("Rervision");

                            RevisionName = Revisons.getString("Name");
                            RevisionNumber = Revisons.getString("RevisionNumber");
                            RevisionID = Revisons.getString("RevisionID");
                            WeightPrefix = Revisons.getInt("WeightPrefix");

                            setTitle("Revizia: " +RevisionNumber);

//                            String Name = Revisons.getString("Name");
//                            String Number = Revisons.getString("RevisionNumber");
//                            String RevisionId = Revisons.getString("RevisionID");
//                            String prefix = Revisons.getString("WeightPrefix");
//                            SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
//                            SharedPreferences.Editor inpRev = Revision.edit();
//                            inpRev.putString("RevisionName",Name);
//                            inpRev.putString("RevisionNumber",Number);
//                            inpRev.putString("RevisionID",RevisionId);
//                            inpRev.putInt("WeightPrefix",Integer.valueOf(prefix));
//                            inpRev.putInt("Type",0);
//                            inpRev.apply();
//
//                            Intent stockInv= new Intent(".InventoryMobile");
//                            startActivity(stockInv);
//                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
                        }
                    }else{
                        Toast.makeText(Inventory.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), Inventory.this);
                }
            }else{
                Toast.makeText(Inventory.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
