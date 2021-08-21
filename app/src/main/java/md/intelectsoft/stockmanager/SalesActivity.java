package md.intelectsoft.stockmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// md.intelectsoft.stockmanager.NetworkUtils.ApiRetrofit;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.InvoiceLineBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveInvoiceBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentRemainResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintInvoiceResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintersResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.PrinterResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Remain;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.ResponseSimple;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.SaveInvoiceResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.WarehouseList;
//import md.intelectsoft.stockmanager.NetworkUtils.Services.CommandService;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import md.intelectsoft.stockmanager.app.utils.BaseEnum;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import md.intelectsoft.stockmanager.app.utils.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.stockmanager.ListAssortment.AssortimentClickentSendIntent;

public class SalesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ImageButton btn_write_barcode,btn_delete,btn_open_asl_list, btn_check_remain_for_assortment;
    TextView txt_input_barcode,txt_total_sales,txtBarcode_introdus;
    ProgressBar pgBar;
    Button btn_print,btn_ok,btn_cancel,btn_change_stock,btn_add_Client;
    ListView list_of_sales;

    AlertDialog.Builder builderType,builderTypePrinters, dialogBuilderRemainAsl;
    SimpleAdapter simpleAdapterASL;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> printers_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> remain_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();

    String UserId,WareUid,InvoiceID,ClientID,ClientName,uid_selected,barcode_introdus,WareName,WorkPlaceID, WorkPlaceGetPrinters,assortmentWareUid, assortmentSelectedName;
    int InvoiceCode;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;
    Boolean invoiceSaved = false;
    boolean createNewInvoice = false;

    Menu menu;
    JSONArray mAssortmentArray;

   // CommandService commandService;

    TerminalAPI terminalAPI;

    int limit_sales, REQUEST_FROM_COUNT_ACTIVITY = 110,REQUEST_FROM_LIST_ASSORTMENT = 210,REQUEST_FROM_GET_CLIENT = 30, width;
    final boolean[] show_keyboard = {false};

    Bitmap mBitmap = null;
    private RTPrinter rtPrinter = null;
    private ProgressDialog progressDialog;

    String mInputComment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_sales);

        setTitle(getResources().getString(R.string.btn_sales_main_activity));

        Toolbar toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);

        btn_write_barcode = findViewById(R.id.btn_write_manual_sales);
        txt_input_barcode = findViewById(R.id.txt_input_barcodes_sales);
        btn_delete = findViewById(R.id.btn_delete_sales);
        btn_open_asl_list = findViewById(R.id.btn_touch_open_asl_sales);
        txt_total_sales = findViewById(R.id.txtTotal_sales);
        pgBar = findViewById(R.id.progressBar_sales);
        txtBarcode_introdus = findViewById(R.id.txtBarcode_introdus_sales);
        btn_add_Client = findViewById(R.id.btn_add_client_sales);
        btn_ok = findViewById(R.id.btn_ok_sales);
        btn_print = findViewById(R.id.btn_print_sales);
        btn_cancel = findViewById(R.id.btn_cancel_sales);
        btn_change_stock = findViewById(R.id.btn_change_stock_sales);
        list_of_sales = findViewById(R.id.LL_list_sales);
        btn_check_remain_for_assortment = findViewById(R.id.btn_check_remain_assortment);
        pgH = new ProgressDialog(SalesActivity.this);


        DrawerLayout drawer = findViewById(R.id.drawer_layout_sales);
        NavigationView navigationView = findViewById(R.id.nav_view_sales);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = headerLayout.findViewById(R.id.txt_name_of_user);
        TextView user_workplace = headerLayout.findViewById(R.id.txt_workplace_user);

        //commandService = ApiRetrofit.getCommandService(this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        UserId = SPFHelp.getInstance().getString("UserId","");
        String uri = SPFHelp.getInstance().getString("URI","0.0.0.0:1111");
        terminalAPI = TerminalRetrofitClient.getApiTerminalService(uri);

        mAssortmentArray = new JSONArray();


        useremail.setText(User.getString("Name",""));


        user_workplace.setText(WorkPlace.getString("Name",""));

        WorkPlaceID = WorkPlace.getString("Uid","0");
        WorkPlaceGetPrinters = WorkPlace.getString("Uid","0");
        final String WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");

        if (WorkPlaceName.equals("Nedeterminat") || WorkPlaceName.equals("")){
            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
            pgH.setCancelable(false);
            pgH.setIndeterminate(true);
            pgH.show();
            ((BaseApp)getApplication()).setDownloadASLVariable(false);
            changeOrSelectWareHouse(false);
        }
        else{
            btn_change_stock.setText(WorkPlaceName);
            WareUid = WorkPlaceID;
            WareName = WorkPlaceName;
        }

        String limit = Settings.getString("LimitSales","0");
        limit_sales = Integer.valueOf(limit);
        if(limit_sales == 0){
            limit_sales = 1000;
        }

        sync = new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,2000,3000);

        btn_print.setEnabled(false);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createNewInvoice){
                    btn_cancel.setText(SalesActivity.this.getResources().getString(R.string.txt_renunt_all));
                    btn_add_Client.setEnabled(true);
                    btn_add_Client.setText(SalesActivity.this.getResources().getString(R.string.btn_adauga_client_sales));
                    mInputComment = null;
                    ClientID = null;
                    btn_print.setEnabled(false);
                    invoiceSaved = false;
                    createNewInvoice = false;
                    uid_selected = null;

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
                    Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
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
                uid_selected = (String)asl_list.get(position).get("Uid");
                assortmentWareUid =  (String)asl_list.get(position).get("WareUid");
                assortmentSelectedName = (String)asl_list.get(position).get("Name");
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!invoiceSaved){
                    if(uid_selected != null){
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
                            ((BaseApp)getApplication()).appendLog(e.getMessage(), SalesActivity.this);
                        }
                    }
                }
            }
        });
        btn_check_remain_for_assortment.setOnClickListener(v -> {
            if(!invoiceSaved) {
                if (uid_selected != null) {
                    pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                    pgH.setIndeterminate(true);
                    pgH.setCancelable(false);
                    pgH.show();
                    //commandService = ApiRetrofit.getCommandService(SalesActivity.this);
                    Call<GetAssortmentRemainResults> call = terminalAPI.getAssortimentRemains(uid_selected,WareUid);

                    call.enqueue(new Callback<GetAssortmentRemainResults>() {
                        @Override
                        public void onResponse(Call<GetAssortmentRemainResults> call, Response<GetAssortmentRemainResults> response) {
                            if(response.isSuccessful()){
                                GetAssortmentRemainResults remain = response.body();
                                if(remain != null && remain.getErrorCode() == 0){
                                    List<Remain> remains = remain.getRemains();
                                    if(remains.size() > 0){
                                        pgH.dismiss();
                                        for(Remain remain1 : remains){
                                            HashMap<String, Object> WareHouse = new HashMap<>();
                                            WareHouse.put("remain", remain1.getRemain());
                                            WareHouse.put("warehouseId", remain1.getWarehouseID());
                                            WareHouse.put("warehouseName", remain1.getWarehouseName());
                                            remain_List_array.add(WareHouse);
                                        }

                                        SimpleAdapter simpleAdapterType = new SimpleAdapter(SalesActivity.this, remain_List_array,R.layout.item_dialog_remain_assortment, new String[]{"warehouseName","remain"}, new int[]{R.id.txtName_warehouse,R.id.text_remain_assortment});
                                        dialogBuilderRemainAsl = new AlertDialog.Builder(SalesActivity.this);
                                        dialogBuilderRemainAsl.setTitle(assortmentSelectedName);
                                        dialogBuilderRemainAsl.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                remain_List_array.clear();
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        dialogBuilderRemainAsl.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int wich) {

                                                String wareDialogId = (String) remain_List_array.get(wich).get("warehouseId");
                                                String wareDialogName = (String) remain_List_array.get(wich).get("warehouseName");
                                                try {
                                                    for (int i = 0; i< mAssortmentArray.length(); i++) {
                                                        JSONObject json = mAssortmentArray.getJSONObject(i);
                                                        String Uid = json.getString("AssortimentUid");
                                                        if (uid_selected.contains(Uid)) {
                                                            json.put("Warehouse",wareDialogId);
                                                            json.put("WareName",wareDialogName);
                                                            asl_list.clear();
                                                            showAssortmentList();
                                                        }
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                remain_List_array.clear();
                                            }
                                        });
                                        dialogBuilderRemainAsl.setCancelable(false);
                                        pgH.dismiss();
                                        if(!SalesActivity.this.isDestroyed())
                                            dialogBuilderRemainAsl.show();
                                    }
                                    else{
                                        pgH.dismiss();
                                        Toast.makeText(SalesActivity.this, "List of remain is empty!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else{
                                    pgH.dismiss();
                                    Toast.makeText(SalesActivity.this, "List of remain is empty or error: " + remain.getErrorCode(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                pgH.dismiss();
                                Toast.makeText(SalesActivity.this, "Not connected successful!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GetAssortmentRemainResults> call, Throwable t) {
                            pgH.dismiss();
                            Toast.makeText(SalesActivity.this, "Error." + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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

                        SaveInvoiceBody saveInvoiceBody = new SaveInvoiceBody();
                        List<InvoiceLineBody> lineBodyList = new ArrayList<>();

                        for (int i = 0; i < mAssortmentArray.length(); i++) {
                            InvoiceLineBody invoiceLineBody = new InvoiceLineBody();

                            try {
                                invoiceLineBody.setAssortiment(mAssortmentArray.getJSONObject(i).getString("AssortimentUid"));
                                invoiceLineBody.setQuantity(mAssortmentArray.getJSONObject(i).getDouble("Count"));
                                invoiceLineBody.setSalePrice(mAssortmentArray.getJSONObject(i).getDouble("Price"));
                                invoiceLineBody.setWarehouse(mAssortmentArray.getJSONObject(i).getString("Warehouse"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            lineBodyList.add(invoiceLineBody);
                        }

                        saveInvoiceBody.setLines(lineBodyList);
                        saveInvoiceBody.setUserID(UserId);
                        if(mInputComment != null)
                            saveInvoiceBody.setComment(mInputComment);
                        else
                            saveInvoiceBody.setComment("FROM TERMINAL");
                        saveInvoiceBody.setTerminalCode("FROM TERMINAL");


                        saveInvoiceBody.setWarehouse(WorkPlaceID);
                        saveInvoiceBody.setClientID(ClientID);

                        Call<SaveInvoiceResult> call = terminalAPI.saveInvoice(saveInvoiceBody);

                        call.enqueue(new Callback<SaveInvoiceResult>() {
                            @Override
                            public void onResponse(Call<SaveInvoiceResult> call, Response<SaveInvoiceResult> response) {
                                SaveInvoiceResult result = response.body();
                                if(result != null){
                                    if(result.getErrorCode() == 0 ){
                                        pgH.dismiss();
                                        invoiceSaved = true;
                                        InvoiceCode = result.getInvoiceCode();
                                        InvoiceID = result.getInvoiceID();
                                        setTitle(getResources().getString(R.string.btn_sales_main_activity) + ": " + InvoiceCode);
                                        btn_print.setEnabled(true);
                                        createNewInvoice = true;
                                        btn_cancel.setText("New");
                                        btn_add_Client.setEnabled(false);

                                        Toast.makeText(SalesActivity.this, getResources().getString(R.string.txt_sales_invoice_saved) + InvoiceCode, Toast.LENGTH_SHORT).show();
                                        boolean print = getSharedPreferences("Settings",MODE_PRIVATE).getBoolean("PrintSales",false);
                                        if(print) {
                                            printSales();
                                        }
                                    }
                                    else{
                                        pgH.dismiss();
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
                                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                                        dialog.setCancelable(false);
                                        dialog.setMessage(getResources().getString(R.string.msg_document_notsaved_cod_error) + result.getErrorCode());
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
                                    pgH.dismiss();
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
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

                            @Override
                            public void onFailure(Call<SaveInvoiceResult> call, Throwable t) {
                                pgH.dismiss();
                                Toast.makeText(SalesActivity.this,  t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                }
                else{
                    finish();
                }
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean print = getSharedPreferences("Settings",MODE_PRIVATE).getBoolean("PrintSales",false);
                if(print) {
                    printSales();
                }
            }
        });
        btn_change_stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!invoiceSaved) {
                    stock_List_array.clear();
                    changeOrSelectWareHouse(true);
                }else{
                    Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_add_Client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.msg_add_client_comment, null);

                AlertDialog addCeva = new AlertDialog.Builder(SalesActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme).create();
                addCeva.setView(dialogView);

                Button btnComment = dialogView.findViewById(R.id.btn_cook);
                Button btnCancel = dialogView.findViewById(R.id.btn_canceled);
                Button btnClient = dialogView.findViewById(R.id.btn_finished_cook);

                btnClient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCeva.dismiss();
                        Intent Logins = new Intent(".GetClientMobile");
                        startActivityForResult(Logins, REQUEST_FROM_GET_CLIENT);
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCeva.dismiss();
                    }
                });

                btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addCeva.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(SalesActivity.this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);
                        builder.setTitle("Comentarii:");
                        builder.setMessage("Introduceti comentariul:");
                        final EditText input = new EditText(SalesActivity.this);
                        input.setFocusable(true);
                        builder.setCancelable(false);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

                        builder.setPositiveButton("Adauga", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mInputComment = input.getText().toString();
                                if(ClientName == null)
                                  btn_add_Client.setText(mInputComment);
                                else{
                                    btn_add_Client.setText(ClientName + " /" + mInputComment);
                                }
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            }
                        });
                        builder.setNegativeButton("Renunt", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                addCeva.show();

            }
        });
        btn_open_asl_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!invoiceSaved) {
                    Intent AddingASL = new Intent(".AssortmentMobile");
                    AddingASL.putExtra("ActivityCount", 181);
                    AddingASL.putExtra("WareName",WareName);
                    AddingASL.putExtra("WareID",WareUid);
                    startActivityForResult(AddingASL, REQUEST_FROM_LIST_ASSORTMENT);
                }
                else{
                    Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
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
                    boolean isExtist = false;
                    if (mAssortmentArray.length()!=0) {
                        for (int i = 0; i < mAssortmentArray.length(); i++) {
                            JSONObject object = mAssortmentArray.getJSONObject(i);
                            String AssortimentUid = object.getString("AssortimentUid");
                            Double countInt = object.getDouble("Count");

                            if (AssortimentUid.contains(UidAsl)) {
                                Double CountAdd = json.getDouble("Count");
                                Double CountNew = CountAdd + countInt;
                                object.put("Count", CountNew);
                                isExtist=true;
                                break;
                            }
                        }
                        if (!isExtist){
                            if (mAssortmentArray.length()<limit_sales)
                                mAssortmentArray.put(json);
                            else
                                Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if (mAssortmentArray.length()<limit_sales)
                            mAssortmentArray.put(json);
                        else
                            Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                    }
                    asl_list.clear();
                    showAssortmentList();

                } catch (JSONException e) {
                    e.printStackTrace();
                    ((BaseApp)getApplication()).appendLog(e.getMessage(), SalesActivity.this);
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
                ClientID = data.getStringExtra("ClientID");
                ClientName = data.getStringExtra("ClientName");
                if(mInputComment == null){
                    btn_add_Client.setText(ClientName);
                }
                else{
                    btn_add_Client.setText(ClientName + " /" + mInputComment);
                }

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

                            for (int k = 0; k < mAssortmentArray.length(); k++) {
                                JSONObject object = mAssortmentArray.getJSONObject(k);
                                String AssortimentUid = object.getString("AssortimentUid");
                                Double countInt = object.getDouble("Count");

                                if (AssortimentUid.contains(AssortimentUid_from_touch)) {
                                    Double CountAdd = object_from_touch.getDouble("Count");
                                    Double CountNew = CountAdd + countInt;

                                    object.put("Count", CountNew);
                                    isExtist = true;
                                    break;
                                }
                            }
                            if (!isExtist) {
                                if (mAssortmentArray.length()<limit_sales)
                                    mAssortmentArray.put(object_from_touch);
                                else
                                    Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            if (mAssortmentArray.length()<limit_sales)
                                mAssortmentArray.put(object_from_touch);
                            else
                                Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_depasirea_limitei_vinzare), Toast.LENGTH_SHORT).show();
                        }
                    }
                    asl_list.clear();
                    showAssortmentList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((BaseApp)getApplication()).appendLog(e.getMessage(), SalesActivity.this);
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
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

                GetAssortmentItemBody assortmentItemBody = new GetAssortmentItemBody();
                assortmentItemBody.setAssortmentIdentifier(txt_input_barcode.getText().toString());
                assortmentItemBody.setShowStocks(true);
                assortmentItemBody.setUserID(UserId);
                assortmentItemBody.setWarehouseID(WareUid);

                txtBarcode_introdus.setText(txt_input_barcode.getText().toString());
                barcode_introdus = txt_input_barcode.getText().toString();
                txt_input_barcode.setText("");

                Call<GetAssortmentItemResult> call = terminalAPI.getAssortmentItem(assortmentItemBody);

                call.enqueue(new Callback<GetAssortmentItemResult>() {
                    @Override
                    public void onResponse(Call<GetAssortmentItemResult> call, Response<GetAssortmentItemResult> response) {
                        GetAssortmentItemResult assortmentItemResult = response.body();
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        if(assortmentItemResult != null){
                            if(assortmentItemResult.getErrorCode() == 0){
                                pgBar.setVisibility(ProgressBar.INVISIBLE);
                                Assortment assortment = new Assortment();
                                assortment.setBarCode(assortmentItemResult.getBarCode());
                                assortment.setCode(assortmentItemResult.getCode());
                                assortment.setName(assortmentItemResult.getName());
                                assortment.setUnit(assortmentItemResult.getUnit());
//                                assortment.setPrice(String.format("%.2f", assortmentItemResult.getPrice()).replace(",","."));
                                assortment.setMarking(assortmentItemResult.getMarking());
//                                assortment.setRemain(String.format("%.2f", assortmentItemResult.getRemain()).replace(",","."));
                                assortment.setAssortimentID(assortmentItemResult.getAssortimentID());
//                                assortment.setAllowNonIntegerSale(String.valueOf(assortmentItemResult.getAllowNonIntegerSale()));
                                final AssortmentParcelable assortmentParcelable = new AssortmentParcelable(assortment);

                                Intent sales = new Intent(".CountSalesMobile");
                                sales.putExtra("WhareHouse",WareUid);
                                sales.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
                                sales.putExtra("WhareNames",WareName);

                                startActivityForResult(sales, REQUEST_FROM_COUNT_ACTIVITY);
                            }
                            else {
                                pgBar.setVisibility(ProgressBar.INVISIBLE);
                                txtBarcode_introdus.setText(barcode_introdus + " - " + getResources().getString(R.string.txt_depozit_nedeterminat));
                                txt_input_barcode.requestFocus();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<GetAssortmentItemResult> call, Throwable t) {
                        Toast.makeText(SalesActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                    }
                });

            } else {
                txt_input_barcode.setText("");
                Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_sales_is_saved), Toast.LENGTH_SHORT).show();
            }
        }else{
            txt_input_barcode.setText("");
            Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_barcode_empty), Toast.LENGTH_SHORT).show();
        }
    }

    public void showWareHouse(boolean wareChanged){

        SimpleAdapter simpleAdapterType = new SimpleAdapter(SalesActivity.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(SalesActivity.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(R.string.txt_renunt_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                if(!wareChanged)
                    finish();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                btn_change_stock.setText(String.valueOf(stock_List_array.get(wich).get("Name")));
                WareUid = String.valueOf(stock_List_array.get(wich).get("Uid"));
                if(wareChanged)
                    WorkPlaceID = String.valueOf(stock_List_array.get(wich).get("Uid"));
                WareName = String.valueOf(stock_List_array.get(wich).get("Name"));
                stock_List_array.clear();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        if(!SalesActivity.this.isDestroyed())
            builderType.show();
    }

    public void changeOrSelectWareHouse(boolean warehouseChanged){
        Call<GetWarehousesListResult> call = terminalAPI.getWareHousesList(UserId);

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
                        showWareHouse(warehouseChanged);
                    }else{
                        pgH.dismiss();
                        Toast.makeText(SalesActivity.this,getResources().getString(R.string.msg_error_code) + warehousesListResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<GetWarehousesListResult> call, Throwable t) {
                pgH.dismiss();
                Toast.makeText(SalesActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void show_printers(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(SalesActivity.this, printers_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderTypePrinters = new AlertDialog.Builder(SalesActivity.this);
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
                String ID = String.valueOf(printers_List_array.get(wich).get("ID"));
                String Name = String.valueOf(printers_List_array.get(wich).get("Name"));
                printInvoiceToService(ID,Name);
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
            for (int i = 0; i < mAssortmentArray.length(); i++){
                JSONObject json = mAssortmentArray.getJSONObject(i);
                HashMap<String, Object> asl_ = new HashMap<>();

                String Name = json.getString("AssortimentName");
                String Cant = json.getString("Count");
                String WareName = json.getString("WareName");
                String WareUid = json.getString("Warehouse");

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
                asl_.put("WareUid",WareUid);
                asl_.put("Ware",WareName);

                String suma =String.format("%.2f",Double.valueOf(Price)* count_to_double );
                sumTotal=sumTotal+Double.valueOf(Price)* Double.valueOf(Cant);
                asl_.put("Suma",suma.replace(",","."));
                asl_list.add(asl_);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ((BaseApp)getApplication()).appendLog(e.getMessage(), SalesActivity.this);
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
                SalesActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Call<Boolean> call = terminalAPI.pingService();

                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if(response.body()){
                                    if(menu!=null)
                                        menu.getItem(0).setIcon(ContextCompat.getDrawable(SalesActivity.this, R.drawable.signal_wi_fi_48));
                                }
                                else{
                                    if(menu!=null)
                                        menu.getItem(0).setIcon(ContextCompat.getDrawable(SalesActivity.this, R.drawable.no_signal_wi_fi_48));
                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                if(menu!=null)
                                    menu.getItem(0).setIcon(ContextCompat.getDrawable(SalesActivity.this, R.drawable.no_signal_wi_fi_48));
                            }
                        });
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
    private void printSales() {
        final SharedPreferences SharedPrinters = getSharedPreferences("Printers", MODE_PRIVATE);
        int positionType = SharedPrinters.getInt("Type",0);

        if(positionType == BaseEnum.POS_PRINTER){
            width = BaseApp.getInstance().getWidthPrinterPrint();
            if(width == 0){
                width = SharedPrinters.getInt("WithDisplay",0);
            }
            Call<GetPrintInvoiceResults> call = terminalAPI.getPrintInvoice(InvoiceID);

            call.enqueue(new Callback<GetPrintInvoiceResults>() {
                @Override
                public void onResponse(Call<GetPrintInvoiceResults> call, Response<GetPrintInvoiceResults> response) {
                    GetPrintInvoiceResults results = response.body();

                    if(results != null){
                        if(results.getErrorCode() == 0)
                            printToBluetoothPrinter(results.getImageFile());

                        else{
                            AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
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
                        AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
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
                    Toast.makeText(SalesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        else {
            printers_List_array.clear();
            Call<GetPrintersResult> call = terminalAPI.getPrinters(WorkPlaceGetPrinters);

            call.enqueue(new Callback<GetPrintersResult>() {
                @Override
                public void onResponse(@NonNull Call<GetPrintersResult> call, @NonNull Response<GetPrintersResult> response) {
                    GetPrintersResult result = response.body();
                    if(result != null){
                        if(result.getErrorCode() == 0){
                            List<PrinterResults> printers = result.getPrinters();
                            if(result.getPrinters() == null){
                                AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
                                dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                                dialog.setCancelable(false);
                                dialog.setMessage(getResources().getString(R.string.txt_not_printers_check_back_office));
                                dialog.setPositiveButton(getResources().getString(R.string.txt_save_document), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                if (SalesActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                                    return;
                                }
                                dialog.show();
                            }

                            if(printers.size() == 1){
                                PrinterResults printerResults = printers.get(0);
                                printInvoiceToService(printerResults.getID(), printerResults.getName());
                            }
                            else if (printers.size() > 1) {
                                for (PrinterResults printer : printers) {
                                    HashMap<String, Object> Printers = new HashMap<>();
                                    Printers.put("Name", printer.getName());
                                    Printers.put("Code", printer.getCode());
                                    Printers.put("ID", printer.getID());
                                    printers_List_array.add(Printers);
                                }
                                show_printers();
                            }
                        }
                        else{
                            AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
                            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                            dialog.setCancelable(false);
                            dialog.setMessage("Eroare la descarcarea imprimatelor: " + result.getErrorCode());
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
                        AlertDialog.Builder dialog = new AlertDialog.Builder(SalesActivity.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        dialog.setCancelable(false);
                        dialog.setMessage("Eroare la descarcarea imprimatelor! Nici un raspuns!");
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
                public void onFailure(Call<GetPrintersResult> call, Throwable t) {
                    Toast.makeText(SalesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void printInvoiceToService(String printerId, String printerName){
        Call<ResponseSimple> call = terminalAPI.printInvoice(InvoiceID,printerId);

        call.enqueue(new Callback<ResponseSimple>() {
            @Override
            public void onResponse(Call<ResponseSimple> call, Response<ResponseSimple> response) {
                ResponseSimple result = response.body();
                if(result != null){
                    if(result.getErrorCode() == 0)
                        Toast.makeText(SalesActivity.this, getResources().getString(R.string.msg_imprimare_sales) + " " + printerName, Toast.LENGTH_SHORT).show();
                    else{
                        AlertDialog.Builder input = new AlertDialog.Builder(SalesActivity.this);
                        input.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        input.setCancelable(false);
                        input.setMessage(getResources().getString(R.string.msg_error_code) + result.getErrorCode());
                        input.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = input.create();
                        dialog.show();
                    }
                }
                else{
                    AlertDialog.Builder input = new AlertDialog.Builder(SalesActivity.this);
                    input.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                    input.setCancelable(false);
                    input.setMessage(getResources().getString(R.string.msg_nu_raspuns_server));
                    input.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = input.create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseSimple> call, Throwable t) {
                Toast.makeText(SalesActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    public void showProgressDialog(final String str){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = new ProgressDialog(SalesActivity.this);
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