package edi.md.mobile.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 25.01.2020
 */

public class TransferFromOneWarehouseToAnotherBody {
    @SerializedName("Confirm")
    @Expose
    private Boolean confirm;
    @SerializedName("FromWarehouseID")
    @Expose
    private String fromWarehouseID;
    @SerializedName("Lines")
    @Expose
    private List<TransferLineBody> lines = null;
    @SerializedName("ToWarehouseID")
    @Expose
    private String toWarehouseID;
    @SerializedName("TransferCode")
    @Expose
    private String transferCode;
    @SerializedName("UserID")
    @Expose
    private String userID;

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    public String getFromWarehouseID() {
        return fromWarehouseID;
    }

    public void setFromWarehouseID(String fromWarehouseID) {
        this.fromWarehouseID = fromWarehouseID;
    }

    public List<TransferLineBody> getLines() {
        return lines;
    }

    public void setLines(List<TransferLineBody> lines) {
        this.lines = lines;
    }

    public String getToWarehouseID() {
        return toWarehouseID;
    }

    public void setToWarehouseID(String toWarehouseID) {
        this.toWarehouseID = toWarehouseID;
    }

    public String getTransferCode() {
        return transferCode;
    }

    public void setTransferCode(String transferCode) {
        this.transferCode = transferCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
