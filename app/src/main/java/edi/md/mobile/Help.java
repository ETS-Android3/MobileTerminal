package edi.md.mobile;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class Help extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent mGetPage = getIntent();
        int mID = mGetPage.getIntExtra("Page",0);
        if(mID == 901){
            setContentView(R.layout.activity_help_checkprice);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 902){
            setContentView(R.layout.activity_help_sales);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 903){
            setContentView(R.layout.activity_help_invoice);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 904){
            setContentView(R.layout.activity_help_transfer);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 905){
            setContentView(R.layout.activity_help_inventory);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 906){
            setContentView(R.layout.activity_help_stock_asl);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 907){
            setContentView(R.layout.activity_help_connect);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 908){
            setContentView(R.layout.activity_help_workplace);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 909){
            setContentView(R.layout.activity_help_printers);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 910){
            setContentView(R.layout.activity_help_security);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 911){
            setContentView(R.layout.activity_help_assortment);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else if(mID == 912){
            setContentView(R.layout.activity_help_count);
            setTitle("CheckPrice");
            Toolbar toolbar = findViewById(R.id.toolbar_help_checkprice);
            setSupportActionBar(toolbar);
        }
        else{
            setContentView(R.layout.activity_help);
            Toolbar toolbar = findViewById(R.id.toolbar_help);
            setSupportActionBar(toolbar);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
