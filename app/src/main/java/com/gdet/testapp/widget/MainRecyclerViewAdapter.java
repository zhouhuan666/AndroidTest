package com.gdet.testapp.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gdet.testapp.R;

import java.util.List;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-10-07
 * 描述：
 */
public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainViewHolder> {

    private Context context;

    private List<String> list;
    private View inflater;

    OnItemClickListener mOnItemClickListener;

    public MainRecyclerViewAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(context).inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.button.setText(list.get(position));
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.page_name);
        }
    }

}
