package model;

public class Evento implements Comparable<Evento>{

	private Double tempo;
	
	public Evento(Double tempo_execucao){
		tempo = tempo_execucao;
	}
	
	@Override
	public int compareTo(Evento e) {
		return this.tempo.compareTo(e.tempo);
	}
	
	public Double getTempo(){
		return tempo;
	}
}
