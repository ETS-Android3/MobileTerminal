package md.intelectsoft.stockmanager.Utils;
/**
 * Created by Igor on 13.05.2020
 */

public class UpdateInformation {
    private boolean isUpdate;
    private String newVersion;
    private String url;

    private String currentVersion;

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public String getNewVerion() {
        return newVersion;
    }

    public void setNewVerion(String newVerion) {
        this.newVersion = newVerion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }
}
