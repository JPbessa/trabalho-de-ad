package model;

import java.util.List;
import java.util.Vector;

import model.exception.QuadroDescartadoException;

import controller.Simulador;

/**
 * Classe que representa um host ligado ao hub. 
 */
public class PC {
	
	/**
	 * Propaga��o el�trica no meio f�sico.
	 */
	private static final double tempoPropagacaoNoMeio = 5 * Math.pow(10, -6); // 5 milisegundos/metro
	
	/**
	 * Dist�ncia do host ao hub.
	 */
	private int distancia;
	
	/**
	 * Buffer de recebimento de mensagens.
	 */
	private Mensagem rx;
	
	/**
	 * Buffer de transmiss�o de mensagens.
	 */
	private Mensagem tx;
	
	private double p;
	
	private IntervaloChegadas A;

	private double taxaChegada;
	
	private final static double tempoEntreQuadros = 9.6;
	
	private double relogio = 0;
	
	private Vector<Double> tap;
	
	/**
	 * Construtor da classe.
	 * @param distancia Dist�ncia do host ao hub (em metros).
	 */
	public PC(int distancia) {
		this.distancia = distancia;
		this.tap = new Vector<Double>();
	}
	
	public void setTap(long val) {
		this.tap.add((double) val);
	}

	/**
	 * Calcula o tempo de atraso de propaga��o para o host.
	 * @return tempo O tempo do atraso de propaga��o.
	 */
	double atrasoPropagacao() {
		return this.distancia * tempoPropagacaoNoMeio; // em milisegundos
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
	
	/**
	 * Verifica se o cabo de conex�o com o hub est� livre ou n�o.
	 * @return boolean Vari�vel que indica se est� livre ou n�o.
	 */
	private boolean meioLivre() {
		//TODO
		return false;
	}
	
	public void gerarEventos() {
		
		List<Quadro> quadros = tx.getQuadros();
		
		for(int i=0; i < tx.getNumeroDeQuadros(); i++){
			try {
				Double tempo_quadro = quadros.get(i).binaryBackoff();
				// caso nao tenha gerado a Exception de descarte do quadro, adiciona a fila.
				Simulador.filaEventos.add(new Evento(tempo_quadro));
				//TODO avaliar como tratar quando entra com o mesmo tempo...
			} catch (QuadroDescartadoException e) {
				System.out.println("Quadro descartado! Não entrou na fila.");
				//e.printStackTrace();
			}
		}
		
		System.out.println("Tamanho da fila de eventos gerada = " + Simulador.filaEventos.size());
		System.out.println("Iniciando transmissao de PC1....");
		tx.transmitir();
		
	}

	public Double calculaMediaTAP() {
		Double soma = 0.0;
		for(int i=0; i<this.tap.size();i++){
			soma+=(Double) this.tap.get(i);
		}
		
		return soma/this.tap.size();
	}
	
//	@Override
//	public synchronized void start() {
//		while (true) {
//			tx = new Mensagem(p);
//			if (meioLivre()) {
//				while (relogio < tempoEntreQuadros) {
//					// incremento relogio
//				}
//				
//				tx.transmitir();
//
//				relogio = 0;
//			} else {
//				while (!meioLivre()) {
//					// incremento relogio
//				}
//				try {
//					sleep((long)0.0096);
//					tx.transmitir();
//				} catch (InterruptedException e) {
//					System.out.println("Erro ao esperar o tempo entre quadros.");
//				}
//			}
//		}
//	}
}