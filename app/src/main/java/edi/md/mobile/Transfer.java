package edi.md.mobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetAssortiment;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.TransferFromOneWareHouseToAnother;

public class Transfer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    ImageButton btn_write_barcode,btn_delete,btn_open_asl_list;
    TextView txt_input_barcode,txt_out,txt_inc,txtBarcode_introdus;
    ProgressBar pgBar;
    Button btn_ok,btn_cancel;
    ListView list_of_transfer;

    AlertDialog.Builder builderType;
    SimpleAdapter simpleAdapterASL;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();

    String ip_,port_,UserId,WareUid,uid_selected,barcode_introdus,WareIDIn;
    ProgressDialog pgH;
    TimerTask timerTaskSync;
    Timer sync;

    Menu menu;
    JSONObject json_asl,sendInvoice,sendAssortiment;
    JSONArray json_array;
    final boolean[] show_keyboard = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_transfer);

        Toolbar toolbar = findViewById(R.id.toolbar_transfer);
        setSupportActionBar(toolbar);

        btn_write_barcode = findViewById(R.id.btn_write_manual_transfer);
        txt_input_barcode = findViewById(R.id.txt_input_barcode_transfer);
        btn_delete = findViewById(R.id.btn_delete_transfer);
        btn_open_asl_list = findViewById(R.id.btn_touch_open_asl_transfer);
        txt_out = findViewById(R.id.txtDepozit_expeditor_transfer);
        txt_inc = findViewById(R.id.txtDepozit_destinar_transfer);
        pgBar = findViewById(R.id.progressBar_transfer);
        txtBarcode_introdus = findViewById(R.id.txtBarcode_introdus_transfer);
        btn_cancel = findViewById(R.id.btn_cancel_transfer);
        btn_ok = findViewById(R.id.btn_ok_transfer);
        list_of_transfer = findViewById(R.id.LL_list_asl_transfer);
        pgH=new ProgressDialog(Transfer.this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_transfer);
        NavigationView navigationView = findViewById(R.id.nav_view_transfer);

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

        final String WorkPlaceID = WorkPlace.getString("Uid","0");

        json_array=new JSONArray();
        json_asl=new JSONObject();

        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        getWareHouse();
        initList_asl();
        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,2000,2000);

        txt_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWareHouseOUT();
            }
        });

        txt_inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWareHouseINC();
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


        txt_input_barcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    txt_input_barcode.requestFocus();
                    pgBar.setVisibility(ProgressBar.VISIBLE);
                    show_keyboard[0] = false;
                    sendAssortiment = new JSONObject();
                    try {
                        sendAssortiment.put("AssortmentIdentifier", txt_input_barcode.getText().toString());
                        sendAssortiment.put("UserID", UserId);
                        sendAssortiment.put("WarehouseID", WareUid);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                    }
                    barcode_introdus = txt_input_barcode.getText().toString();
                    txt_input_barcode.setText("");
                    URL getASL = GetAssortiment(ip_, port_);
                    new AsyncTask_GetAssortiment().execute(getASL);
                }
                else if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
                    txt_input_barcode.requestFocus();
                    if (!txt_input_barcode.getText().toString().equals("")) {
                        txt_input_barcode.requestFocus();
                        pgBar.setVisibility(ProgressBar.VISIBLE);
                        show_keyboard[0] = false;
                        sendAssortiment = new JSONObject();
                        try {
                            sendAssortiment.put("AssortmentIdentifier", txt_input_barcode.getText().toString());
                            sendAssortiment.put("UserID", UserId);
                            sendAssortiment.put("ShowStocks",true);
                            sendAssortiment.put("WarehouseID", WareUid);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                        }
                        barcode_introdus = txt_input_barcode.getText().toString();
                        txt_input_barcode.setText("");
                        URL getASL = GetAssortiment(ip_, port_);
                        new AsyncTask_GetAssortiment().execute(getASL);
                    }
                }
                return false;
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(json_array.length()==0){
                    finish();
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
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
        });
        list_of_transfer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                uid_selected=(String)asl_list.get(position).get("Uid");
            }
        });
        btn_open_asl_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AddingASL = new Intent(".AssortmentMobile");
                AddingASL.putExtra("ActivityCount", 141);
                if(WorkPlaceID.equals("0")){
                    AddingASL.putExtra("WareID",WareUid);
                }
                startActivityForResult(AddingASL, 222);

            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();

                JSONArray sendArr= new JSONArray();
                for (int i = 0; i < json_array.length(); i++) {
                    JSONObject json = null;
                    try {
                        json = json_array.getJSONObject(i);
                        String Uid = json.getString("AssortimentUid");
                        String Cant = json.getString("Count");
                        JSONObject sendObj = new JSONObject();
                        sendObj.put("Assortiment",Uid);
                        sendObj.put("Quantity",Cant);
                        sendArr.put(sendObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                    }
                }
                sendInvoice = new JSONObject();
                Boolean autoConfirm = Settings.getBoolean("AutoConfirmTransfer",false);
                try {
                    sendInvoice.put("Confirm",autoConfirm);
                    sendInvoice.put("Lines",sendArr);
                    sendInvoice.put("UserID",UserId);
                    sendInvoice.put("TransferCode","Android Terminal");
                    sendInvoice.put("ToWarehouseID",WareIDIn);
                    sendInvoice.put("FromWarehouseID",WareUid);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                }
                URL generateSave = TransferFromOneWareHouseToAnother(ip_,port_);
                new AsyncTask_SaveTransfer().execute(generateSave);
            }
        });

    }
    public void show_WareHouse(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Transfer.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Transfer.this);
        builderType.setTitle(getResources().getString(R.string.msg_transfer_from_depozit));
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

                SharedPreferences WareHouse = getSharedPreferences("Ware House Out", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                txt_out.setText(WareName);
                WareUid = WareGUid;
                show_stockIn();
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_stockOutClick(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Transfer.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Transfer.this);
        builderType.setTitle(getResources().getString(R.string.msg_transfer_from_depozit));
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

                SharedPreferences WareHouse = getSharedPreferences("Ware House Out", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                txt_out.setText(WareName);
                WareUid = WareGUid;

            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_stockIn(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Transfer.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Transfer.this);
        builderType.setTitle(getResources().getString(R.string.msg_transfer_to_depozit));
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

                SharedPreferences WareHouse = getSharedPreferences("Ware House In", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();
                txt_inc.setText(WareName);
                WareIDIn = WareGUid;
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void show_stockInClick(){
        //adapter = new ArrayAdapter<>(Sales.this,android.R.layout.simple_list_item_1, stock_List_array);
        SimpleAdapter simpleAdapterType = new SimpleAdapter(Transfer.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Transfer.this);
        builderType.setTitle(getResources().getString(R.string.msg_transfer_to_depozit));
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

                SharedPreferences WareHouse = getSharedPreferences("Ware House In", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();
                txt_inc.setText(WareName);
                WareIDIn = WareGUid;
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
    public void getWareHouseINC(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouseINC().execute(getWareHouse);
    }
    public void getWareHouseOUT(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouseOUT().execute(getWareHouse);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_transfer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(json_array.length()==0){
                finish();
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transfer, menus);
        this.menu = menus;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_close_transfer) {
            if(json_array.length()==0){
                finish();
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
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
            return true;
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
            if(json_array.length()==0){
                finish();
            }else{
                AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_transfer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                Transfer.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        URL generatedURL = Ping(ip_, port_);
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

    }
    public void initList_asl(){
        try {
            for (int i=0;i<json_array.length();i++){
                JSONObject json= json_array.getJSONObject(i);
                HashMap<String, Object> asl_ = new HashMap<>();
                String Name = json.getString("AssortimentName");
                String Cant = json.getString("Count");
                String Uid = json.getString("AssortimentUid");

                asl_.put("Name",Name);
                asl_.put("Cant",Cant);
                asl_.put("Uid",Uid);
                asl_list.add(asl_);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(), Transfer.this);
        }
        simpleAdapterASL = new SimpleAdapter(this, asl_list,R.layout.show_asl_invoice, new String[]{"Name","Cant"},
                new int[]{R.id.textName_asl_invoice,R.id.textCantitate_asl_invoice});
        list_of_transfer.setAdapter(simpleAdapterASL);
    }
    public String getResponseFromURLSendB(URL send_bills){
        StringBuilder data = new StringBuilder();
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
                data.append(current);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(), Transfer.this);
        } finally {
            assert send_bill_Connection != null;
            send_bill_Connection.disconnect();
        }
        return data.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25) {
            txt_input_barcode.requestFocus();
            if (resultCode == RESULT_CANCELED) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            } else if (resultCode == RESULT_OK) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();
                SharedPreferences sPref_saveASL = getSharedPreferences("Transfer", MODE_PRIVATE);
                String response = sPref_saveASL.getString("AssortmentTransferAdded", "{}");
                try {
                    JSONObject json = new JSONObject(response);
                    String UidAsl= json.getString("AssortimentUid");
                    String count = json.getString("Count");
                    boolean isExtist = false;
                    if (json_array.length()!=0) {
                        for (int i = 0; i < json_array.length(); i++) {
                            JSONObject object = json_array.getJSONObject(i);
                            String AssortimentUid = object.getString("AssortimentUid");
                            String CountExist = object.getString("Count");
                            Integer countInt = Integer.valueOf(CountExist);
                            Integer CountAdd = Integer.valueOf(count);

                            if (AssortimentUid.contains(UidAsl)) {
                                Integer CountNew = CountAdd + countInt;
                                String countStr = String.valueOf(CountNew);
                                object.put("Count", countStr);
                                isExtist=true;
                            }
                        }
                        if (!isExtist){
                            json_array.put(json);
                        }
                    }else{
                        json_array.put(json);
                    }
                    asl_list.clear();
                    initList_asl();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                txt_input_barcode.requestFocus();
            }
        }else if(requestCode==222) {
            if (resultCode == RESULT_CANCELED) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
            } else if (resultCode == RESULT_OK) {
                txt_input_barcode.setText("");
                txt_input_barcode.requestFocus();
                asl_list.clear();
                SharedPreferences sPref_saveASL = getSharedPreferences("Transfer", MODE_PRIVATE);
                String response = sPref_saveASL.getString("AssortmentTransferAddedArray", "[]");
                try {
                    JSONArray array_from_touch = new JSONArray(response);
                    if (json_array.length() != 0) {
                        boolean isExtist = false;
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            String AssortimentUid_from_touch = object_from_touch.getString("AssortimentUid");
                            String count = object_from_touch.getString("Count");
                            for (int k = 0; k < json_array.length(); k++) {
                                JSONObject object = json_array.getJSONObject(k);
                                String AssortimentUid = object.getString("AssortimentUid");
                                String CountExist = object.getString("Count");
                                Integer countInt = Integer.valueOf(CountExist);
                                Integer CountAdd = Integer.valueOf(count);

                                if (AssortimentUid.contains(AssortimentUid_from_touch)) {
                                    Integer CountNew = CountAdd + countInt;
                                    String countStr = String.valueOf(CountNew);
                                    object.put("Count", countStr);
                                    isExtist = true;
                                    break;
                                }
                            }
                            if (!isExtist) {
                                json_array.put(object_from_touch);
                            }
                        }
                    } else {
                        for (int i = 0; i < array_from_touch.length(); i++) {
                            JSONObject object_from_touch = array_from_touch.getJSONObject(i);
                            json_array.put(object_from_touch);
                        }
                    }
                    asl_list.clear();
                    initList_asl();
                } catch (JSONException e) {
                    e.printStackTrace();
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
            ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
        } finally {
            send_bill_Connection.disconnect();
        }
        return data.toString();
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
                menu.getItem(0).setIcon(ContextCompat.getDrawable(Transfer.this, R.drawable.signal_wi_fi_48));
            }else {
                this.cancel(true);
                menu.getItem(0).setIcon(ContextCompat.getDrawable(Transfer.this, R.drawable.no_signal_wi_fi_48));
            }
        }
    }
    class AsyncTask_SaveTransfer extends AsyncTask<URL, String, String> {

        @Override
        protected String doInBackground(URL... urls) {
            String responseINC="";
            responseINC=getResponseFromURLSendB(urls[0]);
            return responseINC;
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("")){
                try {
                    pgH.dismiss();
                    JSONObject respo = new JSONObject(response);
                    boolean confirm = respo.getBoolean("Confirmed");
                    int errCodes = respo.getInt("ErrorCode");
                    if (errCodes == 0) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        dialog.setCancelable(false);
                        if(confirm) {
                            dialog.setMessage(getResources().getString(R.string.msg_invoice_saved));
                        }else{
                            dialog.setMessage(getResources().getString(R.string.msg_transfer_saved_not_confirmed));
                        }
                        dialog.setPositiveButton(getResources().getString(R.string.txt_accept_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
                        dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        dialog.setCancelable(false);
                        dialog.setMessage(getResources().getString(R.string.msg_document_notsaved_cod_error) + errCodes);
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
                    ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                }
            }else{
                pgH.dismiss();
                AlertDialog.Builder dialog = new AlertDialog.Builder(Transfer.this);
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
    class AsyncTask_WareHouse extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
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
                            ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Transfer.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Transfer.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_WareHouseINC extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
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
                            show_stockInClick();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Transfer.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Transfer.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_WareHouseOUT extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
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
                            show_stockOutClick();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Transfer.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Transfer.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Transfer.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
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
                        String Remain = responseAssortiment.getString("Remain");
                        String Marking = responseAssortiment.getString("Marking");
                        String Codes = responseAssortiment.getString("Code");
                        String Uid = responseAssortiment.getString("AssortimentID");
                        String Barcodes = responseAssortiment.getString("BarCode");

                        pgBar.setVisibility(ProgressBar.INVISIBLE);

                        Intent sales = new Intent(".CountTransferMobile");
                        sales.putExtra("BarCode", Barcodes);
                        sales.putExtra("Price", Price);
                        sales.putExtra("Names", Names);
                        sales.putExtra("Remain", Remain);
                        sales.putExtra("Marking", Marking);
                        sales.putExtra("Codes", Codes);
                        sales.putExtra("ID", Uid);
                        startActivityForResult(sales, 25);
                    } else {
                        pgBar.setVisibility(ProgressBar.INVISIBLE);
                        txtBarcode_introdus.setText(barcode_introdus +" - " +getResources().getString(R.string.txt_depozit_nedeterminat));
                        txt_input_barcode.requestFocus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),Transfer.this);
                }
            }else{
                Toast.makeText(Transfer.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
                ((Variables)getApplication()).appendLog(response,Transfer.this);
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                txtBarcode_introdus.setText(barcode_introdus +" - " +getResources().getString(R.string.txt_depozit_nedeterminat));
                txt_input_barcode.requestFocus();
            }
        }
    }
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            View mDecorView = getWindow().getDecorView();
//            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }

}
