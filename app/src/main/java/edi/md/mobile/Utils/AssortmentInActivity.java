
package edi.md.mobile.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import edi.md.mobile.Settings.Assortment;

public class AssortmentInActivity implements Parcelable {

    Assortiment assortiment;

    @SerializedName("ErrorCode")
    @Expose
    private Integer errorCode;
    @SerializedName("ErrorMessage")
    @Expose
    private String errorMessage;
    @SerializedName("AllowNonIntegerSale")
    @Expose
    private String allowNonIntegerSale;
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

    public AssortmentInActivity(Parcel in) {
        if (in.readByte() == 0) {
            errorCode = null;
        } else {
            errorCode = in.readInt();
        }
        errorMessage = in.readString();
        allowNonIntegerSale = in.readString();
        assortimentID = in.readString();
        assortimentParentID = in.readString();
        barCode = in.readString();
        code = in.readString();
        incomePrice = in.readString();
        byte tmpIsFolder = in.readByte();
        isFolder = tmpIsFolder == 0 ? null : tmpIsFolder == 1;
        marking = in.readString();
        name = in.readString();
        price = in.readString();
        priceLineID = in.readString();
        remain = in.readString();
        requestedCount = in.readString();
        unit = in.readString();
        unitInPackage = in.readString();
        unitPrice = in.readString();
        vATCode = in.readString();
        mCount = in.readString();
    }

    public AssortmentInActivity(Assortment asl) {
        allowNonIntegerSale = asl.getAllowNonIntegerSale();
        assortimentID = asl.getAssortimentID();
        assortimentParentID = asl.getAssortimentParentID();
        barCode = asl.getBarCode();
        code = asl.getCode();
        incomePrice = asl.getIncomePrice();
        isFolder = asl.getIsFolder();
        marking = asl.getMarking();
        name = asl.getName();
        price = asl.getPrice();
        priceLineID = asl.getPriceLineID();
        remain = asl.getRemain();
        requestedCount = asl.getRequestedCount();
        unit = asl.getUnit();
        unitInPackage = asl.getUnitInPackage();
        unitPrice = asl.getUnitPrice();
        vATCode = asl.getVATCode();
        mCount = asl.getCount();
    }

    public static final Creator<AssortmentInActivity> CREATOR = new Creator<AssortmentInActivity>() {
        @Override
        public AssortmentInActivity createFromParcel(Parcel in) {
            return new AssortmentInActivity(in);
        }

        @Override
        public AssortmentInActivity[] newArray(int size) {
            return new AssortmentInActivity[size];
        }
    };

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

    public String getAllowNonIntegerSale() {
        return allowNonIntegerSale;
    }

    public void setAllowNonIntegerSale(String allowNonIntegerSale) {
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
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if (errorCode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(errorCode);
        }
        dest.writeString(errorMessage);
        dest.writeString(allowNonIntegerSale);
        dest.writeString(assortimentID);
        dest.writeString(assortimentParentID);
        dest.writeString(barCode);
        dest.writeString(code);
        dest.writeString(incomePrice);
        dest.writeByte((byte) (isFolder == null ? 0 : isFolder ? 1 : 2));
        dest.writeString(marking);
        dest.writeString(name);
        dest.writeString(price);
        dest.writeString(priceLineID);
        dest.writeString(remain);
        dest.writeString(requestedCount);
        dest.writeString(unit);
        dest.writeString(unitInPackage);
        dest.writeString(unitPrice);
        dest.writeString(vATCode);
    }
}
