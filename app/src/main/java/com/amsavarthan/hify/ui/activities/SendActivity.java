package com.amsavarthan.hify.ui.activities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.hify.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SendActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final int PLACE_PICKER_REQUEST =101 ;
    private TextView username;
    private String user_id,current_id;
    Button mSend;
    private EditText message;
    private FirebaseFirestore mFirestore;
    private ProgressBar mBar;
    private CircleImageView image;
    private Uri imageUri;
    private ImageView imagePreview;
    private FrameLayout imageLayout;
    private String image_;
    private StorageReference storageReference;
    private GeoDataClient mGeoDataClient;
    private Place place;
    private String name;


    public static void startActivityExtra(Context context,String extraString){
        Intent intent=new Intent(context,SendActivity.class);
        intent.putExtra("userId",extraString);
        context.startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        imageUri=null;
        mGeoDataClient = Places.getGeoDataClient(this, null);

        mFirestore=FirebaseFirestore.getInstance();
        user_id=getIntent().getStringExtra("userId");
        current_id= FirebaseAuth.getInstance().getUid();
        storageReference= FirebaseStorage.getInstance().getReference().child("notification").child(random()+".jpg");

        username=(TextView)findViewById(R.id.user_name);
        image=(CircleImageView)findViewById(R.id.image);
        imagePreview=(ImageView)findViewById(R.id.imagePreview);
        mSend=(Button) findViewById(R.id.send);
        message=(EditText)findViewById(R.id.message);
        mBar=(ProgressBar)findViewById(R.id.progressBar);
        imageLayout=(FrameLayout)findViewById(R.id.imageLayout);

        imageLayout.setVisibility(View.GONE);

        mFirestore.collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                name=documentSnapshot.getString("name");
                username.setText(name);

                image_=documentSnapshot.getString("image");

                RequestOptions requestOptions=new RequestOptions();
                requestOptions.placeholder(getResources().getDrawable(R.mipmap.profile_black));

                Glide.with(SendActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(image_)
                        .into(image);

            }
        });

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String message_=message.getText().toString();

                if(!TextUtils.isEmpty(message_)){

                    if(imageUri==null){

                        //Send only message
                        Toast.makeText(SendActivity.this, "Sending...", Toast.LENGTH_SHORT).show();

                        mBar.setVisibility(View.VISIBLE);
                        Map<String,Object> notificationMessage=new HashMap<>();
                        notificationMessage.put("message",message_);
                        notificationMessage.put("from",current_id);

                        mFirestore.collection("Users/"+user_id+"/Notifications").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                Toast.makeText(SendActivity.this, "Hify sent!", Toast.LENGTH_SHORT).show();
                                message.setText("");
                                mBar.setVisibility(View.INVISIBLE);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SendActivity.this, "Error sending Hify: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                mBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }else{
                        //Send message with Image
                        mBar.setVisibility(View.VISIBLE);

                        Toast.makeText(SendActivity.this, "Image uploading..", Toast.LENGTH_SHORT).show();

                        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(SendActivity.this, "Sending...", Toast.LENGTH_SHORT).show();

                                String downloadUri=taskSnapshot.getDownloadUrl().toString();

                                Map<String,Object> notificationMessage=new HashMap<>();
                                notificationMessage.put("image",downloadUri);
                                notificationMessage.put("message",message_);
                                notificationMessage.put("from",current_id);

                                mFirestore.collection("Users/"+user_id+"/Notifications_image").add(notificationMessage).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {

                                        Toast.makeText(SendActivity.this, "Hify sent!", Toast.LENGTH_SHORT).show();
                                        message.setText("");
                                        mBar.setVisibility(View.INVISIBLE);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SendActivity.this, "Error sending Hify: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        mBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(SendActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }


                }else{
                    Toast.makeText(SendActivity.this, "Enter some message to send.", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    public void SelectImage(View view) {

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Picture"),PICK_IMAGE);

    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void getPhotos(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
               try {
                   // Get the list of photos.
                   PlacePhotoMetadataResponse photos = task.getResult();
                   // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                   PlacePhotoMetadataBuffer photoMetadataBuffer;

                   photoMetadataBuffer = photos.getPhotoMetadata();
                   // Get the first photo in the list.
                   PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                   // Get the attribution text.
                   CharSequence attribution = photoMetadata.getAttributions();
                   // Get a full-size bitmap for the photo.
                   Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                   photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                       @Override
                       public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                           PlacePhotoResponse photo = task.getResult();
                           Bitmap bitmap = photo.getBitmap();
                           imageLayout.setVisibility(View.VISIBLE);
                           imagePreview.setImageURI(saveImageandGetUri(bitmap));
                           Toast.makeText(SendActivity.this, saveImageandGetUri(bitmap).toString(), Toast.LENGTH_SHORT).show();
                       }
                   });
               }catch (Exception ex){
                   Toast.makeText(SendActivity.this, "No image found for this place.", Toast.LENGTH_SHORT).show();
               }
            }
        });
    }

    public Uri saveImageandGetUri(Bitmap bitmap){

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        // Create a file to save the image
        file = new File(file, place.getName()+".jpg");

        try{

            OutputStream stream = null;

            stream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            stream.flush();

            stream.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Display the saved image to ImageView
        imagePreview.setImageURI(savedImageURI);
        return savedImageURI;

    }

    public Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title" , null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);

                new MaterialDialog.Builder(this)
                        .title("Location")
                        .content("Do you want to add the place's photo?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                getPhotos(place.getId());
                                dialog.dismiss();
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

               message.setText("");
                String toastMsg = String.format("Place Name: %s\nAddress: %s\nLatLng: %s,%s"
                        ,place.getName()
                        ,place.getAddress()
                        ,place.getLatLng().latitude
                        ,place.getLatLng().longitude);
                message.setText(toastMsg);
            }
        }

        if(requestCode==PICK_IMAGE){
            if(resultCode==RESULT_OK){
                new MaterialDialog.Builder(this)
                        .title("Attachment")
                        .content("Do you want to crop the image?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                imageUri=data.getData();
                                CropImage.activity(imageUri)
                                        .start(SendActivity.this);
                                dialog.dismiss();
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imageLayout.setVisibility(View.VISIBLE);
                        imageUri=data.getData();
                        imagePreview.setImageURI(imageUri);
                        dialog.dismiss();
                    }
                }).show();
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageLayout.setVisibility(View.VISIBLE);
                imageUri= result.getUri();
                imagePreview.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    public void RemoveImage(View view) {
        new MaterialDialog.Builder(this)
                .title("Attachment")
                .content("Do you want to remove the attachment?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        imagePreview.setImageDrawable(getResources().getDrawable(R.mipmap.fullimage));
                        imageLayout.setVisibility(View.GONE);
                        imageUri=null;
                        dialog.dismiss();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void PreviewImage(View view) {

        Intent intent=new Intent(SendActivity.this,ImagePreview.class)
                .putExtra("url","")
                .putExtra("uri",imageUri.toString());
        startActivity(intent);

    }

    public void PreviewProfileImage(View view) {

        Intent intent=new Intent(SendActivity.this,ImagePreviewSave.class)
                .putExtra("url",image_)
                .putExtra("uri","")
                .putExtra("sender_name",name);
        startActivity(intent);

    }

    public void OnLocationClick(View view) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Some error occurred.", Toast.LENGTH_SHORT).show();
            Log.e("Error",e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Google play services not available", Toast.LENGTH_SHORT).show();
            Log.e("Error",e.getMessage());
        }

    }
}
