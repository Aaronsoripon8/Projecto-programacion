package src.funciones;

// Clase que representa un pago completo con entradas, consumiciones y guardarropa
public class Pagar {
    // Identificador único del pago (se guarda en la base de datos)
    private int id;

    // Array de entradas asociadas al pago
    private Entrada[] entrades;

    // Array de consumiciones asociadas al pago
    private Consumicio[] consumicions;

    // Servicio de guardarropa asociado (puede ser null)
    private Guardaroba guardaroba;

    // Precio total del pago
    private double preu;

    // Constructor sin ID (cuando creamos un nuevo pago antes de guardarlo en la base de datos)
    public Pagar(Entrada[] entrades, Consumicio[] consumicions, Guardaroba guardaroba) {
        this.entrades = entrades;
        this.consumicions = consumicions;
        this.guardaroba = guardaroba;
        this.preu = calcularPreu(); // Calculamos el precio total
    }

    // Constructor con ID (cuando recuperamos un pago desde la base de datos)
    public Pagar(int id, Entrada[] entrades, Consumicio[] consumicions, Guardaroba guardaroba) {
        this.id = id;
        this.entrades = entrades;
        this.consumicions = consumicions;
        this.guardaroba = guardaroba;
        this.preu = calcularPreu(); // Calculamos el precio total
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entrada[] getEntrades() {
        return entrades;
    }

    public Consumicio[] getConsumicions() {
        return consumicions;
    }

    public Guardaroba getGuardaroba() {
        return guardaroba;
    }

    public double getPreu() {
        return preu;
    }

    // Método privado que suma los precios de todas las entradas, consumiciones y guardarropa
    private double calcularPreu() {
        double total = 0;

        // Suma del precio de las entradas
        if (entrades != null) {
            for (Entrada e : entrades) {
                total += e.getPreu();
            }
        }

        // Suma del precio de las consumiciones
        if (consumicions != null) {
            for (Consumicio c : consumicions) {
                total += c.getPreu();
            }
        }

        // Suma del precio del guardarropa (si existe)
        if (guardaroba != null) {
            total += guardaroba.getPreu();
        }

        return total;
    }

    // Devuelve una representación en texto del contenido del pago
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // El sb.append(...) se utiliza para añadir texto a un objeto StringBuilder,
        // que es una forma eficiente de construir cadenas de texto en Java cuando se concatenan muchas partes.
        sb.append("ID: ").append(id).append("\n");

        sb.append("Entradas:\n");
        if (entrades != null) {
            for (Entrada e : entrades) {
                sb.append(" - ").append(e.getDescripcio()).append(" : ").append(e.getPreu()).append(" €\n");
            }
        }

        sb.append("Consumiciones:\n");
        if (consumicions != null) {
            for (Consumicio c : consumicions) {
                sb.append(" - ").append(c.getDescripcio()).append(" : ").append(c.getPreu()).append(" €\n");
            }
        }

        sb.append("Guardarropa:\n");
        if (guardaroba != null) {
            sb.append(" - ").append(guardaroba.getDescripcio()).append(" : ").append(guardaroba.getPreu()).append(" €\n");
        }

        sb.append("Precio total: ").append(preu).append(" €\n");

        return sb.toString();
    }
}