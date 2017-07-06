package comandos;

import java.io.IOException;

import mensajeria.PaqueteComerciar;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Trueque extends ComandosServer {

	@Override
	public void ejecutar() {
		PaqueteComerciar paqueteComerciar;
		paqueteComerciar = (PaqueteComerciar) gson.fromJson(cadenaLeida, PaqueteComerciar.class);
		//BUSCO EN LAS ESCUCHAS AL QUE SE LO TENGO QUE MANDAR
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getPaquetePersonaje().getId() == paqueteComerciar.getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(paqueteComerciar));
				} catch (IOException e) {
					Servidor.log.append("Falló al intentar enviar trueque a:" + conectado.getPaquetePersonaje().getId() + "\n");
				}	
			} 
			else if(conectado.getPaquetePersonaje().getId() == paqueteComerciar.getId()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(paqueteComerciar));
				} catch (IOException e) {
					Servidor.log.append("Falló al intentar enviar trueque a:" + conectado.getPaquetePersonaje().getId() + "\n");
				}
			}
		}
	}

}
