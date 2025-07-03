package model;

// Lớp đơn giản để chứa RoleID và RoleName cho JComboBox
public class RoleItem {
    private int id;
    private String name;

    public RoleItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Quan trọng: Override toString để JComboBox hiển thị tên Role
    @Override
    public String toString() {
        return name;
    }

    // Override equals và hashCode để JComboBox có thể tìm và chọn đúng item
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RoleItem roleItem = (RoleItem) obj;
        return id == roleItem.id; // So sánh dựa trên ID
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}