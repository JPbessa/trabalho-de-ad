package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	// TAp
	private static HashMap<Quadro,Transmissao> tap_transmissoesAbertas = new HashMap<Quadro,Transmissao>();
	private static HashMap<Quadro,Long> tap_valores = new HashMap<Quadro,Long>();
	
	// TAm
	private static HashMap<Mensagem,Transmissao> tam_transmissoesAbertas = new HashMap<Mensagem,Transmissao>();
	private static HashMap<Mensagem,Long> tam_valores = new HashMap<Mensagem,Long>();
	
	// NCm
	private static Map<PC, List<Quadro>> quadrosPorPc = new HashMap<PC, List<Quadro>>();
	
	// Utilizacao
	private static Long utilizacao = 0l;
	
	public static void calcularEstatisticas(Evento primeiroEventoRodada) {
		int rodadaAtual = Simulador.getRodadaAtual();
		Evento evento = primeiroEventoRodada;
		
		while (evento != null){
			adicionarEstatisticaTAP(evento);
			adicionarEstatisticaTAM(evento);
			adicionarEstatisticaNCM(evento);
			adicionarEstatisticaUtilizacao(evento);
			
			evento = Simulador.filaEventos.higher(evento);
		}
		
		if (rodadaAtual == Simulador.numeroDeRodadas) {
			gerarRelatorio();
		}
	}
	
	private static void gerarRelatorio() {
		try {
			writer = new PrintWriter( new FileWriter(Simulador.saida,true) );
			
			esperancaTAP();
			esperancaTAM();
			esperancaNCM();
			calcularUtilizacao();
			calcularVazao();
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void adicionarEstatisticaTAP(Evento evento) {
		// Soh interessa os eventos de Transmissao para o calculo
		if (evento instanceof Transmissao) {

			Transmissao trans = tap_transmissoesAbertas.get(evento.getQuadro());

			// Se o evento nao colidiu, calculamos o TAp
			if (!evento.isColidido()) {
				// Se existe uma transmissao do quadro em aberto
				if (trans != null) {
					// TAp eh o intervalo entre a primeira tentativa e essa com sucesso
					tap_valores.put( evento.getQuadro(), evento.getTempo() - trans.getTempo() );
					// Se nao, eh sinal que o quadro foi transmitido com sucesso na primeira tentativa
				} else {
					// TAp eh Zero
					tap_valores.put( evento.getQuadro(), 0l );
				}
			// Se o evento colidiu
			} else {

				// E existe uma transmissao em aberto para o mesmo quadro,
				// ignora o evento atual pois ele soh eh mais uma colisao.
				// Mas caso nao haja uma transmissao em aberto
				if (trans == null) {
					// Acrescenta a transmissao atual na lista de transmissoes em aberto
					tap_transmissoesAbertas.put(evento.getQuadro(),(Transmissao) evento);
				}
			}

		}
	}
	
	private static void adicionarEstatisticaTAM(Evento evento) {
		if (evento instanceof Transmissao) {
			Mensagem mensagem = evento.getQuadro().getMensagem();
			Transmissao transmissaoPrimeiroQuadro = tam_transmissoesAbertas.get(mensagem);
			
			if (transmissaoPrimeiroQuadro == null) {
				// Este eh o primeiro quadro desta mensagem.
				tam_transmissoesAbertas.put(mensagem, (Transmissao)evento);
			} else {
				Long tempoGasto = evento.getTempo() - transmissaoPrimeiroQuadro.getTempo();
				Long tempoAnterior = tam_valores.get(mensagem) != null ? tam_valores.get(mensagem) : 0l; 
				if (tempoGasto > tempoAnterior) {
					tam_valores.put(mensagem, tempoGasto);
				}
			}
		}
	}
	
	private static void adicionarEstatisticaNCM(Evento evento) {
		if (evento instanceof Recepcao) {
			PC emissor = evento.getQuadro().getMensagem().getEmissor();
			if (!quadrosPorPc.containsKey(emissor)) {
				quadrosPorPc.put(emissor, new ArrayList<Quadro>());
			}
			if (!quadrosPorPc.get(emissor).contains(evento.getQuadro())) {
				quadrosPorPc.get(emissor).add(evento.getQuadro());
			}
		}
	}
	
	private static void adicionarEstatisticaUtilizacao(Evento evento) {
		utilizacao += evento.getPc().getTempoDeTransmissao();
	}
	
	private static void esperancaTAP() {
		for (PC pc : Simulador.getPcsConectados()) {
			Long soma = 0l; float resultado = 0;
			String output;
			for (Quadro quadro : tap_valores.keySet()) {
				if (quadro.getMensagem().getEmissor().equals(pc)) {
					soma += tap_valores.get(quadro);
				}
			}
			resultado = tap_valores.size() > 0 ? (float)soma/(float)tap_valores.size() : 0;
			output = "TAp da estacao " + pc + ": " + resultado*Math.pow(10, -6) + "ms";
			imprimir(output);
			soma = 0l;
		}
	}
	
	private static void esperancaTAM() {
		for (PC pc : Simulador.getPcsConectados()) {
			Long soma = 0l; float resultado = 0;
			String output;
			for (Mensagem mensagem : tam_valores.keySet()) {
				if (mensagem.getEmissor().equals(pc)) {
					soma += tam_valores.get(mensagem);
				}
			}
			resultado = tam_valores.size() > 0 ? (float)soma/(float)tam_valores.size() : 0;
			output = "TAm da estacao " + pc + ": " + resultado*Math.pow(10, -6) + "ms";
			imprimir(output);
			soma = 0l;
		}
	}
	
	private static void esperancaNCM() {
		int soma = 0;
		String output;
		for (PC pc : Simulador.getPcsConectados()) {
			if (quadrosPorPc.containsKey(pc)) {
				for (Quadro quadro : quadrosPorPc.get(pc)) {
					soma += quadro.getNumeroDeColisoes();
				}
				output = "NCm da estacao " + pc + ": " + ((float)soma/(float)quadrosPorPc.get(pc).size()) + " colisoes/quadro";
			} else {
				output = "NCm da estacao " + pc + ": 0.0 colisoes/quadro";
			}
			
			imprimir(output);
			soma = 0;
		}
	}
	
	private static void calcularUtilizacao() {
		 float ro = (float)(utilizacao) / (float)(Simulador.numeroDeRodadas * Simulador.getTamanhoRodada());
		 String output = "Utilizacao do Ethernet: " + ro*100 + "%";
		 imprimir(output); 
	}
	
	private static void calcularVazao() {
		 long tempoSimulacao = (long) (Math.pow(10, -9) * Simulador.getTamanhoRodada() * Simulador.numeroDeRodadas);
		 for (PC pc : Simulador.getPcsConectados()) {
			 String output = "Vazao da estacao " + pc + ": " + pc.getQuadrosEnviados()/tempoSimulacao + " quadros/segundo";
			 imprimir(output);
		 }
	}
	
	private static void imprimir(String output) {
		System.out.println(output);
		writer.println(output);
	}
}
