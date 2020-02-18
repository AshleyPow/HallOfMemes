package laundryapp.hfad.com.hallofmemes.Utitlites;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import laundryapp.hfad.com.hallofmemes.LoginSignUp.LogInFragment;
import laundryapp.hfad.com.hallofmemes.LoginSignUp.SignUpFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    Fragment fragment = new Fragment();

    private String title[] = {"Log In", "Sign Up"};

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return LogInFragment.getInstance(position);


            case 1:
                return SignUpFragment.getInstance(position);


            default:
                return fragment;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
