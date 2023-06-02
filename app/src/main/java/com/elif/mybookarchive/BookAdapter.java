package com.elif.mybookarchive;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elif.mybookarchive.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.Bookholder> {

    ArrayList<Book> bookArrayList;
    public BookAdapter(ArrayList<Book> bookArrayList)
    {
        this.bookArrayList=bookArrayList;
    }



    @Override
    public Bookholder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerRowBinding binding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new Bookholder(binding);
    }

    @Override
    public void onBindViewHolder(BookAdapter.Bookholder holder, int position) {

        holder.binding.recyclerViewtw.setText(bookArrayList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),Bookactivity.class);
                intent.putExtra("info","old");  //bookactivity içersinde kontrol edilerek yeni eklenenin sayfası mı açılacak? ekleme sayfası mı ? kontrol için
                intent.putExtra("bookid",bookArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    public  class  Bookholder extends RecyclerView.ViewHolder{

        private  RecyclerRowBinding binding;
        public Bookholder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
