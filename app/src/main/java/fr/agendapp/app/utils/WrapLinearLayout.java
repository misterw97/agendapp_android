package fr.agendapp.app.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created this class as a workaround of an issue with LinearLayoutManager and RecyclerView used together
 * The error IndexOut.. could definitively broke the App if it happened. Now the App can 'repair itself'
 */
public class WrapLinearLayout extends LinearLayoutManager {
    public WrapLinearLayout(Context context) {
        super(context);
    }

    public WrapLinearLayout(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public WrapLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.e("Erreur", "IndexOutOfBoundsException");
        }
    }
}
