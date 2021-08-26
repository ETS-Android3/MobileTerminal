
package md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Assortment {
    @SerializedName("AllowNonIntegerSale")
    @Expose
    private boolean allowNonIntegerSale;
    @SerializedName("AssortimentID")
    @Expose
    private String assortimentID;
    @SerializedName("AssortimentParentID")
    @Expose
    private String assortimentParentID;
    @SerializedName("BarCode")
    @Expose
    private String barCode;
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("IncomePrice")
    @Expose
    private String incomePrice;
    @SerializedName("IsFolder")
    @Expose
    private Boolean isFolder;
    @SerializedName("Marking")
    @Expose
    private String marking;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Price")
    @Expose
    private String price;
    @SerializedName("PriceLineID")
    @Expose
    private String priceLineID;
    @SerializedName("Remain")
    @Expose
    private String remain;
    @SerializedName("RequestedCount")
    @Expose
    private String requestedCount;
    @SerializedName("Unit")
    @Expose
    private String unit;
    @SerializedName("UnitInPackage")
    @Expose
    private String unitInPackage;
    @SerializedName("UnitPrice")
    @Expose
    private String unitPrice;
    @SerializedName("VATCode")
    @Expose
    private String vATCode;

    private String mCount;

    public boolean getAllowNonIntegerSale() {
        return allowNonIntegerSale;
    }

    public void setAllowNonIntegerSale(boolean allowNonIntegerSale) {
        this.allowNonIntegerSale = allowNonIntegerSale;
    }

    public String getAssortimentID() {
        return assortimentID;
    }

    public void setAssortimentID(String assortimentID) {
        this.assortimentID = assortimentID;
    }

    public String getAssortimentParentID() {
        return assortimentParentID;
    }

    public void setAssortimentParentID(String assortimentParentID) {
        this.assortimentParentID = assortimentParentID;
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

    public String getIncomePrice() {
        return incomePrice;
    }

    public void setIncomePrice(String incomePrice) {
        this.incomePrice = incomePrice;
    }

    public Boolean getIsFolder() {
        return isFolder;
    }

    public void setIsFolder(Boolean isFolder) {
        this.isFolder = isFolder;
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public String getRemain() {
        return remain;
    }

    public void setRemain(String remain) {
        this.remain = remain;
    }

    public String getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(String requestedCount) {
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

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getVATCode() {
        return vATCode;
    }

    public void setVATCode(String vATCode) {
        this.vATCode = vATCode;
    }

    public String getCount() {
        return mCount;
    }

    public void setCount(String count) {
        this.vATCode = count;
    }

}
