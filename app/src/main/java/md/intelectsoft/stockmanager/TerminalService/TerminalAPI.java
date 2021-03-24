package md.intelectsoft.stockmanager.TerminalService;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.AuthentificateUserBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AssortmentListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AuthentificateUserResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TerminalAPI {
    @GET("json/Ping")
    Call<Boolean> pingService ();

    @POST("json/AuthenticateUser")
    Call<AuthentificateUserResult> authenticateUser(@Body AuthentificateUserBody user);

    @GET("json/GetWarehousesList")
    Call<GetWarehousesListResult> getWareHousesList (@Query("UserID") String userId);

    @GET("json/GetAssortimentListForStock")
    Call<AssortmentListResult> getAssortmentListForStock (@Query("UserID") String userId, @Query("WarehouseID") String wareHouseId);

    @POST("json/GetAssortiment")
    Call<GetAssortmentItemResult> getAssortmentItem (@Body GetAssortmentItemBody item);
}
