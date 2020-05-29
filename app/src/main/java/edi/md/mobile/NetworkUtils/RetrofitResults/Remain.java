package edi.md.mobile.NetworkUtils.RetrofitResults;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 28.05.2020
 */

public class Remain {
    @SerializedName("Remain")
    @Expose
    private Double remain;
    @SerializedName("WarehouseID")
    @Expose
    private String warehouseID;
    @SerializedName("WarehouseName")
    @Expose
    private String warehouseName;

    public Double getRemain() {
        return remain;
    }

    public void setRemain(Double remain) {
        this.remain = remain;
    }

    public String getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        this.warehouseID = warehouseID;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }
}
