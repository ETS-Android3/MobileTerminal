package edi.md.mobile.app.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {

    public static final int NONE = -1;
    public static final int CMD_ESC = 1, CMD_TSC = 2, CMD_CPCL = 3, CMD_ZPL = 4, CMD_PIN = 5;
    public static final int CON_BLUETOOTH = 1, CON_BLUETOOTH_BLE = 2, CON_WIFI = 3, CON_USB = 4, CON_COM = 5;
    public static final int NO_DEVICE = -1, HAS_DEVICE = 1;
    public static final int NO_PRINTER = 0, POS_PRINTER = 1, LABLE_PRINTER = 2;
    public static final int NO_MODEL = 0, RP_200 = 1, RP_300 = 2;

    @IntDef({CMD_ESC, CMD_TSC, CMD_CPCL, CMD_ZPL, CMD_PIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CmdType {
    }

    @IntDef({CON_BLUETOOTH, CON_WIFI, CON_USB, CON_COM, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectType {
    }


    @IntDef({NO_DEVICE, HAS_DEVICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChooseDevice {
    }

    @IntDef({NO_PRINTER, POS_PRINTER, LABLE_PRINTER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface typePrinter {
    }

    @IntDef({NO_MODEL, RP_200, RP_300})
    @Retention(RetentionPolicy.SOURCE)
    public @interface modelPrinter {
    }
}
