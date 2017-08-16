package sk.piskula.fuelup.entity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Currency;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.CurrencyUtil;
import sk.piskula.fuelup.screens.MainActivity;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class Vehicle implements Parcelable {

    private Long id;
    private String name;
    private VehicleType type;
    private VolumeUnit volumeUnit;
    private String vehicleMaker;
    private Long startMileage;
    private String currency;
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

    public String getCurrencySymbol() {
        return CurrencyUtil.getCurrencySymbol(this.getCurrency());
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

    public DistanceUnit getDistanceUnit() {
        return this.getVolumeUnit() == VolumeUnit.LITRE ? DistanceUnit.km : DistanceUnit.mi;
    }

    public String getConsumptionUnit() {
        if (this.getDistanceUnit() == DistanceUnit.mi) {
            return MainActivity.getInstance().getString(R.string.units_mpg);
        } else {
            return MainActivity.getInstance().getString(R.string.units_litreper100km);
        }
    }
}
