package servidor;

import comandos.Atacar;
import comandos.FinalizarBatalla;
import estados.Estado;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import mundo.Tile;

public class NPC {
	private int id;
	private int dificultad; // Define qué metodo utiliza para pelear
	private int movimiento; // Define qué metodo utiliza para moverse
	private int persistencia; // Define qué hace luego de morir
	private PaquetePersonaje pp;
	private PaqueteMovimiento pm;
	private PaqueteBatalla pb;
	private PaqueteAtacar pa;
	private PaqueteFinalizarBatalla pfb;

	public PaquetePersonaje getPp() {
		return pp;
	}

	public void setPp(PaquetePersonaje pp) {
		this.pp = pp;
	}

	public PaqueteMovimiento getPm() {
		return pm;
	}

	public void setPm(PaqueteMovimiento pm) {
		this.pm = pm;
	}

	public PaqueteBatalla getPb() {
		return pb;
	}

	public void setPb(PaqueteBatalla pb) {
		this.pb = pb;
	}

	public PaqueteAtacar getPa() {
		return pa;
	}

	public void setPa(PaqueteAtacar pa) {
		this.pa = pa;

		if (pa == null) {
			return;
		}

		if (pa.getId() == this.getPp().getId()) {
			this.enviarAtaque();
			this.getPb().setMiTurno(false);
			return;
		}

		this.getPb().setMiTurno(true);
	}

	public PaqueteFinalizarBatalla getPfb() {
		return pfb;
	}

	public void setPfb(PaqueteFinalizarBatalla pfb) {
		this.pfb = pfb;

		if (pfb == null) {
			return;
		}

		if (pfb.getGanadorBatalla() == this.getPp().getId()) {
			this.ganarBatalla();
			return;
		}

		this.morir();
	}

	public void enviarAtaque() {
		Atacar at = new Atacar();
		at.ejecutarDesdeNPC(this.pa);
		this.setPa(null);
	}

	public void ganarBatalla() {
		FinalizarBatalla fb = new FinalizarBatalla();
		fb.ejecutarDesdeNPC(this.pfb);

		this.setPa(null);
		this.setPb(null);
		this.setPfb(null);
	}

	public void morir() {

		if (this.persistencia == 0) { // Desaparece.
			Servidor.getPersonajesConectados().remove(this.getId());
			Servidor.getUbicacionPersonajes().remove(this.getId());
			Servidor.getNPCsCargados().remove(this.getId());
		}
		if (this.persistencia == 1) { // "Revive" cerca.
			Servidor.getNPCsCargados().get(this.id).getPp().setEstado(Estado.estadoJuego);
			this.setPa(null);
			this.setPb(null);
			this.setPfb(null);

			float x = this.pm.getPosX();
			float y = this.pm.getPosY();
			int j = ModuloNPC.coordenadasABaldosas(x, y)[0];
			int i = ModuloNPC.coordenadasABaldosas(x, y)[1];

			// Se mueve en diagonal.
			if (j < 10) {
				j = 17;
				i = 1;
			} else {
			}

			Servidor.log.append("NPC " + this.id + " ha revivido en las coordenadas (" + j + ", " + i + ") del mapa " + this.pp.getMapa() + "." + System.lineSeparator());
			pm.setPosX(ModuloNPC.baldosasACoordenadas(j, i)[0]);
			pm.setPosY(ModuloNPC.baldosasACoordenadas(j, i)[1]);
		}
		if (this.persistencia == 2) { // "Revive" lejos.
			Servidor.getNPCsCargados().get(this.id).getPp().setEstado(Estado.estadoJuego);
			this.setPa(null);
			this.setPb(null);
			this.setPfb(null);

			float x = this.pm.getPosX();
			float y = this.pm.getPosY();
			int j = ModuloNPC.coordenadasABaldosas(x, y)[0];
			int i = ModuloNPC.coordenadasABaldosas(x, y)[1];

			// Se mueve a la otra mitad del mapa.
			if (j < Tile.ALTO / 2) {
				j += Tile.ALTO / 2;
			} else {
				j -= Tile.ALTO / 2;
			}

			Servidor.log.append("NPC " + this.id + " ha revivido en las coordenadas (" + j + ", " + i + ") del mapa " + this.pp.getMapa() + "." + System.lineSeparator());
			pm.setPosX(ModuloNPC.baldosasACoordenadas(j, i)[0]);
			pm.setPosY(ModuloNPC.baldosasACoordenadas(j, i)[1]);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDificultad() {
		return dificultad;
	}

	public void setDificultad(int dificultad) {
		this.dificultad = dificultad;
	}

	public int getMovimiento() {
		return movimiento;
	}

	public void setMovimiento(int movimiento) {
		this.movimiento = movimiento;
	}

	public int getPersistencia() {
		return persistencia;
	}

	public void setPersistencia(int persistencia) {
		this.persistencia = persistencia;
	}
}
