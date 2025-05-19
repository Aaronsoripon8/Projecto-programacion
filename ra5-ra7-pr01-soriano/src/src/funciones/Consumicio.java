package src.funciones;

public class Consumicio extends Producte{

    public Consumicio(String desc,double preu){
        super( desc, preu);
    }


    @Override
    public String toString() {
        return  "Consumició: " +descripcio + " - "+   String.format("%.2f",preu) + "€";
    }
}