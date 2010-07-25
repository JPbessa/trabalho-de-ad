package model;

import java.util.HashMap;

public class Transmissao extends Evento {

	private boolean transmissaoImediata;
	
	private boolean colidido;
	
	private HashMap<PC,Recepcao> recepcoes = new HashMap<PC, Recepcao>();
	
	public Transmissao(Long tempo, int rodada, PC pc, Quadro quadro) {
		this(tempo, rodada, pc, quadro, false);
	}

	public Transmissao(Long tempo, int rodada, PC pc, Quadro quadro, boolean transmissaoImediata) {
		super(tempo, rodada, pc, quadro);
		this.transmissaoImediata = transmissaoImediata;
	}
	
	@Override
	public void executar() {
		if (!isColidido()){
			getQuadro().transmitir(this);
		}
	}
	
	public boolean isTransmissaoImediata() {
		return transmissaoImediata;
	}

	public void setColidido(boolean colidido) {
		this.colidido = colidido;
	}

	public boolean isColidido() {
		return colidido;
	}

	public void setRecepcoes(HashMap<PC,Recepcao> recepcoes) {
		this.recepcoes = recepcoes;
	}

	public HashMap<PC,Recepcao> getRecepcoes() {
		return recepcoes;
	}

}
