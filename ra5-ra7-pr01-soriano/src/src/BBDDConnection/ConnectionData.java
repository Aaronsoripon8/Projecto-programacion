package src.BBDDConnection;

public class ConnectionData {
    private static final String URL = "jdbc:mysql://daw.inspedralbes.cat:3306/a24aarsorpon_discoteca";
    private static final String USER = "a24aarsorpon_discoteca";
    private static final String PASS = "=ZK_:E%36=Pfh!1S";

    public static String getUsr() {
        return USER;
    }

    public static String getPwd() {
        return PASS;
    }

    public static String getUrl() {
        return URL;
    }
}