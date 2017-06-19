package sk.piskula.fuelup.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
@DatabaseTable(tableName = "expenses")
public class Expense implements Serializable {

    private static final double VersionUID = 064565131l;

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Vehicle vehicle;

    @DatabaseField
    private BigDecimal price;

    @DatabaseField(canBeNull = false)
    private Date date;

    @DatabaseField(canBeNull = false)
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
    public String toString(){
        return "Expense{"
                + "id=" + id
                + ", date=" + date
                + ", info=" + info
                + ", vehicle=" + vehicle
                + ", price=" + price
                +"}";
    }
}
