package sk.piskula.fuelup.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Currency;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.CurrencyUtil;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
@DatabaseTable(tableName = "vehicles")
public class Vehicle implements Parcelable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, unique = true)
    private String name;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private VehicleType type;

    @DatabaseField(columnName = "distance_unit", unknownEnumName = "km", canBeNull = false)
    private DistanceUnit distanceUnit;

    @DatabaseField(columnName = "volume_unit", unknownEnumName = "LITRE", canBeNull = false)
    private VolumeUnit volumeUnit;

    @DatabaseField(columnName = "vehicle_maker")
    private String vehicleMaker;

    @DatabaseField(columnName = "start_mileage")
    private Long startMileage;

    @DatabaseField(canBeNull = false)
    private String currency;

    @DatabaseField(columnName = "path_to_picture", canBeNull = true)
    private String pathToPicture;

    //end of attributes

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public VolumeUnit getVolumeUnit() {
        return volumeUnit;
    }

    public void setVolumeUnit(VolumeUnit volumeUnit) {
        this.volumeUnit = volumeUnit;
    }

    public String getVehicleMaker() {
        return vehicleMaker;
    }

    public void setVehicleMaker(String vehicleMaker) {
        this.vehicleMaker = vehicleMaker;
    }

    public Long getStartMileage() {
        return startMileage;
    }

    public void setStartMileage(Long startMileage) {
        this.startMileage = startMileage;
    }

    public Currency getCurrency() {
        return Currency.getInstance(currency);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Vehicle))
            return false;

        Vehicle other = (Vehicle) obj;
        if (name == null && other.getName() == null) return true;
        if (name == null && "".equals(other.getName())) return true;
        if ("".equals(name) && other.getName() == null) return true;

        return name != null ? name.equals(other.getName()) : other.getName() == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vehicle{"
                + "id=" + id
                + ", name=" + name
                + ", type=" + type
                + ", distanceUnit=" + distanceUnit.name()
                + ", volumeUnit=" + volumeUnit.name()
                + ", vehicleMaker=" + vehicleMaker
                + ", startMileage=" + startMileage
                + ", currency=" + currency
                + ", pathToPicture=" + pathToPicture
                + "}";
    }

    public Bitmap getPicture() {
        if (this.pathToPicture == null || this.pathToPicture.isEmpty()) {
            return null;
        } else {
            return BitmapFactory.decodeFile(this.pathToPicture);
        }
    }

    public String getPathToPicture() {
        return pathToPicture;
    }

    public void setPathToPicture(String pathToPicture) {
        this.pathToPicture = pathToPicture;
    }

    public String getCurrencySymbol(Context context) {
        return CurrencyUtil.getCurrencySymbol(this.getCurrency(), context);
    }

    public Vehicle() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
        dest.writeParcelable(this.type, flags);
        dest.writeInt(this.distanceUnit == null ? -1 : this.distanceUnit.ordinal());
        dest.writeInt(this.volumeUnit == null ? -1 : this.volumeUnit.ordinal());
        dest.writeString(this.vehicleMaker);
        dest.writeValue(this.startMileage);
        dest.writeString(this.currency);
        dest.writeString(this.pathToPicture);
    }

    protected Vehicle(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.type = in.readParcelable(VehicleType.class.getClassLoader());
        int tmpUnit = in.readInt();
        this.distanceUnit = tmpUnit == -1 ? null : DistanceUnit.values()[tmpUnit];
        int tmpVolumeUnit = in.readInt();
        this.volumeUnit = tmpVolumeUnit == -1 ? null : VolumeUnit.values()[tmpVolumeUnit];
        this.vehicleMaker = in.readString();
        this.startMileage = (Long) in.readValue(Long.class.getClassLoader());
        this.currency = in.readString();
        this.pathToPicture = in.readString();
    }

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel source) {
            return new Vehicle(source);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };

    public String getConsumptionUnit(Context context) {
        if (this.getDistanceUnit() == DistanceUnit.mi) {
            return context.getString(R.string.units_mpg);
        } else {
            return context.getString(R.string.units_litreper100km);
        }
    }
}
