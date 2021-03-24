package md.intelectsoft.stockmanager;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class HelpMain extends AppCompatActivity {

    Button btn_help_check_price,btn_help_sales,btn_help_invoice,btn_help_transfer,btn_help_inventory,btn_help_stock,btn_help_connect,
            btn_help_workplace, btn_help_printers,btn_help_security,btn_help_assortment_sales, btn_help_add_count;

    int mCheckPrice = 901,mSales= 902,mInvoice = 903,mTransfer = 904,mInventory = 905,mStock = 906,
            mConnect = 907,mWorkPlace = 908,mPrinters = 909,mSecurity = 910,mAssortment = 911,mCount = 912;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help_main);

        Toolbar toolbar = findViewById(R.id.toolbar_help_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_help_add_count = findViewById(R.id.btn_help_add_cant);
        btn_help_assortment_sales = findViewById(R.id.btn_help_assortment);
        btn_help_check_price = findViewById(R.id.btn_help_check_price);
        btn_help_connect = findViewById(R.id.btn_help_connect);
        btn_help_inventory  = findViewById(R.id.btn_help_inventory);
        btn_help_invoice = findViewById(R.id.btn_help_invoice);
        btn_help_printers = findViewById(R.id.btn_help_printers);
        btn_help_sales = findViewById(R.id.btn_help_sales);
        btn_help_security = findViewById(R.id.btn_help_securitate);
        btn_help_stock = findViewById(R.id.btn_help_stock_assortment);
        btn_help_transfer = findViewById(R.id.btn_help_transfer);
        btn_help_workplace = findViewById(R.id.btn_help_workplace);

        btn_help_add_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mCount);
            }
        });
        btn_help_assortment_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openHelpPage(mAssortment);
            }
        });
        btn_help_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mConnect);
            }
        });
        btn_help_inventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mInventory);
            }
        });
        btn_help_invoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mInvoice);
            }
        });
        btn_help_printers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mPrinters);
            }
        });
        btn_help_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mSales);
            }
        });
        btn_help_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mSecurity);
            }
        });
        btn_help_stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mStock);
            }
        });
        btn_help_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mTransfer);
            }
        });
        btn_help_check_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openHelpPage(mCheckPrice);
            }
        });
        btn_help_workplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage(mWorkPlace);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void openHelpPage(int page){
        Intent openHelpPage = new Intent(this,Help.class);
        openHelpPage.putExtra("Page",page);
        startActivity(openHelpPage);
    }
}
