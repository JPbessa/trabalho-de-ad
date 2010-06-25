package model;

/**
 * Classe que representa um host ligado ao hub. 
 */
public class PC extends Thread {
	
	/**
	 * Propagação elétrica no meio físico.
	 */
	private static final double tempoPropagacaoNoMeio = 5 * Math.pow(10, -6); // 5 milisegundos/metro
	
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
	
	private double p;
	
	private IntervaloChegadas A;

	private double taxaChegada;
	
	private final static double tempoEntreQuadros = 9.6;
	
	private double relogio = 0;
	
	/**
	 * Construtor da classe.
	 * @param distancia Distância do host ao hub (em metros).
	 */
	public PC(int distancia) {
		this.distancia = distancia;
	}
	
	/**
	 * Calcula o tempo de atraso de propagação para o host.
	 * @return tempo O tempo do atraso de propagação.
	 */
	double atrasoPropagacao() {
		return this.distancia * tempoPropagacaoNoMeio; // em milisegundos
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
		if (a.tipo == TipoDistribuicao.DETERMINISTICO) {
			this.taxaChegada = this.A.valor;
		} else if (a.tipo == TipoDistribuicao.EXPONENCIAL) {
			this.taxaChegada = 1/this.A.valor;
		}
	}
	
	/**
	 * Verifica se o cabo de conexão com o hub está livre ou não.
	 * @return boolean Variável que indica se está livre ou não.
	 */
	private boolean meioLivre() {
		//TODO
		return false;
	}
	
	@Override
	public synchronized void start() {
		while (true) {
			tx = new Mensagem(p);
			if (meioLivre()) {
				while (relogio < tempoEntreQuadros) {
					// incremento relogio
				}
				
				tx.transmitir();

				relogio = 0;
			} else {
				while (!meioLivre()) {
					// incremento relogio
				}
				try {
					sleep((long)0.0096);
					tx.transmitir();
				} catch (InterruptedException e) {
					System.out.println("Erro ao esperar o tempo entre quadros.");
				}
			}
		}
	}
}