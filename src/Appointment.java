import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private int appointmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentStartDate;
    private LocalTime appointmentEndTime;
    private AppointmentStatus appointmentStatus; //enum
    private Doctor doctor;
    private Patient patient;

    public Appointment() {
    }

    public Appointment(int appointmentId, LocalDate appointmentDate, LocalTime appointmentStartDate, LocalTime appointmentEndTime,
                       AppointmentStatus appointmentStatus, Doctor doctor, Patient patient) {
        this.appointmentId = appointmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentStartDate = appointmentStartDate;
        this.appointmentEndTime = appointmentEndTime;
        this.appointmentStatus = AppointmentStatus.PENDING; //varsayilan durum beklemede
        this.doctor = doctor;
        this.patient = patient;
    }

    public void showAppointment(int appointmentId) {
        String sql = "SELECT " +
                "a.appointment_id, " +
                "a.appointment_date, " +
                "a.appointment_start_time, " +
                "a.appointment_end_time, " +
                "a.appointment_status, " +
                "a.doctor_name, " +
                "a.patient_name, " +
                "d.specialization, " +
                "p.phone_number " +
                "FROM appointments a " +
                "LEFT JOIN doctors d ON d.id = (SELECT id FROM users WHERE user_name = a.doctor_name) " +
                "LEFT JOIN patients p ON p.id = (SELECT id FROM users WHERE user_name = a.patient_name) " +
                "WHERE a.appointment_id = ?";


        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery(); //veritabaninda donen sonuclarin saklanacagi nesne(rs)
            // executeQuery() -> SELECT sorgularini calistirir

            if (rs.next()) {
                // Randevu bilgilerini birleştiriyoruz
                StringBuilder message = new StringBuilder();
                message.append("<html><b>Appointment Details:</b><br><br>");
                message.append("<b>ID:</b> ").append(rs.getInt("appointment_id")).append("<br>");
                message.append("<b>Date:</b> ").append(rs.getDate("appointment_date")).append("<br>");
                message.append("<b>Start Time:</b> ").append(rs.getTime("appointment_start_time")).append("<br>");
                message.append("<b>End Time:</b> ").append(rs.getTime("appointment_end_time")).append("<br>");
                message.append("<b>Status:</b> ").append(rs.getString("appointment_status")).append("<br>");
                message.append("<b>Doctor Name:</b> ").append(rs.getString("doctor_name")).append("<br>");
                message.append("<b>Doctor Specialization:</b> ").append(rs.getString("specialization")).append("<br>");
                message.append("<b>Patient Name:</b> ").append(rs.getString("patient_name")).append("<br>");
                message.append("<b>Patient Phone:</b> ").append(rs.getString("phone_number")).append("<br>");

                // HTML formatında mesaj kutusu gösteriyoruz
                JOptionPane.showMessageDialog(null,
                        message.toString(),
                            "Appointment Details - ID: " + appointmentId,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No appointment found with specified ID: " + appointmentId,
                        "Hata",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "An error occurred while retrieving appointment information:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentStartDate() {
        return appointmentStartDate;
    }

    public LocalTime getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public AppointmentStatus getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(AppointmentStatus appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Patient getPatient() {
        return patient;
    }
}