package md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Igor on 25.01.2020
 */

public class AuthentificateUserBody {
    @SerializedName("Code")
    @Expose
    private String code;
    @SerializedName("Password")
    @Expose
    private String password;
    @SerializedName("User")
    @Expose
    private String user;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
