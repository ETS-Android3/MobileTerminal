package edi.md.mobile.SettingsMenu;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;

import edi.md.mobile.R;

public class FragmentZebraPrinter extends Fragment {
    BluetoothPrintDriver mBTDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zebra_printer, null);
    }
}
