package sk.piskula.fuelup.entity.dto;

import java.math.BigDecimal;

/**
 * Created by Martin Styk on 10.07.2017.
 */
public class StatisticsDTO {

    // Consumption
    private BigDecimal avgConsumption;
    private BigDecimal avgConsumptionReversed;
    private BigDecimal fuelConsumptionBest;
    private BigDecimal fuelConsumptionWorst;

    // Totals
    private BigDecimal totalCosts;
    private BigDecimal totalCostsFuel;
    private BigDecimal totalCostsExpenses;
    private int totalNumberFillUps;
    private int totalNumberExpenses;
    private BigDecimal totalFuelVolume;
    private long totalDrivenDistance;

    // Costs per distance
    private BigDecimal totalCostsPerDistance;
    private BigDecimal fuelCostsPerDistance;
    private BigDecimal expenseCostsPerDistance;

    // Costs per time
    private BigDecimal averageTotalCostPerWeek;
    private BigDecimal averageFuelCostPerWeek;
    private BigDecimal averageExpenseCostPerWeek;
    private BigDecimal averageTotalCostPerMonth;
    private BigDecimal averageFuelCostPerMonth;
    private BigDecimal averageExpenseCostPerMonth;
    private BigDecimal averageTotalCostPerYear;
    private BigDecimal averageFuelCostPerYear;
    private BigDecimal averageExpenseCostPerYear;

    // Refuelling statistics
    private BigDecimal averageFuelVolumePerFillUp;
    private BigDecimal averageFuelPricePerFillUp;
    private BigDecimal averageNumberOfFillUpsPerWeek;
    private BigDecimal averageNumberOfFillUpsPerMonth;
    private BigDecimal averageNumberOfFillUpsPerYear;
    private int distanceBetweenFillUpsAverage;
    private int distanceBetweenFillUpsLowest;
    private int distanceBetweenFillUpsHighest;

    // Fuel unit price
    private BigDecimal fuelUnitPriceAverage;
    private BigDecimal fuelUnitPriceLowest;
    private BigDecimal fuelUnitPriceHighest;

    // Distance per time
    private long averageDistancePerDay;
    private long averageDistancePerWeek;
    private long averageDistancePerMonth;
    private long averageDistancePerYear;

    // Expense statistics
    private BigDecimal averageNumberOfExpensesPerWeek;
    private BigDecimal averageNumberOfExpensesPerMonth;
    private BigDecimal averageNumberOfExpensesPerYear;

    // FuelUp usage
    private long trackingDays;

    public BigDecimal getAvgConsumption() {
        return avgConsumption;
    }

    public void setAvgConsumption(BigDecimal avgConsumption) {
        this.avgConsumption = avgConsumption;
    }

    public BigDecimal getAvgConsumptionReversed() {
        return avgConsumptionReversed;
    }

    public void setAvgConsumptionReversed(BigDecimal avgConsumptionReversed) {
        this.avgConsumptionReversed = avgConsumptionReversed;
    }

    public BigDecimal getFuelConsumptionBest() {
        return fuelConsumptionBest;
    }

    public void setFuelConsumptionBest(BigDecimal fuelConsumptionBest) {
        this.fuelConsumptionBest = fuelConsumptionBest;
    }

    public BigDecimal getFuelConsumptionWorst() {
        return fuelConsumptionWorst;
    }

    public void setFuelConsumptionWorst(BigDecimal fuelConsumptionWorst) {
        this.fuelConsumptionWorst = fuelConsumptionWorst;
    }

    public BigDecimal getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(BigDecimal totalCosts) {
        this.totalCosts = totalCosts;
    }

    public BigDecimal getTotalCostsFuel() {
        return totalCostsFuel;
    }

    public void setTotalCostsFuel(BigDecimal totalCostsFuel) {
        this.totalCostsFuel = totalCostsFuel;
    }

    public BigDecimal getTotalCostsExpenses() {
        return totalCostsExpenses;
    }

    public void setTotalCostsExpenses(BigDecimal totalCostsExpenses) {
        this.totalCostsExpenses = totalCostsExpenses;
    }

    public int getTotalNumberFillUps() {
        return totalNumberFillUps;
    }

    public void setTotalNumberFillUps(int totalNumberFillUps) {
        this.totalNumberFillUps = totalNumberFillUps;
    }

    public int getTotalNumberExpenses() {
        return totalNumberExpenses;
    }

    public void setTotalNumberExpenses(int totalNumberExpenses) {
        this.totalNumberExpenses = totalNumberExpenses;
    }

    public BigDecimal getTotalFuelVolume() {
        return totalFuelVolume;
    }

    public void setTotalFuelVolume(BigDecimal totalFuelVolume) {
        this.totalFuelVolume = totalFuelVolume;
    }

    public long getTotalDrivenDistance() {
        return totalDrivenDistance;
    }

    public void setTotalDrivenDistance(long totalDrivenDistance) {
        this.totalDrivenDistance = totalDrivenDistance;
    }

    public BigDecimal getFuelCostsPerDistance() {
        return fuelCostsPerDistance;
    }

    public void setFuelCostsPerDistance(BigDecimal fuelCostsPerDistance) {
        this.fuelCostsPerDistance = fuelCostsPerDistance;
    }

    public BigDecimal getExpenseCostsPerDistance() {
        return expenseCostsPerDistance;
    }

    public void setExpenseCostsPerDistance(BigDecimal expenseCostsPerDistance) {
        this.expenseCostsPerDistance = expenseCostsPerDistance;
    }

    public BigDecimal getTotalCostsPerDistance() {
        return totalCostsPerDistance;
    }

    public void setTotalCostsPerDistance(BigDecimal totalCostsPerDistance) {
        this.totalCostsPerDistance = totalCostsPerDistance;
    }

    public BigDecimal getAverageTotalCostPerWeek() {
        return averageTotalCostPerWeek;
    }

    public void setAverageTotalCostPerWeek(BigDecimal averageTotalCostPerWeek) {
        this.averageTotalCostPerWeek = averageTotalCostPerWeek;
    }

    public BigDecimal getAverageFuelCostPerWeek() {
        return averageFuelCostPerWeek;
    }

    public void setAverageFuelCostPerWeek(BigDecimal averageFuelCostPerWeek) {
        this.averageFuelCostPerWeek = averageFuelCostPerWeek;
    }

    public BigDecimal getAverageExpenseCostPerWeek() {
        return averageExpenseCostPerWeek;
    }

    public void setAverageExpenseCostPerWeek(BigDecimal averageExpenseCostPerWeek) {
        this.averageExpenseCostPerWeek = averageExpenseCostPerWeek;
    }

    public BigDecimal getAverageTotalCostPerMonth() {
        return averageTotalCostPerMonth;
    }

    public void setAverageTotalCostPerMonth(BigDecimal averageTotalCostPerMonth) {
        this.averageTotalCostPerMonth = averageTotalCostPerMonth;
    }

    public BigDecimal getAverageFuelCostPerMonth() {
        return averageFuelCostPerMonth;
    }

    public void setAverageFuelCostPerMonth(BigDecimal averageFuelCostPerMonth) {
        this.averageFuelCostPerMonth = averageFuelCostPerMonth;
    }

    public BigDecimal getAverageExpenseCostPerMonth() {
        return averageExpenseCostPerMonth;
    }

    public void setAverageExpenseCostPerMonth(BigDecimal averageExpenseCostPerMonth) {
        this.averageExpenseCostPerMonth = averageExpenseCostPerMonth;
    }

    public BigDecimal getAverageTotalCostPerYear() {
        return averageTotalCostPerYear;
    }

    public void setAverageTotalCostPerYear(BigDecimal averageTotalCostPerYear) {
        this.averageTotalCostPerYear = averageTotalCostPerYear;
    }

    public BigDecimal getAverageFuelCostPerYear() {
        return averageFuelCostPerYear;
    }

    public void setAverageFuelCostPerYear(BigDecimal averageFuelCostPerYear) {
        this.averageFuelCostPerYear = averageFuelCostPerYear;
    }

    public BigDecimal getAverageExpenseCostPerYear() {
        return averageExpenseCostPerYear;
    }

    public void setAverageExpenseCostPerYear(BigDecimal averageExpenseCostPerYear) {
        this.averageExpenseCostPerYear = averageExpenseCostPerYear;
    }

    public BigDecimal getAverageFuelVolumePerFillUp() {
        return averageFuelVolumePerFillUp;
    }

    public void setAverageFuelVolumePerFillUp(BigDecimal averageFuelVolumePerFillUp) {
        this.averageFuelVolumePerFillUp = averageFuelVolumePerFillUp;
    }

    public BigDecimal getAverageFuelPricePerFillUp() {
        return averageFuelPricePerFillUp;
    }

    public void setAverageFuelPricePerFillUp(BigDecimal averageFuelPricePerFillUp) {
        this.averageFuelPricePerFillUp = averageFuelPricePerFillUp;
    }

    public BigDecimal getAverageNumberOfFillUpsPerWeek() {
        return averageNumberOfFillUpsPerWeek;
    }

    public void setAverageNumberOfFillUpsPerWeek(BigDecimal averageNumberOfFillUpsPerWeek) {
        this.averageNumberOfFillUpsPerWeek = averageNumberOfFillUpsPerWeek;
    }

    public BigDecimal getAverageNumberOfFillUpsPerMonth() {
        return averageNumberOfFillUpsPerMonth;
    }

    public void setAverageNumberOfFillUpsPerMonth(BigDecimal averageNumberOfFillUpsPerMonth) {
        this.averageNumberOfFillUpsPerMonth = averageNumberOfFillUpsPerMonth;
    }

    public BigDecimal getAverageNumberOfFillUpsPerYear() {
        return averageNumberOfFillUpsPerYear;
    }

    public void setAverageNumberOfFillUpsPerYear(BigDecimal averageNumberOfFillUpsPerYear) {
        this.averageNumberOfFillUpsPerYear = averageNumberOfFillUpsPerYear;
    }

    public int getDistanceBetweenFillUpsAverage() {
        return distanceBetweenFillUpsAverage;
    }

    public void setDistanceBetweenFillUpsAverage(int distanceBetweenFillUpsAverage) {
        this.distanceBetweenFillUpsAverage = distanceBetweenFillUpsAverage;
    }

    public int getDistanceBetweenFillUpsLowest() {
        return distanceBetweenFillUpsLowest;
    }

    public void setDistanceBetweenFillUpsLowest(int distanceBetweenFillUpsLowest) {
        this.distanceBetweenFillUpsLowest = distanceBetweenFillUpsLowest;
    }

    public int getDistanceBetweenFillUpsHighest() {
        return distanceBetweenFillUpsHighest;
    }

    public void setDistanceBetweenFillUpsHighest(int distanceBetweenFillUpsHighest) {
        this.distanceBetweenFillUpsHighest = distanceBetweenFillUpsHighest;
    }

    public BigDecimal getFuelUnitPriceAverage() {
        return fuelUnitPriceAverage;
    }

    public void setFuelUnitPriceAverage(BigDecimal fuelUnitPriceAverage) {
        this.fuelUnitPriceAverage = fuelUnitPriceAverage;
    }

    public BigDecimal getFuelUnitPriceLowest() {
        return fuelUnitPriceLowest;
    }

    public void setFuelUnitPriceLowest(BigDecimal fuelUnitPriceLowest) {
        this.fuelUnitPriceLowest = fuelUnitPriceLowest;
    }

    public BigDecimal getFuelUnitPriceHighest() {
        return fuelUnitPriceHighest;
    }

    public void setFuelUnitPriceHighest(BigDecimal fuelUnitPriceHighest) {
        this.fuelUnitPriceHighest = fuelUnitPriceHighest;
    }

    public long getAverageDistancePerDay() {
        return averageDistancePerDay;
    }

    public void setAverageDistancePerDay(long averageDistancePerDay) {
        this.averageDistancePerDay = averageDistancePerDay;
    }

    public long getAverageDistancePerWeek() {
        return averageDistancePerWeek;
    }

    public void setAverageDistancePerWeek(long averageDistancePerWeek) {
        this.averageDistancePerWeek = averageDistancePerWeek;
    }

    public long getAverageDistancePerMonth() {
        return averageDistancePerMonth;
    }

    public void setAverageDistancePerMonth(long averageDistancePerMonth) {
        this.averageDistancePerMonth = averageDistancePerMonth;
    }

    public long getAverageDistancePerYear() {
        return averageDistancePerYear;
    }

    public void setAverageDistancePerYear(long averageDistancePerYear) {
        this.averageDistancePerYear = averageDistancePerYear;
    }

    public BigDecimal getAverageNumberOfExpensesPerWeek() {
        return averageNumberOfExpensesPerWeek;
    }

    public void setAverageNumberOfExpensesPerWeek(BigDecimal averageNumberOfExpensesPerWeek) {
        this.averageNumberOfExpensesPerWeek = averageNumberOfExpensesPerWeek;
    }

    public BigDecimal getAverageNumberOfExpensesPerMonth() {
        return averageNumberOfExpensesPerMonth;
    }

    public void setAverageNumberOfExpensesPerMonth(BigDecimal averageNumberOfExpensesPerMonth) {
        this.averageNumberOfExpensesPerMonth = averageNumberOfExpensesPerMonth;
    }

    public BigDecimal getAverageNumberOfExpensesPerYear() {
        return averageNumberOfExpensesPerYear;
    }

    public void setAverageNumberOfExpensesPerYear(BigDecimal averageNumberOfExpensesPerYear) {
        this.averageNumberOfExpensesPerYear = averageNumberOfExpensesPerYear;
    }

    public long getTrackingDays() {
        return trackingDays;
    }

    public void setTrackingDays(long trackingDays) {
        this.trackingDays = trackingDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatisticsDTO dto = (StatisticsDTO) o;

        if (totalNumberFillUps != dto.totalNumberFillUps) return false;
        if (totalNumberExpenses != dto.totalNumberExpenses) return false;
        if (totalDrivenDistance != dto.totalDrivenDistance) return false;
        if (distanceBetweenFillUpsAverage != dto.distanceBetweenFillUpsAverage) return false;
        if (distanceBetweenFillUpsLowest != dto.distanceBetweenFillUpsLowest) return false;
        if (distanceBetweenFillUpsHighest != dto.distanceBetweenFillUpsHighest) return false;
        if (averageDistancePerDay != dto.averageDistancePerDay) return false;
        if (averageDistancePerWeek != dto.averageDistancePerWeek) return false;
        if (averageDistancePerMonth != dto.averageDistancePerMonth) return false;
        if (averageDistancePerYear != dto.averageDistancePerYear) return false;
        if (trackingDays != dto.trackingDays) return false;
        if (avgConsumption != null ? !avgConsumption.equals(dto.avgConsumption) : dto.avgConsumption != null)
            return false;
        if (avgConsumptionReversed != null ? !avgConsumptionReversed.equals(dto.avgConsumptionReversed) : dto.avgConsumptionReversed != null)
            return false;
        if (fuelConsumptionBest != null ? !fuelConsumptionBest.equals(dto.fuelConsumptionBest) : dto.fuelConsumptionBest != null)
            return false;
        if (fuelConsumptionWorst != null ? !fuelConsumptionWorst.equals(dto.fuelConsumptionWorst) : dto.fuelConsumptionWorst != null)
            return false;
        if (totalCosts != null ? !totalCosts.equals(dto.totalCosts) : dto.totalCosts != null)
            return false;
        if (totalCostsFuel != null ? !totalCostsFuel.equals(dto.totalCostsFuel) : dto.totalCostsFuel != null)
            return false;
        if (totalCostsExpenses != null ? !totalCostsExpenses.equals(dto.totalCostsExpenses) : dto.totalCostsExpenses != null)
            return false;
        if (totalFuelVolume != null ? !totalFuelVolume.equals(dto.totalFuelVolume) : dto.totalFuelVolume != null)
            return false;
        if (fuelCostsPerDistance != null ? !fuelCostsPerDistance.equals(dto.fuelCostsPerDistance) : dto.fuelCostsPerDistance != null)
            return false;
        if (expenseCostsPerDistance != null ? !expenseCostsPerDistance.equals(dto.expenseCostsPerDistance) : dto.expenseCostsPerDistance != null)
            return false;
        if (totalCostsPerDistance != null ? !totalCostsPerDistance.equals(dto.totalCostsPerDistance) : dto.totalCostsPerDistance != null)
            return false;
        if (averageTotalCostPerWeek != null ? !averageTotalCostPerWeek.equals(dto.averageTotalCostPerWeek) : dto.averageTotalCostPerWeek != null)
            return false;
        if (averageFuelCostPerWeek != null ? !averageFuelCostPerWeek.equals(dto.averageFuelCostPerWeek) : dto.averageFuelCostPerWeek != null)
            return false;
        if (averageExpenseCostPerWeek != null ? !averageExpenseCostPerWeek.equals(dto.averageExpenseCostPerWeek) : dto.averageExpenseCostPerWeek != null)
            return false;
        if (averageTotalCostPerMonth != null ? !averageTotalCostPerMonth.equals(dto.averageTotalCostPerMonth) : dto.averageTotalCostPerMonth != null)
            return false;
        if (averageFuelCostPerMonth != null ? !averageFuelCostPerMonth.equals(dto.averageFuelCostPerMonth) : dto.averageFuelCostPerMonth != null)
            return false;
        if (averageExpenseCostPerMonth != null ? !averageExpenseCostPerMonth.equals(dto.averageExpenseCostPerMonth) : dto.averageExpenseCostPerMonth != null)
            return false;
        if (averageTotalCostPerYear != null ? !averageTotalCostPerYear.equals(dto.averageTotalCostPerYear) : dto.averageTotalCostPerYear != null)
            return false;
        if (averageFuelCostPerYear != null ? !averageFuelCostPerYear.equals(dto.averageFuelCostPerYear) : dto.averageFuelCostPerYear != null)
            return false;
        if (averageExpenseCostPerYear != null ? !averageExpenseCostPerYear.equals(dto.averageExpenseCostPerYear) : dto.averageExpenseCostPerYear != null)
            return false;
        if (averageFuelVolumePerFillUp != null ? !averageFuelVolumePerFillUp.equals(dto.averageFuelVolumePerFillUp) : dto.averageFuelVolumePerFillUp != null)
            return false;
        if (averageFuelPricePerFillUp != null ? !averageFuelPricePerFillUp.equals(dto.averageFuelPricePerFillUp) : dto.averageFuelPricePerFillUp != null)
            return false;
        if (averageNumberOfFillUpsPerWeek != null ? !averageNumberOfFillUpsPerWeek.equals(dto.averageNumberOfFillUpsPerWeek) : dto.averageNumberOfFillUpsPerWeek != null)
            return false;
        if (averageNumberOfFillUpsPerMonth != null ? !averageNumberOfFillUpsPerMonth.equals(dto.averageNumberOfFillUpsPerMonth) : dto.averageNumberOfFillUpsPerMonth != null)
            return false;
        if (averageNumberOfFillUpsPerYear != null ? !averageNumberOfFillUpsPerYear.equals(dto.averageNumberOfFillUpsPerYear) : dto.averageNumberOfFillUpsPerYear != null)
            return false;
        if (fuelUnitPriceAverage != null ? !fuelUnitPriceAverage.equals(dto.fuelUnitPriceAverage) : dto.fuelUnitPriceAverage != null)
            return false;
        if (fuelUnitPriceLowest != null ? !fuelUnitPriceLowest.equals(dto.fuelUnitPriceLowest) : dto.fuelUnitPriceLowest != null)
            return false;
        if (fuelUnitPriceHighest != null ? !fuelUnitPriceHighest.equals(dto.fuelUnitPriceHighest) : dto.fuelUnitPriceHighest != null)
            return false;
        if (averageNumberOfExpensesPerWeek != null ? !averageNumberOfExpensesPerWeek.equals(dto.averageNumberOfExpensesPerWeek) : dto.averageNumberOfExpensesPerWeek != null)
            return false;
        if (averageNumberOfExpensesPerMonth != null ? !averageNumberOfExpensesPerMonth.equals(dto.averageNumberOfExpensesPerMonth) : dto.averageNumberOfExpensesPerMonth != null)
            return false;
        return averageNumberOfExpensesPerYear != null ? averageNumberOfExpensesPerYear.equals(dto.averageNumberOfExpensesPerYear) : dto.averageNumberOfExpensesPerYear == null;

    }

    @Override
    public int hashCode() {
        int result = avgConsumption != null ? avgConsumption.hashCode() : 0;
        result = 31 * result + (avgConsumptionReversed != null ? avgConsumptionReversed.hashCode() : 0);
        result = 31 * result + (fuelConsumptionBest != null ? fuelConsumptionBest.hashCode() : 0);
        result = 31 * result + (fuelConsumptionWorst != null ? fuelConsumptionWorst.hashCode() : 0);
        result = 31 * result + (totalCosts != null ? totalCosts.hashCode() : 0);
        result = 31 * result + (totalCostsFuel != null ? totalCostsFuel.hashCode() : 0);
        result = 31 * result + (totalCostsExpenses != null ? totalCostsExpenses.hashCode() : 0);
        result = 31 * result + totalNumberFillUps;
        result = 31 * result + totalNumberExpenses;
        result = 31 * result + (totalFuelVolume != null ? totalFuelVolume.hashCode() : 0);
        result = 31 * result + (int) (totalDrivenDistance ^ (totalDrivenDistance >>> 32));
        result = 31 * result + (fuelCostsPerDistance != null ? fuelCostsPerDistance.hashCode() : 0);
        result = 31 * result + (expenseCostsPerDistance != null ? expenseCostsPerDistance.hashCode() : 0);
        result = 31 * result + (totalCostsPerDistance != null ? totalCostsPerDistance.hashCode() : 0);
        result = 31 * result + (averageTotalCostPerWeek != null ? averageTotalCostPerWeek.hashCode() : 0);
        result = 31 * result + (averageFuelCostPerWeek != null ? averageFuelCostPerWeek.hashCode() : 0);
        result = 31 * result + (averageExpenseCostPerWeek != null ? averageExpenseCostPerWeek.hashCode() : 0);
        result = 31 * result + (averageTotalCostPerMonth != null ? averageTotalCostPerMonth.hashCode() : 0);
        result = 31 * result + (averageFuelCostPerMonth != null ? averageFuelCostPerMonth.hashCode() : 0);
        result = 31 * result + (averageExpenseCostPerMonth != null ? averageExpenseCostPerMonth.hashCode() : 0);
        result = 31 * result + (averageTotalCostPerYear != null ? averageTotalCostPerYear.hashCode() : 0);
        result = 31 * result + (averageFuelCostPerYear != null ? averageFuelCostPerYear.hashCode() : 0);
        result = 31 * result + (averageExpenseCostPerYear != null ? averageExpenseCostPerYear.hashCode() : 0);
        result = 31 * result + (averageFuelVolumePerFillUp != null ? averageFuelVolumePerFillUp.hashCode() : 0);
        result = 31 * result + (averageFuelPricePerFillUp != null ? averageFuelPricePerFillUp.hashCode() : 0);
        result = 31 * result + (averageNumberOfFillUpsPerWeek != null ? averageNumberOfFillUpsPerWeek.hashCode() : 0);
        result = 31 * result + (averageNumberOfFillUpsPerMonth != null ? averageNumberOfFillUpsPerMonth.hashCode() : 0);
        result = 31 * result + (averageNumberOfFillUpsPerYear != null ? averageNumberOfFillUpsPerYear.hashCode() : 0);
        result = 31 * result + distanceBetweenFillUpsAverage;
        result = 31 * result + distanceBetweenFillUpsLowest;
        result = 31 * result + distanceBetweenFillUpsHighest;
        result = 31 * result + (fuelUnitPriceAverage != null ? fuelUnitPriceAverage.hashCode() : 0);
        result = 31 * result + (fuelUnitPriceLowest != null ? fuelUnitPriceLowest.hashCode() : 0);
        result = 31 * result + (fuelUnitPriceHighest != null ? fuelUnitPriceHighest.hashCode() : 0);
        result = 31 * result + (int) (averageDistancePerDay ^ (averageDistancePerDay >>> 32));
        result = 31 * result + (int) (averageDistancePerWeek ^ (averageDistancePerWeek >>> 32));
        result = 31 * result + (int) (averageDistancePerMonth ^ (averageDistancePerMonth >>> 32));
        result = 31 * result + (int) (averageDistancePerYear ^ (averageDistancePerYear >>> 32));
        result = 31 * result + (averageNumberOfExpensesPerWeek != null ? averageNumberOfExpensesPerWeek.hashCode() : 0);
        result = 31 * result + (averageNumberOfExpensesPerMonth != null ? averageNumberOfExpensesPerMonth.hashCode() : 0);
        result = 31 * result + (averageNumberOfExpensesPerYear != null ? averageNumberOfExpensesPerYear.hashCode() : 0);
        result = 31 * result + (int) (trackingDays ^ (trackingDays >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "StatisticsDTO{" +
                "avgConsumption=" + avgConsumption +
                ", avgConsumptionReversed=" + avgConsumptionReversed +
                ", fuelConsumptionBest=" + fuelConsumptionBest +
                ", fuelConsumptionWorst=" + fuelConsumptionWorst +
                ", totalCosts=" + totalCosts +
                ", totalCostsFuel=" + totalCostsFuel +
                ", totalCostsExpenses=" + totalCostsExpenses +
                ", totalNumberFillUps=" + totalNumberFillUps +
                ", totalNumberExpenses=" + totalNumberExpenses +
                ", totalFuelVolume=" + totalFuelVolume +
                ", totalDrivenDistance=" + totalDrivenDistance +
                ", fuelCostsPerDistance=" + fuelCostsPerDistance +
                ", expenseCostsPerDistance=" + expenseCostsPerDistance +
                ", totalCostsPerDistance=" + totalCostsPerDistance +
                ", averageTotalCostPerWeek=" + averageTotalCostPerWeek +
                ", averageFuelCostPerWeek=" + averageFuelCostPerWeek +
                ", averageExpenseCostPerWeek=" + averageExpenseCostPerWeek +
                ", averageTotalCostPerMonth=" + averageTotalCostPerMonth +
                ", averageFuelCostPerMonth=" + averageFuelCostPerMonth +
                ", averageExpenseCostPerMonth=" + averageExpenseCostPerMonth +
                ", averageTotalCostPerYear=" + averageTotalCostPerYear +
                ", averageFuelCostPerYear=" + averageFuelCostPerYear +
                ", averageExpenseCostPerYear=" + averageExpenseCostPerYear +
                ", averageFuelVolumePerFillUp=" + averageFuelVolumePerFillUp +
                ", averageFuelPricePerFillUp=" + averageFuelPricePerFillUp +
                ", averageNumberOfFillUpsPerWeek=" + averageNumberOfFillUpsPerWeek +
                ", averageNumberOfFillUpsPerMonth=" + averageNumberOfFillUpsPerMonth +
                ", averageNumberOfFillUpsPerYear=" + averageNumberOfFillUpsPerYear +
                ", distanceBetweenFillUpsAverage=" + distanceBetweenFillUpsAverage +
                ", distanceBetweenFillUpsLowest=" + distanceBetweenFillUpsLowest +
                ", distanceBetweenFillUpsHighest=" + distanceBetweenFillUpsHighest +
                ", fuelUnitPriceAverage=" + fuelUnitPriceAverage +
                ", fuelUnitPriceLowest=" + fuelUnitPriceLowest +
                ", fuelUnitPriceHighest=" + fuelUnitPriceHighest +
                ", averageDistancePerDay=" + averageDistancePerDay +
                ", averageDistancePerWeek=" + averageDistancePerWeek +
                ", averageDistancePerMonth=" + averageDistancePerMonth +
                ", averageDistancePerYear=" + averageDistancePerYear +
                ", averageNumberOfExpensesPerWeek=" + averageNumberOfExpensesPerWeek +
                ", averageNumberOfExpensesPerMonth=" + averageNumberOfExpensesPerMonth +
                ", averageNumberOfExpensesPerYear=" + averageNumberOfExpensesPerYear +
                ", trackingDays=" + trackingDays +
                '}';
    }
}
