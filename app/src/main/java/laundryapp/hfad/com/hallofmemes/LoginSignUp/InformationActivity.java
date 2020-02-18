package laundryapp.hfad.com.hallofmemes.LoginSignUp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import laundryapp.hfad.com.hallofmemes.HomePage.HomeActivity;
import laundryapp.hfad.com.hallofmemes.R;

/*
This activity is is displayed after the SignUp step to take additional information from the user
 */

public class InformationActivity extends AppCompatActivity {
    private static final String TAG = "InformationActivity";

    //widgets
    private CircleImageView imgInfoProfilePicture;
    private TextView tvInfoName;
    private EditText etInfoPhone, etInfoDOB;
    private Button btnGetStarted;
    private Spinner etInfoGender;
    private int REQUEST_CODE = 1;


    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        imgInfoProfilePicture = findViewById(R.id.imgInfoProfilePicture);
        tvInfoName = findViewById(R.id.tvInfoName);
        etInfoPhone = findViewById(R.id.etInfoPhone);
        etInfoDOB = findViewById(R.id.etInfoDOB);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        etInfoGender = findViewById(R.id.etInfoGender);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //loads the name of the user
        loadData();

        //when the GET STARTED button is clicked upload information to the database
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndUploadInformation();
            }
        });

        imgInfoProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });

        //Gender Spinner
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(InformationActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.gender_item));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        etInfoGender.setAdapter(arrayAdapter);

    }

    /*
    Check if any fields are empty or not.
    If any fields are empty show a toast else upload data to the database
     */
    private void checkAndUploadInformation() {
        if (etInfoPhone.getText().toString().isEmpty() | (etInfoGender.getSelectedItem().toString().isEmpty() |
                etInfoGender.getSelectedItem().toString().equals("Gender"))
                | etInfoDOB.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
        else {
            uploadData();
            Intent intent = new Intent(InformationActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            ActivityCompat.finishAffinity(InformationActivity.this);
        }
    }

    //Makes a HashMap and uploads data to the database
    private void uploadData() {
        String uid = firebaseAuth.getUid();

        String phoneNo = etInfoPhone.getText().toString();
        String gender = etInfoGender.getSelectedItem().toString();
        String DOB = etInfoDOB.getText().toString();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("PhoneNo", phoneNo);
        userInfo.put("Gender", gender);
        userInfo.put("DOB", DOB);

        db.collection("users").document(uid).update(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: Data uploaded to database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload data to database: " + e);
                    }
                });

    }

    //Loads the name of the user to display on the top TextView
    private void loadData() {
        String uid = firebaseAuth.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("Name");

                        tvInfoName.setText(name);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Could not load data: " + e);
                    }
                });
    }

    //gets the image from the phone's local storage
    private void getImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imgInfoProfilePicture.setImageBitmap(bitmap);

                uploadImage(uri);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //uploads the image to the database
    private void uploadImage(Uri uri) {
        String uid = FirebaseAuth.getInstance().getUid();
        String imageLocation = "users/" + uid + "/profile_photo";
        StorageReference userPicture = storageReference.child(imageLocation);
        userPicture.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Could not upload photo: "+ e);
                    }
                });
    }

}
