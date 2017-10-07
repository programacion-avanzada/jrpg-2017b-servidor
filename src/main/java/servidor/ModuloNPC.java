package servidor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import com.google.gson.Gson;

import dominio.Asesino;
import dominio.Casta;
import dominio.Elfo;
import dominio.Guerrero;
import dominio.Hechicero;
import dominio.Humano;
import dominio.Orco;
import dominio.Personaje;
import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import mundo.Tile;

public class ModuloNPC {

	public static void ejecutar() {
		cargarNPCs(); // Carga los NPCs cuando se inicia el servidor.

		Runnable epNPC = new Runnable() {
			public void run() {
				enviarPaquetesNPCs(); // Envía los movimientos de los NPCs a los clientes.
			}
		};
		ScheduledExecutorService executor1 = Executors.newScheduledThreadPool(2);
		executor1.scheduleAtFixedRate(epNPC, 0, 500, TimeUnit.MILLISECONDS); // El código de epNPC se ejecuta cada 0.5 segundos.

		Runnable mNPC = new Runnable() {
			public void run() {
				manejarNPCs(); // Le da directivas a los NPC.
			}
		};
		ScheduledExecutorService executor2 = Executors.newScheduledThreadPool(2);
		executor2.scheduleAtFixedRate(mNPC, 0, 500, TimeUnit.MILLISECONDS); // El código de mNPC se ejecuta cada 0.5 segundos.
	}

	private static void cargarNPCs() {
		Scanner cantFile = null; // Contiene la cantidad de archivos a leer.
		int cant;
		int nroArchivo = 1;

		Scanner npcFile = null; // Contiene los atributos del NPC.

		try {
			cantFile = new Scanner(new File("npcs//cant.txt"));
		} catch (FileNotFoundException e1) {
			Servidor.log.append("No se pudo abrir el archivo cant.txt de la carpeta de NPCs." + System.lineSeparator());
			return;
		}
		cant = cantFile.nextInt();
		cantFile.close();

		while (cant != 0) {

			try { // Intenta leer un archivo.
				npcFile = new Scanner(new File("npcs//" + nroArchivo + ".txt"));
			} catch (FileNotFoundException e) {
				Servidor.log.append("No se pudo cargar el archivo " + nroArchivo + ".txt de la carpeta de NPCs." + System.lineSeparator());
				npcFile = null;
			}

			if (npcFile != null) { // Si pudo leer el archivo, crea el NPC.
				try {
					crearNPCDesdeArchivo(npcFile);
				} catch (IOException e) {
					Servidor.log.append("No se pudo cargar el archivo " + nroArchivo + ".txt de la carpeta de NPCs." + System.lineSeparator());
				}
				npcFile.close();
			}

			nroArchivo++;
			cant--;
		}
	}

	private static NPC crearNPCDesdeArchivo(Scanner npcFile) throws IOException {
		NPC nuevoNPC = new NPC();
		PaqueteMovimiento pm = new PaqueteMovimiento();
		PaquetePersonaje pp;
		pp = new PaquetePersonaje();

		pm.setIp("localhost");
		pp.setIp("localhost");

		nuevoNPC.setId(npcFile.nextInt());
		npcFile.nextLine();
		nuevoNPC.setDificultad(npcFile.nextInt());
		npcFile.nextLine();
		nuevoNPC.setMovimiento(npcFile.nextInt());
		npcFile.nextLine();
		nuevoNPC.setPersistencia(npcFile.nextInt());
		npcFile.nextLine();
		npcFile.nextLine();

		// PaqueteMovimiento
		pm.setIdPersonaje(nuevoNPC.getId());
		int jMin = npcFile.nextInt();
		npcFile.nextLine();
		int jMax = npcFile.nextInt();
		npcFile.nextLine();
		int iMin = npcFile.nextInt();
		npcFile.nextLine();
		int iMax = npcFile.nextInt();
		npcFile.nextLine();
		int j = ThreadLocalRandom.current().nextInt(jMin, jMax + 1);
		int i = ThreadLocalRandom.current().nextInt(iMin, iMax + 1);
		pm.setPosX(baldosasACoordenadas(j, i)[0]);
		pm.setPosY(baldosasACoordenadas(j, i)[1]);
		pm.setDireccion(npcFile.nextInt());
		npcFile.nextLine();
		pm.setFrame(npcFile.nextInt());
		npcFile.nextLine();
		npcFile.nextLine();

		// PaquetePersonaje
		pp.setId(nuevoNPC.getId());
		pp.setMapa(npcFile.nextInt());
		npcFile.nextLine();
		pp.setEstado(npcFile.nextInt());
		npcFile.nextLine();
		pp.setCasta(npcFile.nextLine());
		pp.setRaza(npcFile.nextLine());
		pp.setNombre(npcFile.nextLine());
		pp.setSaludTope(npcFile.nextInt());
		npcFile.nextLine();
		pp.setEnergiaTope(npcFile.nextInt());
		npcFile.nextLine();
		pp.setFuerza(npcFile.nextInt());
		npcFile.nextLine();
		pp.setDestreza(npcFile.nextInt());
		npcFile.nextLine();
		pp.setInteligencia(npcFile.nextInt());
		npcFile.nextLine();
		pp.setNivel(npcFile.nextInt());
		npcFile.nextLine();
		pp.setExperiencia(npcFile.nextInt());
		pp.eliminarItems();

		nuevoNPC.setPm(pm);
		nuevoNPC.setPp(pp);
		Servidor.getUbicacionPersonajes().put(nuevoNPC.getId(), nuevoNPC.getPm());
		Servidor.getPersonajesConectados().put(nuevoNPC.getId(), nuevoNPC.getPp());
		Servidor.getNPCsCargados().put(nuevoNPC.getId(), nuevoNPC);
		Servidor.log.append("NPC " + nuevoNPC.getId() + " creado en coordenadas (" + j + ", " + i + ") del mapa " + pp.getMapa() + "." + System.lineSeparator());
		return nuevoNPC;
	}

	public static float[] baldosasACoordenadas(int j, int i) {
		float[] vec = new float[2];

		vec[0] = (j - i) * (Tile.ANCHO / 2) + 2; // El +2 es un parche para que quede centrado en la baldosa.
		vec[1] = (j + i) * (Tile.ALTO / 2) + 4; // El +4 es un parche para que quede centrado en la baldosa.

		return vec;
	}

	public static int[] coordenadasABaldosas(float x, float y) {
		int[] vec = new int[2];

		vec[0] = (int) x / (Tile.ANCHO / 2); // Esto da como resultado j - i
		vec[1] = (int) y / (Tile.ALTO / 2); // Esto da como resultado j + i

		vec[0] = (vec[0] + vec[1]) / 2;
		vec[1] = vec[1] - vec[0];

		// Funciona sin -2 y -4 porque existe un rango de valores de válidos para cada baldosa.

		return vec;
	}

	private static void manejarNPCs() {
		Servidor.getNPCsCargados().forEach((key, npc) -> manejarNPCs(npc));
	}

	private static void manejarNPCs(NPC npc) {
		int dificultad = npc.getDificultad();
		int movimiento = npc.getMovimiento();

		if (Servidor.getPersonajesConectados().get(npc.getId()).getEstado() == Estado.estadoJuego) {
			switch (movimiento) {
			case '1':
				moverTipo1(npc);
				break;
			default:
				moverTipo1(npc);
				break;
			}
		}

		if (Servidor.getPersonajesConectados().get(npc.getId()).getEstado() == Estado.estadoBatalla) {
			switch (dificultad) {
			case '1':
				batallarTipo1(npc);
				break;
			default:
				batallarTipo1(npc);
				break;
			}
		}
	}

	private static void moverTipo1(NPC npc) {
		if (Servidor.getUbicacionPersonajes().get(npc.getId()).getDireccion() == 1) {
			Servidor.getUbicacionPersonajes().get(npc.getId()).setDireccion(5);
		} else {
			Servidor.getUbicacionPersonajes().get(npc.getId()).setDireccion(1);
		}
	}

	private static void batallarTipo1(NPC npc) {
		PaquetePersonaje paqueteNPC = (PaquetePersonaje) npc.getPp().clone();
		PaquetePersonaje paqueteEnemigo = (PaquetePersonaje) Servidor.getPersonajesConectados().get(npc.getPb().getIdEnemigo()).clone();
		Personaje personaje = crearPersonajes(paqueteNPC, paqueteEnemigo)[0];
		Personaje enemigo = crearPersonajes(paqueteNPC, paqueteEnemigo)[1];

		while (npc.getPb() != null) { // Mientras dure la batalla
			Servidor.log.append(null);
			if (npc.getPb() != null && npc.getPb().isMiTurno()) { // Si es mi turno
				// Calcular daño recibido
				int daño = personaje.getSalud() - npc.getPa().getNuevaSaludEnemigo();
				personaje.reducirSalud(daño); // Actualiza salud del NPC.
				// Calcular agotamiento del enemigo
				int agotamiento = enemigo.getEnergia() - npc.getPa().getNuevaEnergiaPersonaje();
				enemigo.reducirEnergia(agotamiento); // Actualiza energía del enemigo.
				// Calcular ataque
				if (!personaje.habilidadCasta1(enemigo)) { // Actualiza salud del enemigo y energía del NPC.
					personaje.serEnergizado(10); // Si se queda sin energía, pide ser energizado
				}

				// Intentar atacar
				if (enemigo.getSalud() > 0) {
					PaqueteAtacar pa = new PaqueteAtacar(personaje.getIdPersonaje(), enemigo.getIdPersonaje(), personaje.getSalud(), personaje.getEnergia(), enemigo.getSalud(), enemigo.getEnergia(), personaje.getDefensa(), enemigo.getDefensa(), personaje.getCasta().getProbabilidadEvitarDaño(), enemigo.getCasta().getProbabilidadEvitarDaño());
					npc.setPa(pa);
				} else {
					PaqueteFinalizarBatalla pfb = new PaqueteFinalizarBatalla();
					pfb.setId(npc.getId());
					pfb.setIdEnemigo(npc.getPb().getIdEnemigo());
					pfb.setGanadorBatalla(npc.getId());
					npc.setPfb(pfb);
				}
			}
		}
	}

	private static Personaje[] crearPersonajes(PaquetePersonaje paquetePersonaje, PaquetePersonaje paqueteEnemigo) {
		Personaje personaje = null;
		Personaje enemigo = null;

		String nombre = paquetePersonaje.getNombre();
		int salud = paquetePersonaje.getSaludTope();
		int energia = paquetePersonaje.getEnergiaTope();
		int fuerza = paquetePersonaje.getFuerza();
		int destreza = paquetePersonaje.getDestreza();
		int inteligencia = paquetePersonaje.getInteligencia();
		int experiencia = paquetePersonaje.getExperiencia();
		int nivel = paquetePersonaje.getNivel();
		int id = paquetePersonaje.getId();

		Casta casta = null;
		try {
			casta = (Casta) Class.forName("dominio" + "." + paquetePersonaje.getCasta()).newInstance();
			personaje = (Personaje) Class.forName("dominio" + "." + paquetePersonaje.getRaza()).getConstructor(String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Casta.class, Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(nombre, salud, energia, fuerza, destreza, inteligencia, casta, experiencia, nivel, id);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			JOptionPane.showMessageDialog(null, "Error al crear la batalla");
		}

		nombre = paqueteEnemigo.getNombre();
		salud = paqueteEnemigo.getSaludTope();
		energia = paqueteEnemigo.getEnergiaTope();
		fuerza = paqueteEnemigo.getFuerza();
		destreza = paqueteEnemigo.getDestreza();
		inteligencia = paqueteEnemigo.getInteligencia();
		experiencia = paqueteEnemigo.getExperiencia();
		nivel = paqueteEnemigo.getNivel();
		id = paqueteEnemigo.getId();

		casta = null;
		if (paqueteEnemigo.getCasta().equals("Guerrero")) {
			casta = new Guerrero();
		} else if (paqueteEnemigo.getCasta().equals("Hechicero")) {
			casta = new Hechicero();
		} else if (paqueteEnemigo.getCasta().equals("Asesino")) {
			casta = new Asesino();
		}

		if (paqueteEnemigo.getRaza().equals("Humano")) {
			enemigo = new Humano(nombre, salud, energia, fuerza, destreza, inteligencia, casta, experiencia, nivel, id);
		} else if (paqueteEnemigo.getRaza().equals("Orco")) {
			enemigo = new Orco(nombre, salud, energia, fuerza, destreza, inteligencia, casta, experiencia, nivel, id);
		} else if (paqueteEnemigo.getRaza().equals("Elfo")) {
			enemigo = new Elfo(nombre, salud, energia, fuerza, destreza, inteligencia, casta, experiencia, nivel, id);
		}

		Personaje[] devolver = new Personaje[2];
		devolver[0] = personaje;
		devolver[1] = enemigo;
		return devolver;
	}

	private static void enviarPaquetesNPCs() {
		Gson gson = new Gson();
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {

			if (conectado.getPaquetePersonaje().getEstado() != Estado.estadoOffline) {

				PaqueteDePersonajes pdp = (PaqueteDePersonajes) new PaqueteDePersonajes(Servidor.getPersonajesConectados()).clone();
				pdp.setComando(Comando.CONEXION);
				synchronized (conectado) {
					try {
						conectado.getSalida().writeObject(gson.toJson(pdp));
					} catch (IOException e) {
						Servidor.log.append("No se pueden actualizar los PaquetePersonaje de los NPCs en este momento." + System.lineSeparator());
					}
				}
			}

			if (conectado.getPaquetePersonaje().getEstado() == Estado.estadoJuego) {

				PaqueteDeMovimientos pdp = (PaqueteDeMovimientos) new PaqueteDeMovimientos(Servidor.getUbicacionPersonajes()).clone();
				pdp.setComando(Comando.MOVIMIENTO);
				synchronized (conectado) {
					try {
						conectado.getSalida().writeObject(gson.toJson(pdp));
					} catch (IOException e) {
						Servidor.log.append("No se pueden actualizar los PaqueteMovimiento de los NPCs en este momento." + System.lineSeparator());
					}
				}
			}

		}
	}
}
