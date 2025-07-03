package model;

public class FoodItem {
    private int id;
    private String name;
    private double price; // Hoặc dùng BigDecimal

    public FoodItem(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        // Hiển thị tên và giá trong ComboBox
        return String.format("%s - %,.0f VNĐ", name, price);
    }

    // equals và hashCode dựa trên ID để ComboBox hoạt động chính xác
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return id == foodItem.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}