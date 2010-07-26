package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import model.Estatistica;
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
	
	public static int numeroDeRodadas = 30;
	private static int rodadaAtual = 1;
	
	private static final long CONVERSAO_TEMPO = 1000000;
	
	public static File saida = new File("saida.txt");
	
	public static Long tamanhoFaseTransiente = 30000000000l;
	private HashMap<Long,Float> estatisticaFaseTransiente = new HashMap<Long,Float>();
	
	public void executarCenario(int cenario) throws IOException {
		
		PC PC1 = new PC(100), PC2 = new PC(80), PC3 = new PC(60), PC4 = new PC(40);
		
		System.out.println("Executando cenario " + cenario + "...");
		
		switch (cenario) {
			case 1:
				PC1.setP(40); // o correto Ž 40. O 4 foi somente para nao printar mta coisa por enquanto.
				PC1.setA(new IntervaloChegadas(80*CONVERSAO_TEMPO, TipoDistribuicao.DETERMINISTICO));
				
				PC2.setP(40); // o correto Ž 40. O 4 foi somente para nao printar mta coisa por enquanto.
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
		// Iniciando cabecalho do arquivo
		PrintWriter writer = new PrintWriter( new FileWriter(saida,true) );
		writer.println("RODADA\tPC\tMEDIA\tVALOR");
		writer.println();
		
		iniciarSimulacao();
	}
	
	private void iniciarSimulacao() {
		
		Evento primeiroEventoRodada = executarFaseTransiente();
		
		// inicio da simulacao - numeroDeRodadas tem a quantidade de execucoes
		for (rodadaAtual = 1; rodadaAtual <= numeroDeRodadas; rodadaAtual++) {
			
			// inicio da execucao dos eventos da rodada
			Evento evento = primeiroEventoRodada;
				
			while (evento!=null){
				
				System.out.println("TEMPO: " + evento.getTempo());
				
				// Se o evento eh da proxima rodada
				if (evento.getTempo() >= rodadaAtual * getTamanhoRodada() + getFaseTransiente()) {
					
					Estatistica.calcularEstatisticas(primeiroEventoRodada);
					
					primeiroEventoRodada = evento;
					break;
				}
				
				// FIXME ver se o evento eh da rodada para coleta de estatistica, passar rodada para evento.
				evento.executar();
				
				atualizarTransmissoesAbertas(evento);
				
				for (PC pc : pcsConectados) {
					pc.gerarMensagens(evento.getTempo(), rodadaAtual);
				}
								
				// Evento da proxima execucao (null se nao tiver mais eventos)
				evento = recuperarProximoEvento(evento);
			}
		}
	}
	
	
	private Evento executarFaseTransiente() {
		/*
		 * Retorna o ultimo evento criado na analise da fase transiente.
		 */
		System.out.println("[Inicio da Fase Transiente]");		
		
		for (PC pc : pcsConectados) {
			pc.gerarMensagens(00,0);
		}
		
		rodadaAtual = 0;
		
		// inicio da execucao dos eventos da rodada
		Evento evento = filaEventos.first();
			
		while (evento!=null){
			
			System.out.println("TEMPO DO EVENTO FASE TRANSIENTE: " + evento.getTempo() + " (" + evento.getClass() + ")");
			
			evento.executar();
			
			atualizarTransmissoesAbertas(evento);
			
			for (PC pc : pcsConectados) {
				pc.gerarMensagens(evento.getTempo(), rodadaAtual);
			}
			
			atualizarEstatisticasFaseTransiente(evento);
			
			evento = recuperarProximoEvento(evento);
			
			if (sairFaseTransiente(evento)) {
				tamanhoFaseTransiente = evento.getTempo()-1;
				System.out.println("[Fim da Fase Transiente]");
				return evento;
			}
		}
		return null;
	}

	private void atualizarEstatisticasFaseTransiente(Evento evento) {
		
		// Divis�o inteira
		Long janela = (evento.getTempo() / getTamanhoRodada()) + 1;
		
		Long janelaProxEvento = (recuperarProximoEvento(evento).getTempo() / getTamanhoRodada()) + 1;
		
		if (!janela.equals(janelaProxEvento)) {
			estatisticaFaseTransiente.put( janela, calcularEstatisticaJanela(evento) );
		}
	}

	private Float calcularEstatisticaJanela(Evento ultimoEventoJanela) {
		
		int qtdQuadros = 0;
		int qtdColisoes = 0;
		
		Long janelaAtual = (ultimoEventoJanela.getTempo() / getTamanhoRodada()) + 1;
		
		Evento evento = ultimoEventoJanela;
		
		while ( evento != null && (evento.getTempo()/getTamanhoRodada())+1 == janelaAtual) {
			
			// Calcula estatistica
			if (evento instanceof Transmissao && !evento.isColidido()) {
				qtdQuadros++;
				qtdColisoes += evento.getQuadro().getNumeroDeColisoes();
			}
			
			evento = filaEventos.lower(evento);
		}
		
		return (float) qtdColisoes/qtdQuadros;
	}

	private boolean sairFaseTransiente(Evento evento) {
		
		Long janelaAtual = (evento.getTempo() / getTamanhoRodada()) + 1;
		
		if (janelaAtual >= 4) {
			
			double mediaQuadrado = (Math.pow(estatisticaFaseTransiente.get(janelaAtual-1),2) + Math.pow(estatisticaFaseTransiente.get(janelaAtual-2),2) + Math.pow(estatisticaFaseTransiente.get(janelaAtual-3),2))/3;
			
			double mediaX = (estatisticaFaseTransiente.get(janelaAtual-1) + estatisticaFaseTransiente.get(janelaAtual-2) + estatisticaFaseTransiente.get(janelaAtual-3))/3;
			
			double var = mediaQuadrado - Math.pow(mediaX,2);
			
			return (var <= 0.001);
			
		} else {
			return false;
		}
	}

	private Evento recuperarProximoEvento(Evento evento) {
		
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
			return filaEventos.higher(evento);
		} else {
			return proximoEvento;
		}
	}

	private void atualizarTransmissoesAbertas(Evento evento) {
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
	}

	private Long getFaseTransiente() {
		return tamanhoFaseTransiente;
	}

	public static Long getTamanhoRodada() {
		return (long) Math.pow(10, 9);
	}

	public static List<PC> getPcsConectados() {
		return pcsConectados;
	}

	public static int getRodadaAtual() {
		return rodadaAtual;
	}
	
}
