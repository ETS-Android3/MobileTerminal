package edi.md.mobile.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import edi.md.mobile.CreateAssortmentActivity;
import edi.md.mobile.NetworkUtils.ApiRetrofit;
import edi.md.mobile.NetworkUtils.Services.CommandService;
import edi.md.mobile.R;
import edi.md.mobile.Variables;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Button btn_check_price,btn_sales,btn_invoice,btn_transfer,btn_inventory,btn_stock_asortment, btn_set_barcodes, buttonCreateItem;
    Integer checkPrice = 1,salesDoc = 2, invoiceDoc = 3,transferIntern = 4,inventAr = 5,stock_asl = 7,workplaces = 8,printers = 9,securitate = 10, setBarcode = 147;
    TimerTask timerTaskSync;
    Timer sync;
    Boolean pingTest=false,keyLicense;
    SharedPreferences Settings;
    private Menu menu;
    NavigationView navigationView;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 12 && grantResults.length == 7) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
            } else if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
            }else if(grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
            }else if(grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
            }else if(grantResults[4] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 5);
            }else if(grantResults[5] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 6);
            }else if(grantResults[6] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 7);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_COARSE_LOCATION

                },
                12);
    }

    private void AskForPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }
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

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        //ask necessary permisions
        AskForPermissions();

        ((Variables)getApplication()).appendLog("Aplication starting",MainActivity.this);
        Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences.Editor inpSet = Settings.edit();

        String pin_user = Settings.getString("PinCod","-*");

        if(pin_user.equals("-*")){
            inpSet.putString("PinCod","0000");
            inpSet.apply();
        }
        inpSet.putString("PinCodAdmin","202827160685");
        inpSet.apply();

        sync=new Timer();
        startTimetaskSync();
        sync.schedule(timerTaskSync,100,2000);

        View headerLayout = navigationView.getHeaderView(0);

        final TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user_main);
        useremail.setText(User.getString("Name",""));
        final TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user_main);
        user_workplace.setText(WorkPlace.getString("Name","Nedeterminat"));
        Button log_out = (Button)headerLayout.findViewById(R.id.btn_log_out_main);

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Variables) getApplication()).setUserAuthentificate(false);
                 User.edit().clear().apply();
                 useremail.setText("");
            }
        });

        btn_check_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".CheckPriceMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", checkPrice);
                            startActivity(Logins);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        buttonCreateItem.setOnClickListener(view -> {
            keyLicense=Settings.getBoolean("Key",false);
            if(pingTest) {
                if(keyLicense) {
                    Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                    if (CheckLogin == null) {
                        CheckLogin = false;
                        ((Variables) getApplication()).setUserAuthentificate(false);
                    }
                    if (CheckLogin) {
                        Intent invoice = new Intent(this, CreateAssortmentActivity.class);
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
        btn_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".SalesMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", salesDoc);
                            startActivity(Logins);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".InvoiceMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", invoiceDoc);
                            startActivity(Logins);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".TransferMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", transferIntern);
                            startActivity(Logins);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".RevisionMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", inventAr);
                            startActivity(Logins);
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_stock_asortment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense = Settings.getBoolean("Key", false);
                if (pingTest) {
                    if (keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setUserAuthentificate(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".StockAssortmentMobile");
                            startActivity(invoice);
                        }
                        else{
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", stock_asl);
                            startActivity(Logins);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_set_barcodes.setOnClickListener(v-> {
                keyLicense = Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = Variables.getInstance().getUserAuthentificate();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            Variables.getInstance().setUserAuthentificate(false);
                        }

                        String WareID = WorkPlace.getString("Uid", "0");

                        if(WareID.equals("0") || WareID.equals("")){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                            dialog.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                            dialog.setCancelable(false);
                            dialog.setMessage("Не выбрано рабочее место!");
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();
                        }
                        else{
                            if (CheckLogin) {
                                Intent assortment = new Intent(".AssortmentMobile");
                                assortment.putExtra("Activity", setBarcode);
                                assortment.putExtra("ActivityCount", 525);
                                assortment.putExtra("WareID",WareID);
                                startActivity(assortment);
                            } else {
                                Intent Logins = new Intent(".LoginMobile");
                                Logins.putExtra("Activity", setBarcode);
                                Logins.putExtra("ActivityCount", 525);
                                Logins.putExtra("WareID",WareID);
                                startActivity(Logins);
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Licenta gresita!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Nu este legatura cu serverul.", Toast.LENGTH_SHORT).show();
                }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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

        if (id == R.id.menu_conect_main) {
            Intent MenuConnect = new Intent(".MenuConnect");
            startActivity(MenuConnect);
        } else if (id == R.id.menu_workplace_main) {
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
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommandService commandService = ApiRetrofit.getCommandService(MainActivity.this);

                        Call<Boolean> call = commandService.pingService();

                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if(response != null){
                                    boolean result = false;
                                    if(response.body() != null)
                                        result = response.body();
                                    if(result){
                                        pingTest=true;
                                        if(menu!=null)
                                            menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.signal_wi_fi_48));
                                    }
                                    else{
                                        pingTest= false;
                                        if(menu!=null)
                                            menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                                    }
                                }
                                else{
                                    pingTest= false;
                                    if(menu!=null)
                                        menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                                }

                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                if(menu!=null)
                                    menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
                            }
                        });
                    }
                });
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSharedPreferences("User", MODE_PRIVATE).edit().clear().apply();
        Variables.getInstance().setUserAuthentificate(false);
        Variables.getInstance().setDownloadASLVariable(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user_main);
        useremail.setText(User.getString("Name", ""));

        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user_main);
        user_workplace.setText(WorkPlace.getString("Name", ""));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        boolean recreare = ((Variables) getApplication()).getIsRecreate();
        if(recreare){
            ((Variables) getApplication()).setRecreate(false);
            recreate();
        }
    }
}
