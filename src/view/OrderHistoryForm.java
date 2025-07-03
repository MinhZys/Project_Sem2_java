package view;

import customer.CustomerHome;
import connect.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class OrderHistoryForm extends JFrame {
    private JTable tableOrders;
    private DefaultTableModel tableModel;
    private JButton btnBack;
    private String customerUsername;

    public OrderHistoryForm(String customerUsername) {
        this.customerUsername = customerUsername;

        setTitle("Lịch sử đơn hàng");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("LỊCH SỬ ĐƠN HÀNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableOrders = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableOrders);

        tableModel.addColumn("Tên món ăn");
        tableModel.addColumn("Số lượng");
        tableModel.addColumn("Ngày đặt");

        loadOrderHistory();

        add(scrollPane, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel(new FlowLayout());
        btnBack = new JButton("Quay lại");
        panelSouth.add(btnBack);
        add(panelSouth, BorderLayout.SOUTH);

//        btnBack.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                new CustomerHome().setVisible(true);
//                dispose();
//            }
//        });
    }

    private void loadOrderHistory() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.FoodName, o.Quantity, o.OrderDate " +
                         "FROM Orders o JOIN Foods f ON o.FoodID = f.FoodID " +
                         "WHERE o.CustomerUsername = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, customerUsername);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String foodName = rs.getString("FoodName");
                int quantity = rs.getInt("Quantity");
                Timestamp orderDate = rs.getTimestamp("OrderDate");

                tableModel.addRow(new Object[]{foodName, quantity, orderDate.toString()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử đơn hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
