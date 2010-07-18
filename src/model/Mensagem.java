package model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

public class Mensagem {
	
	private List<Quadro> quadros = new ArrayList<Quadro>();
	private List<Quadro> quadrosEnviados = new ArrayList<Quadro>();
	
	private int numeroDeQuadros = 0;
	
	private PC emissor;
	
	private long tempoCriacao;

	public Mensagem(double p, PC origem, long tempo) {
		
		if (p < 1) numeroDeQuadros = GeradorDados.gerarGeometrica(p);
		else numeroDeQuadros = (int)p;

		this.emissor = origem;
		this.tempoCriacao = tempo;
		geraQuadros();
	}
	
	public void transmitir() {
		for (Quadro quadro : quadros) {
			//emissor.setTap(quadro.transmitir());
		}
	}
	
	private boolean geraQuadros() {
		for(int i=0; i < numeroDeQuadros; i++) new Quadro(this);
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
	
	public void setQuadroEnviado(Quadro q) {
		this.quadrosEnviados.add(q);
	}
	
	public PC getEmissor(){
		return this.emissor;
	}
	
	public long getTempoCriacao() {
		return tempoCriacao;
	}
	
	@Override
	public String toString() {
		return "t=" + tempoCriacao + "," + "#quadros=" + numeroDeQuadros + ", pc=" + emissor;
	}
}
