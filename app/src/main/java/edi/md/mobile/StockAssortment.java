package edi.md.mobile;

import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.mobile.Settings.Assortment;
import edi.md.mobile.Utils.AssortmentInActivity;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetAssortiment;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveAccumulateAssortmentList;

public class StockAssortment extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    TextView txt_input_barcode,txtBarcode_introdus;
    ImageButton btn_write_barcode,btn_delete,btn_open_asl_list,btn_print_lable;
    Button btn_ok,btn_cancel,btn_change_stock;
    ListView list_of_stock_assortment;
    ProgressBar pgBar;

    AlertDialog.Builder builderType;
    SimpleAdapter simpleAdapterASL;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();

    String ip_,port_,UserId,WareUid,WareName,laberl,uid_selected,barcode_introdus;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;
    private Handler mHandler;

    JSONObject  sendDocument,document_etichete,sendAssortiment;
    JSONArray mArrayAssortment, mArrayForEtichete;

    Menu menu;
    final boolean[] show_keyboard = {false};
    
    int REQUEST_FROM_LIST_ASSORTMENT = 225,REQUEST_FROM_COUNT_STOCK = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_stock_assortment);
        Toolbar toolbar = findViewById(R.id.toolbar_stock_assortment);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_stock_assortment);
        NavigationView navigationView = findViewById(R.id.nav_view_stock_assortment);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        txt_input_barcode= findViewById(R.id.txt_input_barcode_stock_assortment);
        txtBarcode_introdus = findViewById(R.id.txtBarcodes_introdus_stock_assortment);
        btn_write_barcode = findViewById(R.id.btn_write_manual_stock_assortment);
        btn_delete = findViewById(R.id.btn_delete_stock_assortment);
        btn_open_asl_list = findViewById(R.id.btn_touch_open_asl_stock_assortment);
        btn_print_lable = findViewById(R.id.btn_print_lable_stock_assortment);
        pgBar = findViewById(R.id.progressBar_stock_assortment);
        btn_change_stock =findViewById(R.id.btn_change_stock_stock_assortment);
        btn_cancel = findViewById(R.id.btn_cancel_stock_assortment);
        btn_ok = findViewById(R.id.btn_ok_stock_assortment);
        list_of_stock_assortment = findViewById(R.id.LL_list_stock_assortment);
        pgH=new ProgressDialog(StockAssortment.this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        mArrayAssortment =new JSONArray();
        mArrayForEtichete =new JSONArray();

        final String WorkPlaceID = WorkPlace.getString("Uid","0");
        final String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        if (WorkPlaceName.equals("Nedeterminat")|| WorkPlaceName.equals("")){
            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();
            ((Variables)getApplication()).setDownloadASLVariable(false);
            getWareHouse();
        }
        else{
            btn_change_stock.setText(WorkPlaceName);
            WareUid = WorkPlaceID;
            WareName = WorkPlaceName;
        }

        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,2000,2000);

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == 10) {
                    if (msg.arg1 == 11)
                        pgH.dismiss();
                    Toast.makeText(StockAssortment.this, getResources().getString(R.string.msg_imprimare_sales), Toast.LENGTH_SHORT).show();
                }
                else  if(msg.what == 20) {
                    pgH.dismiss();
                    Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_errore_label_printer), Toast.LENGTH_SHORT).show();
                }
                else if (msg.what==201){
                    pgH.dismiss();
                    AlertDialog.Builder input = new AlertDialog.Builder(StockAssortment.this);
                    input.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                    input.setCancelable(false);
                    input.setMessage(getResources().getString(R.string.msg_erroare_la_imrpimare)+ "\n"+ msg.obj + "\n"+"\n"+ getResources().getString(R.string.msg_verifica_imprimantele_bluetooth));
                    input.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = input.create();
                    dialog.show();
                }
            }
        };

        txt_input_barcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) getAssortment();
                else if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER) getAssortment();
                return false;
            }
        });
        list_of_stock_assortment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                uid_selected=(String)asl_list.get(position).get("Uid");
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uid_selected!=null){
                    try {
                        for (int i = 0; i< mArrayAssortment.length(); i++) {
                            JSONObject json = mArrayAssortment.getJSONObject(i);
                            String Uid = json.getString("AssortimentID");
                            if (uid_selected.contains(Uid)) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    mArrayAssortment.remove(i);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    mArrayForEtichete.remove(i);
                                }
                            }
                            asl_list.clear();
                            showAssortment();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                    }
                }
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setCancelable(false);
                pgH.setIndeterminate(true);
                pgH.show();
                URL generateSaveAccumulateAssortmentList = SaveAccumulateAssortmentList(ip_,port_);
                new AsyncTask_SaveAccumulateAssortmentList().execute(generateSaveAccumulateAssortmentList);
            }
        });

        btn_print_lable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref =getSharedPreferences("Conected printers", MODE_PRIVATE);
                SharedPreferences sPrefSetting =getSharedPreferences("Save setting", MODE_PRIVATE);
                final String adresMAC = sPref.getString("AdressPrinters",null);
                laberl = sPrefSetting.getString("Lable",null);
                if(adresMAC!=null) {
                    pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                    pgH.setCancelable(false);
                    pgH.setIndeterminate(true);
                    pgH.show();
                    new Thread() {
                        public void run() {
                            Connection connection = new BluetoothConnection(adresMAC);
                            try {
                                connection.open();
                                for (int j = 0; j< mArrayForEtichete.length(); j++){
                                    JSONObject asl_obj = mArrayForEtichete.getJSONObject(j);
                                    String UnitInPackage = asl_obj.getString("UnitInPackage");
                                    String UnitPrice = asl_obj.getString("UnitPrice");
                                    String Unit = asl_obj.getString("Unit");
                                    String Code = asl_obj.getString("Code");
                                    String Barcode = asl_obj.getString("Barcode");
                                    String Name= asl_obj.getString("Name");
                                    String Price = asl_obj.getString("Price");

                                    String price_unit;
                                    if (UnitInPackage==null){
                                        price_unit = UnitPrice + "/" + Unit;
                                    }else{
                                        price_unit = UnitPrice + "/" + UnitInPackage;
                                    }

                                    String Count = asl_obj.getString("Count");
                                    int count_to_int = Integer.valueOf(Count);

                                    Date datess = new Date();
                                    SimpleDateFormat sdfChisinau = new SimpleDateFormat("yyyy.MM.dd");
                                    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
                                    sdfChisinau.setTimeZone(tzInChisinau);
                                    String sDateInChisinau = sdfChisinau.format(datess); // Convert to String first

                                    for(int sciotcik=0; count_to_int > sciotcik; sciotcik++){
                                        String codes = "codes_imprim";
                                        String data_imprim = "data_imprim";
                                        String name_imprim = "name_imprim";
                                        String rus_name_imprim = "rus_imprim";
                                        String price_imprim = "price_imprim";
                                        String price_unit_imprim = "price_unit_imprim";
                                        String barcode_imprim = "barcode_imprim";
                                        if(laberl!=null) {
                                            laberl = laberl.replace(codes, Code);
                                            laberl = laberl.replace(data_imprim, sDateInChisinau);
                                            laberl = laberl.replace(name_imprim, Name);
                                            laberl = laberl.replace(rus_name_imprim, "");
                                            laberl = laberl.replace(price_imprim, Price);
                                            laberl = laberl.replace(price_unit_imprim, price_unit);
                                            laberl = laberl.replace(barcode_imprim, Barcode);
                                            connection.write(laberl.getBytes());
                                        }
                                        else{
                                            mHandler.obtainMessage(20, 12, -1)
                                                    .sendToTarget();
                                            break;
                                        }
                                    }
                                }
                                mHandler.obtainMessage(10, 11, -1)
                                        .sendToTarget();
                            } catch (ConnectionException e) {
                                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                                mHandler.obtainMessage(201, 14, -1,e.toString())
                                        .sendToTarget();
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                            }
                            try {
                                connection.close();
                            } catch (ConnectionException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                            }
                        }
                    }.start();
                }else{
                    Toast.makeText(StockAssortment.this,getResources().getString(R.string.txt_not_conected_printers), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_open_asl_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AddingASL = new Intent(".AssortmentMobile");
                AddingASL.putExtra("ActivityCount", 151);
                if(WorkPlaceID.equals("0")){
                    AddingASL.putExtra("WareID",WareUid);
                }
                startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
            }
        });
        btn_write_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!show_keyboard[0]) {
                    show_keyboard[0] = true;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txt_input_barcode, InputMethodManager.SHOW_IMPLICIT);
                    txt_input_barcode.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else {
                    show_keyboard[0] = false;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txt_input_barcode.getWindowToken(), 0);
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog();
            }
        });
        btn_change_stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stock_List_array.clear();
                getWareHouseChange();
            }
        });

    }
    private void exitDialog(){
        if(mArrayAssortment.length()==0){
            finish();
        }
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(StockAssortment.this);
            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
            dialog.setCancelable(false);
            dialog.setMessage(getResources().getString(R.string.txt_waring_documentul_nusalvat_doriti_salvati));
            dialog.setPositiveButton(getResources().getString(R.string.msg_dialog_close), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_stock_assortment);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitDialog();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        getMenuInflater().inflate(R.menu.menu_stock_assortment, menus);
        this.menu = menus;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_close_stock_assortment) {
            exitDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
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
            exitDialog();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_stock_assortment);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                StockAssortment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        URL generatedURL = Ping(ip_, port_);
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

    }
    public void show_WareHouse(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(StockAssortment.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(StockAssortment.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                finish();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String WareGUid= String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareNames= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareNames);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                btn_change_stock.setText(WareNames);
                WareUid = WareGUid;
                WareName = WareNames;
                stock_List_array.clear();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_WareHouseChange(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(StockAssortment.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(StockAssortment.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String WareGUid= String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareNames= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareNames);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                btn_change_stock.setText(WareNames);
                WareUid = WareGUid;
                WareName = WareNames;
                stock_List_array.clear();
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
    public void getWareHouseChange(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouseChange().execute(getWareHouse);
    }
    public void showAssortment(){
        try {
            for (int i = 0; i< mArrayAssortment.length(); i++){
                JSONObject json= mArrayAssortment.getJSONObject(i);
                HashMap<String, Object> asl_ = new HashMap<>();
                String Name = json.getString("AssortimentName");
                String Cant = json.getString("Quantity");
                String Uid = json.getString("AssortimentID");
                String NameWare = json.getString("WarehouseName");
                asl_.put("Name",Name);
                asl_.put("Cant",Cant);
                asl_.put("Uid",Uid);
                asl_.put("Ware",NameWare);
                asl_list.add(asl_);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
        }
        simpleAdapterASL = new SimpleAdapter(this, asl_list,R.layout.show_stock_acumulated_asl, new String[]{"Name","Cant","Ware"},
                new int[]{R.id.textName_stock_asl,R.id.textCantitate_stock_asl,R.id.txtStockAsl_stock_asl});
        list_of_stock_assortment.setAdapter(simpleAdapterASL);
    }
    private void getAssortment(){
        txt_input_barcode.requestFocus();
        if (!txt_input_barcode.getText().toString().equals("")) {
            pgBar.setVisibility(ProgressBar.VISIBLE);
            show_keyboard[0] = false;
            sendAssortiment = new JSONObject();
            try {
                sendAssortiment.put("AssortmentIdentifier", txt_input_barcode.getText().toString());
                sendAssortiment.put("ShowStocks", true);
                sendAssortiment.put("UserID", UserId);
                sendAssortiment.put("WarehouseID", WareUid);
            } catch (JSONException e) {
                e.printStackTrace();
                ((Variables) getApplication()).appendLog(e.getMessage(), StockAssortment.this);
            }
            barcode_introdus = txt_input_barcode.getText().toString();
            txt_input_barcode.setText("");
            URL getASL = GetAssortiment(ip_, port_);
            new AsyncTask_GetAssortiment().execute(getASL);
        }
    }
    public String getResponse_from_GetAssortiment(URL send_bills) {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(2000);
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
            ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data.toString();
    }
    public String getResponseFromURLSendAccumulatedAssortiment(URL send_bills) throws IOException {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(5000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());

            wr.writeBytes(String.valueOf(mArrayAssortment));
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
            ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data;
    }
    class AsyncTask_Ping extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String ping="";
            try {
                ping=Response_from_Ping(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ping;
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.equals("true")) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(StockAssortment.this, R.drawable.signal_wi_fi_48));
            }else {
                this.cancel(true);
                menu.getItem(0).setIcon(ContextCompat.getDrawable(StockAssortment.this, R.drawable.no_signal_wi_fi_48));
            }
        }
    }
    class AsyncTask_WareHouse extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
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
                            ((Variables) getApplication()).appendLog(e.getMessage(), StockAssortment.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), StockAssortment.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_WareHouseChange extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
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
                            show_WareHouseChange();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables) getApplication()).appendLog(e.getMessage(), StockAssortment.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), StockAssortment.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_GetAssortiment extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            return getResponse_from_GetAssortiment(urls[0]);
        }
        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("")) {
                try {
                    JSONObject responseAssortiment = new JSONObject(response);
                    int ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        String Names = responseAssortiment.getString("Name");
                        String Price = responseAssortiment.getString("Price");
                        String Uid = responseAssortiment.getString("AssortimentID");
                        String Remain = responseAssortiment.getString("Remain");
                        String Marking = responseAssortiment.getString("Marking");
                        String Codes = responseAssortiment.getString("Code");
                        String Unit = responseAssortiment.getString("Unit");
                        String UnitInPackage = responseAssortiment.getString("UnitInPackage");
                        String UnitPrice = responseAssortiment.getString("UnitPrice");
                        Double priceDouble = Double.valueOf(Price);
                        Price = String.format("%.2f", priceDouble);
                        Double priceunit = Double.valueOf(UnitPrice);
                        UnitPrice = String.format("%.2f", priceunit);
                        String Barcodes = responseAssortiment.getString("BarCode");

                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtBarcode_introdus.setText(barcode_introdus);

                        Assortment assortment = new Assortment();
                        assortment.setBarCode(Barcodes);
                        assortment.setCode(Codes);
                        assortment.setName(Names);
                        assortment.setPrice(Price);
                        assortment.setMarking(Marking);
                        assortment.setRemain(Remain);
                        assortment.setAssortimentID(Uid);
                        assortment.setUnitPrice(UnitPrice);
                        assortment.setUnit(Unit);
                        assortment.setUnitInPackage(UnitInPackage);
                        final AssortmentInActivity assortmentParcelable = new AssortmentInActivity(assortment);

                        Intent sales = new Intent(".CountStockAssortmentMobile");
                        sales.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
                        startActivityForResult(sales, REQUEST_FROM_COUNT_STOCK);
                    } else {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtBarcode_introdus.setText(barcode_introdus + " - " + getResources().getString(R.string.txt_depozit_nedeterminat));
                        txt_input_barcode.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                }
            }else{
                Toast.makeText(StockAssortment.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
                ((Variables)getApplication()).appendLog(response,StockAssortment.this);
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                txtBarcode_introdus.setText(barcode_introdus + " - " + getResources().getString(R.string.txt_depozit_nedeterminat));
                txt_input_barcode.requestFocus();
            }
        }
    }
    class AsyncTask_SaveAccumulateAssortmentList extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String ping="";
            try {
                ping=getResponseFromURLSendAccumulatedAssortiment(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
            }
            return ping;
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if (!response.equals("")){
                try {

                    JSONObject respons =new JSONObject(response);
                    Integer errorcode = respons.getInt("ErrorCode");
                    if (errorcode==0){
                        String documentNumber = respons.getString("DocumentNumber");
                        AlertDialog.Builder NameDoc = new AlertDialog.Builder(StockAssortment.this);
                        NameDoc.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        NameDoc.setMessage(getResources().getString(R.string.msg_document_save_stock_asl)+ documentNumber);
                        NameDoc.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        NameDoc.show();
                    }else{
                        AlertDialog.Builder NameDoc = new AlertDialog.Builder(StockAssortment.this);
                        NameDoc.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        NameDoc.setMessage(getResources().getString(R.string.msg_document_notsaved_cod_error)+ errorcode);
                        NameDoc.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        NameDoc.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                }
            }else{
                AlertDialog.Builder NameDoc = new AlertDialog.Builder(StockAssortment.this);
                NameDoc.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                NameDoc.setMessage(getResources().getString(R.string.msg_document_not_saved_nu_raspuns_serviciu));
                NameDoc.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                NameDoc.show();
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                txt_input_barcode.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        txt_input_barcode.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        txt_input_barcode.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        txt_input_barcode.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        txt_input_barcode.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        txt_input_barcode.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        txt_input_barcode.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        txt_input_barcode.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        txt_input_barcode.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        txt_input_barcode.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        txt_input_barcode.append("0");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = txt_input_barcode.getText().toString();
                        if(!txt_input_barcode.getText().toString().equals("")) {
                            txt_input_barcode.setText(test.substring(0, test.length() - 1));
                            txt_input_barcode.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_COUNT_STOCK){
            if (resultCode==RESULT_CANCELED){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            }else if (resultCode==RESULT_OK){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();
                String response = data.getStringExtra("AssortmentStockAdded");
                try {
                    sendDocument=new JSONObject();
                    document_etichete=new JSONObject();

                    JSONObject json= new JSONObject(response);

                    String NameAsl= json.getString("AssortimentName");
                    String UidAsl= json.getString("AssortimentUid");
                    String count = json.getString("Count");

                    String Price  = json.getString("Price");
                    Price=Price.replace(",",".");
                    Double priceDouble = Double.valueOf(Price);
                    Price = String.format("%.2f",priceDouble);
                    Price=Price.replace(".",",");

                    String UnitPrice  = json.getString("UnitPrice");
                    String UnitInPackage  = json.getString("UnitInPackage");
                    String Code  = json.getString("Code");
                    String Barcode  = json.getString("Barcode");
                    String Unit  = json.getString("Unit");

                    sendDocument.put("AssortimentName",NameAsl);
                    sendDocument.put("AssortimentID",UidAsl);
                    sendDocument.put("Quantity",count);
                    sendDocument.put("WarehouseID",WareUid);
                    sendDocument.put("WarehouseName",WareName);

                    document_etichete.put("AssortimentID",UidAsl);
                    document_etichete.put("Count",count);
                    document_etichete.put("Name",NameAsl);
                    document_etichete.put("Code",Code);
                    document_etichete.put("Barcode",Barcode);
                    document_etichete.put("Price",Price);
                    document_etichete.put("Unit",Unit);
                    document_etichete.put("UnitPrice",UnitPrice);
                    document_etichete.put("UnitInPackage",UnitInPackage);

                    boolean isExtist = false;
                    if (mArrayAssortment.length()!=0) {
                        for (int i = 0; i < mArrayAssortment.length(); i++) {
                            JSONObject object = mArrayAssortment.getJSONObject(i);
                            JSONObject jsonObject = mArrayForEtichete.getJSONObject(i);
                            String AssortimentUid = object.getString("AssortimentID");
                            String CountExist = object.getString("Quantity");
                            String CountExist_etichete = jsonObject.getString("Count");
                            String AssortimentUit_etichete = jsonObject.getString("AssortimentID");

                            if (AssortimentUid.contains(UidAsl) && AssortimentUit_etichete.contains(UidAsl)) {
                                Integer countInt = Integer.valueOf(CountExist);
                                int CountEtichete_exist =Integer.valueOf(CountExist_etichete);
                                Integer CountAdd = Integer.valueOf(count);
                                Integer CountNew = CountAdd + countInt;
                                int CountNew_etichete = CountAdd  +CountEtichete_exist;
                                String countStr = String.valueOf(CountNew);
                                String coutStr_etichete = String.valueOf(CountNew_etichete);

                                object.put("Quantity", countStr);
                                jsonObject.put("Count", coutStr_etichete);
                                isExtist=true;
                            }
                        }
                        if (!isExtist){
                            mArrayAssortment.put(sendDocument);
                            mArrayForEtichete.put(document_etichete);
                        }
                    }else{
                        mArrayAssortment.put(sendDocument);
                        mArrayForEtichete.put(document_etichete);
                    }
                    asl_list.clear();
                    showAssortment();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                }

            }
        }
        else if(requestCode==REQUEST_FROM_LIST_ASSORTMENT) {
            if (resultCode == RESULT_CANCELED) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            } 
            else if (resultCode == RESULT_OK) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();
                String response = data.getStringExtra("AssortmentStockAddedArray");
                try {
                    JSONArray array_from_touch = new JSONArray(response);

                    if (mArrayAssortment.length() != 0) {
                        boolean isExtist = false;
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            String AssortimentUid_from_touch = object_from_touch.getString("AssortimentUid");
                            String count = object_from_touch.getString("Count");
                            String NameAsl= object_from_touch.getString("AssortimentName");

                            String Price  = object_from_touch.getString("Price");
                            Price=Price.replace(",",".");

                            Double priceDouble = Double.valueOf(Price);
                            Price = String.format("%.2f",priceDouble);

                            String UnitPrice  = object_from_touch.getString("UnitPrice");
                            String UnitInPackage  = object_from_touch.getString("UnitInPackage");
                            String Code  = object_from_touch.getString("Code");
                            String Barcode  = object_from_touch.getString("Barcode");
                            String Unit  = object_from_touch.getString("Unit");


                            sendDocument=new JSONObject();
                            document_etichete=new JSONObject();

                            sendDocument.put("AssortimentName",NameAsl);
                            sendDocument.put("AssortimentID",AssortimentUid_from_touch);
                            sendDocument.put("Quantity",count);
                            sendDocument.put("WarehouseID",WareUid);
                            sendDocument.put("WarehouseName",WareName);

                            document_etichete.put("AssortimentID",AssortimentUid_from_touch);
                            document_etichete.put("Count",count);
                            document_etichete.put("Name",NameAsl);
                            document_etichete.put("Code",Code);
                            document_etichete.put("Barcode",Barcode);
                            document_etichete.put("Price",Price);
                            document_etichete.put("Unit",Unit);
                            document_etichete.put("UnitPrice",UnitPrice);
                            document_etichete.put("UnitInPackage",UnitInPackage);

                            for (int k = 0; k < mArrayAssortment.length(); k++) {
                                JSONObject object = mArrayAssortment.getJSONObject(k);
                                JSONObject jsonObject = mArrayForEtichete.getJSONObject(i);
                                String AssortimentUid = object.getString("AssortimentID");
                                String CountExist = object.getString("Quantity");
                                String CountExist_etichete = jsonObject.getString("Count");
                                String AssortimentUit_etichete = jsonObject.getString("AssortimentID");

                                if (AssortimentUid.contains(AssortimentUid_from_touch)  && AssortimentUit_etichete.contains(AssortimentUid_from_touch)) {
                                    Integer countInt = Integer.valueOf(CountExist);
                                    int CountEtichete_exist =Integer.valueOf(CountExist_etichete);
                                    Integer CountAdd = Integer.valueOf(count);
                                    int CountNew = CountAdd + countInt;
                                    int CountNew_etichete = CountAdd  +CountEtichete_exist;

                                    String countStr = String.valueOf(CountNew);
                                    String coutStr_etichete = String.valueOf(CountNew_etichete);

                                    object.put("Quantity", countStr);
                                    jsonObject.put("Count", coutStr_etichete);
                                    isExtist = true;
                                }
                            }
                            if (!isExtist) {
                                mArrayAssortment.put(sendDocument);
                                mArrayForEtichete.put(document_etichete);
                            }
                        }
                    } else {
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            String NameAsl= object_from_touch.getString("AssortimentName");
                            String UidAsl= object_from_touch.getString("AssortimentUid");
                            String Count = object_from_touch.getString("Count");

                            String Price  = object_from_touch.getString("Price");
                            Price=Price.replace(",",".");
                            Double priceDouble = Double.valueOf(Price);
                            Price = String.format("%.2f",priceDouble);


                            String UnitPrice  = object_from_touch.getString("UnitPrice");
                            String UnitInPackage  = object_from_touch.getString("UnitInPackage");
                            String Code  = object_from_touch.getString("Code");
                            String Barcode  = object_from_touch.getString("Barcode");
                            String Unit  = object_from_touch.getString("Unit");

                            sendDocument=new JSONObject();
                            document_etichete=new JSONObject();

                            sendDocument.put("AssortimentName",NameAsl);
                            sendDocument.put("AssortimentID",UidAsl);
                            sendDocument.put("Quantity",Count);
                            sendDocument.put("WarehouseID",WareUid);
                            sendDocument.put("WarehouseName",WareName);

                            document_etichete.put("Name",NameAsl);
                            document_etichete.put("AssortimentID",UidAsl);
                            document_etichete.put("Code",Code);
                            document_etichete.put("Barcode",Barcode);
                            document_etichete.put("Price",Price);
                            document_etichete.put("Unit",Unit);
                            document_etichete.put("UnitPrice",UnitPrice);
                            document_etichete.put("UnitInPackage",UnitInPackage);
                            document_etichete.put("Count",Count);

                            mArrayAssortment.put(sendDocument);
                            mArrayForEtichete.put(document_etichete);
                        }
                    }
                    asl_list.clear();
                    showAssortment();

                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),StockAssortment.this);
                }
            }
        }
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
    protected void onResume() {
        super.onResume();
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        String WorkPlaceID = WorkPlace.getString("Uid","0");
        String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if(!WorkPlaceID.equals(WareUid)){
            btn_change_stock.setText(WareName);
        }
        if(WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("")){
            btn_change_stock.setText(WareName);
        }
    }

}
