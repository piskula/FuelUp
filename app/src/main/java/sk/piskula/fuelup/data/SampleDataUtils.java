package sk.piskula.fuelup.data;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Random;

import sk.piskula.fuelup.business.ExpenseService;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.VolumeUtil;

/**
 * @author Ondrej Oravcok
 * @version 19.6.2017
 */
public class SampleDataUtils {

    private static final int LAST_YEAR = 2017;
    private static final int NUMBER_OF_PREVIOUS_YEARS = 3;
    private static final double MI_TO_KM = 1.5d; //original value 1 mile = 1.609344 km
    private static final double LITRE_TO_GALLON = 0.264172d;

    private static final int MAX_FILLUPS = 60; //from there
    private static final int LOW_VEHICLE_FILLUPS = 10;

    private static final int MAX_EXPENSES = 60; //from there
    private static final int LOW_VEHICLE_EXPENSES = 10;

//    public static List<VehicleType> addVehicleTypes(Dao<VehicleType, Long> vehicleTypeDao) throws SQLException {
//        List<String> types = Arrays.asList("Sedan", "Hatchback", "Combi", "Van", "Motocycle", "Pickup", "Quad", "Sport", "SUV", "Coupe");
//        List<VehicleType> result = new ArrayList<>();
//        for (String type : types) {
//            vehicleTypeDao.create(vehicleType(type));
//            result.add(vehicleTypeDao.queryBuilder().where().eq("name", type).query().get(0));
//        }
//        return result;
//    }

    private static VehicleType vehicleType(String name) {
        VehicleType vehicleType = new VehicleType();

        vehicleType.setName(name);

        return vehicleType;
    }

//    public static List<Vehicle> addVehicles(Dao<Vehicle, Long> vehicleDao, List<VehicleType> types) throws SQLException {
//        List<Vehicle> vehicles = new ArrayList<>();
//
//        vehicleDao.create(vehicle("LongWay driver Pro", "Very massive asphalt destroyer (CZ)", VolumeUnit.LITRE, types.get(1), 16000L, Currency.getInstance("CZK")));
//        vehicles.add(vehicleDao.queryBuilder().where().eq("name", "LongWay driver Pro").query().get(0));
//        vehicleDao.create(vehicle("Amateur vehicle", "British sports car", VolumeUnit.GALLON_UK, types.get(7), 227880L, Currency.getInstance("GBP")));
//        vehicles.add(vehicleDao.queryBuilder().where().eq("name", "Amateur vehicle").query().get(0));
//
//        return vehicles;
//    }

    private static Vehicle vehicle(String name, String maker, VolumeUnit volumeUnit,
                                   VehicleType type, Long mileage, Currency currency) {
        Vehicle vehicle = new Vehicle();

        vehicle.setName(name);
        vehicle.setVehicleMaker(maker);
        vehicle.setVolumeUnit(volumeUnit);
        vehicle.setType(type);
        vehicle.setStartMileage(mileage);
        vehicle.setCurrency(currency);

        return vehicle;
    }

    /*public static void addFillUps(FillUpService fillUpService, List<Vehicle> vehicles) throws SQLException {

        Random random = new Random();

        List<FillUp> fillUps = new ArrayList<>(MAX_FILLUPS);

        int whichVehicle = 1;
        for (int i = 0; i < MAX_FILLUPS; i++) {
            Calendar cal = Calendar.getInstance();
            cal.set(random.nextInt(NUMBER_OF_PREVIOUS_YEARS + 1) + LAST_YEAR - NUMBER_OF_PREVIOUS_YEARS, random.nextInt(12), random.nextInt(27) + 1);
            int dist = random.nextInt(8) + 4;

            if (i > LOW_VEHICLE_FILLUPS) whichVehicle = 0;
            double currencyFactor = whichVehicle == 1 ? 0.16d : 4d;
            double gallonFactor = whichVehicle == 1 ? LITRE_TO_GALLON : 1d;
            fillUps.add(fillUp(vehicles.get(whichVehicle),
                    BigDecimal.valueOf((random.nextDouble() + 2) * 3.1 * currencyFactor),
                    cal.getTime(),
                    Long.valueOf(dist * 40) ,
                    (dist + 3 * random.nextDouble())* 4 * 0.7 * gallonFactor,
                    random.nextBoolean()));
        }
        Collections.sort(fillUps, new Comparator<FillUp>() {
            @Override
            public int compare(FillUp f1, FillUp f2) {
                return f1.getDate().compareTo(f2.getDate());
            }
        });
        for (FillUp fillUp : fillUps) {
            fillUpService.saveWithConsumptionCalculation(fillUp);
        }

    }*/

    private static FillUp fillUp(Vehicle vehicle, BigDecimal perLitre, Date date, Long distanceFromLast, double amount, boolean isFull) {
        FillUp fillUp = new FillUp();

        fillUp.setVehicle(vehicle);
        fillUp.setFuelPricePerLitre(perLitre);
        fillUp.setFullFillUp(isFull);
        fillUp.setDate(date);
        fillUp.setDistanceFromLastFillUp(distanceFromLast);
        fillUp.setFuelVolume(BigDecimal.valueOf(amount));
        fillUp.setInfo("");

        fillUp.setFuelPriceTotal(VolumeUtil.getTotalPriceFromPerLitre(BigDecimal.valueOf(amount), perLitre, vehicle.getVolumeUnit()));

        return fillUp;
    }

    public static void addExpenses(ExpenseService expenseService, List<Vehicle> vehicles) throws SQLException {
        List<String> issues = Arrays.asList("Wheels - winter", "Clutch - new", "Exhaust tuning",
                "Wheels - summer", "Air condition - service", "Oil - change", "Fuel filter - change",
                "Front bumper", "Rear window - repair", "Steering wheel", "Stereo - new", "Brembo breaks",
                "NOS - nitro tuning", "Yearly insurance", "Tires interchange", "Air freshener",
                "Rear window - new", "Kid Seat", "STK & ETK", "Additional insurance", "Radiator - new",
                "Registration fee", "Spark plugs - replace", "Castrol Magnatec lubricate");
        List<Double> prices = Arrays.asList(7d, 50d, 3.6d, 220d, 315d, 450d, 11700d, 7400d, 3200d, 14d, 15d, 18d, 45d, 52d, 110d, 95d, 130d, 8400d, 8500d, 7600d, 350d, 420d, 710d, 700d, 1200d, 115d);
        Random random = new Random();

        int whichVehicle = 1;
//        for (int i = 0; i < MAX_EXPENSES; i++) {
//            Calendar cal = Calendar.getInstance();
//            cal.set(random.nextInt(NUMBER_OF_PREVIOUS_YEARS + 1) + LAST_YEAR - NUMBER_OF_PREVIOUS_YEARS, random.nextInt(11), random.nextInt(27) + 1);
//
//            if (i > LOW_VEHICLE_EXPENSES) whichVehicle = 0;
//            int currencyFactor = whichVehicle == 1 ? 1 : 4;
//            expenseService.save(expense(vehicles.get(whichVehicle),
//                    issues.get(random.nextInt(issues.size())),
//                    BigDecimal.valueOf(prices.get(random.nextInt(prices.size())) * currencyFactor),
//                    cal.getTime()));
//        }
    }

    private static Expense expense(Vehicle vehicle, String info, BigDecimal price, Date date) {
        Expense expense = new Expense();

        expense.setVehicle(vehicle);
        expense.setInfo(info);
        expense.setPrice(price);
        expense.setDate(date);

        return expense;
    }
}
