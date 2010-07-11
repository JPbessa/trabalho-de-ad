package controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import model.Evento;
import model.IntervaloChegadas;
import model.PC;
import model.TipoDistribuicao;
import model.TipoEvento;

public class Simulador {
	
	public static int velocidadeEthernet = (int)Math.pow(10, 7);
	public static Long inicioSimulacao;
	public static PriorityBlockingQueue<Evento> filaEventos = new PriorityBlockingQueue<Evento>();
	
	private static List<PC> pcsConectados = new ArrayList<PC>();
	private int numeroDeRodadas = 5;
	
	public void executarCenario1() {
		inicioSimulacao = now();
		
		System.out.println("Executando cenario 1... (" + inicioSimulacao + ")");
				
		PC PC1 = new PC(100);
		PC1.setP(4);
		PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
		
		pcsConectados.add(PC1);
		
		int rodada;
		//for (rodada = 1; rodada <= numeroDeRodadas; rodada++) {
			PC1.gerarEventos(/*rodada*/1);
			iniciarSimulacao();
		//}
		
	}
	
	private void iniciarSimulacao() {
		for (Evento evento : filaEventos) {
			if (evento.getTipo() == TipoEvento.EMISSAO) {
				evento.getQuadro().transmitir();
			} else if (evento.getTipo() == TipoEvento.RECEPCAO) {
				evento.getQuadro().receber();
			}
		}
	}
	
	private static Long now() {
		Calendar cal = Calendar.getInstance();
		return new Long(cal.getTimeInMillis() * (int)Math.pow(10, 6)); // em ns
	}

	public static List<PC> getPcsConectados() {
		return pcsConectados;
	}
	
}
