package md.intelectsoft.stockmanager.SettingsMenu;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;

import md.intelectsoft.stockmanager.R;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;

import static android.content.Context.MODE_PRIVATE;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.GetLabel;
import static md.intelectsoft.stockmanager.NetworkUtils.NetworkUtils.Response_from_GetLable;

public class FragmentZebraPrinter extends Fragment {
   Button btn_download_lable,btn_calibrate_printer;
   String url,mNamePrinter,mMACPrinter;
   TextView txt_status, txtNamePrinter;

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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View zebraView = inflater.inflate(R.layout.fragment_zebra_printer, null);
        btn_calibrate_printer = zebraView.findViewById(R.id.btn_calibrate_printers);
        btn_download_lable = zebraView.findViewById(R.id.btn_download_label);
        txt_status = zebraView.findViewById(R.id.txt_status_printers);
        txtNamePrinter = zebraView.findViewById(R.id.txtNameZebraPrinters);
        SPFHelp sharedPrefsInstance = SPFHelp.getInstance();

        SharedPreferences SPrefPrinters = getActivity().getSharedPreferences("Printers", MODE_PRIVATE);
        url = sharedPrefsInstance.getString("URI","");
        mNamePrinter =SPrefPrinters.getString("Name_ZebraPrinters","Non");
        mMACPrinter = SPrefPrinters.getString("MAC_ZebraPrinters", null);

        txtNamePrinter.setText("Information and option about " + mNamePrinter);

        get_StatusPrinters();

        btn_download_lable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL getWareHouse = GetLabel(url,"1");
                new AsyncTask_getLable().execute(getWareHouse);
            }
        });

        btn_calibrate_printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMACPrinter != null) {
                    new Thread() {
                        public void run() {
                            Connection connection = new BluetoothConnection(mMACPrinter);
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
                            mHandlerZebra.obtainMessage(MSG_CALIBRATE_Zebra, 1, -1)
                                    .sendToTarget();
                        }
                    }.start();
                }
            }
        });

        return zebraView;
    }
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
                String lable = responseWareHouse.getString("Lable");
                String erroremsg = responseWareHouse.getString("ErrorMessage");
                if (!lable.equals("")){
                    SharedPreferences sPref = getActivity().getSharedPreferences("Printers", MODE_PRIVATE);
                    sPref.edit().putString("Lable",lable).apply();
                    Toast.makeText(getActivity(), "Lable saved!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), erroremsg, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                txt_status.setText(readMessage);
            }
            else if(msg.what == MSG_CALIBRATE_Zebra){
            }
            else if (msg.what == isReadyToPrint_Zebra) {
                txt_status.setText(mNamePrinter + " is ready to print");
            }
            else if (msg.what ==isPaused_Zebra) {
                txt_status.setText(mNamePrinter + " Cannot Print because the printer is paused.");
            }
            else if (msg.what ==isHeadOpen_Zebra) {
                txt_status.setText(mNamePrinter +  " Cannot Print because the printer head is open.");
            }
            else if (msg.what ==isPaperOut_Zebra) {
                txt_status.setText(mNamePrinter + " Cannot Print because the paper is out.");
            }
            else if (msg.what ==MSG_ErrorState_Zebra){
                txt_status.setText(mNamePrinter + " Cannot Print.");
            }
            else{
                txt_status.setText(R.string.connection_failed_bluethoot);
            }
        }
    };

        private void get_StatusPrinters(){
        SharedPreferences sPref =getActivity().getSharedPreferences("Printers", MODE_PRIVATE);
        final String adresMAC = sPref.getString("AdressPrinters", null);
        if (adresMAC != null) {
            new Thread() {
                public void run() {
                    Connection connection = new BluetoothConnection(adresMAC);
                    try {
                        connection.open();
                        ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

                        PrinterStatus printerStatus = printer.getCurrentStatus();
                        if (printerStatus.isReadyToPrint) {
                            mHandlerZebra.obtainMessage(isReadyToPrint_Zebra, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isPaused) {
                            mHandlerZebra.obtainMessage(isPaused_Zebra, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isHeadOpen) {
                            mHandlerZebra.obtainMessage(isHeadOpen_Zebra, 1, -1)
                                    .sendToTarget();
                        } else if (printerStatus.isPaperOut) {
                            mHandlerZebra.obtainMessage(isPaperOut_Zebra, 1, -1)
                                    .sendToTarget();
                        } else {
                            mHandlerZebra.obtainMessage(MSG_ErrorState_Zebra, 1, -1)
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
}
