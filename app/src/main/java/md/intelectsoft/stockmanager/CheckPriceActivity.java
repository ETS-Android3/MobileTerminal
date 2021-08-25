package md.intelectsoft.stockmanager;

import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.WarehouseList;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckPriceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    Button btn_depozit;
    ImageButton print, open_assortment_list, write_barcode,addCount,deleteCount;
    TextView txtInput_barcode, txtNameAsortment, txtBarcodeAssortment,txtCodeAssortment,txtMarkingAssortment,txtStockAssortment,txtPriceAssortment, txtUnit;
    ProgressBar pgBar;
    ProgressDialog progressDialog;
    TimerTask timerTaskSync;
    Timer sync;
    EditText count_lable;
    AlertDialog.Builder builderType;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    String UserId, labelPrint, workPlaceId, workPlaceName;
    
    int REQUEST_FROM_LIST_ASSORTMENT = 666;
    Menu menu;
    final boolean[] show_keyboard = {false};


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");
    GetAssortmentItemResult assortmentItemResult;
    TerminalAPI terminalAPI;

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
        btn_depozit = findViewById(R.id.btn_select_warehouse_check_price);
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
        txtUnit = findViewById(R.id.txt_unit_check_price);
        pgBar = findViewById(R.id.progressBar_check_price);
        count_lable = findViewById(R.id.count_etichete_check_price);
        progressDialog = new ProgressDialog(CheckPriceActivity.this);

        View headerLayout = navigationView.getHeaderView(0);
        TextView textWorkPlace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        TextView textUserName = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if (!SPFHelp.getInstance().getBoolean("ShowCode",false))
            txtCodeAssortment.setVisibility(View.INVISIBLE);
        simpleDateFormat.setTimeZone(timeZone);

        workPlaceName = SPFHelp.getInstance().getString("WorkPlaceName", "");
        workPlaceId = SPFHelp.getInstance().getString("WorkPlaceId", "");
        UserId = SPFHelp.getInstance().getString("UserId","");
        String uri = SPFHelp.getInstance().getString("URI","0.0.0.0:1111");

        terminalAPI = TerminalRetrofitClient.getApiTerminalService(uri);

        textUserName.setText(SPFHelp.getInstance().getString("UserName", ""));
        textWorkPlace.setText(workPlaceName);

        btn_depozit.setText(workPlaceName);

        sync = new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,100,3000);

        txtInput_barcode.requestFocus();

        write_barcode.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm.isActive()) {
                ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);   //hide
            } else {
                ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);  //show
                txtInput_barcode.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        });
        btn_depozit.setOnClickListener(v -> {
            getWareHousesList(UserId);
        });
        txtInput_barcode.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) getAssortmentFromService();
            else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) getAssortmentFromService();

            return false;
        });
        open_assortment_list.setOnClickListener(v -> {
            Intent AddingASL = new Intent(CheckPriceActivity.this, ListAssortment.class);
            AddingASL.putExtra("ActivityCount", 161);
            AddingASL.putExtra("WorkPlaceID",workPlaceId);
            startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
        });
        print.setOnClickListener(v -> {
            SharedPreferences sPref = getSharedPreferences("Printers", MODE_PRIVATE);
            final String printerMACAddress = sPref.getString("MAC_ZebraPrinters",null);
            final int labelsNumber = Integer.parseInt(count_lable.getText().toString());
            labelPrint = sPref.getString("Lable",null);

            if(printerMACAddress != null) {
                if (assortmentItemResult != null) {
                    progressDialog.setMessage(getResources().getString(R.string.btn_imprimare_sales) + " " + assortmentItemResult.getName() + " to " + printerMACAddress);
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();

                    new Thread() {
                        public void run() {
                            Connection connection = new BluetoothConnection(printerMACAddress);
                            try {
                                connection.open();
                                String price_unit;
                                if (assortmentItemResult.getUnitInPackage() == null || assortmentItemResult.getUnitInPackage().equals("")){
                                    price_unit = assortmentItemResult.getUnitPrice() + "/" + assortmentItemResult.getUnit();
                                }else{
                                    price_unit = assortmentItemResult.getUnitPrice() + "/" + assortmentItemResult.getUnitInPackage();
                                }
                                //String api_key_yandex = "trnsl.1.1.20190401T113033Z.81c7241519eea2f0.a881247269077361cbd5b905400518b250efe00b";
                                Date dateNow = new Date();
                                String sDateInChisinau = simpleDateFormat.format(dateNow); // Convert to String first

                                String codes = "codes_imprim";
                                String data_imprim = "data_imprim";
                                String name_imprim = "name_imprim";
                                String rus_name_imprim = "rus_imprim";
                                String price_imprim = "price_imprim";
                                String price_unit_imprim = "price_unit_imprim";
                                String barcode_imprim = "barcode_imprim";

                                if(labelPrint != null){
                                    labelPrint = labelPrint.replace(codes, assortmentItemResult.getCode());
                                    labelPrint = labelPrint.replace(data_imprim, sDateInChisinau);
                                    labelPrint = labelPrint.replace(name_imprim, assortmentItemResult.getName());
                                    labelPrint = labelPrint.replace(rus_name_imprim,"");
                                    labelPrint = labelPrint.replace(price_imprim, String.format("%.2f", assortmentItemResult.getPrice()));
                                    labelPrint = labelPrint.replace(price_unit_imprim,price_unit);
                                    labelPrint = labelPrint.replace(barcode_imprim, assortmentItemResult.getBarCode());
                                    if (labelsNumber>1){
                                        for (int i=0;i<labelsNumber;i++){
                                            connection.write(labelPrint.getBytes("UTF-8"));
                                        }
                                    }else{
                                        connection.write(labelPrint.getBytes());
                                    }

                                    mHandler.obtainMessage(10, 12, -1)
                                            .sendToTarget();
                                }else{
                                    mHandler.obtainMessage(20, 12, -1, "Eticheta lipseste!")
                                            .sendToTarget();
                                }
                            } catch (ConnectionException e) {
                                mHandler.obtainMessage(201, 14, -1,e.toString())
                                        .sendToTarget();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                ((BaseApp)getApplication()).appendLog(e.getMessage(), CheckPriceActivity.this);
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
            int curr = Integer.parseInt(count_lable.getText().toString());
            curr += 1;
            count_lable.setText(String.valueOf(curr));
        });
        deleteCount.setOnClickListener(v -> {
            int curr = Integer.parseInt(count_lable.getText().toString());
            if (curr - 1 > 0) {
                curr -= 1;
            }
            count_lable.setText(String.valueOf(curr));
        });
    }

    private void getWareHousesList(String userID) {
        Call<GetWarehousesListResult> getWarehousesListResultCall = terminalAPI.getWareHousesList(userID);

        progressDialog.setMessage("Obtain workplace...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            getWarehousesListResultCall.cancel();
            if(getWarehousesListResultCall.isCanceled())
                dialog.dismiss();
        });

        progressDialog.show();

        getWarehousesListResultCall.enqueue(new Callback<GetWarehousesListResult>() {
            @Override
            public void onResponse(Call<GetWarehousesListResult> call, Response<GetWarehousesListResult> response) {
                GetWarehousesListResult result = response.body();
                if(result != null && result.getErrorCode() == 0){
                    List<WarehouseList> wareHouses = result.getWarehouses();
                    if(wareHouses.size() > 0){
                        List<Map<String,String>> wareListToDialog = new ArrayList<>();

                        for(int i = 0; i < wareHouses.size(); i++){
                            WarehouseList warehouse = wareHouses.get(i);

                            Map<String, String> item = new HashMap<>();
                            item.put("name",warehouse.getName());
                            item.put("id", warehouse.getWarehouseID());

                            wareListToDialog.add(item);
                        }

                        SimpleAdapter simpleAdapterWareHouses = new SimpleAdapter(CheckPriceActivity.this, wareListToDialog ,android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
                        progressDialog.dismiss();

                        new MaterialAlertDialogBuilder(CheckPriceActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Select your workplace:")
                                .setCancelable(false)
                                .setSingleChoiceItems(simpleAdapterWareHouses, -1, (dialog, which) -> {
                                    workPlaceName = wareListToDialog.get(which).get("name");
                                    workPlaceId = wareListToDialog.get(which).get("id");

                                    btn_depozit.setText(workPlaceName);
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    finish();
                                })
                                .show();

                    }
                    else{
                        progressDialog.dismiss();
                        new MaterialAlertDialogBuilder(CheckPriceActivity.this)
                                .setTitle("List of workplace is empty!!")
                                .setMessage("Check your workplace or call 022-83-53-11 for technical support!")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> {
                                    finish();
                                })
                                .show();
                    }
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.msg_error_code) + result.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetWarehousesListResult> call, Throwable t) {
                progressDialog.dismiss();

                new MaterialAlertDialogBuilder(CheckPriceActivity.this)
                        .setTitle("Load workplace error!")
                        .setMessage("Message: " + t.getMessage())
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, which) -> {
                            finish();
                        })
                        .show();
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
//        if (id == R.id.menu_conect) {
//            Intent MenuConnect = new Intent(".MenuConnect");
//            startActivity(MenuConnect);
//        } else
            if (id == R.id.menu_workplace) {
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
            Intent openHelpPage = new Intent(this,Help.class);
            openHelpPage.putExtra("Page",901);
            startActivity(openHelpPage);
        }
        else if (id == R.id.menu_exit) {
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_checkprice);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FROM_LIST_ASSORTMENT){
            if(resultCode == RESULT_OK){
                if (data != null) {
                    assortmentItemResult = new GetAssortmentItemResult();
                    assortmentItemResult.setName(data.getStringExtra("Name"));
                    assortmentItemResult.setUnit(data.getStringExtra("Unit"));
                    assortmentItemResult.setUnitInPackage(data.getStringExtra("UnitInPackage"));
                    assortmentItemResult.setUnitPrice(data.getDoubleExtra("UnitPrice", 0));
                    assortmentItemResult.setCode(data.getStringExtra("Code"));
                    assortmentItemResult.setBarCode(data.getStringExtra("BarCode"));
                    assortmentItemResult.setAssortimentID(data.getStringExtra("Id"));
                    assortmentItemResult.setMarking(data.getStringExtra("Marking"));
                    assortmentItemResult.setPrice(data.getDoubleExtra("Price", 0));
                    assortmentItemResult.setRemain(data.getDoubleExtra("Remain", 0));

                    txtNameAsortment.setText(assortmentItemResult.getName());
                    txtUnit.setText(assortmentItemResult.getUnit());
                    double price = assortmentItemResult.getPrice();
                    txtPriceAssortment.setText(String.valueOf(price).replace(",","."));

                    if (assortmentItemResult.getMarking() != null) {
                        txtMarkingAssortment.setText(assortmentItemResult.getMarking());
                    } else {
                        txtMarkingAssortment.setText("-");
                    }
                    txtCodeAssortment.setText(assortmentItemResult.getCode());
                    txtBarcodeAssortment.setText(assortmentItemResult.getBarCode());
                    if(assortmentItemResult.getRemain() != null)
                        txtStockAssortment.setText(String.format("%.2f",assortmentItemResult.getRemain()));
                    else
                        txtStockAssortment.setText("0");
                    txtInput_barcode.requestFocus();
                }
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            txtInput_barcode.requestFocus();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_1: {
                    txtInput_barcode.append("1");
                }
                break;
                case KeyEvent.KEYCODE_2: {
                    txtInput_barcode.append("2");
                }
                break;
                case KeyEvent.KEYCODE_3: {
                    txtInput_barcode.append("3");
                }
                break;
                case KeyEvent.KEYCODE_4: {
                    txtInput_barcode.append("4");
                }
                break;
                case KeyEvent.KEYCODE_5: {
                    txtInput_barcode.append("5");
                }
                break;
                case KeyEvent.KEYCODE_6: {
                    txtInput_barcode.append("6");
                }
                break;
                case KeyEvent.KEYCODE_7: {
                    txtInput_barcode.append("7");
                }
                break;
                case KeyEvent.KEYCODE_8: {
                    txtInput_barcode.append("8");
                }
                break;
                case KeyEvent.KEYCODE_9: {
                    txtInput_barcode.append("9");
                }
                break;
                case KeyEvent.KEYCODE_0: {
                    txtInput_barcode.append("0");
                }
                break;
                case KeyEvent.KEYCODE_DEL: {
                    String test = txtInput_barcode.getText().toString();
                    if (!txtInput_barcode.getText().toString().equals("")) {
                        txtInput_barcode.setText(test.substring(0, test.length() - 1));
                        txtInput_barcode.requestFocus();
                    }
                }
                break;
                default:
                    break;
            }
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

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                CheckPriceActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Call<Boolean> call = terminalAPI.pingService();

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
        assortmentItemBody.setWarehouseID(workPlaceId);

        txtBarcodeAssortment.setText(txtInput_barcode.getText().toString());
        txtInput_barcode.setText("");

        Call<GetAssortmentItemResult> call = terminalAPI.getAssortmentItem(assortmentItemBody);

        call.enqueue(new Callback<GetAssortmentItemResult>() {
            @Override
            public void onResponse(Call<GetAssortmentItemResult> call, Response<GetAssortmentItemResult> response) {
                assortmentItemResult = response.body();
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                if(assortmentItemResult != null){
                    if(assortmentItemResult.getErrorCode() == 0){
                        txtPriceAssortment.setText(String.format("%.2f", assortmentItemResult.getPrice()).replace(",","."));
                        txtNameAsortment.setText(assortmentItemResult.getName());
                        txtMarkingAssortment.setText(assortmentItemResult.getMarking());
                        txtStockAssortment.setText(String.format("%.2f", assortmentItemResult.getRemain()).replace(",","."));
                        txtCodeAssortment.setText(assortmentItemResult.getCode());
                        txtUnit.setText("/" + assortmentItemResult.getUnit());

                    }
                    else {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtNameAsortment.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                        txtPriceAssortment.setText("-");
                        txtStockAssortment.setText("-");
                        txtMarkingAssortment.setText("-");
                        txtCodeAssortment.setText("-");
                    }
                    txtInput_barcode.requestFocus();
                }
                else{
                    pgBar.setVisibility(ProgressBar.INVISIBLE);
                    txtNameAsortment.setText(getResources().getString(R.string.txt_depozit_nedeterminat));
                    txtPriceAssortment.setText("-");
                    txtStockAssortment.setText("-");
                    txtMarkingAssortment.setText("-");
                    txtCodeAssortment.setText("-");
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
                    progressDialog.dismiss();
                Toast.makeText(CheckPriceActivity.this, getResources().getString(R.string.msg_imprimare_sales), Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == 20) {
                progressDialog.dismiss();
                Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.msg_errore_label_printer) + " / " + msg.obj, Toast.LENGTH_SHORT).show();
            }
            else if (msg.what==201){
                progressDialog.dismiss();
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
            else{
                Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.msg_errore_label_printer) + msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };
}
