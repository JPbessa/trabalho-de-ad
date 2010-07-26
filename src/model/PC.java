package model;

import java.util.ArrayList;
import java.util.List;

import controller.Simulador;

public class PC {
	
	private static final int tempoPropagacaoNoMeio = 5; // 5 nanosegundos/metro
	private int distancia;
	private List<Mensagem> tx;
	private double p;
	private IntervaloChegadas A;
	private int quadrosEnviados;
	public final long tempoEntreQuadros = 9600; // em ns
	public final long tempoDeTransmissao = (int) Math.pow(10, 5); // em ns
	public final long tempoCriacaoUltimaTransmissao = 0;
	
	int confirmacoes = 0;
	
	public Transmissao transmissaoCorrente;
	
	public PC(int distancia) {
		this.distancia = distancia;
		this.tx = new ArrayList<Mensagem>();
	}
	
//	public void setTap(long val) {
//		this.tap.add((double) val);
//	}

	public Long atrasoPropagacao() {
		return new Long(this.distancia * tempoPropagacaoNoMeio); // em nanosegundos
	}
	
	public int getDistancia() {
		return distancia;
	}

	public void setDistancia(int distancia) {
		this.distancia = distancia;
	}

	public List<Mensagem> getTx() {
		return tx;
	}
	
	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
	}

	public IntervaloChegadas getA() {
		return A;
	}

	public void setA(IntervaloChegadas a) {
		this.A = a;
	}
	
	public int getQuadrosEnviados() {
		return quadrosEnviados;
	}
	
	public void setQuadrosEnviados(int quadrosEnviados) {
		this.quadrosEnviados = quadrosEnviados;
	}
	
	public long getTempoDeTransmissao() {
		return tempoDeTransmissao;
	}
	
	private long tempoUltimaMsg = 0;
	public void gerarMensagens(long tempoAtual,int rodada) {
		
		if (tempoAtual%1000000 == 0) {
			System.out.println("AVISADO: tempo = " + tempoAtual);
		}
		
		if (!tx.isEmpty()) {
			tempoUltimaMsg = tx.get(tx.size()-1).getTempoCriacao();
		}

		// Gera novas mensagens de acordo com o tipo de chegada de mensagens
		if(tempoUltimaMsg <= tempoAtual) {
			
			long tempo = 0;
			if (A.tipo == TipoDistribuicao.DETERMINISTICO) {
				tempo = tempoUltimaMsg + A.getValor();
			} else if (A.tipo == TipoDistribuicao.EXPONENCIAL) {
				Double tempoExponencial = GeradorDados.gerarExponencial(new Double(A.getValor()));
				tempo = tempoUltimaMsg + tempoExponencial.intValue();
			}
			this.tx.add(new Mensagem(this.p, this, tempo));

			System.out.println("GerarMensagens(PC "+this.distancia+") com " + tx.size() +" mensagens. Tempo de Geração: " + tempo + ". Tempo Atual: " + tempoAtual);
		//	criarEventoTransmissao(tempoAtual);			
		}
		
		if(transmissaoCorrente==null) criarEventoTransmissao(tempoAtual);
	}
	
	@Override
	public String toString() {
		return this.distancia + "m";
	}

	public boolean livre(Transmissao eventoTransmissao, long tempoFinalUltimaTransmissao) {
		
		// se tempo Atual >= tempo final da ultimaTransmissao + 9,6us -> true
		if (eventoTransmissao.getTempo() >= (tempoFinalUltimaTransmissao + tempoEntreQuadros))
			return true;
		
		return true;
		
	}

	public void enviarConfirmacao(Recepcao recepcao, long tempo) {
		
		if(concluirEnvioQuadro(recepcao.getQuadro())) {
			quadrosEnviados++;
			criarEventoTransmissao(tempo);
		}
		
	}

	private boolean concluirEnvioQuadro(Quadro quadro) {
		
		boolean b = false;
		
		confirmacoes++;
		
		if(confirmacoes == Simulador.getPcsConectados().size()){
			
//			tx.get(0).getQuadros().remove(quadro);
			tx.get(0).getQuadrosEnviados().add(quadro);
			transmissaoCorrente = null;
			
			// Se acabaram os quadros da mensagem, remova a mesma
			if (tx.get(0).getQuadros().size() == tx.get(0).getQuadrosEnviados().size()) {
				tx.remove(0);
			}
			b = true;
		}
		
		return b;
	}

	private void criarEventoTransmissao(long tempo) {
		
		if (!tx.isEmpty()) {
			
			// verifica se o evento deve ser criado com o tempo de criação da mensagem ou com o tempo atual
			long tempoCriacaoEvento = (tempo > tx.get(0).getTempoCriacao()) ? tempo : tx.get(0).getTempoCriacao();  
			
			// Seleciona o próximo quadro a ser enviado
			Quadro proxQuadro = null;
			for (Quadro quadro: tx.get(0).getQuadros()) {
				if (!tx.get(0).getQuadrosEnviados().contains(quadro)) {
					proxQuadro = quadro;
				}
			}
			
//			Transmissao evento = new Transmissao(tempoCriacaoEvento, Simulador.getRodadaAtual(), this, tx.get(0).getQuadros().get(0) );
			Transmissao evento = new Transmissao(tempoCriacaoEvento, Simulador.getRodadaAtual(), this, proxQuadro );
			Simulador.filaEventos.add(evento);
			confirmacoes = 0;
			System.out.println("Evento transmissao: " + evento);
			
			transmissaoCorrente = evento;
		}
		
	}
}