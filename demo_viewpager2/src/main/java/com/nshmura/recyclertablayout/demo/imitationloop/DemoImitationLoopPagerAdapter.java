package com.nshmura.recyclertablayout.demo.imitationloop;

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

/**
 * Created by Shinichi Nishimura on 2015/07/24.
 */
class DemoImitationLoopViewHoler extends RecyclerView.ViewHolder {

    final TextView textView;

    public DemoImitationLoopViewHoler(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.title);
    }
}

public class DemoImitationLoopPagerAdapter extends RecyclerView.Adapter<DemoImitationLoopViewHoler> implements RecyclerTabLayout.HasPageTitle {

    private static final int NUMBER_OF_LOOPS = 10000;

    private List<String> items = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public DemoImitationLoopPagerAdapter(@NonNull Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    public void addAll(List<String> items) {
        this.items = new ArrayList<>(items);
    }

    public int getCenterPosition(int position) {
        return items.size() * NUMBER_OF_LOOPS / 2 + position;
    }

    public String getValueAt(int position) {
        if (items.size() == 0) {
            return null;
        }
        return items.get(position % items.size());
    }

    @NonNull
    @Override
    public DemoImitationLoopViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DemoImitationLoopViewHoler(layoutInflater.inflate(R.layout.layout_page, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DemoImitationLoopViewHoler holder, int position) {
        holder.textView.setText("Page: " + getValueAt(position));
    }

    @Override
    public int getItemCount() {
        return items.size() * NUMBER_OF_LOOPS;
    }

    @Override
    public String getPageTitle(int position) {
        return getValueAt(position);
    }
}
