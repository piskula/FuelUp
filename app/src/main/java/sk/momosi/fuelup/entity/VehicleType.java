package sk.momosi.fuelup.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class VehicleType implements Parcelable {

    private Long id;
    private String name;

    //end of atributes

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof VehicleType)) return false;

        final VehicleType other = (VehicleType) object;

        return name != null ? name.equals(other.getName()) : other.getName() == null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.name);
    }

    public VehicleType() {
    }

    protected VehicleType(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
    }

    @Override
    public String toString() {
        return "VehicleType{"
                + "id=" + id
                + ", name=" + name
                + "}";
    }

    public static final Parcelable.Creator<VehicleType> CREATOR = new Parcelable.Creator<VehicleType>() {
        @Override
        public VehicleType createFromParcel(Parcel source) {
            return new VehicleType(source);
        }

        @Override
        public VehicleType[] newArray(int size) {
            return new VehicleType[size];
        }
    };
}
