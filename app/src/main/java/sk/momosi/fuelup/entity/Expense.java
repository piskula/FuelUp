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
public class Expense implements Parcelable, Comparable<Expense> {

    private Long id;
    private Vehicle vehicle;
    private BigDecimal price;
    private Date date;
    private String info;

    //end of attributes

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
        return "Expense{"
                + "id=" + id
                + ", date=" + date
                + ", info=" + info
                + ", vehicleId=" + vehicleId
                + ", price=" + price
                + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeParcelable(this.vehicle, flags);
        dest.writeSerializable(this.price);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeString(this.info);
    }

    public Expense() {
    }

    private Expense(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.vehicle = in.readParcelable(Vehicle.class.getClassLoader());
        this.price = (BigDecimal) in.readSerializable();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.info = in.readString();
    }

    public static final Parcelable.Creator<Expense> CREATOR = new Parcelable.Creator<Expense>() {
        @Override
        public Expense createFromParcel(Parcel source) {
            return new Expense(source);
        }

        @Override
        public Expense[] newArray(int size) {
            return new Expense[size];
        }
    };

    @Override
    public int compareTo(@NonNull Expense other) {
        if (date != null && other.getDate() != null)
            return date.compareTo(other.getDate());
        return 0;
    }
}
