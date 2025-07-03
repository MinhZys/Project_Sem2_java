package view;

import connect.DBConnection;
// Bỏ import thư viện băm: import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.Pattern;

public class RegisterForm extends JFrame {

    // --- Các thành phần UI và Pattern (Giữ nguyên) ---
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtPhoneNumber;
    private JTextArea txtAddress;
    private JButton btnRegister, btnCancel;
    private static final Pattern EMAIL_PATTERN = Pattern.compile( /* Giữ nguyên */
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    // --- Constructor và các phương thức tạo UI (Giữ nguyên) ---
     public RegisterForm() { /* Giữ nguyên phần khởi tạo UI */
        setTitle("Đăng ký tài khoản người dùng");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = createTitleLabel("ĐĂNG KÝ TÀI KHOẢN");
        add(lblTitle, BorderLayout.NORTH);
        JPanel panelCenter = createCenterPanel();
        add(panelCenter, BorderLayout.CENTER);
        JPanel panelSouth = createSouthPanel();
        add(panelSouth, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(500, 520));
        setLocationRelativeTo(null);

        btnRegister.addActionListener(e -> registerUser());
        btnCancel.addActionListener(e -> dispose());
     }
     private JLabel createTitleLabel(String text) { /* Giữ nguyên */
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(new Color(0, 102, 204));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        return label;
     }
    private JPanel createCenterPanel() { /* Giữ nguyên */
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder( BorderFactory.createLineBorder(new Color(180, 180, 180)),
                    " Thông tin đăng ký ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.PLAIN, 14) ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15) ));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14); Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; JLabel lblUsername = new JLabel("Tên đăng nhập (*):"); lblUsername.setFont(labelFont); panel.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtUsername = new JTextField(25); txtUsername.setFont(fieldFont); panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; JLabel lblPassword = new JLabel("Mật khẩu (*):"); lblPassword.setFont(labelFont); panel.add(lblPassword, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtPassword = new JPasswordField(25); txtPassword.setFont(fieldFont); panel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; JLabel lblConfirmPassword = new JLabel("Xác nhận MK (*):"); lblConfirmPassword.setFont(labelFont); panel.add(lblConfirmPassword, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtConfirmPassword = new JPasswordField(25); txtConfirmPassword.setFont(fieldFont); panel.add(txtConfirmPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; JLabel lblEmail = new JLabel("Email:"); lblEmail.setFont(labelFont); panel.add(lblEmail, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtEmail = new JTextField(25); txtEmail.setFont(fieldFont); panel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; JLabel lblPhoneNumber = new JLabel("Số điện thoại:"); lblPhoneNumber.setFont(labelFont); panel.add(lblPhoneNumber, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; txtPhoneNumber = new JTextField(25); txtPhoneNumber.setFont(fieldFont); panel.add(txtPhoneNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST; JLabel lblAddress = new JLabel("Địa chỉ:"); lblAddress.setFont(labelFont); panel.add(lblAddress, gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.gridheight = 2; gbc.fill = GridBagConstraints.BOTH;
        txtAddress = new JTextArea(3, 25); txtAddress.setFont(fieldFont); txtAddress.setLineWrap(true); txtAddress.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(txtAddress); scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); panel.add(scrollPane, gbc);

        gbc.gridheight = 1; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; return panel;
     }
    private JPanel createSouthPanel() { /* Giữ nguyên */
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnRegister = new JButton("Đăng ký"); btnCancel = new JButton("Hủy");
        Dimension btnSize = new Dimension(110, 35); Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnRegister.setPreferredSize(btnSize); btnRegister.setFont(btnFont); btnRegister.setBackground(new Color(40, 167, 69)); btnRegister.setForeground(Color.WHITE); btnRegister.setFocusPainted(false);
        btnCancel.setPreferredSize(btnSize); btnCancel.setFont(btnFont); btnCancel.setBackground(new Color(108, 117, 125)); btnCancel.setForeground(Color.WHITE); btnCancel.setFocusPainted(false);
        panel.add(btnRegister); panel.add(btnCancel); return panel;
    }
    // --- ---

    // --- BỎ phương thức hashPassword ---
    // private String hashPassword(String plainTextPassword) { ... }

    // --- Logic xử lý đăng ký - ĐÃ SỬA LẠI ĐỂ LƯU TEXT THƯỜNG ---
    private void registerUser() {
        String username = txtUsername.getText().trim();
        String password = String.valueOf(txtPassword.getPassword()); // Lấy mật khẩu text thường
        String confirmPassword = String.valueOf(txtConfirmPassword.getPassword());
        String email = txtEmail.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String address = txtAddress.getText().trim();

        // --- Kiểm tra dữ liệu đầu vào (Giữ nguyên) ---
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) { showErrorDialog("..."); return; }
        if (!password.equals(confirmPassword)) { showErrorDialog("..."); /* reset pass fields*/ return; }
        if (password.length() < 6) { showErrorDialog("..."); /* reset pass fields*/ return; }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) { showErrorDialog("..."); return; }
        if (!phoneNumber.isEmpty() && !phoneNumber.matches("\\d{10,11}")) { showErrorDialog("..."); return; }
        // --- ---

        // --- KHÔNG CẦN BĂM MẬT KHẨU ---
        // String hashedPassword = hashPassword(password); // Bỏ dòng này

        // --- Tương tác Cơ sở dữ liệu ---
        Connection conn = null;
        PreparedStatement pstCheck = null;
        PreparedStatement pstInsert = null;
        ResultSet rsCheck = null;
        String checkSql = "SELECT UserID FROM Users WHERE Username = ? OR (? IS NOT NULL AND Email = ?)";
        // *** SQL INSERT sử dụng cột 'Password' ***
        String insertSql = "INSERT INTO Users (Username, Password, Email, PhoneNumber, Address, RoleID) VALUES (?, ?, ?, ?, ?, ?)";
        int customerRoleId = 3;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Kiểm tra Username/Email tồn tại (Giữ nguyên)
             pstCheck = conn.prepareStatement(checkSql);
             pstCheck.setString(1, username);
             if (email.isEmpty()) { pstCheck.setNull(2, Types.NVARCHAR); pstCheck.setNull(3, Types.NVARCHAR); }
             else { pstCheck.setString(2, email); pstCheck.setString(3, email); }
             rsCheck = pstCheck.executeQuery();
             if (rsCheck.next()) { showErrorDialog("Tên đăng nhập hoặc Email đã được sử dụng."); conn.rollback(); return; }
             rsCheck.close(); pstCheck.close();


            // 2. Thêm người dùng mới với MẬT KHẨU TEXT THƯỜNG
            pstInsert = conn.prepareStatement(insertSql);
            pstInsert.setString(1, username);
            // *** LƯU MẬT KHẨU TEXT THƯỜNG ***
            pstInsert.setString(2, password); // Lưu trực tiếp mật khẩu nhập vào
            // ... (gán các tham số còn lại - giữ nguyên) ...
             if (email.isEmpty()) pstInsert.setNull(3, Types.NVARCHAR); else pstInsert.setString(3, email);
             if (phoneNumber.isEmpty()) pstInsert.setNull(4, Types.NVARCHAR); else pstInsert.setString(4, phoneNumber);
             if (address.isEmpty()) pstInsert.setNull(5, Types.NVARCHAR); else pstInsert.setString(5, address);
             pstInsert.setInt(6, customerRoleId);

            int rowsAffected = pstInsert.executeUpdate();

            if (rowsAffected > 0) {
                conn.commit();
                showMessageDialog("Đăng ký thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                conn.rollback();
                showErrorDialog("Đăng ký thất bại.");
            }
        } catch (SQLException ex) {
             try { if (conn != null) conn.rollback(); } catch (SQLException se) { /* ignored */ }
            ex.printStackTrace();
             showErrorDialog("Lỗi Cơ sở dữ liệu: " + ex.getMessage());
        } finally {
             // Đóng tài nguyên (giữ nguyên)
            try { if (rsCheck != null) rsCheck.close(); } catch (SQLException e) { /* ignored */ }
            try { if (pstCheck != null) pstCheck.close(); } catch (SQLException e) { /* ignored */ }
            try { if (pstInsert != null) pstInsert.close(); } catch (SQLException e) { /* ignored */ }
             try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { /* ignored */ }
        }
    }

    // --- Các phương thức hiển thị thông báo (Giữ nguyên) ---
     private void showErrorDialog(String message) { /* Giữ nguyên */ JOptionPane.showMessageDialog(this, message, "Lỗi Đăng ký", JOptionPane.ERROR_MESSAGE); }
     private void showMessageDialog(String message, String title, int messageType) { /* Giữ nguyên */ JOptionPane.showMessageDialog(this, message, title, messageType); }

    // --- Main method (Giữ nguyên) ---
     public static void main(String[] args) { /* Giữ nguyên */
         try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
         catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
         SwingUtilities.invokeLater(() -> { new RegisterForm().setVisible(true); });
     }
}