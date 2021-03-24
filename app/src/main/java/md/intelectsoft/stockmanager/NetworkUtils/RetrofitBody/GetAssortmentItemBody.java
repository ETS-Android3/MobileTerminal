package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 25.01.2020
 */

public class GetAssortmentItemBody {
    @SerializedName("AssortmentIdentifier")
    @Expose
    private String assortmentIdentifier;
    @SerializedName("RequestNumbers")
    @Expose
    private List<String> requestNumbers = null;
    @SerializedName("ShowStocks")
    @Expose
    private Boolean showStocks;
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

    public List<String> getRequestNumbers() {
        return requestNumbers;
    }

    public void setRequestNumbers(List<String> requestNumbers) {
        this.requestNumbers = requestNumbers;
    }

    public Boolean getShowStocks() {
        return showStocks;
    }

    public void setShowStocks(Boolean showStocks) {
        this.showStocks = showStocks;
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
