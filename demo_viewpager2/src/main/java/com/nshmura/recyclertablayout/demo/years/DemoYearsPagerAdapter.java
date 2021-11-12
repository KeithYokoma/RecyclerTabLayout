package com.nshmura.recyclertablayout.demo.years;

import com.nshmura.recyclertablayout.RecyclerTabLayout;
import com.nshmura.recyclertablayout.demo.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class DemoYearsViewHoler extends RecyclerView.ViewHolder {

    final TextView textView;

    public DemoYearsViewHoler(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.title);
    }
}

public class DemoYearsPagerAdapter extends RecyclerView.Adapter<DemoYearsViewHoler> implements RecyclerTabLayout.HasPageTitle {

    private List<String> items = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public DemoYearsPagerAdapter(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DemoYearsViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DemoYearsViewHoler(layoutInflater.inflate(R.layout.layout_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DemoYearsViewHoler holder, int position) {
        holder.textView.setText("Page: " + items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public String getPageTitle(int position) {
        return items.get(position);
    }

    public void addAll(List<String> items) {
        this.items = new ArrayList<>(items);
    }
}
