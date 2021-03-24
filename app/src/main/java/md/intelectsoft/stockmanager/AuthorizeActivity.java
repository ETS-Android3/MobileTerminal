package md.intelectsoft.stockmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import md.intelectsoft.stockmanager.BrokerService.Body.SendRegisterApplication;
import md.intelectsoft.stockmanager.BrokerService.BrokerServiceAPI;
import md.intelectsoft.stockmanager.BrokerService.Body.SendGetURI;
import md.intelectsoft.stockmanager.BrokerService.BrokerRetrofitClient;
import md.intelectsoft.stockmanager.BrokerService.Results.AppDataRegisterApplication;
import md.intelectsoft.stockmanager.BrokerService.Results.RegisterApplication;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import md.intelectsoft.stockmanager.BrokerService.Enum.BrokerServiceEnum;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.AuthentificateUserBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AuthentificateUserResult;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.res.Configuration.KEYBOARD_QWERTY;

@SuppressLint("NonConstantResourceId")
public class AuthorizeActivity extends AppCompatActivity {
    @BindView(R.id.layoutActivateApp) ConstraintLayout registerForm;
    @BindView(R.id.layoutLoginToApp) ConstraintLayout authForm;
    @BindView(R.id.layoutButtons) ConstraintLayout buttonsForm;

    @BindView(R.id.layoutCode) TextInputLayout inputLayoutCode;
    @BindView(R.id.inputCode) TextInputEditText inputEditTextCode;

    @BindView(R.id.userCodePassword) EditText editTextUserCode;

    String androidID, deviceName, publicIp, privateIp, deviceSN, osVersion, deviceModel;
    BrokerServiceAPI brokerServiceAPI;
    TerminalAPI terminalAPI;
    ProgressDialog progressDialog;
    Context context;

    @OnClick(R.id.buttonActivate) void registerApplication(){
        String activationCode = inputEditTextCode.getText().toString();

        if(activationCode.equals("")){
            inputLayoutCode.setError("Input filed!");
        }
        else{
            //data send to register app in broker server
            SendRegisterApplication registerApplication = new SendRegisterApplication();

            String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
            registerApplication.setDeviceID(ids);
            registerApplication.setDeviceModel(deviceModel);
            registerApplication.setDeviceName(deviceName);
            registerApplication.setSerialNumber(deviceSN);
            registerApplication.setPrivateIP(privateIp);
            registerApplication.setPublicIP(publicIp);
            registerApplication.setOSType(BrokerServiceEnum.Android);
            registerApplication.setApplicationVersion(getAppVersion(this));
            registerApplication.setProductType(BrokerServiceEnum.StockManager);
            registerApplication.setOSVersion(osVersion);
            registerApplication.setLicenseActivationCode(activationCode);

            registerApplicationToBroker(registerApplication, activationCode);
        }
    }
    @OnClick(R.id.buttonLogin) void loginUser() {
        String userCode = editTextUserCode.getText().toString();

        if(!userCode.equals("") && userCode.length() > 0){
            String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
            authorizationToTerminalService(uri,userCode);
        }
        else{
            Toast showUI = Toast.makeText(context, "Input your code personal!", Toast.LENGTH_SHORT);
            showUI.setGravity(Gravity.CENTER,0, 0);
            showUI.show();
        }
    }


    @OnClick(R.id.buttonCode0) void onClick0(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "0";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode1) void onClick1(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "1";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode2) void onClick2(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "2";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode3) void onClick3(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "3";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode4) void onClick4(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "4";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode5) void onClick5(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "5";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode6) void onClick6(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "6";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode7) void onClick7(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "7";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode8) void onClick8(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "8";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonCode9) void onClick9(){
        String userCode = editTextUserCode.getText().toString();
        userCode = userCode + "9";
        editTextUserCode.setText(userCode);
    }
    @OnClick(R.id.buttonClear) void onClickClear(){
        editTextUserCode.setText("");
    }
    @OnClick(R.id.buttonCodeDelete) void onClickDelete(){
        String userCode = editTextUserCode.getText().toString();
        if(userCode.length() > 1){
            userCode = userCode.substring(0, userCode.length() - 1);
            editTextUserCode.setText(userCode);
        }
        else if (userCode.length() == 1){
            editTextUserCode.setText("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));

        setContentView(R.layout.activity_authorize);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);

        context = this;
        brokerServiceAPI = BrokerRetrofitClient.getApiBrokerService();
        progressDialog = new ProgressDialog(context);

        ActionBar var10000 = this.getSupportActionBar();
        if (var10000 != null) {
            var10000.hide();
        }

        askPermissions();

        deviceModel = Build.MODEL;
        deviceSN = Build.SERIAL;
        deviceName = Build.DEVICE;
        osVersion = Build.VERSION.RELEASE;
        androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        publicIp = getPublicIPAddress(this);
        privateIp = getIPAddress(true);

        SPFHelp.getInstance().putString("DeviceID", deviceId);
        SPFHelp.getInstance().putString("AndroidID", androidID);

        String licenseID = SPFHelp.getInstance().getString("LicenseID",null);

        if(licenseID == null){
            authForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        }
        else{
            registerForm.setVisibility(View.GONE);
            authForm.setVisibility(View.VISIBLE);

            String code = SPFHelp.getInstance().getString("ActivationCode","");

            getURI(licenseID, code,false);
        }

        inputEditTextCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutCode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editTextUserCode.setOnEditorActionListener((v, actionId, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String userCode = editTextUserCode.getText().toString();

                if (!userCode.equals("") && userCode.length() > 0) {
                    String uri = SPFHelp.getInstance().getString("URI", "0.0.0.0:1111");
                    authorizationToTerminalService(uri, userCode);
                } else {
                    Toast showUI = Toast.makeText(context, "Input your code personal!", Toast.LENGTH_SHORT);
                    showUI.setGravity(Gravity.CENTER, 0, 0);
                    showUI.show();
                }
            }
            return false;
        });
    }

    private void askPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int readpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int READ_PHONEpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        int reqInstallPackages = ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES);

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (readpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (READ_PHONEpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }
        if (reqInstallPackages != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    private boolean isKeyboardConnected() {
        int keyBoard = getResources().getConfiguration().keyboard;
        return getResources().getConfiguration().keyboard == 1;
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    private String getPublicIPAddress(Context context) {
        //final NetworkInfo info = NetworkUtils.getNetworkInfo(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();

        RunnableFuture<String> futureRun = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                if ((info != null && info.isAvailable()) && (info.isConnected())) {
                    StringBuilder response = new StringBuilder();

                    try {
                        HttpURLConnection urlConnection = (HttpURLConnection) (
                                new URL("http://checkip.amazonaws.com/").openConnection());
                        urlConnection.setRequestProperty("User-Agent", "Android-device");
                        //urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setReadTimeout(1000);
                        urlConnection.setConnectTimeout(1000);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setRequestProperty("Content-type", "application/json");
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();

                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                        }
                        urlConnection.disconnect();
                        return response.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Log.w(TAG, "No network available INTERNET OFF!");
                    return null;
                }
                return null;
            }
        });

        new Thread(futureRun).start();

        try {
            return futureRun.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerApplicationToBroker(SendRegisterApplication registerApplication, String activationCode) {
        Call<RegisterApplication> registerApplicationCall = brokerServiceAPI.registerApplicationCall(registerApplication);
        progressDialog.setMessage("Register application");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                registerApplicationCall.cancel();
                if(registerApplicationCall.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        registerApplicationCall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();

                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name

                        Map<String, String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID",appDataRegisterApplication.getLicenseID());
                        licenseData.put("LicenseCode",appDataRegisterApplication.getLicenseCode());
                        licenseData.put("CompanyName",appDataRegisterApplication.getCompany());
                        licenseData.put("CompanyIDNO",appDataRegisterApplication.getIDNO());

                        SPFHelp.getInstance().putStrings(licenseData);

                        //after register app ,get URI for accounting system on broker server
                        progressDialog.dismiss();

                        SPFHelp.getInstance().putString("ActivationCode",activationCode);

                        registerForm.setVisibility(View.GONE);
                        authForm.setVisibility(View.VISIBLE);

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5){
                            long nowDate = new Date().getTime();
                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            SPFHelp.getInstance().putString("URI", appDataRegisterApplication.getURI());
                            SPFHelp.getInstance().putLong("DateReceiveURI", nowDate);
                            SPFHelp.getInstance().putLong("ServerDate", serverDate);
                        }
                        else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
//                                        getURI(appDataRegisterApplication.getLicenseID(), activationCode, true);
                                    }))
                                    .show();

                        }
                    }
                    else {
                        progressDialog.dismiss();
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, "Failure connect to broker: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void authorizationToTerminalService(String uri, String userCode) {
        terminalAPI = TerminalRetrofitClient.getApiTerminalService(uri);

        AuthentificateUserBody user = new AuthentificateUserBody();
        user.setCode(userCode);

        Call<AuthentificateUserResult> authorizeUserCall = terminalAPI.authenticateUser(user);

        progressDialog.setMessage("Authenticate user...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                authorizeUserCall.cancel();
                if(authorizeUserCall.isCanceled())
                    dialog.dismiss();
            }
        });
        progressDialog.show();

        authorizeUserCall.enqueue(new Callback<AuthentificateUserResult>() {
            @Override
            public void onResponse(Call<AuthentificateUserResult> call, Response<AuthentificateUserResult> response) {
                AuthentificateUserResult user = response.body();
                if(user.getErrorCode() == 0){
                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("UserName", user.getName());
                    userMap.put("UserId", user.getUserID());

                    SPFHelp.getInstance().putStrings(userMap);

                    progressDialog.dismiss();
                    finish();
                    startActivity(new Intent(context, MainActivity.class));
                }
                else if(user.getErrorCode() == 1){
                    progressDialog.dismiss();

                    Toast showUI = Toast.makeText(context, "User with this code not found!" , Toast.LENGTH_SHORT);
                    showUI.setGravity(Gravity.CENTER,0, 0);
                    showUI.show();
                }

                else{
                    progressDialog.dismiss();
                    Toast showUI = Toast.makeText(context, "Error authenticate user! Message: " + user.getErrorMessage() , Toast.LENGTH_SHORT);
                    showUI.setGravity(Gravity.CENTER,0, 0);
                    showUI.show();
                }
            }

            @Override
            public void onFailure(Call<AuthentificateUserResult> call, Throwable t) {
                progressDialog.dismiss();

                Toast showUI = Toast.makeText(context, "Failure authorize user: " + t.getMessage(), Toast.LENGTH_SHORT);
                showUI.setGravity(Gravity.CENTER,0, 0);
                showUI.show();
            }
        });

    }

    private void getURI(String licenseID, String codeActivation, boolean fromRegistration) {
        //data send to register app in broker server
        SendGetURI registerApplication = new SendGetURI();

        String ids = new UUID(androidID.hashCode(),androidID.hashCode()).toString();
        registerApplication.setDeviceID(ids);
        registerApplication.setDeviceModel(deviceModel);
        registerApplication.setDeviceName(deviceName);
        registerApplication.setSerialNumber(deviceSN);
        registerApplication.setPrivateIP(privateIp);
        registerApplication.setPublicIP(publicIp);
        registerApplication.setLicenseID(licenseID);
        registerApplication.setOSType(BrokerServiceEnum.Android);
        registerApplication.setApplicationVersion(getAppVersion(this));
        registerApplication.setProductType(BrokerServiceEnum.SalesAgent);
        registerApplication.setOSVersion(osVersion);

        Call<RegisterApplication> getURICall = brokerServiceAPI.getURICall(registerApplication);

        if (fromRegistration) {
            progressDialog.setMessage("Obtain URI...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setButton(-1, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getURICall.cancel();
                    if (getURICall.isCanceled())
                        dialog.dismiss();
                }
            });
            progressDialog.show();
        }

        getURICall.enqueue(new Callback<RegisterApplication>() {
            @Override
            public void onResponse(Call<RegisterApplication> call, Response<RegisterApplication> response) {
                RegisterApplication result = response.body();
                if (result == null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Response from broker server is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(result.getErrorCode() == 0) {
                        AppDataRegisterApplication appDataRegisterApplication = result.getAppData();
                        //if app registered successful , save installation id and company name
                        Map<String, String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID",appDataRegisterApplication.getLicenseID());
                        licenseData.put("LicenseCode",appDataRegisterApplication.getLicenseCode());
                        licenseData.put("CompanyName",appDataRegisterApplication.getCompany());
                        licenseData.put("CompanyIDNO",appDataRegisterApplication.getIDNO());

                        SPFHelp.getInstance().putStrings(licenseData);

                        if(appDataRegisterApplication.getURI() != null && !appDataRegisterApplication.getURI().equals("") && appDataRegisterApplication.getURI().length() > 5) {
                            long nowDate = new Date().getTime();
                            String serverStringDate = appDataRegisterApplication.getServerDateTime();
                            serverStringDate = serverStringDate.replace("/Date(","");
                            serverStringDate = serverStringDate.replace("+0200)/","");
                            serverStringDate = serverStringDate.replace("+0300)/","");

                            long serverDate = Long.parseLong(serverStringDate);

                            SPFHelp.getInstance().putString("URI", appDataRegisterApplication.getURI());
                            SPFHelp.getInstance().putLong("DateReceiveURI", nowDate);
                            SPFHelp.getInstance().putLong("ServerDate", serverDate);

                        }else{
                            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setTitle("URL not set!")
                                    .setMessage("The application is not fully configured.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialogInterface, i) -> {
                                        finish();
                                    })
                                    .setNegativeButton("Retry",((dialogInterface, i) -> {
                                        getURI(licenseID, codeActivation, fromRegistration);
                                    }))
                                    .show();
                        }
                    }
                    else if(result.getErrorCode() == 133){
                        Map<String, String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("LicenseCode","");
                        licenseData.put("CompanyName","");
                        licenseData.put("CompanyIDNO","");

                        SPFHelp.getInstance().putStrings(licenseData);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("Application not activated!")
                                .setMessage("The application is not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    finish();
                                })
                                .show();
                    }
                    else if(result.getErrorCode() == 134){
                        Map<String, String> licenseData = new HashMap<>();
                        licenseData.put("LicenseID", null);
                        licenseData.put("LicenseCode","");
                        licenseData.put("CompanyName","");
                        licenseData.put("CompanyIDNO","");

                        SPFHelp.getInstance().putStrings(licenseData);

                        new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                .setTitle("License not activated!")
                                .setMessage("The license for this application not activated! Please activate can you continue.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    authForm.setVisibility(View.GONE);
                                    registerForm.setVisibility(View.VISIBLE);
                                })
                                .setNegativeButton("Cancel",((dialogInterface, i) -> {
                                    finish();
                                }))
                                .show();
                    }
                    else
                        Toast.makeText(context, result.getErrorMessage(), Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<RegisterApplication> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_1: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "1";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_2: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "2";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_3: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "3";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_4: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "4";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_5: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "5";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_6: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "6";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_7: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "7";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_8: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "8";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_9: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "9";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_0: {
                    String userCode = editTextUserCode.getText().toString();
                    userCode = userCode + "0";
                    editTextUserCode.setText(userCode);
                }
                break;
                case KeyEvent.KEYCODE_DEL: {
                    String userCode = editTextUserCode.getText().toString();
                    if(userCode.length() > 1){
                        userCode = userCode.substring(0, userCode.length() - 1);
                        editTextUserCode.setText(userCode);
                    }
                    else if (userCode.length() == 1){
                        editTextUserCode.setText("");
                    }
                }break;

                default:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
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
}