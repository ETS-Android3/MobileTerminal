package md.intelectsoft.stockmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import md.intelectsoft.stockmanager.NetworkUtils.ApiRetrofit;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentRemainResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Remain;
import md.intelectsoft.stockmanager.NetworkUtils.Services.CommandService;
import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static md.intelectsoft.stockmanager.ListAssortment.AssortimentClickentSendIntent;

public class CountListOfAssortmentStock extends AppCompatActivity {
    String ip_,port_,mNameAssortment, WareUid;
    EditText Count_enter;
    ImageButton btn_plus,btn_del;
    TextView name_forasl,price_forasl,btn_save,btn_cancel,txtCode,txtBarCode,txtMarking,txtRemain;
    ProgressBar pgBar;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home : {
                Activity activity=CountListOfAssortmentStock.this;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                View view = activity.getCurrentFocus();
                if (view == null) {
                    view = new View(activity);
                }
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                finish();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_list_of_assortment_stock);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_count_list_asl_stock);
        setSupportActionBar(mToolbar);

        Count_enter = findViewById(R.id.edt_cnt_list_asl_stock);
        btn_plus = findViewById(R.id.image_btn_add_list_asl_stock);
        btn_del = findViewById(R.id.image_btn_del_list_asl_stock);
        btn_save = findViewById(R.id.btn_add_count_list_asl_stock);
        btn_cancel = findViewById(R.id.btn_cancel_count_list_asl_stock);
        name_forasl = findViewById(R.id.txt_name_assortment_count_list_asl_stock);
        price_forasl = findViewById(R.id.txtPrice_asortment_count_list_asl_stock);
        txtCode = findViewById(R.id.txtcode_assortment_count_list_asl_stock);
        txtBarCode = findViewById(R.id.txtBarcode_assortment_count_list_asl_stock);
        txtMarking = findViewById(R.id.txtMarking_asortment_count_list_asl_stock);
        txtRemain = findViewById(R.id.txtStoc_asortment_count_list_asl_stock);
        pgBar = findViewById(R.id.progressBar2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        SharedPreferences sPref = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);

        ip_ = Settings.getString("IP","");
        port_ = Settings.getString("Port","");


        boolean showKB = Settings.getBoolean("ShowKeyBoard",false);
        if (showKB){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            Count_enter.requestFocus();
        }
        boolean ShowCode = Settings.getBoolean("ShowCode", false);

        Intent sales = getIntent();
        AssortmentParcelable assortment = sales.getParcelableExtra(AssortimentClickentSendIntent);
        mNameAssortment = assortment.getName();
        final String mPriceAssortment = assortment.getPrice();
        final String mMarkingAssortment = assortment.getMarking();
        final String mCodeAssortment = assortment.getCode();
        final String mBarcodeAssortment = assortment.getBarCode();
        final String mRemainAssortment = assortment.getRemain();
        final String mIDAssortment = assortment.getAssortimentID();

        name_forasl.setText(mNameAssortment);
        price_forasl.setText(mPriceAssortment);
        WareUid = sales.getStringExtra("WareUid");


        if(mCodeAssortment==null || mCodeAssortment.equals("") || mCodeAssortment.equals("null") ){
            txtCode.setText("-");
        }else{
            if (!ShowCode)
                txtCode.setText("--------");
            else
                txtCode.setText(mCodeAssortment);
        }
        if(mMarkingAssortment==null || mMarkingAssortment.equals("") || mMarkingAssortment.equals("null") ){
            txtMarking.setText("-");
        }else{
            txtMarking.setText(mMarkingAssortment);
        }
        if(mRemainAssortment==null || mRemainAssortment.equals("") || mRemainAssortment.equals("null") ){
            txtRemain.setText("-");
        }else{
            txtRemain.setText(mRemainAssortment);
        }
        if(mBarcodeAssortment==null || mBarcodeAssortment.equals("") || mBarcodeAssortment.equals("null")){
            txtBarCode.setText("-");
        }else{
            txtBarCode.setText(mBarcodeAssortment);
        }

        boolean verifyStock = Settings.getBoolean("CheckStockToServer", false);

        if(verifyStock){
            checkStock(mIDAssortment,WareUid);
        }

        Count_enter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) saveCount(mIDAssortment);
                else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER) saveCount(mIDAssortment);
                return false;
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCount(mIDAssortment);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer curr = Integer.valueOf(Count_enter.getText().toString());
                curr += 1;
                Count_enter.setText(String.valueOf(curr));

            }
        });

        btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer curr = Integer.valueOf(Count_enter.getText().toString());
                if (curr - 1 <= 0) {

                } else {
                    curr -= 1;
                }
                Count_enter.setText(String.valueOf(curr));
            }
        });
    }

    private void checkStock(String mIDAssortment, String wareUid) {
        txtRemain.setVisibility(View.INVISIBLE);
        pgBar.setVisibility(View.VISIBLE);
        CommandService commandService = ApiRetrofit.getCommandService(CountListOfAssortmentStock.this);
        Call<GetAssortmentRemainResults> call = commandService.getAssortimentRemains(mIDAssortment,wareUid);

        call.enqueue(new Callback<GetAssortmentRemainResults>() {
            @Override
            public void onResponse(Call<GetAssortmentRemainResults> call, Response<GetAssortmentRemainResults> response) {
                if(response.isSuccessful()){
                    GetAssortmentRemainResults remain = response.body();
                    if(remain != null && remain.getErrorCode() == 0){
                        List<Remain> remains = remain.getRemains();
                        if(remains.size() > 0){

                            for(Remain remain1 : remains){
                                String remaiId = remain1.getWarehouseID();
                                if(remaiId.equals(wareUid)){
                                    txtRemain.setText(String.format("%.2f",remain1.getRemain()));
                                    txtRemain.setVisibility(View.VISIBLE);
                                    pgBar.setVisibility(View.INVISIBLE);
                                    break;
                                }
                            }
                        }
                        else{
                            txtRemain.setVisibility(View.VISIBLE);
                            pgBar.setVisibility(View.INVISIBLE);
                        }
                    }
                    else{
                        txtRemain.setVisibility(View.VISIBLE);
                        pgBar.setVisibility(View.INVISIBLE);
                    }
                }
                else{
                    txtRemain.setVisibility(View.VISIBLE);
                    pgBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<GetAssortmentRemainResults> call, Throwable t) {
                txtRemain.setVisibility(View.VISIBLE);
                pgBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void saveCount(String uid_save){
        Intent intent = new Intent();
        intent.putExtra("Name",mNameAssortment);
        intent.putExtra("count", String.valueOf(Count_enter.getText()));
        setResult(RESULT_OK, intent);
        finish();
    }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (event.getAction()){
            case KeyEvent.ACTION_DOWN :{
                Count_enter.requestFocus();
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_1 : {
                        Count_enter.append("1");
                    }break;
                    case KeyEvent.KEYCODE_2 : {
                        Count_enter.append("2");
                    }break;
                    case KeyEvent.KEYCODE_3 : {
                        Count_enter.append("3");
                    }break;
                    case KeyEvent.KEYCODE_4 : {
                        Count_enter.append("4");
                    }break;
                    case KeyEvent.KEYCODE_5 : {
                        Count_enter.append("5");
                    }break;
                    case KeyEvent.KEYCODE_6 : {
                        Count_enter.append("6");
                    }break;
                    case KeyEvent.KEYCODE_7 : {
                        Count_enter.append("7");
                    }break;
                    case KeyEvent.KEYCODE_8 : {
                        Count_enter.append("8");
                    }break;
                    case KeyEvent.KEYCODE_9 : {
                        Count_enter.append("9");
                    }break;
                    case KeyEvent.KEYCODE_0 : {
                        Count_enter.append("0");
                    }break;
                    case KeyEvent.KEYCODE_DEL : {
                        String test = Count_enter.getText().toString();
                        if(!Count_enter.getText().toString().equals("")) {
                            Count_enter.setText(test.substring(0, test.length() - 1));
                            Count_enter.requestFocus();
                        }
                    }break;
                    default:break;
                }
            }break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
