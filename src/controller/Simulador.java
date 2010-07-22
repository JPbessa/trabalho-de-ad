package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import model.Evento;
import model.IntervaloChegadas;
import model.PC;
import model.Recepcao;
import model.TipoDistribuicao;
import model.Transmissao;

public class Simulador {
	
	public static int velocidadeEthernet = (int)Math.pow(10, 7);
	public static TreeSet<Evento> filaEventos = new TreeSet<Evento>();
	public static HashMap<PC,List<Transmissao>> transmissoesAbertas = new HashMap<PC,List<Transmissao>>();
	
	private static List<PC> pcsConectados = new ArrayList<PC>();
	
	private int numeroDeRodadas = 2;
	private static int rodadaAtual = 1;
	
	private static final long CONVERSAO_TEMPO = 1000000;
	
	public void executarCenario(int cenario) {

		PC PC1 = new PC(100), PC2 = new PC(80), PC3 = new PC(60), PC4 = new PC(40);
		
		System.out.println("Executando cenario " + cenario + "...");
		
		switch (cenario) {
			case 1:
				PC1.setP(2); // o correto � 40. O 4 foi somente para nao printar mta coisa por enquanto.
				PC1.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(1); // o correto � 40. O 4 foi somente para nao printar mta coisa por enquanto.
				PC2.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				
				break;
			case 2:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.EXPONENCIAL));
				
				PC2.setP(40);
				PC2.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.EXPONENCIAL));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				
				break;
			case 3:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(1);
				PC2.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC3.setP(1);
				PC3.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC4.setP(1);
				PC4.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				pcsConectados.add(PC1);
				pcsConectados.add(PC2);
				pcsConectados.add(PC3);
				pcsConectados.add(PC4);
				
				break;
			case 4:
				PC1.setP(40);
				PC1.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(1);
				PC2.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.EXPONENCIAL));
				
				PC3.setP(1);
				PC3.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.EXPONENCIAL));
				
				PC4.setP(1);
				PC4.setA(new IntervaloChegadas(16*CONVERSAO_TEMPO, TipoDistribuicao.EXPONENCIAL));
				
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
		
		Evento primeiroEventoRodada = executarFaseTransiente();
		
		// inicio da simulacao - numeroDeRodadas tem a quantidade de execucoes
		for (rodadaAtual = 1; rodadaAtual <= numeroDeRodadas; rodadaAtual++) {
			 
			// geracao de eventos antes do inicio da simulacao
//			for (PC pc : pcsConectados) {
//				pc.gerarMensagens(rodadaAtual * getTamanhoRodada() + getFaseTransiente(), rodadaAtual);
//			}
			
			// inicio da execucao dos eventos da rodada
			Evento evento = primeiroEventoRodada;
				
			while (evento!=null){
				
				System.out.println("TEMPO: " + evento.getTempo());
				
				// Se o evento eh da proxima rodada
				if (evento.getTempo() >= rodadaAtual * getTamanhoRodada() + getFaseTransiente()) {
					primeiroEventoRodada = evento;
					break;
				}
				
				// FIXME ver se o evento eh da rodada para coleta de estatistica, passar rodada para evento.
				if (!evento.isColidido())
					evento.executar();
				
				if (evento instanceof Transmissao && !evento.isColidido()) {
					
					for (PC comp: Simulador.pcsConectados) {
						List<Transmissao> transmissoes = transmissoesAbertas.get(comp);
						if (transmissoes == null) {
							transmissoes = new ArrayList<Transmissao>();
							transmissoesAbertas.put(comp,transmissoes);
						}
						transmissoes.add((Transmissao)evento);
					}
					
				} else if (evento instanceof Recepcao && !evento.isColidido()) {
					
					List<Transmissao> transmissoes = transmissoesAbertas.get(evento.getPc());
					transmissoes.remove(((Recepcao) evento).getTransmissao());
					
				}
				
				for (PC pc : pcsConectados) {
					pc.gerarMensagens(evento.getTempo(), rodadaAtual);
				}
								
				// Evento da proxima execucao (null se nao tiver mais eventos)
				Evento proximoEvento = filaEventos.higher(evento); 
				if (proximoEvento == null) {
					int tam = filaEventos.size();
					Long tempoAvancando = evento.getTempo();
					while (filaEventos.size() == tam) {
						tempoAvancando++;
						for (PC pc : pcsConectados) {
							pc.gerarMensagens(tempoAvancando, rodadaAtual);
						}
					}
					evento = filaEventos.higher(evento);
				} else {
					evento = proximoEvento;
				}
			}
		}
	}
	
	private Evento executarFaseTransiente() {
		//FIXME FASE TRANSIENTE
		System.out.println("[Inicio da Fase Transiente]");		
		for (PC pc : pcsConectados) {
			pc.gerarMensagens(00,0);
		}
		System.out.println("[Fim da Fase Transiente]");
		return filaEventos.first();
	}

	private Long getFaseTransiente() {
		return new Long(0);
	}

	private Long getTamanhoRodada() {
		return (long) Math.pow(10, 8);
	}

	public static List<PC> getPcsConectados() {
		return pcsConectados;
	}

	public static int getRodadaAtual() {
		return rodadaAtual;
	}
	
}
