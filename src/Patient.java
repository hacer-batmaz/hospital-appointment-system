import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
        String[] options = {
                "1. List Doctors",
                "2. List Past Appointments",
                "3. List Future Appointments",
                "4. Make an Appointment"};

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
                    listDoctor();
                    menu();
                    break;
                case 2:
                    listPastAppointments();
                    menu();
                    break;
                case 3:
                    listUpcomingAppointments();
                    menu();
                    break;
                case 4:
                    bookAppointment();
                    break;
                default:
                    JOptionPane.showMessageDialog(null,"Please enter a number between 1 and 4.","Warning",JOptionPane.WARNING_MESSAGE);
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

                JOptionPane.showMessageDialog(null, "ID: " + id + "\nStatus: " + status + "\n\nAvailability: " +
                                (isAvailable ? "AVAILABLE (Available)" : "NOT AVAILABLE"), "Status Result", isAvailable ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
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

    public void listUpcomingAppointments() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT a.id, a.appointment_date, a.appointment_start_time, " +
                    "a.appointment_end_time, u.user_name AS doctor_name " +
                    "FROM appointments a " +
                    "JOIN users u ON a.doctor_id = u.id " +
                    "WHERE a.patient_id = ? " +
                    "AND a.appointment_status = 'Approved' " +
                    "AND a.appointment_date >= CURDATE() " +
                    "ORDER BY a.appointment_date, a.appointment_start_time";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1,this.getId());
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h2>Upcoming Appointments</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>Date</th><th>Start</th><th>End</th><th>Doctor</th></tr>");

            boolean hasAppointments = false;

            while (rs.next()) {
                hasAppointments = true;
                message.append("<tr>");
                message.append("<td>").append(rs.getDate("appointment_date")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_start_time")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_end_time")).append("</td>");
                message.append("<td>").append(rs.getString("doctor_name")).append("</td>");
                message.append("</tr>");
            }

            message.append("</table></html>");

            if (hasAppointments)
                JOptionPane.showMessageDialog(null, message.toString(), "Upcoming Appointments", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(null, "No upcoming appointments found.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
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

    public void listPastAppointments() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT a.id, a.appointment_date, a.appointment_start_time, " +
                    "a.appointment_end_time, u.user_name AS doctor_name " +
                    "FROM appointments a " +
                    "JOIN users u ON a.doctor_id = u.id " +
                    "WHERE a.patient_id = ? " +
                    "AND a.appointment_status = 'Completed' " +
                    "AND a.appointment_date < CURDATE() " +
                    "ORDER BY a.appointment_date DESC, a.appointment_start_time DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1,this.getId());
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h2>Past Appointments</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>Date</th><th>Start</th><th>End</th><th>Doctor</th></tr>");

            boolean hasAppointments = false;

            while (rs.next()) {
                hasAppointments = true;
                message.append("<tr>");
                message.append("<td>").append(rs.getDate("appointment_date")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_start_time")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_end_time")).append("</td>");
                message.append("<td>").append(rs.getString("doctor_name")).append("</td>");
                message.append("</tr>");
            }

            message.append("</table></html>");

            if (hasAppointments) {
                JOptionPane.showMessageDialog(null, message.toString(), "Past Appointments", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No past appointments found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving past appointments.", "Database Error", JOptionPane.ERROR_MESSAGE);
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

    public void bookAppointment() {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            //hastaya uygun olan, yani 'Available' durumundaki randevulari listele
            String sql = "SELECT a.id, u.user_name AS doctor_name, a.appointment_date, a.appointment_start_time, a.appointment_end_time " +
                    "FROM appointments a " +
                    "JOIN users u ON a.doctor_id = u.id " +
                    "WHERE a.appointment_status = 'Available' " +
                    "AND a.appointment_date >= CURDATE() " +
                    "ORDER BY a.appointment_date, a.appointment_start_time";
            selectStmt = conn.prepareStatement(sql);
            rs = selectStmt.executeQuery();

            Map<Integer, Integer> rowToAppointmentId = new HashMap<>();
            StringBuilder message = new StringBuilder("<html><h2>Available Appointments</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>#</th><th>Doctor</th><th>Date</th><th>Start</th><th>End</th></tr>");

            int count = 1;
            while (rs.next()) {
                int appointmentId = rs.getInt("id");
                rowToAppointmentId.put(count, appointmentId);

                message.append("<tr>");
                message.append("<td>").append(count).append("</td>");
                message.append("<td>").append(rs.getString("doctor_name")).append("</td>");
                message.append("<td>").append(rs.getDate("appointment_date")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_start_time")).append("</td>");
                message.append("<td>").append(rs.getTime("appointment_end_time")).append("</td>");
                message.append("</tr>");

                count++;
            }
            message.append("</table></html>");

            if (rowToAppointmentId.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No suitable appointment found.", "Make an Appointment", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            //kullanicidan secim al
            String input = JOptionPane.showInputDialog(null, message.toString() + "\nPlease enter the number of the appointment you wish to make:",
                    "Make an Appointment", JOptionPane.INFORMATION_MESSAGE);
            if (input == null) return; // İptal etti
            int selectedRow = Integer.parseInt(input);

            if (!rowToAppointmentId.containsKey(selectedRow)) {
                JOptionPane.showMessageDialog(null, "Invalid selection.", "Make an Appointment", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int selectedAppointmentId = rowToAppointmentId.get(selectedRow);

            //randevuyu hastaya ata
            String updateSql = "UPDATE appointments SET patient_id = ?, appointment_status = 'Approved' WHERE id = ? AND appointment_status = 'Available'";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, this.getId());
            updateStmt.setInt(2, selectedAppointmentId);

            int updated = updateStmt.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(null, "The appointment was made successfully.", "Make an Appointment", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Appointment not available. The appointment you selected is no longer available.",
                        "Make an Appointment", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while making an appointment. ", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (selectStmt != null) selectStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}