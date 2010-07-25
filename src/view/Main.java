package view;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Scanner;

import controller.Simulador;

public class Main {

	public static void main(String[] args) throws IOException {
		
		System.out.println("Escolha o cenario desejado:");
		Scanner scan = new Scanner(new BufferedInputStream(System.in));
		Integer cenario = scan.nextInt();
		
		Simulador simulador = new Simulador();
		simulador.executarCenario(cenario);
		
	}
}
