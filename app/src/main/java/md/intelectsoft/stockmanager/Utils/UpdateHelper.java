package md.intelectsoft.stockmanager.Utils;
import android.content.Context;
import android.content.pm.PackageManager;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by Igor on 12.05.2020
 */

public class UpdateHelper {
    public static String KEY_UPDATE_URL = "update_url";
    public static String KEY_UPDATE_VERSION = "version";
    public static String KEY_UPDATE_ENABLE = "is_update";

    public interface OnUpdateCheckListener{
        void onUpdateCheckListener(UpdateInformation information);
    }

    public static Builder with(Context context){
        return new Builder(context);
    }

    private OnUpdateCheckListener onUpdateCheckListener;
    private Context context;

    public UpdateHelper(Context context, OnUpdateCheckListener onUpdateCheckListener) {
        this.onUpdateCheckListener = onUpdateCheckListener;
        this.context = context;
    }

    public void check(){
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        UpdateInformation updateInformation = new UpdateInformation();

        updateInformation.setUpdate(remoteConfig.getBoolean(KEY_UPDATE_ENABLE));

        updateInformation.setNewVerion(remoteConfig.getString(KEY_UPDATE_VERSION));
        updateInformation.setUrl(remoteConfig.getString(KEY_UPDATE_URL));
        updateInformation.setCurrentVersion(getAppVersion(context));

        onUpdateCheckListener.onUpdateCheckListener(updateInformation);

    }

    private String getAppVersion(Context context){
        String result = "";

        try{
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
            result = result.replaceAll("[a-zA-Z] |-","");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class Builder{

        private Context context;
        private OnUpdateCheckListener onUpdateCheckListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateCheck(OnUpdateCheckListener onUpdateCheckListener){
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        public UpdateHelper build(){
            return new UpdateHelper(context,onUpdateCheckListener);
        }

        public UpdateHelper check(){
            UpdateHelper updateHelper = build();
            updateHelper.check();

            return updateHelper;
        }
    }
}
