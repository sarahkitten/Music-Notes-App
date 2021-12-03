/*
Source Code for the FileListFragment, CIS422 FA21
Author(s): Kale Satta-Hutton
Last Edited: 12/2/21
Sources:
    Base version of the code:
    https://www.youtube.com/watch?v=z--VaNj6l1U&ab_channel=TVACStudio
*/
package com.example.musicnotesapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder>{

    private final List<File> allFiles;
    private TimeAgo timeAgo;

    private final onItemListClick onItemListClick; //when an element is clicked


    public AudioListAdapter(List<File> allFiles, onItemListClick onItemListClick){
        this.allFiles = allFiles; // assign allFiles var from FileListFragment to this
        this.onItemListClick = onItemListClick; //assign each file an onItemListClick
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate holder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles.get(position).getName());
        Log.d("Image:", holder.list_title.getText().toString());

        // assign delete button
        holder.delete_btn = holder.itemView.findViewById(R.id.delete_button);

        //if its a .jpg
        if(holder.list_title.getText().toString().endsWith("jpg")) {
            holder.itemView.findViewById(R.id.list_image_view).setVisibility(View.INVISIBLE);
            holder.list_image = holder.itemView.findViewById(R.id.list_image_view2);
            //set ImageView to navigate properly
            }
        //its a .3gp
        else {
            holder.itemView.findViewById(R.id.list_image_view2).setVisibility(View.INVISIBLE);
            holder.list_image = holder.itemView.findViewById(R.id.list_image_view);
            //set ImageView to navigate properly
        }


        holder.list_date.setText(timeAgo.getTimeAgo(allFiles.get(position).lastModified()));
        //set last modified time
    }

    @Override
    public int getItemCount() {
        return allFiles.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image; // to play a recording
        private ImageView list_image2; // to pull up a saved image
        private final TextView list_title; //note or recording name
        private final TextView list_date; //note or recording last time created

        private ImageButton delete_btn; //to delete note or recording

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_title = itemView.findViewById(R.id.list_title); //holder for TextView
            list_date = itemView.findViewById(R.id.list_date); //holder for TextView
            list_image = itemView.findViewById(R.id.list_image_view); //holder for ImageView
            list_image2 = itemView.findViewById(R.id.list_image_view2); //holder for ImageView

            delete_btn = itemView.findViewById(R.id.delete_button); //holder for ImageButton

            list_image.setOnClickListener(this); // the play recording
            list_image2.setOnClickListener(this); // pull up image
            delete_btn.setOnClickListener(this); // delete
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.delete_button){ // if delete button pressed
                onItemListClick.onClickListener(allFiles.get(getAdapterPosition()), getAdapterPosition(), true);
            }else{ // if anything else pressed
                onItemListClick.onClickListener(allFiles.get(getAdapterPosition()), getAdapterPosition(), false);
            }
        }
    }

    public interface onItemListClick {
        //onClickListener interface class from onItemListClick
        void onClickListener(File file, int position, boolean delete_flag);
    }

}
