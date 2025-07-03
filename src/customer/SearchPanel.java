package customer;

import connect.DBConnection;
import model.CategoryItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SearchPanel extends JPanel {
    private JTextField searchField;
    private JComboBox<CategoryItem> categoryBox;
    private SearchListener searchListener;

    public interface SearchListener {
        void onSearch(String keyword, Integer categoryId);
    }

    public SearchPanel(SearchListener listener) {
        this.searchListener = listener;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm món");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(lblSearch);

        searchField = new JTextField(15);
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.addActionListener(e -> triggerSearch());
        add(searchField);

        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnSearch.addActionListener(e -> triggerSearch());
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(btnSearch);

        add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel lblCategory = new JLabel("Danh mục");
        lblCategory.setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(lblCategory);

        categoryBox = new JComboBox<>();
        categoryBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        categoryBox.addItem(new CategoryItem(0, "Tất cả"));
        loadCategories();
        categoryBox.addActionListener(e -> triggerSearch());
        add(categoryBox);
    }

    private void loadCategories() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT CategoryID, CategoryName FROM Categories");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("CategoryID");
                String name = rs.getString("CategoryName");
                categoryBox.addItem(new CategoryItem(id, name));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void triggerSearch() {
        String keyword = searchField.getText().trim();
        CategoryItem selected = (CategoryItem) categoryBox.getSelectedItem();
        Integer categoryId = (selected != null && selected.getId() != 0) ? selected.getId() : null;

        if (searchListener != null) {
            searchListener.onSearch(keyword, categoryId);
        }
    }
}
