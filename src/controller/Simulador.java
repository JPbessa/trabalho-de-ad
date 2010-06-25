package controller;

import model.PC;
import model.IntervaloChegadas;
import model.TipoDistribuicao;

public class Simulador {

	public void executarCenario1() {
		System.out.println("Executando cenario 1...");
		PC PC1 = new PC(100);
		PC1.setP(40);
		PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		PC PC2 = new PC(80);
		PC2.setP(40);
		PC2.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		//PC1.start();
		//PC2.start();
	}
	
	public void executarCenario2() {
			
	}
	
	public void executarCenario3() {
		
	}
	
	public void executarCenario4() {
		
	}
}
