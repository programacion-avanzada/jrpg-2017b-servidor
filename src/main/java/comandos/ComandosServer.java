package comandos;

import mensajeria.Comando;
import servidor.EscuchaCliente;
import servidor.EscuchaNPC;

public abstract class ComandosServer extends Comando{
	protected EscuchaCliente escuchaCliente;
	protected EscuchaNPC escuchaNPC;

	public void setEscuchaCliente(EscuchaCliente escuchaCliente) {
		this.escuchaCliente = escuchaCliente;
	}
	
	public void setEscuchaNPC(EscuchaNPC escuchaNPC) {
		this.escuchaNPC = escuchaNPC;
	}
}
