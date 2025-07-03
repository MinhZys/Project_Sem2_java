package admin;

import admin.AdminHome;
import connect.DBConnection;
import model.RoleItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageUsersForm extends JFrame {

    private JTable tableUsers;
    private DefaultTableModel tableModel;
    private JTextField txtUserId;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JComboBox<RoleItem> comboRole;
    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnBack;

    private Vector<RoleItem> roleItems;
    private JPanel editPanel; // *** Biến editPanel thành trường của lớp ***

    public ManageUsersForm() {
        setTitle("Quản lý người dùng");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Thêm padding

        // --- Thiết lập các thành phần giao diện ---
        setupTablePanel();     // Thêm tablePanel vào CENTER
        setupEditPanel();      // Khởi tạo editPanel (nhưng chưa thêm vào frame)
        setupSouthContainer(); // Tạo container phía nam, thêm editPanel & buttonPanel, rồi thêm container vào SOUTH

        // --- Tải dữ liệu và Listener ---
        loadRoles();           // Tải roles cho ComboBox
        loadUsers();           // Tải users vào bảng
        setupTableListener();  // Gán listener cho bảng
    }

    // Thiết lập Panel chứa bảng (Không đổi)
    private void setupTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách người dùng"));

        tableModel = new DefaultTableModel();
        tableModel.addColumn("UserID");
        tableModel.addColumn("Username");
        tableModel.addColumn("Email");
        tableModel.addColumn("Số ĐT");
        tableModel.addColumn("Địa chỉ");
        tableModel.addColumn("Vai trò");

        tableUsers = new JTable(tableModel) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableUsers.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableUsers.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(tableUsers);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER); // Thêm vào vùng CENTER của JFrame
    }

    // *** SỬA LẠI: Khởi tạo editPanel nhưng không thêm vào frame ***
    private void setupEditPanel() {
        editPanel = new JPanel(new GridBagLayout()); // Khởi tạo trường editPanel
        editPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết/Chỉnh sửa"));
        editPanel.setPreferredSize(new Dimension(850, 200)); // Giữ kích thước gợi ý

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13); Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // UserID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; JLabel lblUserId = new JLabel("UserID:"); lblUserId.setFont(labelFont); editPanel.add(lblUserId, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; txtUserId = new JTextField(10); txtUserId.setFont(fieldFont); txtUserId.setEditable(false); txtUserId.setBackground(Color.LIGHT_GRAY); editPanel.add(txtUserId, gbc);

        // Username
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; JLabel lblUsername = new JLabel("Username:"); lblUsername.setFont(labelFont); editPanel.add(lblUsername, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; txtUsername = new JTextField(); txtUsername.setFont(fieldFont); editPanel.add(txtUsername, gbc);

        // Role
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0; JLabel lblRole = new JLabel("Vai trò:"); lblRole.setFont(labelFont); editPanel.add(lblRole, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; comboRole = new JComboBox<>(); comboRole.setFont(fieldFont); editPanel.add(comboRole, gbc);

        // Email
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0; JLabel lblEmail = new JLabel("Email:"); lblEmail.setFont(labelFont); editPanel.add(lblEmail, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; txtEmail = new JTextField(); txtEmail.setFont(fieldFont); editPanel.add(txtEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; JLabel lblPhone = new JLabel("Số điện thoại:"); lblPhone.setFont(labelFont); editPanel.add(lblPhone, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; txtPhone = new JTextField(); txtPhone.setFont(fieldFont); editPanel.add(txtPhone, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridheight = 2; gbc.anchor = GridBagConstraints.NORTHWEST; JLabel lblAddress = new JLabel("Địa chỉ:"); lblAddress.setFont(labelFont); editPanel.add(lblAddress, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        txtAddress = new JTextArea(3, 20); txtAddress.setFont(fieldFont); txtAddress.setLineWrap(true); txtAddress.setWrapStyleWord(true);
        JScrollPane addressScrollPane = new JScrollPane(txtAddress); editPanel.add(addressScrollPane, gbc);

        // *** KHÔNG thêm editPanel vào frame ở đây ***
        // add(editPanel, BorderLayout.SOUTH); // Bỏ dòng này
    }

    // *** PHƯƠNG THỨC MỚI: Tạo container phía Nam và thêm vào Frame ***
    private void setupSouthContainer() {
        JPanel southContainer = new JPanel(new BorderLayout());

        // Lấy buttonPanel từ phương thức setupButtonPanel()
        JPanel buttonPanel = setupButtonPanel();

        // Thêm editPanel (đã được khởi tạo ở setupEditPanel) vào giữa container
        if (this.editPanel != null) {
            southContainer.add(this.editPanel, BorderLayout.CENTER);
        } else {
             // Xử lý trường hợp editPanel chưa được tạo (không nên xảy ra)
            southContainer.add(new JLabel("Lỗi: Không thể hiển thị vùng chỉnh sửa."), BorderLayout.CENTER);
             System.err.println("Lỗi nghiêm trọng: editPanel là null khi gọi setupSouthContainer.");
        }

        // Thêm buttonPanel vào dưới cùng của container
        southContainer.add(buttonPanel, BorderLayout.SOUTH);

        // Thêm container tổng hợp này vào vùng SOUTH của JFrame
        add(southContainer, BorderLayout.SOUTH);
    }

    // *** SỬA LẠI: Chỉ tạo và trả về buttonPanel, gán sự kiện ***
    private JPanel setupButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnSave = new JButton("Lưu thay đổi");
        btnDelete = new JButton("Xóa người dùng");
        btnBack = new JButton("Quay lại Admin Home");

        Dimension btnSize = new Dimension(180, 35); Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnSave.setPreferredSize(btnSize); btnSave.setFont(btnFont); btnSave.setBackground(new Color(40, 167, 69)); btnSave.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(btnSize); btnDelete.setFont(btnFont); btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE);
        btnBack.setPreferredSize(btnSize); btnBack.setFont(btnFont);

        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnBack);

        // Gán sự kiện luôn ở đây
        btnSave.addActionListener(e -> saveUserChanges());
        btnDelete.addActionListener(e -> deleteUser());
        btnBack.addActionListener(e -> {
            new AdminHome().setVisible(true);
            dispose();
        });

        return buttonPanel; // Trả về panel đã tạo
    }

    // *** PHƯƠNG THỨC MỚI: Gán listener cho bảng ***
    private void setupTableListener() {
         tableUsers.getSelectionModel().addListSelectionListener(e -> {
            // Chỉ xử lý khi lựa chọn đã ổn định và có dòng được chọn
            if (!e.getValueIsAdjusting() && tableUsers.getSelectedRow() != -1) {
                displaySelectedUserInfo();
            }
        });
    }


    // --- Các phương thức xử lý dữ liệu (loadRoles, loadUsers, displaySelectedUserInfo, clearEditFields, saveUserChanges, deleteUser) ---
    // --- Giữ nguyên như phiên bản trước ---
     private void loadRoles() {
        roleItems = new Vector<>();
        Connection conn = null; Statement st = null; ResultSet rs = null;
        String sql = "SELECT RoleID, RoleName FROM Roles ORDER BY RoleID";
        try {
            conn = DBConnection.getConnection(); st = conn.createStatement(); rs = st.executeQuery(sql);
            while (rs.next()) { roleItems.add(new RoleItem(rs.getInt("RoleID"), rs.getString("RoleName"))); }
            DefaultComboBoxModel<RoleItem> model = new DefaultComboBoxModel<>(roleItems); comboRole.setModel(model);
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Lỗi tải vai trò: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally { /* Đóng resources */ try { if (rs != null) rs.close(); } catch (SQLException e) {} try { if (st != null) st.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
    }
     private void loadUsers() {
        tableModel.setRowCount(0);
        Connection conn = null; PreparedStatement pst = null; ResultSet rs = null;
        String sql = "SELECT u.UserID, u.Username, u.Email, u.PhoneNumber, u.Address, r.RoleName FROM Users u JOIN Roles r ON u.RoleID = r.RoleID ORDER BY u.UserID";
        try {
            conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); rs = pst.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("UserID")); row.add(rs.getString("Username")); row.add(rs.getString("Email")); row.add(rs.getString("PhoneNumber")); row.add(rs.getString("Address")); row.add(rs.getString("RoleName"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Lỗi tải người dùng: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally { /* Đóng resources */ try { if (rs != null) rs.close(); } catch (SQLException e) {} try { if (pst != null) pst.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
    }
     private void displaySelectedUserInfo() {
        int selectedRow = tableUsers.getSelectedRow();
        if (selectedRow >= 0) {
            int userId = (int) tableModel.getValueAt(selectedRow, 0);
            String username = (String) tableModel.getValueAt(selectedRow, 1);
            String email = tableModel.getValueAt(selectedRow, 2) == null ? "" : (String) tableModel.getValueAt(selectedRow, 2);
            String phone = tableModel.getValueAt(selectedRow, 3) == null ? "" : (String) tableModel.getValueAt(selectedRow, 3);
            String address = tableModel.getValueAt(selectedRow, 4) == null ? "" : (String) tableModel.getValueAt(selectedRow, 4);
            String roleName = (String) tableModel.getValueAt(selectedRow, 5);

            txtUserId.setText(String.valueOf(userId)); txtUsername.setText(username); txtEmail.setText(email); txtPhone.setText(phone); txtAddress.setText(address);

             for (RoleItem item : roleItems) { if (item.getName().equalsIgnoreCase(roleName)) { comboRole.setSelectedItem(item); break; } }
            btnSave.setEnabled(true); btnDelete.setEnabled(true);
        } else { clearEditFields(); btnSave.setEnabled(false); btnDelete.setEnabled(false); }
    }
    private void clearEditFields() {
         txtUserId.setText(""); txtUsername.setText(""); txtEmail.setText(""); txtPhone.setText(""); txtAddress.setText("");
         comboRole.setSelectedIndex(-1); tableUsers.clearSelection(); btnSave.setEnabled(false); btnDelete.setEnabled(false);
     }
    private void saveUserChanges() {
        if (txtUserId.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn người dùng để cập nhật.", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
        int userId = Integer.parseInt(txtUserId.getText()); String username = txtUsername.getText().trim(); String email = txtEmail.getText().trim(); String phone = txtPhone.getText().trim(); String address = txtAddress.getText().trim(); RoleItem selectedRoleItem = (RoleItem) comboRole.getSelectedItem();
        if (username.isEmpty()) { JOptionPane.showMessageDialog(this, "Username không trống.", "Lỗi", JOptionPane.ERROR_MESSAGE); txtUsername.requestFocus(); return; }
        if (selectedRoleItem == null) { JOptionPane.showMessageDialog(this, "Chọn vai trò.", "Lỗi", JOptionPane.ERROR_MESSAGE); comboRole.requestFocus(); return; }
        int roleId = selectedRoleItem.getId();

        Connection conn = null; PreparedStatement pstCheck = null; PreparedStatement pstUpdate = null; ResultSet rsCheck = null;
        String checkSql = "SELECT UserID FROM Users WHERE (Username = ? OR (? IS NOT NULL AND Email = ?)) AND UserID != ?";
        String updateSql = "UPDATE Users SET Username = ?, Email = ?, PhoneNumber = ?, Address = ?, RoleID = ? WHERE UserID = ?";
        try {
            conn = DBConnection.getConnection(); conn.setAutoCommit(false);
             boolean infoChanged = true; int selectedRow = tableUsers.getSelectedRow();
             if (selectedRow >= 0) { String oldUsername = (String) tableModel.getValueAt(selectedRow, 1); String oldEmail = tableModel.getValueAt(selectedRow, 2) == null ? "" : (String) tableModel.getValueAt(selectedRow, 2); if (username.equals(oldUsername) && email.equals(oldEmail)) { infoChanged = false; } }
            if (infoChanged) {
                 pstCheck = conn.prepareStatement(checkSql); pstCheck.setString(1, username);
                 if (email.isEmpty()) { pstCheck.setNull(2, Types.NVARCHAR); pstCheck.setNull(3, Types.NVARCHAR); } else { pstCheck.setString(2, email); pstCheck.setString(3, email); }
                 pstCheck.setInt(4, userId); rsCheck = pstCheck.executeQuery();
                 if (rsCheck.next()) { JOptionPane.showMessageDialog(this, "Username hoặc Email đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE); conn.rollback(); return; }
                 rsCheck.close(); pstCheck.close();
            }
            pstUpdate = conn.prepareStatement(updateSql); pstUpdate.setString(1, username);
            if (email.isEmpty()) pstUpdate.setNull(2, Types.NVARCHAR); else pstUpdate.setString(2, email); if (phone.isEmpty()) pstUpdate.setNull(3, Types.NVARCHAR); else pstUpdate.setString(3, phone); if (address.isEmpty()) pstUpdate.setNull(4, Types.NVARCHAR); else pstUpdate.setString(4, address);
            pstUpdate.setInt(5, roleId); pstUpdate.setInt(6, userId);
            int rowsAffected = pstUpdate.executeUpdate();
            if (rowsAffected > 0) { conn.commit(); JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE); loadUsers(); clearEditFields();
            } else { conn.rollback(); JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
        } catch (SQLException ex) { try { if (conn != null) conn.rollback(); } catch (SQLException se) {} ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally { /* Đóng resources */ try { if (rsCheck != null) rsCheck.close(); } catch (SQLException e) {} try { if (pstCheck != null) pstCheck.close(); } catch (SQLException e) {} try { if (pstUpdate != null) pstUpdate.close(); } catch (SQLException e) {} try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {} }
    }
    private void deleteUser() {
         if (txtUserId.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn người dùng để xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return; }
        int userId = Integer.parseInt(txtUserId.getText()); String username = txtUsername.getText();
        int confirmation = JOptionPane.showConfirmDialog(this, "Xóa người dùng '" + username + "' (ID: " + userId + ")?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            Connection conn = null; PreparedStatement pst = null; String sql = "DELETE FROM Users WHERE UserID = ?";
            try {
                conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); pst.setInt(1, userId);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) { JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE); loadUsers(); clearEditFields();
                } else { JOptionPane.showMessageDialog(this, "Xóa thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } catch (SQLException ex) { ex.printStackTrace(); if (ex.getMessage().contains("REFERENCE constraint")) { JOptionPane.showMessageDialog(this, "Không thể xóa, có dữ liệu liên quan.", "Lỗi", JOptionPane.ERROR_MESSAGE); } else { JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE); }
            } finally { /* Đóng resources */ try { if (pst != null) pst.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
        }
    }

    // --- Main method (Giữ nguyên) ---
     public static void main(String[] args) {
         try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
         catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
         SwingUtilities.invokeLater(() -> { new ManageUsersForm().setVisible(true); });
     }
} // Kết thúc lớp