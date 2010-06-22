package model;

/**
 * Classe que representa um host ligado ao hub. 
 */
public class PC extends Thread {
	
	/**
	 * Propagação elétrica no meio físico.
	 */
	private static final double tempoPropagacaoNoMeio = 5.0 * Math.pow(10, -6); // 5 milisegundos/metro
	
	/**
	 * Distância do host ao hub.
	 */
	private int distancia;
	
	/**
	 * Buffer de recebimento de mensagens.
	 */
	private Mensagem rx;
	
	/**
	 * Buffer de transmissão de mensagens.
	 */
	private Mensagem tx;
	
	/**
	 * TODO
	 */
	private Float p;
	
	/**
	 * TODO
	 */
	private IntervaloChegadas A;
	
	/**
	 * 
	 */
	private Float taxaChegada;
	
	/**
	 * Construtor da classe.
	 * @param distancia Distância do host ao hub.
	 */
	public PC(int distancia) {
		this.distancia = distancia;
	}
	
	/**
	 * Calcula o tempo de atraso de propagação para o host.
	 * @return tempo O tempo do atraso de propagação.
	 */
	double atrasoPropagacao() {
		return distancia * tempoPropagacaoNoMeio; // em milisegundos
	}

	public Float getP() {
		return p;
	}

	public void setP(Float p) {
		this.p = p;
	}

	public IntervaloChegadas getA() {
		return A;
	}

	public void setA(IntervaloChegadas a) {
		A = a;
		if (a.tipo == TipoDistribuicao.DETERMINISTICO) {
			taxaChegada = A.valor;
		} else if (a.tipo == TipoDistribuicao.EXPONENCIAL) {
			taxaChegada = 1/A.valor;
		}
	}
	
	private boolean meioLivre() {
		//TODO
		return false;
	}
	
	private void transmitirMensagem() {
		
	}

	private final static Float tempoEntreQuadros = new Float(9.6);
	private Float relogio = new Float(0.0);
	
	@Override
	public synchronized void start() {
		while (true) {
			tx = new Mensagem(p);
			if (meioLivre()) {
				while (relogio < tempoEntreQuadros) {
					// incremento relogio
				}
				transmitirMensagem();
				relogio = new Float(0.0);
			} else {
				while (!meioLivre()) {
					// incremento relogio
				}
				try {
					sleep((long)0.0096);
					transmitirMensagem();
				} catch (InterruptedException e) {
					System.out.println("Erro ao pausar a thread.");
				}
			}
		}
	}
}