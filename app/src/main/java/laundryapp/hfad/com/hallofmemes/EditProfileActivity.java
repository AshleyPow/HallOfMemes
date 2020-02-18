package laundryapp.hfad.com.hallofmemes;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import laundryapp.hfad.com.hallofmemes.LoginSignUp.InformationActivity;
import laundryapp.hfad.com.hallofmemes.LoginSignUp.LoginSignUpActivity;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";

    //widgets
    private Button btnLogOut, btnUpdateInfo;
    private EditText etProfileName, etProfileEmail, etProfilePhone, etProfileGender, etProfileDOB;
    private TextView btnProfileChangePass;
    private CircleImageView imgProfilePhoto;
    private int REQUEST_CODE = 1;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    //User Information
    private Map<String, Object> userInfo = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //reference widgets
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        etProfilePhone = findViewById(R.id.etProfilePhone);
        etProfileGender = findViewById(R.id.etProfileGender);
        etProfileDOB = findViewById(R.id.etProfileDOB);
        btnProfileChangePass = findViewById(R.id.btnProfileChangePass);
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo);
        btnLogOut = findViewById(R.id.btnLogOut);

        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        retrieveData();
        loadImage();

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInformation();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        imgProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImage();
            }
        });
    }

    //handles the sign out functionality
    private void signOut(){

        //Shows a dialog box to confirm to sign out or no
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure you want to sign out?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(EditProfileActivity.this, LoginSignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    //retrieves Name and Email and checks for other user information
    private void retrieveData(){
        String uid = firebaseAuth.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String name = documentSnapshot.getString("Name");
                            String email = documentSnapshot.getString("Email_ID");

                            etProfileName.setText(name);
                            etProfileEmail.setText(email);

                            //checking user information
                            checkInformation(documentSnapshot);

                        }
                        else{
                            Log.d(TAG, "onSuccess: Could not load data");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }

    //checks if user has PhoneNo, Gender, DOB and updates fields accordingly
    private void checkInformation(DocumentSnapshot documentSnapshot) {

        //checking Phone Number
        if (documentSnapshot.getString("PhoneNo") == null) {
            Log.d(TAG, "onSuccess: PhoneNo does not exists");
        }
        else {
            String phoneNo = documentSnapshot.getString("PhoneNo");
            etProfilePhone.setText(phoneNo);
        }

        //checking Gender
        if (documentSnapshot.getString("Gender") == null) {
            Log.d(TAG, "getInformation: Gender does not exists");
        }
        else {
            String gender = documentSnapshot.getString("Gender");
            etProfileGender.setText(gender);
        }

        //checking Date Of Birth
        if (documentSnapshot.getString("DOB") == null) {
            Log.d(TAG, "getInformation: DOB does not exists");
        }
        else {
            String birthday = documentSnapshot.getString("DOB");
            etProfileDOB.setText(birthday);
        }
    }

    /*
    Firstly checks if Name & Email are empty or not. If they are empty then fill them, then fill other fields.
    Checks if EditText field is empty or not.
    If it is empty then log it
    else save the data to the HashMap
     */
    private void getInformation() {

        if (etProfileName.getText().toString().isEmpty() | etProfileEmail.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Name/Email", Toast.LENGTH_SHORT).show();
        }
        else {

            //Update Name
            String name = etProfileName.getText().toString();
            userInfo.put("Name", name);

            //Update Email
            String email = etProfileEmail.getText().toString();
            userInfo.put("Email_ID", email);

            //Update Phone Number
            if (etProfilePhone.getText().toString().isEmpty()) {
                Log.d(TAG, "updateInformation: PhoneNo not provided");
            }
            else {
                String phoneNo = etProfilePhone.getText().toString();
                userInfo.put("PhoneNo", phoneNo);
            }

            //Update Gender
            if (etProfileGender.getText().toString().isEmpty()) {
                Log.d(TAG, "updateInformation: Gender not provided");
            }
            else {
                String gender = etProfileGender.getText().toString();
                userInfo.put("Gender", gender);
            }

            //Update Date Of Birth
            if (etProfileDOB.getText().toString().isEmpty()) {
                Log.d(TAG, "updateInformation: DOB not provided");
            }
            else {
                String birthday = etProfileDOB.getText().toString();
                userInfo.put("DOB", birthday);
            }

            //upload the information in the HashMap to the database
            updateInformation();

        }

    }

    //Update the data from the HashMap to the Database
    private void updateInformation(){
        String uid = firebaseAuth.getUid();

        db.collection("users").document(uid).update(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Information Updated", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: Data saved to database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to save data to database: " + e);
                    }
                });
    }

    //Get the image from the phone's local storage
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
                imgProfilePhoto.setImageBitmap(bitmap);

                uploadImage(uri);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //Upload the selected image to the database
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

    //Load an existing ProfilePicture if available
    private void loadImage() {
        String uid = FirebaseAuth.getInstance().getUid();
        String imageLocation = "users/" + uid + "/profile_photo";

        storageRef.child(imageLocation).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Glide.with(getApplicationContext()).load(uri).into(imgProfilePhoto);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

    }

    //Used to reload the HomeActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ActivityCompat.finishAffinity(EditProfileActivity.this);
    }
}
