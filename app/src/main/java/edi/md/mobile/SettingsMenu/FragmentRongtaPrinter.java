package edi.md.mobile.SettingsMenu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;

import edi.md.mobile.R;

public class FragmentRongtaPrinter extends Fragment {

    Button mSelfTest,mPrintImage;
    TextView txt_header;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rongtaView = inflater.inflate(R.layout.fragment_rongta_printer, null);
        Bundle bundl = getArguments();
        String mNameDevice = bundl.getString("BTName");

        mSelfTest = rongtaView.findViewById(R.id.btn_test_print);
        txt_header = rongtaView.findViewById(R.id.txt_header_fragment_rongta);
        mPrintImage = rongtaView.findViewById(R.id.btn_print_img);
        txt_header.setText("Information and option about " + mNameDevice);

        mSelfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BluetoothPrintDriver.IsNoConnection()){
                    return;
                }
                BluetoothPrintDriver.Begin();
                BluetoothPrintDriver.SelftestPrint();
            }
        });
        return rongtaView;
    }

}
