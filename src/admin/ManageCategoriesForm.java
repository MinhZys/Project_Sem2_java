package admin;

import admin.AdminHome;
import connect.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class ManageCategoriesForm extends JFrame {

    private JTable tableCategories;
    private DefaultTableModel tableModel;
    private JTextField txtCategoryId;
    private JTextField txtCategoryName;
    private JTextArea txtDescription;
    private JButton btnAddNew;
    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnBack;

    private boolean isAddingNew = false;

    public ManageCategoriesForm() {
        setTitle("Quản lý Danh mục Món ăn");
        setSize(750, 550); // Kích thước phù hợp
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setupTablePanel();
        setupEditPanel();

        loadCategories();
        setupTableListener();
    }

    private void setupTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách danh mục"));

        tableModel = new DefaultTableModel();
        tableModel.addColumn("ID");
        tableModel.addColumn("Tên Danh mục");
        tableModel.addColumn("Mô tả");

        tableCategories = new JTable(tableModel) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCategories.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCategories.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableCategories.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableCategories.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableCategories.getColumnModel().getColumn(2).setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(tableCategories);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void setupEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Thêm mới / Chỉnh sửa danh mục"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // CategoryID (Read-only)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        JLabel lblCatId = new JLabel("ID:"); lblCatId.setFont(labelFont); editPanel.add(lblCatId, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9; gbc.gridwidth=2; // Chiếm 2 cột
        txtCategoryId = new JTextField(5); txtCategoryId.setFont(fieldFont); txtCategoryId.setEditable(false); txtCategoryId.setBackground(Color.LIGHT_GRAY); editPanel.add(txtCategoryId, gbc);
        gbc.gridwidth=1; // Reset

        // CategoryName
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        JLabel lblCatName = new JLabel("Tên danh mục (*):"); lblCatName.setFont(labelFont); editPanel.add(lblCatName, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9; gbc.gridwidth=2;
        txtCategoryName = new JTextField(30); txtCategoryName.setFont(fieldFont); editPanel.add(txtCategoryName, gbc);
        gbc.gridwidth=1;

        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridheight = 2; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.weightx = 0.1;
        JLabel lblDesc = new JLabel("Mô tả:"); lblDesc.setFont(labelFont); editPanel.add(lblDesc, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.gridheight = 2; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 0.9;
        txtDescription = new JTextArea(3, 30); txtDescription.setFont(fieldFont); txtDescription.setLineWrap(true); txtDescription.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(txtDescription); editPanel.add(descScrollPane, gbc);
        gbc.gridwidth = 1; gbc.gridheight = 1; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; // Reset

        // Button Panel
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAddNew = new JButton("Thêm mới"); btnSave = new JButton("Lưu"); btnDelete = new JButton("Xóa"); btnBack = new JButton("Quay lại");
        Dimension btnSize = new Dimension(110, 35); Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnAddNew.setPreferredSize(btnSize); btnAddNew.setFont(btnFont);
        btnSave.setPreferredSize(btnSize); btnSave.setFont(btnFont); btnSave.setBackground(new Color(40, 167, 69)); btnSave.setForeground(Color.WHITE);
        btnDelete.setPreferredSize(btnSize); btnDelete.setFont(btnFont); btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE);
        btnBack.setPreferredSize(btnSize); btnBack.setFont(btnFont);
        buttonPanel.add(btnAddNew); buttonPanel.add(btnSave); buttonPanel.add(btnDelete); buttonPanel.add(btnBack);
        editPanel.add(buttonPanel, gbc);

        btnSave.setEnabled(false); btnDelete.setEnabled(false);

        // Actions
        btnAddNew.addActionListener(e -> clearFieldsForNew());
        btnSave.addActionListener(e -> saveCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnBack.addActionListener(e -> { new AdminHome().setVisible(true); dispose(); });

        add(editPanel, BorderLayout.SOUTH);
    }

     private void setupTableListener() {
         tableCategories.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableCategories.getSelectedRow() != -1) {
                displaySelectedCategory();
            }
        });
    }

    private void loadCategories() {
        tableModel.setRowCount(0);
        Connection conn = null; Statement st = null; ResultSet rs = null;
        String sql = "SELECT CategoryID, CategoryName, Description FROM Categories ORDER BY CategoryID";
        try {
            conn = DBConnection.getConnection(); st = conn.createStatement(); rs = st.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("CategoryID"));
                row.add(rs.getString("CategoryName"));
                row.add(rs.getString("Description"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) { /* Xử lý lỗi */ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Lỗi tải danh mục","Lỗi",0);
        } finally { /* Đóng resources */ try { if (rs != null) rs.close(); } catch (SQLException e) {} try { if (st != null) st.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
    }

     private void displaySelectedCategory() {
        int selectedRow = tableCategories.getSelectedRow();
        if (selectedRow >= 0) {
            isAddingNew = false;
            txtCategoryId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtCategoryName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDescription.setText(tableModel.getValueAt(selectedRow, 2) == null ? "" : tableModel.getValueAt(selectedRow, 2).toString());
            btnSave.setEnabled(true); btnDelete.setEnabled(true); btnSave.setText("Lưu");
        }
    }

    private void clearFieldsForNew() {
        isAddingNew = true;
        txtCategoryId.setText(""); txtCategoryName.setText(""); txtDescription.setText("");
        tableCategories.clearSelection(); txtCategoryName.requestFocus();
        btnSave.setEnabled(true); btnDelete.setEnabled(false); btnSave.setText("Thêm");
    }

    private void saveCategory() {
        String catName = txtCategoryName.getText().trim();
        String description = txtDescription.getText().trim();

        if (catName.isEmpty()) { JOptionPane.showMessageDialog(this, "Tên danh mục không trống.", "Lỗi", 0); txtCategoryName.requestFocus(); return; }

        Connection conn = null; PreparedStatement pst = null; PreparedStatement pstCheck = null; ResultSet rsCheck = null;
        String sql;
        int categoryId = -1;
        if (!isAddingNew) {
             if (txtCategoryId.getText().isEmpty()) { JOptionPane.showMessageDialog(this,"Chọn danh mục để sửa.","Lỗi",0); return; }
             categoryId = Integer.parseInt(txtCategoryId.getText());
             sql = "UPDATE Categories SET CategoryName = ?, Description = ? WHERE CategoryID = ?";
        } else {
             sql = "INSERT INTO Categories (CategoryName, Description) VALUES (?, ?)";
        }
        // Kiểm tra tên trùng lặp trước khi lưu
        String checkSql = "SELECT CategoryID FROM Categories WHERE CategoryName = ? AND (? = -1 OR CategoryID != ?)"; // Nếu isAddingNew thì categoryId = -1

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra trùng tên
            pstCheck = conn.prepareStatement(checkSql);
            pstCheck.setString(1, catName);
            pstCheck.setInt(2, categoryId); // Nếu thêm mới thì categoryId = -1, điều kiện AND sẽ luôn đúng phần ID
            pstCheck.setInt(3, categoryId);
            rsCheck = pstCheck.executeQuery();
            if(rsCheck.next()){
                 JOptionPane.showMessageDialog(this,"Tên danh mục đã tồn tại.","Lỗi Trùng lặp",0);
                 conn.rollback();
                 return;
            }
            rsCheck.close(); pstCheck.close();


            // Thực hiện INSERT hoặc UPDATE
            pst = conn.prepareStatement(sql);
            pst.setString(1, catName);
            pst.setString(2, description.isEmpty() ? null : description);
            if (!isAddingNew) {
                pst.setInt(3, categoryId); // Tham số cuối cho WHERE của UPDATE
            }

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(this, (isAddingNew ? "Thêm" : "Cập nhật") + " danh mục thành công!", "Thành công", 1);
                loadCategories(); clearFieldsForNew(); isAddingNew = false; btnSave.setText("Lưu");
            } else {
                conn.rollback();
                JOptionPane.showMessageDialog(this, (isAddingNew ? "Thêm" : "Cập nhật") + " thất bại.", "Lỗi", 0);
            }
        } catch (SQLException ex) {
             try { if(conn != null) conn.rollback(); } catch(SQLException se) {}
             ex.printStackTrace();
              if (ex.getMessage().contains("UNIQUE KEY constraint")) { JOptionPane.showMessageDialog(this,"Tên danh mục đã tồn tại.","Lỗi",0);
              } else { JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage(), "Lỗi", 0); }
        } finally { /* Đóng resources */ try { if(rsCheck != null) rsCheck.close();} catch (SQLException e){} try { if(pstCheck != null) pstCheck.close();} catch (SQLException e){} try { if (pst != null) pst.close(); } catch (SQLException e) {} try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {} }
    }

    private void deleteCategory() {
        if (txtCategoryId.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn danh mục để xóa.", "Cảnh báo", 2); return; }
        int categoryId = Integer.parseInt(txtCategoryId.getText()); String catName = txtCategoryName.getText();

        int confirmation = JOptionPane.showConfirmDialog(this, "Xóa danh mục '" + catName + "' (ID: " + categoryId + ")?\nMón ăn thuộc danh mục này có thể bị ảnh hưởng (tùy cấu hình khóa ngoại).", "Xác nhận", JOptionPane.YES_NO_OPTION, 3);

        if (confirmation == JOptionPane.YES_OPTION) {
            Connection conn = null; PreparedStatement pst = null; String sql = "DELETE FROM Categories WHERE CategoryID = ?";
            try {
                conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); pst.setInt(1, categoryId);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) { JOptionPane.showMessageDialog(this, "Xóa thành công!", "Thành công", 1); loadCategories(); clearFieldsForNew(); isAddingNew = false; btnSave.setText("Lưu");
                } else { JOptionPane.showMessageDialog(this, "Xóa thất bại.", "Lỗi", 0); }
            } catch (SQLException ex) {
                 ex.printStackTrace();
                 if (ex.getMessage().contains("REFERENCE constraint")) { JOptionPane.showMessageDialog(this,"Không thể xóa danh mục đang được món ăn sử dụng.","Lỗi",0);
                 } else { JOptionPane.showMessageDialog(this, "Lỗi CSDL: " + ex.getMessage(), "Lỗi", 0); }
            } finally { /* Đóng resources */ try { if (pst != null) pst.close(); } catch (SQLException e) {} try { if (conn != null) conn.close(); } catch (SQLException e) {} }
        }
    }

     public static void main(String[] args) {
         try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
         catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
         SwingUtilities.invokeLater(() -> { new ManageCategoriesForm().setVisible(true); });
     }
}