package com.example.android.askquastionapp.utils;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CustomItemTouchHelperCallBack extends ItemTouchHelper.Callback {
    /**
     * 获取动作标识
     * 动作标识分：dragFlags和swipeFlags
     * dragFlags：列表滚动方向的动作标识（如竖直列表就是上和下，水平列表就是左和右）
     * wipeFlags：与列表滚动方向垂直的动作标识（如竖直列表就是左和右，水平列表就是上和下）
     */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN|ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT; //拖拽方向
        int swipeFlags = 0; //禁止拖出屏幕
        //最终的动作标识（flags）必须要用makeMovementFlags()方法生成
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return onItemMove != null && onItemMove.onMove(viewHolder.getAdapterPosition(), viewHolder1.getAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    public interface OnItemMove {
        boolean onMove(int fromPosition, int toPosition);
    }

    private OnItemMove onItemMove;

    public void setOnItemMove(OnItemMove onItemMove) {
        this.onItemMove = onItemMove;
    }
}
