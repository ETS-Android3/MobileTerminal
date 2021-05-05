package edi.md.mobile.SettingsMenu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import edi.md.mobile.R;

import static edi.md.mobile.NetworkUtils.NetworkUtils.Ping;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_Ping;

public class Conect extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    EditText ip_, port;
    Button test;
    ProgressBar pgBar;
    SharedPreferences Settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.header_conectare_activity);
        setContentView(R.layout.activity_conect);
        Toolbar toolbar = findViewById(R.id.toolbar_connect);
        setSupportActionBar(toolbar);

        ip_=findViewById(R.id.et_ip_conect);
        port=findViewById(R.id.et_port_conect);
        test=findViewById(R.id.btn_test_conect);
        pgBar=findViewById(R.id.progressBar_conect);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_conect);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Settings = getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences.Editor inputSeting =Settings.edit();

        ip_.setText(Settings.getString("IP",""));
        port.setText(Settings.getString("Port",""));

        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgBar.setVisibility(ProgressBar.VISIBLE);
                inputSeting.putString("IP",ip_.getText().toString());
                inputSeting.putString("Port",port.getText().toString());
                inputSeting.apply();
                String ip= ip_.getText().toString();
                String port_ = port.getText().toString();
                URL generatedURL = Ping(ip, port_);
                new AsyncTask_Ping().execute(generatedURL);
            }
        });
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
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                ip_.setBackgroundResource(R.drawable.ping_true_conect);
            }else {
                this.cancel(true);
                pgBar.setVisibility(ProgressBar.INVISIBLE);
                ip_.setBackgroundResource(R.drawable.ping_false_connect);
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_conect);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close_conect) {
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
            SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);
            WorkPlace.edit().clear().apply();
            finishAffinity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_conect);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
