package model;

import java.util.List;
import java.util.Random;

import model.exception.QuadroDescartadoException;
import controller.Simulador;

public class Quadro {
	
	public static final int tamanho = 1000;
	private PC emissor;
	private int numeroDeColisoes = 0;

	private long tempoFinalUltimaTransmissao = 0;
	private Mensagem mensagem;
	
	public Quadro(Mensagem msg) {
		this.emissor = msg.getEmissor();
		msg.setQuadro(this);
		this.mensagem = msg;
	}
	
	public long transmitir(Transmissao eventoTransmissao) {
		
		if (eventoTransmissao.isTransmissaoImediata() || meioLivre(eventoTransmissao)){
			
			boolean temColisaoTransmissao = temColisaoTransmissao(eventoTransmissao);
			
			if (emissor.livre(eventoTransmissao, tempoFinalUltimaTransmissao) && !temColisaoTransmissao){
					
				//Evento eventoTransmissao = Simulador.filaEventos.pollFirst();
				//emissor.getTx().setQuadroEnviado(emissor.getTx().getQuadros().remove(0));
				System.out.println("Quadro " + this.hashCode() + " enviado pelo PC " + eventoTransmissao.getPc());
				
				Long tempoEmissorHub = eventoTransmissao.getTempo() + 
									   emissor.getTempoDeTransmissao() + 
									   emissor.atrasoPropagacao(); // Tempo do emissor ate dentro do HUB.
				
				Long tempoHubReceptor;
				for (PC pc : Simulador.getPcsConectados()) {
					tempoHubReceptor = emissor.getTempoDeTransmissao() + pc.atrasoPropagacao(); // Tempo de saida do hub ate o receptor.
					
					Long tempo = tempoEmissorHub + tempoHubReceptor;
					
					Evento novoEvento = new Recepcao(tempo, eventoTransmissao.getRodada(), pc, this, eventoTransmissao);
					Simulador.filaEventos.add(novoEvento);
					
					System.out.println("Evento de Recepcao adicionado a fila: " + novoEvento);
					System.out.println("Tempo para recebimento do quadro: " + (tempo - eventoTransmissao.getTempo()) + " ns");
				}
				
				tempoFinalUltimaTransmissao = tempoEmissorHub + emissor.getTempoDeTransmissao();
			}
			else if (temColisaoTransmissao){
				
				try {
					tratarColisao(eventoTransmissao);
					eventoTransmissao.setColidido(true);
				} catch (QuadroDescartadoException e) {
					this.emissor.transmissaoCorrente = null;
					System.out.println("Quadro descartado: " + this);
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

	public void tratarColisao(Evento evento) throws QuadroDescartadoException {
		
		numeroDeColisoes++;
		
		System.out.println("COLIDIU! Quadro: " + this.hashCode() + ", PC: " + emissor);
		
		//FIXME tempoAdicional deve ser Tempo da Colisão + binaryBackOff
		Long tempoAdicional = evento.getTempo() + binaryBackoff();
	
		Evento novoEvento = new Transmissao(tempoAdicional, evento.getRodada(), emissor, this, true);
		System.out.println("Tempo futuro " + tempoAdicional);
		Simulador.filaEventos.add(novoEvento);	
		
	}
	
	public void tratarColisaoRecepcao(Recepcao evento) throws QuadroDescartadoException {
		
		numeroDeColisoes++;
		
		System.out.println("COLIDIU! Quadro: " + evento.getTransmissao().getQuadro().hashCode() + ", PC: " + evento.getTransmissao().getPc());
		
		//FIXME tempoAdicional deve ser Tempo da Colisão + binaryBackOff
		Long tempoAdicional = evento.getTransmissao().getTempo() + binaryBackoff();
	
		Evento novoEvento = new Transmissao(tempoAdicional, evento.getRodada(), evento.getTransmissao().getPc(), evento.getTransmissao().getQuadro(), true);
				
		System.out.println("Tempo futuro " + tempoAdicional);
		Simulador.filaEventos.add(novoEvento);
		
		List<Transmissao> transmissoesEmissor = Simulador.transmissoesAbertas.get(evento.getTransmissao().getPc());
		transmissoesEmissor.remove(((Recepcao) evento).getTransmissao());
		
		List<Transmissao> transmissoesReceptor = Simulador.transmissoesAbertas.get(evento.getPc());
		transmissoesReceptor.remove(((Recepcao) evento).getTransmissao());
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
		emissor.enviarConfirmacao(eventoRecepcao, eventoRecepcao.getTempo()+emissor.getTempoDeTransmissao());
		
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
		 * Se a transmissao atual estiver entre o inicio de uma transmissao e o fim da recepcao
		 * do mesmo quadro para o computador da transmissao atual, ocorre uma colisao.
		 */
		
		List<Transmissao> transmissoesAbertas = Simulador.transmissoesAbertas.get(eventoTransmissao.getPc());
		
		if (transmissoesAbertas != null && !transmissoesAbertas.isEmpty()) {
			for (Transmissao trans: transmissoesAbertas) {

				Recepcao recp = trans.getRecepcoes().get(emissor);
				if (recp!=null && !recp.getQuadro().equals(eventoTransmissao.getQuadro())){
					if (!recp.isColidido()){
						System.out.println("Quadros colididos: TX: " + eventoTransmissao.getQuadro() + ", RX: " + recp.getQuadro());
						recp.setColidido(true);
						try {
							tratarColisaoRecepcao(recp);
							if(transmissoesAbertas.isEmpty()) break;
						} catch (QuadroDescartadoException e) {
							System.out.println("Quadro descartado " + recp.getQuadro());
						}
					}
				}
				
			}
			
			return true;
		}
		
		return false;
		
	}
	
	@Override
	public String toString() {
		return "" + this.hashCode() ;
	}
	
	public Mensagem getMensagem() {
		return mensagem;
	}
	
	public int getNumeroDeColisoes() {
		return numeroDeColisoes;
	}
}
