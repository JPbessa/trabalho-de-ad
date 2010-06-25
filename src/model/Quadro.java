package model;

import java.util.Random;

import model.exception.ColisaoDectadaException;
import model.exception.QuadroDescartadoException;


public class Quadro {
	
	private static final int tamanhoDoQuadro = 1000;
	
	private byte[] dados = new byte[tamanhoDoQuadro];
	
	private PC emissor;
	
	private int numeroDeColisoes = 0;
	
	public void transmitir() {
		//if (emissor.getRx())
		// se sinal recebido n‹o Ž o mesmo que a esta‹o est‡ enviando ou se sinais sobrepostos existem na porta RX,
		// entao houve colisao
	}
	
	private double binaryBackoff() throws QuadroDescartadoException {
		if (numeroDeColisoes > 15) throw new QuadroDescartadoException();
		else {
			double tempoMultiplicador = 0.0512; // 51,2 us
			
			int k = this.numeroDeColisoes > 10 ? 10 : this.numeroDeColisoes;
			int limite = (int)Math.pow(2, k) - 1;
			Random rand = new Random();
			int i = rand.nextInt(limite + 1); // intervalo fechado [0, 2^k - 1]
			
			return i * tempoMultiplicador;
		}
	}
}
