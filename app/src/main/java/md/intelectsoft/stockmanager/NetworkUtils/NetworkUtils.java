package md.intelectsoft.stockmanager.NetworkUtils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    public static URL Ping (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/Ping")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;

    }
    public static URL Autentificare (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/AuthenticateUser")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetClienta (String url_,String ClientID){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetClient?Identifier=" + ClientID)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetAssortiment (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_ + "json/GetAssortiment")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL SavePurchaseInvoice (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/SavePurchaseInvoice")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL TransferFromOneWareHouseToAnother (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/TransferFromOneWarehouseToAnother")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetPrinters (String url_,String ware){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetPrinters?WarehouseID="+ware)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL PrintInvoice (String url_,String InvoiceID,String PrinterID){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/PrintInvoice?InvoiceID="+InvoiceID + "&PrinterID=" + PrinterID)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL SaveInvoice (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/SaveInvoice")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetWareHouseList (String url_,String userId)  {
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetWarehousesList?UserID=" + userId)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL SaveAccumulateAssortmentList (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/SaveAccumulateAssortmentList")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetRevisions (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetRevisions")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL CreateRevision (String url_,String UserID,String Warehouse){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/CreateRevisions?UserID="+ UserID + "&Warehouse=" + Warehouse)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL SaveRevisionLine (String url_){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/SaveRevisionLine")
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetAssortimentListForStock (String url_,String UserID,String WareHouse){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetAssortimentListForStock?UserID="+ UserID+ "&WarehouseID="+WareHouse)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
    public static URL GetLabel (String url_,String typeLable){
        Uri builtUri;
        builtUri = Uri.parse(url_+"json/GetLableTemplate?LableType="+typeLable)
                .buildUpon()
                .build();
        URL url =null;
        try {
            url= new URL (builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String Response_from_Ping(URL url) throws IOException {
        String resp = "false";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_GetWareHouse(URL url) throws IOException {
        String resp = "false";
        HttpURLConnection urlConnection =(HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(6000);

        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_GetClient(URL url) throws IOException {
        String resp = "false";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(3000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_PrintInvoice(URL url) throws IOException {
        String resp = "false";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(4000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_GetRevision(URL url) throws IOException {
        String resp = "";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(2000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_CreateRevision(URL url) throws IOException {
        String resp = "";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_GetAssortimentListForStock(URL url) throws IOException {
        String resp = "";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(7000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
    public static String Response_from_GetLable(URL url) throws IOException {
        String resp = "";

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(2000);
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return resp;
            }

        } finally{
            urlConnection.disconnect();
        }
    }
}
