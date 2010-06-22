package model;

public class IntervaloChegadas {

	Float valor;
	
	TipoDistribuicao tipo;

	public IntervaloChegadas(Float valor, TipoDistribuicao tipo) {
		this.valor = valor;
		this.tipo = tipo;
	}
	
	public Float getValor() {
		return valor;
	}

	public void setValor(Float valor) {
		this.valor = valor;
	}

	public TipoDistribuicao getTipo() {
		return tipo;
	}

	public void setTipo(TipoDistribuicao tipo) {
		this.tipo = tipo;
	}
	
	
}
