package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.google.gson.Gson;

import comandos.ComandosServer;
import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteNPC;
import mensajeria.PaqueteUsuario;

public class EscuchaNPC extends Thread{
	
	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private final Gson gson = new Gson();
	
	private PaqueteNPC paqueteNPC;
	private PaqueteBatalla paqueteBatalla;
	private PaqueteAtacar paqueteAtacar;
	private PaqueteFinalizarBatalla paqueteFinalizarBatalla;
	private PaqueteUsuario paqueteUsuario;
	
	public EscuchaNPC(String ip, Socket socket, ObjectInputStream entrada, ObjectOutputStream salida) throws IOException {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paqueteNPC = new PaqueteNPC();
	}
	
	public void run() {
		try {
			ComandosServer comand;
			Paquete paquete;
			paqueteUsuario = new PaqueteUsuario();

			String cadenaLeida = (String) entrada.readObject();
		
			while (!((paquete = gson.fromJson(cadenaLeida, Paquete.class)).getComando() == Comando.DESCONECTAR)){
								

				comand = (ComandosServer) paquete.getObjeto(Comando.NOMBREPAQUETE);
				comand.setCadena(cadenaLeida);
				comand.setEscuchaNPC(this);
				comand.ejecutar();
				cadenaLeida = (String) entrada.readObject();
			}

			entrada.close();
			salida.close();
			socket.close();

			Servidor.getNCPSActivos().remove(paqueteNPC.getNombre());
			Servidor.getUbicacionNPC().remove(paqueteNPC.getNombre());
			Servidor.getClientesConectados().remove(this);


		} catch (IOException | ClassNotFoundException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
		} 
	}

	public PaqueteNPC getPaqueteNPC() {
		return paqueteNPC;
	}

	public void setPaqueteNPC(PaqueteNPC paqueteNPC) {
		this.paqueteNPC = paqueteNPC;
	}

	public PaqueteBatalla getPaqueteBatalla() {
		return paqueteBatalla;
	}

	public void setPaqueteBatalla(PaqueteBatalla paqueteBatalla) {
		this.paqueteBatalla = paqueteBatalla;
	}

	public PaqueteAtacar getPaqueteAtacar() {
		return paqueteAtacar;
	}

	public void setPaqueteAtacar(PaqueteAtacar paqueteAtacar) {
		this.paqueteAtacar = paqueteAtacar;
	}

	public PaqueteFinalizarBatalla getPaqueteFinalizarBatalla() {
		return paqueteFinalizarBatalla;
	}

	public void setPaqueteFinalizarBatalla(PaqueteFinalizarBatalla paqueteFinalizarBatalla) {
		this.paqueteFinalizarBatalla = paqueteFinalizarBatalla;
	}

	public PaqueteUsuario getPaqueteUsuario() {
		return paqueteUsuario;
	}

	public void setPaqueteUsuario(PaqueteUsuario paqueteUsuario) {
		this.paqueteUsuario = paqueteUsuario;
	}
	
	
}
