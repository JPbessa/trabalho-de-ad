package model;

public abstract class Evento implements Comparable<Evento>{

	private Long tempo;
	private int rodada;
	private PC pc;
	private Quadro quadro;
	
	public Evento(Long tempo, int rodada, PC pc, Quadro quadro){
		this.tempo = tempo;
		this.rodada = rodada;
		this.pc = pc;
		this.quadro = quadro;
	}
	
	public abstract void executar();
	
	@Override
	public int compareTo(Evento e) {
		if (this.getTempo() > e.getTempo()) {
			return 1;
		} else if (this.getTempo().equals(e.getTempo())) {
			if (this.getPc().getDistancia() >= e.getPc().getDistancia()) {
				return 1;
			}
		}
		return -1;
	}
	
	public Long getTempo(){
		return tempo;
	}
	
	public int getRodada(){
		return rodada;
	}

	public PC getPc() {
		return pc;
	}

	public Quadro getQuadro() {
		return quadro;
	}
	
	@Override
	public String toString() {
		return "evento {" +
					"tempo: " + tempo + ", " +
					"rodada: " + rodada + ", " +
					"pc: " + pc + ", " +
					"quadro: " + quadro +
				"}";
	}
	
	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

}
