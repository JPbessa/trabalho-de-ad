package model;

import java.util.Collection;
import java.util.Random;
import java.util.SortedSet;

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
	
	public void transmitir(Transmissao eventoTransmissao) {
		
		// Sente o meio
		// Se estiver livre e tiver passado 9,6us, transmite
		// Se estiver livre mas ainda não passou 9,6us, espera passar os 9,6us
		// Se ao transmitir houver colisão, espera 3,2us e chama o Binary Backoff para encontrar novo tempo de transmissão
		// Se o meio estiver ocupado, espera o fim da transmissão + 9,6us
		
		
		if (eventoTransmissao.isTransmissaoImediata()){
			transmiteImediatamente(eventoTransmissao);
		}
		else{
			if (meioLivre(eventoTransmissao)){
				if (emissor.livre(eventoTransmissao, tempoFinalUltimaTransmissao)){
					transmiteImediatamente(eventoTransmissao);
				}
				else{ // Modifica o tempo do evento de transmissão para daqui a 9,6us e transmite independente do meio
					System.out.println("Tempo entre quadros nao finalizado. Aguardando 9,6 para novo envio.");
					eventoTransmissao.setTempo((eventoTransmissao.getTempo() + emissor.tempoEntreQuadros));
					
					eventoTransmissao.setTransmissaoImediata(true);
					
					Evento novoEvento = new Transmissao(eventoTransmissao.getTempo(), eventoTransmissao.getRodada(), emissor, this, true);
					Simulador.filaEventos.add(novoEvento);
				}
			}
			else{ // se meio ocupado, espera o fim da transmissao + 9,6us e transmite independente do meio
				System.out.println("Meio ocupado. Espera fim da transmissão atual + 9,6 para novo envio.");
				
				// Pega o evento anterior
				Evento eventoAnterior = Simulador.filaEventos.lower(eventoTransmissao);
				
				while (eventoAnterior != null){
					// Se for um evento do tipo Transmissao e o emissor é outro PC
					if (eventoAnterior instanceof Transmissao && !eventoAnterior.getPc().equals(emissor)){
						
						// Modifica o tempo do evento e transmite imediatamente
						eventoTransmissao.setTempo(GeradorDados.gerarTempoFinalTransmissao((Transmissao)eventoAnterior)
								+ emissor.tempoEntreQuadros);
						
						eventoTransmissao.setTransmissaoImediata(true);
						
						Evento novoEvento = new Transmissao(eventoTransmissao.getTempo(), eventoTransmissao.getRodada(), emissor, this, true);
						Simulador.filaEventos.add(novoEvento);
						
						break;
					}else{
						eventoAnterior = Simulador.filaEventos.lower(eventoAnterior);
					}
				}
			}
		}
	}

	private void transmiteImediatamente(Transmissao eventoTransmissao) {
		
		Recepcao eventoColidido = temColisaoTransmissao(eventoTransmissao);
		
		if (eventoColidido == null){
				
			System.out.println("Quadro " + this.hashCode() + " enviado pelo PC " + eventoTransmissao.getPc());
			
			// Tempo do emissor ate dentro do HUB.
			Long tempoEmissorHub = eventoTransmissao.getTempo() + 
								   emissor.getTempoDeTransmissao() + 
								   emissor.atrasoPropagacao();
			
			Long tempoHubReceptor;
			for (PC pc : Simulador.getPcsConectados()) {
				tempoHubReceptor = emissor.getTempoDeTransmissao() + pc.atrasoPropagacao(); // Tempo de saida do hub ate o receptor.
				
				Long tempo = tempoEmissorHub + tempoHubReceptor;
				
				/*if (eventoTransmissao.getTempo() > Simulador.tamanhoFaseTransiente && pc.getDistancia() == 100) {
					// Se j‡ saiu da fase transiente, somo o tempo ao tempoOcupado. (PC 1 como referencia)
					Simulador.tempoOcupado += 2 * (emissor.getTempoDeTransmissao() + emissor.atrasoPropagacao());
				}*/
				
				Evento novoEvento = new Recepcao(tempo, eventoTransmissao.getRodada(), pc, this, eventoTransmissao);
				Simulador.filaEventos.add(novoEvento);
				
				System.out.println("Evento de Recepcao adicionado a fila: " + novoEvento);
				System.out.println("Tempo para recebimento do quadro: " + (tempo - eventoTransmissao.getTempo()) + " ns");
			}
			
			tempoFinalUltimaTransmissao = GeradorDados.gerarTempoFinalTransmissao(eventoTransmissao);
			
		}
		else{ // espera 3,2us + binary backoff e retransmite
			
			try {
				tratarColisao(eventoTransmissao, eventoColidido);
			} catch (QuadroDescartadoException e) {
				System.out.println("Quadro " + eventoTransmissao.getQuadro().hashCode() + " descartado!");
			}
			
		}
	}
	
	private boolean meioLivre(Transmissao eventoTransmissao) {
		
		/*
		 * Percorre a lista de eventos para verificar se o início da Transmissao 
		 * esta entre o inicio e o fim de uma Transmissao dos outros computadores
		 */
		
		// pega todas as transmissoes anteriores à transmissao atual
		SortedSet<Evento> transmissoesAnteriores = Simulador.filaEventos.subSet(Simulador.filaEventos.first(), eventoTransmissao);
		
		// para cada uma dessas transmissoes, calcula o tempo final da transmissao do pc ao hub e verifica se o
		// tempo de transmissao do evento atual está nesse intervalo
		for (Evento evento : transmissoesAnteriores){

			// se o evento é de transmissão e não é do PC que está transmitindo o evento atual
			if ((evento instanceof Transmissao) && (!evento.getPc().equals(eventoTransmissao.getPc()))){
				 
				if ((eventoTransmissao.getTempo() > evento.getTempo()) 
						&& (eventoTransmissao.getTempo() < GeradorDados.gerarTempoFinalTransmissao((Transmissao)evento))){
					return false;
				}
			}
		}
		
		return true;
		
	}


	private void tratarColisao(Transmissao eventoTransmissao, Recepcao eventoColidido) throws QuadroDescartadoException {
		
		// incrementa o numero de colisões dos quadros
		eventoTransmissao.getQuadro().setNumeroDeColisoes(eventoTransmissao.getQuadro().getNumeroDeColisoes()+1);
		eventoColidido.getQuadro().setNumeroDeColisoes(eventoColidido.getQuadro().getNumeroDeColisoes()+1);
		
		// seta todos os eventos envolvidos como colididos
		eventoTransmissao.setColidido(true);
		Transmissao txEventoColidido = eventoColidido.getTransmissao();
		txEventoColidido.setColidido(true);
		Collection<Recepcao> recepcoes = txEventoColidido.getRecepcoes().values();
		for (Recepcao recepcao : recepcoes){
			recepcao.setColidido(true);
		}
						
		System.out.println("COLIDIU! Quadro: " + this.hashCode() + ", PC: " + emissor +", NumColisoes (tratarColisao) =" + eventoTransmissao.getQuadro().getNumeroDeColisoes());
		System.out.println("Colisão! Aguardando 3,2 + Binary Backoff para novo envio.");
		
		// calcula o novo tempo de envio dos quadros
		Long tempoAdicionalTx = eventoTransmissao.getTempo() + 3200 + eventoTransmissao.getQuadro().binaryBackoff();
		Long tempoAdicionalRx = eventoColidido.getTempo() + 3200 + eventoColidido.getQuadro().binaryBackoff();
		
		// cria novo evento e coloca na fila de eventos
		Evento novoEventoTx = new Transmissao(tempoAdicionalTx, eventoTransmissao.getRodada(), emissor, this, true);
		Evento novoEventoRx = new Transmissao(tempoAdicionalRx, eventoTransmissao.getRodada(), emissor, eventoColidido.getQuadro(), true);
		
		System.out.println("Tempo futuro do quadro que estava sendo transmitido" + tempoAdicionalTx);
		System.out.println("Tempo futuro do quadro que estava sendo recebido" + tempoAdicionalRx);
		
		// adiciona novos eventos na lista
		Simulador.filaEventos.add(novoEventoTx);
		Simulador.filaEventos.add(novoEventoRx);
		
	}
	
	
	private Recepcao temColisaoTransmissao(Transmissao eventoTransmissao){
		
		/*
		 * Verificar se durante o tempo de transmissão do evento atual
		 * algum outro evento vai chegar para ser recebido e não é o quadro
		 * enviado pelo PC emissor.
		 */
		
		Long tempoInicioTransmissaoAtual = eventoTransmissao.getTempo();
		Long tempoFimTransmissaoAtual = GeradorDados.gerarTempoFinalTransmissao(eventoTransmissao);
		
		// pega todas as recepcoes futuras
		SortedSet<Evento> recepcoesFuturas = Simulador.filaEventos.tailSet(eventoTransmissao, false);
		
		// para cada uma dessas recepções, verifica se o tempo de inicio delas esta entre o periodo de execução
		// da transmissao atual
		for (Evento evento : recepcoesFuturas){

			// se o evento é de recepção e não é do PC que está transmitindo o evento atual
			if ((evento instanceof Recepcao) && (!evento.getPc().equals(emissor))){
				 
				if ((tempoInicioTransmissaoAtual < evento.getTempo()) 
						&& (tempoFimTransmissaoAtual > evento.getTempo())){
					return (Recepcao)evento;
				}
			}
		}

		return null;
				
	}

	
	/*public void tratarColisaoRecepcao(Recepcao evento) throws QuadroDescartadoException {
		
		evento.getQuadro().setNumeroDeColisoes(evento.getQuadro().getNumeroDeColisoes()+1);
		
		System.out.println("COLIDIU! Quadro: " + evento.getTransmissao().getQuadro().hashCode() + ", PC: " + evento.getTransmissao().getPc() + ", NumColisoes (tratarColisaoRecepcao) ="+ evento.getQuadro().getNumeroDeColisoes());
		
		//FIXME tempoAdicional deve ser Tempo da Colisão + binaryBackOff
		Long tempoAdicional = evento.getTransmissao().getTempo() + evento.getQuadro().binaryBackoff();
	
		Evento novoEvento = new Transmissao(tempoAdicional, evento.getRodada(), evento.getTransmissao().getPc(), evento.getTransmissao().getQuadro(), true);
				
		System.out.println("Tempo futuro " + tempoAdicional);
		Simulador.filaEventos.add(novoEvento);
		
		List<Transmissao> transmissoesEmissor = Simulador.transmissoesAbertas.get(evento.getTransmissao().getPc());
		transmissoesEmissor.remove(((Recepcao) evento).getTransmissao());
		
		List<Transmissao> transmissoesReceptor = Simulador.transmissoesAbertas.get(evento.getPc());
		transmissoesReceptor.remove(((Recepcao) evento).getTransmissao());
	}*/
	
	
	public long receber(Recepcao eventoRecepcao) {
		// Coleta as estatisticas da rodada
		//System.out.println("receber();");
		System.out.println("Quadro " + this.toString() + " recebido! Evento:" + eventoRecepcao.toString());
		emissor.enviarConfirmacao(eventoRecepcao, eventoRecepcao.getTempo()+emissor.getTempoDeTransmissao());
		
		//FIXME verificar se no intervalo entre getTempo e getTempo() + getTempoDeTransmissao(), nao ocorre colisao.
		return 0;
	}
	
	public Long binaryBackoff() throws QuadroDescartadoException {
		
		if (numeroDeColisoes >= 16) {
			throw new QuadroDescartadoException();
		}
		else {
			Long tempoMultiplicador = new Long(51200); // 51,2 us
		
			int k = numeroDeColisoes >= 10 ? 10 : numeroDeColisoes;
			int limite = (int)Math.pow(2, k) - 1;
			Random rand = new Random();
			int i = rand.nextInt(limite + 1); // intervalo fechado [0, 2^k - 1]
			
			return i * tempoMultiplicador;
		}
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
	
	public void setNumeroDeColisoes(int numeroDeColisoes) {
		this.numeroDeColisoes = numeroDeColisoes;
	}
}
