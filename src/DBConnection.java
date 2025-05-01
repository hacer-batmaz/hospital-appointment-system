import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_management";
    private static final String USER = "root";
    private static final String PASSWORD = "123456abc";

    //baglantiyi baslatir, connection nesnesi doner
    public static Connection connect() {
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //JDBC surucusunu yukle
            conn = DriverManager.getConnection(URL, USER, PASSWORD); //baglantiyi kur
        } catch (ClassNotFoundException e) { //hata kismi
            System.err.println("JDBC sürücüsü bulunamadı!");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "JDBC sürücüsü yüklenemedi!", "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            System.err.println("Veritabanına bağlanılamadı!");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Veritabanına bağlanılamadı!\n" + e.getMessage(), "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }

        return conn;
    }
}