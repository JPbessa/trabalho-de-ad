package view;
import java.io.BufferedInputStream;
import java.util.Scanner;

import controller.Simulador;

public class Main {

	public static void main(String[] args) {
		
		System.out.println("Escolha o cenario desejado:");
		
		Scanner scan = new Scanner(new BufferedInputStream(System.in));
		
		Integer cenario = scan.nextInt();
		
		Simulador simulador = new Simulador();
		
		switch(cenario) {
			case 1:
				simulador.executarCenario1();
				break;
			case 2:
				simulador.executarCenario2();
				break;
			case 3:
				simulador.executarCenario3();
				break;
			case 4:
				simulador.executarCenario4();
				break;
			default:
				System.out.println("Cenario Invalido!");
				System.exit(0);
		}
	}
}
