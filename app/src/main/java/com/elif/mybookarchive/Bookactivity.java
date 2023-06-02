package com.elif.mybookarchive;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.elif.mybookarchive.databinding.ActivityBookactivityBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Bookactivity extends AppCompatActivity {
    private ActivityBookactivityBinding binding;
    Bitmap selectedImage;
    SQLiteDatabase database;

    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitmek için
    ActivityResultLauncher<String> permissionLauncher;  //izin vermek için


    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityBookactivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database=this.openOrCreateDatabase("Books",MODE_PRIVATE,null);

        Intent intent= getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new"))
        {
            binding.booknamept.setText("");
            binding.authornamept.setText("");
            binding.numberofpagespt.setText("");

            binding.savebutton.setVisibility(View.VISIBLE);
            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimg);
            binding.imageView.setImageBitmap(selectImage);

        }else {
            int bookid = intent.getIntExtra("bookid",1);
            binding.savebutton.setVisibility(View.INVISIBLE);

            try{

                Cursor cursor=database.rawQuery("SELECT*FROM books WHERE id =?",new String[]{String.valueOf(bookid)});

                int booknameIx =cursor.getColumnIndex("bookname");
                int authornameIx =cursor.getColumnIndex("authorname");
                int pagesIx =cursor.getColumnIndex("pages");
                int imageIx =cursor.getColumnIndex("image");

                while(cursor.moveToNext())
                {
                    binding.booknamept.setText(cursor.getString(booknameIx));
                    binding.authornamept.setText(cursor.getString(authornameIx));
                    binding.numberofpagespt.setText(cursor.getString(pagesIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }



    public Bitmap makeSmallerImage(Bitmap image,int maxSize)
    {
        int height =image.getHeight();
        int width = image.getWidth();

        float bitmapRatio = (float)width/(float) height;
        if(bitmapRatio>1)
        {//landscape images için geçerli küçültme

            width=maxSize;
            height=(int)(width/bitmapRatio);

        }else {
            width=(int) (height/bitmapRatio);
            height=maxSize;

        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }



    public void save(View view) {
        String bookName = binding.booknamept.getText().toString();
        String authorName = binding.authornamept.getText().toString();
        String page = binding.numberofpagespt.getText().toString();

        Bitmap smallimage=makeSmallerImage(selectedImage,300);

        //sqlite için ekoymak için veriye çevirmek lazım,
        // byte dizisine çevirmek
        ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
        smallimage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] bytearray = outputStream.toByteArray();

        try {
            database= this.openOrCreateDatabase("Books",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS books(" +
                    "id INTEGER PRIMARY KEY," +
                    "bookname VARCHAR," +
                    "authorname VARCHAR," +
                    "pages VARCHAR," +
                    "image BLOB )");

            //values de değer yazılmıyor, sonradan çalıştırılabilecek sqlite statements çalıştırılacak
            //sonradan çağırılabilir
            //database.execSQL("INSERT INTO (bookname,authorname,pages,image) VALUES()");
            String sqlString ="INSERT INTO books(bookname,authorname,pages,image) VALUES(?,?,?,?)";

            //sonradan bağlama binding işlemlerini kolaylaştırmak için yapı
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);

            sqLiteStatement.bindString(1,bookName);
            sqLiteStatement.bindString(2,authorName);
            sqLiteStatement.bindString(3,page);
            sqLiteStatement.bindBlob(4,bytearray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }



        //kayıttan sonra geri dönmek
        Intent intent=new Intent(Bookactivity.this, MainActivity.class);
            //bundan önceki bütün aktivityleri kapat yeni açtığımı çalıştır
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);


    }

    public void selectImage(View view) {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            //android33+
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES))
                {
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }

            }
            else
            {
                //gallery
                Intent intentToGallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }else {//android 32
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
                {
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }

            }
            else
            {
                //gallery
                Intent intentToGallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }

    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) //kullanıcı galeriden seçim yapmış
                        {
                            Intent intentFromResult = result.getData();
                            if (intentFromResult != null) { //boşluğu kontrol ediyoruz
                                Uri imageData = intentFromResult.getData(); //görselin neede kayıtlı olduğunu veriyor
                                try {// veriyi bitmap e çevirme
                                    if (Build.VERSION.SDK_INT >= 28) { //telefon versiyon kontrolü
                                        ImageDecoder.Source source = ImageDecoder.createSource(Bookactivity.this.getContentResolver(),imageData);
                                        selectedImage = ImageDecoder.decodeBitmap(source);
                                        binding.imageView.setImageBitmap(selectedImage);

                                    } else {
                                        selectedImage = MediaStore.Images.Media.getBitmap(Bookactivity.this.getContentResolver(),imageData);
                                        binding.imageView.setImageBitmap(selectedImage);
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });


        permissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result) {
                            //permission granted, galeriye gitcez
                            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            activityResultLauncher.launch(intentToGallery);

                        } else {
                            //permission denied
                            Toast.makeText(Bookactivity.this, "Permisson needed!", Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }





}