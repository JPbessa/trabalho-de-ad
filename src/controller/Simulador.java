package controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.PriorityQueue;

import model.Evento;
import model.IntervaloChegadas;
import model.PC;
import model.TipoDistribuicao;

public class Simulador {
	
	public static PriorityQueue<Evento> filaEventos = new PriorityQueue<Evento>();
	
	public static Long inicioSimulacao;
	
	private static List<PC> pcsConectados = new ArrayList<PC>();
	
	public void executarCenario1() {
		inicioSimulacao = now();
		
		System.out.println("Executando cenario 1... (" + inicioSimulacao + ")");
				
		PC PC1 = new PC(100);
		PC1.setP(4);
		PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		pcsConectados.add(PC1);
		
		PC1.gerarEventos();
		
		System.out.println("[PC1] Media TAP = " + PC1.calculaMediaTAP());
	}
	
	public static Long now() {
		Calendar cal = Calendar.getInstance();
		return new Long(cal.getTimeInMillis() * (int)Math.pow(10, 6)); // em ns
	    
	}

	public static List<PC> getPcsConectados() {
		return pcsConectados;
	}
	
}
