package com.example.musicapp.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicapp.R;
import com.example.musicapp.fragment.List_Playlist;
import com.example.musicapp.model.Category;

import java.util.List;

public class exploreAdapter extends RecyclerView.Adapter<exploreAdapter.myViewHolder> {
    private List<Category> categories;

    public exploreAdapter(List<Category> categories) {
        this.categories = categories;
    }

    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        CardView cardView;
        TextView textView;
        ImageView imageView;
        OnItemClickListener listener;

        public myViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExplore);
            textView = itemView.findViewById(R.id.textExplore);
            imageView = itemView.findViewById(R.id.imageViewExplore);
            itemView.setOnClickListener(this);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            int position = getAbsoluteAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
//                listener.onItemClick(categories.get(position));
                Category selectedCategory = categories.get(position);
                List_Playlist fragment = new List_Playlist();
                fragment.setCategoryId(selectedCategory.getId());

                // Pass the selected category ID to the Fragment
                Bundle args = new Bundle();
                args.putString("categoryId", selectedCategory.getId());
                fragment.setArguments(args);

                // Add the Fragment to the Activity
                ((AppCompatActivity)v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }

        public void setOnItemClickListener(OnItemClickListener listenerInput) {
            listener = listenerInput;
        }

    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_card, parent, false);
        return new myViewHolder(view,new OnItemClickListener() {
            @Override
            public void onItemClick(Category category) {

            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView.setText(category.getName());

        Glide.with(holder.itemView.getContext())
                .load(category.getImageUrl())
                .placeholder(R.drawable.image_up)
                .error(R.drawable.image_up)
                .into(holder.imageView);

        if (position % 2 == 0) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
                       holder.cardView.setLayoutParams(layoutParams);
        } else {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            layoutParams.setMarginEnd(0);
            holder.cardView.setLayoutParams(layoutParams);
        }

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}