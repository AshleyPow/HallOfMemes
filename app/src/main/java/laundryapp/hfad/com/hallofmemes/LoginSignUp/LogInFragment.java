package laundryapp.hfad.com.hallofmemes.LoginSignUp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import laundryapp.hfad.com.hallofmemes.HomePage.HomeActivity;
import laundryapp.hfad.com.hallofmemes.R;

public class LogInFragment extends Fragment {
    private static final String TAG = "LogInFragment";

    //tab position
    int position;

    //Login Screen instance
    EditText etLoginEmail, etLoginPassword;
    Button btnLogin;
    TextView tvDontHaveAccount, tvForgotPass;

    //Firebase
    private FirebaseAuth mAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LogInFragment() {
        // Required empty public constructor
    }

    public static Fragment getInstance(int position){
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        LogInFragment logInFragment = new LogInFragment();
        logInFragment.setArguments(bundle);
        return logInFragment;
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
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);

        //connect widgets
        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvDontHaveAccount = view.findViewById(R.id.tvDontHaveAnAccount);
        tvForgotPass = view.findViewById(R.id.tvForgotPass);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //when user clicks login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

        tvDontHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Move to SignUpFragment", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    //checks if email is valid or not
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //this method is signs the user in
    private void signInUser(){
        String loginEmail = etLoginEmail.getText().toString();
        String loginPassword = etLoginPassword.getText().toString();

        if (loginEmail.isEmpty() | loginPassword.isEmpty()){
            Toast.makeText(getContext(), "Enter email/password", Toast.LENGTH_SHORT).show();
        }
        else if (!isEmailValid(loginEmail)){
            Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_SHORT).show();
        }

        else {
            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: User Signed In");
                                Toast.makeText(getContext(), "Signed In Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                ActivityCompat.finishAffinity(getActivity());

                            } else {
                                Log.d(TAG, "onComplete: Authentication Failed");
                                Toast.makeText(getContext(), "Sign In Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
