package src.funciones;

public abstract class Producte {
    protected String descripcio;
    protected double preu;

    public Producte(String descripcio, double preu) {
        this.descripcio = descripcio;
        this.preu = preu;
    }

    public String getDescripcio(){
        return this.descripcio;
    }

    public double getPreu(){
        return this.preu;
    }
}