package edi.md.mobile.NetworkUtils.RetrofitResults;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 25.01.2020
 */

public class GetWarehousesListResult {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("Warehouses")
    @Expose
    private List<WarehouseList> warehouses = null;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<WarehouseList> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<WarehouseList> warehouses) {
        this.warehouses = warehouses;
    }
}
