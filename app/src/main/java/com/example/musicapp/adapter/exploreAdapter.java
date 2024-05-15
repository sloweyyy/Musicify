package com.example.musicapp.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        ImageView imageView;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExplore);
            textView = itemView.findViewById(R.id.textExplore);
            imageView = itemView.findViewById(R.id.imageViewExplore); // Add ImageView
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

        if (position % 2 == 0) {
            // Item chẵn, căn giữa về bên trái
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            layoutParams.setMarginStart(0);
            holder.cardView.setLayoutParams(layoutParams);
        } else {
            // Item lẻ, căn giữa về bên phải
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            layoutParams.setMarginEnd(0); // Set margin end về 0 để căn giữa về bên phải
            holder.cardView.setLayoutParams(layoutParams);
        }


    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}