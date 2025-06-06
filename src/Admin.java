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

    private boolean deleteDoctor(int doctorId) {
        Connection conn = null;
        PreparedStatement deleteDoctorPstmt = null;
        PreparedStatement deleteUserPstmt = null;

        try {
            conn = DBConnection.connect(); // Veritabanı bağlantısı açıldı
            conn.setAutoCommit(false); // Auto-commit'i kapat

            // Önce doctors tablosundan silme işlemi
            String deleteDoctorSql = "DELETE FROM doctors WHERE id = ?";
            deleteDoctorPstmt = conn.prepareStatement(deleteDoctorSql);
            deleteDoctorPstmt.setInt(1, doctorId);

            int doctorAffectedRows = deleteDoctorPstmt.executeUpdate();

            if (doctorAffectedRows == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(null, "Doctor with ID " + doctorId + " not found!", "Error", JOptionPane.ERROR_MESSAGE);//bulunamadi
                return false;
            }

            int adminChoice = JOptionPane.showConfirmDialog(null,"Doctor with ID " + doctorId + " has been deleted from doctors table.\n" +
                    "Do you also want to delete from users table?", "Confirm User Deletion", JOptionPane.YES_NO_OPTION);

            if (adminChoice == JOptionPane.YES_OPTION) {
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                deleteUserPstmt = conn.prepareStatement(deleteUserSql);
                deleteUserPstmt.setInt(1, doctorId);

                int userAffectedRows = deleteUserPstmt.executeUpdate();
                if (userAffectedRows > 0) {
                    conn.commit();
                    JOptionPane.showMessageDialog(null,"Doctor with ID " + doctorId + " has been completely deleted from both tables!",
                            "Succes", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null,"Doctor was deleted from doctors table but could not be deleted from users table!",
                            "Partial Succes",JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } else {
                conn.commit();
                JOptionPane.showMessageDialog(null,"Doctor with ID " + doctorId + " has been deleted only from doctors table.",
                        "Partial Deletion", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }

            String errMesage = "An error occured while deleting the doctor:\n";//hata mesaji
            if (e.getMessage().contains("foreign key constraint fails")) {//yabanci anahtar kisitlamasi hatasi
                errMesage += "This doctor cannot be deleted because they have related records in other tables.";
            } else {
                errMesage += e.getMessage();
            }

            JOptionPane.showMessageDialog(null,errMesage,"Error",JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {//kaynaklari kapat
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

    public void deleteDoctorInteractive() {
        String input = JOptionPane.showInputDialog(null,"Enter Doctor ID to delete: ", "Delete doctor",JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,"Operation cancelled.","Info",JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            int doctorId = Integer.parseInt(input);

            int confirm = JOptionPane.showConfirmDialog(null,"Are you sure you want to delete doctor with ID " + doctorId + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = deleteDoctor(doctorId);
                if (!success)
                    JOptionPane.showMessageDialog(null,"Deletion failed for doctor ID " + doctorId, "Error",JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null,"Deletion cancelled.", "Error",JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,"Invalid ID format: Please enter a number.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }
}