package edi.md.mobile.NetworkUtils.RetrofitResults;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class GetAssortmentItemResult {
    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("AllowNonIntegerSale")
    @Expose
    private Boolean allowNonIntegerSale;
    @SerializedName("AssortimentID")
    @Expose
    private String assortimentID;
    @SerializedName("BarCode")
    @Expose
    private String barCode;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("IncomePrice")
    @Expose
    private Double incomePrice;
    @SerializedName("Marking")
    @Expose
    private String marking;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("PriceLineID")
    @Expose
    private String priceLineID;
    @SerializedName("Remain")
    @Expose
    private Double remain;
    @SerializedName("RequestedCount")
    @Expose
    private Double requestedCount;
    @SerializedName("Unit")
    @Expose
    private String unit;
    @SerializedName("UnitInPackage")
    @Expose
    private String unitInPackage;
    @SerializedName("UnitPrice")
    @Expose
    private Double unitPrice;
    @SerializedName("VATCode")
    @Expose
    private String vATCode;

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

    public Boolean getAllowNonIntegerSale() {
        return allowNonIntegerSale;
    }

    public void setAllowNonIntegerSale(Boolean allowNonIntegerSale) {
        this.allowNonIntegerSale = allowNonIntegerSale;
    }

    public String getAssortimentID() {
        return assortimentID;
    }

    public void setAssortimentID(String assortimentID) {
        this.assortimentID = assortimentID;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getIncomePrice() {
        return incomePrice;
    }

    public void setIncomePrice(Double incomePrice) {
        this.incomePrice = incomePrice;
    }

    public String getMarking() {
        return marking;
    }

    public void setMarking(String marking) {
        this.marking = marking;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public Double getRemain() {
        return remain;
    }

    public void setRemain(Double remain) {
        this.remain = remain;
    }

    public Double getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(Double requestedCount) {
        this.requestedCount = requestedCount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitInPackage() {
        return unitInPackage;
    }

    public void setUnitInPackage(String unitInPackage) {
        this.unitInPackage = unitInPackage;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getVATCode() {
        return vATCode;
    }

    public void setVATCode(String vATCode) {
        this.vATCode = vATCode;
    }
}
