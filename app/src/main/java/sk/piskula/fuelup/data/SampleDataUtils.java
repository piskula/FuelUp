package sk.piskula.fuelup.data;

import com.j256.ormlite.dao.Dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;

/**
 * @author Ondrej Oravcok
 * @version 19.6.2017
 */
public class SampleDataUtils {

    public static List<VehicleType> addVehicleTypes(Dao<VehicleType, Integer> vehicleTypeDao) throws SQLException {
        List<String> types = Arrays.asList("Sedan", "Hatchback", "Combi", "Van", "Motocycle", "Pickup", "Quad", "Sport", "SUV", "Coupe");
        List<VehicleType> result = new ArrayList<>();
        for (String type : types) {
            vehicleTypeDao.create(vehicleType(type));
            result.add(vehicleTypeDao.queryBuilder().where().eq("name", type).query().get(0));
        }
        return result;
    }

    private static VehicleType vehicleType(String name) {
        VehicleType vehicleType = new VehicleType();

        vehicleType.setName(name);

        return vehicleType;
    }

    public static List<Vehicle> addVehicles(Dao<Vehicle, Integer> vehicleDao, List<VehicleType> types) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();

        vehicleDao.create(vehicle("Sprinterik", "Mercedes", DistanceUnit.km, types.get(3), 227880L));
        vehicles.add(vehicleDao.queryBuilder().where().eq("name", "Sprinterik").query().get(0));
        vehicleDao.create(vehicle("Civic", "Honda", DistanceUnit.km, types.get(1), 227880L));
        vehicles.add(vehicleDao.queryBuilder().where().eq("name", "Civic").query().get(0));

        return vehicles;
    }

    private static Vehicle vehicle(String name, String maker,
                            DistanceUnit unit, VehicleType type, Long mileage) {
        Vehicle vehicle = new Vehicle();

        vehicle.setName(name);
        vehicle.setVehicleMaker(maker);
        vehicle.setUnit(unit);
        vehicle.setType(type);
        vehicle.setStartMileage(mileage);

        return vehicle;
    }

    public static void addFillUps(Dao<FillUp, Integer> fillUpDao, List<Vehicle> vehicles) throws SQLException {
        final int COUNT = 30;
        Random random = new Random();

        for (int i = 0; i < COUNT; i++) {
            Calendar cal = Calendar.getInstance();
            cal.set(2017, random.nextInt(11), random.nextInt(27) + 1);
            int dist = random.nextInt(5) + 6;
            fillUpDao.create(fillUp(vehicles.get(i % 2), BigDecimal.valueOf(random.nextDouble() * 8), cal.getTime(), Long.valueOf(dist * 40) , (dist + 3 * random.nextDouble())* 4 * 0.8 ));
        }

    }

    private static FillUp fillUp(Vehicle vehicle, BigDecimal perLitre, Date date, Long distanceFromLast, double amount) {
        FillUp fillUp = new FillUp();

        fillUp.setVehicle(vehicle);
        fillUp.setFuelPricePerLitre(perLitre);
        fillUp.setFullFillUp(true);
        fillUp.setDate(date);
        fillUp.setDistanceFromLastFillUp(distanceFromLast);
        fillUp.setFuelVolume(amount);
        fillUp.setInfo("");

        fillUp.setFuelPriceTotal(fillUp.getFuelPricePerLitre().multiply(BigDecimal.valueOf(fillUp.getFuelVolume())));

        return fillUp;
    }
}
