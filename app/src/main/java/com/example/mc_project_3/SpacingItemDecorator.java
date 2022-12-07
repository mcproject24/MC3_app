package com.example.mc_project_3;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpacingItemDecorator extends RecyclerView.ItemDecoration {
    private final int verticalSpace;

    public SpacingItemDecorator(int verticalSpace){
        this.verticalSpace = verticalSpace;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state){
        outRect.bottom = verticalSpace;
    }


}
