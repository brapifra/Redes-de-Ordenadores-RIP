import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;
import java.util.Random;

public class HiloEnvio extends Thread {
	private DatagramSocket socket;
	private Integer comando_version = new Integer(2);

	public HiloEnvio() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("Error: Socket");
			System.exit(0);
		}
	}

	public void run() {
		while (true) {
			byte[] mensajefinal;
			byte[] ceros = new byte[2];
			for (Vecino v : Rip.vecinos.values()) {
				if (!v.isAdyacente())
					continue;
				if ((v.getDistancia() == 16)
						&& (GregorianCalendar.getInstance().getTimeInMillis() - v.getUltima_noticia() >= 100000)) {
					continue;
				}
				ByteArrayOutputStream concatenar = new ByteArrayOutputStream();
				try {
					concatenar.write(comando_version.byteValue());
					concatenar.write(comando_version.byteValue());
					concatenar.write(ceros);
					concatenar.write(Rip.NodoPropio.getMessage(v.getIp()));
					for (Vecino nodo : Rip.vecinos.values()) {
						concatenar.write(nodo.getMessage(v.getIp()));
					}
					for (Subred s : Rip.subredes.values()) {
						concatenar.write(s.getMessage(v.getIp()));
					}
					concatenar.close();
				} catch (IOException e) {
					System.out.println("Error: Concantenado");
					System.exit(0);
				}
				mensajefinal = concatenar.toByteArray();
				DatagramPacket paquete = new DatagramPacket(mensajefinal, mensajefinal.length, v.getIp(),
						v.getPuerto());
				try {
					socket.send(paquete);
				} catch (IOException e) {
					System.out.println(v.getIp().getHostAddress() + " " + v.getPuerto() + " " + v.getUltima_noticia()
							+ " " + v.getDistancia());
					System.out.println("Error: Envío datagrama");
				}
			}
			try {
				double h = new Random().nextGaussian();
				h += 10;
				h *= 1000;
				sleep((long) h);
			} catch (InterruptedException e) {
				System.out.println("Error: Hilo mal dormido");
				System.exit(0);
			}
		}
	}
}
