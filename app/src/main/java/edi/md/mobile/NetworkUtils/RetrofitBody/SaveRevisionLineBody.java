package edi.md.mobile.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class SaveRevisionLineBody {
    @SerializedName("Assortiment")
    @Expose
    private String assortiment;
    @SerializedName("FinalQuantity")
    @Expose
    private Boolean finalQuantity;
    @SerializedName("Quantity")
    @Expose
    private Double quantity;
    @SerializedName("RevisionID")
    @Expose
    private String revisionID;

    public String getAssortiment() {
        return assortiment;
    }

    public void setAssortiment(String assortiment) {
        this.assortiment = assortiment;
    }

    public Boolean getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(Boolean finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getRevisionID() {
        return revisionID;
    }

    public void setRevisionID(String revisionID) {
        this.revisionID = revisionID;
    }
}
