package md.intelectsoft.stockmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.AssortmentListResult;
import md.intelectsoft.stockmanager.TerminalService.TerminalAPI;
import md.intelectsoft.stockmanager.TerminalService.TerminalRetrofitClient;
import md.intelectsoft.stockmanager.adapters.AssortmentListGridAdapter;
import md.intelectsoft.stockmanager.app.utils.SPFHelp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class CheckoutActivity extends AppCompatActivity {
    @BindView(R.id.buttonShowChangeWorkPlace) Button workPlace;
    @BindView(R.id.textLoadAssortment) TextView loadAssortment;
    @BindView(R.id.progressBarLoadAssortment) ProgressBar progressBarLoadAssortment;
    @BindView(R.id.listViewShowAssortment) ListView listView;

    String workPlaceName, workPlaceId, userId;
    AssortmentListGridAdapter adapter;
    TerminalAPI terminalAPI;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        Window window = getWindow();
        decorView.setSystemUiVisibility(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));

        setContentView(R.layout.activity_checkout);

        ButterKnife.bind(this);
        ButterKnife.setDebug(true);
        Toolbar toolbar = findViewById(R.id.toolbar_sales);
        setSupportActionBar(toolbar);
        setTitle("CheckOut");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(v -> finish());

        context = this;

        workPlaceName = SPFHelp.getInstance().getString("WorkPlaceName", "");
        workPlaceId = SPFHelp.getInstance().getString("WorkPlaceId", "");
        userId = SPFHelp.getInstance().getString("UserId", "");
        String uri = SPFHelp.getInstance().getString("URI", "");
        terminalAPI = TerminalRetrofitClient.getApiTerminalService(uri);

        if(!workPlaceId.equals(""))
            workPlace.setText(workPlaceName);

        List<Assortment> assortments = BaseApp.getInstance().getAssortments();
        if(assortments.size() > 0){
            loadAssortment.setVisibility(View.GONE);
            progressBarLoadAssortment.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            showAssortment(assortments, "00000000-0000-0000-0000-000000000000");
        }
        else{
            loadAssortment.setVisibility(View.VISIBLE);
            progressBarLoadAssortment.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);

            loadAssortments(userId, workPlaceId);
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Assortment itemSelected = adapter.getItem(position);
            if(itemSelected.getIsFolder()){
                showAssortment(assortments, itemSelected.getAssortimentID());
            }
            else{
                Toast.makeText(context, itemSelected.getName() + " adaugat!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAssortment(List<Assortment> assortments, String s) {
        List<Assortment> toShow = new ArrayList<>();
        for (Assortment item: assortments) {
            if(item.getAssortimentParentID().equals(s))
                toShow.add(item);
        }
        sortList(toShow);

        adapter = new AssortmentListGridAdapter(context, R.layout.item_grid_one_columns, toShow);
        listView.setAdapter(adapter);
    }

    private void sortList (List<Assortment> asl_list) {
        Collections.sort(asl_list, new Comparator<Assortment>() {

            public int compare(Assortment o1, Assortment o2) {

                String xy1 = String.valueOf(o1.getIsFolder());
                String xy2 = String.valueOf(o2.getIsFolder());
                int sComp = xy2.compareTo(xy1);

                if (sComp != 0) {
                    return sComp;
                } else {
                    String x1 = o1.getName();
                    String x2 = o2.getName();
                    return x1.compareTo (x2);
                }
            }});
    }

    private void loadAssortments(String userId, String workPlaceId) {
        Call<AssortmentListResult> call = terminalAPI.getAssortmentListForStock(userId, workPlaceId);
        call.enqueue(new Callback<AssortmentListResult>() {
            @Override
            public void onResponse(Call<AssortmentListResult> call, Response<AssortmentListResult> response) {
                AssortmentListResult assortmentListResult = response.body();
                if(assortmentListResult != null){
                    if(assortmentListResult.getErrorCode() == 0 ){
                        List<Assortment> items = assortmentListResult.getAssortments();
                        if(items != null && items.size() > 0){
                            BaseApp.getInstance().setAssortments(items);

                            loadAssortment.setVisibility(View.GONE);
                            progressBarLoadAssortment.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);

                            showAssortment(items, "00000000-0000-0000-0000-000000000000");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<AssortmentListResult> call, Throwable t) {

            }
        });
    }
}