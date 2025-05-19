package src.funciones;

public class Guardaroba extends Producte {

    public Guardaroba(String desc,double preu){
        super( desc, preu);
    }

    public String getDescripcio() {
        return descripcio;
    }

    public double getPreu() {
        return preu;
    }

    @Override
    public String toString() {
        return  "Guardarobes: " + descripcio + " - " + String.format("%.2f",preu) + "€";
    }
}