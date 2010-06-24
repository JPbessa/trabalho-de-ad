package model;

public class Mensagem {
	
	private Quadro[] quadros;
	
	private int numeroDeQuadros;
	
	public Mensagem(double p) {
		if (p > 0 && p < 1) {
			// geomŽtrica
			
		} else {
			// deterministico
			numeroDeQuadros = (int)p;
		}
	}
	
	public void transmitir() {
		for (Quadro quadro : quadros) {
			quadro.transmitir();
		}
	}
}
