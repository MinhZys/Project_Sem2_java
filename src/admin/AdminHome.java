package admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import view.LoginForm;
import admin.ManageUsersForm; // Đổi lại đường dẫn nếu cần
import admin.ManageFoodForm;  // Đổi lại đường dẫn nếu cần
import admin.ManageCategoriesForm; // *** Import form quản lý danh mục ***
// import view.admin.RevenueReportForm;

public class AdminHome extends JFrame {

    // --- Constructor và applyNimbusLookAndFeel() giữ nguyên ---
     public AdminHome() {
        applyNimbusLookAndFeel();
        setTitle("Trang quản trị (Admin)");
        setSize(750, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setJMenuBar(createMenuBar());
        JLabel lblWelcome = new JLabel("Chào mừng Admin!", JLabel.CENTER);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblWelcome.setForeground(new Color(0, 102, 204));
        add(lblWelcome, BorderLayout.CENTER);
    }
     private void applyNimbusLookAndFeel() {
        try { for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; } } }
        catch (Exception ex) { try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); } }
    }


    // --- SỬA LẠI createMenuBar() ---
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Font menuFont = new Font("Segoe UI", Font.PLAIN, 14);

        // --- Menu: Quản lý ---
        JMenu menuManage = new JMenu("Quản lý");
        menuManage.setFont(menuFont);

        // Mục Quản lý người dùng
        JMenuItem itemManageUsers = new JMenuItem("Quản lý người dùng");
        itemManageUsers.setFont(menuFont);
        itemManageUsers.setIcon(loadIcon("/icons/users_16.jpg")); // Giữ icon cũ hoặc đổi
        itemManageUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_DOWN_MASK));
        itemManageUsers.addActionListener(e -> openManageUsersForm());

        // Mục Quản lý món ăn
        JMenuItem itemManageFood = new JMenuItem("Quản lý món ăn");
        itemManageFood.setFont(menuFont);
        itemManageFood.setIcon(loadIcon("/icons/food_16.jpg")); // Giữ icon cũ hoặc đổi
        itemManageFood.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.ALT_DOWN_MASK));
        itemManageFood.addActionListener(e -> openManageFoodForm());

        // *** THÊM MỤC QUẢN LÝ DANH MỤC ***
        JMenuItem itemManageCategories = new JMenuItem("Quản lý danh mục");
        itemManageCategories.setFont(menuFont);
        itemManageCategories.setIcon(loadIcon("/icons/doanhmuc.png")); // *** Đặt icon cho danh mục ***
        itemManageCategories.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK)); // Phím tắt Alt+C
        itemManageCategories.addActionListener(e -> openManageCategoriesForm()); // *** Gán sự kiện mới ***
        // *** --- ***

        menuManage.add(itemManageUsers);
        menuManage.add(itemManageFood);
        menuManage.add(itemManageCategories); // *** Thêm mục mới vào menu ***
        menuBar.add(menuManage);

        // --- Menu: Báo cáo (Giữ nguyên) ---
        JMenu menuReport = new JMenu("Báo cáo"); menuReport.setFont(menuFont);
        JMenuItem itemViewReports = new JMenuItem("Xem báo cáo doanh thu"); itemViewReports.setFont(menuFont); itemViewReports.setIcon(loadIcon("/icons/report_16.png")); itemViewReports.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK)); itemViewReports.addActionListener(e -> openRevenueReportForm());
        menuReport.add(itemViewReports); menuBar.add(menuReport);

        // --- Menu: Hệ thống (Giữ nguyên) ---
        JMenu menuSystem = new JMenu("Hệ thống"); menuSystem.setFont(menuFont);
        JMenuItem itemLogout = new JMenuItem("Đăng xuất"); itemLogout.setFont(menuFont); itemLogout.setIcon(loadIcon("/icons/logout_16.png")); itemLogout.addActionListener(e -> logout());
        JMenuItem itemExit = new JMenuItem("Thoát"); itemExit.setFont(menuFont); itemExit.setIcon(loadIcon("/icons/exit_16.png")); itemExit.addActionListener(e -> exitApplication());
        menuSystem.add(itemLogout); menuSystem.addSeparator(); menuSystem.add(itemExit); menuBar.add(menuSystem);

        return menuBar;
    }

    // --- Phương thức loadIcon (Giữ nguyên) ---
    private ImageIcon loadIcon(String path, int size) { /* Giữ nguyên */ try { URL imgURL = getClass().getResource(path); if (imgURL != null) { ImageIcon o = new ImageIcon(imgURL); Image i = o.getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH); return new ImageIcon(i); } else { System.err.println("Icon not found: "+path); return null; } } catch(Exception e){ System.err.println("Icon loading error: "+path+" - "+e); return null; } }
    private ImageIcon loadIcon(String path) { return loadIcon(path, 16); }

    // --- Các phương thức xử lý sự kiện (Thêm phương thức mở form danh mục) ---
    private void openManageUsersForm() { new ManageUsersForm().setVisible(true); dispose(); }
    private void openManageFoodForm() { new ManageFoodForm().setVisible(true); dispose(); }
    private void openRevenueReportForm() { JOptionPane.showMessageDialog(this,"Chức năng báo cáo chưa triển khai.","Thông báo",1); }
    private void logout() { /* Giữ nguyên */ int c = JOptionPane.showConfirmDialog(this,"Đăng xuất?","Xác nhận",0,3); if(c==0){new LoginForm().setVisible(true); dispose();} }
    private void exitApplication() { /* Giữ nguyên */ int c = JOptionPane.showConfirmDialog(this,"Thoát ứng dụng?","Xác nhận",0,2); if(c==0){System.exit(0);} }

    // *** PHƯƠNG THỨC MỚI ĐỂ MỞ FORM DANH MỤC ***
    private void openManageCategoriesForm() {
        new ManageCategoriesForm().setVisible(true);
        dispose(); // Đóng cửa sổ AdminHome
    }
    // *** --- ***

    // --- Main method (Giữ nguyên) ---
    public static void main(String[] args) { /* Giữ nguyên */ SwingUtilities.invokeLater(()->new AdminHome().setVisible(true)); }
}