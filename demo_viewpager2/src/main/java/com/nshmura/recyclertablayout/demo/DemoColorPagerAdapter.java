package com.nshmura.recyclertablayout.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nshmura.recyclertablayout.RecyclerTabLayout;

import java.util.ArrayList;
import java.util.List;

class DemoColorViewHolder extends RecyclerView.ViewHolder {

    final TextView textView;

    public DemoColorViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.title);
    }
}

public class DemoColorPagerAdapter extends RecyclerView.Adapter<DemoColorViewHolder> implements RecyclerTabLayout.HasPageTitle {

    private List<ColorItem> items = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public DemoColorPagerAdapter(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public ColorItem getColorItem(int position) {
        return items.get(position);
    }

    public void addAll(List<ColorItem> items) {
        this.items = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public DemoColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DemoColorViewHolder(layoutInflater.inflate(R.layout.layout_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DemoColorViewHolder holder, int position) {
        holder.textView.setText("Page: " + items.get(position).hex);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Nullable
    @Override
    public String getPageTitle(int position) {
        return items.get(position).name;
    }
}
