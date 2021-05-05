package edi.md.mobile;

import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static edi.md.mobile.NetworkUtils.NetworkUtils.CreateRevision;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetRevisions;
import static edi.md.mobile.NetworkUtils.NetworkUtils.GetWareHouseList;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_CreateRevision;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetRevision;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetWareHouse;

public class Revisions extends AppCompatActivity {

    Button btn_createRevision,btn_cancel,btn_accept;
    ListView listView;
    JSONArray json_array;
    ProgressDialog pgH;

    String UserId,ip_,port_,uid_selected,NumberRevision,NameRevision,WorkPlaceName,WorkPlaceId;
    Integer WeightPrefix;

    AlertDialog.Builder builderType;
    SimpleAdapter simpleAdapterASL;
    ArrayList<HashMap<String, Object>> stock_List_array = new ArrayList<>();
    ArrayList<HashMap<String, Object>> asl_list = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_revisions);
        Toolbar toolbar = findViewById(R.id.toolbar_list_revision);
        setSupportActionBar(toolbar);

        btn_accept=findViewById(R.id.btn_accept_revision);
        btn_cancel=findViewById(R.id.btn_cancel_list_revision);
        btn_createRevision =findViewById(R.id.btn_new_revision);
        listView=findViewById(R.id.LL_list_revision);
        pgH =new ProgressDialog(Revisions.this);

        final SharedPreferences Settings =getSharedPreferences("Settings", MODE_PRIVATE);
        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        final SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
        final SharedPreferences WorkPlace = getSharedPreferences("Work Place", MODE_PRIVATE);

        UserId = User.getString("UserID","");
        ip_=Settings.getString("IP","");
        port_=Settings.getString("Port","");

        WorkPlaceName = WorkPlace.getString("Name","Nedeterminat");
        WorkPlaceId = WorkPlace.getString("Uid",null);


        pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
        pgH.setIndeterminate(true);
        pgH.setCancelable(false);
        pgH.show();

        URL generateGetRevision = GetRevisions(ip_,port_);
        new AsyncTask_GetRevision().execute(generateGetRevision);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uid_selected!=null){
                    SharedPreferences.Editor inpRev = Revision.edit();
                    inpRev.putString("RevisionName",NameRevision);
                    inpRev.putString("RevisionNumber",NumberRevision);
                    inpRev.putString("RevisionID",uid_selected);
                    inpRev.putInt("WeightPrefix",WeightPrefix);
                    inpRev.putInt("Type",1);
                    inpRev.apply();

                    Intent stockInv= new Intent(".InventoryMobile");
                    startActivity(stockInv);
                    finish();
                }else{
                    Toast.makeText(Revisions.this, getResources().getString(R.string.msg_select_revizie), Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NameRevision = (String)asl_list.get(position).get("Name");
                uid_selected=(String)asl_list.get(position).get("Uid");
                NumberRevision = (String)asl_list.get(position).get("Number");
                WeightPrefix = Integer.valueOf((String)asl_list.get(position).get("WeightPrefix"));
            }
        });
        btn_createRevision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();
                getWareHouse();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_revision, menus);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_close_revision : {
                finish();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void initList_asl(){
        try {
            for (int i=0;i<json_array.length();i++){
                JSONObject json= json_array.getJSONObject(i);
                HashMap<String, Object> asl_ = new HashMap<>();
                String Name = json.getString("Name");
                String RevisionNumber = json.getString("RevisionNumber");
                String Uid = json.getString("RevisionID");
                String prefix = json.getString("WeightPrefix");
                WeightPrefix = Integer.valueOf(prefix);
                if(WorkPlaceName != null && !WorkPlaceName.equals("Nedeterminat")){
                    if(Name.contains(WorkPlaceName)){
                        asl_.put("Name",Name);
                        asl_.put("Number",RevisionNumber);
                        asl_.put("Uid",Uid);
                        asl_.put("WeightPrefix",prefix);
                        asl_list.add(asl_);
                    }
                }
                else{
                    asl_.put("Name",Name);
                    asl_.put("Number",RevisionNumber);
                    asl_.put("Uid",Uid);
                    asl_.put("WeightPrefix",prefix);
                    asl_list.add(asl_);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
        }
        simpleAdapterASL = new SimpleAdapter(this, asl_list,R.layout.show_revision_list, new String[]{"Name","Number"},
                new int[]{R.id.textName_revisionList,R.id.textNumber_revisionList});
        listView.setAdapter(simpleAdapterASL);
    }
    public void show_WareHouse(){

        SimpleAdapter simpleAdapterType = new SimpleAdapter(Revisions.this, stock_List_array,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        builderType = new AlertDialog.Builder(Revisions.this);
        builderType.setTitle(getResources().getString(R.string.txt_header_msg_list_depozitelor));
        builderType.setNegativeButton(getResources().getString(R.string.txt_renunt_all), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stock_List_array.clear();
                dialogInterface.dismiss();
            }
        });
        builderType.setAdapter(simpleAdapterType, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                String WareGUid= String.valueOf(stock_List_array.get(wich).get("Uid"));
                String WareName= String.valueOf(stock_List_array.get(wich).get("Name"));
                String WareCode= String.valueOf(stock_List_array.get(wich).get("Code"));

                SharedPreferences WareHouse = getSharedPreferences("Ware House", MODE_PRIVATE);
                SharedPreferences.Editor addWareHouse = WareHouse.edit();
                addWareHouse.putString("WareName",WareName);
                addWareHouse.putString("WareUid",WareGUid);
                addWareHouse.putString("WareCode",WareCode);
                addWareHouse.apply();

                stock_List_array.clear();
                pgH.setTitle(getResources().getString(R.string.msg_create_new_revision));
                pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                pgH.setIndeterminate(true);
                pgH.setCancelable(false);
                pgH.show();
                URL generateCreateRevision = CreateRevision(ip_,port_,UserId,WareGUid);
                new AsyncTask_CreateRevison().execute(generateCreateRevision);
            }
        });
        builderType.setCancelable(false);
        pgH.dismiss();
        builderType.show();
    }
    public void getWareHouse(){
        URL getWareHouse = GetWareHouseList(ip_,port_,UserId);
        new AsyncTask_WareHouse().execute(getWareHouse);
    }
    class AsyncTask_GetRevision extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="";
            try {
                response =  Response_from_GetRevision(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if (!response.equals("")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            json_array = responseWareHouse.getJSONArray("Rervisions");
                            initList_asl();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        pgH.dismiss();
                        Toast.makeText(Revisions.this,getResources().getString(R.string.msg_error_code)+ ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Revisions.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_WareHouse extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetWareHouse(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(),Revisions.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if (!response.equals("false")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            JSONArray ListWare = responseWareHouse.getJSONArray("Warehouses");
                            for (int i = 0; i < ListWare.length(); i++) {
                                JSONObject object = ListWare.getJSONObject(i);
                                String WareCode = object.getString("Code");
                                String WareName = object.getString("Name");
                                String WareUid = object.getString("WarehouseID");
                                HashMap<String, Object> WareHouse = new HashMap<>();
                                WareHouse.put("Name", WareName);
                                WareHouse.put("Code", WareCode);
                                WareHouse.put("Uid", WareUid);
                                stock_List_array.add(WareHouse);
                            }
                            show_WareHouse();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables) getApplication()).appendLog(e.getMessage(), Revisions.this);
                        }
                    }else{
                        pgH.dismiss();
                        Toast.makeText(Revisions.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables) getApplication()).appendLog(e.getMessage(), Revisions.this);
                }
            }else{
                pgH.dismiss();
                Toast.makeText(Revisions.this,getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }
        }
    }
    class AsyncTask_CreateRevison extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="";
            try {
                response = Response_from_CreateRevision(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            pgH.dismiss();
            if(!response.equals("")) {
                try {
                    JSONObject responseWareHouse = new JSONObject(response);
                    Integer ErrorCode = responseWareHouse.getInt("ErrorCode");
                    if (ErrorCode == 0) {
                        try {
                            JSONObject Revisons = responseWareHouse.getJSONObject("Rervision");
                            String Name = Revisons.getString("Name");
                            String Number = Revisons.getString("RevisionNumber");
                            String RevisionId = Revisons.getString("RevisionID");
                            String prefix = Revisons.getString("WeightPrefix");

                            SharedPreferences Revision = getSharedPreferences("Revision", MODE_PRIVATE);
                            SharedPreferences.Editor inpRev = Revision.edit();
                            inpRev.putString("RevisionName",Name);
                            inpRev.putString("RevisionNumber",Number);
                            inpRev.putString("RevisionID",RevisionId);
                            if(prefix == null || prefix.equals("null")){
                                prefix = "10154";
                            }
                            inpRev.putInt("WeightPrefix",Integer.valueOf(prefix));
                            inpRev.putInt("Type",0);
                            inpRev.apply();

                            Intent stockInv= new Intent(".InventoryMobile");
                            startActivity(stockInv);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
                        }
                    }else{
                        Toast.makeText(Revisions.this,getResources().getString(R.string.msg_error_code) + ErrorCode, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    ((Variables)getApplication()).appendLog(e.getMessage(), Revisions.this);
                }
            }else{
                Toast.makeText(Revisions.this, getResources().getString(R.string.msg_nu_raspuns_server), Toast.LENGTH_SHORT).show();
            }

        }
    }
}
