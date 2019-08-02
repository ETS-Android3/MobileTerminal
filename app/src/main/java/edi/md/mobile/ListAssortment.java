package edi.md.mobile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import edi.md.mobile.Settings.ASL;
import edi.md.mobile.Settings.Assortment;
import edi.md.mobile.Utils.Assortiment;
import edi.md.mobile.Utils.AssortmentInActivity;
import edi.md.mobile.Utils.ServiceApi;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edi.md.mobile.NetworkUtils.NetworkUtils.SaveRevisionLine;

public class ListAssortment extends AppCompatActivity {
    final Context context = this;
    final int REQUEST_CODE_forCount = 303;
    public String cnt, WareUid,UserId;
    FloatingActionButton save_assortments;
    ListView list_assortments;
    SearchView search_asl;
    String ip_,port_, mGUIDAssortment;
    ProgressDialog pgH;
    SimpleAdapter simpleAdapterASL;
    JSONObject _ass,sendRevision,json_item_added_inventory;
    String mNameAssortment, mPriceAssortment;
    JSONArray jsonArray,guidSelected ,array_added_items_inventroy;
    List<String> list_clicked_item =new ArrayList<>();
    List<String> list_name_clicked_item =new ArrayList<>();
    int resultOut,index_clecked_item=0,index_clecked_item_name=1;
    Boolean mIsFolderAssortment;
    Handler handler;
    TimerTask timerTask;
    Timer t;
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();
    Toolbar mToolbar;
    Variables myapp;
    boolean multi_folders = false;

    Assortiment mAssortimentArray = new Assortiment();
    Assortment mAssortment;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                int siz = list_clicked_item.size();
                if (siz == 0) {
                    resultOut = jsonArray.length();
                    if (resultOut==0) {
                        Intent intent2 = new Intent();
                        setResult(RESULT_CANCELED, intent2);
                        finish();
                    }
                    else {
                        AlertDialog.Builder exit = new AlertDialog.Builder(context);
                        exit.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        exit.setMessage(getResources().getString(R.string.msg_dialog_list_asl_document_not_saved));
                        exit.setNegativeButton(getResources().getString(R.string.toggle_btn_check_remain_nu), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent2 = new Intent();
                                setResult(RESULT_CANCELED, intent2);
                                finish();
                            }
                        });
                        exit.setPositiveButton(getResources().getString(R.string.toggle_btn_check_remain_da), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences sPref_saveASL = getSharedPreferences("Save sales", MODE_PRIVATE);
                                final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
                                Intent getActivity = getIntent();
                                final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                                switch (id_intent) {
                                    case 181: {//sales
                                        SharedPreferences sales = getSharedPreferences("Save sales", MODE_PRIVATE);
                                        final SharedPreferences.Editor inp_sales = sPref_saveASL.edit();
                                        Intent added = new Intent();
                                        inpASL.putString("JSONArray", jsonArray.toString());
                                        inpASL.apply();
                                        setResult(RESULT_OK, added);
                                        finish();
                                    }
                                    break;
                                    case 191: {//invoice
                                        Intent added = new Intent();
                                        inpASL.putString("JSONArray", jsonArray.toString());
                                        inpASL.apply();
                                        setResult(RESULT_OK, added);
                                        finish();
                                    }
                                    break;
                                    case 171: {//inventorie
                                        Intent added = new Intent();
                                        setResult(RESULT_OK, added);
                                        finish();
                                    }
                                    break;
                                    default: {
                                        Intent added = new Intent();
                                        setResult(RESULT_OK, added);
                                        finish();
                                    }
                                }
                            }
                        });
                        exit.show();
                    }
                }
                else{
                    index_clecked_item_name -= 2;
                    setTitle(list_name_clicked_item.get(index_clecked_item_name));
                    index_clecked_item_name +=1;
                    list_name_clicked_item.remove(index_clecked_item_name);
                    //last_clicked_index_item = siz - 1;
                    index_clecked_item -= 1;
                    mGUIDAssortment =list_clicked_item.get(index_clecked_item);
                    asl_list.clear();
                    init_home_assortment(mGUIDAssortment);
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","Price","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    list_assortments.setAdapter(simpleAdapterASL);
                    list_clicked_item.remove(index_clecked_item);
                }
            }
            break;
            case R.id.m_home: {
                list_clicked_item.clear();
                list_name_clicked_item.clear();
                index_clecked_item_name=1;
                list_name_clicked_item.add(0,getResources().getString(R.string.header_list_assortment));
                setTitle(getResources().getString(R.string.header_list_assortment));
                index_clecked_item = 0;
                asl_list.clear();
                init_home_assortment("00000000-0000-0000-0000-000000000000");
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assortment, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_list_assortment);

        mToolbar = findViewById(R.id.toolbar_sales_add);
        setSupportActionBar(mToolbar);
        pgH = new ProgressDialog(ListAssortment.this);

        save_assortments = findViewById(R.id.fl_btn_save_list_assortment);
        list_assortments =findViewById(R.id.LW_assortment_list);
        search_asl=findViewById(R.id.search_text);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        t = new Timer();

        mGUIDAssortment = "00000000-0000-0000-0000-000000000000";

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);


        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        WareUid = WorkPlace.getString("Uid","0");

        if(WareUid.equals("0")){
            Intent asl = getIntent();
            WareUid = asl.getStringExtra("WareID");
        }

        myapp =((Variables)getApplication());
        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
        pgH.setIndeterminate(true);
        pgH.setCancelable(false);
        list_name_clicked_item.add(0,getResources().getString(R.string.header_list_assortment));
        setTitle(getResources().getString(R.string.header_list_assortment));

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        asl_list.sort(new Comparator<HashMap<String, Object>>() {
                            @Override
                            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                                return o1.get("Name").toString().compareTo(o2.get("Name").toString());
                            }
                        });
                    }
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});

                    list_assortments.setAdapter(simpleAdapterASL);
                    pgH.dismiss();
                }
                else if(msg.what == 10) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        asl_list.sort(new Comparator<HashMap<String, Object>>() {
//                            @Override
//                            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
//                                return String.valueOf(o2.get("Folder_is")).compareTo(String.valueOf(o1.get("Folder_is")));
//                            }
//                        });
//
//                    }
                    order_sortAll(asl_list);
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    pgH.dismiss();
                    ((Variables)getApplication()).setDownloadASLVariable(true);
                    list_assortments.setAdapter(simpleAdapterASL);
                }
                else if(msg.what==20){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        asl_list.sort(new Comparator<HashMap<String, Object>>() {
                            @Override
                            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                                return String.valueOf(o2.get("Folder_is")).compareTo(String.valueOf(o1.get("Folder_is")));
                            }
                        });

                    }
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    list_assortments.setAdapter(simpleAdapterASL);
                }
            }

        };
        jsonArray = new JSONArray();
        array_added_items_inventroy = new JSONArray();

        if (((Variables)getApplication()).getDownloadASLVariable()==null){
            ((Variables)getApplication()).setDownloadASLVariable(false);
        }
        boolean dwnlASL =((Variables)getApplication()).getDownloadASLVariable();
        if(dwnlASL){
            init_home_assortment("00000000-0000-0000-0000-000000000000");
        }else {
            pgH.show();
            initializareAsortiment();
        }


        list_assortments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sPref = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();

                mGUIDAssortment = (String) asl_list.get(position).get("ID");
                mIsFolderAssortment = (boolean) asl_list.get(position).get("Folder_is");
                mPriceAssortment = (String) asl_list.get(position).get("mPriceAssortment");
                mNameAssortment = (String) asl_list.get(position).get("Name");
                String mCodeAssortment = (String) asl_list.get(position).get("Code");
                String mBarcodeAssortment = (String) asl_list.get(position).get("BarCode");
                String mIncomePriceAssortment = (String) asl_list.get(position).get("IncomePrice");
                String mMarkingAssortment = (String) asl_list.get(position).get("mMarkingAssortment");
                String mRemainAssortment = (String) asl_list.get(position).get("Remain");
                String mUnitAssortment = (String) asl_list.get(position).get("mUnitAssortment");
                String mUnitPriceAssortment = (String) asl_list.get(position).get("mUnitPrice");
                String mUnitInPackageAssortment = (String) asl_list.get(position).get("mUnitInPackage");

                if(mUnitPriceAssortment!=null){
                    mUnitPriceAssortment=mUnitPriceAssortment.replace(",",".");
                    Double priceunit = Double.valueOf(mUnitPriceAssortment);
                    mUnitPriceAssortment =String.format("%.2f",priceunit);
                }

                String alow_integer =(String) asl_list.get(position).get("AllowNonIntegerSale");
                final String ParentUid = (String) asl_list.get(position).get("Parent_ID");
                if (!mIsFolderAssortment) {
                    ed.putString("Guid_Assortiment", mGUIDAssortment);
                    ed.putString("Code_Assortiment", mCodeAssortment);
                    ed.putString("BarCode_Assortiment", mBarcodeAssortment);
                    ed.putString("Name_Assortiment", mNameAssortment);
                    String price = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                    price = price.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                    ed.putString("Price_Assortiment", price);
                    ed.putString("IncomePrice", mIncomePriceAssortment);
                    ed.putString("AllowNonIntegerSale", alow_integer);
                    ed.apply();

                    Intent getActivity = getIntent();
                    final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                    final boolean auto_send =  getActivity.getBooleanExtra("AutoCount",false);
                    switch (id_intent){
                        case 141: {
                            _ass = new JSONObject();
                            try {
                                _ass.put("AssortimentName", mNameAssortment);
                                _ass.put("AssortimentUid", mGUIDAssortment);
                                String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                                price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                                _ass.put("mPriceAssortment", price_list);
                                _ass.put("Code",mCodeAssortment);
                                _ass.put("Barcode",mBarcodeAssortment);
                                _ass.put("mUnitAssortment",mUnitAssortment);
                                _ass.put("mUnitPrice",mUnitPriceAssortment);
                                _ass.put("mUnitInPackage",mUnitInPackageAssortment);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                            }
                            Intent count_activity = new Intent(".CountListAssortmentMobile");///
                            startActivityForResult(count_activity, REQUEST_CODE_forCount);
                        }break;
                        case 151: {//stock assortment
                            _ass = new JSONObject();
                            try {
                                _ass.put("AssortimentName", mNameAssortment);
                                _ass.put("AssortimentUid", mGUIDAssortment);
                                String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                                price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                                _ass.put("mPriceAssortment", price_list);
                                _ass.put("Code",mCodeAssortment);
                                _ass.put("Barcode",mBarcodeAssortment);
                                _ass.put("mUnitAssortment",mUnitAssortment);
                                _ass.put("mUnitPrice",mUnitPriceAssortment);
                                _ass.put("mUnitInPackage",mUnitInPackageAssortment);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                            }
                            Intent count_activity = new Intent(".CountListAssortmentMobile");///
                            startActivityForResult(count_activity, REQUEST_CODE_forCount);
                        }break;
                        case 181: {//sales
                            String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                            price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");

                            mAssortment = new Assortment();
                            mAssortment.setAssortimentID(mGUIDAssortment);
                            mAssortment.setBarCode(mBarcodeAssortment);
                            mAssortment.setCode(mCodeAssortment);
                            mAssortment.setName(mNameAssortment);
                            mAssortment.setPrice(price_list);
                            mAssortment.setMarking(mMarkingAssortment);
                            mAssortment.setRemain(mRemainAssortment);
                            mAssortment.setUnit(mUnitAssortment);
                            mAssortment.setUnitPrice(mUnitPriceAssortment);
                            mAssortment.setUnitInPackage(mUnitInPackageAssortment);
//                            _ass = new JSONObject();
//                            try {
//                                _ass.put("AssortimentName", mNameAssortment);
//                                _ass.put("AssortimentUid", mGUIDAssortment);
//                                String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
//                                price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
//                                _ass.put("mPriceAssortment", price_list);
//                                _ass.put("Code",mCodeAssortment);
//                                _ass.put("Barcode",mBarcodeAssortment);
//                                _ass.put("mUnitAssortment",mUnitAssortment);
//                                _ass.put("mUnitPrice",mUnitPriceAssortment);
//                                _ass.put("mUnitInPackage",mUnitInPackageAssortment);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
//                            }
                            Intent count_activity = new Intent(".CountListAssortmentMobile");///
                            startActivityForResult(count_activity, REQUEST_CODE_forCount);
                        }break;
                        case 191: {//invoice
                            _ass = new JSONObject();
                            try {
                                _ass.put("AssortimentName", mNameAssortment);
                                _ass.put("AssortimentUid", mGUIDAssortment);
                                _ass.put("IncomePrice", mIncomePriceAssortment);
                                String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                                price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                                _ass.put("mPriceAssortment", price_list);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                            }
                            Intent count_activity = new Intent(".CountInvoiceFromTouchMobile");
                            startActivityForResult(count_activity, 333);
                        }break;
                        case 171: {//inventory
                            if(auto_send) {
                                json_item_added_inventory = new JSONObject();

                                SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                                String RevisionID = Revision.getString("RevisionID", "");
                                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                                pgH.setIndeterminate(true);
                                pgH.setCancelable(false);
                                pgH.show();
                                sendRevision = new JSONObject();
                                try {
                                    boolean isExtist = false;
                                    for(int i=0; i <array_added_items_inventroy.length();i++){
                                        JSONObject object= array_added_items_inventroy.getJSONObject(i);
                                        String GUID = object.getString("AssortimentUid");
                                        String count_exist = object.getString("AssortimentUid");
                                        count_exist = count_exist.replace(",",".");
                                        double cant_exist_double = Double.valueOf(count_exist);
                                        if (GUID.equals(mGUIDAssortment)){
                                            cant_exist_double = cant_exist_double +1;
                                            object.put("Quantity",String.valueOf(cant_exist_double));
                                            isExtist=true;
                                            break;
                                        }

                                    }
                                    if (!isExtist){
                                        json_item_added_inventory.put("AssortimentName", mNameAssortment);
                                        json_item_added_inventory.put("AssortimentUid", mGUIDAssortment);
                                        json_item_added_inventory.put("Quantity", "1");

                                        array_added_items_inventroy.put(json_item_added_inventory);
                                    }


                                    sendRevision.put("Assortiment", mGUIDAssortment);
                                    sendRevision.put("Quantity", "1");
                                    sendRevision.put("RevisionID", RevisionID);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                                }
                                URL generateSaveLine = SaveRevisionLine(ip_, port_);
                                new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                            }else{

                                json_item_added_inventory = new JSONObject();
                                _ass = new JSONObject();

                                try {
                                    _ass.put("AssortimentName", mNameAssortment);
                                    _ass.put("AssortimentUid", mGUIDAssortment);

                                    String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                                    price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                                    _ass.put("mPriceAssortment", price_list);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                                }
                                Intent count_activity = new Intent(".CountListAssortmentMobile");
                                count_activity.putExtra("NonAutoCount", 717);
                                startActivityForResult(count_activity, 444);
                            }
                        }break;
                        //return to check price activity
                        case 161: {
                            String price_list = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                            price_list = price_list.replace(getResources().getString(R.string.txt_list_asl_view_price),"");

                            Assortment assortment = new Assortment();
                            assortment.setBarCode(mBarcodeAssortment);
                            assortment.setCode(mCodeAssortment);
                            assortment.setName(mNameAssortment);
                            assortment.setPrice(price_list);
                            assortment.setMarking(mMarkingAssortment);
                            assortment.setRemain(mRemainAssortment);
                            assortment.setUnit(mUnitAssortment);
                            assortment.setUnitPrice(mUnitPriceAssortment);
                            assortment.setUnitInPackage(mUnitInPackageAssortment);
                            AssortmentInActivity assortmentParcelable = new AssortmentInActivity(assortment);

                            Intent intent = new Intent();
                            intent.putExtra("AssortmentInActivity",assortmentParcelable);
                            setResult(RESULT_OK,intent);
                            finish();
                        }break;

                    }
                }
                else {
                    list_clicked_item.add(index_clecked_item, ParentUid);
                    list_name_clicked_item.add(index_clecked_item_name, mNameAssortment);
                    setTitle(mNameAssortment);
                    index_clecked_item += 1;
                    index_clecked_item_name +=1;
                    asl_list.clear();
                    asl_list = myapp.get_AssortimentFromParent(mGUIDAssortment);
                    order_sortAll(asl_list);
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    list_assortments.setAdapter(simpleAdapterASL);
                }
            }
        });

        save_assortments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getActivity = getIntent();
                final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                switch (id_intent) {
                    case 141: {//transfer
                        SharedPreferences sPref_saveASL = getSharedPreferences("Transfer", MODE_PRIVATE);
                        final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
                        Intent added = new Intent();
                        inpASL.putString("AssortmentTransferAddedArray", jsonArray.toString());
                        inpASL.apply();
                        setResult(RESULT_OK, added);
                        finish();
                    }break;
                    case 151: {//stockAssortment
                        SharedPreferences sPref_saveASL = getSharedPreferences("StockAssortment", MODE_PRIVATE);
                        final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
                        Intent added = new Intent();
                        inpASL.putString("AssortmentStockAddedArray", jsonArray.toString());
                        inpASL.apply();
                        setResult(RESULT_OK, added);
                        finish();
                    }break;
                    case 181: {//sales
//                        SharedPreferences sPref_saveASL = getSharedPreferences("Sales", MODE_PRIVATE);
//                        final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
//                        Intent added = new Intent();
//                        inpASL.putString("AssortmentSalesAddedArray", jsonArray.toString());
//                        inpASL.apply();
//                        Intent intent = new Intent();
//                        intent.putExtra("AssortimentForSales",mAssortimentArray);

                        setResult(RESULT_OK);
                        finish();
                    }break;
                    case 191: {//invoice
                        SharedPreferences sPref_saveASL = getSharedPreferences("Invoice", MODE_PRIVATE);
                        final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
                        Intent added = new Intent();
                        inpASL.putString("AssortmentInvoiceAddedArray", jsonArray.toString());
                        inpASL.apply();
                        setResult(RESULT_OK, added);
                        finish();
                    }break;
                    case 171: {//inventory
                        Intent added = new Intent();
                        added.putExtra("ArrayAdded",array_added_items_inventroy.toString());
                        setResult(RESULT_OK, added);
                        finish();
                    }break;
                    case 161: {//check_price
                        Intent added = new Intent();
                        setResult(RESULT_OK, added);
                        finish();
                    }break;
                }

            }
        });
        search_asl.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if(!searchText.equals("")) {
                    t.cancel();
                    t = new Timer();
                    startTimetask(searchText);
                    t.schedule(timerTask, 1200);
                }else {
                    list_clicked_item.clear();
                    list_name_clicked_item.clear();
                    index_clecked_item_name=1;
                    list_name_clicked_item.add(0,getResources().getString(R.string.header_list_assortment));
                    setTitle(getResources().getString(R.string.header_list_assortment));
                    index_clecked_item=0;
                    mGUIDAssortment ="00000000-0000-0000-0000-000000000000";
                    init_home_assortment(mGUIDAssortment);
                }
                return true;
            }
        });

    }

    private void startTimetask(final String newText){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                ListAssortment.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newText.length() >= 3) {
                            asl_list.clear();
                            onSearch(newText);
                        }
                    }
                });
            }
        };

    }
    private void initializareAsortiment() {
        Thread downloadASL = new Thread(new Runnable() {
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
                final Call<ASL> assortiment = assortiment_API.getAssortiment(UserId,WareUid);
                assortiment.enqueue(new Callback<ASL>() {
                    @Override
                    public void onResponse(Call<ASL> call, Response<ASL> response) {

                        ASL assortiment_body = response.body();
                        List<Assortment> assortmentListData = assortiment_body.getAssortments();
                        SharedPreferences CheckUidFolder =getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                        String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");
                        try {
                            guidSelected = new JSONArray(selectedUidJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                        }
                        if(guidSelected.length()>0)
                            multi_folders=true;
                        else
                            multi_folders=false;
                        Variables myapp =((Variables)getApplication());

                        for (int i=0; i<assortmentListData.size();i++){
                            HashMap<String, Object> asl_ = new HashMap<>();
                            String asl_name= assortmentListData.get(i).getName();
                            String uid_asl = assortmentListData.get(i).getAssortimentID();
                            String parent_uid_asl = assortmentListData.get(i).getAssortimentParentID();
                            String price =   assortmentListData.get(i).getPrice();
                            String code_asl =  assortmentListData.get(i).getCode();
                            String barcode_asl = assortmentListData.get(i).getBarCode();
                            String allow_integer =  assortmentListData.get(i).getAllowNonIntegerSale();
                            String incomePrice= assortmentListData.get(i).getIncomePrice();
                            String unitary= assortmentListData.get(i).getUnit();
                            String finalUnitPrice= assortmentListData.get(i).getUnitPrice();
                            String UnitInPackage= assortmentListData.get(i).getUnitInPackage();
                            boolean is_folder= assortmentListData.get(i).getIsFolder();

                            Double priceunit = Double.valueOf(price);
                            price =String.format("%.2f",priceunit);

                            myapp.add_AssortimentID(uid_asl,assortmentListData.get(i));

                            if(multi_folders){
                                for (int j=0;j<guidSelected.length();j++) {
                                    String guidArray = null;
                                    try {
                                        guidArray = guidSelected.getString(j);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                                    }
                                    boolean contains_id = uid_asl.contains(guidArray);
                                    if (contains_id) {
                                        asl_.put("Folder_is", true);
                                        asl_.put("Parent_ID", parent_uid_asl);
                                        asl_.put("Name", asl_name);
                                        asl_.put("ID", uid_asl);
                                        asl_.put("icon", R.drawable.folder_assortiment_item);
                                        asl_list.add(asl_);
                                    }
                                }
                            }else{
                                if(parent_uid_asl.equals("00000000-0000-0000-0000-000000000000")) {
                                    if (!is_folder) {
                                        asl_.put("Folder_is", false);
                                        String Asl_Price =getResources().getString(R.string.txt_list_asl_view_price)+ price + getResources().getString(R.string.txt_list_asl_view_valuta);
                                        asl_.put("icon", R.drawable.assortiment_item);
                                        asl_.put("Name", asl_name);
                                        asl_.put("ID", uid_asl);
                                        asl_.put("Code", code_asl);
                                        asl_.put("Bar_code", getResources().getString(R.string.txt_list_asl_view_barcode) + barcode_asl);
                                        asl_.put("BarCode", barcode_asl);
                                        asl_.put("AllowNonIntegerSale", allow_integer);
                                        asl_.put("mPriceAssortment", Asl_Price);
                                        asl_.put("IncomePrice", incomePrice);
                                        asl_.put("mUnitAssortment", unitary);
                                        asl_.put("mUnitPrice", finalUnitPrice);
                                        asl_.put("mUnitInPackage", UnitInPackage);
                                        asl_list.add(asl_);
                                    } else {
                                        asl_.put("Folder_is", true);
                                        asl_.put("Parent_ID", parent_uid_asl);
                                        asl_.put("Name", asl_name);
                                        asl_.put("ID", uid_asl);
                                        asl_.put("Bar_code", "");
                                        asl_.put("icon", R.drawable.folder_assortiment_item);
                                        asl_list.add(asl_);
                                    }
                                }

                            }
                        }
                        handler.obtainMessage(10, 12).sendToTarget();
                    }

                    @Override
                    public void onFailure(Call<ASL> call, Throwable t) {
                        pgH.dismiss();
                        AlertDialog.Builder failureAsl = new AlertDialog.Builder(ListAssortment.this);
                        failureAsl.setCancelable(false);
                        failureAsl.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                        failureAsl.setMessage(getResources().getString(R.string.msg_eroare_download_asl) + t + "\n" + getResources().getString(R.string.msg_reload_download_asl));
                        failureAsl.setPositiveButton(getResources().getString(R.string.toggle_btn_check_remain_da), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pgH.setMessage(getResources().getString(R.string.msdownload_assortment));
                                pgH.setCancelable(false);
                                pgH.setIndeterminate(true);
                                pgH.show();
                            }
                        });
                        failureAsl.setNegativeButton(getResources().getString(R.string.toggle_btn_check_remain_nu), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        failureAsl.show();
                        //.0.t.printStackTrace();
                    }
                });
            }
        });
        downloadASL.start();
    } //initASLList
    private void init_home_assortment(String id) {
        asl_list.clear();

        SharedPreferences CheckUidFolder =getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);

        String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");
        try {
            guidSelected = new JSONArray(selectedUidJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(guidSelected.length()>0) {
            for (int j = 0; j < guidSelected.length(); j++) {
                String guidArray = null;
                try {
                    guidArray = guidSelected.getString(j);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
                HashMap<String, Object> asl_ = new HashMap<>();
                Assortment asl_folder = myapp.get_AssortimentFromID(guidArray);
                String name =  asl_folder.getName();
                String uid_asl =  asl_folder.getAssortimentID();
                String parent_uid_asl =  asl_folder.getAssortimentParentID();
                asl_.put("Folder_is", true);
                asl_.put("Parent_ID", parent_uid_asl);
                asl_.put("Name", name);
                asl_.put("Bar_code", "");
                asl_.put("ID", uid_asl);
                asl_.put("icon", R.drawable.folder_assortiment_item);
                asl_list.add(asl_);
            }
        }
        else{
            asl_list = myapp.get_AssortimentFromParent(id);
        }
        order_sortAll(asl_list);
        simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
        list_assortments.setAdapter(simpleAdapterASL);
    } //initASLLis
    private void onSearch(String search_text) {
        asl_list= myapp.get_Search_Assortment(search_text);
        handler.obtainMessage(20).sendToTarget();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_forCount) {
            if (resultCode == RESULT_OK) {
                cnt = data.getStringExtra("count");
                mAssortment.setCount(cnt);

                AssortmentInActivity assortmentInActivity= new AssortmentInActivity(mAssortment);
                ((Variables)getApplication()).addAssortimentToArray(assortmentInActivity);
            }
        }else if (requestCode==333){
            if (resultCode == RESULT_OK) {
                String  Count = data.getStringExtra("count");
                String  SalePrice = data.getStringExtra("SalePrice");
                String  IncomSum = data.getStringExtra("IncomSum");
                String  Suma = data.getStringExtra("Suma");

                try {
                    _ass.put("Count", Count);
                    _ass.put("SalePrice", SalePrice);
                    _ass.put("IncomSum", IncomSum);
                    _ass.put("Suma", Suma);



                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
            }
        }
        else if (requestCode==444){
            if (resultCode == RESULT_OK) {
                String  Count = data.getStringExtra("count");
                Count =Count.replace(",",".");
                double new_count = Double.valueOf(Count);
                try {
                    boolean isExtist = false;
                    for(int i=0; i <array_added_items_inventroy.length();i++){
                        JSONObject object = array_added_items_inventroy.getJSONObject(i);

                        String GUID = object.getString("AssortimentUid");
                        String count_exist = object.getString("Quantity");
                        count_exist = count_exist.replace(",",".");
                        double cant_exist_double = Double.valueOf(count_exist);
                        if (GUID.equals(mGUIDAssortment)){
                            cant_exist_double = cant_exist_double + new_count;
                            object.put("Quantity",String.valueOf(cant_exist_double));
                            isExtist=true;
                            break;
                        }

                    }
                    if (!isExtist){
                        json_item_added_inventory.put("AssortimentName", mNameAssortment);
                        json_item_added_inventory.put("AssortimentUid", mGUIDAssortment);
                        json_item_added_inventory.put("Quantity",Count);
                        array_added_items_inventroy.put(json_item_added_inventory);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_count_added_inventory), Toast.LENGTH_SHORT).show();
            }
        }
    }
    public String getResponseFromURLSaveRevisionLineAdd(URL send_bills) {
        StringBuilder data = new StringBuilder();
        HttpURLConnection send_bill_Connection = null;
        try {
            send_bill_Connection = (HttpURLConnection) send_bills.openConnection();
            send_bill_Connection.setConnectTimeout(4000);
            send_bill_Connection.setRequestMethod("POST");
            send_bill_Connection.setRequestProperty("Content-Type", "application/json");
            send_bill_Connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(send_bill_Connection.getOutputStream());
            wr.writeBytes(String.valueOf(sendRevision));
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
            ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
        } finally {
            if (send_bill_Connection != null) {
                send_bill_Connection.disconnect();
            }
        }

        return data.toString();
    }

    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

            }
        }

        return super.dispatchTouchEvent(event);
    }

    @SuppressLint("StaticFieldLeak")
    class AsyncTask_SaveRevisionLine extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            return getResponseFromURLSaveRevisionLineAdd(urls[0]);
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if (!response.equals("")) {
                try {
                    JSONObject responseAssortiment = new JSONObject(response);
                    int ErrorCode = responseAssortiment.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_count_added_inventory), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_error_code) + ErrorCode , Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
            }else{
                Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onBackPressed() {
        int siz = list_clicked_item.size();
        if (siz == 0) {
            resultOut = jsonArray.length();
            if (resultOut==0) {
                Intent intent2 = new Intent();
                setResult(RESULT_CANCELED, intent2);
                finish();
            } else {
                AlertDialog.Builder exit = new AlertDialog.Builder(context);
                exit.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
                exit.setMessage(getResources().getString(R.string.msg_dialog_list_asl_document_not_saved));
                exit.setNegativeButton(getResources().getString(R.string.toggle_btn_check_remain_nu), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent2 = new Intent();
                        setResult(RESULT_CANCELED, intent2);
                        finish();
                    }
                });
                exit.setPositiveButton(getResources().getString(R.string.toggle_btn_check_remain_da), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sPref_saveASL = getSharedPreferences("Save sales", MODE_PRIVATE);
                        final SharedPreferences.Editor inpASL = sPref_saveASL.edit();
                        Intent getActivity = getIntent();
                        final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                        switch (id_intent) {
                            case 181: {//sales
                                SharedPreferences sales = getSharedPreferences("Save sales", MODE_PRIVATE);
                                final SharedPreferences.Editor inp_sales = sPref_saveASL.edit();
                                Intent added = new Intent();
                                inpASL.putString("JSONArray", jsonArray.toString());
                                inpASL.apply();
                                setResult(RESULT_OK, added);
                                finish();
                            }
                            break;
                            case 191: {//invoice
                                Intent added = new Intent();
                                inpASL.putString("JSONArray", jsonArray.toString());
                                inpASL.apply();
                                setResult(RESULT_OK, added);
                                finish();
                            }
                            break;
                            case 171: {//inventorie
                                Intent added = new Intent();
                                setResult(RESULT_OK, added);
                                finish();
                            }
                            break;
                            default: {
                                Intent added = new Intent();
                                setResult(RESULT_OK, added);
                                finish();
                            }
                        }
                    }
                });
                exit.show();
            }


        }
        else{
            index_clecked_item_name -= 2;
            setTitle(list_name_clicked_item.get(index_clecked_item_name));
            index_clecked_item_name +=1;
            list_name_clicked_item.remove(index_clecked_item_name);
            //last_clicked_index_item = siz - 1;
            index_clecked_item -= 1;
            mGUIDAssortment =list_clicked_item.get(index_clecked_item);
            asl_list.clear();
            init_home_assortment(mGUIDAssortment);
            simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                    new String[]{"Name","icon","mPriceAssortment","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
            list_assortments.setAdapter(simpleAdapterASL);
            list_clicked_item.remove(index_clecked_item);
        }
       // }
    }

    private static void order_sort(ArrayList<HashMap<String, Object>> asl_list) {


        Collections.sort(asl_list, new Comparator<HashMap<String, Object>>() {

            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {

                String x1 = o1.get("Name").toString();
                String x2 = o2.get("Name").toString();
                int sComp = x1.compareTo(x2);

                if (sComp != 0) {
                    return sComp;
                } else {
                    String xy1 = String.valueOf(o1.get("Folder_is"));
                    String xy2 = String.valueOf(o2.get("Folder_is"));
                    return xy1.compareTo (xy2);
                }
            }});
    }
    private static void order_sortAll(ArrayList<HashMap<String, Object>> asl_list) {


        Collections.sort(asl_list, new Comparator<HashMap<String, Object>>() {

            public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {

                String xy1 = String.valueOf(o1.get("Folder_is"));
                String xy2 = String.valueOf(o2.get("Folder_is"));
                int sComp = xy2.compareTo(xy1);

                if (sComp != 0) {
                    return sComp;
                } else {
                    String x1 = o1.get("Name").toString();
                    String x2 = o2.get("Name").toString();
                    return x1.compareTo (x2);
                }
            }});
    }
}
