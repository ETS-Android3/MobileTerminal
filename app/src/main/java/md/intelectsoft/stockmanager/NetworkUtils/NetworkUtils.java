package md.intelectsoft.stockmanager.NetworkUtils;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    public static URL Ping (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/Ping")
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
    public static URL Autentificare (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/AuthenticateUser")
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
    public static URL GetClienta (String ip, String port,String ClientID){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetClient?Identifier=" + ClientID)
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
    public static URL GetAssortiment (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetAssortiment")
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
    public static URL SavePurchaseInvoice (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/SavePurchaseInvoice")
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
    public static URL TransferFromOneWareHouseToAnother (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/TransferFromOneWarehouseToAnother")
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
    public static URL GetPrinters (String ip, String port,String ware){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetPrinters?WarehouseID="+ware)
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
    public static URL PrintInvoice (String ip, String port,String InvoiceID,String PrinterID){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/PrintInvoice?InvoiceID="+InvoiceID + "&PrinterID=" + PrinterID)
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
    public static URL SaveInvoice (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/SaveInvoice")
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
    public static URL GetWareHouseList (String ip, String port,String userId)  {
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetWarehousesList?UserID=" + userId)
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
    public static URL SaveAccumulateAssortmentList (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/SaveAccumulateAssortmentList")
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
    public static URL GetRevisions (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetRevisions")
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
    public static URL CreateRevision (String ip, String port,String UserID,String Warehouse){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/CreateRevisions?UserID="+ UserID + "&Warehouse=" + Warehouse)
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
    public static URL SaveRevisionLine (String ip, String port){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/SaveRevisionLine")
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
    public static URL GetAssortimentListForStock (String ip, String port,String UserID,String WareHouse){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetAssortimentListForStock?UserID="+ UserID+ "&WarehouseID="+WareHouse)
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
    public static URL GetLabel (String ip, String port,String typeLable){
        Uri builtUri;
        builtUri = Uri.parse("http://" + ip + ":" + port + "/DataTerminalService/json/GetLableTemplate?LableType="+typeLable)
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
