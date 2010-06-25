package view;
import controller.Simulador;

public class Main {

	public static void main(String[] args) {
		
		Integer cenario = Integer.parseInt(args[0]);
		
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
