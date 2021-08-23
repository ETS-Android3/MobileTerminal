package md.intelectsoft.stockmanager.TerminalService;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.AuthentificateUserBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.CreateAssortmentBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetClientsBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveInvoiceBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AssortmentListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AuthentificateUserResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentItemResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetAssortmentRemainResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintInvoiceResults;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetPrintersResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.GetWarehousesListResult;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.ResponseSimple;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.SaveInvoiceResult;
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

    @GET("json/GetAssortimentRemains")
    Call<GetAssortmentRemainResults> getAssortimentRemains (@Query("AssortmentID") String assortmentID, @Query("WarehouseID") String warehouseID);

    @GET("json/SetBarcode")
    Call<ResponseSimple> saveBarcodeForAssortment (@Query("UserID") String userId, @Query("Barcode") String barcode, @Query("ID") String assortmentId);

    @GET("json/GetPrinters")
    Call<GetPrintersResult> getPrinters (@Query("WarehouseID") String warehouseId);

    @GET("json/GetPrintInvoice")
    Call<GetPrintInvoiceResults> getPrintInvoice (@Query("InvoiceID") String invoiceID);

    @GET("json/PrintInvoice")
    Call<ResponseSimple> printInvoice (@Query("InvoiceID") String invoiceId, @Query("PrinterID") String printerId);

    @POST("json/SaveInvoice")
    Call<SaveInvoiceResult> saveInvoice (@Body SaveInvoiceBody saveInvoiceBody);
    @GET("json/GetAssortimentListForStock")
    Call<AssortmentListResult> getAssortimentListForStock (@Query("UserID") String param1, @Query("WarehouseID") String param2);
    @POST("json/CreateAssortiment")
    Call<ResponseSimple> createAssortment (@Body CreateAssortmentBody body);
    @GET("json/GetClients")
    Call<GetClientsBody> getClients (@Query("Criteria")String criteria);
}
