package sk.piskula.fuelup.entity.enums;

/**
 * @author Ondrej Oravcok
 * @version 13.7.2017
 */
public enum VolumeUnit {
    LITRE("\u2113"), GALLON_US("Gal"), GALLON_UK("Gal");

    private String value;

    VolumeUnit(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
