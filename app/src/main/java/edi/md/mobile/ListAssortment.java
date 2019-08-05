package edi.md.mobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
    public String cnt, WareUid,UserId;
    FloatingActionButton save_assortments;
    ListView list_assortments;
    SearchView search_asl;
    String ip_,port_, mGUIDAssortment;
    ProgressDialog pgH;
    SimpleAdapter simpleAdapterASL;
    JSONObject mNewAdd_Assortemnt,sendRevision,json_item_added_inventory;
    String mNameAssortment, mPriceAssortment;
    JSONArray mArrayAdded_Assortments, mFoldersSelected,array_added_items_inventroy;
    List<String> mList_Clicked_item =new ArrayList<>();
    List<String> mList_Clicked_item_Name =new ArrayList<>();
    int index_clecked_item=0,index_clecked_item_name=1,ACTIVITY_TRANSFER = 141,
            ACTIVITY_SALES=181,ACTIVITY_INVENTORY=171,ACTIVITY_INVOICE = 191,ACTIVITY_CHECK_PRICE=161,
            ACTIVITY_STOCK_ASSORTMENT=151,REQUEST_CODE_COUNT_INVOICE = 333,
            REQUEST_CODE_COUNT_INVENTORY = 444, REQUEST_CODE_Count_LIST_ASSORTMENT = 303;
    Boolean mIsFolderAssortment;
    Handler handler;
    TimerTask timerTask;
    Timer t;
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();
    Toolbar mToolbar;
    Variables myapp;
    boolean multi_folders = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                if (mList_Clicked_item.size() == 0) {
                    if (mArrayAdded_Assortments.length() == 0) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                    else Exit_Dialog();
                }
                else{
                    index_clecked_item_name -= 2;
                    setTitle(mList_Clicked_item_Name.get(index_clecked_item_name));
                    index_clecked_item_name +=1;
                    mList_Clicked_item_Name.remove(index_clecked_item_name);
                    index_clecked_item -= 1;
                    mGUIDAssortment = mList_Clicked_item.get(index_clecked_item);
                    asl_list.clear();
                    showAssortmentFromID(mGUIDAssortment);
                    mList_Clicked_item.remove(index_clecked_item);
                }
            }
            break;
            case R.id.m_home: {
                mList_Clicked_item.clear();
                mList_Clicked_item_Name.clear();
                index_clecked_item_name=1;
                mList_Clicked_item_Name.add(0,getResources().getString(R.string.header_list_assortment));
                setTitle(getResources().getString(R.string.header_list_assortment));
                index_clecked_item = 0;
                asl_list.clear();
                showAssortmentFromID("00000000-0000-0000-0000-000000000000");
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
    protected void onCreate(final Bundle savedInstanceState) {
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

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");
        mArrayAdded_Assortments = new JSONArray();
        array_added_items_inventroy = new JSONArray();
        WareUid = WorkPlace.getString("Uid","0");
        myapp =((Variables)getApplication());
        mList_Clicked_item_Name.add(0,getResources().getString(R.string.header_list_assortment));
        setTitle(getResources().getString(R.string.header_list_assortment));
        mGUIDAssortment = "00000000-0000-0000-0000-000000000000";
        t = new Timer();

        if(WareUid.equals("0")){
            Intent asl = getIntent();
            WareUid = asl.getStringExtra("WareID");
        }

        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
        pgH.setIndeterminate(true);
        pgH.setCancelable(false);

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if(msg.what == 10) {
                    SortAssortmentList(asl_list);
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","Price","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    pgH.dismiss();
                    ((Variables)getApplication()).setDownloadASLVariable(true);
                    list_assortments.setAdapter(simpleAdapterASL);
                }
            }

        };

        boolean dwnlASL =((Variables)getApplication()).getDownloadASLVariable();
        if(dwnlASL){
            showAssortmentFromID("00000000-0000-0000-0000-000000000000");
        }
        else {
            pgH.show();
            downloadShowAssortment();
        }

        list_assortments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences sPref = getSharedPreferences("Save touch assortiment", MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();

                mGUIDAssortment = (String) asl_list.get(position).get("ID");
                mIsFolderAssortment = (boolean) asl_list.get(position).get("Folder_is");
                mPriceAssortment = (String) asl_list.get(position).get("Price");
                mNameAssortment = (String) asl_list.get(position).get("Name");
                String mCodeAssortment = (String) asl_list.get(position).get("Code");
                String mBarcodeAssortment = (String) asl_list.get(position).get("BarCode");
                String mIncomePriceAssortment = (String) asl_list.get(position).get("IncomePrice");
                String mMarkingAssortment = (String) asl_list.get(position).get("Marking");
                String mRemainAssortment = (String) asl_list.get(position).get("Remain");
                String mUnitAssortment = (String) asl_list.get(position).get("Unit");
                String mUnitPriceAssortment = (String) asl_list.get(position).get("UnitPrice");
                String mUnitInPackageAssortment = (String) asl_list.get(position).get("UnitInPackage");

                if(mUnitPriceAssortment!=null){
                    mUnitPriceAssortment=mUnitPriceAssortment.replace(",",".");
                    Double priceunit = Double.valueOf(mUnitPriceAssortment);
                    mUnitPriceAssortment =String.format("%.2f",priceunit);
                }

                String alow_integer =(String) asl_list.get(position).get("AllowNonIntegerSale");
                final String ParentUid = (String) asl_list.get(position).get("Parent_ID");

                if (!mIsFolderAssortment) {
                    String price_replaced=null;
                    if(mPriceAssortment!=null){
                        price_replaced = mPriceAssortment.replace(getResources().getString(R.string.txt_list_asl_view_valuta),"");
                        price_replaced = price_replaced.replace(getResources().getString(R.string.txt_list_asl_view_price),"");
                        price_replaced = price_replaced.replace(",",".");
                    }
                    
                    ed.putString("Guid_Assortiment", mGUIDAssortment);
                    ed.putString("Code_Assortiment", mCodeAssortment);
                    ed.putString("BarCode_Assortiment", mBarcodeAssortment);
                    ed.putString("Name_Assortiment", mNameAssortment);
                    ed.putString("Price_Assortiment", price_replaced);
                    ed.putString("IncomePrice", mIncomePriceAssortment);
                    ed.putString("AllowNonIntegerSale", alow_integer);
                    ed.putString("Remain",mRemainAssortment);
                    ed.putString("Marking",mMarkingAssortment);
                    ed.apply();

                    Intent getActivity = getIntent();
                    final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                    final boolean auto_send =  getActivity.getBooleanExtra("AutoCount",false);

                    if(id_intent == ACTIVITY_TRANSFER){
                        addAssortmentClicked(mNameAssortment,mGUIDAssortment,price_replaced,mCodeAssortment,mBarcodeAssortment,mUnitAssortment,mUnitPriceAssortment,mUnitInPackageAssortment);
                        Intent count_activity = new Intent(".CountListAssortmentMobile");///
                        startActivityForResult(count_activity, REQUEST_CODE_Count_LIST_ASSORTMENT);
                    }
                    else if (id_intent == ACTIVITY_STOCK_ASSORTMENT){
                        addAssortmentClicked(mNameAssortment,mGUIDAssortment,price_replaced,mCodeAssortment,mBarcodeAssortment,mUnitAssortment,mUnitPriceAssortment,mUnitInPackageAssortment);
                        Intent count_activity = new Intent(".CountListAssortmentMobile");///
                        startActivityForResult(count_activity, REQUEST_CODE_Count_LIST_ASSORTMENT);
                    }
                    else if (id_intent == ACTIVITY_SALES){
                        addAssortmentClicked(mNameAssortment,mGUIDAssortment,price_replaced,mCodeAssortment,mBarcodeAssortment,mUnitAssortment,mUnitPriceAssortment,mUnitInPackageAssortment);
                        Intent count_activity = new Intent(".CountListAssortmentMobile");
                        startActivityForResult(count_activity, REQUEST_CODE_Count_LIST_ASSORTMENT);
                    }
                    else if (id_intent == ACTIVITY_INVOICE){
                        mNewAdd_Assortemnt = new JSONObject();
                        try {
                            mNewAdd_Assortemnt.put("AssortimentName", mNameAssortment);
                            mNewAdd_Assortemnt.put("AssortimentUid", mGUIDAssortment);
                            mNewAdd_Assortemnt.put("IncomePrice", mIncomePriceAssortment);
                            mNewAdd_Assortemnt.put("Price", price_replaced);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                        }
                        Intent count_activity = new Intent(".CountInvoiceFromTouchMobile");
                        startActivityForResult(count_activity, REQUEST_CODE_COUNT_INVOICE);
                    }
                    else if (id_intent == ACTIVITY_INVENTORY){
                        SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                        SharedPreferences.Editor add_count = SaveCount.edit();

                        if(auto_send) {
                            json_item_added_inventory = new JSONObject();
                            sendRevision = new JSONObject();

                            SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                            String RevisionID = Revision.getString("RevisionID", "");
                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                            pgH.setIndeterminate(true);
                            pgH.setCancelable(false);
                            pgH.show();

                            try {
                                boolean isExtist = false;
                                for(int i=0; i <array_added_items_inventroy.length();i++){
                                    JSONObject object= array_added_items_inventroy.getJSONObject(i);
                                    String GUID = object.getString("AssortimentUid");
                                    String count_exist = object.getString("Quantity");
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
                                String ExistingCount = SaveCount.getString(mGUIDAssortment,"0");
                                ExistingCount = ExistingCount.replace(",",".");
                                Double countExist = Double.valueOf(ExistingCount);
                                Double total_count = countExist + 1;
                                add_count.putString(mGUIDAssortment,String.format("%.2f",total_count));
                                add_count.apply();
                                sendRevision.put("Assortiment", mGUIDAssortment);
                                sendRevision.put("Quantity", "1");
                                sendRevision.put("RevisionID", RevisionID);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                            }
                            URL generateSaveLine = SaveRevisionLine(ip_, port_);
                            new AsyncTask_SaveRevisionLine().execute(generateSaveLine);
                        }
                        else{
                            json_item_added_inventory = new JSONObject();
                            mNewAdd_Assortemnt = new JSONObject();

                            try {
                                mNewAdd_Assortemnt.put("AssortimentName", mNameAssortment);
                                mNewAdd_Assortemnt.put("AssortimentUid", mGUIDAssortment);
                                mNewAdd_Assortemnt.put("Price", price_replaced);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                            }
                            Intent count_activity = new Intent(".CountListAssortmentMobile");
                            count_activity.putExtra("NonAutoCount_from_Inventory", 717);
                            startActivityForResult(count_activity, REQUEST_CODE_COUNT_INVENTORY);
                        }
                    }
                    else if (id_intent == ACTIVITY_CHECK_PRICE){
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
                    }
                }
                else {
                    mList_Clicked_item.add(index_clecked_item, ParentUid);
                    mList_Clicked_item_Name.add(index_clecked_item_name, mNameAssortment);
                    setTitle(mNameAssortment);
                    index_clecked_item += 1;
                    index_clecked_item_name +=1;
                    asl_list.clear();
                    asl_list = myapp.get_AssortimentFromParent(mGUIDAssortment);
                    SortAssortmentList(asl_list);
                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                            new String[]{"Name","icon","Price","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
                    list_assortments.setAdapter(simpleAdapterASL);
                }
            }
        });

        save_assortments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getActivity = getIntent();
                final int id_intent = getActivity.getIntExtra("ActivityCount", 101);

                if (id_intent == ACTIVITY_TRANSFER){
                    saveCloseActivity("AssortmentTransferAddedArray",mArrayAdded_Assortments.toString());
                }
                else if (id_intent == ACTIVITY_STOCK_ASSORTMENT){
                    saveCloseActivity("AssortmentStockAddedArray",mArrayAdded_Assortments.toString());
                }
                else if (id_intent == ACTIVITY_SALES){
                    saveCloseActivity("AssortmentSalesAddedArray",mArrayAdded_Assortments.toString());
                }
                else if (id_intent == ACTIVITY_INVOICE){
                    saveCloseActivity("AssortmentInvoiceAddedArray",mArrayAdded_Assortments.toString());
                }
                else if (id_intent == ACTIVITY_INVENTORY){
                    saveCloseActivity("AssortmentInventoryAddedArray",array_added_items_inventroy.toString());
                }
                else if (id_intent == ACTIVITY_CHECK_PRICE){
                    setResult(RESULT_OK);
                    finish();
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
                    mList_Clicked_item.clear();
                    mList_Clicked_item_Name.clear();
                    index_clecked_item_name=1;
                    mList_Clicked_item_Name.add(0,getResources().getString(R.string.header_list_assortment));
                    setTitle(getResources().getString(R.string.header_list_assortment));
                    index_clecked_item=0;
                    mGUIDAssortment ="00000000-0000-0000-0000-000000000000";
                    showAssortmentFromID(mGUIDAssortment);
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
                            searchAssortment(newText);
                        }
                    }
                });
            }
        };

    }
    private void downloadShowAssortment() {
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
                            mFoldersSelected = new JSONArray(selectedUidJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                        }
                        if(mFoldersSelected.length()>0)
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
                                for (int j = 0; j< mFoldersSelected.length(); j++) {
                                    String guidArray = null;
                                    try {
                                        guidArray = mFoldersSelected.getString(j);
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
                            }
                            else{
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
                                        asl_.put("Price", Asl_Price);
                                        asl_.put("IncomePrice", incomePrice);
                                        asl_.put("Unit", unitary);
                                        asl_.put("UnitPrice", finalUnitPrice);
                                        asl_.put("UnitInPackage", UnitInPackage);
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
                        ((Variables)getApplication()).appendLog(t.getMessage(), ListAssortment.this);
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
                    }
                });
            }
        });
        downloadASL.start();
    }
    private void showAssortmentFromID(String id) {
        asl_list.clear();
        SharedPreferences CheckUidFolder =getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
        String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");

        try {
            mFoldersSelected = new JSONArray(selectedUidJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(mFoldersSelected.length()>0) {
            for (int j = 0; j < mFoldersSelected.length(); j++) {
                String guidArray = null;
                try {
                    guidArray = mFoldersSelected.getString(j);
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
        SortAssortmentList(asl_list);
        simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                new String[]{"Name","icon","Price","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
        list_assortments.setAdapter(simpleAdapterASL);
    }
    private void searchAssortment(String search_text) {
        asl_list= myapp.get_Search_Assortment(search_text);
        SortAssortmentList(asl_list);
        simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                new String[]{"Name","icon","Price","Bar_code"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list});
        list_assortments.setAdapter(simpleAdapterASL);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_Count_LIST_ASSORTMENT) {
            if (resultCode == RESULT_OK) {
                cnt = data.getStringExtra("count");
                try {
                    mNewAdd_Assortemnt.put("Count", cnt);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
                mArrayAdded_Assortments.put(mNewAdd_Assortemnt);
            }
        }
        else if (requestCode==REQUEST_CODE_COUNT_INVOICE){
            if (resultCode == RESULT_OK) {
                String  Count = data.getStringExtra("Count");
                String  SalePrice = data.getStringExtra("SalePrice");
                String  IncomSum = data.getStringExtra("IncomSum");
                String  Suma = data.getStringExtra("Suma");

                try {
                    mNewAdd_Assortemnt.put("Count", Count);
                    mNewAdd_Assortemnt.put("SalePrice", SalePrice);
                    mNewAdd_Assortemnt.put("IncomPrice", IncomSum);
                    mNewAdd_Assortemnt.put("Suma", Suma);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
                mArrayAdded_Assortments.put(mNewAdd_Assortemnt);
            }
        }
        else if (requestCode==REQUEST_CODE_COUNT_INVENTORY){
            if (resultCode == RESULT_OK) {
                SharedPreferences SaveCount = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
                SharedPreferences.Editor add_count = SaveCount.edit();
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
                    String ExistingCount = SaveCount.getString(mGUIDAssortment,"0");
                    ExistingCount = ExistingCount.replace(",",".");
                    Double countExist = Double.valueOf(ExistingCount);
                    Double total_count = countExist + new_count;
                    add_count.putString(mGUIDAssortment,String.format("%.2f",total_count));
                    add_count.apply();
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
        if (mList_Clicked_item.size() == 0) {
            if (mArrayAdded_Assortments.length() == 0) {
                setResult(RESULT_CANCELED);
                finish();
            }
            else Exit_Dialog();
        }
        else{
            index_clecked_item_name -= 2;
            setTitle(mList_Clicked_item_Name.get(index_clecked_item_name));
            index_clecked_item_name +=1;
            mList_Clicked_item_Name.remove(index_clecked_item_name);
            index_clecked_item -= 1;
            mGUIDAssortment = mList_Clicked_item.get(index_clecked_item);
            asl_list.clear();
            showAssortmentFromID(mGUIDAssortment);
            mList_Clicked_item.remove(index_clecked_item);
        }
    }
    private static void SortAssortmentList(ArrayList<HashMap<String, Object>> asl_list) {
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
    private void Exit_Dialog(){
        AlertDialog.Builder exit = new AlertDialog.Builder(context);
        exit.setTitle(getResources().getString(R.string.msg_dialog_title_atentie));
        exit.setMessage(getResources().getString(R.string.msg_dialog_list_asl_document_not_saved));
        exit.setNegativeButton(getResources().getString(R.string.toggle_btn_check_remain_nu), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        exit.setPositiveButton(getResources().getString(R.string.toggle_btn_check_remain_da), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent getActivity = getIntent();
                final int id_intent = getActivity.getIntExtra("ActivityCount", 101);

                if (id_intent == ACTIVITY_SALES){
                    Intent sales = new Intent();
                    sales.putExtra("AssortmentSalesAddedArray", mArrayAdded_Assortments.toString());
                    setResult(RESULT_OK, sales);
                    finish();
                }
                else if (id_intent == ACTIVITY_INVOICE){
                    Intent invoice = new Intent();
                    invoice.putExtra("AssortmentInvoiceAddedArray", mArrayAdded_Assortments.toString());
                    setResult(RESULT_OK, invoice);
                    finish();
                }
                else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        exit.show();
    }
    private void addAssortmentClicked(String name,String id,String price,String code,String barcode,String unit,String unitPrice,String unitInPackage){
        mNewAdd_Assortemnt = new JSONObject();
        try {
            mNewAdd_Assortemnt.put("AssortimentName", name);
            mNewAdd_Assortemnt.put("AssortimentUid", id);
            mNewAdd_Assortemnt.put("Price", price);
            mNewAdd_Assortemnt.put("Code",code);
            mNewAdd_Assortemnt.put("Barcode",barcode);
            mNewAdd_Assortemnt.put("Unit",unit);
            mNewAdd_Assortemnt.put("UnitPrice",unitPrice);
            mNewAdd_Assortemnt.put("UnitInPackage",unitInPackage);
        } catch (JSONException e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
        }
    }
    private void saveCloseActivity(String nameKey,String value){
        Intent intent = new Intent();
        intent.putExtra(nameKey, value);
        setResult(RESULT_OK, intent);
        finish();
    }
}