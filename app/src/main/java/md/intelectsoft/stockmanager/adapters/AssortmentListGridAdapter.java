package md.intelectsoft.stockmanager.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

import md.intelectsoft.stockmanager.NetworkUtils.RetrofitResults.Assortment;
import md.intelectsoft.stockmanager.R;


/**
 * Created by Igor on 10.02.2020
 */

public class AssortmentListGridAdapter extends ArrayAdapter<Assortment> {
    int layoutId;
    public AssortmentListGridAdapter(@NonNull Context context, int resource, @NonNull List<Assortment> objects) {
        super(context, resource, objects);
        this.layoutId = resource;
    }

    private static class ViewHolder {
        TextView productName, productName2, productCode, productPrice, productStock;
        ImageView productImage;
        ConstraintLayout layoutParent;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        viewHolder = new ViewHolder();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        convertView = inflater.inflate(R.layout.item_grid_one_columns ,null,false);

        viewHolder.productName = convertView.findViewById(R.id.productName);
        viewHolder.productName2 = convertView.findViewById(R.id.textProductName2);
        viewHolder.productImage = convertView.findViewById(R.id.roundedImageView);
        viewHolder.productPrice = convertView.findViewById(R.id.productPrice);
        viewHolder.productCode = convertView.findViewById(R.id.productCode);
        viewHolder.productStock = convertView.findViewById(R.id.productStock);
        viewHolder.layoutParent = convertView.findViewById(R.id.ll_item_grid_view);

        Assortment item = getItem(position);

        viewHolder.productName.setText(item.getName());

        if(item.getIsFolder()) {
            viewHolder.productImage.setVisibility(View.VISIBLE);
            viewHolder.productName2.setVisibility(View.GONE);
            viewHolder.productPrice.setVisibility(View.GONE);
            viewHolder.productCode.setVisibility(View.GONE);
            viewHolder.productStock.setVisibility(View.GONE);
        }else {
            viewHolder.productImage.setVisibility(View.INVISIBLE);
            viewHolder.productName2.setVisibility(View.VISIBLE);

            viewHolder.productName2.setText(item.getName());
            viewHolder.productPrice.setText(String.format("%.2f", item.getPrice()) + " MDL");

        }

        return convertView;
    }
}
