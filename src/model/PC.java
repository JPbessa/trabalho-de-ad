package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import controller.Simulador;

public class PC {
	
	private static final int tempoPropagacaoNoMeio = 5; // 5 nanosegundos/metro
	private int distancia;
	private List<Mensagem> tx;
	private double p;
	private IntervaloChegadas A;
	public final long tempoEntreQuadros = 9600; // em ns
	public final long tempoDeTransmissao = (int) Math.pow(10, 5); // em ns 
	
	int confirmacoes = 0;
	
	private Vector<Double> tap;

	private Transmissao proximaTransmissao;
	
	public PC(int distancia) {
		this.distancia = distancia;
		this.tap = new Vector<Double>();
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
	
	public long getTempoDeTransmissao() {
		return tempoDeTransmissao;
	}
	
	public void gerarMensagens(long tempoAtual,int rodada) {
		
		long tempoUltimaMsg = tx.isEmpty() ? 0 : tx.get(tx.size()-1).getTempoCriacao();

		// Gera novas mensagens de acordo com o tipo de chegada de mensagens
		if(tempoUltimaMsg <= tempoAtual) {
			
			if (tx.isEmpty()) {
				this.tx.add(new Mensagem(this.p, this, 0));
			} else {
				long tempo = 0;
				if (A.tipo == TipoDistribuicao.DETERMINISTICO) {
					tempo = tempoUltimaMsg + A.getValor();
				} else if (A.tipo == TipoDistribuicao.EXPONENCIAL) {
					Double tempoExponencial = GeradorDados.gerarExponencial(new Double(A.getValor()));
					tempo = tempoUltimaMsg + tempoExponencial.intValue();
				}
				this.tx.add(new Mensagem(this.p, this, tempo));
			}

			System.out.println("GerarMensagens(PC "+this.distancia+") com " + tx.size() +" mensagens.");
			criarEventoTransmissao(tempoAtual);			
		}
		
		if(proximaTransmissao==null) criarEventoTransmissao(tempoAtual);
	}
	
	@Override
	public String toString() {
		return this.distancia + "m";
	}

	public boolean livre(Transmissao eventoTransmissao) {
		// TODO ter passado 9,6 us da ultima transmissao
		return true;
	}

	public void enviarConfirmacao(Quadro quadro, long tempo) {
		
		if(concluirEnvioQuadro(quadro))	criarEventoTransmissao(tempo);
		
	}

	private boolean concluirEnvioQuadro(Quadro quadro) {
		
		boolean b = false;
		
		confirmacoes++;
		
		if(confirmacoes == Simulador.getPcsConectados().size()){
			tx.get(0).getQuadros().remove(quadro);
			
			// Se acabaram os quadros da mensagem, remova a mesma
			if (tx.get(0).getQuadros().isEmpty()) {
				tx.remove(0);
			}
			b = true;
		}
		
		return b;
	}

	private void criarEventoTransmissao(long tempo) {
		
		if (!tx.isEmpty()) {
			
			Transmissao evento = new Transmissao(tempo, Simulador.getRodadaAtual(), this, tx.get(0).getQuadros().get(0));
			Simulador.filaEventos.add(evento);
			confirmacoes = 0;
			System.out.println("Evento transmissao: " + evento);
			
			proximaTransmissao = evento;
		}
		
	}
}