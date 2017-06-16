package sk.piskula.fuelup.entity;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class Vehicle implements Serializable {

    @DatabaseField(generatedId = true, columnName = "id")
    private Long id;

    @DatabaseField(canBeNull = false, columnName = "name")
    private String name;

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

}
