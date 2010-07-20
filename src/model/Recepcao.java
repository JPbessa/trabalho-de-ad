package model;

public class Recepcao extends Evento {

	public Recepcao(Long tempo, int rodada, PC pc, Quadro quadro) {
		super(tempo, rodada, pc, quadro);
	}

	@Override
	public void executar() {
		getQuadro().receber(this);
	}

}
