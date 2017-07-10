package sk.piskula.fuelup.entity.dto;

import java.math.BigDecimal;

/**
 * Created by Martin Styk on 10.07.2017.
 */

public class StatisticsDTO {

    private BigDecimal avgConsumption;

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
}
