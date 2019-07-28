package edi.md.mobile.Utils;



import edi.md.mobile.Settings.ASL;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServiceApi {
    @GET("/DataTerminalService/json/GetAssortimentListForStock")
      Call<ASL> getAssortiment(@Query("UserID") String param1, @Query("WarehouseID") String param2);
}
