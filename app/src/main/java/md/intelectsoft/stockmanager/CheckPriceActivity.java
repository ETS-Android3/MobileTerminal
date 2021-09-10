package md.intelectsoft.stockmanager;

import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.text.TextUtils;
import android.util.Base64;
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

import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.utils.BitmapConvertUtil;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintInvoiceResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintersResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.PrinterResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.WarehouseList;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import md.intelectsoft.stockmanager.app.utils.BaseEnum;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import md.intelectsoft.stockmanager.app.utils.ToastUtil;
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

    AlertDialog.Builder builderType,builderTypePrinters;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> printers_List_array = new ArrayList<>();
    String UserId, labelPrint, workPlaceId, workPlaceName;

    private RTPrinter rtPrinter = null;
    Bitmap mBitmap = null;

    int REQUEST_FROM_LIST_ASSORTMENT = 666, width;
    Menu menu;
    final boolean[] show_keyboard = {false};


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");
    AssortmentParcelable result;
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
            printPrices();
//            SharedPreferences sPref = getSharedPreferences("Printers", MODE_PRIVATE);
//            final String printerMACAddress = sPref.getString("MAC_ZebraPrinters",null);
//            final int labelsNumber = Integer.parseInt(count_lable.getText().toString());
//            labelPrint = sPref.getString("Lable",null);
//
//            if(printerMACAddress != null) {
//                if (result != null) {
//                    progressDialog.setMessage(getResources().getString(R.string.btn_imprimare_sales) + " " + result.getName() + " to " + printerMACAddress);
//                    progressDialog.setCancelable(false);
//                    progressDialog.setIndeterminate(true);
//                    progressDialog.show();
//
//                    new Thread() {
//                        public void run() {
//                            Connection connection = new BluetoothConnection(printerMACAddress);
//                            try {
//                                connection.open();
//                                String price_unit;
//                                if (result.getUnitInPackage() == null || result.getUnitInPackage().equals("")){
//                                    price_unit = result.getUnitPrice() + "/" + result.getUnit();
//                                }else{
//                                    price_unit = result.getUnitPrice() + "/" + result.getUnitInPackage();
//                                }
//                                //String api_key_yandex = "trnsl.1.1.20190401T113033Z.81c7241519eea2f0.a881247269077361cbd5b905400518b250efe00b";
//                                Date dateNow = new Date();
//                                String sDateInChisinau = simpleDateFormat.format(dateNow); // Convert to String first
//
//                                String codes = "codes_imprim";
//                                String data_imprim = "data_imprim";
//                                String name_imprim = "name_imprim";
//                                String rus_name_imprim = "rus_imprim";
//                                String price_imprim = "price_imprim";
//                                String price_unit_imprim = "price_unit_imprim";
//                                String barcode_imprim = "barcode_imprim";
//
//                                if(labelPrint != null){
//                                    labelPrint = labelPrint.replace(codes, result.getCode());
//                                    labelPrint = labelPrint.replace(data_imprim, sDateInChisinau);
//                                    labelPrint = labelPrint.replace(name_imprim, result.getName());
//                                    labelPrint = labelPrint.replace(rus_name_imprim,"");
//                                    labelPrint = labelPrint.replace(price_imprim, String.format("%.2f", result.getPrice()));
//                                    labelPrint = labelPrint.replace(price_unit_imprim,price_unit);
//                                    labelPrint = labelPrint.replace(barcode_imprim, result.getBarCode());
//                                    if (labelsNumber>1){
//                                        for (int i=0;i<labelsNumber;i++){
//                                            connection.write(labelPrint.getBytes("UTF-8"));
//                                        }
//                                    }else{
//                                        connection.write(labelPrint.getBytes());
//                                    }
//
//                                    mHandler.obtainMessage(10, 12, -1)
//                                            .sendToTarget();
//                                }else{
//                                    mHandler.obtainMessage(20, 12, -1, "Eticheta lipseste!")
//                                            .sendToTarget();
//                                }
//                            } catch (ConnectionException e) {
//                                mHandler.obtainMessage(201, 14, -1,e.toString())
//                                        .sendToTarget();
//                            } catch (UnsupportedEncodingException e) {
//                                e.printStackTrace();
//                                ((BaseApp)getApplication()).appendLog(e.getMessage(), CheckPriceActivity.this);
//                            }
//                        }
//                    }.start();
//                }
//            }
//            else{
//                Toast.makeText(CheckPriceActivity.this,getResources().getString(R.string.txt_not_conected_printers), Toast.LENGTH_SHORT).show();
//            }
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
    private void printPrices() {
        final SharedPreferences SharedPrinters = getSharedPreferences("Printers", MODE_PRIVATE);
        int positionType = SharedPrinters.getInt("Type",0);

        if(positionType == BaseEnum.POS_PRINTER){
            width = BaseApp.getInstance().getWidthPrinterPrint();
            if(width == 0){
                width = SharedPrinters.getInt("WithDisplay",0);
            }
            Call<GetPrintInvoiceResults> call = terminalAPI.getPrintAssortmentPrice(assortmentItemResult.getAssortimentID(),workPlaceId);

            call.enqueue(new Callback<GetPrintInvoiceResults>() {
                @Override
                public void onResponse(Call<GetPrintInvoiceResults> call, Response<GetPrintInvoiceResults> response) {
                    GetPrintInvoiceResults results = response.body();

                    if(results != null){
                        if(results.getErrorCode() == 0)
                            printToBluetoothPrinter(results.getImageFile());

                        else{
                            AlertDialog.Builder dialog = new AlertDialog.Builder(CheckPriceActivity.this);
                            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                            dialog.setCancelable(false);
                            dialog.setMessage("Eroare la descarcarea imaginei: " + results.getErrorCode());
                            dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }

                    }
                    else{
                        AlertDialog.Builder dialog = new AlertDialog.Builder(CheckPriceActivity.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        dialog.setCancelable(false);
                        dialog.setMessage("Nici o imprimanta de la server");
                        dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }

                @Override
                public void onFailure(Call<GetPrintInvoiceResults> call, Throwable t) {
                    Toast.makeText(CheckPriceActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
//        else {
//            printers_List_array.clear();
//            Call<GetPrintersResult> call = terminalAPI.getPrinters(WorkPlaceGetPrinters);
//
//            call.enqueue(new Callback<GetPrintersResult>() {
//                @Override
//                public void onResponse(@NonNull Call<GetPrintersResult> call, @NonNull Response<GetPrintersResult> response) {
//                    GetPrintersResult result = response.body();
//                    if(result != null){
//                        if(result.getErrorCode() == 0){
//                            List<PrinterResults> printers = result.getPrinters();
//                            if(result.getPrinters() == null){
//                                AlertDialog.Builder dialog = new AlertDialog.Builder(CheckPriceActivity.this);
//                                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
//                                dialog.setCancelable(false);
//                                dialog.setMessage(getResources().getString(R.string.txt_not_printers_check_back_office));
//                                dialog.setPositiveButton(getResources().getString(R.string.txt_save_document), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                                if (CheckPriceActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
//                                    return;
//                                }
//                                dialog.show();
//                            }
//
//                            if(printers.size() == 1){
//                                PrinterResults printerResults = printers.get(0);
//                                printInvoiceToService(printerResults.getID(), printerResults.getName());
//                            }
//                            else if (printers.size() > 1) {
//                                for (PrinterResults printer : printers) {
//                                    HashMap<String, Object> Printers = new HashMap<>();
//                                    Printers.put("Name", printer.getName());
//                                    Printers.put("Code", printer.getCode());
//                                    Printers.put("ID", printer.getID());
//                                    printers_List_array.add(Printers);
//                                }
//                                show_printers();
//                            }
//                        }
//                        else{
//                            AlertDialog.Builder dialog = new AlertDialog.Builder(CheckPriceActivity.this);
//                            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
//                            dialog.setCancelable(false);
//                            dialog.setMessage("Eroare la descarcarea imprimatelor: " + result.getErrorCode());
//                            dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            dialog.show();
//                        }
//                    }
//                    else{
//                        AlertDialog.Builder dialog = new AlertDialog.Builder(CheckPriceActivity.this);
//                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
//                        dialog.setCancelable(false);
//                        dialog.setMessage("Eroare la descarcarea imprimatelor! Nici un raspuns!");
//                        dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        });
//                        dialog.show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<GetPrintersResult> call, Throwable t) {
//                    Toast.makeText(CheckPriceActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }
    private void printToBluetoothPrinter(String imageId){
        rtPrinter = BaseApp.getInstance().getRtPrinter();

        byte[] decodedString = Base64.decode(imageId, Base64.DEFAULT );

        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        mBitmap = bitmap;

        if (BaseApp.getInstance().getCurrentCmdType() == BaseEnum.CMD_ESC) {
            if (mBitmap.getWidth() > 48 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bitmap,48 * 8, 4000);
            }
        }
        else {
            if (mBitmap.getWidth() > 72 * 8) {
                mBitmap = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bitmap, 72 * 8, 4000);
            }
        }

        try {
            print();
        } catch (SdkException e) {
            e.printStackTrace();
        }
    }
    private void print() throws SdkException {
        if (mBitmap == null) {
            ToastUtil.show(this, "No image,Firstly Upload a image");
            return;
        }
        escPrint();
    }

    private void escPrint() throws SdkException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog("Loading...");

                CmdFactory cmdFactory = new EscFactory();
                Cmd cmd = cmdFactory.create();
                cmd.append(cmd.getHeaderCmd());

                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                BitmapSetting bitmapSetting = new BitmapSetting();

                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_MULTI_COLOR);

                bitmapSetting.setBimtapLimitWidth(width * 8);
                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());//Sync Write
                }

                hideProgressDialog();
            }
        }).start();
    }
//    public void show_printers(){
//        SimpleAdapter simpleAdapterType = new SimpleAdapter(CheckPriceActivity.this, printers_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
//        builderTypePrinters = new AlertDialog.Builder(CheckPriceActivity.this);
//        builderTypePrinters.setTitle(getResources().getString(R.string.txt_header_msg_sales_printers));
//        builderTypePrinters.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                printers_List_array.clear();
//                dialogInterface.dismiss();
//            }
//        });
//        builderTypePrinters.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int wich) {
//                String ID = String.valueOf(printers_List_array.get(wich).get("ID"));
//                String Name = String.valueOf(printers_List_array.get(wich).get("Name"));
//                printInvoiceToService(ID,Name);
//            }
//        });
//        builderTypePrinters.setCancelable(false);
//        if(isDestroyed())
//            return;
//        else
//            builderTypePrinters.show();
//    }

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
                     result = data.getParcelableExtra("AssortimentClicked");

                    assortmentItemResult = new GetAssortmentItemResult();
                    assortmentItemResult.setName(result.getName());
                    assortmentItemResult.setUnit(result.getUnit());
                    assortmentItemResult.setUnitInPackage(result.getUnitInPackage());
                    assortmentItemResult.setUnitPrice(result.getUnitPrice());
                    assortmentItemResult.setCode(result.getCode());
                    assortmentItemResult.setBarCode(result.getBarCode());
                    assortmentItemResult.setAssortimentID(result.getAssortimentID());
                    assortmentItemResult.setMarking(result.getMarking());
                    assortmentItemResult.setPrice(result.getPrice());
                    assortmentItemResult.setRemain(result.getRemain());

                    txtNameAsortment.setText(result.getName());
                    txtUnit.setText(result.getUnit());
                    double price = Double.valueOf(result.getPrice());
                    txtPriceAssortment.setText(result.getPrice().replace(",","."));

                    if (result.getMarking() != null) {
                        txtMarkingAssortment.setText(result.getMarking());
                    } else {
                        txtMarkingAssortment.setText("-");
                    }
                    txtCodeAssortment.setText(result.getCode());
                    txtBarcodeAssortment.setText(result.getBarCode());
                    if(result.getRemain() != null)
                        txtStockAssortment.setText(String.format("%.2f",result.getRemain()));
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
    public void showProgressDialog(final String str){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = new ProgressDialog(CheckPriceActivity.this);
                }
                if(!TextUtils.isEmpty(str)){
                    progressDialog.setMessage(str);
                }else{
                    progressDialog.setMessage("Loading...");
                }
                progressDialog.show();
            }
        });
    }
    public void hideProgressDialog(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.hide();
                }
            }
        });

    }
}
