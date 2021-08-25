package md.intelectsoft.stockmanager.SettingsMenu;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.utils.BitmapConvertUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import md.intelectsoft.stockmanager.R;
import md.intelectsoft.stockmanager.BaseApp;
import md.intelectsoft.stockmanager.app.utils.BaseEnum;
import md.intelectsoft.stockmanager.app.utils.ToastUtil;

public class FragmentRongtaPrinter extends Fragment {

    Button mSelfTest,mPrintImage;
    TextView txt_header;

    Bitmap mBitmap = null;
    private RTPrinter rtPrinter = null;
    private int bmpPrintWidth;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rongtaView = inflater.inflate(R.layout.fragment_rongta_printer, null);
        Bundle bundle = getArguments();
        String mNameDevice = bundle.getString("BTName");

        mSelfTest = rongtaView.findViewById(R.id.btn_test_print);
        txt_header = rongtaView.findViewById(R.id.txt_header_fragment_rongta);
        mPrintImage = rongtaView.findViewById(R.id.btn_print_img);
        txt_header.setText("Information and option about " + mNameDevice);

        rtPrinter = BaseApp.getInstance().getRtPrinter();
        bmpPrintWidth = BaseApp.getInstance().getWidthPrinterPrint();

        mSelfTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escSelftestPrint();
            }
        });

        mPrintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getActivity().getAssets();
                InputStream istr = null;
                try {
                    istr = assetManager.open("report.png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Bitmap bitmap = BitmapFactory.decodeStream(istr);
                Bitmap bitmap = null;

                String encodedImage = "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AZw7OlXdw+ny2nelqnkuddJ3taNal0s93XdcXvuuVDXPpVzfcXntu1LVPJdyfQDnjk5V93C6vPZdqWqeS530Xe2o1uVST/cdl9e+K1XNcynXd1xe+65UNc+lXB/AuaNT1T2cLq99V6qa51InfVc7qnW51NN9x+W170pV81zK9R2X174rVc1zKdcHcO7oVHUPp8tr35Wq5rnUSd/VjmpdLvV033F57btS1TyXcn3H5bXvSlXzXMr1AZw7OlXdw+ny2nelqnkuddJ3taNal0s93XdcXvuuVDXPpVzfcXntu1LVPJdyfQDnjk5V93C6vPZdqWqeS530Xe2o1uVST/cdl9e+K1XNcynXd1xe+65UNc+lXB/AuaNT1T2cLq99V6qa51InfVc7qnW51NN9x+W170pV81zK9R2X174rVc1zKdcHcO7oVHUPp8tr35Wq5rnUSd/VjmpdLvV033F57btS1TyXcn3H5bXvSlXzXMr1AZw7OlXdw+ny2nelqnkuddJ3taNal0s93XdcXvuuVDXPpVzfcXntu1LVPJdyfQDnjk5V93C6vPZdqWqeS530Xe2o1uVST/cdl9e+K1XNcynXd1xe+65UNc+lXB/AuaNT1T2cLq99V6qa51InfVc7qnW51NN9x+W170pV81zK9R2X174rVc1zKdcHcO7oVHUPp8tr35Wq5rnUSd/VjmpdLvV033F57btS1TyXcn3H5bXvSlXzXMr1AZw7OlXdw+ny2nelqnkuddJ3taNal0s93XdcXvuuVDXPpVzfcXntu1LVPJdyfQDnjk5V93C6vPZdqWqeS530Xe2o1uVST/cdl9e+K1XNcynXd1xe+65UNc+lXB/AuaNT1T2cLq99V6qa51InfVc7qnW51NN9x+W170pV81zK9R2X174rVc1zKdcHcO7oVHUPp8tr35Wq5rnUSd/VjmpdLvV033F57btS1TyXcn3H5bXvSlXzXMr1AZw7OlXdw+ny2nelqnkuddJ3taNal0s93XdcXvuuVDXPpVzfcXntu1LVPJdyfQDnjk5V93C6vPZdqWqeS530Xe2o1uVST/cdl9e+K1XNcynXd1xe+65UNc+lXB/AuaNT1T2cLq99V6qa51InfVc7qnW51NN9x+W170pV81zK9R2X174rVc1zKdcHcO7oVHUPp8tr35Wq5rnUSd/VjmpdLvV033F57btS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";

                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT );

                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                mBitmap = bitmap;

                if (BaseApp.getInstance().getCurrentCmdType() == BaseEnum.CMD_ESC) {
                    if (mBitmap.getWidth() > 48 * 8) {
                        mBitmap = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bitmap,48 * 8, 4000);
                    }
                } else if (BaseApp.getInstance().getCurrentCmdType() == BaseEnum.CMD_PIN) {
                    if (mBitmap.getWidth() > 210 * 8) {
                        mBitmap = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bitmap,210 * 8, 4000);
                    }
                } else {
                    if (mBitmap.getWidth() > 72 * 8) {
                        mBitmap = BitmapConvertUtil.decodeSampledBitmapFromBitmap(bitmap, 72 * 8, 4000);
                    }
                }

                try {
                    print();
                } catch (SdkException e) {
                    e.printStackTrace();
                }

            }
        });

        return rongtaView;
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

    private void print() throws SdkException {

        if (mBitmap == null) {
            ToastUtil.show(getActivity(), "No image,Firstly Upload a image");
            return;
        }

        switch (BaseApp.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            default:
                break;
        }
    }

    private void escPrint() throws SdkException {
        new Thread(new Runnable() {
            @Override
            public void run() {

                showProgressDialog("Loading...");

                CmdFactory cmdFactory = new EscFactory();
                Cmd cmd = cmdFactory.create();
                cmd.append(cmd.getHeaderCmd());

                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                BitmapSetting bitmapSetting = new BitmapSetting();

                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_MULTI_COLOR);

                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                try {
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());//Sync Write
                }

                hideProgressDialog();
            }
        }).start();
    }


    public void showProgressDialog(final String str){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog == null){
                    progressDialog = new ProgressDialog(getActivity());
                }
                if(!TextUtils.isEmpty(str)){
                    progressDialog.setMessage(str);
                }else{
                    progressDialog.setMessage("Loading...");
                }
                progressDialog.show();
            }
        });

    }

    private void escSelftestPrint() {
        CmdFactory cmdFactory = new EscFactory();
        Cmd cmd = cmdFactory.create();
        cmd.append(cmd.getHeaderCmd());
        cmd.append(cmd.getLFCRCmd());
        cmd.append(cmd.getSelfTestCmd());
        cmd.append(cmd.getLFCRCmd());
        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }

    public void hideProgressDialog(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog != null && progressDialog.isShowing()){
                    progressDialog.hide();
                }
            }
        });

    }
}
