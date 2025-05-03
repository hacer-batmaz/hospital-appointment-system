import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Admin extends User{
    public Admin() {
        super();
    }

    public Admin(int id, String userName, String password) {
        super(id, userName, password);
    }

    @Override
    public void showDashboard() {
        System.out.println("Admin panel opens...");
    }

    public boolean addDoctor(Doctor doctor) {
        Connection conn = null;
        PreparedStatement userPstmt = null;
        PreparedStatement doctorPstmt = null;

        try {
            conn = DBConnection.connect();//veritabani baglantisi acildi
            conn.setAutoCommit(false);//auto-commit i kapat
            String userSql = "INSERT INTO users (id, user_name , password) VALUES (?, ?, ?)";

            userPstmt = conn.prepareStatement(userSql);

            //PreparedStatement parametreleri ayarlandi
            userPstmt.setInt(1, doctor.getId());
            userPstmt.setString(2, doctor.getUserName());
            userPstmt.setString(3, doctor.getPassword());

            int userAffectedRows = userPstmt.executeUpdate();//sorguyu calistir

            if (userAffectedRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,"User could not be added!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String doctorSql = "INSERT INTO doctors (id, specialization) VALUES (?, ?)";
            doctorPstmt = conn.prepareStatement(doctorSql);
            doctorPstmt.setInt(1, doctor.getId());
            doctorPstmt.setString(2, doctor.getSpecialization());

            int doctorAffectedRows = doctorPstmt.executeUpdate();

            if (doctorAffectedRows > 0) {
                conn.commit();
                JOptionPane.showMessageDialog(null, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                conn.rollback(); //hata durumunda geri al
                JOptionPane.showMessageDialog(null, "Doctor could not be added!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Hata durumunda transaction'ı geri al
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }

            String errorMsg = "An error occurred while adding a doctor:\n";
            if (e.getMessage().contains("foreign key constraint fails")) {//yabanci anahtar kisitlamasi basarisiz
                errorMsg += "The user must be added first in the users table.";
            } else if (e.getMessage().contains("Duplicate entry")) {//yinelenen giris
                errorMsg += "A doctor with this ID already exists.";
            } else {
                errorMsg += e.getMessage();
            }

            JOptionPane.showMessageDialog(null, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try { //kaynaklari kapat
                if (userPstmt != null) userPstmt.close();
                if (doctorPstmt != null) doctorPstmt.close();
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                    } catch (SQLException e) {
                        System.err.println("Error resetting autocommit: " + e.getMessage());
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
