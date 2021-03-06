package model;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;

public class GeradorDados {
	
	public static Double gerarExponencial(Double esperanca){
		RandomData gerador = new RandomDataImpl();
		return gerador.nextExponential(esperanca);
	}

	public static int gerarGeometrica(double p) {
		return (int)Math.ceil(Math.log(Math.random()/Math.log(1.0 - p)));
	}
	
	public static long gerarTempoFinalTransmissao(Transmissao transmissao){
		return transmissao.getTempo() + transmissao.getPc().atrasoPropagacao() + transmissao.getPc().getTempoDeTransmissao();
	}
	
	public static long gerarTempoFinalRecepcao(Recepcao recepcao){
		return recepcao.getTempo() + recepcao.getPc().getTempoDeTransmissao();
	}
	
	
	
}
