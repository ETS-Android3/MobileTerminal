package edi.md.mobile.NetworkUtils;
import android.content.Context;

import edi.md.mobile.NetworkUtils.Services.CommandService;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Igor on 25.01.2020
 */

public class ApiRetrofit {

    public static CommandService getCommandService (Context context){
        String ip = context.getSharedPreferences("Settings", MODE_PRIVATE).getString("IP","192.168.1.1");
        String port = context.getSharedPreferences("Settings", MODE_PRIVATE).getString("Port","1111");
        if(ip.equals(""))
            ip = "192.168.1.1";
        if(port.equals(""))
            port = "1111";
        String uri = "http://" + ip + ":"+ port;

        return RetrofitClient.getDataRetrofitClient(uri).create(CommandService.class);
    }

}
