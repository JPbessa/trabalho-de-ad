package model;

import java.util.Vector;

import controller.Simulador;

public class PC {
	
	private static final int tempoPropagacaoNoMeio = 5; // 5 nanosegundos/metro
	private int distancia;
	private Mensagem tx;
	private double p;
	private IntervaloChegadas A;
	private double taxaChegada;
	private final int tempoEntreQuadros = 9600; // em ns
	private final int tempoDeTransmissao = (int) Math.pow(10, 5); // em ns 
	
	private Vector<Double> tap;

	public PC(int distancia) {
		this.distancia = distancia;
		this.tap = new Vector<Double>();
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

	public Mensagem getTx() {
		return tx;
	}
	
	public double getP() {
		return p;
	}

	public void setP(double p) {
		this.p = p;
		this.tx = new Mensagem(this.p, this);
	}

	public IntervaloChegadas getA() {
		return A;
	}

	public void setA(IntervaloChegadas a) {
		this.A = a;
		if (a.tipo == TipoDistribuicao.DETERMINISTICO) {
			this.taxaChegada = this.A.valor;
		} else if (a.tipo == TipoDistribuicao.EXPONENCIAL) {
			this.taxaChegada = 1/this.A.valor;
		}
	}
	
	public int getTempoDeTransmissao() {
		return tempoDeTransmissao;
	}
	
	public void gerarEventos(int rodada) {
		int eventosCriados = 0;
		for (Quadro quadro : tx.getQuadros()) {
			Long tempo = (Long) (Simulador.inicioSimulacao + (tempoEntreQuadros+tempoDeTransmissao)*eventosCriados);
			Simulador.filaEventos.add(new Evento(tempo, rodada, TipoEvento.EMISSAO, this, quadro));
			System.out.println("Evento criado: (" + tempo + ", " + rodada + ", TipoEvento.EMISSAO, PC" + distancia + ", " + quadro.hashCode() + ")");
			eventosCriados++;
		}
	}
}