package model;

import java.util.List;

import model.exception.QuadroDescartadoException;
import controller.Simulador;

public class Recepcao extends Evento {
	
	private Transmissao transmissao;
	
	public Recepcao(Long tempo, int rodada, PC pc, Quadro quadro, Transmissao transmissao) {
		super(tempo, rodada, pc, quadro);
		this.setTransmissao(transmissao);
		this.transmissao.getRecepcoes().put(pc,this);
	}

	@Override
	public void executar() {
		if (!isColidido()) {
			getQuadro().receber(this);
		} else {
			try {
				getQuadro().tratarColisao(this);
//				List<Transmissao> transmissoes = Simulador.transmissoesAbertas.get(this.getPc());
//				transmissoes.remove(this);
			} catch (QuadroDescartadoException e) {
				this.transmissao.getPc().transmissaoCorrente = null;
				System.out.println("Quadro descartado: " + getQuadro());
			}
		}
	}

	public void setTransmissao(Transmissao transmissao) {
		this.transmissao = transmissao;
	}

	public Transmissao getTransmissao() {
		return transmissao;
	}

	@Override
	public boolean isColidido() {
		return this.getTransmissao().isColidido();
	}

	public void setColidido(boolean colisao) {
		this.transmissao.setColidido(colisao);
	}

}
