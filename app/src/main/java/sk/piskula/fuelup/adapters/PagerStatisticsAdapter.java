package sk.piskula.fuelup.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.MainActivity;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartConsumptionFragment;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartConsumptionPerTimeFragment;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartConsumptionPreviewFragment;
import sk.piskula.fuelup.screens.statisticfragments.StatisticsChartFuelPricePreviewFragment;

/**
 * @author Martin Styk
 */
public class PagerStatisticsAdapter extends FragmentStatePagerAdapter {

    private Vehicle vehicle;

    public PagerStatisticsAdapter(FragmentManager fm, Vehicle vehicle) {
        super(fm);
        this.vehicle = vehicle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StatisticsChartConsumptionFragment.newInstance(vehicle);
            case 1:
                return StatisticsChartConsumptionPreviewFragment.newInstance(vehicle);
            case 2:
                return StatisticsChartFuelPricePreviewFragment.newInstance(vehicle);
            case 3:
                return StatisticsChartConsumptionPerTimeFragment.newInstance(vehicle);
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
                return MainActivity.getInstance().getString(R.string.statistics_fuel_consumption);
            case 1:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_consumption);
            case 2:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_price);
            case 3:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_consumption_per_month);
            default:
                return "Fragment" + position;
        }
    }
}