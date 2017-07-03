package comandos;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

import mensajeria.Comando;
import mensajeria.PaqueteMensaje;
import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Talk extends ComandosServer {

	@Override
	public void ejecutar() {
		int idUser = 0;
		int contador = 0;
		PaqueteMensaje paqueteMensaje = (PaqueteMensaje) (gson.fromJson(cadenaLeida, PaqueteMensaje.class));
		
		if (!(paqueteMensaje.getUserReceptor() == null)) {
			if (Servidor.mensajeAUsuario(paqueteMensaje)) {
				
				paqueteMensaje.setComando(Comando.TALK);
				
				for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
					if(personaje.getValue().getNombre().equals(paqueteMensaje.getUserReceptor())) {
						idUser = personaje.getValue().getId();
					}
				}
				
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
					if(conectado.getIdPersonaje() == idUser) {
						try {
							conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}			
			} else {
				System.out.println("Server: Mensaje No Enviado!");
			}
		} else {				
			for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
				if(personaje.getValue().getNombre().equals(paqueteMensaje.getUserEmisor())) {
					idUser = personaje.getValue().getId();
				}
			}
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if(conectado.getIdPersonaje() != idUser) {
					try {
						conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}		
			Servidor.mensajeAAll(contador);	
		}
	}
}
