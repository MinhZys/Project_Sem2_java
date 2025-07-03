package admin; // Hoặc package admin tùy cấu trúc của bạn

import admin.AdminHome;
import connect.DBConnection;
import model.CategoryItem; // Import model cho danh mục

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter; // Để lọc file ảnh
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Vector;
import java.math.BigDecimal; // Dùng BigDecimal cho Price

public class ManageFoodForm extends JFrame {

    //--- Khai báo các thành phần UI ---
    private JTable tableFoods;
    private DefaultTableModel tableModel;
    private JTextField txtFoodId;
    private JTextField txtFoodName;
    private JTextArea txtDescription;
    private JTextField txtPrice;
    private JTextField txtImagePath;
    private JComboBox<String> comboStatus; // Chỉ chứa Draft, Unavailable
    private JComboBox<CategoryItem> comboCategory;
    private JLabel lblImagePreview; // Label xem trước ảnh
    private JButton btnAddNew;
    private JButton btnSave;      // Lưu thông tin (trạng thái Draft/Unavailable)
    private JButton btnDelete;
    private JButton btnTogglePublish; // Nút Đăng bán / Ngừng bán
    private JButton btnBack;
    private JButton btnBrowseImage;

    //--- Các biến trạng thái và dữ liệu ---
    private Vector<CategoryItem> categoryItems; // Dữ liệu cho combo danh mục
    private boolean isAddingNew = false;        // Cờ trạng thái thêm mới/sửa

    //--- Constructor ---
    public ManageFoodForm() {
        setTitle("Quản lý Món ăn");
        setSize(1000, 720); // Kích thước cửa sổ
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng form này không thoát cả ứng dụng
        setLocationRelativeTo(null); // Căn giữa màn hình
        setLayout(new BorderLayout(10, 10)); // Layout chính
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        applyNimbusLookAndFeel(); // Áp dụng giao diện

        // Khởi tạo và sắp xếp các thành phần giao diện
        setupTablePanel();      // Panel bảng
        setupEditPanel();       // Panel chỉnh sửa + nút

        // Tải dữ liệu ban đầu
        loadCategories();       // Tải danh mục cho ComboBox
        loadFoods();            // Tải món ăn vào bảng
        setupTableListener();   // Gán sự kiện cho việc chọn dòng trên bảng
    }

    //--- Phương thức cài đặt giao diện ---

    // Áp dụng giao diện Nimbus
    private void applyNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed Nimbus L&F: " + ex.getMessage());
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // Cài đặt Panel chứa bảng hiển thị danh sách món ăn
    private void setupTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách món ăn"));
        tableModel = new DefaultTableModel();
        // Định nghĩa các cột cho bảng
        tableModel.addColumn("ID");
        tableModel.addColumn("Tên Món Ăn");
        tableModel.addColumn("Danh mục");
        tableModel.addColumn("Giá (VNĐ)");
        tableModel.addColumn("Trạng thái"); // Published, Draft, Unavailable
        tableModel.addColumn("Mô tả");

        tableFoods = new JTable(tableModel) {
            @Override public boolean isCellEditable(int row, int column) { return false; } // Không cho sửa trực tiếp trên bảng
        };
        tableFoods.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ chọn 1 dòng
        tableFoods.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableFoods.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        // Thiết lập độ rộng ưu tiên cho các cột
        tableFoods.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableFoods.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableFoods.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableFoods.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableFoods.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableFoods.getColumnModel().getColumn(5).setPreferredWidth(280);
        tableFoods.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Hoặc OFF nếu muốn cuộn ngang

        JScrollPane scrollPane = new JScrollPane(tableFoods);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER); // Thêm panel bảng vào giữa Frame
    }

    // Cài đặt Panel chứa các trường nhập liệu và nút chức năng
     private void setupEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Thêm mới / Chỉnh sửa món ăn"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8); // Khoảng cách giữa các component
        gbc.anchor = GridBagConstraints.WEST;   // Căn lề trái cho các component
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        // --- Hàng 0 ---
        // ID Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0; // Label không giãn
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("ID:"), gbc);
        // ID TextField
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.4; // Field giãn
        gbc.anchor = GridBagConstraints.WEST;
        txtFoodId = new JTextField(5); txtFoodId.setFont(fieldFont); txtFoodId.setEditable(false); txtFoodId.setBackground(Color.LIGHT_GRAY); editPanel.add(txtFoodId, gbc);
        // Status Label
        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("Trạng thái Lưu:"), gbc);
        // Status ComboBox
        gbc.gridx = 3; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5; gbc.gridwidth=2; // Span 2 cột cuối
        gbc.anchor = GridBagConstraints.WEST;
        comboStatus = new JComboBox<>(new String[]{"Draft", "Unavailable"}); comboStatus.setToolTipText("Trạng thái khi lưu"); comboStatus.setFont(fieldFont); editPanel.add(comboStatus, gbc);
        gbc.gridwidth = 1; // Reset

        // --- Hàng 1 ---
        // Name Label
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("Tên món (*):"), gbc);
        // Name TextField
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 4; gbc.weightx = 1.0; // Span 4 cột
        gbc.anchor = GridBagConstraints.WEST;
        txtFoodName = new JTextField(); txtFoodName.setFont(fieldFont); editPanel.add(txtFoodName, gbc);
        gbc.gridwidth = 1; // Reset

        // --- Hàng 2 ---
        // Price Label
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("Giá (*):"), gbc);
        // Price TextField
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.WEST;
        txtPrice = new JTextField(); txtPrice.setFont(fieldFont); editPanel.add(txtPrice, gbc);
        // Category Label
        gbc.gridx = 2; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("Danh mục:"), gbc);
        // Category ComboBox
        gbc.gridx = 3; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 0.5; gbc.gridwidth=2; // Span 2 cột cuối
        gbc.anchor = GridBagConstraints.WEST;
        comboCategory = new JComboBox<>(); comboCategory.setFont(fieldFont); editPanel.add(comboCategory, gbc);
        gbc.gridwidth = 1; // Reset

        // --- Hàng 3 ---
        // Image Path Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST; editPanel.add(new JLabel("Ảnh:"), gbc);
        // Image Path TextField
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridwidth = 3; gbc.weightx = 0.9; // Span 3 cột
        gbc.anchor = GridBagConstraints.WEST;
        txtImagePath = new JTextField(); txtImagePath.setFont(fieldFont); editPanel.add(txtImagePath, gbc);
        // Browse Button
        gbc.gridx = 4; gbc.gridy = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        btnBrowseImage = new JButton("..."); btnBrowseImage.setToolTipText("Chọn ảnh"); btnBrowseImage.setMargin(new Insets(2, 5, 2, 5)); btnBrowseImage.setFont(fieldFont); btnBrowseImage.addActionListener(e -> browseImage()); editPanel.add(btnBrowseImage, gbc);

        // --- Hàng 4 + 5: Description (Left), Image Preview (Right) ---
        // Description (Label + TextArea)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.gridheight = 2; // Span 2 cột, 2 hàng
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 0.6; gbc.weighty = 1.0; // Cho phép giãn cả 2 chiều
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JPanel descPanel = new JPanel(new BorderLayout(0, 3)); descPanel.setOpaque(false);
        descPanel.add(new JLabel("Mô tả:"), BorderLayout.NORTH);
        txtDescription = new JTextArea(4, 20); txtDescription.setFont(fieldFont); txtDescription.setLineWrap(true); txtDescription.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(txtDescription);
        descPanel.add(descScrollPane, BorderLayout.CENTER);
        editPanel.add(descPanel, gbc);

        // Image Preview
        gbc.gridx = 2; gbc.gridy = 4; gbc.gridwidth = 3; gbc.gridheight = 2; // Span 3 cột (2,3,4), 2 hàng
        gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 0.4; gbc.weighty = 1.0; // Cho phép giãn
        gbc.anchor = GridBagConstraints.CENTER; // Căn giữa ảnh
        lblImagePreview = new JLabel("Xem trước ảnh", JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(180, 120)); // Kích thước cố định
        lblImagePreview.setMinimumSize(new Dimension(180, 120));
        lblImagePreview.setBorder(BorderFactory.createEtchedBorder());
        lblImagePreview.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lblImagePreview.setForeground(Color.GRAY);
        editPanel.add(lblImagePreview, gbc);

        // --- Hàng 6: Panel Nút ---
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 5; gbc.gridheight = 1; // Quay lại 1 hàng
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0; gbc.weighty = 0.0; // Không giãn dọc
        gbc.insets = new Insets(15, 5, 10, 5); // Padding trên và dưới
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Giảm padding dọc của flowlayout
        btnAddNew = new JButton("Thêm mới"); btnSave = new JButton("Lưu"); btnDelete = new JButton("Xóa"); btnTogglePublish = new JButton("Đăng bán"); btnBack = new JButton("Quay lại");
        Dimension btnSize = new Dimension(110, 35); Font btnFont = new Font("Segoe UI", Font.BOLD, 14);
        btnAddNew.setPreferredSize(btnSize); btnAddNew.setFont(btnFont);
        btnSave.setPreferredSize(btnSize); btnSave.setFont(btnFont); btnSave.setBackground(new Color(40, 167, 69)); btnSave.setForeground(Color.WHITE); btnSave.setFocusPainted(false);
        btnDelete.setPreferredSize(btnSize); btnDelete.setFont(btnFont); btnDelete.setBackground(new Color(220, 53, 69)); btnDelete.setForeground(Color.WHITE); btnDelete.setFocusPainted(false);
        btnTogglePublish.setPreferredSize(new Dimension(130, 35)); btnTogglePublish.setFont(btnFont); btnTogglePublish.setBackground(new Color(0, 123, 255)); btnTogglePublish.setForeground(Color.WHITE); btnTogglePublish.setFocusPainted(false);
        btnBack.setPreferredSize(btnSize); btnBack.setFont(btnFont); btnBack.setFocusPainted(false);
        buttonPanel.add(btnAddNew); buttonPanel.add(btnSave); buttonPanel.add(btnDelete); buttonPanel.add(btnTogglePublish); buttonPanel.add(btnBack);
        editPanel.add(buttonPanel, gbc);

        // Trạng thái nút ban đầu
        btnSave.setEnabled(false); btnDelete.setEnabled(false); btnTogglePublish.setEnabled(false);

        // Gán sự kiện
        btnAddNew.addActionListener(e -> clearFieldsForNew()); btnSave.addActionListener(e -> saveFood()); btnDelete.addActionListener(e -> deleteFood()); btnTogglePublish.addActionListener(e -> togglePublishStatus()); btnBack.addActionListener(e -> { new AdminHome().setVisible(true); dispose(); });

        add(editPanel, BorderLayout.SOUTH); // Thêm panel vào dưới cùng frame
    }


    // Gán listener cho việc chọn dòng trên bảng
    private void setupTableListener() {
        tableFoods.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableFoods.getSelectedRow() != -1) {
                displaySelectedFoodInfo();
            }
        });
    }

    //--- Các phương thức xử lý dữ liệu ---

    // Tải danh mục từ CSDL vào ComboBox
    private void loadCategories() {
        categoryItems = new Vector<>();
        Connection conn = null; Statement st = null; ResultSet rs = null;
        String sql = "SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryName";
        try {
            conn = DBConnection.getConnection(); st = conn.createStatement(); rs = st.executeQuery(sql);
            categoryItems.add(new CategoryItem(0, "-- Chọn danh mục --")); // Mục mặc định
            while (rs.next()) { categoryItems.add(new CategoryItem(rs.getInt(1), rs.getString(2))); }
            comboCategory.setModel(new DefaultComboBoxModel<>(categoryItems));
        } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi tải danh mục: " + ex.getMessage());
        } finally { closeDbResources(rs, st, conn); }
    }

    // Tải danh sách món ăn từ CSDL vào bảng
    private void loadFoods() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        Connection conn = null; Statement st = null; ResultSet rs = null;
        String sql = "SELECT f.FoodID, f.FoodName, ISNULL(c.CategoryName, N'Chưa phân loại') AS CategoryName, " +
                     "f.Price, f.Status, f.Description " +
                     "FROM Foods f LEFT JOIN Categories c ON f.CategoryID = c.CategoryID " +
                     "ORDER BY f.FoodID";
        try {
            conn = DBConnection.getConnection(); st = conn.createStatement(); rs = st.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("FoodID"));
                row.add(rs.getString("FoodName"));
                row.add(rs.getString("CategoryName"));
                row.add(String.format("%,.0f", rs.getBigDecimal("Price"))); // Định dạng giá
                row.add(rs.getString("Status"));
                row.add(rs.getString("Description"));
                tableModel.addRow(row);
            }
        } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi tải món ăn: " + ex.getMessage());
        } finally { closeDbResources(rs, st, conn); }
    }

    // Hiển thị thông tin món ăn được chọn lên form chỉnh sửa
    private void displaySelectedFoodInfo() {
        int selectedRow = tableFoods.getSelectedRow();
        if (selectedRow < 0) return; // Không có dòng nào được chọn

        isAddingNew = false; // Đang ở chế độ sửa
        int foodId = (int) tableModel.getValueAt(selectedRow, 0);
        txtFoodId.setText(String.valueOf(foodId));

        Connection conn = null; PreparedStatement pst = null; ResultSet rs = null;
        String sql = "SELECT FoodName, CategoryID, Price, Status, Description, ImagePath FROM Foods WHERE FoodID = ?";
        try {
            conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); pst.setInt(1, foodId); rs = pst.executeQuery();
            if (rs.next()) {
                txtFoodName.setText(rs.getString("FoodName"));
                txtPrice.setText(rs.getBigDecimal("Price").toPlainString());
                String currentStatus = rs.getString("Status");
                if ("Draft".equalsIgnoreCase(currentStatus)||"Unavailable".equalsIgnoreCase(currentStatus)) { comboStatus.setSelectedItem(currentStatus); } else { comboStatus.setSelectedItem("Draft"); }
                txtDescription.setText(rs.getString("Description") == null ? "" : rs.getString("Description"));
                String imagePath = rs.getString("ImagePath");
                txtImagePath.setText(imagePath == null ? "" : imagePath);
                updateImagePreview(imagePath); // Hiển thị ảnh

                // Chọn Category trong ComboBox
                int categoryIdDb = rs.getInt("CategoryID"); CategoryItem itemToSelect = null;
                if (!rs.wasNull()) { for (CategoryItem item : categoryItems) { if (item.getId() == categoryIdDb) { itemToSelect = item; break; } } }
                if (itemToSelect == null) { comboCategory.setSelectedIndex(0); } else { comboCategory.setSelectedItem(itemToSelect); }

                // Cập nhật và kích hoạt các nút
                updateToggleButton(currentStatus);
                btnTogglePublish.setEnabled(true);
                btnSave.setEnabled(true); btnDelete.setEnabled(true); btnSave.setText("Lưu");
            } else { showError("Không tìm thấy chi tiết món ăn ID " + foodId); clearFieldsForNew(); }
        } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi tải chi tiết món ăn ID " + foodId);
        } finally { closeDbResources(rs, pst, conn); }
    }

    // Cập nhật ảnh xem trước
    private void updateImagePreview(String imagePath) {
        ImageIcon icon = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                 File imgFile = new File(imagePath);
                 if(imgFile.exists() && imgFile.isFile()){ icon = new ImageIcon(imagePath); }
                 else if (imagePath.toLowerCase().startsWith("http")) { icon = new ImageIcon(new URL(imagePath)); }
                 else { URL imgUrl = getClass().getResource(imagePath); if (imgUrl != null) { icon = new ImageIcon(imgUrl); } }

                if (icon != null && icon.getIconWidth() > 0 ) { // Kiểm tra icon hợp lệ
                     Image img = icon.getImage();
                     // Tính toán kích thước phù hợp với JLabel preview
                     int lblW = lblImagePreview.getPreferredSize().width - 10;
                     int lblH = lblImagePreview.getPreferredSize().height - 10;
                     int imgW = icon.getIconWidth(); int imgH = icon.getIconHeight();
                     int newW = lblW; int newH = (int) (((double)lblW / imgW) * imgH);
                     if(newH > lblH) { newH = lblH; newW = (int) (((double)lblH / imgH) * imgW); }
                     if(newW <= 0 || newH <= 0) {newW = 64; newH=64;} // Kích thước tối thiểu nếu tính toán lỗi
                    Image resizedImg = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(resizedImg)); lblImagePreview.setText(null); return;
                }
            } catch (Exception e) { System.err.println("Lỗi tải ảnh xem trước: " + imagePath + " - " + e.getMessage()); }
        }
        lblImagePreview.setIcon(null); lblImagePreview.setText("Không có ảnh"); // Reset nếu lỗi hoặc không có đường dẫn
    }

    // Xóa trắng các trường và chuẩn bị cho việc thêm mới
    private void clearFieldsForNew() {
        isAddingNew = true;
        txtFoodId.setText(""); txtFoodName.setText(""); txtDescription.setText("");
        txtPrice.setText(""); txtImagePath.setText("");
        comboStatus.setSelectedItem("Draft");
        comboCategory.setSelectedIndex(0); // Chọn mục mặc định
        lblImagePreview.setIcon(null); lblImagePreview.setText("Xem trước ảnh"); // Xóa ảnh
        tableFoods.clearSelection(); // Bỏ chọn dòng trên bảng
        txtFoodName.requestFocus(); // Focus vào tên món
        btnSave.setEnabled(true); btnDelete.setEnabled(false); btnTogglePublish.setEnabled(false); // Vô hiệu hóa Xóa và Đăng/Ngừng
        btnSave.setText("Thêm"); // Đổi tên nút Save thành Thêm
    }

    //--- Các phương thức xử lý sự kiện nút ---

    // Mở hộp thoại chọn file ảnh
    private void browseImage() {
         JFileChooser fileChooser = new JFileChooser();
         FileNameExtensionFilter filter = new FileNameExtensionFilter("Hình ảnh", "jpg", "jpeg", "png", "gif", "bmp");
         fileChooser.setFileFilter(filter);
         int result = fileChooser.showOpenDialog(this);
         if (result == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             txtImagePath.setText(selectedFile.getAbsolutePath()); // Lấy đường dẫn tuyệt đối
             updateImagePreview(selectedFile.getAbsolutePath()); // Hiển thị preview ngay
         }
     }

    // Lưu thông tin món ăn (Thêm mới hoặc Cập nhật)
    private void saveFood() {
        // 1. Lấy và kiểm tra dữ liệu
        String foodName = txtFoodName.getText().trim(); String description = txtDescription.getText().trim(); String priceStr = txtPrice.getText().trim(); String imagePath = txtImagePath.getText().trim();
        String statusToSave = (String) comboStatus.getSelectedItem(); // Draft hoặc Unavailable
        CategoryItem selectedCategory = (CategoryItem) comboCategory.getSelectedItem();
        BigDecimal price; Integer categoryId = null;

        if (foodName.isEmpty() || priceStr.isEmpty() || statusToSave == null) { showError("Tên món, Giá, Trạng thái Lưu không trống."); return; }
        try { price = new BigDecimal(priceStr); if (price.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { showError("Giá phải là số không âm."); txtPrice.requestFocus(); return; }
        if (selectedCategory != null && selectedCategory.getId() > 0) { categoryId = selectedCategory.getId(); }

        // 2. Chuẩn bị SQL
        Connection conn = null; PreparedStatement pst = null; String sql; int generatedFoodId = -1;

        if (isAddingNew) {
            sql = "INSERT INTO Foods (FoodName, Description, Price, ImagePath, Status, CategoryID) VALUES (?, ?, ?, ?, ?, ?)";
            statusToSave = "Draft"; // Mặc định là Draft khi thêm
        } else {
             if (txtFoodId.getText().isEmpty()) { showError("Chọn món ăn để cập nhật."); return; }
             sql = "UPDATE Foods SET FoodName = ?, Description = ?, Price = ?, ImagePath = ?, Status = ?, CategoryID = ? WHERE FoodID = ?";
        }

        // 3. Thực thi SQL
        try {
            conn = DBConnection.getConnection(); conn.setAutoCommit(false);

            if (isAddingNew) { pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); }
            else { pst = conn.prepareStatement(sql); }

            pst.setString(1, foodName); pst.setString(2, description.isEmpty() ? null : description); pst.setBigDecimal(3, price); pst.setString(4, imagePath.isEmpty() ? null : imagePath); pst.setString(5, statusToSave);
            if (categoryId == null) { pst.setNull(6, Types.INTEGER); } else { pst.setInt(6, categoryId); }
            if (!isAddingNew) { pst.setInt(7, Integer.parseInt(txtFoodId.getText())); }

            int rowsAffected = pst.executeUpdate();

            // 4. Xử lý kết quả
            if (rowsAffected > 0) {
                if (isAddingNew) {
                    ResultSet generatedKeys = pst.getGeneratedKeys();
                    if (generatedKeys.next()) { generatedFoodId = generatedKeys.getInt(1); }
                    generatedKeys.close();
                }
                conn.commit(); showMessage((isAddingNew ? "Thêm" : "Cập nhật") + " thành công!", "Thành công", 1);
                loadFoods(); // Tải lại bảng
                int idToSelect = isAddingNew ? generatedFoodId : Integer.parseInt(txtFoodId.getText());
                if (idToSelect > 0) { findAndSelectFood(idToSelect); } else { clearFieldsForNew(); } // Chọn lại món vừa thêm/sửa
                isAddingNew = false; btnSave.setText("Lưu"); // Đặt lại trạng thái nút
            } else { conn.rollback(); showError((isAddingNew ? "Thêm" : "Cập nhật") + " thất bại."); }
        } catch (SQLException ex) { try { if(conn != null) conn.rollback(); } catch(SQLException se) {} ex.printStackTrace(); if(ex.getMessage().contains("UNIQUE KEY")){showError("Tên món ăn đã tồn tại.");} else {showError("Lỗi CSDL: "+ex.getMessage());}
        } finally { closeDbResources(null, pst, conn); } // Đóng PreparedStatement và Connection
    }

    // Tìm và chọn dòng trên bảng theo ID
    private void findAndSelectFood(int foodId) {
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             Object idObj = tableModel.getValueAt(i, 0);
             if (idObj != null && idObj instanceof Integer && ((Integer) idObj) == foodId) {
                 tableFoods.setRowSelectionInterval(i, i);
                 tableFoods.scrollRectToVisible(tableFoods.getCellRect(i, 0, true));
                 // Listener sẽ tự gọi displaySelectedFoodInfo()
                 return;
             }
         }
         System.err.println("Không tìm thấy FoodID " + foodId + " trong bảng sau khi tải lại.");
         clearFieldsForNew(); // Reset nếu không tìm thấy
    }

    // Cập nhật trạng thái Đăng/Ngừng bán
    private void togglePublishStatus() {
        int selectedRow = tableFoods.getSelectedRow();
        if (selectedRow < 0) { showError("Vui lòng chọn một món ăn từ bảng."); return; }
        if (txtFoodId.getText().isEmpty()) { showError("Không tìm thấy ID của món ăn đã chọn."); return; }

        int foodId = Integer.parseInt(txtFoodId.getText());
        String currentStatus = "";
        Object statusObj = tableModel.getValueAt(selectedRow, 4); // Cột 4 là Status
         if (statusObj != null) { currentStatus = statusObj.toString(); }
         else { showError("Không xác định được trạng thái hiện tại."); return; }

        String newStatus = "Published".equalsIgnoreCase(currentStatus) ? "Unavailable" : "Published"; // Chuyển đổi trạng thái
        String actionText = "Published".equalsIgnoreCase(currentStatus) ? "Ngừng bán" : "Đăng bán";

        int confirmation = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn '" + actionText + "' món ăn này?\nTrạng thái sẽ đổi thành: " + newStatus,"Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            Connection conn = null; PreparedStatement pst = null; String sql = "UPDATE Foods SET Status = ? WHERE FoodID = ?";
            try {
                conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql);
                pst.setString(1, newStatus); pst.setInt(2, foodId);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    showMessage("'" + actionText + "' thành công!", "Thành công", 1);
                    loadFoods(); // Tải lại bảng
                    // Chọn lại dòng vừa cập nhật để thấy thay đổi trạng thái và nút
                    findAndSelectFood(foodId);
                } else { showError("'" + actionText + "' thất bại."); }
            } catch (SQLException ex) { ex.printStackTrace(); showError("Lỗi CSDL khi cập nhật trạng thái: " + ex.getMessage());
            } finally { closeDbResources(null, pst, conn); }
        }
    }

     // Cập nhật text và màu nút Đăng/Ngừng bán
     private void updateToggleButton(String currentStatus) {
         if ("Published".equalsIgnoreCase(currentStatus)) {
             btnTogglePublish.setText("Ngừng bán");
             btnTogglePublish.setBackground(new Color(255, 193, 7)); // Màu vàng cam
             btnTogglePublish.setToolTipText("Thay đổi trạng thái thành Unavailable");
         } else { // Draft or Unavailable
             btnTogglePublish.setText("Đăng bán");
             btnTogglePublish.setBackground(new Color(0, 123, 255)); // Màu xanh dương
             btnTogglePublish.setToolTipText("Thay đổi trạng thái thành Published");
         }
     }

    // Xóa món ăn
    private void deleteFood() {
        if (txtFoodId.getText().isEmpty()) { showError("Chọn món ăn để xóa."); return; }
        int foodId = Integer.parseInt(txtFoodId.getText()); String foodName = txtFoodName.getText();
        int confirmation = JOptionPane.showConfirmDialog(this, "Xóa món '" + foodName + "' (ID: " + foodId + ")?\nCác đơn hàng liên quan có thể bị ảnh hưởng (trở thành NULL).","Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            Connection conn = null; PreparedStatement pst = null; String sql = "DELETE FROM Foods WHERE FoodID = ?";
            try {
                conn = DBConnection.getConnection(); pst = conn.prepareStatement(sql); pst.setInt(1, foodId);
                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) { showMessage("Xóa món ăn thành công!", "Thành công", 1); loadFoods(); clearFieldsForNew(); }
                else { showError("Xóa thất bại. Món ăn có thể đã bị xóa."); }
            } catch (SQLException ex) { ex.printStackTrace(); if (ex.getMessage().contains("REFERENCE constraint")) { showError("Không thể xóa món ăn đã có trong đơn hàng (kiểm tra lại ràng buộc FK)."); } else { showError("Lỗi CSDL khi xóa: " + ex.getMessage()); }
            } finally { closeDbResources(null, pst, conn); }
        }
    }

    //--- Phương thức tiện ích ---
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    private void showMessage(String message, String title, int messageType) { JOptionPane.showMessageDialog(this, message, title, messageType); }

    // Phương thức đóng tài nguyên CSDL chung
    private void closeDbResources(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignored */ }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { /* ignored */ }
        try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
    }


    //--- Main (Để test) ---
    public static void main(String[] args) {
         applyStaticNimbusLookAndFeel();
         SwingUtilities.invokeLater(() -> new ManageFoodForm().setVisible(true));
     }
      private static void applyStaticNimbusLookAndFeel() {
          try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
          catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
      }

} 