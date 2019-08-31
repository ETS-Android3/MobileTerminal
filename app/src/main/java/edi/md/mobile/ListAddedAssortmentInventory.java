package edi.md.mobile;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edi.md.mobile.Settings.Assortment;

public class ListAddedAssortmentInventory extends AppCompatActivity {

    ListView list_assortment;

    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();
    SimpleAdapter simpleAdapterASL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list_added_assortment_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar_list_inve);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        list_assortment = findViewById(R.id.list_added_assortment_inventory);

        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
        SharedPreferences SaveCountName = getSharedPreferences("SaveNameInventory", MODE_PRIVATE);
        Map<String, ?> allEntries = SaveCount.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            HashMap<String, Object> asl_ = new HashMap<>();
            asl_.put("Name",SaveCountName.getString(entry.getKey(),"NoN"));
            asl_.put("Count",entry.getValue().toString());
            asl_list.add(asl_);

        }
        simpleAdapterASL = new SimpleAdapter(this, asl_list,R.layout.show_asl_invoice, new String[]{"Name","Count"},
                new int[]{R.id.textName_asl_invoice,R.id.textCantitate_asl_invoice});
        list_assortment.setAdapter(simpleAdapterASL);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        getMenuInflater().inflate(R.menu.menu_list_add_asl_inventory, menus);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        else if (id == R.id.action_close_list_inventory){
           finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
