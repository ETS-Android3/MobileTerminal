package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Client {
    @SerializedName("CardCode")
    @Expose
    private String cardCode;
    @SerializedName("CardID")
    @Expose
    private String cardID;
    @SerializedName("CardName")
    @Expose
    private String cardName;
    @SerializedName("ClientID")
    @Expose
    private String clientID;
    @SerializedName("ClientName")
    @Expose
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }
}
