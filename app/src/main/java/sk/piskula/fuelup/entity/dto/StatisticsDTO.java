package sk.piskula.fuelup.entity.dto;

import java.math.BigDecimal;

/**
 * Created by Martin Styk on 10.07.2017.
 */

public class StatisticsDTO {

    private BigDecimal avgConsumption;

    private BigDecimal totalPriceFillUps;

    private BigDecimal totalPriceExpenses;

    private BigDecimal totalPrice;

    private BigDecimal totalPricePerDistance;

    public BigDecimal getAvgConsumption() {
        return avgConsumption;
    }

    public void setAvgConsumption(BigDecimal avgConsumption) {
        this.avgConsumption = avgConsumption;
    }

    public BigDecimal getTotalPricePerDistance() {
        return totalPricePerDistance;
    }

    public void setTotalPricePerDistance(BigDecimal totalPricePerDistance) {
        this.totalPricePerDistance = totalPricePerDistance;
    }

    public BigDecimal getTotalPriceFillUps() {
        return totalPriceFillUps;
    }

    public void setTotalPriceFillUps(BigDecimal totalPriceFillUps) {
        this.totalPriceFillUps = totalPriceFillUps;
    }

    public BigDecimal getTotalPriceExpenses() {
        return totalPriceExpenses;
    }

    public void setTotalPriceExpenses(BigDecimal totalPriceExpenses) {
        this.totalPriceExpenses = totalPriceExpenses;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
