package com.example.musicapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicapp.R;
import com.example.musicapp.model.Category;

import java.util.List;

public class exploreAdapter extends RecyclerView.Adapter<exploreAdapter.myViewHolder> {
    private List<Category> categories;

    public exploreAdapter(List<Category> categories) {
        this.categories = categories;
    }

    class myViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExplore);
            textView = itemView.findViewById(R.id.textExplore);
        }
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_card, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView.setText(category.getName());
        int margin = (holder.itemView.getContext().getResources().getDisplayMetrics().widthPixels - 300) / 10;

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
        layoutParams.leftMargin = margin;

        holder.cardView.setLayoutParams(layoutParams);

        // Sử dụng Glide hoặc Picasso để tải và hiển thị hình ảnh background

//        Glide.with(holder.itemView.getContext())
//                .load(category.getBackgroundImageUrl())
//                .into(imageView);

        // Đặt ImageView làm nền cho CardView

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}