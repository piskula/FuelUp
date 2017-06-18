package sk.piskula.fuelup.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
@DatabaseTable(tableName = "vehicle_types")
public class VehicleType implements Serializable {

    private static final long serialVersionUID = -2158359200803252221L;

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, unique = true)
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
        if(this == object) return true;
        if(!(object instanceof VehicleType)) return false;

        final VehicleType other = (VehicleType) object;

        return name != null ? name.equals(other.getName()) : other.getName() == null;
    }

}
