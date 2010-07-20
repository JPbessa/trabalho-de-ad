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
	
	public long transmitir(Transmissao eventoTransmissao) {
		
		if (eventoTransmissao.isTransmissaoImediata() || meioLivre()){
			
			if (emissor.livre(eventoTransmissao)){
					
				//Evento eventoTransmissao = Simulador.filaEventos.pollFirst();
				//emissor.getTx().setQuadroEnviado(emissor.getTx().getQuadros().remove(0));
				System.out.println("Quadro " + this.hashCode() + " enviado!");
				
				Long tempoEmissorHub = eventoTransmissao.getTempo() + 
									   emissor.getTempoDeTransmissao() + 
									   emissor.atrasoPropagacao(); // Tempo do emissor ate o HUB.
				
				Long tempoHubReceptor;
				for (PC pc : Simulador.getPcsConectados()) {
					tempoHubReceptor = pc.atrasoPropagacao() +
							 		   emissor.getTempoDeTransmissao(); // Tempo do hub ate o receptor.
					
					Long tempo = tempoEmissorHub + tempoHubReceptor;
					
					Evento novoEvento = new Recepcao(tempo, eventoTransmissao.getRodada(), pc, this);
					Simulador.filaEventos.add(novoEvento);
					System.out.println("Evento de Recepcao adicionado a fila: " + novoEvento);
					
					System.out.println("Tempo para recebimento do quadro: " + (tempo - eventoTransmissao.getTempo()) + " ns");
				}
			}
			else{
				System.out.println("Emissor ocupado! Tempo entre quadros nao finalizado. Aguardando 9,6 para novo envio.");
				// Gerar evento de transmissao daqui a 9,6us e transmitir independente do meio
				Evento novoEvento = new Transmissao((eventoTransmissao.getTempo() + emissor.tempoEntreQuadros), eventoTransmissao.getRodada(), emissor, this, true);
				Simulador.filaEventos.add(novoEvento);
			}
			
		}
		
		return 0; //FIXME
	}
	
	private boolean meioLivre() {
		// TODO
		
		/*
		 * Percorre a lista de eventos para verificar se
		 * a Transmissao esta entre o inicio e o fim de uma
		 * Recepcao do proprio computador emissor
		 */
		return true;
	}

	public long receber(Recepcao eventoRecepcao) {
		// Coleta as estatisticas da rodada
		//System.out.println("receber();");
		System.out.println("Quadro " + this.toString() + " recebido! Evento:" + eventoRecepcao.toString());
		emissor.enviarConfirmacao(this, eventoRecepcao.getTempo()+emissor.getTempoDeTransmissao());
		
		//FIXME verificar se no intervalo entre getTempo e getTempo() + getTempoDeTransmissao(), nao ocorre colisao.
		return 0;
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
	
	@Override
	public String toString() {
		return "" + this.hashCode() ;
	}
}
