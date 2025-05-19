package src;

// Importación de clases necesarias
import src.funciones.*; // Importa las clases del paquete funciones (como Entrada, Consumicio, Guardaroba, Pagar)
import src.dao.*;       // Importa las clases para gestión de datos (DAOs)

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static src.Dades.*;                 // Importa datos como arrays de descripciones y precios
import static utils.utils.llegirInt;       // Importa función para leer enteros con validación

public class Discoteca {
    static Scanner scan = new Scanner(System.in); // Para leer la entrada del usuario

    // Creación de instancias concretas de los DAOs (para MySQL y CSV)
    static PagarDAO daoBD = new PagarDAOMySQL();
    static PagarDAO daoCSV = new PagarDAOCSV();

    // Se combinan los dos DAOs en una sola clase que se encarga de usarlos según convenga
    static PagarDAO dao = new PagarDAOCombined(daoBD, daoCSV);

    // Método principal del programa
    public static void main(String[] args) {
        boolean salir = false; // Controla si el usuario quiere salir del programa

        // Bucle del menú principal
        while (!salir) {
            // Mostrar menú
            System.out.println("\n--- Menú Discoteca ---");
            System.out.println("1 - Crear nueva entrada");
            System.out.println("2 - Buscar entrada por ID");
            System.out.println("3 - Mostrar todas las entradas");
            System.out.println("4 - Actualizar entrada");
            System.out.println("5 - Borrar entrada");
            System.out.println("0 - Salir");

            // Leer opción del usuario
            int opcion = llegirInt(scan, "Elige una opción:", 0, 5);

            try {
                // Ejecutar la función correspondiente a la opción
                switch (opcion) {
                    case 1 -> crearEntrada();
                    case 2 -> buscarEntrada();
                    case 3 -> mostrarEntradas();
                    case 4 -> actualizarEntrada();
                    case 5 -> borrarEntrada();
                    case 0 -> salir = true;
                }
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        }

        System.out.println("¡Hasta pronto!"); // Mensaje de salida
    }

    // Crea una nueva entrada y la guarda usando el DAO
    private static void crearEntrada() throws SQLException {
        Entrada[] entrada = elegirEntrada();
        Consumicio[] consumicion = elegirConsumiciones();
        Guardaroba guardarropa = elegirGuardarropa();
        Pagar pagar = new Pagar(entrada, consumicion, guardarropa); // Crear objeto Pagar
        dao.save(pagar); // Guardar en el sistema (BD y/o CSV)
        System.out.println("Entrada guardada correctamente con ID: " + pagar.getId());
        System.out.println(pagar); // Mostrar resumen del pago
    }

    // Buscar una entrada por su ID y mostrarla
    private static void buscarEntrada() throws SQLException {
        int id = llegirInt(scan, "Introduce el ID de la entrada a buscar:", 1, Integer.MAX_VALUE);
        Pagar pagar = dao.findById(id);
        if (pagar != null) {
            System.out.println("Entrada encontrada:\n" + pagar);
        } else {
            System.out.println("No se ha encontrado ninguna entrada con ese ID.");
        }
    }

    // Mostrar todas las entradas registradas
    private static void mostrarEntradas() throws SQLException {
        List<Pagar> lista = dao.findAll();
        if (lista.isEmpty()) {
            System.out.println("No hay entradas registradas.");
        } else {
            System.out.println("Lista de entradas:");
            for (Pagar p : lista) {
                System.out.println(p);
                System.out.println("---------------------");
            }
        }
    }

    // Actualiza una entrada existente
    private static void actualizarEntrada() throws SQLException {
        int id = llegirInt(scan, "Introduce el ID de la entrada a actualizar:", 1, Integer.MAX_VALUE);
        Pagar pagar = dao.findById(id);
        if (pagar == null) {
            System.out.println("No se ha encontrado ninguna entrada con ese ID.");
            return;
        }
        System.out.println("Entrada actual:\n" + pagar);
        System.out.println("Introduce los nuevos datos:");
        Entrada[] entrada = elegirEntrada();
        Consumicio[] consumicion = elegirConsumiciones();
        Guardaroba guardarropa = elegirGuardarropa();
        Pagar nuevoPago = new Pagar(entrada, consumicion, guardarropa);
        nuevoPago.setId(id); // Mantener el mismo ID para sobrescribir
        dao.update(nuevoPago); // Actualizar en base de datos
        System.out.println("Entrada actualizada correctamente.");
    }

    // Borra una entrada por su ID
    private static void borrarEntrada() throws SQLException {
        int id = llegirInt(scan, "Introduce el ID de la entrada a borrar:", 1, Integer.MAX_VALUE);
        dao.delete(id); // Eliminar del sistema
        System.out.println("Entrada borrada (si existía).");
    }

    // Permite elegir una opción de guardarropa
    private static Guardaroba elegirGuardarropa() {
        System.out.println("¿Quieres guardarropa?");
        for (int i = 0; i < guardarobes.length; i++) {
            System.out.println((i + 1) + " - " + guardarobes[i]);
        }
        int opcion = llegirInt(scan, "Elige una opción:", 1, guardarobes.length) - 1;
        return new Guardaroba(guardarobes[opcion], 0);
    }

    // Permite elegir múltiples consumiciones
    private static Consumicio[] elegirConsumiciones() {
        ArrayList<Consumicio> consumiciones = new ArrayList<>();
        System.out.println("Consumiciones disponibles:");
        for (int i = 0; i < consumicioDesc.length; i++) {
            // Aquí añadimos el símbolo de euro
            System.out.println((i + 1) + " - " + consumicioDesc[i] + " - " + consumicioPreu[i] + " €");
        }
        int opcion;
        while (true) {
            opcion = llegirInt(scan, "Elige una o varias consumiciones (0 para terminar):", 0, consumicioDesc.length) - 1;
            if (opcion == -1) break; // 0 para terminar
            consumiciones.add(new Consumicio(consumicioDesc[opcion], consumicioPreu[opcion]));
        }
        return consumiciones.toArray(new Consumicio[0]); // Devolver array
    }

    // Permite elegir una única entrada
    private static Entrada[] elegirEntrada() {
        System.out.println("Entradas disponibles:");
        for (int i = 0; i < entradaDesc.length; i++) {
            // Aquí añadimos el símbolo de euro también
            System.out.println((i + 1) + " - " + entradaDesc[i] + " - " + entradaPreu[i] + " €");
        }
        int opcion = llegirInt(scan, "Elige una opción:", 1, entradaDesc.length) - 1;
        Entrada entrada = new Entrada(entradaDesc[opcion], entradaPreu[opcion]);
        return new Entrada[] { entrada }; // Devolver en formato array
    }
}