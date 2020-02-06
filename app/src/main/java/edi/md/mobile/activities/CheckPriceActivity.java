package edi.md.mobile.activities;

import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static edi.md.mobile.ListAssortment.AssortimentClickentSendIntent;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import edi.md.mobile.NetworkUtils.ApiRetrofit;
import edi.md.mobile.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import edi.md.mobile.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import edi.md.mobile.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import edi.md.mobile.NetworkUtils.RetrofitResults.WarehouseList;
import edi.md.mobile.NetworkUtils.Services.CommandService;
import edi.md.mobile.R;
import edi.md.mobile.Utils.AssortmentParcelable;
import edi.md.mobile.Variables;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckPriceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    Button btn_depozit;
    ImageButton print, open_assortment_list, write_barcode,addCount,deleteCount;
    TextView txtInput_barcode, txtNameAsortment, txtBarcodeAssortment,txtCodeAssortment,txtMarkingAssortment,txtStockAssortment,txtPriceAssortment;
    ProgressBar pgBar;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;
    EditText count_lable;
    AlertDialog.Builder builderType;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    String ip_,port_,UserId,Eticheta,WareUid, mNameAssortment = null, mPriceAssortment,
            mMarkingAssortment, mCodeAssortment, mUnitAssortment, mBarcodeAssortment, mUnitPrice, mUnitInPackage,WareNames;
    
    int REQUEST_FROM_LIST_ASSORTMENT = 666;
    Menu menu;
    final boolean[] show_keyboard = {false};

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
        pgBar = findViewById(R.id.progressBar_check_price);
        count_lable = findViewById(R.id.count_etichete_check_price);
        pgH=new ProgressDialog(CheckPriceActivity.this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        if (!Settings.getBoolean("ShowCode",false))
            txtCodeAssortment.setVisibility(View.INVISIBLE);

        UserId = User.getString("UserID","");
        ip_= Settings.getString("IP","");
        port_= Settings.getString("Port","");

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        final String WorkPlaceID = WorkPlace.getString("Uid","0");
        String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if (WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("")){
            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();
            btn_depozit.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
            changeOrSecelectWareHouse(false);
        }
        else{
            btn_depozit.setText(WorkPlaceName);
            WareUid = WorkPlaceID;
        }

        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,100,2000);
        txtInput_barcode.requestFocus();

        write_barcode.setOnClickListener(v -> {
            if (!show_keyboard[0]) {
                show_keyboard[0] = true;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(txtInput_barcode, InputMethodManager.SHOW_IMPLICIT);
                txtInput_barcode.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            else {
                show_keyboard[0] = false;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtInput_barcode.getWindowToken(), 0);
            }
        });

        btn_depozit.setOnClickListener(v -> {
            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();
            changeOrSecelectWareHouse(true);
        });

        txtInput_barcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) getAssortmentFromService();
            else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) getAssortmentFromService();

            return false;
        });
        open_assortment_list.setOnClickListener(v -> {
            Intent AddingASL = new Intent(".AssortmentMobile");
            AddingASL.putExtra("ActivityCount", 161);
            AddingASL.putExtra("WareID",WareUid);
            startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
        });
        print.setOnClickListener(v -> {
            SharedPreferences sPref =getSharedPreferences("Printers", MODE_PRIVATE);
            SharedPreferences sPrefSetting =getSharedPreferences("Settings", MODE_PRIVATE);
            final String adresMAC = sPref.getString("MAC_ZebraPrinters",null);
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
                                ((Variables)getApplication()).appendLog(e.getMessage(), CheckPriceActivity.this);
                            }
                        }
                    }.start();
                }
            }
            else{
                Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.txt_not_conected_printers), Toast.LENGTH_SHORT).show();
            }
        });
        addCount.setOnClickListener(v -> {
            if (isDigitInteger(count_lable.getText().toString())) {
                Integer curr = Integer.valueOf(count_lable.getText().toString());
                curr += 1;
                count_lable.setText(String.valueOf(curr));
            }
        });
        deleteCount.setOnClickListener(v -> {
            if (isDigitInteger(count_lable.getText().toString())) {
                Integer curr = Integer.valueOf(count_lable.getText().toString());
                if (curr - 1 > 0) {
                    curr -= 1;
                }
                count_lable.setText(String.valueOf(curr));
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
        getMenuInflater().inflate(R.menu.menu_check_price, menus);
        this.menu = menus;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
           finish();
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
        }else if(id == R.id.menu_help) {
//            Intent openHelpPage = new Intent(this,Help.class);
//            openHelpPage.putExtra("Page",901);
//            startActivity(openHelpPage);
        }
        else if (id == R.id.menu_exit) {
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
            ((Variables)getApplication()).appendLog(e.getMessage(), CheckPriceActivity.this);
            return false;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_LIST_ASSORTMENT){
            if(resultCode == RESULT_OK){
                if (data != null) {
                    AssortmentParcelable assortment = data.getParcelableExtra(AssortimentClickentSendIntent);
                    mNameAssortment = assortment.getName();
                    mPriceAssortment = assortment.getPrice();
                    mMarkingAssortment = assortment.getMarking();
                    mCodeAssortment = assortment.getCode();
                    mBarcodeAssortment = assortment.getBarCode();
                    mUnitAssortment = assortment.getUnit();
                    mUnitPrice = assortment.getUnitPrice();
                    mUnitInPackage = assortment.getUnitInPackage();

                    txtNameAsortment.setText(mNameAssortment);
                    double price = 0.00;
                    try{
                        price = Double.parseDouble(mPriceAssortment);
                    }catch (Exception e){
                        price = Double.parseDouble(mPriceAssortment.replace(",","."));
                    }
                    txtPriceAssortment.setText(String.valueOf(price));

                    if (mMarkingAssortment != null) {
                        txtMarkingAssortment.setText(mMarkingAssortment);
                    } else {
                        txtMarkingAssortment.setText("-");
                    }
                    txtCodeAssortment.setText(mCodeAssortment);
                    txtBarcodeAssortment.setText(mBarcodeAssortment);
                    if(assortment.getRemain()!=null)
                        txtStockAssortment.setText(assortment.getRemain());
                    else
                        txtStockAssortment.setText("0");
                    txtInput_barcode.requestFocus();
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
    @Override
    protected void onRestart() {
        super.onRestart();
        final SharedPreferences WorkPlace = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean ShowCode = WorkPlace.getBoolean("ShowCode", false);
        if (ShowCode) {
            txtCodeAssortment.setVisibility(View.VISIBLE);
        } else {
            txtCodeAssortment.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        String WorkPlaceID = WorkPlace.getString("Uid","0");
        String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if(!WorkPlaceID.equals(WareUid)){
            btn_depozit.setText(WareNames);
        }
        if(WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("")){
            btn_depozit.setText(WareNames);
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

    public void showWareHousesChange(boolean warehouseChanged){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(CheckPriceActivity.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(CheckPriceActivity.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));

        builderType.setNegativeButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                if(!warehouseChanged)
                    finish();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                btn_depozit.setText( String.valueOf(stock_List_array.get(wich).get("Name")));
                WareUid = String.valueOf(stock_List_array.get(wich).get("Uid"));
                WareNames =  String.valueOf(stock_List_array.get(wich).get("Name"));
                stock_List_array.clear();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        if(!CheckPriceActivity.this.isDestroyed())
            builderType.show();
    }

    public void changeOrSecelectWareHouse(boolean warehouseChanged){
        CommandService commandService = ApiRetrofit.getCommandService(CheckPriceActivity.this);
        Call<GetWarehousesListResult> call = commandService.getWareHousesList(UserId);

        call.enqueue(new Callback<GetWarehousesListResult>() {
            @Override
            public void onResponse(Call<GetWarehousesListResult> call, Response<GetWarehousesListResult> response) {
                GetWarehousesListResult warehousesListResult = response.body();
                if(warehousesListResult != null){
                    if(warehousesListResult.getErrorCode() == 0){
                        List<WarehouseList> warehouseList = warehousesListResult.getWarehouses();

                        for(WarehouseList warehouse : warehouseList){
                            HashMap<String, Object> WareHouse = new HashMap<>();
                            WareHouse.put("Name", warehouse.getName());
                            WareHouse.put("Code", warehouse.getCode());
                            WareHouse.put("Uid", warehouse.getWarehouseID());
                            stock_List_array.add(WareHouse);
                        }
                        showWareHousesChange(warehouseChanged);
                    }else{
                        pgH.dismiss();
                        Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.msg_error_code) + warehousesListResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetWarehousesListResult> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(CheckPriceActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                CheckPriceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommandService commandService = ApiRetrofit.getCommandService(CheckPriceActivity.this);
                        Call<Boolean> call = commandService.pingService();

                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if(response.body()){
                                    if(menu!=null)
                                        menu.getItem(0).setIcon(ContextCompat.getDrawable(CheckPriceActivity.this, R.drawable.signal_wi_fi_48));
                                }
                                else{
                                    if(menu!=null)
                                        menu.getItem(0).setIcon(ContextCompat.getDrawable(CheckPriceActivity.this, R.drawable.no_signal_wi_fi_48));
                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                if(menu!=null)
                                    menu.getItem(0).setIcon(ContextCompat.getDrawable(CheckPriceActivity.this, R.drawable.no_signal_wi_fi_48));
                            }
                        });
                    }
                });
            }
        };

    }

    private void getAssortmentFromService(){
        pgBar.setVisibility(ProgressBar.VISIBLE);
        show_keyboard[0] = false;

        GetAssortmentItemBody assortmentItemBody = new GetAssortmentItemBody();
        assortmentItemBody.setAssortmentIdentifier(txtInput_barcode.getText().toString());
        assortmentItemBody.setShowStocks(true);
        assortmentItemBody.setUserID(UserId);
        assortmentItemBody.setWarehouseID(WareUid);

        txtBarcodeAssortment.setText(txtInput_barcode.getText().toString());
        txtInput_barcode.setText("");

        CommandService commandService = ApiRetrofit.getCommandService(CheckPriceActivity.this);
        Call<GetAssortmentItemResult> call = commandService.getAssortmentItem(assortmentItemBody);

        call.enqueue(new Callback<GetAssortmentItemResult>() {
            @Override
            public void onResponse(Call<GetAssortmentItemResult> call, Response<GetAssortmentItemResult> response) {
                GetAssortmentItemResult assortmentItemResult = response.body();
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                if(assortmentItemResult != null){
                    if(assortmentItemResult.getErrorCode() == 0){
                        txtPriceAssortment.setText(String.format("%.2f", assortmentItemResult.getPrice()).replace(",","."));
                        txtNameAsortment.setText(assortmentItemResult.getName());
                        txtMarkingAssortment.setText(assortmentItemResult.getMarking());
                        txtStockAssortment.setText(String.format("%.2f", assortmentItemResult.getRemain()).replace(",","."));
                        txtCodeAssortment.setText(assortmentItemResult.getCode());

                        txtInput_barcode.requestFocus();
                    }
                    else {
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
            public void onFailure(Call<GetAssortmentItemResult> call, Throwable t) {
                Toast.makeText(CheckPriceActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                txtNameAsortment.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
            }
        });
    }

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            if(msg.what == 10) {
                if (msg.arg1 == 12)
                    pgH.dismiss();
                Toast.makeText(CheckPriceActivity.this, getResources().getString(R.string.msg_imprimare_sales), Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 20) {
                pgH.dismiss();
                Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.msg_errore_label_printer), Toast.LENGTH_SHORT).show();
            }
            else if (msg.what==201){
                pgH.dismiss();
                AlertDialog.Builder input = new AlertDialog.Builder(CheckPriceActivity.this);
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
}
