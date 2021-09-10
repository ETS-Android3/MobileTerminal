package md.intelectsoft.stockmanager.NetworkUtils.Services;
import java.util.List;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.AuthentificateUserBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetAssortmentItemBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveAccumulateAssortmentListBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveInvoiceBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SavePurchaseInvoiceBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.SaveRevisionLineBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.TransferFromOneWarehouseToAnotherBody;
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

/**
 * Created by Igor on 25.01.2020
 */

//public interface CommandService {
//    @GET("/DataTerminalService/json/Ping")
//    Call<Boolean> pingService ();
//
//    @POST("/DataTerminalService/json/AuthenticateUser")
//    Call<AuthentificateUserResult> authentificateUser (@Body AuthentificateUserBody user);
//
//    @GET("/DataTerminalService/json/SetBarcode")
//    Call<ResponseSimple> saveBarcodeForAssortment (@Query("UserID") String userId, @Query("Barcode") String barcode, @Query("ID") String assortmentId);
//
//    @GET("/DataTerminalService/json/GetAssortimentListForStock")
//    Call<AssortmentListResult> getAssortimentListForStock (@Query("UserID") String param1, @Query("WarehouseID") String param2);
//
//    @GET("/DataTerminalService/json/GetClient")
//    Call<AssortmentListResult> getClient (@Query("Identifier") String param1);
//
//    @POST("/DataTerminalService/json/GetAssortiment")
//    Call<GetAssortmentItemResult> getAssortmentItem (@Body GetAssortmentItemBody code);
//
//    @POST("/DataTerminalService/json/SavePurchaseInvoice")
//    Call<ResponseSimple> savePurchaseInvoice (@Body SavePurchaseInvoiceBody purchaseInvoiceBody);
//
//    @POST("/DataTerminalService/json/TransferFromOneWarehouseToAnother")
//    Call<ResponseSimple> transferFromOneWarehouseToAnother (@Body TransferFromOneWarehouseToAnotherBody transferFromOneWarehouseToAnotherBody);
//
//    @GET("/DataTerminalService/json/GetPrinters")
//    Call<GetPrintersResult> getPrinters (@Query("WarehouseID") String warehousId);
//
//    @GET("/DataTerminalService/json/GetPrintInvoice")
//    Call<GetPrintInvoiceResults> getPrintInvoice (@Query("InvoiceID") String invoiceID);
//
//    @GET("/DataTerminalService/json/PrintInvoice")
//    Call<ResponseSimple> printInvoice (@Query("InvoiceID") String invoiceId, @Query("PrinterID") String printerId);
//
//    @POST("/DataTerminalService/json/SaveInvoice")
//    Call<SaveInvoiceResult> saveInvoice (@Body SaveInvoiceBody saveInvoiceBody);
//
//    @GET("/DataTerminalService/json/GetWarehousesList")
//    Call<GetWarehousesListResult> getWareHousesList (@Query("UserID") String userId);
//
//    @POST("/DataTerminalService/json/SaveAccumulateAssortmentList")
//    Call<ResponseSimple> saveAccumulateAssortmentList (@Body List<SaveAccumulateAssortmentListBody> saveAccumulateAssortmentListBodies);
//
//    @GET("/DataTerminalService/json/GetRevisions")
//    Call<ResponseSimple> getRevisions ();
//
//    @GET("/DataTerminalService/json/CreateRevisions")
//    Call<ResponseSimple> createRevision (@Query("UserID") String userId, @Query("Warehouse") String wareHouse);
//
//    @POST("/DataTerminalService/json/SaveRevisionLine")
//    Call<ResponseSimple> savateReveRevisionLine (@Body SaveRevisionLineBody saveRevisionLineBody);
//
//    @GET("/DataTerminalService/json/GetLableTemplate")
//    Call<ResponseSimple> getLableTemplate (@Query("LableType") String lableType);
//
//    @GET("/DataTerminalService/json/GetAssortimentRemains")
//    Call<GetAssortmentRemainResults> getAssortimentRemains (@Query("AssortmentID") String assortmentID, @Query("WarehouseID") String warehouseID);
//}
