package utils;

import java.util.Scanner;

public class utils {
    public static int llegirInt(Scanner scanner, String s) {
        int numero;
        while(true){
            System.out.println(s);
            if(scanner.hasNextInt()){
                numero = scanner.nextInt();
                scanner.nextLine();
                return numero;
            }else{
                System.out.println("ERROR: Si us plau, introdueixi un número enter vàlid.");
                scanner.next();
            }
        }
    }

    public static int llegirInt(Scanner scanner, String s, int min, int max ) {
        int numero;
        while(true){
            numero = llegirInt(scanner,s);
            if (numero >= min && numero <= max){
                return numero;
            }else{
                System.out.println("ERROR: Si us plau, introdueixi un número entre " + min + " i " + max+".");
            }
        }
    }
}