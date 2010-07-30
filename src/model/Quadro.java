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
				
					// usado erroneamente só para saber que esse evento foi substituído e não deve ser considerado
					eventoTransmissao.setColidido(true);
					
					Evento novoEvento = new Transmissao(eventoTransmissao.getTempo() + emissor.tempoEntreQuadros, eventoTransmissao.getRodada(), 
							emissor, this, true);
					novoEvento.setTransmissaoImediata(true);
					
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
						long novoTempoTx = GeradorDados.gerarTempoFinalTransmissao((Transmissao)eventoAnterior)
								+ emissor.tempoEntreQuadros;
						
						// usado erroneamente só para saber que esse evento foi substituído e não deve ser considerado
						eventoTransmissao.setColidido(true);
						
						Evento novoEvento = new Transmissao(novoTempoTx, eventoTransmissao.getRodada(), emissor, this, true);
						novoEvento.setTransmissaoImediata(true);
						
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

			// se o evento é de transmissão e não é o quadro que o emissor está transmitindo
			if ((evento instanceof Transmissao) && (!evento.getQuadro().equals(eventoTransmissao.getQuadro())) && !evento.isColidido()){
				 
				if (eventoTransmissao.getTempo().equals(evento.getTempo()) || eventoTransmissao.getTempo().equals(GeradorDados.gerarTempoFinalTransmissao((Transmissao)evento))
					 ||	((eventoTransmissao.getTempo() > evento.getTempo()) && (eventoTransmissao.getTempo() < GeradorDados.gerarTempoFinalTransmissao((Transmissao)evento)))){
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
		System.out.println("Colisao! Aguardando 3,2 + Binary Backoff para novo envio.");
		
		// calcula o novo tempo de envio dos quadros
		Long tempoAdicionalTx = eventoTransmissao.getTempo() + 3200 + eventoTransmissao.getQuadro().binaryBackoff();
		Long tempoAdicionalRx = eventoColidido.getTempo() + 3200 + eventoColidido.getQuadro().binaryBackoff();
		
		// cria novo evento e coloca na fila de eventos
		Evento novoEventoTx = new Transmissao(tempoAdicionalTx, eventoTransmissao.getRodada(), emissor, this, false);
		Evento novoEventoRx = new Transmissao(tempoAdicionalRx, eventoTransmissao.getRodada(), emissor, eventoColidido.getQuadro(), false);
		
		System.out.println("Tempo futuro do quadro que estava sendo transmitido " + tempoAdicionalTx);
		System.out.println("Tempo futuro do quadro que estava sendo recebido " + tempoAdicionalRx);
		
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

			// se o evento é de recepção e não é o quadro que está sendo transmitido
			if ((evento instanceof Recepcao) && (!evento.getQuadro().equals(eventoTransmissao.getQuadro())) && !evento.isColidido()){
				 
				if (tempoInicioTransmissaoAtual.equals(evento.getTempo()) ||
						tempoFimTransmissaoAtual.equals(evento.getTempo()) ||
						((tempoInicioTransmissaoAtual < evento.getTempo()) && (tempoFimTransmissaoAtual > evento.getTempo()))){
					return (Recepcao)evento;
				}
			}
		}

		return null;
				
	}
		
	
	public void receber(Recepcao eventoRecepcao) {
		// Verifica se houve colisão entre as recepções, ou seja, se está recebendo
		// mais de um quadro ao mesmo tempo
	
		Long tempoFinalRecepcao = eventoRecepcao.getTempo() + emissor.getTempoDeTransmissao();
		Recepcao eventoColidido = temColisaoRecepcao(eventoRecepcao);
		
		if (eventoColidido == null){
				
			System.out.println("Quadro " + this.toString() + " recebido! Evento:" + eventoRecepcao.toString());
			emissor.enviarConfirmacao(eventoRecepcao, tempoFinalRecepcao);	
						
		}
		else{ // espera 3,2us + binary backoff e retransmite
			
			try {
				tratarColisaoRecepcao(eventoRecepcao, eventoColidido);
			} catch (QuadroDescartadoException e) {
				System.out.println("Quadro " + eventoRecepcao.getQuadro().hashCode() + " descartado!");
			}
			
		}
	}
	
	
	private Recepcao temColisaoRecepcao(Recepcao eventoRecepcao){
		
		/*
		 * Verificar se durante o tempo de recepcao do evento atual
		 * algum outro evento vai chegar para ser recebido e não é o mesmo quadro
		 * ou um quadro enviado pelo pc que está recebendo.
		 */
		
		Long tempoInicioRecepcaoAtual = eventoRecepcao.getTempo();
		Long tempoFimRecepcaoAtual = GeradorDados.gerarTempoFinalRecepcao(eventoRecepcao);
		
		// pega todas as recepcoes futuras
		SortedSet<Evento> recepcoesFuturas = Simulador.filaEventos.tailSet(eventoRecepcao, false);
		
		// para cada uma dessas recepções, verifica se o tempo de inicio delas esta entre o periodo de execução
		// da recepcao atual
		for (Evento evento : recepcoesFuturas){

			// se o evento é de recepção e não é do PC que está recebendo o evento atual e não é o mesmo quadro
			if (evento instanceof Recepcao){
				
				Recepcao recepcao = (Recepcao)evento;
				
				if (!eventoRecepcao.getPc().equals(eventoRecepcao.getTransmissao().getPc()) &&
						!recepcao.getTransmissao().getPc().equals(recepcao.getPc()) && 
						!recepcao.getQuadro().equals(eventoRecepcao.getQuadro())){
					
					if (tempoInicioRecepcaoAtual < recepcao.getTempo() || tempoFimRecepcaoAtual > recepcao.getTempo() ||
							((tempoInicioRecepcaoAtual < recepcao.getTempo()) && (tempoFimRecepcaoAtual > recepcao.getTempo()))){
						return recepcao;
					}
				
				}
			}
		}

		return null;
				
	}
	
	
	private void tratarColisaoRecepcao(Recepcao eventoRecepcao, Recepcao eventoColidido) throws QuadroDescartadoException {
		
		// incrementa o numero de colisões dos quadros
		eventoRecepcao.getQuadro().setNumeroDeColisoes(eventoRecepcao.getQuadro().getNumeroDeColisoes()+1);
		eventoColidido.getQuadro().setNumeroDeColisoes(eventoColidido.getQuadro().getNumeroDeColisoes()+1);
		
		// seta todos os eventos envolvidos como colididos
		Transmissao txEventoRecepcao = eventoRecepcao.getTransmissao();
		txEventoRecepcao.setColidido(true);
		Collection<Recepcao> recepcoes = txEventoRecepcao.getRecepcoes().values();
		for (Recepcao recepcao : recepcoes){
			recepcao.setColidido(true);
		}
				
		Transmissao txEventoColidido = eventoColidido.getTransmissao();
		txEventoColidido.setColidido(true);
		Collection<Recepcao> recepcoesColidido = txEventoColidido.getRecepcoes().values();
		for (Recepcao recepcao : recepcoesColidido){
			recepcao.setColidido(true);
		}
						
		System.out.println("COLIDIU! Quadro: " + this.hashCode() + ", PC: " + emissor +", NumColisoes (tratarColisao) =" + eventoRecepcao.getQuadro().getNumeroDeColisoes());
		System.out.println("Colisao! Aguardando 3,2 + Binary Backoff para novo envio.");
		
		// calcula o novo tempo de envio dos quadros
		Long tempoAdicionalRx = eventoRecepcao.getTempo() + 3200 + eventoRecepcao.getQuadro().binaryBackoff();
		Long tempoAdicionalRxColidido = eventoColidido.getTempo() + 3200 + eventoColidido.getQuadro().binaryBackoff();
		
		// cria novo evento e coloca na fila de eventos
		Evento novoEventoRx = new Transmissao(tempoAdicionalRx, eventoRecepcao.getRodada(), txEventoRecepcao.getPc(), this, true);
		Evento novoEventoRxColidido = new Transmissao(tempoAdicionalRxColidido, eventoRecepcao.getRodada(), txEventoColidido.getPc(), eventoColidido.getQuadro(), true);
		
		System.out.println("Tempo futuro do quadro que estava sendo transmitido " + tempoAdicionalRx);
		System.out.println("Tempo futuro do quadro que estava sendo recebido " + tempoAdicionalRxColidido);
		
		// adiciona novos eventos na lista
		Simulador.filaEventos.add(novoEventoRx);
		Simulador.filaEventos.add(novoEventoRxColidido);
		
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
