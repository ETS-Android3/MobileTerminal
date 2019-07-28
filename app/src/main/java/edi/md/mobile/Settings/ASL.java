
package edi.md.mobile.Settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ASL {

    @SerializedName("Assortments")
    @Expose
    private List<Assortment> assortments = null;

    public List<Assortment> getAssortments() {
        return assortments;
    }

    public void setAssortments(List<Assortment> assortments) {
        this.assortments = assortments;
    }

}
