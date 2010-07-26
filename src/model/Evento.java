package model;

public abstract class Evento implements Comparable<Evento>{

	private Long tempo;
	private int rodada;
	private PC pc;
	private Quadro quadro;
	private long id;
	private static long uid;
	
	public Evento(Long tempo, int rodada, PC pc, Quadro quadro){
		
		assert(quadro!=null);
		
		this.tempo = tempo;
		this.rodada = rodada;
		this.pc = pc;
		this.quadro = quadro;
		this.id = uid++;
	}
	
	public abstract void executar();
	
	@Override
	public int compareTo(Evento e) {
		if (this.getTempo() > e.getTempo()) {
			return 1;
		} else if (this.getTempo().equals(e.getTempo())) {
			if (this.getId() > e.getId()){
				return 1;
			}else if (this.getId() == e.getId()){
				return 0;
			}
		}
		return -1;
	}
	
	public long getId() {
		return id;
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
	
	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	public abstract boolean isColidido();

}
