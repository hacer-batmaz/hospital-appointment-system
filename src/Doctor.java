public class Doctor extends User{
    private String specialization;//uzmalik alani
    public Doctor(int id, String userName, String password,String specialization) {
        super(id, userName, password);
        this.specialization = specialization;
    }

    @Override
    public void showDashboard() {
        System.out.println("Doctor panel opens...");
    }
}