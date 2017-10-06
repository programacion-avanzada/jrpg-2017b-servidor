package comandos;

import java.io.IOException;

import mensajeria.PaqueteDeNpcs;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ActualizarNpcs extends ComandosServer
{
	@Override
	public void ejecutar()
	{
		PaqueteDeNpcs paqueteActualizarNpcs = (PaqueteDeNpcs) gson.fromJson(cadenaLeida, PaqueteDeNpcs.class);
		escuchaCliente.setPaqueteDeNpcs(paqueteActualizarNpcs);
		
		if (paqueteActualizarNpcs.getPaquetesNpcs() != null)
			Servidor.setPaqueteDeNpcs(paqueteActualizarNpcs);
		
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) 
		{
			try 
			{
				conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteDeNpcs()));
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				Servidor.log.append("Falló al intentar enviar finalizarBatalla a:" + conectado.getPaquetePersonaje().getId() + "\n");
			}
		}
	}

}
