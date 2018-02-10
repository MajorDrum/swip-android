package com.carmichael.swip;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.carmichael.swip.Models.TradeItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "UploadActivity";
    ImageView imgUploadImage;
    TextView tvUploadImage;
    EditText etItemName;
    EditText etItemDescription;
    public static final int CAMERA_REQUEST_CODE = 003;
    public static final int TAKE_IMAGE_REQUEST = 024;
    public static final int CHOOSE_IMAGE_REQUEST = 042;
    AlertDialog alertDialog;
    File imageFile;
    Bitmap bitmap;
    private FirebaseAuth mAuth;
    FirebaseUser fUser;
    DatabaseReference mDatabase;
//    AmazonS3 s3;
//    TransferUtility transferUtility;
//    TransferObserver observer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        final View imageButtonView = getLayoutInflater().inflate(R.layout.view_camera_choice, null);

        Button btnLoadImage = (Button) imageButtonView.findViewById(R.id.loadimage);
        Button btnTakeImage = (Button) imageButtonView.findViewById(R.id.takeimage);
        imgUploadImage = (ImageView)findViewById(R.id.imgUploadImage);
        etItemDescription = (EditText)findViewById(R.id.etItemDescription);
        etItemName = (EditText)findViewById(R.id.etItemName);
        imgUploadImage.setVisibility(View.GONE);

        tvUploadImage = (TextView) findViewById(R.id.tvUploadImage);

//        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
//                getApplicationContext(),
//                "identityPoolId",
//                Regions.US_WEST_1);
//
//        s3 = new AmazonS3Client(credentialsProvider);


        imgUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageButtonView.getParent() != null) {
                    ((ViewGroup) imageButtonView.getParent()).removeAllViews();
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadActivity.this);
                alertDialogBuilder.setView(imageButtonView);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                Window window = alertDialog.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                window.setAttributes(wlp);
            }
        });

        tvUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageButtonView.getParent() != null) {
                    ((ViewGroup) imageButtonView.getParent()).removeAllViews();
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadActivity.this);
                alertDialogBuilder.setView(imageButtonView);
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                Window window = alertDialog.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.gravity = Gravity.BOTTOM;
                window.setAttributes(wlp);
            }
        });



        btnLoadImage.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                alertDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHOOSE_IMAGE_REQUEST);
            }});

        btnTakeImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST_CODE);
                }else{
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, TAKE_IMAGE_REQUEST);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload_menu, menu);
        return true;
    }

    ProgressDialog pd;

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.upload_menu_item:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UploadActivity.this);
                alertDialogBuilder.setMessage("Would you like to upload this item?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pd = new ProgressDialog(UploadActivity.this);
                        pd.setMessage("Uploading your item...");
                        pd.show();
                    if(imgUploadImage.getDrawable() != null){ // This is not working
                        // Post item to TradeItems database
                        String itemName = etItemName.getText().toString();
                        String itemDescription = etItemDescription.getText().toString();

                        TradeItem myItem = new TradeItem();
                        myItem.setName(itemName);
                        myItem.setDescription(itemDescription);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        mDatabase = database.getReference();

                        mAuth = FirebaseAuth.getInstance();
                        fUser = mAuth.getCurrentUser();

                        final String key = mDatabase.child("TradeItems").push().getKey();
                        Map<String, String> postValues = new HashMap<>();
                        postValues.put("name", myItem.getName());
                        postValues.put("description", myItem.getDescription());
                        postValues.put("userId", fUser.getUid());

                        Map<String, Object> tradeItemUpdates = new HashMap<>();
                        tradeItemUpdates.put("/TradeItems/" + key, postValues);


                        mDatabase.child("Users").child(fUser.getUid()).child("tradeItems").child(key).setValue("true");
                        mDatabase.updateChildren(tradeItemUpdates);


                        String tradeItemReference = "TradeItems/" + key;
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        StorageReference tradeItemRef = storageRef.child(tradeItemReference);


                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = tradeItemRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                pd.dismiss();
                                Toast.makeText(UploadActivity.this, "Unable to upload item.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                etItemName.setText(null);
                                etItemDescription.setText(null);
                                tvUploadImage.setVisibility(View.VISIBLE);
                                imgUploadImage.setImageBitmap(null);
                                etItemName.requestFocus();
                                pd.dismiss();
                                Toast.makeText(UploadActivity.this, "Item uploaded successfully.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        Toast.makeText(UploadActivity.this, "No picture detected.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_IMAGE_REQUEST);
            }
            else {
                Toast.makeText(this, "You will need to enable camera permissions if you wish to " +
                        "take a picture to upload.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        Uri targetUri;
        if (requestCode == TAKE_IMAGE_REQUEST && resultCode != RESULT_CANCELED) {
            tvUploadImage.setVisibility(View.GONE);
            imgUploadImage.setVisibility(View.VISIBLE);
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");

            imgUploadImage.setImageBitmap(bitmap);
        }else if(requestCode == CHOOSE_IMAGE_REQUEST && resultCode == RESULT_OK){
            tvUploadImage.setVisibility(View.GONE);
            imgUploadImage.setVisibility(View.VISIBLE);
            targetUri = data.getData();
            imageFile = new File(targetUri.toString());
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                imgUploadImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UploadActivity.this, TradeActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }
}
