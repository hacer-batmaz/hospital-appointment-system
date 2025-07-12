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

    public void listDoctor() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT u.user_name, d.specialization FROM users u JOIN doctors d ON u.id = d.id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h2>Doctors</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>Name</th><th>Specialization</th>");

            boolean hasDoctors = false;

            while (rs.next()) {
                message.append("<tr>");
                message.append("<td>").append(rs.getString("user_name")).append("</td>");
                message.append("<td>").append(rs.getString("specialization")).append("</td>");

            }
            message.append("</table>");

            if (hasDoctors) {
                JOptionPane.showMessageDialog(null, "No doctors found.", "Doctors", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, message.toString(), "Doctors", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while listing doctors.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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