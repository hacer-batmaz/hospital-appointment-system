public class Patient extends User{
    private String phoneNumber;
    public Patient(int id, String userName, String password,String phoneNumber) {
        super(id, userName, password);
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void showDashboard() {
        System.out.println("Patient panel opens...");
    }
}