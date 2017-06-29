package sk.piskula.fuelup.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Styk
 */
public class PagerStatisticsAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;

    public PagerStatisticsAdapter(FragmentManager fm, List<? extends Fragment> fragments) {
        super(fm);
        this.fragments = new ArrayList<>(fragments);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "FRAGMENT " + (position + 1);
    }
}