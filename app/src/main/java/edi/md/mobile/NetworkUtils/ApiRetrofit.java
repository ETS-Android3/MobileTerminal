package edi.md.mobile.NetworkUtils;
import android.content.Context;

import edi.md.mobile.NetworkUtils.Services.CommandService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Igor on 25.01.2020
 */

public class ApiRetrofit {

    public static CommandService getCommandService (Context context){
        String uri = "http://" + context.getSharedPreferences("Settings", MODE_PRIVATE).getString("IP","192.168.1.1") + ":"
                + context.getSharedPreferences("Settings", MODE_PRIVATE).getString("Port","1111");

        return RetrofitClient.getDataRetrofitClient(uri).create(CommandService.class);
    }

}
