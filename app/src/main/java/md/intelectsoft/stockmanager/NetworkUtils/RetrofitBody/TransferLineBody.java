package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class TransferLineBody {
    @SerializedName("Assortiment")
    @Expose
    private String assortiment;
    @SerializedName("Quantity")
    @Expose
    private Double quantity;

    public String getAssortiment() {
        return assortiment;
    }

    public void setAssortiment(String assortiment) {
        this.assortiment = assortiment;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
