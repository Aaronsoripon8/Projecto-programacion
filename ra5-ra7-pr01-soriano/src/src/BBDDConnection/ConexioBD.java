package src.BBDDConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexioBD {

    // Aquí guardamos la conexión a la base de datos para usarla siempre la misma
    private static Connection connection;

    // Constructor privado para que nadie pueda crear un objeto de esta clase
    private ConexioBD() {
        // Evita que alguien instancie esta clase
    }

    // Método que abre la conexión con la base de datos usando los datos guardados
    private static void openConnection() {
        // Conseguimos usuario, contraseña y url para conectar de otra clase llamada ConnectionData
        String usr = ConnectionData.getUsr();
        String pwd = ConnectionData.getPwd();
        String url = ConnectionData.getUrl();

        try {
            // Intentamos crear la conexión con esos datos
            connection = DriverManager.getConnection(url, usr, pwd);
        } catch (SQLException e) {
            // Si falla, mostramos un mensaje con el problema
            System.out.println("Problema al establir la connexió: " + e.getMessage());
        }
    }

    // Método público para obtener la conexión, si no existe o está cerrada, la abre
    public static Connection getInstance() throws SQLException {
        try {
            // Si la conexión no existe o está cerrada, abrimos una nueva
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
        } catch (SQLException e) {
            // Si hay error al comprobar la conexión, lo mostramos y también intentamos abrirla
            System.out.println("Error comprovant l'estat de la connexió: " + e.getMessage());
            openConnection();
        }
        // Devolvemos la conexión ya abierta y lista para usar
        return connection;
    }

    // Método para cerrar la conexión cuando ya no se necesite
    public static void closeConnection() {
        if (connection != null) { // Si existe la conexión
            try {
                // Si no está ya cerrada, la cerramos
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                // Si falla al cerrar, mostramos un mensaje con el error
                System.out.println("No s'ha pogut tancar la connexió: " + e.getMessage());
            }
        }
    }
}