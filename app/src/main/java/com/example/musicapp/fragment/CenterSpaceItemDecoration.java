package com.example.musicapp.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public CenterSpaceItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // vị trí của item
        int column = position % spanCount; // cột hiện tại

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // khoảng cách bên trái của item
            outRect.right = (column + 1) * spacing / spanCount; // khoảng cách bên phải của item

            if (position < spanCount) {
                outRect.top = spacing; // khoảng cách phía trên của hàng đầu tiên
            }
            outRect.bottom = spacing; // khoảng cách phía dưới của mỗi hàng
        } else {
            outRect.left = column * spacing / spanCount; // khoảng cách bên trái của item
            outRect.right = spacing - (column + 1) * spacing / spanCount; // khoảng cách bên phải của item
            if (position >= spanCount) {
                outRect.top = spacing; // khoảng cách phía trên của mỗi hàng
            }
        }
    }
}