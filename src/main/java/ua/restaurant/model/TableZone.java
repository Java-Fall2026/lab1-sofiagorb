package ua.restaurant.model;

public enum TableZone {

    MAIN_HALL(1.0),
    TERRACE(1.1),
    VIP(1.5),
    BAR(0.9);

    private final double priceCoefficient;

    TableZone(double priceCoefficient) {
        this.priceCoefficient = priceCoefficient;
    }

    public double getPriceCoefficient() {
        return priceCoefficient;
    }
}