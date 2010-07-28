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
	
	private static Map<PC, List<Quadro>> quadrosPorPc = new HashMap<PC, List<Quadro>>();
	
	public static void calcularEstatisticas(Evento primeiroEventoProximaRodada) {
		int rodadaAtual = Simulador.getRodadaAtual();
		Evento evento = primeiroEventoProximaRodada;
		
		while (evento.getRodada() == rodadaAtual){
			evento = Simulador.filaEventos.lower(evento); // Calculando as estatisticas de tras para frente.
			
			//adicionarEstatisticaTAP(evento);
			//adicionarEstatisticaTAM(evento);
			adicionarEstatisticaNCM(evento);
		}
		
		if (rodadaAtual == Simulador.numeroDeRodadas) {
			gerarRelatorio();
		}
	}
	
	private static void gerarRelatorio() {
		try {
			writer = new PrintWriter( new FileWriter(Simulador.saida,true) );
			
			//calcularTAP();
			//calcularTAM();
			calcularNCM();
			calcularUtilizacao();
			calcularVazao();
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
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
	
	private static void calcularNCM() {
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
			
			System.out.println(output);
			writer.println(output);
			soma = 0;
		}
	}
	
	private static void calcularUtilizacao() {
		 float utilizacao = (float)(Simulador.tempoOcupado * 100) / (float)(Simulador.numeroDeRodadas * Simulador.getTamanhoRodada());
		 String output = "Utilizacao do Ethernet: " + utilizacao + "%";
		 System.out.println(output); 
		 writer.println(output);
	}
	
	private static void calcularVazao() {
		 long tempoSimulacao = (long) (Math.pow(10, -9) * Simulador.getTamanhoRodada() * Simulador.numeroDeRodadas);
		 for (PC pc : Simulador.getPcsConectados()) {
			 String output = "Vazao da estacao " + pc + ": " + pc.getQuadrosEnviados()/tempoSimulacao + " quadros/segundo";
			 System.out.println(output);
			 writer.println(output);
		 }
	}
}
