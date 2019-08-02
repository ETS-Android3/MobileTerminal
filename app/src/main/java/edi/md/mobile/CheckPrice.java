package edi.md.mobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetAssortiment;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import edi.md.mobile.Utils.AssortmentInActivity;

public class CheckPrice extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    Button btn_depozit;
    ImageButton print, open_assortment_list, write_barcode,addCount,deleteCount;
    TextView txtInput_barcode, txtNameAsortment, txtBarcodeAssortment,txtCodeAssortment,txtMarkingAssortment,txtStockAssortment,txtPriceAssortment;
    Switch show_stock;
    ProgressBar pgBar;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;
    EditText count_lable;
    private Handler mHandler;
    JSONObject sendAssortiment;
    AlertDialog.Builder builderType;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();

    String ip_,port_,UserId,Eticheta,WareUid, mNameAssortment = null, mPriceAssortment,Remain,
            mMarkingAssortment, mCodeAssortment, mUnitAssortment, mBarcodeAssortment, mUnitPrice, mUnitInPackage,WareNames;
    
    int REQUEST_FROM_LIST_ASSORTMENT = 666;
    Menu menu;
    Boolean onedate =false;
//add helou in cetfhjkhdfzhkgvhk.mfdsaSdgvjmnkbhcgfxzcvbnhjhytestydgcvhbjlk;k'piouiy

    //sdfghjkfdgg
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_check_price);
        Toolbar toolbar = findViewById(R.id.toolbar_check_price);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_checkprice);
        NavigationView navigationView = findViewById(R.id.nav_view_check_price);
        btn_depozit=findViewById(R.id.btn_select_warehouse_check_price);
        print = findViewById(R.id.btn_print_etichete_check_price);
        open_assortment_list = findViewById(R.id.btn_touch_open_asl_check_price);
        write_barcode = findViewById(R.id.btn_add_manual_check_price);
        addCount = findViewById(R.id.img_btn_add_count_check_price);
        deleteCount = findViewById(R.id.img_btn_delete_count_check_price);
        txtInput_barcode = findViewById(R.id.txt_input_barcode_check_price);
        txtNameAsortment = findViewById(R.id.txtName_assortment_check_price);
        txtBarcodeAssortment =findViewById(R.id.txtBarcode_assortment_checkPrice);
        txtCodeAssortment = findViewById(R.id.txtcode_assortment_check_price);
        txtMarkingAssortment = findViewById(R.id.txtMarking_asortment_check_price);
        txtStockAssortment = findViewById(R.id.txtStoc_asortment_check_price);
        txtPriceAssortment = findViewById(R.id.txtPrice_asortment_check_price);
        show_stock = findViewById(R.id.show_stock_check_price);
        pgBar = findViewById(R.id.progressBar_check_price);
        count_lable = findViewById(R.id.count_etichete_check_price);
        pgH=new ProgressDialog(CheckPrice.this);

        if(!show_stock.isChecked()){
            txtStockAssortment.setVisibility(View.INVISIBLE);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        boolean ShowCode = WorkPlace.getBoolean("ShowCode",false);
        if (!ShowCode){
            txtCodeAssortment.setVisibility(View.INVISIBLE);
        }
        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        final String WorkPlaceID = WorkPlace.getString("Uid","0");
        String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if (WorkPlaceName.equals("Nedeterminat")){
            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();
            btn_depozit.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
            getWareHouse();
        }
        else{
            btn_depozit.setText(WorkPlaceName);
            WareUid = WorkPlaceID;
        }

        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == 10) {
                    if (msg.arg1 == 12)
                        pgH.dismiss();
                    Toast.makeText(CheckPrice.this, getResources().getString(R.string.msg_imprimare_sales), Toast.LENGTH_SHORT).show();
                }
                else if(msg.what == 20) {
                    pgH.dismiss();
                    Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_errore_label_printer), Toast.LENGTH_SHORT).show();
                }
                else if (msg.what==201){
                    pgH.dismiss();
                    AlertDialog.Builder input = new AlertDialog.Builder(CheckPrice.this);
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
        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,100,2000);
        txtInput_barcode.requestFocus();
        final boolean[] show_keyboard = {false};
        write_barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!show_keyboard[0]) {
                    show_keyboard[0] = true;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txtInput_barcode, InputMethodManager.SHOW_IMPLICIT);
                    txtInput_barcode.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else {
                    show_keyboard[0] = false;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtInput_barcode.getWindowToken(), 0);
                }
            }
        });


        show_stock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    txtStockAssortment.setVisibility(View.VISIBLE);
                }
                else{
                    txtStockAssortment.setVisibility(View.INVISIBLE);
                }
            }
        });

        btn_depozit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setCancelable(false);
                pgH.setIndeterminate(true);
                pgH.show();
                ChangeWareHouse();
            }
        });

        txtInput_barcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    pgBar.setVisibility(ProgressBar.VISIBLE);
                    show_keyboard[0] = false;
                    sendAssortiment = new JSONObject();
                    try {
                        sendAssortiment.put("AssortmentIdentifier", txtInput_barcode.getText().toString());
                        sendAssortiment.put("ShowStocks", show_stock.isChecked());
                        sendAssortiment.put("UserID", UserId);
                        sendAssortiment.put("WarehouseID", WareUid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                    }
                    URL getASL = GetAssortiment(ip_, port_);
                    new AsyncTask_GetAssortiment().execute(getASL);

                    txtBarcodeAssortment.setText(txtInput_barcode.getText().toString());
                    txtInput_barcode.setText("");
                }
                else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!onedate) {
                        pgBar.setVisibility(ProgressBar.VISIBLE);
                        sendAssortiment = new JSONObject();
                        try {
                            sendAssortiment.put("AssortmentIdentifier", txtInput_barcode.getText().toString());
                            sendAssortiment.put("ShowStocks", show_stock.isChecked());
                            sendAssortiment.put("UserID", UserId);
                            sendAssortiment.put("WarehouseID", WareUid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                        }
                        URL getASL = GetAssortiment(ip_, port_);
                        new AsyncTask_GetAssortiment().execute(getASL);

                        txtBarcodeAssortment.setText(txtInput_barcode.getText().toString());
                        txtInput_barcode.setText("");
                        onedate=true;
                    }
                }
                return false;
            }
        });
        open_assortment_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AddingASL = new Intent(".AssortmentMobile");
                AddingASL.putExtra("ActivityCount", 161);
                if(WorkPlaceID.equals("0")){
                    AddingASL.putExtra("WareID",WareUid);
                }
                startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sPref =getSharedPreferences("Conected printers", MODE_PRIVATE);
                SharedPreferences sPrefSetting =getSharedPreferences("Settings", MODE_PRIVATE);
                final String adresMAC = sPref.getString("AdressPrinters",null);
                final int number_etichete =Integer.valueOf(count_lable.getText().toString());
                Eticheta = sPrefSetting.getString("Lable",null);
                if(adresMAC!=null) {
                    if (mNameAssortment != null) {
                        pgH.setMessage(getResources().getString(R.string.msg_erroare_la_imrpimare));
                        pgH.setCancelable(false);
                        pgH.setIndeterminate(true);
                        pgH.show();
                        new Thread() {
                            public void run() {

                                Connection connection = new BluetoothConnection(adresMAC);
                                try {
                                    connection.open();
                                    String price_unit;
                                    if (mUnitInPackage ==null){
                                        price_unit = mUnitPrice + "/" + mUnitAssortment;
                                    }else{
                                        price_unit = mUnitPrice + "/" + mUnitInPackage;
                                    }
                                    //String api_key_yandex = "trnsl.1.1.20190401T113033Z.81c7241519eea2f0.a881247269077361cbd5b905400518b250efe00b";
                                    Date datess = new Date();
                                    SimpleDateFormat sdfChisinau = new SimpleDateFormat("yyyy.MM.dd");
                                    TimeZone tzInChisinau = TimeZone.getTimeZone("Europe/Chisinau");
                                    sdfChisinau.setTimeZone(tzInChisinau);
                                    String sDateInChisinau = sdfChisinau.format(datess); // Convert to String first
                                    String codes = "codes_imprim";
                                    String data_imprim = "data_imprim";
                                    String name_imprim = "name_imprim";
                                    String rus_name_imprim = "rus_imprim";
                                    String price_imprim = "price_imprim";
                                    String price_unit_imprim = "price_unit_imprim";
                                    String barcode_imprim = "barcode_imprim";

                                    if(Eticheta!=null){
                                        Eticheta = Eticheta.replace(codes, mCodeAssortment);
                                        Eticheta = Eticheta.replace(data_imprim,sDateInChisinau);
                                        Eticheta = Eticheta.replace(name_imprim, mNameAssortment);
                                        Eticheta = Eticheta.replace(rus_name_imprim,"");
                                        Eticheta = Eticheta.replace(price_imprim, mPriceAssortment);
                                        Eticheta = Eticheta.replace(price_unit_imprim,price_unit);
                                        Eticheta = Eticheta.replace(barcode_imprim, mBarcodeAssortment);
                                        if (number_etichete>1){
                                            for (int i=0;i<number_etichete;i++){
                                                connection.write(Eticheta.getBytes("UTF-8"));
                                            }
                                        }else{
                                            connection.write(Eticheta.getBytes());
                                        }

                                        mHandler.obtainMessage(10, 12, -1)
                                                .sendToTarget();
                                    }else{
                                        mHandler.obtainMessage(20, 12, -1)
                                                .sendToTarget();
                                    }
                                } catch (ConnectionException e) {
                                    mHandler.obtainMessage(201, 14, -1,e.toString())
                                            .sendToTarget();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                                }
                            }
                        }.start();
                    }
                }
                else{
                    Toast.makeText(CheckPrice.this,getResources().getString(R.string.txt_not_conected_printers), Toast.LENGTH_SHORT).show();
                }
            }
        });
        addCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDigitInteger(count_lable.getText().toString())) {
                    Integer curr = Integer.valueOf(count_lable.getText().toString());
                    curr += 1;
                    count_lable.setText(String.valueOf(curr));
                }
            }
        });
        deleteCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDigitInteger(count_lable.getText().toString())) {
                    Integer curr = Integer.valueOf(count_lable.getText().toString());
                    if (curr - 1 > 0) {
                        curr -= 1;
                    }
                    count_lable.setText(String.valueOf(curr));
                }
            }
        });
        count_lable.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isDigitInteger(count_lable.getText().toString())) {
                    count_lable.setError(getResources().getString(R.string.msg_only_number_integer));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_checkprice);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_price, menus);
        this.menu = menus;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_close) {
           finish();
        }

        return super.onOptionsItemSelected(item);
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
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_checkprice);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isDigitInteger(String s) throws NumberFormatException {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_LIST_ASSORTMENT){
            if(resultCode == RESULT_OK){
                if (data != null) {
                    AssortmentInActivity assortment = data.getParcelableExtra("AssortmentInActivity");

                    mNameAssortment = assortment.getName();
                    mPriceAssortment = assortment.getPrice();
                    mMarkingAssortment = assortment.getMarking();
                    mCodeAssortment = assortment.getCode();
                    mBarcodeAssortment = assortment.getBarCode();
                    mUnitAssortment = assortment.getUnit();
                    mUnitPrice = assortment.getUnitPrice();
                    if (mUnitPrice !=null) mUnitPrice = mUnitPrice.replace(".",",");
                    mUnitInPackage = assortment.getUnitInPackage();

                    txtNameAsortment.setText(mNameAssortment);
                    txtPriceAssortment.setText(mPriceAssortment);
                    txtMarkingAssortment.setText(mMarkingAssortment);
                    txtCodeAssortment.setText(mCodeAssortment);
                    txtBarcodeAssortment.setText(mBarcodeAssortment);
                    if (show_stock.isChecked()){
                        if(assortment.getRemain()!=null)
                            txtStockAssortment.setText(assortment.getRemain());
                        else
                            txtStockAssortment.setText("0");
                    }
                    txtInput_barcode.requestFocus();
                }else{

                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                txtInput_barcode.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        txtInput_barcode.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        txtInput_barcode.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        txtInput_barcode.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        txtInput_barcode.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        txtInput_barcode.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        txtInput_barcode.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        txtInput_barcode.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        txtInput_barcode.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        txtInput_barcode.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        txtInput_barcode.append("0");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = txtInput_barcode.getText().toString();
                        if(!txtInput_barcode.getText().toString().equals("")) {
                            txtInput_barcode.setText(test.substring(0, test.length() - 1));
                            txtInput_barcode.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void show_WareHouse(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(CheckPrice.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(CheckPrice.this);
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
                String WareName= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                btn_depozit.setText(WareName);
                WareUid = WareGUid;
                WareNames = WareName;
                stock_List_array.clear();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_WareHouseChange(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(CheckPrice.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(CheckPrice.this);
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
                String WareName= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                btn_depozit.setText(WareName);
                WareUid = WareGUid;
                WareNames = WareName;
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
    public void ChangeWareHouse(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouseChange().execute(getWareHouse);
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
            ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data;
    }

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                CheckPrice.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        URL generatedURL = Ping(ip_, port_);
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

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
                menu.getItem(0).setIcon(ContextCompat.getDrawable(CheckPrice.this, R.drawable.signal_wi_fi_48));
            }else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(CheckPrice.this, R.drawable.no_signal_wi_fi_48));
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
                ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("false")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    int ErrorCode = responseWareHouse.getInt("ErrorCode");
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
                            ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
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
                ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("false")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    int ErrorCode = responseWareHouse.getInt("ErrorCode");
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
                            ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
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
            onedate=false;
            if(!response.equals("")) {
                try {
                    JSONObject responseAssortiment = new JSONObject(response);
                    int ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        mNameAssortment = responseAssortiment.getString("Name");
                        mPriceAssortment = responseAssortiment.getString("Price");
                        Remain = responseAssortiment.getString("Remain");
                        mMarkingAssortment = responseAssortiment.getString("Marking");
                        mCodeAssortment = responseAssortiment.getString("Code");
                        mUnitAssortment = responseAssortiment.getString("Unit");
                        mUnitInPackage = responseAssortiment.getString("UnitInPackage");
                        mUnitPrice = responseAssortiment.getString("UnitPrice");
                        Double priceDouble = Double.valueOf(mPriceAssortment);
                        mPriceAssortment = String.format("%.2f", priceDouble);
                        mPriceAssortment = mPriceAssortment.replace(".", ",");
                        Double priceunit = Double.valueOf(mUnitPrice);
                        mUnitPrice = String.format("%.2f", priceunit);
                        mUnitPrice = mUnitPrice.replace(".", ",");
                        mBarcodeAssortment = responseAssortiment.getString("BarCode");

                        pgBar.setVisibility(ProgressBar.INVISIBLE);

                        txtPriceAssortment.setText(mPriceAssortment);
                        txtNameAsortment.setText(mNameAssortment);
                        if (!mMarkingAssortment.equals("null")) {
                            txtMarkingAssortment.setText(mMarkingAssortment);
                        } else {
                            txtMarkingAssortment.setText("-");
                        }
                        txtStockAssortment.setText(Remain);
                        txtCodeAssortment.setText(mCodeAssortment);
                        txtInput_barcode.requestFocus();
                    } else {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtNameAsortment.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                        txtPriceAssortment.setText("-");
                        txtStockAssortment.setText("-");
                        txtMarkingAssortment.setText("-");
                        txtCodeAssortment.setText("-");
                        txtInput_barcode.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),CheckPrice.this);
                }
            }else{
                Toast.makeText(CheckPrice.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
                ((Variables)getApplication()).appendLog(response,CheckPrice.this);
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                txtNameAsortment.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                txtPriceAssortment.setText("-");
                txtStockAssortment.setText("-");
                txtMarkingAssortment.setText("-");
                txtCodeAssortment.setText("-");
                txtInput_barcode.requestFocus();
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
            btn_depozit.setText(WorkPlaceName);
        }
        if(WorkPlaceName.equals("Nedeterminat")){
            btn_depozit.setText(WareNames);
        }
    }
}
