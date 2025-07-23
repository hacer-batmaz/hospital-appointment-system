import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String[] options = {
                "1. List Doctors",
                "2. List Patients",
                "3. Add Doctors",
                "4. Delete Doctors",
                "5. Delete Appointment"};

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
                    listPatient();
                    menu();
                    break;
                case 3:
                    addDoctor();
                    menu();
                    break;
                case 4:
                    deleteDoctorWithPrompt();
                    menu();
                    break;
                case 5:
                    deleteAppointmentsByPatientName();
                    menu();
                    break;
                default:
                    JOptionPane.showMessageDialog(null,"Please enter a number between 1 and 5.","Warning",JOptionPane.WARNING_MESSAGE);
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

    public boolean addDoctor() {
        Connection conn = null;
        PreparedStatement userPstmt = null;
        PreparedStatement doctorPstmt = null;

        try {
            String idInput = JOptionPane.showInputDialog(null, "Enter Doctor ID:", "Doctor ID", JOptionPane.QUESTION_MESSAGE);
            if (idInput == null) return false;
            int id = Integer.parseInt(idInput.trim());

            String userName = JOptionPane.showInputDialog(null, "Enter Doctor Name:", "Doctor Name", JOptionPane.QUESTION_MESSAGE);
            if (userName == null || userName.trim().isEmpty()) return false;

            String password = JOptionPane.showInputDialog(null, "Enter Password:", "Password", JOptionPane.QUESTION_MESSAGE);
            if (password == null || password.trim().isEmpty()) return false;

            String specialization = JOptionPane.showInputDialog(null, "Enter Specialization:", "Specialization", JOptionPane.QUESTION_MESSAGE);
            if (specialization == null || specialization.trim().isEmpty()) return false;

            conn = DBConnection.connect();//veritabani baglantisi acildi
            conn.setAutoCommit(false);//auto-commit i kapat
            String userSql = "INSERT INTO users (id, user_name , password) VALUES (?, ?, ?)";

            userPstmt = conn.prepareStatement(userSql);

            //PreparedStatement parametreleri ayarlandi
            userPstmt.setInt(1, id);
            userPstmt.setString(2, userName.trim());
            userPstmt.setString(3, password.trim());

            int userAffectedRows = userPstmt.executeUpdate();//sorguyu calistir

            if (userAffectedRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,"User could not be added!", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String doctorSql = "INSERT INTO doctors (id, specialization) VALUES (?, ?)";
            doctorPstmt = conn.prepareStatement(doctorSql);
            doctorPstmt.setInt(1, id);
            doctorPstmt.setString(2, specialization.trim());

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
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid ID input! Please enter a numeric value.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
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

    public void deleteDoctorWithPrompt() {
        Connection conn = null;
        PreparedStatement checkDoctorPstmt = null;
        PreparedStatement deleteDoctorPstmt = null;
        PreparedStatement deleteUserPstmt = null;
        ResultSet rs = null;

        String input = JOptionPane.showInputDialog(null, "Enter Doctor ID to delete: ", "Delete Doctor", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Operation cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int doctorId;
        try {
            doctorId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid ID format: Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            conn = DBConnection.connect();

            String checkDoctorSql = "SELECT * FROM doctors WHERE id = ?";
            checkDoctorPstmt = conn.prepareStatement(checkDoctorSql);
            checkDoctorPstmt.setInt(1, doctorId);
            rs = checkDoctorPstmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Doctor with ID " + doctorId + " not found!", "Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //sadece doktor varsa sorulacak
            int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to delete doctor with ID " + doctorId + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            conn.setAutoCommit(false);

            String deleteDoctorSql = "DELETE FROM doctors WHERE id = ?";
            deleteDoctorPstmt = conn.prepareStatement(deleteDoctorSql);
            deleteDoctorPstmt.setInt(1, doctorId);

            int doctorAffectedRows = deleteDoctorPstmt.executeUpdate();
            if (doctorAffectedRows == 0) {
                conn.rollback(); //teorik olarak gerek yok ama güvenli
                JOptionPane.showMessageDialog(null, "Doctor could not be deleted.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //users tablosundan da silinsin mi
            int adminChoice = JOptionPane.showConfirmDialog(null,
                    "Doctor with ID " + doctorId + " has been deleted from doctors table.\nDo you also want to delete from users table?",
                    "Confirm User Deletion", JOptionPane.YES_NO_OPTION);

            if (adminChoice == JOptionPane.YES_OPTION) {
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                deleteUserPstmt = conn.prepareStatement(deleteUserSql);
                deleteUserPstmt.setInt(1, doctorId);

                int userAffectedRows = deleteUserPstmt.executeUpdate();
                if (userAffectedRows > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(null,
                            "Doctor with ID " + doctorId + " has been completely deleted from both tables!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null,
                            "Doctor was deleted from doctors table but could not be deleted from users table!",
                            "Partial Success", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                conn.commit();
                JOptionPane.showMessageDialog(null,
                        "Doctor with ID " + doctorId + " has been deleted only from doctors table.",
                        "Partial Deletion", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }

            String errMessage = "An error occurred while deleting the doctor:\n";
            if (e.getMessage().contains("foreign key constraint fails")) {
                errMessage += "This doctor cannot be deleted because they have related records in other tables.";
            } else {
                errMessage += e.getMessage();
            }

            JOptionPane.showMessageDialog(null, errMessage, "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkDoctorPstmt != null) checkDoctorPstmt.close();
                if (deleteDoctorPstmt != null) deleteDoctorPstmt.close();
                if (deleteUserPstmt != null) deleteUserPstmt.close();
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

    public void deleteAppointmentsByPatientName() {
        String input = JOptionPane.showInputDialog(null, "Enter the ID of the patient whose appointment list you want to delete",
                "delete patient appointment", JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Patient ID cannot be empty!" ,"Warning",JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId;
        try {
            patientId = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid ID", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DBConnection.connect();
            String sql = "DELETE FROM appointments WHERE patient_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, String.valueOf(patientId));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "All appointments have been deleted successfully.",
                        "Succsfuly", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "No patient with this name was found or there is no appointment to delete.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while deleting appointments.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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

    public void listPatient() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.connect();
            String sql = "SELECT u.user_name, p.phone_number FROM users u JOIN patients p ON u.id = p.id";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append("<html><h2>Patients</h2>");
            message.append("<table border='1' cellpadding='5'>");
            message.append("<tr><th>Name</th><th>Phone Number</th>");

            boolean hasPatients = false;

            while (rs.next()) {
                message.append("<tr>");
                message.append("<td>").append(rs.getString("user_name")).append("</td>");
                message.append("<td>").append(rs.getString("phone_number")).append("</td>");

            }
            message.append("</table>");

            if (hasPatients) {
                JOptionPane.showMessageDialog(null, "No patients found.", "Patients", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, message.toString(), "Patients", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while listing patients.", "Error", JOptionPane.ERROR_MESSAGE);
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
}