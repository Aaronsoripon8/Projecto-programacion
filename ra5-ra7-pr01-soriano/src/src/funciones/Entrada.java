package src.funciones;

public class Entrada extends Producte{

    public Entrada(String desc,double preu){
        super( desc, preu);
    }


    @Override
    public String toString() {
        return  "Entrada: " +descripcio + " - "+   String.format("%.2f",preu) + "€";
    }
}