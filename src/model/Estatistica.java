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
	
	private static Vector<Long> estatPC1 = new Vector<Long>();
	private static Vector<Long> estatPC2 = new Vector<Long>();
	private static Vector<Long> estatPC3 = new Vector<Long>();
	private static Vector<Long> estatPC4 = new Vector<Long>();
	
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
		
		if (evento.getTempo() >= Simulador.tamanhoFaseTransiente) {
			// A rodada atual no Simulador está sempre na frente, pois esta rotina é executada depois.
			while (evento != null && evento.getRodada() == rodadaAtual-1) {
				
				adicionarEstatisticaTAP(evento);
				adicionarEstatisticaTAM(evento);
				adicionarEstatisticaNCM(evento);
				
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
	}
	
	private static void adicionarEstatisticaTAP(Evento evento) {
		/*
		 * TAp = Tempo de Acesso:
		 * Intervalo entre o primeiro instante em que o quadro foi considerado
		 * para transmiss„o atÈ o inÌcio de sua transmiss„o em que ocorreu com sucesso.
		 */
		
		// SÛ interessa os eventos de Transmiss„o para o c·lculo
		if (evento instanceof Transmissao) {
			
			Transmissao trans = tap_transmissoesAbertas.get(evento.getQuadro());
			
			// Se o evento n„o colidiu, calculamos o TAp
			if (!evento.isColidido()) {
				
				// Se existe uma transmiss„o do quadro em aberto
				if (trans != null) {
					
					// TAp È o intervalo entre a primeira tentativa e essa com sucesso
					tap_valores.put( evento.getQuadro(), evento.getTempo() - trans.getTempo() );
					
				// Se n„o, È sinal que o quadro foi transmitido com sucesso na primeira tentativa
				} else {
					
					// TAp È Zero
					tap_valores.put( evento.getQuadro(), 0l );
				}
				
			// Se o evento colidiu
			} else {
				
				// E existe uma transmiss„o em aberto para o mesmo quadro,
				// ignora o evento atual pois ele sÛ È mais uma colis„o.
				// Mas caso n„o haja uma transmiss„o em aberto 
				if (trans == null) {
					
					// Acrescenta a transmiss„o atual na lista de transmissıes em aberto
					tap_transmissoesAbertas.put(evento.getQuadro(),(Transmissao) evento);
				}
			}
			
		}
	}
	
	private static void calcularTAP() {
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
		
		Long soma = 0l;
		for (Long valor : tap_valores.values()) {
			soma += valor;
		}
		float resultado = tap_valores.size() > 0 ? soma/tap_valores.size() : 0;
		System.out.println("E[TAp] = " + resultado);
	}
	
	private static void adicionarEstatisticaTAM(Evento evento) {
		/*
		 * TAm = Tempo de Acesso de uma Mensagem:
		 * Intervalo entre o instante em que o primeiro quadro da mensagem È
		 * considerado para transmiss„o atÈ o instante de inÌcio da transmiss„o
		 * com sucesso do ˙ltimo quadro da mensagem.
		 */
		
		// SÛ interessa os eventos de Transmiss„o para o c·lculo
		if (evento instanceof Transmissao) {
			
			Transmissao trans = tam_transmissoesAbertas.get(evento.getQuadro().getMensagem());
			
			Quadro ultimoQuadroMensagem = evento.getQuadro().getMensagem().getQuadros().get( evento.getQuadro().getMensagem().getQuadros().size()-1 );
			
			// Se o evento n„o colidiu e È do ultimo quadro da mensagem, calculamos o TAm
			if (!evento.isColidido() && evento.getQuadro().equals(ultimoQuadroMensagem)) {
				
				// Se n„o h· Transmiss„o em aberto
				// (ou seja, a Mensagem sÛ tem um quadro que foi enviado com sucesso na primeira vez)
				if (trans == null) {
					tam_valores.put( evento.getQuadro().getMensagem(), 0l );
					
				// Se n„o, a Transmiss„o em aberto existe porque a mensagem tem v·rios quadros
				} else {
					tam_valores.put( evento.getQuadro().getMensagem(), evento.getTempo() - trans.getTempo() );
				}
				
			// Se o evento colidiu
			} else {
				
				// E existe uma transmiss„o em aberto para a mesma mensagem,
				// ignora o evento atual pois ele sÛ È mais uma colis„o.
				// Mas caso n„o haja uma transmiss„o em aberto 
				if (trans == null) {
					
					// Acrescenta a transmiss„o atual na lista de transmissıes em aberto
					tam_transmissoesAbertas.put(evento.getQuadro().getMensagem(), (Transmissao) evento);
				}
			}
		}
	}
	
	private static void calcularTAM() {
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
		
		Long soma = 0l;
		
		for (Long valor : tam_valores.values()) {
			soma += valor;
		}
		float resultado = tam_valores.size() > 0 ? soma/tam_valores.size() : 0;
		System.out.println("E[TAm] = " + resultado);
	}
	
	private static void adicionarEstatisticaNCM(Evento evento) {
		/*
		 * NCm = N˙mero MÈdio de Colisıes por Quadro na EstaÁ„o
		 * Calculado dividindo o n˙mero de colisıes dos quadros de uma mensagem 
		 * pelo n˙mero de quadros da mensagem.
		 */
		
		// SÛ nos interessa os eventos de Transmissao que tenham terminado com sucesso (sem colisao)
		// Assim garantimos que contabilizaremos apenas 1 ˙nica vez cada quadro e suas colisoes
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
		
		float mediaColisoesPC1 = qtdQuadrosPC1 > 0 ? (float) qtdColisoesPC1/qtdQuadrosPC1 : 0;
		float mediaColisoesPC2 = qtdQuadrosPC2 > 0 ? (float) qtdColisoesPC2/qtdQuadrosPC2 : 0;
		float mediaColisoesPC3 = qtdQuadrosPC3 > 0 ? (float) qtdColisoesPC3/qtdQuadrosPC3 : 0;
		float mediaColisoesPC4 = qtdQuadrosPC4 > 0 ? (float) qtdColisoesPC4/qtdQuadrosPC4 : 0;
		
		System.out.println("NCm 1: " + mediaColisoesPC1 + " colisoes/quadro");
		System.out.println("NCm 2: " + mediaColisoesPC2 + " colisoes/quadro");
		System.out.println("NCm 3: " + mediaColisoesPC3 + " colisoes/quadro");
		System.out.println("NCm 4: " + mediaColisoesPC4 + " colisoes/quadro");
	}
	
	private static void calcularUtilizacao() {
		/*
		 * UtilizaÁ„o
		 * RelaÁ„o entre o tempo que o meio est· ocupado com alguma transmiss„o
		 * (quadro com sucesso, ou colis„o ou reforÁo de colis„o) e o tempo total
		 * de simulaÁ„o, desprezando o tempo da fase transiente. Utilizar a
		 * estaÁ„o 1 como referÍncia.
		 */
		float utilizacao = (float)(Simulador.tempoOcupado * 100) / (float)(Simulador.numeroDeRodadas * Simulador.getTamanhoRodada());
		System.out.println("Utilização do Ethernet: " + utilizacao + "%"); 
	}
	
	private static void calcularVazao() {
		long tempoSimulacao = (long) (Math.pow(10, -9) * Simulador.getTamanhoRodada() * Simulador.numeroDeRodadas);
		for (PC pc : Simulador.getPcsConectados()) {
			System.out.println("Vazão do PC de " + pc.getDistancia() + "m: " + pc.getQuadrosEnviados()/tempoSimulacao + " quadros/segundo");
		}
	}

	private static float somaTotal(Vector<Long> estatPC) {
		
		float total = 0;
		
		for(int i=0; i < estatPC.size(); i++){
			total += (Long) estatPC.get(i);
		}
		
		return 0;
	}
	
}
