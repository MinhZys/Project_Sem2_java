package employee;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import view.LoginForm;
import customer.ManageOrdersForm;

public class EmployeeHome extends JFrame {

    public EmployeeHome() {
        setTitle("Trang chủ Nhân viên");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Xin chào Nhân viên!", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnManageOrders = new JButton("Xử lý đơn hàng");
        JButton btnManageReservations = new JButton("Quản lý đặt trước");
        JButton btnLogout = new JButton("Đăng xuất");

        panel.add(btnManageOrders);
        panel.add(btnManageReservations);
        panel.add(btnLogout);

        add(panel, BorderLayout.CENTER);

        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LoginForm().setVisible(true);
                dispose();
            }
        });
        btnManageOrders.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
        new ManageOrdersForm().setVisible(true);
        dispose();
    }
});

    }
}
