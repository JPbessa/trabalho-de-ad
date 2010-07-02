package model;

public abstract class Evento implements Comparable<Evento>{

	private Double tempo;
	
	@Override
	public int compareTo(Evento e) {
		return this.tempo.compareTo(e.tempo);
	}
}
