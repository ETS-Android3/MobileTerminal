package edi.md.mobile;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Button btn_check_price,btn_sales,btn_invoice,btn_transfer,btn_inventory,btn_stock_asortment;
    Integer checkPrice=1,salesDoc=2, invoiceDoc =3,transferIntern=4,inventAr=5,stock_asl=7,workplaces=8,printers=9,securitate = 10;
    TimerTask timerTaskSync;
    Timer sync;
    Boolean pingTest=false,keyLicense;
    SharedPreferences Settings;
    Boolean checkPin;
    private Menu menu;
    NavigationView navigationView;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 12 && grantResults.length == 7) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
            } else if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }else if(grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }else if(grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else if(grantResults[4] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            }else if(grantResults[5] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH}, 1);
            }else if(grantResults[6] != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
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
        navigationView = findViewById(R.id.nav_view);
        btn_check_price=findViewById(R.id.btn_check_price);
        btn_sales = findViewById(R.id.btn_sales);
        btn_invoice = findViewById(R.id.btn_invoice);
        btn_transfer = findViewById(R.id.btn_transfer);
        btn_inventory = findViewById(R.id.btn_inventory);
        btn_stock_asortment = findViewById(R.id.btn_stock_assortment);

        requestMultiplePermissions();

        ((Variables)getApplication()).appendLog("Aplication starting",MainActivity.this);
        Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        final SharedPreferences.Editor editor = WorkPlace.edit();
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
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));
        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
        String nameWorkplace = WorkPlace.getString("Name","");
//        if(nameWorkplace.equals("")){
//            editor.putString("Name","Nedeterminat");
//            editor.apply();
//        }
        user_workplace.setText(WorkPlace.getString("Name",""));

        btn_check_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
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
        btn_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
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
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
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
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
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
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
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
                keyLicense=Settings.getBoolean("Key",false);
                if(pingTest) {
                    if(keyLicense) {
                        Boolean CheckLogin = ((Variables) getApplication()).getLoginVariable();
                        if (CheckLogin == null) {
                            CheckLogin = false;
                            ((Variables) getApplication()).setLoginVariable(false);
                        }
                        if (CheckLogin) {
                            Intent invoice = new Intent(".StockAssortmentMobile");
                            startActivity(invoice);
                        } else {
                            Intent Logins = new Intent(".LoginMobile");
                            Logins.putExtra("Activity", stock_asl);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menus);
        this.menu = menus;
        return true;
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
            Logins.putExtra("Activity", workplaces);
            startActivity(Logins);
        } else if (id == R.id.menu_printers) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", printers);
            startActivity(Logins);
        } else if (id == R.id.menu_securitate) {
            Intent Logins = new Intent(".LoginMobile");
            Logins.putExtra("Activity", securitate);
            startActivity(Logins);
        } else if (id == R.id.menu_about) {
            Intent MenuConnect = new Intent(".MenuAbout");
            startActivity(MenuConnect);
        } else if (id == R.id.menu_exit) {
            SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
            WorkPlace.edit().clear().apply();
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
                        URL generatedURL = Ping(Settings.getString("IP",""), Settings.getString("Port",""));
                        new AsyncTask_Ping().execute(generatedURL);
                    }
                });
            }
        };

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
                pingTest=true;
                menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.signal_wi_fi_48));
            }else {
                pingTest=false;
                this.cancel(true);
                menu.getItem(0).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.no_signal_wi_fi_48));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        boolean logins = ((Variables) getApplication()).getLoginVariable();

        outState.putString("userName", User.getString("Name",""));
        outState.putString("userUid", User.getString("UserID",""));

        outState.putString("workplaceUid", WorkPlace.getString("Uid",""));
        outState.putString("workplaceName", WorkPlace.getString("Name",""));

        outState.putBoolean("Login",logins);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        SharedPreferences.Editor inpUser = User.edit();
        SharedPreferences.Editor inpWorkPlace = WorkPlace.edit();

        ((Variables) getApplication()).setLoginVariable(savedInstanceState.getBoolean("Login"));

        inpUser.putString("Name",savedInstanceState.getString("userName"));
        inpUser.putString("UserID",savedInstanceState.getString("userUid"));

        inpWorkPlace.putString("Uid",savedInstanceState.getString("workplaceUid"));
        inpWorkPlace.putString("Name",savedInstanceState.getString("workplaceName"));

        inpUser.apply();
        inpWorkPlace.apply();
    }

    @Override
    protected void onDestroy() {
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor input_LogIn = User.edit();
        input_LogIn.putString("Name", "");
        input_LogIn.putString("UserID", "");
        input_LogIn.apply();
        SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        WorkPlace.edit().clear().apply();
        ((Variables) getApplication()).setLoginVariable(false);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name", ""));

        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
        TextView user_workplace = (TextView) headerLayout.findViewById(R.id.txt_workplace_user);
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
