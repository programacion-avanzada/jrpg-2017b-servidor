package testsServidor;

import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;
import servidor.Conector;
import servidor.Servidor;

public class TestConector {

	private String getRandomString() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 8) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

	@Test
	public void testConexionConLaDB() {
		new Servidor();
		Servidor.main(null);

		Conector conector = new Conector();
		conector.connect();

		// Pasado este punto la conexi�n con la base de datos result� exitosa

		Assert.assertEquals(1, 1);

		conector.close();
	}

	@Test
	public void testRegistrarUsuario() {
		new Servidor();
		Servidor.main(null);

		Conector conector = new Conector();
		conector.connect();

		PaqueteUsuario pu = new PaqueteUsuario();
		pu.setUsername("UserTest");
		pu.setPassword("test");

		conector.registrarUsuario(pu);

		pu = conector.getUsuario("UserTest");

		Assert.assertEquals("UserTest", pu.getUsername());

		conector.close();
	}

	@Test
	public void testRegistrarPersonaje() throws IOException {
		new Servidor();
		Servidor.main(null);

		Conector conector = new Conector();
		conector.connect();
		String randomero = getRandomString();
		PaquetePersonaje pp = new PaquetePersonaje();
		pp.setCasta("Humano");
		pp.setDestreza(1);
		pp.setEnergiaTope(1);
		pp.setExperiencia(1);
		pp.setFuerza(1);
		pp.setInteligencia(1);
		pp.setNivel(1);
		pp.setNombre(randomero);
		pp.setRaza("Asesino");
		pp.setSaludTope(1);

		PaqueteUsuario pu = new PaqueteUsuario();
		pu.setUsername(getRandomString());
		pu.setPassword("test");

		conector.registrarUsuario(pu);
		conector.registrarPersonaje(pp, pu);

		pp = conector.getPersonaje(pu);

		Assert.assertEquals(randomero, pp.getNombre());
		conector.close();
	}

	@Test
	public void testLoginUsuario() {
		new Servidor();
		Servidor.main(null);

		Conector conector = new Conector();
		conector.connect();

		PaqueteUsuario pu = new PaqueteUsuario();
		pu.setUsername("UserTest");
		pu.setPassword("test");

		conector.registrarUsuario(pu);

		boolean resultadoLogin = conector.loguearUsuario(pu);

		Assert.assertEquals(true, resultadoLogin);

		conector.close();
	}

	@Test
	public void testLoginUsuarioFallido() {
		new Servidor();
		Servidor.main(null);

		Conector conector = new Conector();
		conector.connect();

		PaqueteUsuario pu = new PaqueteUsuario();
		pu.setUsername("userInventado");
		pu.setPassword("test");

		boolean resultadoLogin = conector.loguearUsuario(pu);

		Assert.assertEquals(false, resultadoLogin);

		conector.close();
	}

}
