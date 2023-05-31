package com.elif.mybookarchive;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

public class Bookactivity extends AppCompatActivity {
private ActivityBookactivityBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher; //galeriye gitmek için
    ActivityResultLauncher<String> permissionLauncher;  //izin vermek için

    Bitmap selectedImage;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityBookactivityBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        Bitmap smallimage=makeSmallerImage(selectedImage,300);

        //sqlite için ekoymak için veriye çevirmek lazım,
        // byte dizisine çevirmek
        ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
        smallimage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] bytearray = outputStream.toByteArray();


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

        return image.createScaledBitmap(image,width,height,true);
    }



    public void save(View view) {
        String bookName = binding.booknamept.getText().toString();
        String authorName = binding.authornamept.getText().toString();
        String page = binding.numberofpagespt.getText().toString();




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