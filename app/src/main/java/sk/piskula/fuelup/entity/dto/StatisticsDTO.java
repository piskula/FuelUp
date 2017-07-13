package sk.piskula.fuelup.entity.dto;

import java.math.BigDecimal;

/**
 * Created by Martin Styk on 10.07.2017.
 */

public class StatisticsDTO {

    private BigDecimal avgConsumption;
    private BigDecimal avgConsumptionReversed;

    private BigDecimal totalPriceFillUps;

    private BigDecimal totalPriceExpenses;

    private BigDecimal totalPrice;

    private BigDecimal totalFuelVolume;

    private BigDecimal fillUpPricePerDistance;

    private BigDecimal expensePricePerDistance;

    private BigDecimal totalPricePerDistance;


    private long totalDrivenDistance;

    private int totalNumberExpenses;

    private int totalNumberFillUps;

    private BigDecimal avgFuelPricePerLitre;

    private BigDecimal avgFuelVolumePerFillUp;

    private BigDecimal avgFuelPricePerFillUp;

    private long trackingDays;

    private BigDecimal averageTotalCostPerWeek;
    private BigDecimal averageFuelCostPerWeek;
    private BigDecimal averageExpenseCostPerWeek;
    private BigDecimal averageTotalCostPerMonth;
    private BigDecimal averageFuelCostPerMonth;
    private BigDecimal averageExpenseCostPerMonth;
    private BigDecimal averageTotalCostPerYear;
    private BigDecimal averageFuelCostPerYear;
    private BigDecimal averageExpenseCostPerYear;

    private long averageDistancePerDay;
    private long averageDistancePerWeek;
    private long averageDistancePerMonth;
    private long averageDistancePerYear;

    private BigDecimal averageNumberOfFillUpsPerWeek;
    private BigDecimal averageNumberOfFillUpsPerMonth;
    private BigDecimal averageNumberOfFillUpsPerYear;

    private BigDecimal averageNumberOfExpensesPerWeek;
    private BigDecimal averageNumberOfExpensesPerMonth;
    private BigDecimal averageNumberOfExpensesPerYear;

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

    public BigDecimal getTotalFuelVolume() {
        return totalFuelVolume;
    }

    public void setTotalFuelVolume(BigDecimal totalFuelVolume) {
        this.totalFuelVolume = totalFuelVolume;
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

    public long getTotalDrivenDistance() {
        return totalDrivenDistance;
    }

    public void setTotalDrivenDistance(long totalDrivenDistance) {
        this.totalDrivenDistance = totalDrivenDistance;
    }

    public int getTotalNumberExpenses() {
        return totalNumberExpenses;
    }

    public void setTotalNumberExpenses(int totalNumberExpenses) {
        this.totalNumberExpenses = totalNumberExpenses;
    }

    public int getTotalNumberFillUps() {
        return totalNumberFillUps;
    }

    public void setTotalNumberFillUps(int totalNumberFillUps) {
        this.totalNumberFillUps = totalNumberFillUps;
    }

    public BigDecimal getFillUpPricePerDistance() {
        return fillUpPricePerDistance;
    }

    public void setFillUpPricePerDistance(BigDecimal fillUpPricePerDistance) {
        this.fillUpPricePerDistance = fillUpPricePerDistance;
    }

    public BigDecimal getExpensePricePerDistance() {
        return expensePricePerDistance;
    }

    public void setExpensePricePerDistance(BigDecimal expensePricePerDistance) {
        this.expensePricePerDistance = expensePricePerDistance;
    }

    public BigDecimal getAvgFuelPricePerLitre() {
        return avgFuelPricePerLitre;
    }

    public void setAvgFuelPricePerLitre(BigDecimal avgFuelPricePerLitre) {
        this.avgFuelPricePerLitre = avgFuelPricePerLitre;
    }

    public BigDecimal getAvgFuelVolumePerFillUp() {
        return avgFuelVolumePerFillUp;
    }

    public void setAvgFuelVolumePerFillUp(BigDecimal avgFuelVolumePerFillUp) {
        this.avgFuelVolumePerFillUp = avgFuelVolumePerFillUp;
    }

    public BigDecimal getAvgFuelPricePerFillUp() {
        return avgFuelPricePerFillUp;
    }

    public void setAvgFuelPricePerFillUp(BigDecimal avgFuelPricePerFillUp) {
        this.avgFuelPricePerFillUp = avgFuelPricePerFillUp;
    }

    public long getTrackingDays() {
        return trackingDays;
    }

    public void setTrackingDays(long trackingDays) {
        this.trackingDays = trackingDays;
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

    public void setAverageNumberOfExpensesPerYear(BigDecimal averageNumberOfexpensesPerYear) {
        this.averageNumberOfExpensesPerYear = averageNumberOfexpensesPerYear;
    }
}
