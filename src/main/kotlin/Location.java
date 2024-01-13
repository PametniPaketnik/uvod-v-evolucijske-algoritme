public class Location {
    private int postalCode;
    private String postOfficeName;
    private String address;

    //private int numberOfParcelLockers;

    public Location(int postalCode, String postOfficeName, String address) {
        this.postalCode = postalCode;
        this.postOfficeName = postOfficeName;
        this.address = address;
        //this.numberOfParcelLockers = numberOfParcelLockers;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public String getPostOfficeName() {
        return postOfficeName;
    }

    public String getAddress() {
        return address;
    }

}
