package model;

/**
 * Classe que representa um host ligado ao hub. 
 */
public class PC extends Thread {
	
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
	
	/**
	 * Construtor da classe.
	 * @param distancia Dist�ncia do host ao hub (em metros).
	 */
	public PC(int distancia) {
		this.distancia = distancia;
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