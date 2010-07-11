package model;

public class Evento implements Comparable<Evento>{

	private Long tempo;
	private int rodada;
	private TipoEvento tipo;
	private PC pc;
	private Quadro quadro;
	
	public Evento(Long tempo, int rodada, TipoEvento tipo, PC pc, Quadro quadro){
		this.tempo = tempo;
		this.rodada = rodada;
		this.tipo = tipo;
		this.pc = pc;
		this.quadro = quadro;
	}
	
	@Override
	public int compareTo(Evento e) {
		return this.tempo.compareTo(e.tempo);
	}
	
	public Long getTempo(){
		return tempo;
	}
	
	public int getRodada(){
		return rodada;
	}

	public TipoEvento getTipo() {
		return tipo;
	}

	public PC getPc() {
		return pc;
	}

	public Quadro getQuadro() {
		return quadro;
	}
}
