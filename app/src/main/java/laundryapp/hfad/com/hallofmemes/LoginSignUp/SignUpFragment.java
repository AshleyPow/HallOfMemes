package laundryapp.hfad.com.hallofmemes.LoginSignUp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import laundryapp.hfad.com.hallofmemes.R;


public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";

    //KEY_VALUES
    final String KEY_NAME = "Name";
    final String KEY_EMAIL = "Email_ID";

    //tab position
    int position;

    //Sign Up Screen Widgets
    EditText etSignupEmail, etSignupName, etSignUpPassword;
    Button btnSignup;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        SignUpFragment signUpFragment = new SignUpFragment();
        signUpFragment.setArguments(bundle);
        return signUpFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        //connect widgets
        etSignupName = view.findViewById(R.id.etSignupName);
        etSignupEmail = view.findViewById(R.id.etSignupEmail);
        etSignUpPassword = view.findViewById(R.id.etSignupPassword);
        btnSignup = view.findViewById(R.id.btnSignUp);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //when user clicks signup button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: registering user");
                registerNewUser();
            }
        });

        return view;
    }

    //checks if email is valid or not
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //This method registers a new user with the database DO NOT TOUCH!
    private void registerNewUser() {
        String signupEmail = etSignupEmail.getText().toString();
        String signupPassword = etSignUpPassword.getText().toString();
        String signupName = etSignupName.getText().toString();

        if (signupEmail.isEmpty() | signupPassword.isEmpty() | signupName.isEmpty()) {
            Toast.makeText(getContext(), "Enter name/email/password", Toast.LENGTH_SHORT).show();
        } else if (!isEmailValid(signupEmail)) {
            Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(signupEmail, signupPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getActivity(), "Signed Up Successfully", Toast.LENGTH_SHORT).show();

                                Log.d(TAG, "onClick: Saving Data");
                                saveData();

                                Intent intent = new Intent(getActivity(), InformationActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void saveData(){
        String uid = FirebaseAuth.getInstance().getUid();
        String name = etSignupName.getText().toString();
        String email = etSignupEmail.getText().toString();
        Map<String, Object> user = new HashMap<>();
        user.put(KEY_NAME, name);
        user.put(KEY_EMAIL, email);

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Data saved to Database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Data not saved: " + e);
                    }
                });
    }

}
