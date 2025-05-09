public enum AppointmentStatus {
    APPROVED("Approved"), //onaylandi
    CANCELLED("Cancelled"), //iptal edildi
    COMPLETED("Completed"), //tamamlandi
    AVAILABLA("Available"), //bos
    PENDING("Pending"); //beklemede

    private String status;

    AppointmentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}