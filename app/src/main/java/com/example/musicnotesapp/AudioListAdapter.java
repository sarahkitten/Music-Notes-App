package com.example.musicnotesapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>{

    private final List<File> allFiles;
    private TimeAgo timeAgo;

    private final onItemListClick onItemListClick;

    public AudioListAdapter(List<File> allFiles, onItemListClick onItemListClick){
        this.allFiles = allFiles; // assign allFiles var from FileListFragment to this
        this.onItemListClick = onItemListClick;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles.get(position).getName());
        Log.d("Image:", holder.list_title.getText().toString());
        if(holder.list_title.getText().toString().endsWith("jpg")) {
            holder.itemView.findViewById(R.id.list_image_view).setVisibility(View.INVISIBLE);
            holder.list_image = holder.itemView.findViewById(R.id.list_image_view2);
            }
        else
            holder.itemView.findViewById(R.id.list_image_view2).setVisibility(View.INVISIBLE);
            holder.list_image = holder.itemView.findViewById(R.id.list_image_view);

        holder.list_date.setText(timeAgo.getTimeAgo(allFiles.get(position).lastModified()));

    }

    @Override
    public int getItemCount() {
        return allFiles.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image;
        private final TextView list_title;
        private final TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);
            list_image = itemView.findViewById(R.id.list_image_view);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemListClick.onClickListener(allFiles.get(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
    }

}
