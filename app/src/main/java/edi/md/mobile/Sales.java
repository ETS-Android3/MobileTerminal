package edi.md.mobile;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
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

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
import com.zebra.sdk.util.internal.Sleeper;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import edi.md.mobile.Settings.Assortment;
import edi.md.mobile.Utils.AssortmentInActivity;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetAssortiment;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetPrinters;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.PrintInvoice;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_PrintInvoice;
import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveInvoice;

public class Sales extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ImageButton btn_write_barcode,btn_delete,btn_open_asl_list;
    TextView txt_input_barcode,txt_total_sales,txtBarcode_introdus;
    ProgressBar pgBar;
    Button btn_print,btn_ok,btn_cancel,btn_change_stock,btn_add_Client;
    ListView list_of_sales;

    AlertDialog.Builder builderType,builderTypePrinters;
    SimpleAdapter simpleAdapterASL;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> printers_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();

    String ip_,port_,UserId,WareUid,InvoiceID,PrinterID,ClientID,uid_selected,barcode_introdus,WareName,InvoiceCode,WorkPlaceID;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;
    Boolean invoiceSaved=false;
    boolean createNewInvoice = false;

    Menu menu;
    JSONObject sendInvoice,sendAssortiment;
    JSONArray mAssortmentArray;

    int limit_sales, REQUEST_FROM_COUNT_ACTIVITY=110,REQUEST_FROM_LIST_ASSORTMENT = 210,REQUEST_FROM_GET_CLIENT = 30;
    final boolean[] show_keyboard = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle(getResources().getString(R.string.btn_sales_main_activity));

        setContentView(R.layout.activity_sales);
        Toolbar toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);

        btn_write_barcode=findViewById(R.id.btn_write_manual_sales);
        txt_input_barcode=findViewById(R.id.txt_input_barcodes_sales);
        btn_delete=findViewById(R.id.btn_delete_sales);
        btn_open_asl_list=findViewById(R.id.btn_touch_open_asl_sales);
        txt_total_sales=findViewById(R.id.txtTotal_sales);
        pgBar=findViewById(R.id.progressBar_sales);
        txtBarcode_introdus=findViewById(R.id.txtBarcode_introdus_sales);
        btn_add_Client=findViewById(R.id.btn_add_client_sales);
        btn_ok=findViewById(R.id.btn_ok_sales);
        btn_print=findViewById(R.id.btn_print_sales);
        btn_cancel=findViewById(R.id.btn_cancel_sales);
        btn_change_stock=findViewById(R.id.btn_change_stock_sales);
        list_of_sales=findViewById(R.id.LL_list_sales);
        pgH=new ProgressDialog(Sales.this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_sales);
        NavigationView navigationView = findViewById(R.id.nav_view_sales);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        mAssortmentArray =new JSONArray();

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        WorkPlaceID = WorkPlace.getString("Uid","0");
        final String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if (WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("")){
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

        String limit = Settings.getString("LimitSales","0");
        limit_sales = Integer.valueOf(limit);
        if(limit_sales==0){
            limit_sales=1000;
        }

        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,2000,3000);

        btn_print.setEnabled(false);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createNewInvoice){
                    btn_cancel.setText(Sales.this.getResources().getString(R.string.txt_renunt_all));
                    btn_add_Client.setEnabled(true);
                    btn_print.setEnabled(false);
                    invoiceSaved = false;

                    mAssortmentArray =new JSONArray();
                    setTitle(getResources().getString(R.string.btn_sales_main_activity));
                    asl_list.clear();
                    showAssortmentList();
                }
                else{
                    exitDialog();
                }

            }
        });
        btn_write_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!invoiceSaved) {
                    if (!show_keyboard[0]) {
                        show_keyboard[0] = true;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(txt_input_barcode, InputMethodManager.SHOW_IMPLICIT);
                        txt_input_barcode.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else {
                        show_keyboard[0] = false;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(txt_input_barcode.getWindowToken(), 0);
                    }
                }
                else{
                    Toast.makeText(Sales.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });
        txt_input_barcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) getAssortmentSales();
                else if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER)getAssortmentSales();
                return false;
            }
        });
        list_of_sales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                uid_selected=(String)asl_list.get(position).get("Uid");
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!invoiceSaved){
                    if(uid_selected!=null){
                        try {
                            for (int i = 0; i< mAssortmentArray.length(); i++) {
                                JSONObject json = mAssortmentArray.getJSONObject(i);
                                String Uid = json.getString("AssortimentUid");
                                if (uid_selected.contains(Uid)) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        mAssortmentArray.remove(i);
                                    }
                                }
                                asl_list.clear();
                                showAssortmentList();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                        }
                    }
                }
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!invoiceSaved) {
                    pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                    pgH.setIndeterminate(true);
                    pgH.setCancelable(false);
                    pgH.show();

                    JSONArray sendArr = new JSONArray();
                    for (int i = 0; i < mAssortmentArray.length(); i++) {
                        JSONObject json = null;
                        try {
                            json = mAssortmentArray.getJSONObject(i);
                            String Uid = json.getString("AssortimentUid");
                            String Cant = json.getString("Count");
                            String Price = json.getString("Price");
                            String WhareHouse = json.getString("Warehouse");

                            Price= Price.replace(",",".");

                            JSONObject sendObj = new JSONObject();
                            sendObj.put("Assortiment", Uid);
                            sendObj.put("Quantity", Cant);
                            sendObj.put("SalePrice", Price);
                            sendObj.put("Warehouse", WhareHouse);
                            sendArr.put(sendObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                        }
                    }
                    sendInvoice = new JSONObject();
                    try {
                        sendInvoice.put("ClientID", ClientID);
                        sendInvoice.put("Lines", sendArr);
                        sendInvoice.put("UserID", UserId);
                        sendInvoice.put("TerminalCode", "testIgor");
                        sendInvoice.put("Warehouse", WorkPlaceID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                    }
                    URL generateSave = SaveInvoice(ip_, port_);
                    new AsyncTask_SaveInvoice().execute(generateSave);
                }
                else{
                    finish();
                }
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    printSales();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_change_stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!invoiceSaved) {
                    stock_List_array.clear();
                    getWareHouseChange();
                }else{
                    Toast.makeText(Sales.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_add_Client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Logins = new Intent(".GetClientMobile");
                startActivityForResult(Logins, REQUEST_FROM_GET_CLIENT);
            }
        });
        btn_open_asl_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!invoiceSaved) {
                    Intent AddingASL = new Intent(".AssortmentMobile");
                    AddingASL.putExtra("ActivityCount", 181);
                    if(WorkPlaceID.equals("") || WorkPlaceID.equals("0")){
                       AddingASL.putExtra("WareID",WareUid);
                    }
                    startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
                }
                else{
                    Toast.makeText(Sales.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_sales);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitDialog();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        getMenuInflater().inflate(R.menu.menu_sales, menus);
        this.menu = menus;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_close_sales) {
            exitDialog();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_sales);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_COUNT_ACTIVITY){
            if (resultCode == RESULT_CANCELED){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            }
            else if (resultCode==RESULT_OK){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();

                String response = data.getStringExtra("AssortmentSalesAdded");
                try {
                    JSONObject json= new JSONObject(response);
                    String UidAsl= json.getString("AssortimentUid");
                    String count = json.getString("Count");
                    boolean isExtist = false;
                    if (mAssortmentArray.length()!=0) {
                        for (int i = 0; i < mAssortmentArray.length(); i++) {
                            JSONObject object = mAssortmentArray.getJSONObject(i);
                            String AssortimentUid = object.getString("AssortimentUid");
                            String CountExist = object.getString("Count");
                            Double countInt = Double.valueOf(CountExist);
                            Double CountAdd = Double.valueOf(count);

                            if (AssortimentUid.contains(UidAsl)) {
                                Double CountNew = CountAdd + countInt;
                                String countStr = String.valueOf(CountNew);
                                object.put("Count", countStr);
                                isExtist=true;
                                break;
                            }
                        }
                        if (!isExtist){
                            if (mAssortmentArray.length()<limit_sales)
                                mAssortmentArray.put(json);
                            else
                                Toast.makeText(Sales.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if (mAssortmentArray.length()<limit_sales)
                            mAssortmentArray.put(json);
                        else
                            Toast.makeText(Sales.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                    }
                    asl_list.clear();
                    showAssortmentList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                }
            }
        }
        else if (requestCode == REQUEST_FROM_GET_CLIENT){
            if (resultCode==RESULT_CANCELED){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            }
            else if (resultCode==RESULT_OK){
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                ClientID= data.getStringExtra("ClientID");
                btn_add_Client.setText(data.getStringExtra("ClientName"));
            }
        }
        else if(requestCode == REQUEST_FROM_LIST_ASSORTMENT) {
            if (resultCode == RESULT_CANCELED) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            }
            else if (resultCode == RESULT_OK) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();
                String response = data.getStringExtra("AssortmentSalesAddedArray");
                try {
                    JSONArray array_from_touch = new JSONArray(response);
                    if (mAssortmentArray.length() != 0) {
                        boolean isExtist = false;
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            String AssortimentUid_from_touch = object_from_touch.getString("AssortimentUid");
                            String count = object_from_touch.getString("Count");
                            for (int k = 0; k < mAssortmentArray.length(); k++) {
                                JSONObject object = mAssortmentArray.getJSONObject(k);
                                String AssortimentUid = object.getString("AssortimentUid");
                                String CountExist = object.getString("Count");
                                Double countInt = Double.valueOf(CountExist);
                                Double CountAdd = Double.valueOf(count);

                                if (AssortimentUid.contains(AssortimentUid_from_touch)) {
                                    Double CountNew = CountAdd + countInt;
                                    String countStr = String.valueOf(CountNew);
                                    object.put("Count", countStr);
                                    isExtist = true;
                                    break;
                                }
                            }
                            if (!isExtist) {
                                if (mAssortmentArray.length()<limit_sales)
                                    mAssortmentArray.put(object_from_touch);
                                else
                                    Toast.makeText(Sales.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            if (mAssortmentArray.length()<limit_sales)
                                mAssortmentArray.put(object_from_touch);
                            else
                                Toast.makeText(Sales.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                        }
                    }
                    asl_list.clear();
                    showAssortmentList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                }

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
    private void exitDialog(){
        if(invoiceSaved){
            finish();
        }else{
            if(mAssortmentArray.length()==0){
                finish();
            }
            else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
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

    }
    private void getAssortmentSales(){
        txt_input_barcode.requestFocus();
        if (!txt_input_barcode.getText().toString().equals("")) {
            if (!invoiceSaved) {
                txt_input_barcode.requestFocus();
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
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                }
                txtBarcode_introdus.setText(txt_input_barcode.getText().toString());
                barcode_introdus = txt_input_barcode.getText().toString();
                txt_input_barcode.setText("");
                URL getASL = GetAssortiment(ip_, port_);
                new AsyncTask_GetAssortiment().execute(getASL);
            } else {
                txt_input_barcode.setText("");
                Toast.makeText(Sales.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
            }
        }else{
            txt_input_barcode.setText("");
            Toast.makeText(Sales.this,getResources().getString(R.string.msg_barcode_empty), Toast.LENGTH_SHORT).show();
        }
    }
    public void show_WareHouse(){

        SimpleAdapter simpleAdapterType = new SimpleAdapter(Sales.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Sales.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                finish();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String WareGUid  = String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareNames = String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode  = String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareNames);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                btn_change_stock.setText(WareNames);
                WareUid = WareGUid;
                WorkPlaceID = WareGUid;
                WareName = WareNames;
                stock_List_array.clear();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_WareHouseChange(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Sales.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Sales.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
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
        if(Sales.this != null)
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
    public void show_printers(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Sales.this, printers_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderTypePrinters = new AlertDialog.Builder(Sales.this);
        builderTypePrinters.setTitle(getResources().getString(R.string.txt_header_msg_sales_printers));
        builderTypePrinters.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                printers_List_array.clear();
                dialogInterface.dismiss();
            }
        });
        builderTypePrinters.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String ID= String.valueOf(printers_List_array.get(wich).get("ID"));
                URL getPrintInvoice = PrintInvoice(ip_,port_,InvoiceID,ID);
                new AsyncTask_PrintInvoice().execute(getPrintInvoice);
            }
        });
        builderTypePrinters.setCancelable(false);
        if(isDestroyed())
            return;
        else
            builderTypePrinters.show();
    }
    public void showAssortmentList(){
        double sumTotal=0.0;
        try {
            for (int i = 0; i< mAssortmentArray.length(); i++){
                JSONObject json= mAssortmentArray.getJSONObject(i);
                HashMap<String, Object> asl_ = new HashMap<>();

                String Name = json.getString("AssortimentName");
                String Cant = json.getString("Count");
                String WareName = json.getString("WareName");

                Cant=Cant.replace(",",".");
                double count_to_double = Double.parseDouble(Cant);
                String count_to_string =String.format("%.3f",count_to_double);
                String Price = json.getString("Price");
                Price = Price.replace(",",".");
                String Uid = json.getString("AssortimentUid");

                asl_.put("Name",Name);
                asl_.put("Cant",count_to_string);
                asl_.put("Price",Price);
                asl_.put("Uid",Uid);
                asl_.put("Ware",WareName);

                String suma =String.format("%.2f",Double.valueOf(Price)* count_to_double );
                sumTotal=sumTotal+Double.valueOf(Price)* Double.valueOf(Cant);
                asl_.put("Suma",suma.replace(",","."));
                asl_list.add(asl_);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
        }
        txt_total_sales.setText(String.format("%.2f",sumTotal));
        simpleAdapterASL = new SimpleAdapter(this, asl_list,R.layout.show_asl_sales, new String[]{"Name","Cant","Suma","Ware"},
                new int[]{R.id.textName,R.id.textCantitate,R.id.textSuma,R.id.textNameWare});
        simpleAdapterASL.notifyDataSetChanged();
        list_of_sales.setAdapter(simpleAdapterASL);
    }
    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                Sales.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        URL generatedURL = Ping(ip_, port_);
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

    }
    public String getResponseSaveInvoice(URL send_bills) {
        String data = "";
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(5000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());
            wr.writeBytes(String.valueOf(sendInvoice));
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
            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data;
    }
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                show_keyboard[0] = false;
                Activity activity=Sales.this;
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
            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data.toString();
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
                        String Remain = responseAssortiment.getString("Remain");
                        String Marking = responseAssortiment.getString("Marking");
                        String Codes = responseAssortiment.getString("Code");
                        String Uid = responseAssortiment.getString("AssortimentID");
                        String Barcodes = responseAssortiment.getString("BarCode");
                        boolean allowInteger = responseAssortiment.getBoolean("AllowNonIntegerSale");

                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        Assortment assortment = new Assortment();
                        assortment.setBarCode(Barcodes);
                        assortment.setCode(Codes);
                        assortment.setName(Names);
                        assortment.setPrice(Price);
                        assortment.setMarking(Marking);
                        assortment.setRemain(Remain);
                        assortment.setAssortimentID(Uid);
                        assortment.setAllowNonIntegerSale(String.valueOf(allowInteger));
                        final AssortmentInActivity assortmentParcelable = new AssortmentInActivity(assortment);

                        Intent sales = new Intent(".CountSalesMobile");
                        sales.putExtra("WhareHouse",WareUid);
                        sales.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
                        sales.putExtra("WhareNames",WareName);

                        startActivityForResult(sales, REQUEST_FROM_COUNT_ACTIVITY);
                    } else {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtBarcode_introdus.setText(barcode_introdus + " - " + getResources().getString(R.string.txt_depozit_nedeterminat));
                        txt_input_barcode.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                    Toast.makeText(Sales.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(Sales.this,"Eroare: Nu este raspuns de la serviciu!", Toast.LENGTH_SHORT).show();
                ((Variables)getApplication()).appendLog(response,Sales.this);
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                txtBarcode_introdus.setText(barcode_introdus + " - " + getResources().getString(R.string.txt_depozit_nedeterminat));
                txt_input_barcode.requestFocus();
            }
        }
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
                menu.getItem(0).setIcon(ContextCompat.getDrawable(Sales.this, R.drawable.signal_wi_fi_48));
            }else {
                this.onCancelled();
                this.cancel(true);
                menu.getItem(0).setIcon(ContextCompat.getDrawable(Sales.this, R.drawable.no_signal_wi_fi_48));
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
                ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
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
                            ((Variables) getApplication()).appendLog(e.getMessage(), Sales.this);
                            Toast.makeText(Sales.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Sales.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Sales.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Sales.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
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
                ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
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
                            Toast.makeText(Sales.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                            ((Variables) getApplication()).appendLog(e.getMessage(), Sales.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Sales.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Sales.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Sales.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_PrintInvoice extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_PrintInvoice(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (!response.equals("false")){
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        Toast.makeText(Sales.this, getResources().getString(R.string.msg_imprimare_sales), Toast.LENGTH_SHORT).show();
                    } else {
                        AlertDialog.Builder input = new AlertDialog.Builder(Sales.this);
                        input.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        input.setCancelable(false);
                        input.setMessage(getResources().getString(R.string.msg_error_code) + ErrorCode);
                        input.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = input.create();
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                }
            }else{
                AlertDialog.Builder input = new AlertDialog.Builder(Sales.this);
                input.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                input.setCancelable(false);
                input.setMessage(getResources().getString(R.string.msg_nu_raspuns_server));
                input.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = input.create();
                dialog.show();
            }
        }
    }
    class AsyncTask_GetPrinters extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("false")) {
                try {
                    JSONObject responsePrinters = new JSONObject(response);
                    Integer ErrorCode = responsePrinters.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            JSONArray ListPrinter = responsePrinters.getJSONArray("Printers");
                            if (ListPrinter.length() > 1) {
                                for (int i = 0; i < ListPrinter.length(); i++) {
                                    JSONObject object = ListPrinter.getJSONObject(i);
                                    String PrCode = object.getString("Code");
                                    String PrName = object.getString("Name");
                                    PrinterID = object.getString("ID");
                                    HashMap<String, Object> Printers = new HashMap<>();
                                    Printers.put("Name", PrName);
                                    Printers.put("Code", PrCode);
                                    Printers.put("ID", PrinterID);
                                    printers_List_array.add(Printers);
                                }
                                show_printers();
                            } else if (ListPrinter.length() == 1) {
                                JSONObject one_pr = ListPrinter.getJSONObject(0);
                                PrinterID = one_pr.getString("ID");
                                URL getPrintInvoice = PrintInvoice(ip_, port_, InvoiceID, PrinterID);
                                new AsyncTask_PrintInvoice().execute(getPrintInvoice);
                            }else{
                                AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
                                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                                dialog.setCancelable(false);
                                dialog.setMessage(getResources().getString(R.string.txt_not_printers_check_back_office));
                                dialog.setPositiveButton(getResources().getString(R.string.txt_save_document), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Sales.this);
                }
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                dialog.setCancelable(false);
                dialog.setMessage(getResources().getString(R.string.txt_not_printers_check_back_office));
                dialog.setPositiveButton(getResources().getString(R.string.txt_save_document), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                if (Sales.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                  dialog.show();
            }

        }
    }
    class AsyncTask_SaveInvoice extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            return getResponseSaveInvoice(urls[0]);
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("")) {
                try {
                    JSONObject respo = new JSONObject(response);
                    int errCode = respo.getInt("ErrorCode");
                    if (errCode == 0) {

                        pgH.dismiss();
                        invoiceSaved = true;
                        InvoiceCode = respo.getString("InvoiceCode");
                        InvoiceID = respo.getString("InvoiceID");
                        setTitle(getResources().getString(R.string.btn_sales_main_activity) + ": " + InvoiceCode);
                        btn_print.setEnabled(true);
                        createNewInvoice = true;
                        btn_cancel.setText("New");
                        btn_add_Client.setEnabled(false);

                        Toast.makeText(Sales.this, getResources().getString(R.string.txt_sales_invoice_saved) + InvoiceCode, Toast.LENGTH_SHORT).show();
                        try {
                            printSales();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        final AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
//                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
//                        dialog.setCancelable(false);
//                        dialog.setMessage(getResources().getString(R.string.txt_sales_invoice_saved) + InvoiceCode);
//                        dialog.setPositiveButton(getResources().getString(R.string.txt_save_document), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//
//                            }
//                        });
//                        dialog.show();
                    } else {
                        pgH.dismiss();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        dialog.setCancelable(false);
                        dialog.setMessage(getResources().getString(R.string.msg_document_notsaved_cod_error) + errCode);
                        dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Sales.this);
                }
            }else{
                pgH.dismiss();
                AlertDialog.Builder dialog = new AlertDialog.Builder(Sales.this);
                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                dialog.setCancelable(false);
                dialog.setMessage(getResources().getString(R.string.msg_document_not_saved_nu_raspuns_serviciu));
                dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

        }
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
        if(WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("") ){
            btn_change_stock.setText(WareName);
        }

    }
    private void printSales() throws JSONException {
        final SharedPreferences SharedPrinters = getSharedPreferences("Printers", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        int positionType = SharedPrinters.getInt("Type",0);
        String UserName = User.getString("Name","NoN");
        Date current = new Date();
        String date = current.toString();

        if(positionType == 1){
            if(BluetoothPrintDriver.IsNoConnection()){
                Toast.makeText(Sales.this,getResources().getString(R.string.information_print_sales),Toast.LENGTH_SHORT).show();
            }
            BluetoothPrintDriver.Begin();
            BluetoothPrintDriver.BT_Write("\r");
            BluetoothPrintDriver.BT_Write("Contul spre plata NR: " + InvoiceCode);
            BluetoothPrintDriver.BT_Write("\r");
            BluetoothPrintDriver.BT_Write("Creat de " +UserName );
            BluetoothPrintDriver.BT_Write("\r");
            BluetoothPrintDriver.BT_Write("Data crearii" +date );
            BluetoothPrintDriver.BT_Write("\r");
            BluetoothPrintDriver.AddCodePrint(BluetoothPrintDriver.CODE39, InvoiceCode);
            BluetoothPrintDriver.BT_Write("\r");
            for (int i = 0; i< mAssortmentArray.length(); i++){
                JSONObject json= mAssortmentArray.getJSONObject(i);
                String Name = json.getString("AssortimentName");
                String Cant = json.getString("Count");
                String Price = json.getString("Price");

                Cant=Cant.replace(",",".");
                Price = Price.replace(",",".");

                String suma =String.format("%.2f",Double.valueOf(Price) * Double.parseDouble(Cant));
                String mToPrint = Name + " " + Cant + " " + suma;

                BluetoothPrintDriver.BT_Write("\r");
                BluetoothPrintDriver.BT_Write(mToPrint);
                BluetoothPrintDriver.BT_Write("\r");
            }

        }
        else {
            printers_List_array.clear();
            URL getWareHouse = GetPrinters(ip_,port_,WorkPlaceID);
            new AsyncTask_GetPrinters().execute(getWareHouse);
        }
    }
}
