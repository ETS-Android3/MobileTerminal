package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateAssortmentBody {
    @SerializedName("Barcode")
    @Expose
    private String barcode;
    @SerializedName("Marking")
    @Expose
    private String marking;
    @SerializedName("Name")
    @Expose
    private String name;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
}

