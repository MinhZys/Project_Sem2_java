package model;

// Lớp để chứa CategoryID và CategoryName cho JComboBox
public class CategoryItem {
    private int id;
    private String name;

    public CategoryItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; } // Hiển thị tên trong ComboBox

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategoryItem that = (CategoryItem) obj;
        return id == that.id; // So sánh bằng ID
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }
}