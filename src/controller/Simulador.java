package controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import model.Evento;
import model.IntervaloChegadas;
import model.PC;
import model.TipoDistribuicao;

public class Simulador {
	
	public static PriorityQueue<Evento> filaEventos = new PriorityQueue<Evento>();
	public static long inicioSimulacao;
	
	public void executarCenario1() {
		inicioSimulacao = now();
		
		System.out.println("Executando cenario 1... (" + inicioSimulacao + ")");
				
		PC PC1 = new PC(100);
		PC1.setP(40);
		PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		PC1.gerarEventos();
		
		System.out.println("[PC1] Media TAP = " + PC1.calculaMediaTAP());
	}
	
	public static long now() {
		Calendar cal = Calendar.getInstance();
	    //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	    return cal.getTimeInMillis();
	}

	public void executarCenario2() {
			
	}
	
	public void executarCenario3() {
		
	}
	
	public void executarCenario4() {
		
	}
	
	private void executarSimulacao() {
		Iterator it = filaEventos.iterator();
		Evento e;
		while(it.hasNext()){
			e = (Evento) it.next();
			
		}
	}
}
