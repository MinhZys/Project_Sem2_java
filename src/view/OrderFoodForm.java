package view;

import customer.CustomerHome;
import connect.DBConnection;
import model.FoodItem; // Đảm bảo bạn đã tạo lớp này

import javax.swing.*;
// Bỏ import swing.text.DocumentFilter và AbstractDocument nếu không dùng bộ lọc số lượng
// import javax.swing.text.AttributeSet;
// import javax.swing.text.BadLocationException;
// import javax.swing.text.DocumentFilter;
// import javax.swing.text.AbstractDocument;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import java.math.BigDecimal; // Dùng BigDecimal để lấy giá chính xác

public class OrderFoodForm extends JFrame {
    // --- Thành phần UI ---
    private JComboBox<FoodItem> comboFood;
    private JTextField txtQuantity;
    private JButton btnOrder, btnBack;
    private JLabel lblSelectedPriceValue; // Hiển thị giá đơn vị
    private JLabel lblTotalPriceValue;    // Hiển thị tổng tiền

    // --- Dữ liệu ---
    private String customerUsername;
    private int initialFoodId = -1; // ID món ăn chọn sẵn, -1 nếu không có

    // --- Constructor ---
    // Constructor chính (khi vào từ menu hoặc nơi khác không có món chọn sẵn)
    public OrderFoodForm(String customerUsername) {
        this(customerUsername, -1); // Gọi constructor đầy đủ với initialFoodId = -1
    }

    // Constructor đầy đủ (khi vào từ nút "Đặt món" trên CustomerHome)
    public OrderFoodForm(String customerUsername, int initialFoodId) {
        this.customerUsername = customerUsername;
        this.initialFoodId = initialFoodId;

        // Áp dụng giao diện Nimbus
        applyNimbusLookAndFeel();

        setTitle("Đặt món ăn - Khách hàng: " + customerUsername);
        setSize(550, 380); // Điều chỉnh kích thước nếu cần
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Chỉ đóng cửa sổ này
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Padding

        // --- Tiêu đề ---
        JLabel lblTitle = new JLabel("CHỌN VÀ ĐẶT MÓN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 102, 204)); // Màu xanh dương
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- Panel Trung tâm ---
        add(createOrderPanel(), BorderLayout.CENTER);

        // --- Panel Nút phía dưới ---
        add(createButtonPanel(), BorderLayout.SOUTH);

        // --- Tải dữ liệu và cập nhật trạng thái ban đầu ---
        loadFoodItems(); // Tải món ăn (sẽ chọn món nếu initialFoodId hợp lệ)
        // updateTotalPrice(); // Cập nhật tổng tiền ban đầu (đã gọi trong loadFoodItems thông qua updateSelectedPrice)
    }

    // Áp dụng Nimbus Look and Feel
    private void applyNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) { /* Xử lý lỗi nếu cần */ }
    }

    // Tạo panel trung tâm chứa các thành phần đặt hàng
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Chi tiết đặt hàng"),
                BorderFactory.createEmptyBorder(10, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font priceFont = new Font("Segoe UI", Font.BOLD, 14);

        // Hàng 0: Chọn món
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2; // Độ rộng tương đối của nhãn
        JLabel lblFood = new JLabel("Chọn món:"); lblFood.setFont(labelFont); panel.add(lblFood, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8; // Độ rộng tương đối của ComboBox
        comboFood = new JComboBox<>(); comboFood.setFont(fieldFont);
        // Sự kiện khi chọn món khác -> cập nhật giá và tổng tiền
        comboFood.addActionListener(e -> {
            updateSelectedPrice();
            updateTotalPrice();
        });
        panel.add(comboFood, gbc);

        // Hàng 1: Đơn giá
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        JLabel lblUnitPrice = new JLabel("Đơn giá:"); lblUnitPrice.setFont(labelFont); panel.add(lblUnitPrice, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        lblSelectedPriceValue = new JLabel("0 VNĐ"); lblSelectedPriceValue.setFont(priceFont); lblSelectedPriceValue.setForeground(Color.BLUE); panel.add(lblSelectedPriceValue, gbc);

        // Hàng 2: Số lượng
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        JLabel lblQuantity = new JLabel("Số lượng:"); lblQuantity.setFont(labelFont); panel.add(lblQuantity, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        txtQuantity = new JTextField("1", 5); // Mặc định số lượng là 1, độ rộng gợi ý 5
        txtQuantity.setFont(fieldFont);
        // Sự kiện khi nhập số lượng -> cập nhật tổng tiền
        txtQuantity.addKeyListener(new KeyAdapter() {
             @Override public void keyReleased(KeyEvent e) { updateTotalPrice(); }
        });
         // Tùy chọn: Bộ lọc chỉ cho nhập số
         // ((AbstractDocument) txtQuantity.getDocument()).setDocumentFilter(new NumberOnlyFilter());
        panel.add(txtQuantity, gbc);

        // Hàng 3: Thành tiền
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.2;
        JLabel lblTotalPrice = new JLabel("Thành tiền:"); lblTotalPrice.setFont(labelFont); panel.add(lblTotalPrice, gbc);
        gbc.gridx = 1; gbc.weightx = 0.8;
        lblTotalPriceValue = new JLabel("0 VNĐ"); lblTotalPriceValue.setFont(priceFont); lblTotalPriceValue.setForeground(Color.RED); panel.add(lblTotalPriceValue, gbc);

        return panel;
    }

     // Tạo panel chứa các nút phía dưới
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Căn phải
        btnOrder = new JButton("Đặt hàng");
        btnBack = new JButton("Quay lại");

        Dimension btnSize = new Dimension(120, 35); Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnOrder.setPreferredSize(btnSize); btnOrder.setFont(btnFont); btnOrder.setBackground(new Color(40, 167, 69)); btnOrder.setForeground(Color.WHITE); btnOrder.setFocusPainted(false);
        btnBack.setPreferredSize(btnSize); btnBack.setFont(btnFont); btnBack.setFocusPainted(false);

        btnOrder.addActionListener(e -> orderFood());
        btnBack.addActionListener(e -> {
            // Nhớ truyền username khi quay lại CustomerHome
            new CustomerHome(this.customerUsername).setVisible(true);
            dispose();
        });

        panel.add(btnOrder);
        panel.add(btnBack);
        return panel;
    }

    // Tải danh sách món ăn vào ComboBox
    private void loadFoodItems() {
        Vector<FoodItem> foodVec = new Vector<>();
        Connection conn = null; PreparedStatement pst = null; ResultSet rs = null;
        String sql = "SELECT FoodID, FoodName, Price FROM Foods WHERE Status = N'Published' ORDER BY FoodName";
        FoodItem itemToSelect = null;
        try {
            conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); rs = pst.executeQuery();
            // Thêm mục mặc định "-- Chọn món ăn --"
            foodVec.add(new FoodItem(0, "-- Chọn món ăn --", 0));
            while (rs.next()) {
                // Dùng BigDecimal để lấy giá từ CSDL cho chính xác
                BigDecimal priceDb = rs.getBigDecimal("Price");
                FoodItem currentItem = new FoodItem(
                    rs.getInt("FoodID"),
                    rs.getString("FoodName"),
                    priceDb != null ? priceDb.doubleValue() : 0.0 // Chuyển sang double cho FoodItem
                );
                foodVec.add(currentItem);
                // Tìm item cần chọn sẵn
                if (currentItem.getId() == this.initialFoodId) {
                    itemToSelect = currentItem;
                }
            }
            DefaultComboBoxModel<FoodItem> model = new DefaultComboBoxModel<>(foodVec);
            comboFood.setModel(model);

            // Chọn món ăn ban đầu nếu có ID hợp lệ được truyền vào
            if (itemToSelect != null) {
                comboFood.setSelectedItem(itemToSelect);
            } else {
                comboFood.setSelectedIndex(0); // Chọn mục "-- Chọn món ăn --"
            }

        } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi tải danh sách món ăn!");
        } finally { /* Đóng resources */ try { if (rs != null) rs.close(); } catch (SQLException e) {} try { if (pst != null) pst.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
        // Cập nhật giá cho lựa chọn ban đầu
        updateSelectedPrice();
        updateTotalPrice(); // Cập nhật tổng tiền ban đầu
    }

    // Cập nhật JLabel hiển thị đơn giá
    private void updateSelectedPrice() {
        FoodItem selected = (FoodItem) comboFood.getSelectedItem();
        if (selected != null && selected.getId() > 0) { // ID > 0 là món ăn hợp lệ
            lblSelectedPriceValue.setText(String.format("%,.0f VNĐ", selected.getPrice()));
        } else {
            lblSelectedPriceValue.setText("0 VNĐ"); // Nếu chọn mục mặc định
        }
    }

    // Cập nhật JLabel hiển thị thành tiền
    private void updateTotalPrice() {
         FoodItem selected = (FoodItem) comboFood.getSelectedItem();
         int quantity = 0;
         try {
              // Lấy số lượng, đảm bảo là số không âm
              quantity = Integer.parseInt(txtQuantity.getText().trim());
              if (quantity < 0) quantity = 0;
         } catch (NumberFormatException e) {
              quantity = 0; // Coi là 0 nếu nhập không phải số
         }

         if (selected != null && selected.getId() > 0) { // Chỉ tính tiền khi chọn món hợp lệ
              double total = selected.getPrice() * quantity;
              lblTotalPriceValue.setText(String.format("%,.0f VNĐ", total));
         } else {
              lblTotalPriceValue.setText("0 VNĐ"); // Nếu chưa chọn món
         }
    }

    // Xử lý đặt hàng
    private void orderFood() {
        FoodItem selectedFood = (FoodItem) comboFood.getSelectedItem();
        int quantity;

        // Kiểm tra đã chọn món hợp lệ chưa
        if (selectedFood == null || selectedFood.getId() <= 0) {
            showError("Vui lòng chọn một món ăn từ danh sách!");
            comboFood.requestFocus();
            return;
        }

        // Kiểm tra và lấy số lượng
        try {
            quantity = Integer.parseInt(txtQuantity.getText().trim());
            if (quantity <= 0) {
                showError("Số lượng phải là số nguyên lớn hơn 0!");
                txtQuantity.requestFocus();
                txtQuantity.selectAll();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng nhập vào không hợp lệ!");
            txtQuantity.requestFocus();
            txtQuantity.selectAll();
            return;
        }

        int foodId = selectedFood.getId();

        Connection conn = null; PreparedStatement orderPst = null;
        try {
             conn = DBConnection.getConnection();
             // Thêm đơn hàng với trạng thái 'Pending'
             String orderSql = "INSERT INTO Orders (CustomerUsername, FoodID, Quantity, Status) VALUES (?, ?, ?, ?)";
             orderPst = conn.prepareStatement(orderSql);
             orderPst.setString(1, customerUsername);
             orderPst.setInt(2, foodId);
             orderPst.setInt(3, quantity);
             orderPst.setString(4, "Pending"); // Trạng thái chờ

             int result = orderPst.executeUpdate();
             if (result > 0) {
                 showMessage("Đặt hàng thành công!\nMón: " + selectedFood.getName() + "\nSố lượng: " + quantity, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                 // Reset form sau khi đặt thành công
                 txtQuantity.setText("1");
                 comboFood.setSelectedIndex(0); // Quay về "-- Chọn món ăn --"
                 // updateSelectedPrice(); // Được gọi khi combo box thay đổi index
                 // updateTotalPrice(); // Được gọi khi combo box thay đổi index
             } else { showError("Đặt hàng thất bại. Vui lòng thử lại."); }
        } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi CSDL khi đặt hàng!");
        } finally { /* Đóng resources */ try { if (orderPst != null) orderPst.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
    }

    // --- Phương thức tiện ích ---
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    private void showMessage(String message, String title, int messageType) { JOptionPane.showMessageDialog(this, message, title, messageType); }

    // --- Main (Để test) ---
    public static void main(String[] args) {
         applyStaticNimbusLookAndFeel(); // Áp dụng L&F cho main test
         SwingUtilities.invokeLater(() -> {
             // new OrderFoodForm("test_customer").setVisible(true); // Test không chọn sẵn
             new OrderFoodForm("test_customer", 1).setVisible(true); // Test chọn sẵn món có ID = 1 (nếu có)
         });
     }
      // Phương thức tĩnh để áp dụng L&F cho hàm main test
      private static void applyStaticNimbusLookAndFeel() {
          try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
          catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
      }

    // Tùy chọn: Lớp lọc chỉ cho nhập số vào JTextField
    /*
    static class NumberOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            if (string.matches("\\d*")) { // Chỉ cho phép chữ số
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            if (text.matches("\\d*")) { // Chỉ cho phép chữ số
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
    */

} // Kết thúc lớp OrderFoodForm