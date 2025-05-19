package src.dao;

import src.funciones.Pagar;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PagarDAOCombined implements PagarDAO {

    // Estas dos variables guardan las dos formas de guardar los pagos:
    // una en base de datos (daoBD) y otra en archivo CSV (daoCSV)
    private PagarDAO daoBD;   // Soporte principal (base de datos)
    private PagarDAO daoCSV;  // Soporte secundario (archivo CSV)

    // Estas variables indican si cada soporte está funcionando o no
    private boolean daoBDDisponible = true;
    private boolean daoCSVDisponible = true;

    // Constructor que recibe los dos DAOs (BD y CSV) para usarlos luego
    public PagarDAOCombined(PagarDAO daoBD, PagarDAO daoCSV) {
        this.daoBD = daoBD;
        this.daoCSV = daoCSV;
    }

    //Esa función revisa si alguna de las dos fuentes (BD o CSV) estaba caída
    // ahora funciona, entonces copia los datos del que funcionaba hacia el que se recuperó
    // para que estén sincronizados.
    private void restaurarSiRecuperado() {
        // Si la base de datos estaba caída pero ahora parece que funciona:
        if (!daoBDDisponible) {
            try {
                // Coge todos los pagos del CSV
                List<Pagar> llistaCSV = daoCSV.findAll();
                // Guarda uno a uno esos pagos en la base de datos
                for (Pagar p : llistaCSV) {
                    daoBD.save(p);
                }
                daoBDDisponible = true; // Marca que la BD ya está disponible
                System.out.println("Restaurada información de CSV a BD después de recuperación.");
            } catch (SQLException e) {
                // Si falla, sigue marcada como no disponible
                System.out.println("No se pudo restaurar la BD tras recuperación: " + e.getMessage());
                daoBDDisponible = false;
            }
        }

        // Lo mismo para el CSV: si estaba caído y ahora funciona, sincroniza con la BD
        if (!daoCSVDisponible) {
            try {
                // Coge todos los pagos de la base de datos
                List<Pagar> llistaBD = daoBD.findAll();
                // Guarda uno a uno esos pagos en el CSV
                for (Pagar p : llistaBD) {
                    daoCSV.save(p);
                }
                daoCSVDisponible = true; // Marca que el CSV ya está disponible
                System.out.println("Restaurada información de BD a CSV después de recuperación.");
            } catch (SQLException e) {
                // Si falla, sigue marcada como no disponible
                System.out.println("No se pudo restaurar el CSV tras recuperación: " + e.getMessage());
                daoCSVDisponible = false;
            }
        }
    }

    // Guarda un pago en ambos soportes
    @Override
    public void save(Pagar pagar) throws SQLException {
        restaurarSiRecuperado(); // Primero revisa si hay que restaurar

        SQLException exBD = null;
        SQLException exCSV = null;

        // Intenta guardar en la base de datos
        try {
            daoBD.save(pagar);
            daoBDDisponible = true;  // Si funciona, marca que está disponible
        } catch (SQLException e) {
            exBD = e;
            daoBDDisponible = false; // Si falla, marca como no disponible
            System.out.println("Error guardando en BD: " + e.getMessage());
        }

        // Intenta guardar en el CSV
        try {
            daoCSV.save(pagar);
            daoCSVDisponible = true; // Si funciona, está disponible
        } catch (SQLException e) {
            exCSV = e;
            daoCSVDisponible = false; // Si falla, no disponible
            System.out.println("Error guardando en CSV: " + e.getMessage());
        }

        // Si fallan los dos, lanza error
        if (exBD != null && exCSV != null) {
            throw new SQLException("Error guardando en BD y CSV", exBD);
        }
    }

    // Busca un pago por ID, primero intenta en BD, si falla prueba en CSV
    @Override
    public Pagar findById(int id) throws SQLException {
        restaurarSiRecuperado(); // Revisa restauración

        try {
            Pagar res = daoBD.findById(id);
            daoBDDisponible = true; // Si funciona, está disponible
            return res;
        } catch (SQLException e) {
            daoBDDisponible = false; // Si falla, no disponible
            System.out.println("Error leyendo de BD, prueba CSV: " + e.getMessage());
        }

        try {
            Pagar res = daoCSV.findById(id);
            daoCSVDisponible = true; // Si funciona, está disponible
            return res;
        } catch (SQLException e) {
            daoCSVDisponible = false; // Si falla, no disponible
            System.out.println("Error leyendo de CSV: " + e.getMessage());
        }

        // Si no lo encuentra en ninguno, lanza error
        throw new SQLException("No se pudo leer el pago con id " + id);
    }

    // Devuelve todos los pagos, prueba BD y si falla CSV
    @Override
    public List<Pagar> findAll() throws SQLException {
        restaurarSiRecuperado(); // Revisa restauración

        try {
            List<Pagar> llista = daoBD.findAll();
            daoBDDisponible = true; // BD disponible
            return llista;
        } catch (SQLException e) {
            daoBDDisponible = false; // BD no disponible
            System.out.println("Error listando BD, prueba CSV: " + e.getMessage());
        }

        try {
            List<Pagar> llista = daoCSV.findAll();
            daoCSVDisponible = true; // CSV disponible
            return llista;
        } catch (SQLException e) {
            daoCSVDisponible = false; // CSV no disponible
            System.out.println("Error listando CSV: " + e.getMessage());
        }

        // Si falla todo, devuelve lista vacía para no romper nada
        return new ArrayList<>();
    }

    // Actualiza un pago, intenta en BD y CSV
    @Override
    public void update(Pagar pagar) throws SQLException {
        restaurarSiRecuperado(); // Revisa restauración

        SQLException exBD = null;
        SQLException exCSV = null;

        try {
            daoBD.update(pagar);
            daoBDDisponible = true; // BD disponible
        } catch (SQLException e) {
            exBD = e;
            daoBDDisponible = false; // BD no disponible
            System.out.println("Error actualizando BD: " + e.getMessage());
        }

        // Comentamos la actualización en CSV porque no está implementada para evitar la excepción
        /*
        try {
            daoCSV.update(pagar);
            daoCSVDisponible = true; // CSV disponible
        } catch (SQLException e) {
            exCSV = e;
            daoCSVDisponible = false; // CSV no disponible
            System.out.println("Error actualizando CSV: " + e.getMessage());
        }
        */

        // Si fallan ambos, lanza error (aunque el CSV está desactivado, por eso exCSV está null)
        if (exBD != null /*&& exCSV != null*/) {
            throw new SQLException("Error actualizando en BD", exBD);
        }
    }

    // Borra un pago por id en BD y CSV
    @Override
    public void delete(int id) throws SQLException {
        restaurarSiRecuperado(); // Revisa restauración

        SQLException exBD = null;
        SQLException exCSV = null;

        try {
            daoBD.delete(id);
            daoBDDisponible = true; // BD disponible
        } catch (SQLException e) {
            exBD = e;
            daoBDDisponible = false; // BD no disponible
            System.out.println("Error eliminando BD: " + e.getMessage());
        }

        // NUEVO: Intentamos borrar en CSV y controlamos la excepción
        try {
            daoCSV.delete(id);  // Borra en CSV (asegúrate que este método está implementado correctamente)
            daoCSVDisponible = true; // CSV disponible si no falla
        } catch (SQLException e) {
            exCSV = e;
            daoCSVDisponible = false; // CSV no disponible si falla
            System.out.println("Error eliminando CSV: " + e.getMessage());
        }

        // Si ambos fallan, lanza excepción
        if (exBD != null && exCSV != null) {
            throw new SQLException("Error eliminando en BD y CSV", exBD);
        }
    }
}
