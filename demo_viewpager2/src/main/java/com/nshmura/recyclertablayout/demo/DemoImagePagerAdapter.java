package com.nshmura.recyclertablayout.demo;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nshmura.recyclertablayout.RecyclerTabLayout;

import java.util.ArrayList;
import java.util.List;

class DemoImageViewHolder extends RecyclerView.ViewHolder {
    final TextView textView;

    public DemoImageViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.title);
    }
}

public class DemoImagePagerAdapter extends RecyclerView.Adapter<DemoImageViewHolder> implements RecyclerTabLayout.HasPageTitle {

    private List<Integer> items = new ArrayList<>();

    private LayoutInflater layoutInflater;

    public DemoImagePagerAdapter(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DemoImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DemoImageViewHolder(layoutInflater.inflate(R.layout.layout_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DemoImageViewHolder holder, int position) {
        holder.textView.setText("Page: " + position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @DrawableRes
    public int getImageResourceId(int position) {
        return items.get(position);
    }

    public void addAll(List<Integer> items) {
        this.items = new ArrayList<>(items);
    }

    @Nullable
    @Override
    public String getPageTitle(int position) {
        return String.valueOf(items.get(position));
    }
}
