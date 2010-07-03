package model;

import java.util.Random;

import controller.Simulador;

import model.exception.ColisaoDectadaException;
import model.exception.QuadroDescartadoException;


public class Quadro {
	
	private static final int tamanhoDoQuadro = 1000;
	
	//private byte[] dados = new byte[tamanhoDoQuadro];
	
	private PC emissor;
	
	private int numeroDeColisoes = 0;
	
	private long t_envio;
	
	public Quadro(Mensagem msg) {
		this.emissor = msg.getEmissor();
		this.t_envio = Simulador.inicioSimulacao + calculaTempoEnvio();
		msg.setQuadro(this);
	}
	
	// TODO definir calculo correto para calcular o tempo de envio (propagacao + tam_quadro)
	private long calculaTempoEnvio() {
		Random rand = new Random();
		int i = 1 + rand.nextInt(100);
		return (long) (tamanhoDoQuadro/i);
	}

	public long transmitir() {
		Simulador.filaEventos.remove(this);
		System.out.println("Quadro enviado!");
		
		return Simulador.now();
		//if (emissor.getRx())
		// se sinal recebido n�o � o mesmo que a esta��o est� enviando ou se sinais sobrepostos existem na porta RX,
		// entao houve colisao
	}
	
	public Double binaryBackoff() throws QuadroDescartadoException {
		if (numeroDeColisoes > 15) throw new QuadroDescartadoException();
		else {
			Double tempoMultiplicador = 0.0512; // 51,2 us

			// TODO Acertar rotina, coloquei apenas para executar cenario 1.
//			int k = this.numeroDeColisoes > 10 ? 10 : this.numeroDeColisoes;
//			int limite = (int)Math.pow(2, k) - 1;
//			Random rand = new Random();
//			int i = rand.nextInt(limite + 1); // intervalo fechado [0, 2^k - 1]
//			
//			return i * tempoMultiplicador;

			Random rand = new Random();
			int i = rand.nextInt(100);
			// teste para cenario 1
			if (i < 10) throw new QuadroDescartadoException();
			
			return Double.parseDouble("" + i);
		}
	}
}
