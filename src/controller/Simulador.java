package controller;

import java.util.PriorityQueue;

import model.Evento;
import model.IntervaloChegadas;
import model.PC;
import model.TipoDistribuicao;

public class Simulador {
	
	public static PriorityQueue<Evento> filaEventos;

	public void executarCenario1() {
		System.out.println("Executando cenario 1...");
		PC PC1 = new PC(100);
		PC1.setP(40);
		PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		PC1.gerarEventos();
		
	}
	
	public void executarCenario2() {
			
	}
	
	public void executarCenario3() {
		
	}
	
	public void executarCenario4() {
		
	}
}
