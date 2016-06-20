import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.GregorianCalendar;

public class HiloEscucha extends Thread {
	private DatagramSocket socket;

	public HiloEscucha() {
		try {
			socket = new DatagramSocket(Rip.NodoPropio.getPuerto());
		} catch (SocketException e) {
			System.out.println("Error: Socket puerto");
			System.exit(0);
		}
	}

	public void run() {
		byte[] buffer = new byte[4 + 25 * 20];
		DatagramPacket paquete = new DatagramPacket(buffer, 4 + 25 * 20);
		while (true) {
			try {
				socket.receive(paquete);
			} catch (IOException e) {
				System.out.println("Error: Socket Escucha.");
				System.exit(0);
			}
			ByteArrayInputStream leer = new ByteArrayInputStream(paquete.getData());
			int num = (paquete.getLength() - 4) / 20;
			try {
				byte[] cabecera = new byte[4];// Bytes comunes cabecera RIP
				leer.read(cabecera);
				for (int i = 0; i < num; i++) {
					leer = LeerPaquete(leer, paquete);

				}
			} catch (IOException e) {
				System.out.println("Error: lectura.");
				System.exit(0);
			}
		}
	}

	public static ByteArrayInputStream LeerPaquete(ByteArrayInputStream leer, DatagramPacket paquete)
			throws IOException {
		byte[] family_route = new byte[4];
		byte[] IPaddress = new byte[4];
		byte[] mask = new byte[4];
		byte[] n_hop = new byte[4];
		byte[] distancia = new byte[4];
		int mascara;
		int m = 0;
		InetAddress nexthop = paquete.getAddress();// ip del remitente del
													// paquete
		leer.read(family_route);
		leer.read(IPaddress);
		InetAddress ip = InetAddress.getByAddress(IPaddress);// ip del nodo a
																// meter
		leer.read(mask);
		leer.read(n_hop);
		leer.read(distancia);
		mascara = ByteBuffer.wrap(mask).getInt();
		while ((mascara & 1) != 1) { // Bucle hallar mascara
			mascara = mascara >> 1;
			m++;
		}
		// //comprobado hasta aqu�
		if (ip.equals(Rip.NodoPropio.getIp())) {
			return leer;
		}
		if (m == 0) {
			if (Rip.vecinos.containsKey(ip.getHostAddress())) {
				for (Vecino n : Rip.vecinos.values()) {
					if (ip.equals(n.getIp())) {
						if (n.isAdyacente() && n.getIp().equals(nexthop)) {
							n.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis());
						} else if (!n.isAdyacente() && (n.getDistancia() < 16)) {
							n.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis());
						}
						if (nexthop.equals(n.getNexthop())) {
							n.setDistancia(ByteBuffer.wrap(distancia).getInt() + 1);
							if (n.getDistancia() > 16) {// ///Para no tener
														// metricas de 17 o
														// superior
								n.setDistancia(16);
							}
						} else if ((ByteBuffer.wrap(distancia).getInt() + 1) < n.getDistancia()) {
							n.setDistancia(ByteBuffer.wrap(distancia).getInt() + 1);
							if (n.getDistancia() > 16) {// ///Para no tener
														// metricas de 17 o
														// superior
								n.setDistancia(16);
							}
							n.setNextHop(nexthop);
						}
					}
				}
			} else {
				Vecino Nuevonodo = new Vecino(ip, ByteBuffer.wrap(distancia).getInt() + 1);
				Nuevonodo.setNextHop(nexthop);
				if (Nuevonodo.getDistancia() > 16) {// ///Para no tener metricas
													// de 17 o superior
					Nuevonodo.setDistancia(16);
				}
				Nuevonodo.setAdyacente(false);
				if (Nuevonodo.getDistancia() == 1) {
					Nuevonodo.setAdyacente(true);
					if (Nuevonodo.getIp().equals(nexthop)) {
						Nuevonodo.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis());
						Rip.vecinos.put(Nuevonodo.getIp().getHostAddress(), Nuevonodo);
					}
				}
				if (!Nuevonodo.isAdyacente() && Nuevonodo.getDistancia() < 16) {
					Nuevonodo.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis());
					Rip.vecinos.put(Nuevonodo.getIp().getHostAddress(), Nuevonodo);
				}
			}
		} else {
			if (Rip.subredes.containsKey(ip.getHostAddress())) {
				for (Subred s : Rip.subredes.values()) {
					if (ip.equals(s.getIp())) {
						if (nexthop.equals(s.getNextHop())) {
							s.setDistancia(ByteBuffer.wrap(distancia).getInt() + 1);
							if (s.getDistancia() > 16) {
								s.setDistancia(16);
							}
						} else if (ByteBuffer.wrap(distancia).getInt() + 1 < s.getDistancia()) {
							s.setDistancia(ByteBuffer.wrap(distancia).getInt() + 1);
							s.setNextHop(nexthop);
						}
					}
				}
			} else {
				Subred nuevared = new Subred(ip, 32 - m);
				nuevared.setNextHop(nexthop);
				nuevared.setDistancia(ByteBuffer.wrap(distancia).getInt() + 1);
				if (nuevared.getDistancia() > 16) {
					nuevared.setDistancia(16);
				}
				nuevared.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis());
				Rip.subredes.put(nuevared.getIp().getHostAddress(), nuevared);
			}
		}
		return leer;
	}

}
