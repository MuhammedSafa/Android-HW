package com.example.msb.hw;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MSB on 21.03.2016.
 */
public class CustomAdapter extends BaseAdapter {

    String[] resultList;
    Context context;
    List<String > arrList = new ArrayList<String>();
    private  static LayoutInflater inflater = null;
    String pName, pNumber;

    public CustomAdapter(MainActivity mainActivity,  List<String > list)
    {
        // result = prgmNameList;
        arrList = list;
        context = mainActivity;
        inflater = (LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return arrList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView name;
        TextView number;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.customlistview, null);

        resultList = arrList.get(position).split(":");
        pName = resultList[0];
        pNumber = resultList[1];

        holder.name = (TextView) rowView.findViewById(R.id.name);
        holder.name.setText(pName);

        holder.number = (TextView) rowView.findViewById(R.id.number);
        holder.number.setText(pNumber);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You Have Clicked " + arrList.get(position), Toast.LENGTH_SHORT).show();
                Uri number = Uri.parse("tel:" + arrList.get(position).split(":")[1]);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                context.startActivity(callIntent);
            }
        });
        return rowView;
    }
}
