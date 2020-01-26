
package edi.md.mobile.NetworkUtils.RetrofitResults;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import edi.md.mobile.NetworkUtils.RetrofitResults.Assortment;

public class AssortmentListResult {

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
