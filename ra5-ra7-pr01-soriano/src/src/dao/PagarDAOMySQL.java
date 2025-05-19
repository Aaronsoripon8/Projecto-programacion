package src.dao;

// Importa la conexión a la base de datos y las clases relacionadas
import src.BBDDConnection.ConexioBD;
import src.funciones.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagarDAOMySQL implements PagarDAO {

    // Instancia única de la clase (patrón Singleton)
    private static PagarDAOMySQL instance;

    // Constructor privado para evitar que otros instancien directamente
    public PagarDAOMySQL() {}

    // Método para obtener la instancia única (patrón Singleton)
    public static synchronized PagarDAOMySQL getInstance() {
        if (instance == null) {
            instance = new PagarDAOMySQL();
        }
        return instance;
    }

    // Obtiene la conexión a la base de datos
    private Connection getConnection() throws SQLException {
        return ConexioBD.getInstance();
    }

    // Guarda un objeto Pagar en la base de datos (y sus componentes relacionados)
    @Override
    public void save(Pagar pagar) throws SQLException {
        String sqlPagar = "INSERT INTO pagar (preu) VALUES (?)";
        Connection conn = getConnection();
        try {
            // Comienza la transacción manual
            conn.setAutoCommit(false);

            // Inserta el pago
            try (PreparedStatement ps = conn.prepareStatement(sqlPagar, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, pagar.getPreu());
                ps.executeUpdate();

                // Obtiene el ID generado
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int pagarId = rs.getInt(1);
                    pagar.setId(pagarId);

                    // Inserta las entradas asociadas
                    String sqlEntrada = "INSERT INTO entrada (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
                    try (PreparedStatement psEntrada = conn.prepareStatement(sqlEntrada)) {
                        for (Entrada e : pagar.getEntrades()) {
                            psEntrada.setInt(1, pagarId);
                            psEntrada.setString(2, e.getDescripcio());
                            psEntrada.setDouble(3, e.getPreu());
                            psEntrada.addBatch(); // Agrupa ejecuciones
                        }
                        psEntrada.executeBatch(); // Ejecuta todas las inserciones juntas
                    }

                    // Inserta las consumiciones asociadas
                    String sqlConsumicio = "INSERT INTO consumicio (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
                    try (PreparedStatement psConsumicio = conn.prepareStatement(sqlConsumicio)) {
                        for (Consumicio c : pagar.getConsumicions()) {
                            psConsumicio.setInt(1, pagarId);
                            psConsumicio.setString(2, c.getDescripcio());
                            psConsumicio.setDouble(3, c.getPreu());
                            psConsumicio.addBatch();
                        }
                        psConsumicio.executeBatch();
                    }

                    // Inserta el guardarropa (si existe)
                    if (pagar.getGuardaroba() != null) {
                        String sqlGuardaroba = "INSERT INTO guardaroba (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
                        try (PreparedStatement psGuardaroba = conn.prepareStatement(sqlGuardaroba)) {
                            psGuardaroba.setInt(1, pagarId);
                            psGuardaroba.setString(2, pagar.getGuardaroba().getDescripcio());
                            psGuardaroba.setDouble(3, pagar.getGuardaroba().getPreu());
                            psGuardaroba.executeUpdate();
                        }
                    }
                }
            }
            conn.commit(); // Confirma todos los cambios
        } catch (SQLException e) {
            conn.rollback(); // Revierte los cambios si hay error
            throw e;
        } finally {
            conn.setAutoCommit(true); // Vuelve al modo autocommit
        }
    }

    // Busca un objeto Pagar por ID
    @Override
    public Pagar findById(int id) throws SQLException {
        Connection conn = getConnection();
        Pagar pagar = null;

        String sqlPagar = "SELECT * FROM pagar WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlPagar)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double preu = rs.getDouble("preu");

                    // Recupera entradas asociadas
                    List<Entrada> entrades = new ArrayList<>();
                    String sqlEntrada = "SELECT * FROM entrada WHERE pagar_id = ?";
                    try (PreparedStatement psEntrada = conn.prepareStatement(sqlEntrada)) {
                        psEntrada.setInt(1, id);
                        try (ResultSet rsEntrada = psEntrada.executeQuery()) {
                            while (rsEntrada.next()) {
                                String desc = rsEntrada.getString("descripcio");
                                double preuEntrada = rsEntrada.getDouble("preu");
                                entrades.add(new Entrada(desc, preuEntrada));
                            }
                        }
                    }

                    // Recupera consumiciones asociadas
                    List<Consumicio> consumicions = new ArrayList<>();
                    String sqlConsumicio = "SELECT * FROM consumicio WHERE pagar_id = ?";
                    try (PreparedStatement psConsumicio = conn.prepareStatement(sqlConsumicio)) {
                        psConsumicio.setInt(1, id);
                        try (ResultSet rsConsumicio = psConsumicio.executeQuery()) {
                            while (rsConsumicio.next()) {
                                String desc = rsConsumicio.getString("descripcio");
                                double preuConsumicio = rsConsumicio.getDouble("preu");
                                consumicions.add(new Consumicio(desc, preuConsumicio));
                            }
                        }
                    }

                    // Recupera guardarropa (solo uno)
                    Guardaroba guardaroba = null;
                    String sqlGuardaroba = "SELECT * FROM guardaroba WHERE pagar_id = ?";
                    try (PreparedStatement psGuardaroba = conn.prepareStatement(sqlGuardaroba)) {
                        psGuardaroba.setInt(1, id);
                        try (ResultSet rsGuardaroba = psGuardaroba.executeQuery()) {
                            if (rsGuardaroba.next()) {
                                String desc = rsGuardaroba.getString("descripcio");
                                double preuGuardaroba = rsGuardaroba.getDouble("preu");
                                guardaroba = new Guardaroba(desc, preuGuardaroba);
                            }
                        }
                    }

                    // Crea objeto Pagar completo
                    pagar = new Pagar(
                            entrades.toArray(new Entrada[0]),
                            consumicions.toArray(new Consumicio[0]),
                            guardaroba);
                    pagar.setId(id);
                }
            }
        }

        return pagar;
    }

    // Recupera todos los pagos de la base de datos
    @Override
    public List<Pagar> findAll() throws SQLException {
        Connection conn = getConnection();
        List<Pagar> lista = new ArrayList<>();

        String sqlPagar = "SELECT * FROM pagar";
        try (PreparedStatement ps = conn.prepareStatement(sqlPagar);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Pagar pagar = findById(id); // Carga el objeto completo con findById
                if (pagar != null) {
                    lista.add(pagar);
                }
            }
        }

        return lista;
    }

    // Actualiza un pago existente en la base de datos
    @Override
    public void update(Pagar pagar) throws SQLException {
        Connection conn = getConnection();

        try {
            conn.setAutoCommit(false);

            // Actualiza el precio del pago
            String sqlUpdatePagar = "UPDATE pagar SET preu = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdatePagar)) {
                ps.setDouble(1, pagar.getPreu());
                ps.setInt(2, pagar.getId());
                ps.executeUpdate();
            }

            // Elimina los registros relacionados antiguos
            String sqlDeleteEntradas = "DELETE FROM entrada WHERE pagar_id = ?";
            String sqlDeleteConsumicions = "DELETE FROM consumicio WHERE pagar_id = ?";
            String sqlDeleteGuardaroba = "DELETE FROM guardaroba WHERE pagar_id = ?";
            try (PreparedStatement psEntradas = conn.prepareStatement(sqlDeleteEntradas);
                 PreparedStatement psConsumicions = conn.prepareStatement(sqlDeleteConsumicions);
                 PreparedStatement psGuardaroba = conn.prepareStatement(sqlDeleteGuardaroba)) {
                psEntradas.setInt(1, pagar.getId());
                psConsumicions.setInt(1, pagar.getId());
                psGuardaroba.setInt(1, pagar.getId());

                psEntradas.executeUpdate();
                psConsumicions.executeUpdate();
                psGuardaroba.executeUpdate();
            }

            // Inserta nuevamente las entradas
            String sqlInsertEntrada = "INSERT INTO entrada (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
            try (PreparedStatement psEntrada = conn.prepareStatement(sqlInsertEntrada)) {
                for (Entrada e : pagar.getEntrades()) {
                    psEntrada.setInt(1, pagar.getId());
                    psEntrada.setString(2, e.getDescripcio());
                    psEntrada.setDouble(3, e.getPreu());
                    psEntrada.addBatch();
                }
                psEntrada.executeBatch();
            }

            // Inserta nuevamente las consumiciones
            String sqlInsertConsumicio = "INSERT INTO consumicio (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
            try (PreparedStatement psConsumicio = conn.prepareStatement(sqlInsertConsumicio)) {
                for (Consumicio c : pagar.getConsumicions()) {
                    psConsumicio.setInt(1, pagar.getId());
                    psConsumicio.setString(2, c.getDescripcio());
                    psConsumicio.setDouble(3, c.getPreu());
                    psConsumicio.addBatch();
                }
                psConsumicio.executeBatch();
            }

            // Inserta nuevamente el guardarropa
            if (pagar.getGuardaroba() != null) {
                String sqlInsertGuardaroba = "INSERT INTO guardaroba (pagar_id, descripcio, preu) VALUES (?, ?, ?)";
                try (PreparedStatement psGuardaroba = conn.prepareStatement(sqlInsertGuardaroba)) {
                    psGuardaroba.setInt(1, pagar.getId());
                    psGuardaroba.setString(2, pagar.getGuardaroba().getDescripcio());
                    psGuardaroba.setDouble(3, pagar.getGuardaroba().getPreu());
                    psGuardaroba.executeUpdate();
                }
            }

            conn.commit(); // Confirma todo
        } catch (SQLException e) {
            conn.rollback(); // Revierte si hay error
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Elimina un pago y todo lo relacionado (entradas, consumiciones, guardarropa)
    @Override
    public void delete(int id) throws SQLException {
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);

            // Sentencias SQL para eliminar todo lo relacionado con pagar_id
            String sqlDeleteEntradas = "DELETE FROM entrada WHERE pagar_id = ?";
            String sqlDeleteConsumicions = "DELETE FROM consumicio WHERE pagar_id = ?";
            String sqlDeleteGuardaroba = "DELETE FROM guardaroba WHERE pagar_id = ?";
            String sqlDeletePagar = "DELETE FROM pagar WHERE id = ?";

            try (PreparedStatement psEntradas = conn.prepareStatement(sqlDeleteEntradas);
                 PreparedStatement psConsumicions = conn.prepareStatement(sqlDeleteConsumicions);
                 PreparedStatement psGuardaroba = conn.prepareStatement(sqlDeleteGuardaroba);
                 PreparedStatement psPagar = conn.prepareStatement(sqlDeletePagar)) {

                psEntradas.setInt(1, id);
                psEntradas.executeUpdate();

                psConsumicions.setInt(1, id);
                psConsumicions.executeUpdate();

                psGuardaroba.setInt(1, id);
                psGuardaroba.executeUpdate();

                psPagar.setInt(1, id);
                psPagar.executeUpdate();
            }

            conn.commit(); // Confirma la transacción
        } catch (SQLException e) {
            conn.rollback(); // Revierte si hay error
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
