package view;

import customer.ManageOrdersForm;
import connect.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class InvoiceForm extends JFrame {
    private JTextArea txtInvoice;
    private JButton btnBack;
    private int orderId;

    public InvoiceForm(int orderId) {
        this.orderId = orderId;

        setTitle("Hóa đơn đơn hàng");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("HÓA ĐƠN", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        txtInvoice = new JTextArea();
        txtInvoice.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtInvoice.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtInvoice);
        add(scrollPane, BorderLayout.CENTER);

        btnBack = new JButton("Quay lại");
        add(btnBack, BorderLayout.SOUTH);

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageOrdersForm().setVisible(true);
                dispose();
            }
        });

        generateInvoice();
    }

    private void generateInvoice() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT o.OrderID, o.CustomerUsername, f.FoodName, o.Quantity, f.Price, o.OrderDate, o.Status " +
                         "FROM Orders o JOIN Foods f ON o.FoodID = f.FoodID " +
                         "WHERE o.OrderID = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, orderId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int orderID = rs.getInt("OrderID");
                String customer = rs.getString("CustomerUsername");
                String foodName = rs.getString("FoodName");
                int quantity = rs.getInt("Quantity");
                double price = rs.getDouble("Price");
                Timestamp orderDate = rs.getTimestamp("OrderDate");
                String status = rs.getString("Status");

                double total = quantity * price;

                StringBuilder sb = new StringBuilder();
                sb.append("====================================\n");
                sb.append("             HÓA ĐƠN                \n");
                sb.append("====================================\n");
                sb.append("Mã đơn hàng  : ").append(orderID).append("\n");
                sb.append("Khách hàng   : ").append(customer).append("\n");
                sb.append("Ngày đặt     : ").append(orderDate.toString()).append("\n");
                sb.append("Tình trạng   : ").append(status).append("\n");
                sb.append("------------------------------------\n");
                sb.append("Tên món ăn   : ").append(foodName).append("\n");
                sb.append("Số lượng     : ").append(quantity).append("\n");
                sb.append("Giá 1 món    : ").append(String.format("%,.0f", price)).append(" VNĐ\n");
                sb.append("------------------------------------\n");
                sb.append("TỔNG TIỀN    : ").append(String.format("%,.0f", total)).append(" VNĐ\n");
                sb.append("====================================\n");
                sb.append("       Xin cảm ơn quý khách!        \n");
                sb.append("====================================\n");

                txtInvoice.setText(sb.toString());
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
