package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class SaveAccumulateAssortmentListBody {

    @SerializedName("AssortimentID")
    @Expose
    private String assortimentID;
    @SerializedName("Quantity")
    @Expose
    private Double quantity;
    @SerializedName("WarehouseID")
    @Expose
    private String warehouseID;

    public String getAssortimentID() {
        return assortimentID;
    }

    public void setAssortimentID(String assortimentID) {
        this.assortimentID = assortimentID;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        this.warehouseID = warehouseID;
    }
}
