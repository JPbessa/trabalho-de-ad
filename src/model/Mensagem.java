package model;

import java.util.ArrayList;
import java.util.List;

public class Mensagem {
	
	private List<Quadro> quadros = new ArrayList<Quadro>();
	
	private int numeroDeQuadros;
	
	private PC emissor;
	
	public Mensagem(double p, PC origem) {
		if (p > 0 && p < 1) {
			// geom�trica
			
		} else {
			// deterministico
			numeroDeQuadros = (int)p;
		}

		this.emissor = origem;
		geraQuadros(p);
	}
	
	public void transmitir() {
		for (Quadro quadro : quadros) {
			emissor.setTap(quadro.transmitir());
		}
	}
	
	private boolean geraQuadros(double n) {
		for(int i=0; i<(int)n; i++) new Quadro(this);
		return true;
	}

	public int getNumeroDeQuadros() {
		return numeroDeQuadros;
	}

	public List<Quadro> getQuadros() {
		return quadros;
	}
	
	public void setQuadro(Quadro q) {
		this.quadros.add(q);
	}
	
	public PC getEmissor(){
		return this.emissor;
	}
}
