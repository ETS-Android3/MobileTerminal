package edi.md.mobile.SettingsMenu;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edi.md.mobile.R;
import edi.md.mobile.Sales;
import edi.md.mobile.Settings.ASL;
import edi.md.mobile.Settings.Assortment;
import edi.md.mobile.Utils.ServiceApi;
import edi.md.mobile.Variables;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;

public class WorkPlace extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Button btn_get_workplace,btn_add_folder,btn_delete_all_folder;
    Switch check_stock,show_cod,auto_confirm,show_keyboard;
    TextView txtFolders,txt_user;
    String ip_,port_,UserId;
    ProgressDialog pgH;

    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();
    AlertDialog.Builder builderType;

    String[] kit_lists;
    boolean[] checkedItems;
    Handler handler;

    JSONArray myJSONArray=new JSONArray();
    JSONArray myJSONArrayBool=new JSONArray();
    JSONArray myJSONArrayUid=new JSONArray();
    String WareGUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.header_workplace_activity);
        setContentView(R.layout.activity_work_place);
        Toolbar toolbar = findViewById(R.id.toolbar_workplace);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_workplace);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        btn_add_folder=findViewById(R.id.btn_add_folder_workplace);
        btn_delete_all_folder=findViewById(R.id.btn_delete_folder_workplace);
        btn_get_workplace = findViewById(R.id.btn_change_workplace);
        check_stock = findViewById(R.id.switch_check_stock_worplace);
        show_cod = findViewById(R.id.switch_show_cod_workplace);
        auto_confirm = findViewById(R.id.switch_autoconfirm_workplace);
        show_keyboard = findViewById(R.id.switch_show_keyboard_workplace);
        txtFolders = findViewById(R.id.txt_show_folders_workplace);
        txt_user = findViewById(R.id.txt_user_workplace);
        pgH=new ProgressDialog(WorkPlace.this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
        String selected = CheckUidFolder.getString("selected_Name_Array","[]");

        final SharedPreferences.Editor inpSet = Settings.edit();
        String workplaceName = WorkPlace.getString("Name","Nedeterminat");
        if(workplaceName.equals(""))
            workplaceName ="Nedeterminat";
        btn_get_workplace.setText(workplaceName);

        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        txt_user.setText(User.getString("Name",""));
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        user_workplace.setText(WorkPlace.getString("Name",""));

        boolean autoConfirm = Settings.getBoolean("AutoConfirmTransfer",false);
        boolean show = Settings.getBoolean("ShowKeyBoard",false);
        boolean CheckStock = Settings.getBoolean("CheckStockInput",false);
        boolean ShowCode = Settings.getBoolean("ShowCode",false);

        if (show){
            show_keyboard.setChecked(true);
        }else{
            show_keyboard.setChecked(false);
        }

        if (autoConfirm){
            auto_confirm.setChecked(true);
        }else{
            auto_confirm.setChecked(false);
        }

        if (CheckStock){
            check_stock.setChecked(true);
        }else{
            check_stock.setChecked(false);
        }
        if (ShowCode){
            show_cod.setChecked(true);
        }else{
            show_cod.setChecked(false);
        }

        try {
            myJSONArray = new JSONArray(selected);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        txtFolders.setText("");
        if(myJSONArray.length()>0) {
            for (int i = 0; i < myJSONArray.length(); i++) {
                if (i != 0) {
                    txtFolders.append(",");
                }
                try {
                    txtFolders.append(myJSONArray.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        btn_delete_all_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtFolders.setText("");
                SharedPreferences SaveFolders =getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                SharedPreferences.Editor test = SaveFolders.edit();
                test.putString("b" +
                        "" +
                        "" +
                        "oolean_Array", "[]");//boolJSON
                test.putString("selected_Uid_Array","[]");//selectedUidJSON
                test.putString("selected_Name_Array", "[]");//selectedJSON
                test.apply();
            }
        });
        btn_add_folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean dwnlASL =((Variables)getApplication()).getDownloadASLVariable();
                if(dwnlASL){
                    int mas=0;
                    Variables myapp =(Variables)getApplication();
                    asl_list =myapp.get_AssortimentFolders();
                    mas=asl_list.size();
                    kit_lists=new String[mas];
                    checkedItems = new boolean[mas];
                    for (int i=0;i<mas;i++){
                        checkedItems[i]=false;
                        kit_lists[i]=(String)asl_list.get(i).get("Name");
                    }
                    if(asl_list.size()>0){
                        SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(WorkPlace.this);
                        builder.setTitle("Choose items");
                        String selected = CheckUidFolder.getString("selected_Name_Array","[]");
                        String bolrns = CheckUidFolder.getString("boolean_Array","[]");
                        String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");

                        try {
                            myJSONArrayBool = new JSONArray(bolrns);
                            myJSONArray = new JSONArray(selected);
                            myJSONArrayUid = new JSONArray(selectedUidJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(!bolrns.equals("[]")) {
                            checkedItems = new boolean[myJSONArrayBool.length()];
                            for (int o = 0; o < myJSONArrayBool.length(); o++) {
                                try {
                                    checkedItems[o] = myJSONArrayBool.getBoolean(o);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        builder.setMultiChoiceItems(kit_lists,checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                String name_asl = (String)asl_list.get(which).get("Name");
                                String uid_asl =  (String)asl_list.get(which).get("ID");
                                if(isChecked){
                                    myJSONArray.put(name_asl);
                                    myJSONArrayUid.put(uid_asl);
                                }else{
                                    for (int i=0;i<myJSONArray.length();i++){
                                        try {
                                            String namesArray = myJSONArray.getString(i);
                                            if(namesArray.equals(name_asl)){
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    myJSONArray.remove(i);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    for (int i=0;i<myJSONArrayUid.length();i++){
                                        try {
                                            String uid_aslArray = myJSONArrayUid.getString(i);
                                            if(uid_aslArray.equals(uid_asl)){
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    myJSONArrayUid.remove(i);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                txtFolders.setText("");
                                for(int i=0; i<myJSONArray.length();i++){
                                    if (i!=0){
                                        txtFolders.append(",");
                                    }
                                    try {
                                        txtFolders.append(myJSONArray.getString(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                                SharedPreferences.Editor test = CheckUidFolder.edit();
                                JSONArray booleab = new JSONArray();
                                for (boolean checkedItem : checkedItems) {
                                    booleab.put(checkedItem);
                                }
                                test.putString("boolean_Array", booleab.toString());
                                test.putString("selected_Uid_Array", myJSONArrayUid.toString());
                                test.putString("selected_Name_Array", myJSONArray.toString());
                                test.apply();
                                dialog.dismiss();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }else{
                    DownloadASL();
                }
            }
        });

        show_keyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inpSet.putBoolean("ShowKeyBoard",true);
                    inpSet.apply();
                }else{
                    inpSet.putBoolean("ShowKeyBoard",false);
                    inpSet.apply();
                }
            }
        });
        auto_confirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inpSet.putBoolean("AutoConfirmTransfer",true);
                    inpSet.apply();
                }else{
                    inpSet.putBoolean("AutoConfirmTransfer",false);
                    inpSet.apply();
                }
            }
        });
        check_stock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inpSet.putBoolean("CheckStockInput",true);
                    inpSet.apply();
                }else{
                    inpSet.putBoolean("CheckStockInput",false);
                    inpSet.apply();
                }
            }
        });
        show_cod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inpSet.putBoolean("ShowCode",true);
                    inpSet.apply();
                }else{
                    inpSet.putBoolean("ShowCode",false);
                    inpSet.apply();
                }
            }
        });
        btn_get_workplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage("loading..");
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();
                UserId = User.getString("UserID","");
                ip_=Settings.getString("IP","");
                port_=Settings.getString("Port","");
                if(UserId.equals("")){
                    Intent Logins = new Intent(".LoginMobile");
                    Logins.putExtra("Activity", 6);
                    startActivityForResult(Logins,6);
                }else{
                    getWareHouse();
                }
            }
        });
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if(msg.what == 10) {
                    if (msg.arg1 == 12) {
                        pgH.dismiss();
                        ((Variables)getApplication()).setDownloadASLVariable(true);
                        int mas=0;
                        Variables myapp =(Variables)getApplication();
                        asl_list =myapp.get_AssortimentFolders();
                        mas=asl_list.size();
                        kit_lists=new String[mas];
                        checkedItems = new boolean[mas];
                        for (int i=0;i<mas;i++){
                            checkedItems[i]=false;
                            kit_lists[i]=(String)asl_list.get(i).get("Name");
                        }
                        if(asl_list.size()>0){
                            SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                            AlertDialog.Builder builder = new AlertDialog.Builder(WorkPlace.this);
                            builder.setTitle("Choose items");
                            String selected = CheckUidFolder.getString("selected_Name_Array","[]");
                            String bolrns = CheckUidFolder.getString("boolean_Array","[]");
                            String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");

                            try {
                                myJSONArrayBool = new JSONArray(bolrns);
                                myJSONArray = new JSONArray(selected);
                                myJSONArrayUid = new JSONArray(selectedUidJSON);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(!bolrns.equals("[]")) {
                                checkedItems = new boolean[myJSONArrayBool.length()];
                                for (int o = 0; o < myJSONArrayBool.length(); o++) {
                                    try {
                                        checkedItems[o] = myJSONArrayBool.getBoolean(o);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            builder.setMultiChoiceItems(kit_lists,checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    String name_asl = (String)asl_list.get(which).get("Name");
                                    String uid_asl =  (String)asl_list.get(which).get("ID");
                                    if(isChecked){
                                        myJSONArray.put(name_asl);
                                        myJSONArrayUid.put(uid_asl);
                                    }else{
                                        for (int i=0;i<myJSONArray.length();i++){
                                            try {
                                                String namesArray = myJSONArray.getString(i);
                                                if(namesArray.equals(name_asl)){
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                        myJSONArray.remove(i);
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        for (int i=0;i<myJSONArrayUid.length();i++){
                                            try {
                                                String uid_aslArray = myJSONArrayUid.getString(i);
                                                if(uid_aslArray.equals(uid_asl)){
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                        myJSONArrayUid.remove(i);
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            });
                            builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    txtFolders.setText("");
                                    for(int i=0; i<myJSONArray.length();i++){
                                        if (i!=0){
                                            txtFolders.append(",");
                                        }
                                        try {
                                            txtFolders.append(myJSONArray.getString(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                                    SharedPreferences.Editor test = CheckUidFolder.edit();
                                    JSONArray booleab = new JSONArray();
                                    for (boolean checkedItem : checkedItems) {
                                        booleab.put(checkedItem);
                                    }
                                    test.putString("boolean_Array", booleab.toString());
                                    test.putString("selected_Uid_Array", myJSONArrayUid.toString());
                                    test.putString("selected_Name_Array", myJSONArray.toString());
                                    test.apply();
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                }else{
                    String t = msg.obj.toString();
                    pgH.dismiss();
                    ((Variables)getApplication()).setDownloadASLVariable(false);
                    android.app.AlertDialog.Builder failureAsl = new android.app.AlertDialog.Builder(WorkPlace.this);
                    failureAsl.setCancelable(false);
                    failureAsl.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                    failureAsl.setMessage(getResources().getString(R.string.msg_eroare_download_asl) + t + "\n" + getResources().getString(R.string.msg_reload_download_asl));
                    failureAsl.setPositiveButton(getResources().getString(R.string.toggle_btn_check_remain_da), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DownloadASL();
                        }
                    });
                    failureAsl.setNegativeButton(getResources().getString(R.string.toggle_btn_check_remain_nu), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    failureAsl.show();
                }
            }

        };

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==6){
            if(resultCode==RESULT_OK){
                SharedPreferences LogIn = getSharedPreferences("User", MODE_PRIVATE);
                UserId = LogIn.getString("UserID","");
                txt_user.setText(LogIn.getString("Name",""));
                getWareHouse();
            }else{
                pgH.dismiss();
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_workplace);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_workplace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close_workplace) {
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
            finish();
        } else if (id == R.id.menu_workplace) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 8);
            startActivity(Logins);
            finish();
        } else if (id == R.id.menu_printers) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 9);
            startActivity(Logins);
            finish();
        } else if (id == R.id.menu_securitate) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", 10);
            startActivity(Logins);
            finish();
        } else if (id == R.id.menu_about) {
            Intent MenuConnect = new Intent(".MenuAbout");
            startActivity(MenuConnect);
            finish();
        } else if (id == R.id.menu_exit) {
//            SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
//            WorkPlace.edit().clear().apply();
            finishAffinity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_workplace);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void show_WareHouse(){
        SimpleAdapter simpleAdapterType = new SimpleAdapter(WorkPlace.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(WorkPlace.this);
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
                String WareUid= String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareName= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Work Place", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("Name",WareName);
                addWareHouse.putString("Uid",WareUid);
                addWareHouse.putString("Code",WareCode);
                addWareHouse.apply();
                WareGUid=WareUid;

                btn_get_workplace.setText(WareName);
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
    public void DownloadASL(){
        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
        pgH.setIndeterminate(true);
        pgH.setCancelable(false);
        pgH.show();

        txtFolders.setText("");
        SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
        SharedPreferences.Editor test = CheckUidFolder.edit();
        test.putString("boolean_Array", "[]");
        test.putString("selected_Uid_Array","[]");
        test.putString("selected_Name_Array", "[]");
        test.apply();

        Thread t = new Thread(new Runnable() {
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.MINUTES)
                        .readTimeout(4, TimeUnit.MINUTES)
                        .writeTimeout(2, TimeUnit.MINUTES)
                        .build();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://"+ ip_+ ":"+port_)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build();
                ServiceApi assortiment_API = retrofit.create(ServiceApi.class);
                final Call<ASL> assortiment = assortiment_API.getAssortiment(UserId,WareGUid);
                assortiment.enqueue(new Callback<ASL>() {
                    @Override
                    public void onResponse(Call<ASL> call, Response<ASL> response) {

                        ASL assortiment_body = response.body();
                        List<Assortment> assortmentListData = assortiment_body.getAssortments();

                        Variables myapp =((Variables)getApplication());

                        //myapp.SaveAsortment(assortmentListData);
                        int mas=0;
                        for (int i=0; i<assortmentListData.size();i++){
                            Boolean is_folder = assortmentListData.get(i).getIsFolder();
                            String barcode = assortmentListData.get(i).getBarCode();
                            String unit = assortmentListData.get(i).getUnit();
                            String unitin_package = assortmentListData.get(i).getUnitInPackage();
                            if (barcode==null)
                                assortmentListData.get(i).setBarCode("null");
                            if (unit==null)
                                assortmentListData.get(i).setUnit("null");
                            if (unitin_package==null)
                                assortmentListData.get(i).setUnitInPackage("null");
                            String uid_asl = assortmentListData.get(i).getAssortimentID();

                            myapp.add_AssortimentID(uid_asl,assortmentListData.get(i));
                            if(is_folder){
                                HashMap<String, Object> asl_folder = new HashMap<>();

                                String asl_name = assortmentListData.get(i).getName();

                                myapp.add_AssortimentID(uid_asl,assortmentListData.get(i));

                                asl_folder.put("Name", asl_name);
                                asl_folder.put("ID", uid_asl);
                                asl_list.add(asl_folder);
                                mas++;
                            }
                        }
                        kit_lists=new String[mas];
                        checkedItems = new boolean[mas];
                        for (int i=0;i<mas;i++){
                            checkedItems[i]=false;
                            kit_lists[i]=(String)asl_list.get(i).get("Name");
                        }
                        handler.obtainMessage(10, 12, -1).sendToTarget();
                    }

                    @Override
                    public void onFailure(Call<ASL> call, Throwable t) {
                        handler.obtainMessage(20,t.getMessage()).sendToTarget();
                    }
                });





            }
        });
        t.start();
    }
    class AsyncTask_WareHouse extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),WorkPlace.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(!response.equals("false") || response != null) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    int ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            String WareHouses =  responseWareHouse.getString("Warehouses");
                            if (WareHouses == null || WareHouses.equals("null")){
                                pgH.dismiss();
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(WorkPlace.this);
                                alertDialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                                alertDialog.setMessage(getResources().getString(R.string.msg_list_warehouses_null));
                                alertDialog.setCancelable(false);
                                alertDialog.setPositiveButton(getResources().getString(R.string.msg_dialog_close), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
                                        WorkPlace.edit().clear().apply();
                                        finishAffinity();
                                    }
                                });
                                alertDialog.setNegativeButton(getResources().getString(R.string.msg_dialog_close_ramine), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                alertDialog.show();
                            }else{
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
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(),WorkPlace.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(WorkPlace.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(),WorkPlace.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(WorkPlace.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
