package model;

import java.util.Random;

import model.exception.QuadroDescartadoException;
import controller.Simulador;

public class Quadro {
	
	public static final int tamanho = 1000;
	private PC emissor;
	private int numeroDeColisoes = 0;
	
	public Quadro(Mensagem msg) {
		this.emissor = msg.getEmissor();
		msg.setQuadro(this);
	}
	
	public long transmitir() {
		Evento eventoTransmissao = Simulador.filaEventos.remove();
		System.out.println("Quadro " + this.hashCode() + " enviado!");
		
		Long tempoEmissorHub = eventoTransmissao.getTempo() + 
							   emissor.getTempoDeTransmissao() + 
							   emissor.atrasoPropagacao(); // Tempo do emissor ate o HUB.
		
		Long tempoHubReceptor;
		for (PC pc : Simulador.getPcsConectados()) {
			tempoHubReceptor = pc.atrasoPropagacao() +
					 		   emissor.getTempoDeTransmissao(); // Tempo do hub ate o receptor.
			
			Long tempo = tempoEmissorHub + tempoHubReceptor;
			Simulador.filaEventos.add(new Evento(tempo, TipoEvento.RECEPCAO, pc, this));
			System.out.println("Tempo para envio do quadro: " + (tempo - eventoTransmissao.getTempo()) + " ns");
		}
		
		return Simulador.now();
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
