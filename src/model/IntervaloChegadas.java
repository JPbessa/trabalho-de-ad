package model;

public class IntervaloChegadas {

	long valor;
	
	TipoDistribuicao tipo;

	public IntervaloChegadas(long valor, TipoDistribuicao tipo) {
		this.valor = valor;
		this.tipo = tipo;
	}
	
	public long getValor() {
		return valor;
	}

	public void setValor(long valor) {
		this.valor = valor;
	}

	public TipoDistribuicao getTipo() {
		return tipo;
	}

	public void setTipo(TipoDistribuicao tipo) {
		this.tipo = tipo;
	}
	
	
}
