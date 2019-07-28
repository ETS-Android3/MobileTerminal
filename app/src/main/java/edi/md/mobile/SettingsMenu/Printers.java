package edi.md.mobile.SettingsMenu;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import edi.md.mobile.R;

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetLabel;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetLable;

public class Printers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final static int  MSG_CALIBRATE = 4;
    private final static int  MSG_ErrorState = 5;
    private final static int  isReadyToPrint = 6;
    private final static int  isPaused = 7;
    private final static int  isHeadOpen = 8;
    private final static int  isPaperOut = 9;
    TextView txt_status;

    Button btn_search,btn_show_paired,btn_calibrate,btn_getlable;
    String adressConectoin,ip_,port_;
    Boolean Conected_device = false,get_status=false;
    private BluetoothAdapter mBTAdapter;
    private AdapterBluethoot mBTArrayAdapter;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;// bi-directional client-to-client data path
    private static final List<BluetoothDevice> list_of_device =  new ArrayList<BluetoothDevice>();;

    private final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothDevice.ACTION_FOUND :{
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    list_of_device.add(device);
                    //mBTArrayAdapter.add(device.getName() + " | " + device.getAddress());
                    mBTArrayAdapter.notifyDataSetChanged();

                }break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED : {
                    btn_search.setEnabled(true);
                    btn_calibrate.setEnabled(true);
                }break;
            }

        }
    };
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            btn_calibrate.setEnabled(false);
            btn_search.setEnabled(false);
            btn_show_paired.setEnabled(false);
            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(Printers.this, "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }
            if(mBTAdapter.isDiscovering()){
                mBTAdapter.cancelDiscovery();
                btn_search.setEnabled(true);
            }
            else{
                btn_search.setEnabled(true);
            }

            txt_status.setText(R.string.status_bluetooth_conection);
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v.findViewById(android.R.id.text2)).getText().toString();
            String infoName = ((TextView) v.findViewById(android.R.id.text1)).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = infoName.substring(0,info.length() - 17);
            adressConectoin=address;
            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(Printers.this, "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(Printers.this, "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        SharedPreferences sPref = getSharedPreferences("Conected printers", MODE_PRIVATE);
                        final SharedPreferences.Editor sPrefInput = sPref.edit();
                        sPrefInput.putString("AdressPrinters",address);
                        sPrefInput.apply();

                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.header_printer_activity);
        setContentView(R.layout.activity_printers);

        Toolbar toolbar = findViewById(R.id.toolbar_printers);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_printers);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        txt_status = findViewById(R.id.txt_status_printers);
        btn_search = findViewById(R.id.btn_search_printers);
        btn_show_paired = findViewById(R.id.btn_show_printers);
        btn_calibrate=findViewById(R.id.btn_calibrate_printers);
        ListView mDevicesListView = findViewById(R.id.LW_printers);
        btn_getlable = findViewById(R.id.btn_download_label);
        mBTArrayAdapter = new AdapterBluethoot(Printers.this);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));

        mHandler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    }
                    txt_status.setText(readMessage);
                }
                else if(msg.what == CONNECTING_STATUS) {
                    if (msg.arg1 == 1) {
                        txt_status.setText("Conected to: " +(String) msg.obj);
                        get_StatusPrinters();
                    }
                    else if (msg.arg1 == -1){
                        txt_status.setText(R.string.status_created_socket_bluetooth);
                    }else{
                        txt_status.setText(R.string.status_conection_bluetooth_handler);
                    }

                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                }
                else if(msg.what == MSG_CALIBRATE){
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                }
                else if (msg.what == isReadyToPrint) {
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                    Toast.makeText(Printers.this, "Ready To Print", Toast.LENGTH_SHORT).show();
                }
                else if (msg.what ==isPaused) {
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                    Toast.makeText(Printers.this, "Cannot Print because the printer is paused.", Toast.LENGTH_SHORT).show();
                }
                else if (msg.what ==isHeadOpen) {
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                    Toast.makeText(Printers.this, "Cannot Print because the printer head is open.", Toast.LENGTH_SHORT).show();
                }
                else if (msg.what ==isPaperOut) {
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                    Toast.makeText(Printers.this, "Cannot Print because the paper is out.", Toast.LENGTH_SHORT).show();
                }
                else if (msg.what ==MSG_ErrorState){
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                    Toast.makeText(Printers.this, "Cannot Print.", Toast.LENGTH_SHORT).show();
                }
                else{
                    txt_status.setText(R.string.connection_failed_bluethoot);
                    btn_calibrate.setEnabled(true);
                    btn_search.setEnabled(true);
                    btn_show_paired.setEnabled(true);
                }
            }
        };

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            txt_status.setText(R.string.bluetooth_not_found);
            Toast.makeText(Printers.this,"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else{
            btn_calibrate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(Conected_device) {
                        SharedPreferences sPref = getSharedPreferences("Conected printers", MODE_PRIVATE);
                        final String adresMAC = sPref.getString("AdressPrinters", null);
                        if (adresMAC != null) {
                            btn_calibrate.setEnabled(false);
                            btn_show_paired.setEnabled(false);
                            btn_search.setEnabled(false);
                            new Thread() {
                                public void run() {
                                    Connection connection = new BluetoothConnection(adresMAC);
                                    try {
                                        connection.open();
                                        ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
                                        printer.calibrate();
                                    } catch (ConnectionException e) {
                                        e.printStackTrace();
                                    } catch (ZebraPrinterLanguageUnknownException e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            connection.close();
                                        } catch (ConnectionException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    mHandler.obtainMessage(MSG_CALIBRATE, 1, -1)
                                            .sendToTarget();
                                }
                            }.start();
                        }
                    }else{

                        Toast.makeText(Printers.this, "Nu v-ati conectat la nici o imprimanta!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mBTAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    else{
                        discover();
                    }

                }
            });

            btn_show_paired.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txt_status.setText("");
                    listPairedDevices();
                }
            });


        }//else adapter not null

        SharedPreferences sPref = getSharedPreferences("Settings", MODE_PRIVATE);
        ip_=sPref.getString("IP","");
        port_=sPref.getString("Port","");

        btn_getlable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL getWareHouse = GetLabel(ip_,port_,"1");
                new AsyncTask_getLable().execute(getWareHouse);
            }
        });

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_printers);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_printers, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_close_printers) {
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout_printers);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        Set<BluetoothDevice> mPairedDevices = mBTAdapter.getBondedDevices();
        btn_calibrate.setEnabled(true);

        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            btn_search.setEnabled(true);
        }
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices){
                list_of_device.add(device);
                //mBTArrayAdapter.add(device.getName() + " | " + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
               // mBTArrayAdapter.add(device.getName() + " | " + device.getAddress());

            Toast.makeText(Printers.this, "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(Printers.this, "Bluetooth not on", Toast.LENGTH_SHORT).show();
    }
    private void discover(){
        btn_search.setEnabled(false);
        btn_calibrate.setEnabled(false);


        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(Printers.this,"Discovery stopped",Toast.LENGTH_SHORT).show();
            btn_calibrate.setEnabled(true);
            btn_search.setEnabled(true);
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(Printers.this, "Discovery started", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(Printers.this, "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent Data){
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                txt_status.setText(R.string.bluetooth_satus_enabled);
                discover();
            }
            else
                txt_status.setText(R.string.status_bluetooth_disabled);
        }
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        //private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            //OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                //tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            //mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer ;
            // buffer store for the stream    = new byte[1024]
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        buffer = new byte[1024];
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
//        public void write(String input) {
//            byte[] bytes = input.getBytes();           //converts entered String into bytes
//            try {
//                mmOutStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public OutputStream getMmOutStream() {
//            return mmOutStream;
//        }
    }
    @SuppressLint("StaticFieldLeak")
    class AsyncTask_getLable extends AsyncTask<URL, String, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response="false";
            try {
                response = Response_from_GetLable(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject responseWareHouse = new JSONObject(response);
                String lable =responseWareHouse.getString("Lable");
                String erroremsg = responseWareHouse.getString("ErrorMessage");
                if (!lable.equals("")){
                    SharedPreferences sPref = getSharedPreferences("Save setting", MODE_PRIVATE);
                    final SharedPreferences.Editor sPrefInput = sPref.edit();
                    sPrefInput.putString("Lable",lable);
                    sPrefInput.apply();
                    Toast.makeText(Printers.this, "Lable saved!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(Printers.this, erroremsg, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void get_StatusPrinters(){
        SharedPreferences sPref =getSharedPreferences("Conected printers", MODE_PRIVATE);
        final String adresMAC = sPref.getString("AdressPrinters", null);
        if (adresMAC != null) {
            btn_calibrate.setEnabled(false);
            btn_search.setEnabled(false);
            btn_show_paired.setEnabled(false);
            new Thread() {
                public void run() {
                    Connection connection = new BluetoothConnection(adresMAC);
                    try {
                        connection.open();
                        ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

                        PrinterStatus printerStatus = printer.getCurrentStatus();
                        if (printerStatus.isReadyToPrint) {
                            mHandler.obtainMessage(isReadyToPrint, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isPaused) {
                            mHandler.obtainMessage(isPaused, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isHeadOpen) {
                            mHandler.obtainMessage(isHeadOpen, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isPaperOut) {
                            mHandler.obtainMessage(isPaperOut, 1, -1)
                                    .sendToTarget();
                        } else {
                            mHandler.obtainMessage(MSG_ErrorState, 1, -1)
                                    .sendToTarget();
                        }
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                    } catch (ZebraPrinterLanguageUnknownException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    @Override
    protected void onDestroy() {
        if(adressConectoin!=null){
            unregisterReceiver(blReceiver);
        }

        super.onDestroy();
    }

    private class AdapterBluethoot extends ArrayAdapter<BluetoothDevice> {

        public AdapterBluethoot(Context context) {
            super(context, android.R.layout.simple_list_item_2, list_of_device);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice device = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_2, null);
            }
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(device.getName());
            ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(device.getAddress());
            return convertView;
        }
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
