package com.elif.mybookarchive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.elif.mybookarchive.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Book> bookArrayList;
    BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

        bookArrayList=new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter=new BookAdapter(bookArrayList);
        binding.recyclerView.setAdapter(bookAdapter);

        getData();

    }

    //verileri çekmek
    private void getData()
    {
        try {
            SQLiteDatabase sqLiteDatabase =this.openOrCreateDatabase("Books",MODE_PRIVATE,null);

            Cursor cursor =sqLiteDatabase.rawQuery("SELECT* FROM books",null);
            int nameIx=cursor.getColumnIndex("bookname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext())
            {
                String name =cursor.getString(nameIx);
                int id =cursor.getInt(idIx);
                Book books =new Book(name,id);
                bookArrayList.add(books);

            }

            bookAdapter.notifyDataSetChanged(); //veri geldi göster demek
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //burada menu koda bağlanır
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater =getMenuInflater();
        menuInflater.inflate(R.menu.book_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    //tıklandığında ne olacağı
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.add_book)
        {
            Intent intent =new Intent(MainActivity.this,Bookactivity.class);
            intent.putExtra("info","new");  //bookactivity içersinde kontrol edilerek yeni eklenenin sayfası mı açılacak? ekleme sayfası mı ? kontrol için
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}