package src.dao;

import src.funciones.Pagar;
import java.sql.SQLException;
import java.util.List;

// Esta es una interfaz, es decir, define qué métodos tiene que tener cualquier clase que trabaje con pagos (Pagar)
// No tiene código, solo dice qué funciones hay que implementar para manejar los pagos
public interface PagarDAO {

    // Guardar un pago nuevo en la base de datos o archivo, puede lanzar error si algo falla
    void save(Pagar pagar) throws SQLException;

    // Buscar un pago por su ID y devolverlo, si no lo encuentra o hay error lanza excepción
    Pagar findById(int id) throws SQLException;

    // Buscar y devolver todos los pagos que haya, puede lanzar error si falla
    List<Pagar> findAll() throws SQLException;

    // Actualizar un pago que ya existe, con los datos nuevos, puede lanzar error
    void update(Pagar pagar) throws SQLException;

    // Borrar un pago según su ID, puede lanzar error si falla la eliminación
    void delete(int id) throws SQLException;
}