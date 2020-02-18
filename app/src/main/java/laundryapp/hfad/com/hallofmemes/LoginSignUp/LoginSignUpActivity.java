package laundryapp.hfad.com.hallofmemes.LoginSignUp;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import laundryapp.hfad.com.hallofmemes.Utitlites.PagerAdapter;
import laundryapp.hfad.com.hallofmemes.R;

public class LoginSignUpActivity extends AppCompatActivity{
    private static final String TAG = "LoginSignUpActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sign_up);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        final ViewPager viewPager = findViewById(R.id.pager);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }
}
