package model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.moment.Variance;

import controller.Simulador;

public class Estatistica {
	
	public static Map<Integer,Double> mediasRodadas = new HashMap<Integer,Double>();
	
	public static void calcularMediaRodada() {
		
		/*
		 * Calcula a media dos eventos de recepcao gerados em uma determinada rodada
		 */
		
		int rodadaAtual = Simulador.getRodadaAtual();
		long tamanhoRodada = Simulador.getTamanhoRodada();
		
		long tempoInicialRodada = (rodadaAtual - 1) * tamanhoRodada;
		long tempoFinalRodada = rodadaAtual * tamanhoRodada;
		double soma = 0;
		
		for (Evento evento : Simulador.filaEventos) {
			if (evento instanceof Recepcao && // Só coleta eventos na saída
				evento.getTempo() >= tempoInicialRodada && 
				evento.getTempo() <= tempoFinalRodada ) {
			
				soma += evento.getTempo();
			}
		}
		
		double mediaRodada = soma/tamanhoRodada;
		
		mediasRodadas.put(rodadaAtual, mediaRodada);
		
	}
	
	public static double calcularMediaGeral(){
		
		/*
		 * Calcula média geral das rodadas
		 */
		
		int denominador = mediasRodadas.size();
		double soma = 0;
		
		for (Integer i=1; i <= denominador; i++){
			soma += mediasRodadas.get(i);
		}
		
		return soma/denominador;
		
	}

	public static void calcularVarianciaRodada(){
		
		/*
		 * Calcula a variancia das rodadas: E[X^2] - E[X]^2
		 */
		
		Variance variancia = new Variance();
		
		double[] mediaRodada = transformaDouble(mediasRodadas);
		
		double var = variancia.evaluate(mediaRodada);
		
		double somatorio = 0.0;
		
		for(int i =0 ; i<mediaRodada.length; i++){
			somatorio += Math.pow((mediaRodada[i] - calcularMediaGeral()), 2);
		}
		
		double r = (1/(mediaRodada.length - 1)) * somatorio;
		
	}

	private static double[] transformaDouble(Map<Integer, Double> medias) {
		
		double[] d = new double[medias.size()];
		
		for(int i=0; i<medias.size();i++){
			d[i] = medias.get(i);
		}
		
		return d;
	}
}
