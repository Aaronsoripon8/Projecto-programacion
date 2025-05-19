package src.dao;

import src.funciones.*;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PagarDAOCSV implements PagarDAO {

    // Nombre del archivo CSV donde se guardan los pagos
    private static final String FILE_NAME = "pagaments.csv";

    // Método para guardar un pago en el archivo CSV
    @Override
    public void save(Pagar pagar) throws SQLException {
        // Usa BufferedWriter para escribir en el archivo en modo "añadir" (true)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            // Escribe el precio del pago como texto en una línea nueva
            writer.write(String.valueOf(pagar.getPreu()));
            writer.newLine(); // Salto de línea para el próximo pago
        } catch (IOException e) {
            // Si hay error escribiendo en el archivo, lanza una excepción para avisar
            throw new SQLException("Error escribiendo en CSV", e);
        }
    }

    // Método para buscar un pago por ID (aquí simplificado porque no guardamos ID en CSV)
    // Comentar: Esta es una implementación básica que busca por índice (posición en archivo)
    @Override
    public Pagar findById(int id) throws SQLException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String linia;
            int index = 0;
            while ((linia = reader.readLine()) != null) {
                if (index == id) {
                    // Convertimos el texto en número para el precio
                    double preu = Double.parseDouble(linia);
                    // Devolvemos un objeto Pagar (placeholder con datos vacíos)
                    return new Pagar(new Entrada[]{}, new Consumicio[]{}, null); // Aquí no hay más datos
                }
                index++;
            }
        } catch (IOException e) {
            throw new SQLException("Error leyendo del CSV", e);
        }
        throw new SQLException("No se encontró el pago con id " + id);
    }

    // Lee todos los pagos del archivo CSV y devuelve una lista
    @Override
    public List<Pagar> findAll() throws SQLException {
        List<Pagar> llista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String linia;
            // Lee línea por línea el archivo hasta el final
            while ((linia = reader.readLine()) != null) {
                // Convierte la línea (texto) en un número decimal (precio)
                double preu = Double.parseDouble(linia);
                // Añade un nuevo objeto Pagar a la lista (aquí usa datos vacíos como ejemplo)
                llista.add(new Pagar(new Entrada[]{}, new Consumicio[]{}, null)); // Solo un placeholder
            }
        } catch (IOException e) {
            // Si hay error leyendo el archivo, lanza una excepción para avisar
            throw new SQLException("Error leyendo del CSV", e);
        }
        return llista; // Devuelve la lista con todos los pagos encontrados
    }

    // Actualizar pago no está implementado en CSV
    @Override
    public void update(Pagar pagar) throws SQLException {
        throw new UnsupportedOperationException("No implementado para CSV");
    }

    // Borrar pago en CSV: IMPLEMENTACIÓN NUEVA
    @Override
    public void delete(int id) throws SQLException {
        // Comentar: Aquí implementamos borrado por índice (posición) en CSV
        List<String> lineas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String linia;
            while ((linia = reader.readLine()) != null) {
                lineas.add(linia); // Guardamos todas las líneas en memoria
            }
        } catch (IOException e) {
            throw new SQLException("Error leyendo del CSV", e);
        }

        if (id < 0 || id >= lineas.size()) {
            throw new SQLException("No se encontró el pago con id " + id + " para eliminar.");
        }

        // Eliminamos la línea que corresponde al ID dado
        lineas.remove(id);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            // Sobrescribimos el archivo con todas las líneas menos la borrada
            for (String linea : lineas) {
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SQLException("Error escribiendo el CSV después de eliminar", e);
        }
    }
}