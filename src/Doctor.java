import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Doctor extends User{
    private String specialization;//uzmalik alani

    public Doctor() {
    }

    public Doctor(int id, String userName, String password, String specialization) {
        super(id, userName, password);
        this.specialization = specialization;
    }

    @Override
    public void showDashboard() {
        String[] options = {
                "1. List Available Appointments",
                "2. Listing Upcoming Appointments"};

        StringBuilder menu = new StringBuilder("Select the action you want to perform:\n");
        for (String option : options) {
            menu.append(option).append("\n");
        }

        String inputStr = JOptionPane.showInputDialog(null, menu.toString());
        if (inputStr == null) {
            JOptionPane.showMessageDialog(null, "Operation canceled by user.","Information",JOptionPane.INFORMATION_MESSAGE);
            menu();
            return;
        }
        if (!inputStr.matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "Only numbers are allowed!", "Error", JOptionPane.ERROR_MESSAGE);
            showDashboard();
            return;
        }

        try {
            int input = Integer.parseInt(inputStr);
            switch (input) {
                case 1:
                    getAvailableAppointments();
                    menu();
                    break;
                case 2:
                    listUpcomingAppointments();
                    menu();
                    break;
                default:
                    JOptionPane.showMessageDialog(null,"Please enter 1 or 2.","Warning",JOptionPane.WARNING_MESSAGE);
                    showDashboard();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,"Please enter a valid number.","Error",JOptionPane.ERROR_MESSAGE);
            showDashboard();
        }
    }

    private void menu() {
        int choice = JOptionPane.showConfirmDialog(null, "Do you want to take another action?" ,"Next Action",JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION)
            showDashboard();
        else
            JOptionPane.showMessageDialog(null, "The transaction is complete. You have logged out.","Logout",JOptionPane.INFORMATION_MESSAGE);
    }

    public void getAvailableAppointments() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String input = JOptionPane.showInputDialog(null, "Enter the ID of the doctor to list available appointments:",
                    "Doctor ID Input", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            int id = Integer.parseInt(input);

            conn = DBConnection.connect();
            String sql = "SELECT a.id, a.appointment_date, a.appointment_start_time, " +
                    "a.appointment_end_time " +
                    "FROM appointments a " +
                    "WHERE a.doctor_id = ? AND a.appointment_status = 'Available' " +
                    "ORDER BY a.appointment_date, a.appointment_start_time";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h2>Available Appointments</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>ID</th><th>Date</th><th>Time Slot</th>");

            boolean hasAppointments = false;//available yoksa diye

            while(rs.next()) {
                hasAppointments = true;
                message.append("<tr>");
                message.append("<td>").append(rs.getInt("id")).append("</td>");
                message.append("<td>").append(rs.getDate("appointment_date")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_start_time"))
                        .append(" - ").append(rs.getTime("appointment_end_time")).append("</td>");
            }
            message.append("</table>");

            if (!hasAppointments) {
                message.append("No available appointments found.");//available hasta yok
            }
            JOptionPane.showMessageDialog(null, message.toString(), "Available Appointments for Doctor ID: " + id,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid input! Please enter a numeric ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error retrieving appointments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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

    public void listUpcomingAppointments() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT a.id, a.appointment_date, a.appointment_start_time, a.appointment_end_time, " +
                    "u.user_name AS patient_name " +
                    "FROM appointments a " +
                    "JOIN users u ON a.patient_id = u.id " +
                    "WHERE a.doctor_id = ? AND a.appointment_date >= CURDATE() " +
                    "ORDER BY a.appointment_date, a.appointment_start_time";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1,String.valueOf(getId()));
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h3>Upcoming Appointments</h3>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>ID</th><th>Date</th><th>Beginning</th><th>Finish</th><th>Patient</th></tr>");

            boolean hasAppointments = false;

            while (rs.next()) {
                hasAppointments = true;
                message.append("<tr>");
                message.append("<td>").append(rs.getInt("id")).append("</td>");
                message.append("<td>").append(rs.getDate("appointment_date")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_start_time")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_end_time")).append("</td>");
                message.append("<td>").append(rs.getString("patient_name")).append("</td>");
                message.append("</tr>");
            }

            message.append("</table></html>");

            if (!hasAppointments) {
                JOptionPane.showMessageDialog(null, "No next appointment found.", "Appointments", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, message.toString(), "Upcoming Appointments", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while booking appointments.", "Error", JOptionPane.ERROR_MESSAGE);
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}