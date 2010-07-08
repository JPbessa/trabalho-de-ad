package model;

public class Evento implements Comparable<Evento>{

	private Long tempo;
	private TipoEvento tipo;
	private PC pc;
	private Quadro quadro;
	
	public Evento(Long tempo, TipoEvento tipo, PC pc, Quadro quadro){
		this.tempo = tempo;
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
}
