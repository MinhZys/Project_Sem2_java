package view;

import employee.EmployeeHome;
import customer.CustomerHome;
import admin.AdminHome;
import connect.DBConnection;
// Bỏ import thư viện băm: import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginForm extends JFrame {
    // --- Các thành phần UI (Giữ nguyên) ---
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister, btnExit;

    // --- Constructor (Giữ nguyên) ---
     public LoginForm() { /* Giữ nguyên */
        setTitle("Đăng nhập hệ thống");
        setSize(450, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP", JLabel.CENTER); lblTitle.setFont(new Font("Arial", Font.BOLD, 28)); lblTitle.setForeground(new Color(70, 130, 180)); lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); add(lblTitle, BorderLayout.NORTH);
        JPanel panelCenter = new JPanel(new GridBagLayout()); panelCenter.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); panelCenter.setBackground(Color.WHITE); GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8, 8, 8, 8); gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; JLabel lblUsername = new JLabel("Tài khoản:"); lblUsername.setFont(new Font("Arial", Font.PLAIN, 14)); panelCenter.add(lblUsername, gbc); gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST; txtUsername = new JTextField(15); txtUsername.setFont(new Font("Arial", Font.PLAIN, 14)); panelCenter.add(txtUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.EAST; JLabel lblPassword = new JLabel("Mật khẩu:"); lblPassword.setFont(new Font("Arial", Font.PLAIN, 14)); panelCenter.add(lblPassword, gbc); gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST; txtPassword = new JPasswordField(15); txtPassword.setFont(new Font("Arial", Font.PLAIN, 14)); panelCenter.add(txtPassword, gbc); add(panelCenter, BorderLayout.CENTER);
        JPanel panelSouth = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); btnLogin = new JButton("Đăng nhập"); btnRegister = new JButton("Đăng ký"); btnExit = new JButton("Thoát");
        Dimension buttonSize = new Dimension(110, 35); Font buttonFont = new Font("Arial", Font.BOLD, 14); btnLogin.setPreferredSize(buttonSize); btnRegister.setPreferredSize(buttonSize); btnExit.setPreferredSize(buttonSize); btnLogin.setFont(buttonFont); btnRegister.setFont(buttonFont); btnExit.setFont(buttonFont); btnLogin.setBackground(new Color(0, 128, 0)); btnLogin.setForeground(Color.WHITE); btnRegister.setBackground(new Color(255, 165, 0)); btnRegister.setForeground(Color.WHITE); btnExit.setBackground(new Color(128, 128, 128)); btnExit.setForeground(Color.WHITE);
        panelSouth.add(btnLogin); panelSouth.add(btnRegister); panelSouth.add(btnExit); panelSouth.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); add(panelSouth, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> { RegisterForm registerForm = new RegisterForm(); registerForm.setVisible(true); });
        btnExit.addActionListener(e -> System.exit(0));
        txtPassword.addActionListener(e -> login());
     }

    // --- Phương thức login() - ĐÃ SỬA LẠI ĐỂ SO SÁNH TEXT THƯỜNG ---
    private void login() {
        String username = txtUsername.getText().trim();
        String enteredPassword = String.valueOf(txtPassword.getPassword()); // Mật khẩu người dùng nhập

        if (username.isEmpty() || enteredPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tài khoản và Mật khẩu!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // *** SQL SO SÁNH TRỰC TIẾP Username VÀ Password (TEXT THƯỜNG) ***
            String sql = "SELECT u.UserID, r.RoleName " +
                         "FROM Users u JOIN Roles r ON u.RoleID = r.RoleID " +
                         "WHERE u.Username = ? AND u.Password = ?"; // So sánh trực tiếp cột Password

            pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, enteredPassword); // Truyền mật khẩu text thường vào tham số

            rs = pst.executeQuery();

            // Nếu rs.next() trả về true, nghĩa là có dòng khớp -> đăng nhập thành công
            if (rs.next()) {
                String role = rs.getString("RoleName");
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công với quyền: " + role, "Thông báo", JOptionPane.INFORMATION_MESSAGE);

                // Mở giao diện phù hợp
                 if (role.equalsIgnoreCase("Admin")) { new AdminHome().setVisible(true); }
                 else if (role.equalsIgnoreCase("Employee")) { new EmployeeHome().setVisible(true); }
                 else if (role.equalsIgnoreCase("Customer")) { new CustomerHome(username).setVisible(true); }
                 this.dispose(); // Đóng form đăng nhập

            } else {
                // Không tìm thấy dòng nào khớp (sai username hoặc password)
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus(); // Focus lại ô mật khẩu
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
             JOptionPane.showMessageDialog(this, "Lỗi Kết nối hoặc Truy vấn CSDL!\n" + ex.getMessage(), "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Đóng tài nguyên
             try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignored */ }
             try { if (pst != null) pst.close(); } catch (SQLException e) { /* ignored */ }
             try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    // --- Main method (Giữ nguyên) ---
     public static void main(String[] args) { /* Giữ nguyên */
         try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
         catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
         SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
     }
}