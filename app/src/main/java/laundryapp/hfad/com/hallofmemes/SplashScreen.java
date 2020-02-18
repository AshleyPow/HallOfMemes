package laundryapp.hfad.com.hallofmemes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import laundryapp.hfad.com.hallofmemes.HomePage.HomeActivity;
import laundryapp.hfad.com.hallofmemes.LoginSignUp.LoginSignUpActivity;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";

    //Firebase
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        user = FirebaseAuth.getInstance().getCurrentUser();

        //start thread
        background.start();
    }

    Thread background = new Thread() {
        public void run() {
            try {
                // Thread will sleep for 5 seconds
                sleep(2*1000);

                // After 2 seconds redirect to another intent
                checkUser();

                //Remove activity
                finish();
            }catch (Exception e) {
                Log.d(TAG, "run: " + e);
            }
        }
    };

    private void checkUser(){
        if (user != null) {
            Intent intent = new Intent(SplashScreen.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        else{
            Log.d(TAG, "checkUser: User is signed out");
            Intent intent = new Intent(SplashScreen.this, LoginSignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
