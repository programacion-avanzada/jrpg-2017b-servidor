package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Batalla extends ComandosServer {

	@Override // Recibe paquetes de un cliente y los envía a otro cliente o a un NPC. Un NPC no inicia batallas con este método porque no tiene cliente.
	public void ejecutar() {
		// Se crea el PaqueteBatalla a partir del EscuchaCliente del retador.
		escuchaCliente.setPaqueteBatalla((PaqueteBatalla) gson.fromJson(cadenaLeida, PaqueteBatalla.class));
		Servidor.log.append(escuchaCliente.getPaqueteBatalla().getId() + " quiere batallar con " + escuchaCliente.getPaqueteBatalla().getIdEnemigo() + System.lineSeparator());

		try {
			// Se le asigna estadoBatalla en la lista de personajes del servidor a ambos.
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getId()).setEstado(Estado.estadoBatalla);
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getIdEnemigo()).setEstado(Estado.estadoBatalla);
			escuchaCliente.getPaqueteBatalla().setMiTurno(true);
			escuchaCliente.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla())); // Se le envía el PaqueteBatalla al cliente del retador.

			if (escuchaCliente.getPaqueteBatalla().getIdEnemigo() > 0) { // Si tiene ID positivo, el enemigo es un usuario.
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) { // Busca el cliente del que fue retado en la lista de clientes.
					if (conectado.getIdPersonaje() == escuchaCliente.getPaqueteBatalla().getIdEnemigo()) {
						int aux = escuchaCliente.getPaqueteBatalla().getId();
						escuchaCliente.getPaqueteBatalla().setId(escuchaCliente.getPaqueteBatalla().getIdEnemigo());
						escuchaCliente.getPaqueteBatalla().setIdEnemigo(aux);
						escuchaCliente.getPaqueteBatalla().setMiTurno(false);
						conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla())); // Se le envía el PaqueteBatalla al cliente del que fue retado.
						break; // Sale del ciclo for each
					}
				}
			} else { // Si tiene ID negativo, el enemigo es un NPC.
				int aux = escuchaCliente.getPaqueteBatalla().getId();
				escuchaCliente.getPaqueteBatalla().setId(escuchaCliente.getPaqueteBatalla().getIdEnemigo());
				escuchaCliente.getPaqueteBatalla().setIdEnemigo(aux);
				escuchaCliente.getPaqueteBatalla().setMiTurno(false);
				Servidor.getNPCsCargados().get(escuchaCliente.getPaqueteBatalla().getId()).setPb(escuchaCliente.getPaqueteBatalla()); // Se le envía el PaqueteBatalla al NPC.
			}
		} catch (IOException e) {
			Servidor.log.append("Falló al intentar enviar Batalla \n");
		}

		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}

	}

}
