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

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import edi.md.mobile.R;

public class FragmentRongtaPrinter extends Fragment {

    Button mSelfTest,mPrintImage;
    TextView txt_header;
    ImageView setImageTest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rongtaView = inflater.inflate(R.layout.fragment_rongta_printer, null);
        Bundle bundl = getArguments();
        String mNameDevice = bundl.getString("BTName");

        mSelfTest = rongtaView.findViewById(R.id.btn_test_print);
        txt_header = rongtaView.findViewById(R.id.txt_header_fragment_rongta);
        mPrintImage = rongtaView.findViewById(R.id.btn_print_img);
        setImageTest = rongtaView.findViewById(R.id.imageView5);

        mPrintImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
//                if(BluetoothPrintDriver.IsNoConnection()){
//                    return;
//                }
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.connect_black_36dp);
//
//                File file = new File(Environment.getExternalStorageDirectory()+ "/Notifications","/lable.txt"); // mention apk file path here
//                StringBuilder text = new StringBuilder();
//
//                try {
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//                    String line;
//
//                    while ((line = br.readLine()) != null) {
//                        text.append(line);
//                    }
//                    br.close();
//                }
//                catch (IOException e) {
//                    Toast.makeText(getContext(), "Exception read file", Toast.LENGTH_SHORT).show();
//                }
//                byte[] encodeValue =text.toString().getBytes();
//                Bitmap bmp = BitmapFactory.decodeByteArray(encodeValue, 0, encodeValue.length);
//
//                //Bitmap bitmap2 = BitmapFactory.decodeByteArray(bufTemp2, 0, bufTemp2.length);
//                setImageTest.setImageBitmap(bmp);
//                BluetoothPrintDriver.printByteData(encodeValue);
                BluetoothPrintDriver.Begin();
                BluetoothPrintDriver.StatusInquiry();
            }
        });

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
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }


}
