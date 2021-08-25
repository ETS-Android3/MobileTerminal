package md.intelectsoft.stockmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.CreateAssortmentBody;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.ResponseSimple;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CreateAssortment extends AppCompatActivity {
    TextInputLayout inputLayoutName, inputLayoutBarcode, inputLayoutMarking;
    TextInputEditText inputEditTextName, inputEditTextBarcode, inputEditTextMarking;
    Button buttonCreate;

    TerminalAPI terminalApi;
    ProgressDialog pgH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_create_assortment);
        Toolbar toolbar = findViewById(R.id.toolbar_create_product);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(getString(R.string.creare));
        toolbar.setNavigationOnClickListener(v -> finish());

        buttonCreate = findViewById(R.id.buttonCreate);
        inputLayoutName = findViewById(R.id.layoutName);
        inputLayoutBarcode = findViewById(R.id.layoutBarcode);
        inputLayoutMarking = findViewById(R.id.layoutMarking);
        inputEditTextName = findViewById(R.id.inputName);
        inputEditTextBarcode = findViewById(R.id.inputBarcode);
        inputEditTextMarking = findViewById(R.id.inputMarking);

        String url = SPFHelp.getInstance().getString("URI","");
        terminalApi = TerminalRetrofitClient.getApiTerminalService(url);
        pgH=new ProgressDialog(this);

        buttonCreate.setOnClickListener(view -> {
            if(inputEditTextName.getText().toString().equals("") && inputEditTextBarcode.getText().toString().equals("")){
                inputLayoutName.setError(getString(R.string.complete_this_filed));
                inputLayoutBarcode.setError(getString(R.string.complete_this_filed));
            }
            else{
                if(inputEditTextName.getText().toString().equals(""))
                    inputLayoutName.setError(getString(R.string.complete_this_filed));
                else if(inputEditTextBarcode.getText().toString().equals(""))
                    inputLayoutBarcode.setError(getString(R.string.complete_this_filed));
                else{
                    CreateAssortmentBody item = new CreateAssortmentBody();
                    item.setName(inputEditTextName.getText().toString());
                    item.setBarcode(inputEditTextBarcode.getText().toString());
                    item.setMarking(inputEditTextMarking.getText().toString().equals("") ? "" : inputEditTextMarking.getText().toString());

                    pgH.setMessage(getResources().getString(R.string.msg_dialog_loading));
                    pgH.setIndeterminate(true);
                    pgH.setCancelable(false);
                    pgH.show();


                    Call<ResponseSimple> call = terminalApi.createAssortment(item);
                    call.enqueue(new Callback<ResponseSimple>() {
                        @Override
                        public void onResponse(Call<ResponseSimple> call, Response<ResponseSimple> response) {
                            pgH.dismiss();
                            if(response.body().getErrorCode() == 0){
                                Toast.makeText(CreateAssortment.this, getString(R.string.item_created), Toast.LENGTH_SHORT).show();
                                inputEditTextName.setText("");
                                inputEditTextBarcode.setText("");
                                inputEditTextMarking.setText("");
                                inputEditTextName.requestFocus();
                            }

                            else
                                Toast.makeText(CreateAssortment.this, CreateAssortment.this.getString(R.string.error_create_item) + response.body().getErrorCode(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<ResponseSimple> call, Throwable t) {
                            pgH.dismiss();
                            Toast.makeText(CreateAssortment.this, CreateAssortment.this.getString(R.string.error_create_item)  + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        inputEditTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutName.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputEditTextBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals(""))
                    inputLayoutBarcode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

        return super.dispatchTouchEvent(event);
    }
}