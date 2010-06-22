package model;

public class Mensagem {
	
	private Quadro[] quadros;
	
	private int numeroDeQuadros;
	
	public Mensagem(Float p) {
		if (p > 0 && p < 1) {
			// geométrica
			
		} else {
			// deterministico
			numeroDeQuadros = p.intValue();
		}
	}
}
