package customer;

import employee.EmployeeHome;
import connect.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageOrdersForm extends JFrame {
    private JTable tableOrders;
    private DefaultTableModel tableModel;
    private JButton btnApprove, btnCancel, btnBack;

    public ManageOrdersForm() {
        setTitle("Quản lý đơn hàng");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐƠN HÀNG", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel();
        tableOrders = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableOrders);

        tableModel.addColumn("Mã đơn");
        tableModel.addColumn("Khách hàng");
        tableModel.addColumn("Tên món ăn");
        tableModel.addColumn("Số lượng");
        tableModel.addColumn("Ngày đặt");

        loadPendingOrders();

        add(scrollPane, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel(new FlowLayout());
        btnApprove = new JButton("Duyệt đơn");
        btnCancel = new JButton("Hủy đơn");
        btnBack = new JButton("Quay lại");

        panelSouth.add(btnApprove);
        panelSouth.add(btnCancel);
        panelSouth.add(btnBack);
        add(panelSouth, BorderLayout.SOUTH);

        btnApprove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processOrder("Approved");
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processOrder("Cancelled");
            }
        });

        btnBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new EmployeeHome().setVisible(true);
                dispose();
            }
        });
    }

    private void loadPendingOrders() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT o.OrderID, o.CustomerUsername, f.FoodName, o.Quantity, o.OrderDate " +
                         "FROM Orders o JOIN Foods f ON o.FoodID = f.FoodID " +
                         "WHERE o.Status = 'Pending'";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("OrderID");
                String customer = rs.getString("CustomerUsername");
                String foodName = rs.getString("FoodName");
                int quantity = rs.getInt("Quantity");
                Timestamp orderDate = rs.getTimestamp("OrderDate");

                tableModel.addRow(new Object[]{orderId, customer, foodName, quantity, orderDate.toString()});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải đơn hàng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processOrder(String newStatus) {
        int selectedRow = tableOrders.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, newStatus);
            pst.setInt(2, orderId);

            int result = pst.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật đơn hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                tableModel.removeRow(selectedRow); // Xóa dòng khỏi bảng ngay
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
