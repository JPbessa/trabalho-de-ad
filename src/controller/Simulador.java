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
	private int numeroDeRodadas = 1;
	
	public void executarCenario(int cenario) {

		PC PC1 = new PC(100), PC2 = new PC(80), PC3 = new PC(60), PC4 = new PC(40);
		
		inicioSimulacao = now();
		System.out.println("Executando cenario " + cenario + "... (" + inicioSimulacao + ")");
		
		switch (cenario) {
			case 1:
				PC1.setP(4); // o correto Ž 40. O 4 foi somente para nao printar mta coisa por enquanto.
				PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(4); // o correto Ž 40. O 4 foi somente para nao printar mta coisa por enquanto.
				PC2.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				
				break;
			case 2:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.EXPONENCIAL));
				
				PC2.setP(40);
				PC2.setA(new IntervaloChegadas(80, TipoDistribuicao.EXPONENCIAL));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				
				break;
			case 3:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(1);
				PC2.setA(new IntervaloChegadas(16, TipoDistribuicao.DETERMINISTICO));
				
				PC3.setP(1);
				PC3.setA(new IntervaloChegadas(16, TipoDistribuicao.DETERMINISTICO));
				
				PC4.setP(1);
				PC4.setA(new IntervaloChegadas(16, TipoDistribuicao.DETERMINISTICO));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				pcsConectados.add(PC3);
				pcsConectados.add(PC4);
				
				break;
			case 4:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(1);
				PC2.setA(new IntervaloChegadas(16, TipoDistribuicao.EXPONENCIAL));
				
				PC3.setP(1);
				PC3.setA(new IntervaloChegadas(16, TipoDistribuicao.EXPONENCIAL));
				
				PC4.setP(1);
				PC4.setA(new IntervaloChegadas(16, TipoDistribuicao.EXPONENCIAL));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				pcsConectados.add(PC3);
				pcsConectados.add(PC4);
				
				break;
			default:
				System.out.println("Cenario Invalido");
				System.exit(0);
				break;
		}
		
		iniciarSimulacao();
	}
	
	private void iniciarSimulacao() {

		int rodada;
		for (rodada = 1; rodada <= numeroDeRodadas; rodada++) {
			for (PC pc : pcsConectados) {
				pc.gerarEventos(rodada);
			}
			for (Evento evento : filaEventos) {
				if (evento.getTipo() == TipoEvento.EMISSAO) {
					evento.getQuadro().transmitir();
				} else if (evento.getTipo() == TipoEvento.RECEPCAO) {
					evento.getQuadro().receber();
				}
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
