package md.intelectsoft.stockmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.Client;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitBody.GetClientsBody;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchProvider extends AppCompatActivity {

    String url;
    EditText searchField;
    TerminalAPI terminalAPI;
    RecyclerView searchProviderList;
    // List<Client> clients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_provider);
        searchField = findViewById(R.id.searchEditText);
        url = SPFHelp.getInstance().getString("URI", "");
        terminalAPI = TerminalRetrofitClient.getApiTerminalService(url);
        searchField.requestFocus();

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != "") {
                    searchProvider(charSequence.toString());
                } else {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    void searchProvider(String charSequence) {
        if (!charSequence.isEmpty()) {
            terminalAPI.getClients(charSequence).enqueue(new Callback<GetClientsBody>() {
                @Override
                public void onResponse(Call<GetClientsBody> call, Response<GetClientsBody> response) {
                    if (response != null) {
                        GetClientsBody clientsResponse = response.body();
                        if (clientsResponse != null)
                            if (!clientsResponse.getClients().isEmpty()){
                                getSearchClients(clientsResponse.getClients());
                            }else {
                               getSearchClients(Collections.<Client>emptyList());
                            }
                    }
                }


                @Override
                public void onFailure(Call<GetClientsBody> call, Throwable t) {

                }

            });
        } else {
            getSearchClients(Collections.emptyList());
        }
    }

    void getSearchClients(List<Client> clients) {

        searchProviderList = findViewById(R.id.search_list);

        ProviderAdapter adapter = new ProviderAdapter(clients);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        searchProviderList.setLayoutManager(layoutManager);
        searchProviderList.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
        searchProviderList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public class ProviderHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView providerName;
        String mClientId, mClientName;

        public ProviderHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            providerName = itemView.findViewById(R.id.provider_name);
        }

        void setData(String name, String id) {
            providerName.setText(name);
            mClientName = name;
            mClientId = id;
        }


        @Override
        public void onClick(View v) {
//            SPFHelp.getInstance().putString("ProviderId", mClientId);
//            SPFHelp.getInstance().putString("ProviderName", mClientName);

            Intent intent = new Intent();
            intent.putExtra("ProviderName",mClientName);
            intent.putExtra("ProviderId",mClientId);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private class ProviderAdapter extends RecyclerView.Adapter<ProviderHolder> {

        private List<Client> clients = null;



        public ProviderAdapter(List<Client> clients) {
            this.clients = clients;

        }


        public ProviderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.provider_list_item, parent, false);
            return new ProviderHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProviderHolder holder, int position) {
            Client client = clients.get(position);
            holder.setData(client.getClientName(), client.getClientID());
        }

        @Override
        public int getItemCount() {
            return clients.size();
        }
    }
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
