package md.intelectsoft.stockmanager.SettingsMenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import md.intelectsoft.stockmanager.R;
import md.intelectsoft.stockmanager.BaseApp;
import md.intelectsoft.stockmanager.app.utils.BaseEnum;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;

import static md.intelectsoft.stockmanager.BaseApp.mLablePrinters;
import static md.intelectsoft.stockmanager.BaseApp.mPOSPrinters;
import static md.intelectsoft.stockmanager.BaseApp.mRongtaModelList;
import static md.intelectsoft.stockmanager.BaseApp.mZebraModelList;

public class Printers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PrinterObserver {

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    private final static int MESSAGE_READ_Zebra = 2;
    private final static int CONNECTING_STATUS_Zebra = 3;
    private final static int  MSG_CALIBRATE_Zebra = 4;
    private final static int  MSG_ErrorState_Zebra = 5;
    private final static int  isReadyToPrint_Zebra = 6;
    private final static int  isPaused_Zebra = 7;
    private final static int  isHeadOpen_Zebra = 8;
    private final static int  isPaperOut_Zebra = 9;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private BluetoothAdapter mBluetoothAdapter = null;
    int mSelectedPrinter,modelType;
    Spinner mTypePrinter , mModelPrinter;
    FragmentTransaction fTrans;
    Fragment mRongtaPrinterFragment,mZebraPrinterFragment;
    TextView tv_device_selected;

    String[] mTypePrinterList = {"NoN", mPOSPrinters, mLablePrinters};

    Button btn_select_device, btn_connected_toDevice;
    String adressConectoin,url_;
    private ConnectedThread mConnectedThread;
    private BluetoothSocket mBTSocket = null;
    ArrayAdapter<String> adapterModel ;

    private RTPrinter rtPrinter = null;
    private PrinterFactory printerFactory;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private PrinterInterface curPrinterInterface = null;
    private BluetoothDevice bluetoothDevice;

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

        btn_select_device = findViewById(R.id.btn_connect_device);
        mTypePrinter = findViewById(R.id.spinner_type_printer);
        mModelPrinter = findViewById(R.id.spinner_model_printer);
        tv_device_selected = findViewById(R.id.txt_status_printers);
        btn_connected_toDevice = findViewById(R.id.btn_device_connect_on_bluetooth);

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

        if(SharedPrinters.getInt("WithDisplay",0) != 0)
            BaseApp.getInstance().setWidthPrinterPrint(SharedPrinters.getInt("WithDisplay",0));

        //если пос принтера
        if(positionType == BaseEnum.POS_PRINTER){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mRongtaModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(modelType);

        }
        //если принтер этикеток
        else if(positionType == BaseEnum.LABLE_PRINTER){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mZebraModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(modelType);
        }
        //если ничего не выбрано
        else if(positionType == BaseEnum.NO_PRINTER){
            adapterModel = new ArrayAdapter<String>(Printers.this, android.R.layout.simple_spinner_item, mZebraModelList);
            adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mModelPrinter.setAdapter(adapterModel);
            mModelPrinter.setSelection(0);
        }

        // обработчик нажатия
        mTypePrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == BaseEnum.POS_PRINTER){
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
                else if(position == BaseEnum.LABLE_PRINTER){
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
                if( position == BaseEnum.NO_PRINTER){
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

                        if(position == BaseEnum.RP_200){
                            SharedPrintersEditor.putInt("WithDisplay",48);
                            SharedPrintersEditor.apply();
                            BaseApp.getInstance().setWidthPrinterPrint(48);
                        }
                        else  if (position == BaseEnum.RP_300){
                            SharedPrintersEditor.putInt("WithDisplay",72);
                            SharedPrintersEditor.apply();
                            BaseApp.getInstance().setWidthPrinterPrint(72);
                        }
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
SPFHelp sharedPrefsInstance = SPFHelp.getInstance();
        View headerLayout = navigationView.getHeaderView(0);
        TextView useremail = (TextView) headerLayout.findViewById(R.id.txt_name_of_user);
        useremail.setText(sharedPrefsInstance.getString("UserName",""));


        url_ = SPFHelp.getInstance().getString("URI","");

        PrinterObserverManager.getInstance().add(this);

        BaseApp.instance.setCurrentCmdType(BaseEnum.CMD_ESC);
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        rtPrinter.setPrinterInterface(curPrinterInterface);

        btn_select_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent serverIntent = null;
                if(mSelectedPrinter != 0){
                    serverIntent = new Intent(Printers.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);
                }

//                BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
//                bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
//                    @Override
//                    public void onDeviceItemClick(BluetoothDevice device) {
//                        bluetoothDevice = device;
//                        if (TextUtils.isEmpty(device.getName())) {
//                            tv_device_selected.setText(device.getAddress());
//                        } else {
//                            tv_device_selected.setText(device.getName() + " [" + device.getAddress() + "]");
//                        }
//                        configObj = new BluetoothEdrConfigBean(device);
//                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
//                    }
//                });
//                bluetoothDeviceChooseDialog.show(Printers.this.getFragmentManager(), null);
            }
        });

        btn_connected_toDevice.setOnClickListener(v-> {
            int mType = getSharedPreferences("Printers", MODE_PRIVATE).getInt("Type",0);

            if (mType == 1){
                if (tv_device_selected.getTag() == null || Integer.parseInt(tv_device_selected.getTag().toString() ) == BaseEnum.NO_DEVICE) {//未选择设备
                    showAlertDialog("Please choose a device");
                    return;
                }

                tv_device_selected.setText("Connecting...");

                BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
                connectBluetooth(bluetoothEdrConfigBean);
            }
            else if ( mType == 2){
                adressConectoin = bluetoothDevice.getAddress();
                // Spawn a new thread to avoid blocking the GUI one
                new Thread()
                {
                    public void run() {
                        boolean fail = false;

                        try {
                            mBTSocket = createBluetoothSocket(bluetoothDevice);
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
                            sPrefInput.putString("MAC_ZebraPrinters",adressConectoin);
                            sPrefInput.putString("Name_ZebraPrinters",bluetoothDevice.getName());
                            sPrefInput.apply();

                            mConnectedThread = new ConnectedThread(mBTSocket);
                            mConnectedThread.start();

                            mHandlerZebra.obtainMessage(CONNECTING_STATUS_Zebra, 1, -1, bluetoothDevice.getName()).sendToTarget();
                        }
                    }
                }.start();
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

    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
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

    public void showAlertDialog(final String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Printers.this);
                dialog.setTitle("Tip");
                dialog.setMessage(msg);
                dialog.setNegativeButton("Back", null);
                dialog.show();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.menu_conect) {
//            Intent MenuConnect = new Intent(".MenuConnect");
//            startActivity(MenuConnect);
//            finish();
//        } else
            if (id == R.id.menu_workplace) {
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
        if(requestCode ==  REQUEST_ENABLE_BT){
            if (resultCode != Activity.RESULT_OK) {
                // User did not enable Bluetooth or an error occured
                tv_device_selected.setText(R.string.status_bluetooth_disabled);
            }
        }
        else if (requestCode == REQUEST_CONNECT_DEVICE){
            if(resultCode == RESULT_OK){
                bluetoothDevice = data.getExtras().getParcelable("btdevice");;
                if (TextUtils.isEmpty(bluetoothDevice.getName())) {
                    tv_device_selected.setText(bluetoothDevice.getAddress());
                } else {
                    tv_device_selected.setText(bluetoothDevice.getName() + " [" + bluetoothDevice.getAddress() + "]");
                }
                configObj = new BluetoothEdrConfigBean(bluetoothDevice);
                tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
            }
        }
    }

    //pentru zebra sau label printer
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void printerObserverCallback(PrinterInterface printerInterface, int i) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (i) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        showToast(printerInterface.getConfigObject().toString() + "connect succes");
                        tv_device_selected.setText("Connected to :" + printerInterface.getConfigObject().toString());
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);

                        curPrinterInterface = printerInterface;
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        BaseApp.getInstance().setRtPrinter(rtPrinter);

                        initFragmentUI(mSelectedPrinter,printerInterface.getConfigObject().toString());

                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
                            showToast(printerInterface.getConfigObject().toString() + "disconnect");
                        } else {
                            showToast("disconnect");
                        }
                        tv_device_selected.setText("Click to connect a device");
                        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);//多连接-从已连接列表中移除
                        BaseApp.getInstance().setRtPrinter(null);

                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }

    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private final Handler mHandlerZebra = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == MESSAGE_READ_Zebra){
                String readMessage = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                }
                tv_device_selected.setText(readMessage);
            }
            else if(msg.what == CONNECTING_STATUS_Zebra) {
                if (msg.arg1 == 1) {
                    tv_device_selected.setText("Conected to: " +(String) msg.obj);
                    initFragmentUI(mSelectedPrinter,(String) msg.obj);
                }
                else if (msg.arg1 == -1){
                    tv_device_selected.setText(R.string.status_created_socket_bluetooth);
                }else{
                    tv_device_selected.setText(R.string.status_conection_bluetooth_handler);
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
                tv_device_selected.setText(R.string.connection_failed_bluethoot);
            }
        }
        };

    public void initFragmentUI(int position,String BTName){
        if(Printers.this.isDestroyed()){
            return;
        }
        if(position == 1){
            Bundle bundle = new Bundle();
            bundle.putString("BTName",BTName);

            if(!mRongtaPrinterFragment.isAdded())
                mRongtaPrinterFragment.setArguments(bundle);

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
