package customer;

import model.CategoryItem;
import view.LoginForm;
import view.OrderFoodForm;
import view.OrderHistoryForm;
import connect.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerHome extends JFrame {
    private String customerUsername;
    private JPanel productDisplayPanel;
    private JScrollPane scrollPane;
    private JTextField txtSearch;
    private JComboBox<CategoryItem> cbCategory;
    private JPanel rightDetailPanel;

    public CustomerHome(String username) {
        this.customerUsername = username;
        applyNimbusLookAndFeel();
        setTitle("FastFood Menu - Khách hàng: " + customerUsername);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setJMenuBar(createMenuBar());
        add(createTopPanel(), BorderLayout.NORTH);

        
        setupProductDisplayArea();

        
        
        rightDetailPanel = new JPanel();
        rightDetailPanel.setPreferredSize(new Dimension(300, 0));
        rightDetailPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết món"));
        rightDetailPanel.setLayout(new BoxLayout(rightDetailPanel, BoxLayout.Y_AXIS));
        rightDetailPanel.setBackground(Color.WHITE);
        add(rightDetailPanel, BorderLayout.EAST);

        loadAndDisplayPublishedFoods("", null);
    }

    private void applyNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu mFunc = new JMenu("Chức năng");

        JMenuItem iOrd = new JMenuItem("Đặt món ăn");
        iOrd.addActionListener(e -> openOrderFoodForm());
        mFunc.add(iOrd);

        JMenuItem iHist = new JMenuItem("Xem lịch sử đơn hàng");
        iHist.addActionListener(e -> openOrderHistoryForm());
        mFunc.add(iHist);
        mb.add(mFunc);

        JMenu mAcc = new JMenu("Tài khoản");
        JMenuItem iLogout = new JMenuItem("Đăng xuất");
        iLogout.addActionListener(e -> logout());
        mAcc.add(iLogout);

        JMenuItem iExit = new JMenuItem("Thoát");
        iExit.addActionListener(e -> System.exit(0));
        mAcc.add(iExit);
        mb.add(mAcc);

        return mb;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Tìm món:");
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Tìm");

        JLabel lblCategory = new JLabel("Danh mục:");
        cbCategory = new JComboBox<>();
        cbCategory.addItem(new CategoryItem(0, "Tất cả"));
        loadCategories();

        btnSearch.addActionListener(e -> performSearch());
        cbCategory.addActionListener(e -> performSearch());

        topPanel.add(lblSearch);
        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topPanel.add(txtSearch);
        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topPanel.add(btnSearch);
        topPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        topPanel.add(lblCategory);
        topPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        topPanel.add(cbCategory);

        return topPanel;
    }

    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        CategoryItem selected = (CategoryItem) cbCategory.getSelectedItem();
        Integer categoryId = (selected != null && selected.getId() != 0) ? selected.getId() : null;
        loadAndDisplayPublishedFoods(keyword, categoryId);
    }

    private void loadCategories() {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT CategoryID, CategoryName FROM Categories ORDER BY CategoryName")) {
            while (rs.next()) {
                cbCategory.addItem(new CategoryItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupProductDisplayArea() {
        productDisplayPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        productDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        productDisplayPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(productDisplayPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadAndDisplayPublishedFoods(String keyword, Integer categoryId) {
        productDisplayPanel.removeAll();

        StringBuilder sql = new StringBuilder("SELECT FoodID, FoodName, Description, Price, ImagePath FROM Foods WHERE Status = N'Published'");
        if (keyword != null && !keyword.isEmpty()) sql.append(" AND FoodName LIKE ?");
        if (categoryId != null) sql.append(" AND CategoryID = ?");
        sql.append(" ORDER BY FoodName");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (keyword != null && !keyword.isEmpty()) pst.setString(index++, "%" + keyword + "%");
            if (categoryId != null) pst.setInt(index, categoryId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("FoodID");
                String name = rs.getString("FoodName");
                String desc = rs.getString("Description");
                double price = rs.getDouble("Price");
                String img = rs.getString("ImagePath");

                productDisplayPanel.add(createFoodCard(id, name, desc, price, img));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải món ăn", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        productDisplayPanel.revalidate();
        productDisplayPanel.repaint();
    }

    private JPanel createFoodCard(int foodId, String name, String description, double price, String imagePath) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 220));

        JLabel lblImage = new JLabel("", JLabel.CENTER);
        lblImage.setPreferredSize(new Dimension(120, 80));

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(imagePath);
                Image scaled = icon.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
                lblImage.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                lblImage.setText("Ảnh lỗi");
            }
        } else {
            lblImage.setText("Không có ảnh");
        }

        card.add(lblImage, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.add(lblName);

        JLabel lblPrice = new JLabel(String.format("%,.0f VNĐ", price));
        lblPrice.setForeground(new Color(220, 53, 69));
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.add(lblPrice);

        card.add(info, BorderLayout.CENTER);

        JButton btnView = new JButton("Xem món");
        btnView.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnView.addActionListener(e -> showFoodDetail(foodId));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(Color.WHITE);
        south.add(btnView);

        card.add(south, BorderLayout.SOUTH);

        return card;
    }

    private void showFoodDetail(int foodId) {
        rightDetailPanel.removeAll();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM Foods WHERE FoodID = ?")) {

            pst.setInt(1, foodId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String name = rs.getString("FoodName");
                String desc = rs.getString("Description");
                double price = rs.getDouble("Price");
                String img = rs.getString("ImagePath");

                JLabel imgLabel = new JLabel();
                imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                imgLabel.setPreferredSize(new Dimension(200, 120));

                if (img != null && !img.isEmpty()) {
                    ImageIcon icon = new ImageIcon(img);
                    Image scaled = icon.getImage().getScaledInstance(200, 120, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(scaled));
                } else {
                    imgLabel.setText("Không có ảnh");
                }

                JLabel lblName = new JLabel(name);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

                JTextArea descArea = new JTextArea(desc);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);
                descArea.setEditable(false);
                descArea.setBorder(BorderFactory.createTitledBorder("Mô tả"));
                descArea.setBackground(Color.WHITE);

                JLabel lblPrice = new JLabel(String.format("Giá: %,.0f VNĐ", price));
                lblPrice.setForeground(Color.RED);
                lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

                JButton btnOrder = new JButton("Đặt món");
                btnOrder.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnOrder.addActionListener(e -> openOrderFoodFormWithId(foodId));

                rightDetailPanel.add(imgLabel);
                rightDetailPanel.add(Box.createVerticalStrut(10));
                rightDetailPanel.add(lblName);
                rightDetailPanel.add(Box.createVerticalStrut(10));
                rightDetailPanel.add(new JScrollPane(descArea));
                rightDetailPanel.add(Box.createVerticalStrut(10));
                rightDetailPanel.add(lblPrice);
                rightDetailPanel.add(Box.createVerticalStrut(10));
                rightDetailPanel.add(btnOrder);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rightDetailPanel.revalidate();
        rightDetailPanel.repaint();
    }

    private void openOrderFoodForm() {
        new OrderFoodForm(customerUsername).setVisible(true);
        dispose();
    }

    private void openOrderFoodFormWithId(int foodId) {
        new OrderFoodForm(customerUsername, foodId).setVisible(true);
        dispose();
    }

    private void openOrderHistoryForm() {
        new OrderHistoryForm(customerUsername).setVisible(true);
        dispose();
    }

    private void logout() {
        int c = JOptionPane.showConfirmDialog(this, "Đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            new LoginForm().setVisible(true);
            dispose();
        }
    }
}