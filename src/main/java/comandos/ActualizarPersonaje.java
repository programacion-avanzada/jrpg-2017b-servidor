package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ActualizarPersonaje extends ComandosServer {

	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) gson.fromJson(cadenaLeida, PaquetePersonaje.class));

		if (escuchaCliente.getPaquetePersonaje().getId() < 0) {
			Servidor.log.append("El NPC " + escuchaCliente.getPaquetePersonaje().getId() + " ha evitado la actualización con éxito." + System.lineSeparator());
			return;
		}

		Servidor.getConector().actualizarPersonaje(escuchaCliente.getPaquetePersonaje());
		Servidor.getPersonajesConectados().remove(escuchaCliente.getPaquetePersonaje().getId());
		Servidor.getPersonajesConectados().put(escuchaCliente.getPaquetePersonaje().getId(), escuchaCliente.getPaquetePersonaje());

		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			try {
				conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaquetePersonaje()));
			} catch (IOException e) {
				Servidor.log.append("Falló al intentar enviar paquetePersonaje a:" + conectado.getPaquetePersonaje().getId() + "\n");
			}
		}

	}

}
