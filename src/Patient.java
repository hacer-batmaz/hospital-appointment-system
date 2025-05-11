import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Patient extends User{
    private String phoneNumber;

    public Patient() {
    }

    public Patient(int id, String userName, String password, String phoneNumber) {
        super(id, userName, password);
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void showDashboard() {
        System.out.println("Patient panel opens...");
    }

    public boolean isStatusAvailable(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isAvailable = false;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT appointment_status FROM appointments WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();//sorgu calisir

            if (rs.next()){
                String status = rs.getString("appointment_status");
                isAvailable = "available".equalsIgnoreCase(status);

                JOptionPane.showMessageDialog(null, "ID: " + id + "\nStatus: " + status + "\n\nAvailability: " + (isAvailable ? "AVAILABLE (Available)" : "NOT AVAILABLE"),
                        "Status Result", isAvailable ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No record found with ID: " + id, "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return isAvailable;
    }
}