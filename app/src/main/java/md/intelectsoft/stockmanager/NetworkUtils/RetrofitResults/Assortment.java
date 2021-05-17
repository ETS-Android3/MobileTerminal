
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
    private double incomePrice;
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
    private double price;
    @SerializedName("PriceLineID")
    @Expose
    private String priceLineID;
    @SerializedName("Remain")
    @Expose
    private double remain;
    @SerializedName("RequestedCount")
    @Expose
    private double requestedCount;
    @SerializedName("Unit")
    @Expose
    private String unit;
    @SerializedName("UnitInPackage")
    @Expose
    private String unitInPackage;
    @SerializedName("UnitPrice")
    @Expose
    private double unitPrice;
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

    public double getIncomePrice() {
        return incomePrice;
    }

    public void setIncomePrice(double incomePrice) {
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceLineID() {
        return priceLineID;
    }

    public void setPriceLineID(String priceLineID) {
        this.priceLineID = priceLineID;
    }

    public double getRemain() {
        return remain;
    }

    public void setRemain(double remain) {
        this.remain = remain;
    }

    public double getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(double requestedCount) {
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
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
