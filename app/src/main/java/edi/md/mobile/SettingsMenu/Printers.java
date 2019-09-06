package edi.md.mobile.SettingsMenu;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
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
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import edi.md.mobile.R;
import edi.md.mobile.Utils.DeviceListActivity;
import edi.md.mobile.Variables;

import static edi.md.mobile.NetworkUtils.NetworkUtils.GetLabel;
import static edi.md.mobile.NetworkUtils.NetworkUtils.Response_from_GetLable;
import static edi.md.mobile.Variables.mLablePrinters;
import static edi.md.mobile.Variables.mPOSPrinters;
import static edi.md.mobile.Variables.mRongtaModelList;
import static edi.md.mobile.Variables.mZebraModelList;

public class Printers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // Debugging
    private static final String TAG = "BloothPrinterActivity";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE_Rongta = 1;
    public static final int MESSAGE_READ_Rongta = 2;
    public static final int MESSAGE_WRITE_Rongta = 3;
    public static final int MESSAGE_DEVICE_NAME_Rongta = 4;
    public static final int MESSAGE_TOAST_Rongta = 5;
    private final static int MESSAGE_READ_Zebra = 2;
    private final static int CONNECTING_STATUS_Zebra = 3;
    private final static int  MSG_CALIBRATE_Zebra = 4;
    private final static int  MSG_ErrorState_Zebra = 5;
    private final static int  isReadyToPrint_Zebra = 6;
    private final static int  isPaused_Zebra = 7;
    private final static int  isHeadOpen_Zebra = 8;
    private final static int  isPaperOut_Zebra = 9;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static int revBytes=0;
    public  static boolean isHex=false;

    public static final int REFRESH = 8;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothPrintDriver mChatService = null;

    int mSelectedPrinter,modelType;
    Spinner mTypePrinter , mModelPrinter;
    FragmentTransaction fTrans;
    Fragment mRongtaPrinterFragment,mZebraPrinterFragment;
    TextView txt_status;

    String[] mTypePrinterList = {"NoN", mPOSPrinters, mLablePrinters};

    Button btn_connect_device;
    String adressConectoin,ip_,port_;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    private static final List<BluetoothDevice> list_of_device =  new ArrayList<BluetoothDevice>();
    ArrayAdapter<String> adapterModel;

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
        btn_connect_device = findViewById(R.id.btn_connect_device);
        mTypePrinter = findViewById(R.id.spinner_type_printer);
        mModelPrinter = findViewById(R.id.spinner_model_printer);

        mZebraPrinterFragment = new FragmentZebraPrinter();
        mRongtaPrinterFragment = new FragmentRongtaPrinter();

        final SharedPreferences SharedPrinters = getSharedPreferences("Printers", MODE_PRIVATE);
        final SharedPreferences.Editor SharedPrintersEditor = SharedPrinters.edit();

        // адаптер тип принтера
        ArrayAdapter<String> adapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTypePrinterList);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTypePrinter.setAdapter(adapterType);
        int positionType = SharedPrinters.getInt("Type",0);
        modelType =  SharedPrinters.getInt("Model",0);
        mSelectedPrinter = positionType;
        mTypePrinter.setSelection(positionType);

        if(positionType == 1){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mRongtaModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(modelType);

        }
        else if(positionType == 2){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mZebraModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(modelType);
        }
        else if(positionType == 0){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mZebraModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(0);
        }

        // обработчик нажатия
        mTypePrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 1){
                    // адаптер модель принтера
                    adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mRongtaModelList);
                    adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    mModelPrinter.setAdapter(adapterModel);
                    if(mSelectedPrinter != position)
                        mModelPrinter.setSelection(0);
                    else
                        mModelPrinter.setSelection(modelType);
                    mModelPrinter.setEnabled(true);

                    SharedPrintersEditor.putInt("Type",position);
                    SharedPrintersEditor.apply();
                }
                else if(position == 2){
                    // адаптер модель принтера
                    adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mZebraModelList);
                    adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    mModelPrinter.setAdapter(adapterModel);
                    if(mSelectedPrinter != position)
                        mModelPrinter.setSelection(0);
                    else
                        mModelPrinter.setSelection(modelType);
                    mModelPrinter.setEnabled(true);

                    SharedPrintersEditor.putInt("Type",position);
                    SharedPrintersEditor.apply();
                }
                if( position == 0){
                    SharedPrintersEditor.putInt("Type",position);
                    SharedPrintersEditor.apply();

                    mModelPrinter.setSelection(0);
                    mModelPrinter.setEnabled(false);
                }
                mModelPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SharedPrintersEditor.putInt("Model",position);
                        SharedPrintersEditor.apply();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                mSelectedPrinter = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final SharedPreferences User = getSharedPreferences("User", MODE_PRIVATE);
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(User.getString("Name",""));


        SharedPreferences sPref = getSharedPreferences("Settings", MODE_PRIVATE);
        ip_=sPref.getString("IP","");
        port_=sPref.getString("Port","");

        btn_connect_device.setOnClickListener(new View.OnClickListener() {
            Intent serverIntent = null;
            @Override
            public void onClick(View v) {
                if(mSelectedPrinter != 0){
                    // Launch the DeviceListActivity to see devices and do scan
                    serverIntent = new Intent(Printers.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                }

            }
        });
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

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
        getMenuInflater().inflate(R.menu.menu_printers, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
            finishAffinity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_printers);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        final SharedPreferences SharedPrinters = getSharedPreferences("Printers", MODE_PRIVATE);
        int mType = SharedPrinters.getInt("Type",0);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    final String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    if( mType == 1){

                        // Attempt to connect to the device
                        mChatService.connect(device);
                    }
                    else if ( mType == 2){
                        adressConectoin=address;
                        // Spawn a new thread to avoid blocking the GUI one
                        new Thread()
                        {
                            public void run() {
                                boolean fail = false;

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
                                        mHandlerZebra.obtainMessage(CONNECTING_STATUS_Zebra, -1, -1)
                                                .sendToTarget();
                                    } catch (IOException e2) {
                                        //insert code to deal with this
                                        Toast.makeText(Printers.this, "Socket creation failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if(!fail) {
                                    SharedPreferences sPref = getSharedPreferences("Printers", MODE_PRIVATE);
                                    final SharedPreferences.Editor sPrefInput = sPref.edit();
                                    sPrefInput.putString("MAC_ZebraPrinters",address);
                                    sPrefInput.putString("Name_ZebraPrinters",device.getName());
                                    sPrefInput.apply();

                                    mConnectedThread = new ConnectedThread(mBTSocket);
                                    mConnectedThread.start();

                                    mHandlerZebra.obtainMessage(CONNECTING_STATUS_Zebra, 1, -1, device.getName())
                                            .sendToTarget();
                                }
                            }
                        }.start();
                    }

                }
                break;
            case REQUEST_ENABLE_BT:{
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    if(mType == 1){
                        setupChat();
                    }
                } else {
                    // User did not enable Bluetooth or an error occured
                    txt_status.setText(R.string.status_bluetooth_disabled);
                }
            }

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

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;

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
                        mHandlerZebra.obtainMessage(MESSAGE_READ_Zebra, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
              //  Stop the Bluetooth chat services eONGTA
//        if (mChatService != null) mChatService.stop();
        super.onDestroy();
    }

    /**Rongta settings
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }
    @Override
    public synchronized void onResume() {
        super.onResume();

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothPrintDriver.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothPrintDriver(this, mHandlerRongta);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandlerRongta = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE_Rongta:
                    switch (msg.arg1) {
                        case BluetoothPrintDriver.STATE_CONNECTED:
                            txt_status.setText("Connected to: ");
                            txt_status.append(mConnectedDeviceName);

                            initFragmentUI(mSelectedPrinter,mConnectedDeviceName);

                            break;
                        case BluetoothPrintDriver.STATE_CONNECTING:
                            txt_status.setText("Connecting...");
                            //setTitle(R.string.title_connecting);
                            break;
                        case BluetoothPrintDriver.STATE_LISTEN:
                        case BluetoothPrintDriver.STATE_NONE:
                            txt_status.setText("Not Connected");
                            //setTitle(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE_Rongta:
                    break;
                case MESSAGE_READ_Rongta:
                    String ErrorMsg = null;
                    byte[] readBuf = (byte[]) msg.obj;
                    float Voltage = 0;
                    if(D) Log.i(TAG, "readBuf[0]:"+readBuf[0]+"  readBuf[1]:"+readBuf[1]+"  readBuf[2]:"+readBuf[2]);
                    if(readBuf[2]==0)
                        ErrorMsg = "NO ERROR!";
                    else
                    {
                        if((readBuf[2] & 0x02) != 0)
                            ErrorMsg = "ERROR: No printer connected!";
                        if((readBuf[2] & 0x04) != 0)
                            ErrorMsg = "ERROR: No paper!";
                        if((readBuf[2] & 0x08) != 0)
                            ErrorMsg = "ERROR: Voltage is too low!  ";
                        if((readBuf[2] & 0x40) != 0)
                            ErrorMsg = "ERROR: Printer Over Heat!  ";
                    }
                    Voltage = (float) ((readBuf[0]*256 + readBuf[1])/10.0);
                    //if(D) Log.i(TAG, "Voltage: "+Voltage);
                    DisplayToast(ErrorMsg+"\n"+"Battery voltage: "+Voltage+" V");

                    break;
                case MESSAGE_DEVICE_NAME_Rongta:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to: "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST_Rongta:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final Handler mHandlerZebra = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == MESSAGE_READ_Zebra){
                String readMessage = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                }
                txt_status.setText(readMessage);
            }
            else if(msg.what == CONNECTING_STATUS_Zebra) {
                if (msg.arg1 == 1) {
                    txt_status.setText("Conected to: " +(String) msg.obj);
                    initFragmentUI(mSelectedPrinter,(String) msg.obj);
                }
                else if (msg.arg1 == -1){
                    txt_status.setText(R.string.status_created_socket_bluetooth);
                }else{
                    txt_status.setText(R.string.status_conection_bluetooth_handler);
                }
            }
            else if(msg.what == MSG_CALIBRATE_Zebra){
            }
            else if (msg.what == isReadyToPrint_Zebra) {
                Toast.makeText(Printers.this, "Ready To Print", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what ==isPaused_Zebra) {
                Toast.makeText(Printers.this, "Cannot Print because the printer is paused.", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what ==isHeadOpen_Zebra) {
                Toast.makeText(Printers.this, "Cannot Print because the printer head is open.", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what ==isPaperOut_Zebra) {
                Toast.makeText(Printers.this, "Cannot Print because the paper is out.", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what ==MSG_ErrorState_Zebra){
                Toast.makeText(Printers.this, "Cannot Print.", Toast.LENGTH_SHORT).show();
            }
            else{
                txt_status.setText(R.string.connection_failed_bluethoot);
            }
        }
        };

    public void DisplayToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 100);
        toast.show();
    }

    public void initFragmentUI(int position,String BTName){
        if(position == 1){
            Bundle bundl = new Bundle();
            bundl.putString("BTName",BTName);

            if(!mRongtaPrinterFragment.isAdded())
                mRongtaPrinterFragment.setArguments(bundl);

            fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.container_fragment, mRongtaPrinterFragment);
            fTrans.commit();
        }
        else if(position == 2){
            fTrans = getFragmentManager().beginTransaction();
            fTrans.replace(R.id.container_fragment, mZebraPrinterFragment);
            fTrans.commit();
        }
    }
}
