package sk.momosi.fuelup.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.screens.MainActivity;
import sk.momosi.fuelup.screens.statisticfragments.StatisticsChartConsumptionPerTimeFragment;
import sk.momosi.fuelup.screens.statisticfragments.StatisticsChartConsumptionPreviewFragment;
import sk.momosi.fuelup.screens.statisticfragments.StatisticsChartCostsPerTimeFragment;
import sk.momosi.fuelup.screens.statisticfragments.StatisticsChartFuelPricePreviewFragment;

/**
 * @author Martin Styk
 */
public class PagerStatisticsAdapter extends FragmentStatePagerAdapter {

    private final Vehicle vehicle;

    public PagerStatisticsAdapter(FragmentManager fm, Vehicle vehicle) {
        super(fm);
        this.vehicle = vehicle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return StatisticsChartConsumptionPreviewFragment.newInstance(vehicle);
            case 1:
                return StatisticsChartFuelPricePreviewFragment.newInstance(vehicle);
            case 2:
                return StatisticsChartConsumptionPerTimeFragment.newInstance(vehicle);
            case 3:
                return StatisticsChartCostsPerTimeFragment.newInstance(vehicle);
            default:
                throw new RuntimeException("Wrong position");
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_consumption);
            case 1:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_price);
            case 2:
                return MainActivity.getInstance().getString(R.string.statistics_fuel_consumption_per_month);
            case 3:
                return MainActivity.getInstance().getString(R.string.statistics_costs_per_month);
            default:
                throw new RuntimeException("Wrong position");
        }
    }
}