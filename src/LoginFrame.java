import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Hospital Appointment System - Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());//ana pencereyi BorderLayout yap

        //formu tutacak panel (hala GridLayout)
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20)); //ustten bosluk

        formPanel.add(new JLabel("Username:"));
        txtUsername = new JTextField();
        formPanel.add(txtUsername);

        formPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField();
        formPanel.add(txtPassword);

        formPanel.add(new JLabel());//bos hucre

        btnLogin = new JButton("Login");
        formPanel.add(btnLogin);

        add(formPanel, BorderLayout.NORTH);//form panelini NORTH (üst) bölgeye yerleştir

        btnLogin.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {
        String userName = txtUsername.getText();
        String password = String.valueOf(txtPassword.getPassword());

        try (Connection conn = DBConnection.connect()){
            String sql = "SELECT u.id, u.user_name, u.password, " +
                    "       p.phone_number, d.specialization, " +
                    "       CASE " +
                    "           WHEN a.id IS NOT NULL THEN 'admin' " +
                    "           WHEN d.id IS NOT NULL THEN 'doctor' " +
                    "           WHEN p.id IS NOT NULL THEN 'patient' " +
                    "           ELSE NULL " +
                    "       END AS role " +
                    "FROM users u " +
                    "LEFT JOIN patients p ON u.id = p.id " +
                    "LEFT JOIN doctors d ON u.id = d.id " +
                    "LEFT JOIN admin a ON u.id = a.id " +
                    "WHERE u.user_name = ? AND u.password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");//'admin', 'doctor', 'patient'
                int id = rs.getInt("id");

                User user;
                switch (role) {
                    case "admin":
                        user = new Admin(id, userName, password);
                        break;
                    case "doctor":
                        String specialization = rs.getString("specialization");
                        user = new Doctor(id, userName, password, specialization);
                        break;
                    case "patient":
                        String phoneNumber = rs.getString("phone_number");
                        user = new Patient(id, userName, password, phoneNumber);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Invalid Role");
                        return;
                }

                JOptionPane.showMessageDialog(this, "\"Login successful! Welcome, " + role + " " + userName + ".");
                dispose();//login ekranini kapat
                user.showDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Username or password is incorrect!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error!");
        }
    }
}