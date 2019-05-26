package org.maddev.helpers.grand_exchange;

public class ItemPair {

    private String name;
    private int quantity;
    private int increasePriceTimes;
    private int originalPrice;
    private int priceMinimum;

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getIncreasePriceTimes() {
        return increasePriceTimes;
    }

    public void setOriginalPrice(int originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setPriceMinimum(int priceMinimum) {
        this.priceMinimum = priceMinimum;
    }

    public int getPriceMinimum() {
        return priceMinimum;
    }

    public ItemPair(String name, int quantity, int increasePriceTimes, int priceMin) {
        this.name = name;
        this.quantity = quantity;
        this.increasePriceTimes = increasePriceTimes;
        this.priceMinimum = priceMin;
    }

    public ItemPair(String name, int quantity, int increasePriceTimes) {
        this.name = name;
        this.quantity = quantity;
        this.increasePriceTimes = increasePriceTimes;
    }

    public ItemPair(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
