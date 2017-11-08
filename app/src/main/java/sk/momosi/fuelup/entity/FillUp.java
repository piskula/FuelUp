package sk.momosi.fuelup.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class FillUp implements Parcelable, Comparable<FillUp> {

    private Long id;
    private Vehicle vehicle;
    private Long distanceFromLastFillUp;
    private BigDecimal fuelVolume;
    private BigDecimal fuelPricePerLitre;
    private BigDecimal fuelPriceTotal;
    private boolean isFullFillUp;
    private BigDecimal fuelConsumption;
    private Date date;
    private String info;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Long getDistanceFromLastFillUp() {
        return distanceFromLastFillUp;
    }

    public void setDistanceFromLastFillUp(Long distanceFromLastFillUp) {
        this.distanceFromLastFillUp = distanceFromLastFillUp;
    }

    public BigDecimal getFuelVolume() {
        return fuelVolume;
    }

    public void setFuelVolume(BigDecimal fuelVolume) {
        this.fuelVolume = fuelVolume;
    }

    public BigDecimal getFuelPricePerLitre() {
        return fuelPricePerLitre;
    }

    public void setFuelPricePerLitre(BigDecimal fuelPricePerLitre) {
        this.fuelPricePerLitre = fuelPricePerLitre;
    }

    public BigDecimal getFuelPriceTotal() {
        return fuelPriceTotal;
    }

    public void setFuelPriceTotal(BigDecimal fuelPriceTotal) {
        this.fuelPriceTotal = fuelPriceTotal;
    }

    public boolean isFullFillUp() {
        return isFullFillUp;
    }

    public void setFullFillUp(boolean fullFillUp) {
        isFullFillUp = fullFillUp;
    }

    public BigDecimal getFuelConsumption() {
        return fuelConsumption;
    }

    public void setFuelConsumption(BigDecimal fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        String vehicleId = vehicle != null ? ""+vehicle.getId() : "NULL";
        return "FillUp{" +
                "id=" + id +
                ", vehicleId=" + vehicleId +
                ", distanceFromLastFillUp=" + distanceFromLastFillUp +
                ", fuelVolume=" + fuelVolume +
                ", fuelPricePerLitre=" + fuelPricePerLitre +
                ", fuelPriceTotal=" + fuelPriceTotal +
                ", isFullFillUp=" + isFullFillUp +
                ", fuelConsumption=" + fuelConsumption +
                ", date=" + date +
                ", info='" + info + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull FillUp other) {
        if (date != null && other != null && other.getDate() != null) {
            int dateCompare = date.compareTo(other.getDate());
            if (dateCompare != 0) return dateCompare;
            else return id.compareTo(other.getId());
        }
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeParcelable(this.vehicle, flags);
        dest.writeValue(this.distanceFromLastFillUp);
        dest.writeSerializable(this.fuelVolume);
        dest.writeSerializable(this.fuelPricePerLitre);
        dest.writeSerializable(this.fuelPriceTotal);
        dest.writeByte(this.isFullFillUp ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.fuelConsumption);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeString(this.info);
    }

    public FillUp() {
    }

    private FillUp(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.vehicle = in.readParcelable(Vehicle.class.getClassLoader());
        this.distanceFromLastFillUp = (Long) in.readValue(Long.class.getClassLoader());
        this.fuelVolume = (BigDecimal) in.readSerializable();
        this.fuelPricePerLitre = (BigDecimal) in.readSerializable();
        this.fuelPriceTotal = (BigDecimal) in.readSerializable();
        this.isFullFillUp = in.readByte() != 0;
        this.fuelConsumption = (BigDecimal) in.readSerializable();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.info = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FillUp fillUp = (FillUp) o;

        if (isFullFillUp != fillUp.isFullFillUp) return false;
        if (id != null ? !id.equals(fillUp.id) : fillUp.id != null) return false;
        if (vehicle != null ? !vehicle.equals(fillUp.vehicle) : fillUp.vehicle != null)
            return false;
        if (distanceFromLastFillUp != null ? !distanceFromLastFillUp.equals(fillUp.distanceFromLastFillUp) : fillUp.distanceFromLastFillUp != null)
            return false;
        if (date != null ? !date.equals(fillUp.date) : fillUp.date != null) return false;
        return info != null ? info.equals(fillUp.info) : fillUp.info == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (vehicle != null ? vehicle.hashCode() : 0);
        result = 31 * result + (distanceFromLastFillUp != null ? distanceFromLastFillUp.hashCode() : 0);
        result = 31 * result + (isFullFillUp ? 1 : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (info != null ? info.hashCode() : 0);
        return result;
    }

    public static final Creator<FillUp> CREATOR = new Creator<FillUp>() {
        @Override
        public FillUp createFromParcel(Parcel source) {
            return new FillUp(source);
        }

        @Override
        public FillUp[] newArray(int size) {
            return new FillUp[size];
        }
    };
}
