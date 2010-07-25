package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;

import controller.Simulador;

public class Estatistica {
	
	static {
		File saida = new File("saida.txt");
		if (saida.canRead()) {
			saida.delete();
			System.out.println("Arquivo de saida apagado.");
		}
	}
	
	private static PrintWriter writer;
	
	private static Vector estatPC1 = new Vector();
	private static Vector estatPC2 = new Vector();
	private static Vector estatPC3 = new Vector();
	private static Vector estatPC4 = new Vector();
	
	// Estruturas utilizadas no calculo do TAp
	private static HashMap<Quadro,Transmissao> tap_transmissoesAbertas = new HashMap<Quadro,Transmissao>();
	private static HashMap<Quadro,Long> tap_valores = new HashMap<Quadro,Long>();

	// Estruturas utilizadas no calculo do TAm
	private static HashMap<Mensagem,Transmissao> tam_transmissoesAbertas = new HashMap<Mensagem,Transmissao>();
	private static HashMap<Mensagem,Long> tam_valores = new HashMap<Mensagem,Long>();
	
	// Estruturas utilizadas no calculo do Ncm
	private static int qtdColisoesPC1=0, qtdColisoesPC2=0, qtdColisoesPC3=0, qtdColisoesPC4=0; 
	private static int qtdQuadrosPC1=0, qtdQuadrosPC2=0, qtdQuadrosPC3=0, qtdQuadrosPC4=0;
	
	public static void calcularEstatisticas(Evento primeiroEventoRodada) {
		
		int rodadaAtual = Simulador.getRodadaAtual();
		long tamanhoRodada = Simulador.getTamanhoRodada();
		long tempoInicialRodada = (rodadaAtual - 1) * tamanhoRodada;
		long tempoFinalRodada = rodadaAtual * tamanhoRodada;
		
		Evento evento = primeiroEventoRodada;
		
		while (evento != null && evento.getTempo() >= tempoInicialRodada && evento.getTempo() <= tempoFinalRodada) {
			
			adicionarEstatisticaTAP(evento);
			adicionarEstatisticaTAM(evento);
			adicionarEstatisticaNCM(evento);
			adicionarEstatisticaUtilizacao(evento);
			
			evento = Simulador.filaEventos.higher(evento);
		}
		
		try {
			
			writer = new PrintWriter( new FileWriter(Simulador.saida,true) );
			
			calcularTAP();
			calcularTAM();
			calcularNCM();
			calcularUtilizacao();
			calcularVazao();
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void adicionarEstatisticaTAP(Evento evento) {
		/*
		 * TAp = Tempo de Acesso:
		 * Intervalo entre o primeiro instante em que o quadro foi considerado
		 * para transmissão até o início de sua transmissão em que ocorreu com sucesso.
		 */
		
		// Só interessa os eventos de Transmissão para o cálculo
		if (evento instanceof Transmissao) {
			
			Transmissao trans = tap_transmissoesAbertas.get(evento.getQuadro());
			
			// Se o evento não colidiu, calculamos o TAp
			if (!evento.isColidido()) {
				
				// Se existe uma transmissão do quadro em aberto
				if (trans != null) {
					
					// TAp é o intervalo entre a primeira tentativa e essa com sucesso
					tap_valores.put( evento.getQuadro(), evento.getTempo() - trans.getTempo() );
					
				// Se não, é sinal que o quadro foi transmitido com sucesso na primeira tentativa
				} else {
					
					// TAp é Zero
					tap_valores.put( evento.getQuadro(), 0l );
				}
				
			// Se o evento colidiu
			} else {
				
				// E existe uma transmissão em aberto para o mesmo quadro,
				// ignora o evento atual pois ele só é mais uma colisão.
				// Mas caso não haja uma transmissão em aberto 
				if (trans == null) {
					
					// Acrescenta a transmissão atual na lista de transmissões em aberto
					tap_transmissoesAbertas.put(evento.getQuadro(),(Transmissao) evento);
				}
			}
			
		}
	}
	
	private static void calcularTAP() {
		long tapTotal = 0;
		int qtdQuad = 0;
		for (Entry<Quadro, Long> reg: tap_valores.entrySet()) {
			qtdQuad++;
			switch(reg.getKey().getMensagem().getEmissor().getDistancia()){
				case 100:
					estatPC1.add(reg.getValue());
					break;
				case 80:
					estatPC2.add(reg.getValue());
					break;
				case 60:
					estatPC3.add(reg.getValue());
					break;
				case 40:
					estatPC4.add(reg.getValue());
					break;
			}
		}
		if(!estatPC1.isEmpty()) writer.println(Simulador.getRodadaAtual() + "\tPC 100\tE[TAp]\t" + ((float)somaTotal(estatPC1)/estatPC1.size()));
		if(!estatPC2.isEmpty()) writer.println(Simulador.getRodadaAtual() + "\tPC 80\tE[TAp]\t" + ((float)somaTotal(estatPC2)/estatPC2.size()));
		if(!estatPC3.isEmpty()) writer.println(Simulador.getRodadaAtual() + "\tPC 60\tE[TAp]\t" + ((float)somaTotal(estatPC3)/estatPC3.size()));
		if(!estatPC4.isEmpty()) writer.println(Simulador.getRodadaAtual() + "\tPC 40\tE[TAp]\t" + ((float)somaTotal(estatPC4)/estatPC4.size()));
		
		estatPC1.clear();
		estatPC2.clear();
		estatPC3.clear();
		estatPC4.clear();
		
		System.out.println("TAp (Quadro=TAp): " + tap_valores);
	}
	
	private static void adicionarEstatisticaTAM(Evento evento) {
		/*
		 * TAm = Tempo de Acesso de uma Mensagem:
		 * Intervalo entre o instante em que o primeiro quadro da mensagem é
		 * considerado para transmissão até o instante de início da transmissão
		 * com sucesso do último quadro da mensagem.
		 */
		
		// Só interessa os eventos de Transmissão para o cálculo
		if (evento instanceof Transmissao) {
			
			Transmissao trans = tam_transmissoesAbertas.get(evento.getQuadro().getMensagem());
			
			Quadro ultimoQuadroMensagem = evento.getQuadro().getMensagem().getQuadros().get( evento.getQuadro().getMensagem().getQuadros().size()-1 );
			
			// Se o evento não colidiu e é do ultimo quadro da mensagem, calculamos o TAm
			if (!evento.isColidido() && evento.getQuadro().equals(ultimoQuadroMensagem)) {
				
				// Se não há Transmissão em aberto
				// (ou seja, a Mensagem só tem um quadro que foi enviado com sucesso na primeira vez)
				if (trans == null) {
					tam_valores.put( evento.getQuadro().getMensagem(), 0l );
					
				// Se não, a Transmissão em aberto existe porque a mensagem tem vários quadros
				} else {
					tam_valores.put( evento.getQuadro().getMensagem(), evento.getTempo() - trans.getTempo() );
				}
				
			// Se o evento colidiu
			} else {
				
				// E existe uma transmissão em aberto para a mesma mensagem,
				// ignora o evento atual pois ele só é mais uma colisão.
				// Mas caso não haja uma transmissão em aberto 
				if (trans == null) {
					
					// Acrescenta a transmissão atual na lista de transmissões em aberto
					tam_transmissoesAbertas.put(evento.getQuadro().getMensagem(), (Transmissao) evento);
				}
			}
		}
	}
	
	private static void calcularTAM() {
		long tamTotal = 0;
		int qtdMsg = 0;
		for (Entry<Mensagem,Long> reg: tam_valores.entrySet()) {
			qtdMsg++;
			switch(reg.getKey().getEmissor().getDistancia()){
			case 100:
				estatPC1.add(reg.getValue());
				break;
			case 80:
				estatPC2.add(reg.getValue());
				break;
			case 60:
				estatPC3.add(reg.getValue());
				break;
			case 40:
				estatPC4.add(reg.getValue());
				break;
		}
		}
		if(!estatPC1.isEmpty()) writer.println("Rodada: " + Simulador.getRodadaAtual() + "\tPC 100\tE[TAm]\t" + ((float)somaTotal(estatPC1)/estatPC1.size()));
		if(!estatPC2.isEmpty())writer.println("Rodada: " + Simulador.getRodadaAtual() + "\tPC 80\tE[TAm]\t" + ((float)somaTotal(estatPC2)/estatPC2.size()));
		if(!estatPC3.isEmpty())writer.println("Rodada: " + Simulador.getRodadaAtual() + "\tPC 60\tE[TAm]\t" + ((float)somaTotal(estatPC3)/estatPC3.size()));
		if(!estatPC4.isEmpty())writer.println("Rodada: " + Simulador.getRodadaAtual() + "\tPC 40\tE[TAm]\t" + ((float)somaTotal(estatPC4)/estatPC4.size()));
		
		estatPC1.clear();
		estatPC2.clear();
		estatPC3.clear();
		estatPC4.clear();
		
		System.out.println("TAm (Mensagem=TAm): " + tam_valores);
	}
	
	private static void adicionarEstatisticaNCM(Evento evento) {
		/*
		 * NCm = Número Médio de Colisões por Quadro na Estação
		 * Calculado dividindo o número de colisões dos quadros de uma mensagem 
		 * pelo número de quadros da mensagem.
		 */
		
		// Só nos interessa os eventos de Transmissao que tenham terminado com sucesso (sem colisao)
		// Assim garantimos que contabilizaremos apenas 1 única vez cada quadro e suas colisoes
		if (evento instanceof Transmissao && !evento.isColidido()) {
			switch(evento.getQuadro().getMensagem().getEmissor().getDistancia()){
			case 100:
				qtdQuadrosPC1++;
				qtdColisoesPC1 += evento.getQuadro().getNumeroDeColisoes();
				break;
			case 80:
				qtdQuadrosPC2++;
				qtdColisoesPC2 += evento.getQuadro().getNumeroDeColisoes();
				break;
			case 60:
				qtdQuadrosPC3++;
				qtdColisoesPC3 += evento.getQuadro().getNumeroDeColisoes();
				break;
			case 40:
				qtdQuadrosPC4++;
				qtdColisoesPC4 += evento.getQuadro().getNumeroDeColisoes();
				break;
			}
		}
	}
	
	private static void calcularNCM() {
		
		float mediaColisoesPC1 = (float) qtdColisoesPC1/qtdQuadrosPC1;
		float mediaColisoesPC2 = (float) qtdColisoesPC2/qtdQuadrosPC2;
		float mediaColisoesPC3 = (float) qtdColisoesPC3/qtdQuadrosPC3;
		float mediaColisoesPC4 = (float) qtdColisoesPC4/qtdQuadrosPC4;
		
		writer.println("NCm:\t" + mediaColisoesPC1 + "\t colisoes/quadro");
		writer.println("NCm:\t" + mediaColisoesPC2 + "\t colisoes/quadro");
		writer.println("NCm:\t" + mediaColisoesPC3 + "\t colisoes/quadro");
		writer.println("NCm:\t" + mediaColisoesPC4 + "\t colisoes/quadro");
		writer.println();
		
		if (qtdQuadrosPC1 != 0 && qtdQuadrosPC2 != 0 && qtdQuadrosPC3 != 0 && qtdQuadrosPC4 != 0) {
			System.out.println("NCm: " + mediaColisoesPC1 + " colisoes/quadro");
			System.out.println("NCm: " + mediaColisoesPC2 + " colisoes/quadro");
			System.out.println("NCm: " + mediaColisoesPC3 + " colisoes/quadro");
			System.out.println("NCm: " + mediaColisoesPC4 + " colisoes/quadro");
		} else {
			System.out.println("Nao foi possivel calcular o NCm.");
		}
	}
	
	private static void adicionarEstatisticaUtilizacao(Evento evento) {
		/*
		 * Utilização
		 * Relação entre o tempo que o meio está ocupado com alguma transmissão
		 * (quadro com sucesso, ou colisão ou reforço de colisão) e o tempo total
		 * de simulação, desprezando o tempo da fase transiente. Utilizar a
		 * estação 1 como referência.
		 */
		
		
	}
	
	private static void calcularUtilizacao() {
		
	}
	
	private static void calcularVazao() {
		long tempoSimulacao = (long) (Math.pow(10, -9) * Simulador.getTamanhoRodada() * Simulador.numeroDeRodadas);
		for (PC pc : Simulador.getPcsConectados()) {
			System.out.println("Vaz‹o do PC de " + pc.getDistancia() + "m: " + pc.getQuadrosEnviados()/tempoSimulacao);
		}
	}

	private static float somaTotal(Vector estatPC) {
		
		float total = 0;
		
		for(int i=0; i < estatPC.size(); i++){
			total += (Long) estatPC.get(i);
		}
		
		return 0;
	}
	
}
