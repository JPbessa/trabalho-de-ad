package model;

public class IntervaloChegadas {

	double valor;
	
	TipoDistribuicao tipo;

	public IntervaloChegadas(double valor, TipoDistribuicao tipo) {
		this.valor = valor;
		this.tipo = tipo;
	}
	
	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public TipoDistribuicao getTipo() {
		return tipo;
	}

	public void setTipo(TipoDistribuicao tipo) {
		this.tipo = tipo;
	}
	
	
}
