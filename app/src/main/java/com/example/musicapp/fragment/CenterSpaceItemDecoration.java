package com.example.musicapp.fragment;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class CenterSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;

    public CenterSpaceItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column

        outRect.left = column * spacing / spanCount; // space left
        outRect.right = spacing - (column + 1) * spacing / spanCount; // space right
        if (position >= spanCount) {
            outRect.top = 0;
            outRect.bottom = 0;// space top
        }
        // no bottom space needed
    }
}