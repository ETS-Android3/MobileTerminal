package md.intelectsoft.stockmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import md.intelectsoft.stockmanager.NetworkUtils.ApiRetrofit;
//import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveRevisionLineBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AssortmentListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
//import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.ResponseSimple;
//import md.intelectsoft.stockmanager.NetworkUtils.Services.CommandService;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
//import md.intelectsoft.stockmanager.Utils.AssortmentParcelable;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListAssortment extends AppCompatActivity {
    final Context context = this;
    public String cnt, workPlaceID, workPlaceName,UserId;
    FloatingActionButton save_assortments;
    ListView list_assortments;
    SearchView search_asl;
    String url_, mGUIDAssortment;
    ProgressDialog pgH;
    SimpleAdapter simpleAdapterASL;
    JSONObject mNewAdd_Assortemnt,sendRevision,json_item_added_inventory;
    String mNameAssortment, mPriceAssortment;
    JSONArray mArrayAdded_Assortments, mFoldersSelected,array_added_items_inventroy;
    List<String> mList_Clicked_item =new ArrayList<>();
    List<String> lastClickedItemName =new ArrayList<>();
    int index_clecked_item=0,index_clecked_item_name=1,ACTIVITY_TRANSFER = 141,
            ACTIVITY_SALES=181,ACTIVITY_INVENTORY=171,ACTIVITY_INVOICE = 191,ACTIVITY_CHECK_PRICE=161,
            ACTIVITY_STOCK_ASSORTMENT=151,REQUEST_CODE_COUNT_INVOICE = 333,
            REQUEST_CODE_COUNT_INVENTORY = 444, REQUEST_CODE_Count_LIST_ASSORTMENT = 303,
            REQUEST_CODE_COUNT_STOCK_ASL = 404, ACTIVITY_SET_BARCODE = 525 ,REQUEST_SETBARCODE = 252, POSITION_CLICKED;
    Boolean mIsFolderAssortment;
    TimerTask timerTask;
    Timer timer;
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();
    Toolbar mToolbar;
    boolean multi_folders = false;

    //CommandService commandService;

    TerminalAPI terminalAPI;

    public static final String AssortimentClickentSendIntent = "AssortimentClicked";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                if (mList_Clicked_item.size() == 0) {
                    if (mArrayAdded_Assortments.length() == 0) {
                        Intent getActivity = getIntent();
                        final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
                        if (id_intent == ACTIVITY_INVENTORY){
                            saveCloseActivity("AssortmentInventoryAddedArray",array_added_items_inventroy.toString());
                        }else{
                            setResult(RESULT_CANCELED);
                            finish();
                        }

                    }
                    else Exit_Dialog();
                }
                else{
                    index_clecked_item_name -= 2;
                    setTitle(lastClickedItemName.get(index_clecked_item_name));
                    index_clecked_item_name +=1;
                    lastClickedItemName.remove(index_clecked_item_name);
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
                lastClickedItemName.clear();
                index_clecked_item_name=1;
                lastClickedItemName.add(0,getResources().getString(R.string.header_list_assortment));
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
        list_assortments = findViewById(R.id.LW_assortment_list);
        search_asl = findViewById(R.id.search_text);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //commandService = ApiRetrofit.getCommandService(ListAssortment.this);

        terminalAPI = TerminalRetrofitClient.getApiTerminalService(SPFHelp.getInstance().getString("URI", "0.0.0.0:1111"));

        UserId = SPFHelp.getInstance().getString("UserId","");
        mArrayAdded_Assortments = new JSONArray();
        array_added_items_inventroy = new JSONArray();

        workPlaceName = getIntent().getStringExtra("WareName");
        workPlaceID = getIntent().getStringExtra("WorkPlaceID");

        lastClickedItemName.add(0,getResources().getString(R.string.header_list_assortment));
        setTitle(getResources().getString(R.string.header_list_assortment));
        mGUIDAssortment = "00000000-0000-0000-0000-000000000000";
        timer = new Timer();

        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
        pgH.setIndeterminate(true);
        pgH.setCancelable(false);

        if(BaseApp.getInstance().getDownloadASLVariable()){
            showAssortmentFromID("00000000-0000-0000-0000-000000000000");
        }
        else {
            pgH.show();
            downloadShowAssortment();
        }

        list_assortments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POSITION_CLICKED = position;
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
                String mAlowNonIntegerSalesAssortment =(String) asl_list.get(position).get("AllowNonIntegerSale");
                final String ParentUid = (String) asl_list.get(position).get("Parent_ID");

                //проверяем если выбраный товар не папка это не папка
//                if (!mIsFolderAssortment) {
//                    Assortment assortment = new Assortment();
//                    assortment.setBarCode(mBarcodeAssortment);
//                    assortment.setCode(mCodeAssortment);
//                    assortment.setName(mNameAssortment);
//                    assortment.setPrice(mPriceAssortment);
//                    assortment.setMarking(mMarkingAssortment);
//                    assortment.setRemain(mRemainAssortment);
//                    assortment.setUnit(mUnitAssortment);
//                    assortment.setUnitPrice(mUnitPriceAssortment);
//                    assortment.setUnitInPackage(mUnitInPackageAssortment);
//                    assortment.setIncomePrice(mIncomePriceAssortment);
//                    assortment.setAssortimentID(mGUIDAssortment);
//                    assortment.setAllowNonIntegerSale(mAlowNonIntegerSalesAssortment);
//                    final AssortmentParcelable assortmentParcelable = new AssortmentParcelable(assortment);
//
//                    addAssortmentClicked(mNameAssortment,mGUIDAssortment,mPriceAssortment,mCodeAssortment,mBarcodeAssortment,mUnitAssortment,mUnitPriceAssortment,mUnitInPackageAssortment);
//                    Intent count_activity = new Intent(".CountListAssortmentMobile");
//                    count_activity.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
//                    count_activity.putExtra("WareUid", workPlaceID);
//
//                    Intent getActivity = getIntent();
//                    final int id_intent = getActivity.getIntExtra("ActivityCount", 101);
//                    final boolean auto_send =  getActivity.getBooleanExtra("AutoCount",false);
//
//                    //проверяем айди открытого окна  после чего открываем окно с воода кол-во
//                    if(id_intent == ACTIVITY_TRANSFER){
//                        startActivityForResult(count_activity, REQUEST_CODE_Count_LIST_ASSORTMENT);
//                    }
//                    else if (id_intent == ACTIVITY_STOCK_ASSORTMENT){
//                        Intent count_activity_stock = new Intent(ListAssortment.this, CountListOfAssortmentStock.class);
//                        count_activity_stock.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
//                        count_activity.putExtra("WareUid", workPlaceID);
//                        startActivityForResult(count_activity_stock, REQUEST_CODE_COUNT_STOCK_ASL);
//                    }
//                    else if (id_intent == ACTIVITY_SALES){
//                        startActivityForResult(count_activity, REQUEST_CODE_Count_LIST_ASSORTMENT);
//                    }
//                    else if (id_intent == ACTIVITY_INVOICE){
//                        Intent count_activity_invoice = new Intent(ListAssortment.this,CountListOfAssortmentInvoice.class);
//                        count_activity_invoice.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
//                        startActivityForResult(count_activity_invoice, REQUEST_CODE_COUNT_INVOICE);
//                    }
//                    else if (id_intent == ACTIVITY_INVENTORY){
//                        SharedPreferences SaveCountEnumeratorAssortmentScanned = getSharedPreferences("SaveCountInventory", MODE_PRIVATE);
//                        SharedPreferences SaveCountName = getSharedPreferences("SaveNameInventory", MODE_PRIVATE);
//                        SharedPreferences.Editor add_count_clicked = SaveCountEnumeratorAssortmentScanned.edit();
//                        SharedPreferences.Editor add_name = SaveCountName.edit();
//                        //проверяем если стоит вручную вести кол-во или по 1 автоматически
//                        if(auto_send) {
//                            json_item_added_inventory = new JSONObject();
//
//                            SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
//                            String RevisionID = Revision.getString("RevisionID", "");
//
//                            SharedPreferences revisionHistory = getSharedPreferences(RevisionID, MODE_PRIVATE);
//
//                            pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
//                            pgH.setIndeterminate(true);
//                            pgH.setCancelable(false);
//                            pgH.show();
//                            try {
//                                boolean isExtist = false;
//                                //добавляю в массив нажатого товара чтобы потом отабразить какие товары были отправлены из окна с списком товаров
//                                for(int i=0; i <array_added_items_inventroy.length();i++){
//                                    JSONObject object = array_added_items_inventroy.getJSONObject(i);
//                                    String GUID = object.getString("AssortimentUid");
//                                    String count_exist = object.getString("Quantity");
//
//                                    double cant_exist_double = 0.00;
//                                    try{
//                                        cant_exist_double = Double.parseDouble(count_exist);
//                                    }catch (Exception e){
//                                        count_exist = count_exist.replace(",",".");
//                                        cant_exist_double = Double.parseDouble(count_exist);
//                                    }
//                                    //если товар уже есть то сумирую кол-во
//                                    if (GUID.equals(mGUIDAssortment)){
//                                        cant_exist_double = cant_exist_double +1;
//                                        object.put("Quantity",String.valueOf(cant_exist_double));
//                                        isExtist=true;
//                                        break;
//                                    }
//                                }
//                                //если нажатый товар в списке нет, то добавляю
//                                if (!isExtist){
//                                    json_item_added_inventory.put("AssortimentName", mNameAssortment);
//                                    json_item_added_inventory.put("AssortimentUid", mGUIDAssortment);
//                                    json_item_added_inventory.put("Quantity", "1");
//                                    array_added_items_inventroy.put(json_item_added_inventory);
//                                }
//                                //получаю товар что уже был добавлен и добавляю 1 шт и сохраняем
//                                String ExistingCount = SaveCountEnumeratorAssortmentScanned.getString(mGUIDAssortment,"0.00");
//                                double countExist = 0.00;
//                                try{
//                                    countExist = Double.parseDouble(ExistingCount);
//                                }catch (Exception e){
//                                    ExistingCount = ExistingCount.replace(",",".");
//                                    countExist = Double.parseDouble(ExistingCount);
//                                }
//                                Double total_count = countExist + 1;
//
//                                add_count_clicked.putString(mGUIDAssortment,String.format("%.2f",total_count));
//                                add_name.putString(mGUIDAssortment,mNameAssortment);
//                                add_name.apply();
//                                add_count_clicked.apply();
//
//                                //надо отправить сервису для сохранения нажатого товара
//                                SaveRevisionLineBody revisionLineBody = new SaveRevisionLineBody();
//                                revisionLineBody.setAssortiment(mGUIDAssortment);
//                                revisionLineBody.setRevisionID(RevisionID);
//                                revisionLineBody.setFinalQuantity(false);
//                                revisionLineBody.setQuantity(1.0);
//
//                                CommandService commandService = ApiRetrofit.getCommandService(ListAssortment.this);
//                                Call<ResponseSimple> call = commandService.saveRevisionLine(revisionLineBody);
//
//                                call.enqueue(new Callback<ResponseSimple>() {
//                                    @Override
//                                    public void onResponse(Call<ResponseSimple> call, Response<ResponseSimple> response) {
//                                        ResponseSimple responseSimple = response.body();
//                                        pgH.dismiss();
//                                        if(responseSimple != null){
//                                            if(responseSimple.getErrorCode() == 0){
//                                                Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_count_added_inventory), Toast.LENGTH_SHORT).show();
//
//                                                revisionHistory.edit().putString("name",mNameAssortment).putString("count", "1").apply();
//                                            }
//                                            else{
//                                                Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_error_code) + responseSimple.getErrorCode() , Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                        else
//                                            Toast.makeText(ListAssortment.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    @Override
//                                    public void onFailure(Call<ResponseSimple> call, Throwable t) {
//                                        pgH.dismiss();
//                                        Toast.makeText(ListAssortment.this, t.getMessage() , Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        //если не стоит автоматическое добавление товара в ревизию
//                        else{
//                            //создаю новый обьект JSON ажатого товара
//                            json_item_added_inventory = new JSONObject();
//                            mNewAdd_Assortemnt = new JSONObject();
//
//                            try {
//                                mNewAdd_Assortemnt.put("AssortimentName", mNameAssortment);
//                                mNewAdd_Assortemnt.put("AssortimentUid", mGUIDAssortment);
//                                mNewAdd_Assortemnt.put("Price",mPriceAssortment);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
//                            }
//                            //открываю окно с вводом кол-во
//                            Intent count_activity_inventory = new Intent(ListAssortment.this,CountListOfAssortmentInventory.class);
//                            count_activity_inventory.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
//                            startActivityForResult(count_activity_inventory, REQUEST_CODE_COUNT_INVENTORY);
//                        }
//                    }
//                    else if (id_intent == ACTIVITY_CHECK_PRICE){
//                        setResult(RESULT_OK,count_activity);
//                        finish();
//                    }
//                    else if (id_intent == ACTIVITY_SET_BARCODE){
//                        Intent setBarcode = new Intent(ListAssortment.this,SetAssortmentBarcode.class);
//                        setBarcode.putExtra(AssortimentClickentSendIntent,assortmentParcelable);
//                        startActivityForResult(setBarcode, REQUEST_SETBARCODE);
//                    }
//                }
//                else {
//                    /*
//                    если это папка то ставлю в массив этапы нажатых папок и имя, для отображения в баре и для возврата на папку вверх
//                    после чего получаю все итемы данной папке и отображаю на экран
//                     */
//                    mList_Clicked_item.add(index_clecked_item, ParentUid);
//                    lastClickedItemName.add(index_clecked_item_name, mNameAssortment);
//                    setTitle(mNameAssortment);
//                    index_clecked_item += 1;
//                    index_clecked_item_name +=1;
//                    asl_list.clear();
//                    asl_list = BaseApp.getInstance().get_AssortimentFromParent(mGUIDAssortment);
//                    SortAssortmentList(asl_list);
//                    simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
//                            new String[]{"Name","icon","PriceWithText","Bar_code","Unit"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list,R.id.txt_unit});
//                    list_assortments.setAdapter(simpleAdapterASL);
//                }
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
                else if (id_intent == ACTIVITY_SET_BARCODE){
                    finish();
                }
            }
        });

        search_asl.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if(!searchText.equals("")) {
                    timer.cancel();
                    timer = new Timer();
                    startTimetask(searchText);
                    timer.schedule(timerTask, 1200);
                }else {
                    mList_Clicked_item.clear();
                    lastClickedItemName.clear();
                    index_clecked_item_name=1;
                    lastClickedItemName.add(0,getResources().getString(R.string.header_list_assortment));
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
        Call<AssortmentListResult> call = terminalAPI.getAssortmentListForStock(UserId, workPlaceID);
        call.enqueue(new Callback<AssortmentListResult>() {
            @Override
            public void onResponse(Call<AssortmentListResult> call, Response<AssortmentListResult> response) {
                AssortmentListResult assortiment_body = response.body();

                if(assortiment_body.getAssortments() == null){
                    Toast.makeText(ListAssortment.this,"Error to download assortment\nBody in response is null!", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Assortment> assortmentListData = assortiment_body.getAssortments();
                SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
                String selectedUidJSON = CheckUidFolder.getString("selected_Uid_Array","[]");

                try {
                    mFoldersSelected = new JSONArray(selectedUidJSON);
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
                multi_folders = mFoldersSelected.length() > 0;

//                for (int i=0; i<assortmentListData.size();i++){
//                    HashMap<String, Object> asl_ = new HashMap<>();
//                    String asl_name= assortmentListData.get(i).getName();
//                    String uid_asl = assortmentListData.get(i).getAssortimentID();
//                    String parent_uid_asl = assortmentListData.get(i).getAssortimentParentID();
//                    String price =   assortmentListData.get(i).getPrice();
//                    String code_asl =  assortmentListData.get(i).getCode();
//                    String barcode_asl = assortmentListData.get(i).getBarCode();
//                    String allow_integer =  assortmentListData.get(i).getAllowNonIntegerSale();
//                    String incomePrice= assortmentListData.get(i).getIncomePrice();
//                    String marking = assortmentListData.get(i).getMarking();
//                    String unitary= assortmentListData.get(i).getUnit();
//                    String finalUnitPrice= assortmentListData.get(i).getUnitPrice();
//                    String UnitInPackage= assortmentListData.get(i).getUnitInPackage();
//                    boolean is_folder= assortmentListData.get(i).getIsFolder();
//
//                    double priceunit = Double.parseDouble(price);
//
//                    price = String.format("%.2f",priceunit);
//
//                    BaseApp.getInstance().add_AssortimentID(uid_asl,assortmentListData.get(i));
//
//                    if(multi_folders){
//                        for (int j = 0; j< mFoldersSelected.length(); j++) {
//                            String guidArray = null;
//                            try {
//                                guidArray = mFoldersSelected.getString(j);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
//                            }
//                            boolean contains_id = false;
//                            if (guidArray != null) {
//                                contains_id = uid_asl.contains(guidArray);
//                            }
//                            if (contains_id) {
//                                asl_.put("Folder_is", true);
//                                asl_.put("Parent_ID", parent_uid_asl);
//                                asl_.put("Name", asl_name);
//                                asl_.put("ID", uid_asl);
//                                asl_.put("icon", R.drawable.folder_open_black_48dp);
//                                asl_list.add(asl_);
//                            }
//                        }
//                    }
//                    else{
//                        if(parent_uid_asl.equals("00000000-0000-0000-0000-000000000000")) {
//                            if (!is_folder) {
//                                asl_.put("Folder_is", false);
//                                asl_.put("icon", R.drawable.assortiment_item);
//                                asl_.put("Name", asl_name);
//                                asl_.put("ID", uid_asl);
//                                asl_.put("Code", code_asl);
//                                asl_.put("Marking",marking);
//                                asl_.put("Bar_code", getResources().getString(R.string.txt_list_asl_view_barcode) + barcode_asl);
//                                asl_.put("BarCode", barcode_asl);
//                                asl_.put("AllowNonIntegerSale", allow_integer);
//                                asl_.put("Price", price);
//                                asl_.put("PriceWithText",getResources().getString(R.string.txt_list_asl_view_price)+ price + getResources().getString(R.string.txt_list_asl_view_valuta));
//                                asl_.put("IncomePrice", incomePrice);
//                                asl_.put("Unit",  " /"+  unitary);
//                                asl_.put("UnitPrice", finalUnitPrice);
//                                asl_.put("UnitInPackage", UnitInPackage);
//                                asl_list.add(asl_);
//                            } else {
//                                asl_.put("Folder_is", true);
//                                asl_.put("Parent_ID", parent_uid_asl);
//                                asl_.put("Name", asl_name);
//                                asl_.put("ID", uid_asl);
//                                asl_.put("Bar_code", "");
//                                asl_.put("icon", R.drawable.folder_open_black_48dp);
//                                asl_list.add(asl_);
//                            }
//                        }
//
//                    }
//                }

                if (ListAssortment.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                pgH.dismiss();
                SortAssortmentList(asl_list);
                simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                        new String[]{"Name","icon","PriceWithText","Bar_code","Unit"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list,R.id.txt_unit});

                ((BaseApp)getApplication()).setDownloadASLVariable(true);
                list_assortments.setAdapter(simpleAdapterASL);
            }

            @Override
            public void onFailure(Call<AssortmentListResult> call, Throwable t) {
                pgH.dismiss();
                ((BaseApp)getApplication()).appendLog(t.getMessage(), ListAssortment.this);
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
                        downloadShowAssortment();
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
    private void showAssortmentFromID(String id) {
        asl_list.clear();
        SharedPreferences CheckUidFolder = getSharedPreferences("SaveFolderFilter", MODE_PRIVATE);
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
                    ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
                }
                HashMap<String, Object> asl_ = new HashMap<>();
                Assortment asl_folder = BaseApp.getInstance().get_AssortimentFromID(guidArray);

                asl_.put("Folder_is", true);
                asl_.put("Parent_ID", asl_folder.getAssortimentParentID());
                asl_.put("Name", asl_folder.getName());
                asl_.put("Bar_code", "");
                asl_.put("ID",  asl_folder.getAssortimentID());
                asl_.put("icon", R.drawable.folder_open_black_48dp);
                asl_list.add(asl_);
            }
        }
        else{
            asl_list =  BaseApp.getInstance().get_AssortimentFromParent(id);
        }
        SortAssortmentList(asl_list);
        simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                new String[]{"Name","icon","PriceWithText","Bar_code","Unit"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list,R.id.txt_unit});
        list_assortments.setAdapter(simpleAdapterASL);
    }

    private void searchAssortment(String search_text) {
        asl_list =  BaseApp.getInstance().get_Search_Assortment(search_text);
        SortAssortmentList(asl_list);
        simpleAdapterASL = new SimpleAdapter(ListAssortment.this, asl_list,R.layout.list_assortiment_view,
                new String[]{"Name","icon","PriceWithText","Bar_code","Unit"}, new int[]{R.id.text_view_asl,R.id.image_view_asl_xm,R.id.txt_price_asl_list,R.id.txt_barcode_asl_list,R.id.txt_unit});
        list_assortments.setAdapter(simpleAdapterASL);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_Count_LIST_ASSORTMENT) {
            if (resultCode == RESULT_OK) {
               if(data!=null)
                   onResultCount(data);
               else
                   Toast.makeText(ListAssortment.this, "Intent count is null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode==REQUEST_CODE_COUNT_INVOICE){
            if (resultCode == RESULT_OK) {
                if(data!=null)
                    onResultInvoice(data);
                else
                    Toast.makeText(ListAssortment.this, "Intent invo is null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode==REQUEST_CODE_COUNT_INVENTORY){
            if (resultCode == RESULT_OK) {
                if (data!=null)
                    onResultInventory(data);
                else
                    Toast.makeText(ListAssortment.this, "Intent inve is null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CODE_COUNT_STOCK_ASL){
            if (resultCode == RESULT_OK) {
                if (data!=null)
                    onResultCount(data);
                else
                    Toast.makeText(ListAssortment.this, "Intent inve is null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_SETBARCODE){
            if(resultCode == RESULT_OK){
                String barcode = data.getStringExtra("Barcode");
                asl_list.get(POSITION_CLICKED).put("Bar_code",getResources().getString(R.string.txt_list_asl_view_barcode) + barcode);
                asl_list.get(POSITION_CLICKED).put("BarCode",barcode);
                simpleAdapterASL.notifyDataSetChanged();
            }
        }
    }
    @Override
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
            setTitle(lastClickedItemName.get(index_clecked_item_name));
            index_clecked_item_name +=1;
            lastClickedItemName.remove(index_clecked_item_name);
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
            ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
        }
    }
    private void saveCloseActivity(String nameKey,String value){
        Intent intent = new Intent();
        intent.putExtra(nameKey, value);
        setResult(RESULT_OK, intent);
        finish();
    }
    private void onResultInventory (Intent data){
        String  Count = data.getStringExtra("Count");
        boolean mFinalCount = data.getBooleanExtra("Final",false);
        double new_count=0.00;
        try{
            new_count = Double.parseDouble(Count);
        }catch (Exception e){
            Count =Count.replace(",",".");
            new_count = Double.parseDouble(Count);
        }
        try {
            boolean isExtist = false;
            for(int i=0; i <array_added_items_inventroy.length();i++){
                JSONObject object = array_added_items_inventroy.getJSONObject(i);

                String GUID = object.getString("AssortimentUid");
                String count_exist = object.getString("Quantity");
                double cant_exist_double = Double.parseDouble(count_exist);
                if (GUID.equals(mGUIDAssortment)){
                    if(mFinalCount){
                        cant_exist_double = new_count;
                    }else{
                        cant_exist_double = cant_exist_double + new_count;
                    }
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
    private void onResultCount(Intent data){
        cnt = data.getStringExtra("count");
        try {
            mNewAdd_Assortemnt.put("Count", cnt);
            mNewAdd_Assortemnt.put("Warehouse", workPlaceID);
            mNewAdd_Assortemnt.put("WareName", workPlaceName);
        } catch (JSONException e) {
            e.printStackTrace();
            ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
        }
        mArrayAdded_Assortments.put(mNewAdd_Assortemnt);
    }
    private void onResultInvoice (Intent data){
        String  Count = data.getStringExtra("Count");
        String  SalePrice = data.getStringExtra("SalePrice");
        String  IncomSum = data.getStringExtra("IncomeSum");
        String  Suma = data.getStringExtra("Suma");

        try {
            mNewAdd_Assortemnt.put("Count", Count);
            mNewAdd_Assortemnt.put("SalePrice", SalePrice);
            mNewAdd_Assortemnt.put("IncomePrice", IncomSum);
            mNewAdd_Assortemnt.put("Suma", Suma);
        } catch (JSONException e) {
            e.printStackTrace();
            ((BaseApp)getApplication()).appendLog(e.getMessage(), ListAssortment.this);
        }
        mArrayAdded_Assortments.put(mNewAdd_Assortemnt);
    }
}