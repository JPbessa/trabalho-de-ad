package model;

import java.util.List;
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
	
	public void setTap(long val) {
		this.tap.add((double) val);
	}

	public Long atrasoPropagacao() {
		return new Long(this.distancia * tempoPropagacaoNoMeio); // em nanosegundos
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
	
	public void gerarEventos() {
		
		List<Quadro> quadros = tx.getQuadros();
		int eventosCriados = 0;
		for(Quadro quadro : quadros){
			Long tempo = (Long) (Simulador.inicioSimulacao + (tempoEntreQuadros+tempoDeTransmissao)*eventosCriados);
			Simulador.filaEventos.add(new Evento(tempo, TipoEvento.EMISSAO, this, quadro));
			System.out.println("Evento criado: (" + tempo + ", TipoEvento.EMISSAO, PC1, " + quadro.hashCode() + ")");
			eventosCriados++;
		}
		
		System.out.println("Tamanho da fila de eventos gerada = " + Simulador.filaEventos.size());
		System.out.println("Iniciando transmissao de PC1....");
		tx.transmitir();
		
	}

}