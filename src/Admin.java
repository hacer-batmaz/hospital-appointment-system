import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Admin extends User{
    public Admin(int id, String userName, String password) {
        super(id, userName, password);
    }

    @Override
    public void showDashboard() {
        System.out.println("Admin panel opens...");
    }

    public void addDoctor(Doctor doctor) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.connect();//veritabani baglantisi acildi
            String sql = "INSERT INTO doctors (id, password, specialization) VALUES (?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            //PreparedStatement parametreleri ayarlandi
            pstmt.setInt(1, doctor.getId());
            pstmt.setString(2, doctor.getPassword());
            pstmt.setString(3, doctor.getUserName());

            int affectedRows = pstmt.executeUpdate();//sorguyu calistir

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null,"Doctor added successfully!", "Successful", JOptionPane.INFORMATION_MESSAGE);
                return;
            } else {
                JOptionPane.showMessageDialog(null, "Doctor could not be added!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while adding a doctor: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "An error occurred while adding a doctor:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            try { //kaynaklari kapat
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("An error occurred while closing resources: " + e.getMessage());
            }
        }
    }
}
