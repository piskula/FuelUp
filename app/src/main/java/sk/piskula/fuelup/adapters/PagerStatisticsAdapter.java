package sk.piskula.fuelup.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartConsumptionFragment;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartConsumptionPreviewFragment;

/**
 * @author Martin Styk
 */
public class PagerStatisticsAdapter extends FragmentStatePagerAdapter {

    private Vehicle vehicle;
    private Context context;

    public PagerStatisticsAdapter(FragmentManager fm, Vehicle vehicle, Context context) {
        super(fm);
        this.vehicle = vehicle;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StatisticsChartConsumptionFragment.newInstance(vehicle);
            case 1:
                return StatisticsChartConsumptionPreviewFragment.newInstance(vehicle);
            default:
                return StatisticsChartConsumptionFragment.newInstance(vehicle);
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.statistics_fuel_consumption);
            case 1:
                return context.getString(R.string.statistics_fuel_consumption);
            default:
                return "Fragment" + position;
        }
    }
}