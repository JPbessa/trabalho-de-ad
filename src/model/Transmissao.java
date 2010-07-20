package model;

public class Transmissao extends Evento {

	private boolean transmissaoImediata;
	
	public Transmissao(Long tempo, int rodada, PC pc, Quadro quadro) {
		this(tempo, rodada, pc, quadro, false);
	}

	public Transmissao(Long tempo, int rodada, PC pc, Quadro quadro, boolean transmissaoImediata) {
		super(tempo, rodada, pc, quadro);
		this.transmissaoImediata = transmissaoImediata;
	}
	
	@Override
	public void executar() {
		getQuadro().transmitir(this);
	}
	
	public boolean isTransmissaoImediata() {
		return transmissaoImediata;
	}

}
