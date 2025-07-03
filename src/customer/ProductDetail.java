package customer;

import connect.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ProductDetail extends JFrame {
    public ProductDetail(String username, int foodId) {
        setTitle("Chi tiết món ăn");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.setBackground(Color.WHITE);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM Foods WHERE FoodID = ?")) {

            pst.setInt(1, foodId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String name = rs.getString("FoodName");
                double price = rs.getDouble("Price");
                String description = rs.getString("Description");
                String imgPath = rs.getString("ImagePath");

                JLabel lblImg = new JLabel();
                lblImg.setAlignmentX(Component.CENTER_ALIGNMENT);
                if (imgPath != null && !imgPath.isEmpty()) {
                    ImageIcon icon = new ImageIcon(imgPath);
                    Image scaled = icon.getImage().getScaledInstance(200, 140, Image.SCALE_SMOOTH);
                    lblImg.setIcon(new ImageIcon(scaled));
                } else {
                    lblImg.setText("Không có ảnh");
                }
                content.add(lblImg);

                JLabel lblName = new JLabel(name);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
                content.add(lblName);

                JLabel lblPrice = new JLabel(String.format("%,.0f VNĐ", price));
                lblPrice.setForeground(Color.RED);
                lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
                content.add(lblPrice);

                JTextArea txtDesc = new JTextArea(description);
                txtDesc.setLineWrap(true);
                txtDesc.setWrapStyleWord(true);
                txtDesc.setEditable(false);
                txtDesc.setBorder(BorderFactory.createTitledBorder("Mô tả"));
                content.add(txtDesc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu món ăn", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        add(content, BorderLayout.CENTER);
    }
}
