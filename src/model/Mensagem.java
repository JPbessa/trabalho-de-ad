package model;

import java.util.ArrayList;
import java.util.List;

public class Mensagem {
	
	private List<Quadro> quadros = new ArrayList<Quadro>();
	
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
