package servidor;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeMovimientos;

public class AtencionMovimientosNPC {
	private final Gson gson = new Gson();

	public AtencionMovimientosNPC() {
		
	}

	public void run() {

		synchronized(this){
		
			try {
	
				while (true) {
			
					// Espero a que se conecte alguien
					wait();
					
					// EL FOREACH DEBERIA SER POR CADA NPC CONECTADO
					for(EscuchaNPC npcActivo : Servidor.getNCPSActivos()) {
						
						for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
						
							//if(/*npcActivo.getPaqueteNPC().calcularDistancia(ubicacionConectado)*/){
							
								//PaqueteDeMovimientos pdp = (PaqueteDeMovimientos) new PaqueteDeMovimientos(Servidor.getUbicacionPersonajes()).clone();
								//pdp.setComando(Comando.MOVIMIENTO);
								//synchronized (conectado) {
								//	conectado.getSalida().writeObject(gson.toJson(pdp));									
								//}
							//}
						}
					}
				}
			} catch (Exception e){
				Servidor.log.append("Falló al intentar enviar paqueteDeMovimientos \n");
			}
		}
	}
}
