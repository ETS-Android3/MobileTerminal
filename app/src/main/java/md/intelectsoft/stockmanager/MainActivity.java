package md.intelectsoft.stockmanager;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.WarehouseList;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.Utils.UpdateHelper;
import md.intelectsoft.stockmanager.Utils.UpdateInformation;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener , UpdateHelper.OnUpdateCheckListener {
    Button btn_check_price,btn_sales,btn_invoice,btn_transfer,btn_inventory,btn_stock_asortment, btn_set_barcodes,buttonCreateItem;
    Integer checkPrice = 1,salesDoc = 2, invoiceDoc = 3,transferIntern = 4,inventAr = 5,stock_asl = 7,workplaces = 8,printers = 9,securitate = 10, setBarcode = 147;
    TimerTask timerTaskSync;
    Timer sync;
    Boolean pingTest = false;
    private Menu menu;
    NavigationView navigationView;

    TextView userNameView, textWorkplace;

    private ProgressDialog progressDialog;
    TerminalAPI terminalAPI;
    String userID, workPlaceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences Setting = getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = Setting.getString("Language", "ru");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UpdateHelper.with(this).onUpdateCheck(this).check();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_main);
        btn_check_price=findViewById(R.id.btn_check_price);
        btn_sales = findViewById(R.id.btn_sales);
        btn_invoice = findViewById(R.id.btn_invoice);
        btn_transfer = findViewById(R.id.btn_transfer);
        btn_inventory = findViewById(R.id.btn_inventory);
        btn_stock_asortment = findViewById(R.id.btn_stock_assortment);
        btn_set_barcodes = findViewById(R.id.btn_set_assortment_barcode);
        buttonCreateItem = findViewById(R.id.btn_create_assortment);

        View headerLayout = navigationView.getHeaderView(0);
        userNameView = (TextView) headerLayout.findViewById(R.id.txt_name_of_user_main);
        textWorkplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user_main);
        progressDialog = new ProgressDialog(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme);

        String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
        String workPlaceName = SPFHelp.getInstance().getString("WorkPlaceName", null);
        String userName = SPFHelp.getInstance().getString("UserName","");
        userID = SPFHelp.getInstance().getString("UserId","");

        terminalAPI = TerminalRetrofitClient.getApiTerminalService(uri);

        if(workPlaceName == null){
            getWareHousesList(userID);
        }

        userNameView.setText(userName);
        textWorkplace.setText(workPlaceName == null ? "Nedeterminat" : workPlaceName);

        Button log_out = (Button) headerLayout.findViewById(R.id.btn_log_out_main);
        log_out.setOnClickListener(v -> {
            ((BaseApp) getApplication()).setUserAuthentificate(false);
            Map<String, String> userMap = new HashMap<>();
            userMap.put("UserName", "");
            userMap.put("UserId", "");

            SPFHelp.getInstance().putStrings(userMap);
            startActivity(new Intent(MainActivity.this, AuthorizeActivity.class));
        });

        btn_check_price.setOnClickListener(v -> {
            if(pingTest) {
                Intent invoice = new Intent(MainActivity.this, CheckPriceActivity.class);
                startActivity(invoice);
            }else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });
        btn_sales.setOnClickListener(v -> {
            if(pingTest) {
                Intent invoice = new Intent(this, SalesActivity.class);
                startActivity(invoice);
            }
            else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });
        btn_invoice.setOnClickListener(v -> {
            if(pingTest) {
                Intent invoice = new Intent(".InvoiceMobile");
                startActivity(invoice);
            }
            else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });
        btn_transfer.setOnClickListener(v -> {
            if(pingTest) {
                Intent invoice = new Intent(".TransferMobile");
                startActivity(invoice);
            }else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });
        btn_inventory.setOnClickListener(v -> {
            if(pingTest) {
                Intent invoice = new Intent(".RevisionMobile");
                startActivity(invoice);
            }else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();

        });
        buttonCreateItem.setOnClickListener(view -> {
            Boolean keyLicense = false;
            String license = SPFHelp.getInstance().getString("LicenseID","");
            if(!license.isEmpty()){

            keyLicense=true;
            }
            if(pingTest) {
                if(keyLicense) {
                    Boolean CheckLogin = !SPFHelp.getInstance().getString("UserId","").isEmpty();
//                    if (CheckLogin == null) {
//                        CheckLogin = false;
//                        ((Variables) getApplication()).setUserAuthentificate(false);
//                    }
                    if (CheckLogin) {
                        Intent invoice = new Intent(this, CreateAssortment.class);
                        startActivity(invoice);
                    } else {
                        Intent Logins = new Intent(".LoginMobile");
                        Logins.putExtra("Activity", 12);
                        startActivity(Logins);
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
            }
        });
        btn_stock_asortment.setOnClickListener(v -> {
            if (pingTest) {
                Intent invoice = new Intent(".StockAssortmentMobile");
                startActivity(invoice);
            } else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });
        btn_set_barcodes.setOnClickListener(v-> {
            if(pingTest) {
                Intent assortment = new Intent(".AssortmentMobile");
                assortment.putExtra("Activity", setBarcode);
                assortment.putExtra("ActivityCount", 525);
                assortment.putExtra("WareID",SPFHelp.getInstance().getString("WorkPlaceId",""));
                startActivity(assortment);
            }
            else
                Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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

                        SimpleAdapter simpleAdapterWareHouses = new SimpleAdapter(MainActivity.this, wareListToDialog ,android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
                        progressDialog.dismiss();

                        new MaterialAlertDialogBuilder(MainActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Select your workplace:")
                                .setCancelable(false)
                                .setSingleChoiceItems(simpleAdapterWareHouses, -1, (dialog, which) -> {
                                    workPlaceName = wareListToDialog.get(which).get("name");
                                    String id = wareListToDialog.get(which).get("id");

                                    SPFHelp.getInstance().putString("WorkPlaceName", workPlaceName);
                                    SPFHelp.getInstance().putString("WorkPlaceId", id);

                                    textWorkplace.setText(workPlaceName);
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", (dialog, which) -> {
                                    finish();
                                })
                                .show();

                    }
                    else{
                        progressDialog.dismiss();
                        new MaterialAlertDialogBuilder(MainActivity.this)
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
                    Toast.makeText(MainActivity.this,getResources().getString(R.string.msg_error_code) + result.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetWarehousesListResult> call, Throwable t) {
                progressDialog.dismiss();

                new MaterialAlertDialogBuilder(MainActivity.this)
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        getMenuInflater().inflate(R.menu.main, menus);
        this.menu = menus;
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.menu_conect_main) {
//            Intent MenuConnect = new Intent(".MenuConnect");
//            startActivity(MenuConnect);
//        } else
        if (id == R.id.menu_workplace_main) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", workplaces);
            startActivity(Logins);
        } else if (id == R.id.menu_printers_main) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", printers);
            startActivity(Logins);
        } else if (id == R.id.menu_securitate_main) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", securitate);
            startActivity(Logins);
        } else if (id == R.id.menu_about_main) {
            Intent MenuConnect = new Intent(".MenuAbout");
            startActivity(MenuConnect);
        }else if(id == R.id.menu_help_main) {
//            Intent MenuHelp = new Intent(MainActivity.this, HelpMain.class);
//            startActivity(MenuHelp);
        }
        else if (id == R.id.menu_exit_main) {
            finishAffinity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startTimetaskSync(){
        timerTaskSync = new TimerTask() {
            @Override
            public void run() {
                Call<Boolean> call = terminalAPI.pingService();
                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if(response != null){
                            boolean result = false;
                            if(response.body() != null)
                                result = response.body();
                            if(result){
                                pingTest = true;
                                if(menu != null)
                                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.signal_wi_fi_48));
                            }
                            else{
                                pingTest = false;
                                if(menu != null)
                                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                            }
                        }
                        else {
                            pingTest = false;
                            if (menu != null)
                                menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        pingTest = false;
                        if(menu != null)
                            menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                    }
                });
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sync = new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,100,5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BaseApp.getInstance().setUserAuthentificate(false);
        BaseApp.getInstance().setDownloadASLVariable(false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        boolean recreare = ((BaseApp) getApplication()).getIsRecreate();
        if(recreare){
            ((BaseApp) getApplication()).setRecreate(false);
            recreate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerTaskSync.cancel();
        sync.cancel();
    }

    @Override
    public void onUpdateCheckListener(UpdateInformation information) {
        boolean update = information.isUpdate();

        if(update && !information.getNewVerion().equals(information.getCurrentVersion())){
            android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this,R.style.ThemeOverlay_AppCompat_Dialog_Alert_TestDialogTheme)
                    .setTitle("New version " + information.getNewVerion() + " available")
                    .setMessage("Please update to new version to continue use.Current version: " + information.getCurrentVersion())
                    .setPositiveButton("UPDATE",(dialogInterface, i) -> {
                        progressDialog.setMessage("download new version...");
                        progressDialog.setIndeterminate(true);
                        progressDialog.show();
                        downloadAndInstallApk(information.getUrl());
                    })
                    .setNegativeButton("No,thanks", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .create();
            alertDialog.show();
        }
    }

    private void downloadAndInstallApk(String url){
        //get destination to update file and set Uri
        //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better

        String destination = Environment.getExternalStorageDirectory()+ "/IntelectSoft";
        String fileName = "/mobileterminal.apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();

        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Download new version...");
        request.setTitle("MobileTerminal update");

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                progressDialog.dismiss();
                File file = new File(Environment.getExternalStorageDirectory()+ "/IntelectSoft","/mobileterminal.apk"); // mention apk file path here

                Uri uri = FileProvider.getUriForFile(MainActivity.this, md.intelectsoft.stockmanager.BuildConfig.APPLICATION_ID + ".provider",file);
                if(file.exists()){
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(install);
                }
                unregisterReceiver(this);
                finish();

            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
