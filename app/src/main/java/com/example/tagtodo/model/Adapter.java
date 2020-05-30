package com.example.tagtodo.model;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tagtodo.note.NoteDetails;
import com.example.tagtodo.R;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView noteTitle, noteContent;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle  = itemView.findViewById(R.id.titles);
            noteContent= itemView.findViewById(R.id.content);
            view = itemView;
        }
    }

     List<String> titles;
     List<String> content;

    //Constructor for adapter
    public Adapter(List<String> title , List<String> content ){
        this.titles = title;
        this.content = content;
    }

    //  Used to create view for recycler view
    //  It will inflate note_view_layout.xml into the view for the Recycler from the parent(giving context) with parameters->
    //      1.xml file for the layout
    //      2.parent(where we want to display this)
    //      3.false(as we do not want to attach it to root)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
        return new ViewHolder(view);
    }

    //  Bind data to the view created by onCreateViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.noteTitle.setText(titles.get(position));
        holder.noteContent.setText(titles.get(position));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //when an item in holder is clicked, open the note (NoteDetails activity)
                Intent i = new Intent(v.getContext(), NoteDetails.class);
                i.putExtra("title",titles.get(position));          //Send title data(with key title) from adapter to NoteDetails when note number-> position is clicked
                i.putExtra("content",content.get(position));       //Send content data(with key content) from adapter to NoteDetails when note number-> position is clicked
                v.getContext().startActivity(i);
            }
        });
    }


    //  Number of items we want to display in recycler view
    @Override
    public int getItemCount() {
        return titles.size();
    }

}