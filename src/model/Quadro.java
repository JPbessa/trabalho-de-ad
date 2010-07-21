package model;

import java.util.Random;
import java.util.TreeSet;

import model.exception.QuadroDescartadoException;
import controller.Simulador;

public class Quadro {
	
	public static final int tamanho = 1000;
	private PC emissor;
	private int numeroDeColisoes = 0;
	private long tempoFinalUltimaTransmissao = 0;
	
	public Quadro(Mensagem msg) {
		this.emissor = msg.getEmissor();
		msg.setQuadro(this);	
	}
	
	public long transmitir(Transmissao eventoTransmissao) {
		
		if (eventoTransmissao.isTransmissaoImediata() || meioLivre(eventoTransmissao)){
			
			boolean temColisaoTransmissao = temColisaoTransmissao(eventoTransmissao);
			
			if (emissor.livre(eventoTransmissao, tempoFinalUltimaTransmissao) && !temColisaoTransmissao){
					
				//Evento eventoTransmissao = Simulador.filaEventos.pollFirst();
				//emissor.getTx().setQuadroEnviado(emissor.getTx().getQuadros().remove(0));
				System.out.println("Quadro " + this.hashCode() + " enviado!");
				
				Long tempoEmissorHub = eventoTransmissao.getTempo() + 
									   emissor.getTempoDeTransmissao() + 
									   emissor.atrasoPropagacao(); // Tempo do emissor ate dentro do HUB.
				
				Long tempoHubReceptor;
				for (PC pc : Simulador.getPcsConectados()) {
					tempoHubReceptor = emissor.getTempoDeTransmissao() + pc.atrasoPropagacao(); // Tempo de saida do hub ate o receptor.
					
					Long tempo = tempoEmissorHub + tempoHubReceptor;
					
					Evento novoEvento = new Recepcao(tempo, eventoTransmissao.getRodada(), pc, this);
					Simulador.filaEventos.add(novoEvento);
					
					System.out.println("Evento de Recepcao adicionado a fila: " + novoEvento);
					System.out.println("Tempo para recebimento do quadro: " + (tempo - eventoTransmissao.getTempo()) + " ns");
				}
				
				tempoFinalUltimaTransmissao = tempoEmissorHub + emissor.getTempoDeTransmissao();
			}
			else if (temColisaoTransmissao){
				
				try {
					numeroDeColisoes++;
					System.out.println("COLIDIU! quadro - " + eventoTransmissao.getQuadro().hashCode());
					Long tempoAdicional = eventoTransmissao.getTempo() + binaryBackoff();
					Evento novoEvento = new Transmissao(tempoAdicional, eventoTransmissao.getRodada(), emissor, this, true);
					System.out.println("Tempo futuro " + tempoAdicional);
					Simulador.filaEventos.add(novoEvento);	
				} catch (QuadroDescartadoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
						
			}	
			else{				
				System.out.println("Emissor ocupado! Tempo entre quadros nao finalizado. Aguardando 9,6 para novo envio.");
				// Gerar evento de transmissao daqui a 9,6us e transmitir independente do meio
				eventoTransmissao.setTempo((eventoTransmissao.getTempo() + emissor.tempoEntreQuadros));
				Evento novoEvento = new Transmissao(eventoTransmissao.getTempo(), eventoTransmissao.getRodada(), emissor, this, true);
				Simulador.filaEventos.add(novoEvento);
			}
			
		}
		
		return 0; //FIXME
	}
	
	private boolean meioLivre(Transmissao eventoTransmissao) {
	
		/*
		 * Percorre a lista de eventos para verificar se
		 * a Transmissao esta entre o inicio e o fim de uma
		 * Transmissao dos outros computadores
		 */
		
		for (Evento evento : Simulador.filaEventos){
			
			if ((evento instanceof Transmissao) && (!evento.getPc().equals(eventoTransmissao.getPc()))){
				
				if ((eventoTransmissao.getTempo() > evento.getTempo()) 
						&& (eventoTransmissao.getTempo() < GeradorDados.gerarTempoFinalTransmissao((Transmissao)evento))){
					return false;
				}
			}
			
		}
		
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
	
	public Long binaryBackoff() throws QuadroDescartadoException {
		
		if (numeroDeColisoes > 15) throw new QuadroDescartadoException();
		else {
			Long tempoMultiplicador = new Long(51200); // 51,2 us
		
			int k = this.numeroDeColisoes > 10 ? 10 : this.numeroDeColisoes;
			int limite = (int)Math.pow(2, k) - 1;
			Random rand = new Random();
			int i = rand.nextInt(limite + 1); // intervalo fechado [0, 2^k - 1]
			
			return i * tempoMultiplicador;
		}
	}
	
	public boolean temColisaoTransmissao(Transmissao eventoTransmissao){
		
		/*
		 * Para todo evento de recepção que o emissor recebe
		 * é verificado se o momento que o evento a ser transmitido
		 * está entre o tempo que o evento de recepçao demora para ser recebido.
		 * Se sim e esses eventos têm quadros diferentes, há colisão.
		 */
		
		for (Evento evento : Simulador.filaEventos){
			
			//FIXME nao percorrer toda a lista
			
			if ((evento instanceof Recepcao) && (evento.getPc().equals(emissor))){
				if (eventoTransmissao.getTempo() >= evento.getTempo() 
						&& eventoTransmissao.getTempo() <= GeradorDados.gerarTempoFinalRecepcao((Recepcao)evento)){
					
					if (!evento.getQuadro().equals(eventoTransmissao.getQuadro())){
						
						return true;
					}
						
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "" + this.hashCode() ;
	}
}
