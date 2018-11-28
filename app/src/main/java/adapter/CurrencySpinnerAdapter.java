package adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lonelyzerg.tianhang.currencyconverter.R;

import java.util.Objects;

public class CurrencySpinnerAdapter extends ArrayAdapter {
    private String[] currency_list;
    private TypedArray flag_list;
    private Context context;

    public class ViewHolder{
        TextView currency;
        ImageView flag;
    }

    public CurrencySpinnerAdapter(Context context, String[] currency_list, TypedArray flag_list) {
        super(context, R.layout.currency_items);
        this.currency_list = currency_list;
        this.flag_list = flag_list;
        this.context = context;

    }

    @Override
    public int getCount(){
        return currency_list.length;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        ViewHolder vh = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.currency_items, parent, false);
            vh.currency = convertView.findViewById(R.id.currency_name);
            vh.flag = convertView.findViewById(R.id.currency_flag);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.currency.setText(currency_list[position]);
        vh.flag.setImageResource(flag_list.getResourceId(position,-1));
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
