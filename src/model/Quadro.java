package model;

import java.util.Random;

public class Quadro {
	
	private static final int tamanhoDoQuadro = 1000;
	
	private byte[] dados = new byte[tamanhoDoQuadro];
	
	private PC emissor;
	
	private int numeroDeColisoes = 0;
	
	public void transmitir() {
		
	}
	
	private double binaryBackoff() {
		double tempoMultiplicador = 0.0512; // 51,2 us
		
		int k = this.numeroDeColisoes > 10 ? 10 : this.numeroDeColisoes;
		int limite = (int)Math.pow(2, k) - 1;
		Random rand = new Random();
		int i = rand.nextInt(limite + 1); // intervalo fechado [0, 2^k - 1]
		
		return i * tempoMultiplicador;
	}
}
