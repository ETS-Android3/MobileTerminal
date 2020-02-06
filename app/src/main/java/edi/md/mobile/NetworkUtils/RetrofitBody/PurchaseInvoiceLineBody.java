package edi.md.mobile.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class PurchaseInvoiceLineBody {
    @SerializedName("Assortiment")
    @Expose
    private String assortiment;
    @SerializedName("IncomeSum")
    @Expose
    private Double incomeSum;
    @SerializedName("Quantity")
    @Expose
    private Double quantity;
    @SerializedName("SalePrice")
    @Expose
    private Double salePrice;
    @SerializedName("Warehouse")
    @Expose
    private String warehouse;

    public String getAssortiment() {
        return assortiment;
    }

    public void setAssortiment(String assortiment) {
        this.assortiment = assortiment;
    }

    public Double getIncomeSum() {
        return incomeSum;
    }

    public void setIncomeSum(Double incomeSum) {
        this.incomeSum = incomeSum;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}
