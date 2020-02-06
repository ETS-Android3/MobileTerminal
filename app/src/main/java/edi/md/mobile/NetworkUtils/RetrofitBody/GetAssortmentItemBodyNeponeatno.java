package edi.md.mobile.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class GetAssortmentItemBodyNeponeatno {
    @SerializedName("AssortmentIdentifier")
    @Expose
    private String assortmentIdentifier;
    @SerializedName("UserID")
    @Expose
    private String userID;
    @SerializedName("WarehouseID")
    @Expose
    private String warehouseID;

    public String getAssortmentIdentifier() {
        return assortmentIdentifier;
    }

    public void setAssortmentIdentifier(String assortmentIdentifier) {
        this.assortmentIdentifier = assortmentIdentifier;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        this.warehouseID = warehouseID;
    }
}
