package edi.md.mobile.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Igor on 25.01.2020
 */

public class SaveInvoiceBody {
    @SerializedName("ClientCardID")
    @Expose
    private String clientCardID;
    @SerializedName("ClientID")
    @Expose
    private String clientID;
    @SerializedName("Comment")
    @Expose
    private String comment;
    @SerializedName("InvoiceNumber")
    @Expose
    private String invoiceNumber;
    @SerializedName("Lines")
    @Expose
    private List<InvoiceLineBody> lines = null;
    @SerializedName("TerminalCode")
    @Expose
    private String terminalCode;
    @SerializedName("UserID")
    @Expose
    private String userID;
    @SerializedName("Warehouse")
    @Expose
    private String warehouse;

    public String getClientCardID() {
        return clientCardID;
    }

    public void setClientCardID(String clientCardID) {
        this.clientCardID = clientCardID;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public List<InvoiceLineBody> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceLineBody> lines) {
        this.lines = lines;
    }

    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}
