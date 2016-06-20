import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.net.DatagramPacket;

public class Rip {
	protected static Vecino NodoPropio;
	protected static HashMap<String, Subred> subredes = new HashMap<String, Subred>();
	protected static HashMap<String, Vecino> vecinos = new HashMap<String, Vecino>();

	public static void main(String[] args) throws IOException {
		InetAddress direccion = null;
		int puerto = 5512;
		if (args.length == 1) {
			String[] partes = args[0].split("\\:");
			try {
				direccion = InetAddress.getByName(partes[0]);
			} catch (UnknownHostException e) {
				System.out.println("IP Incorrecta");
				System.exit(0);
			}
			if (partes.length == 2) {
				puerto = Integer.parseInt(partes[1]);
			}
		} else {
			NetworkInterface interfaz;
			Enumeration<InetAddress> ips_eth0 = null;
			try {
				interfaz = NetworkInterface.getByName("eth0");
				ips_eth0 = interfaz.getInetAddresses();
			} catch (SocketException e) {
				System.out.println("Error al intentar conseguir la ip.");
				System.exit(0);
			}
			while (ips_eth0.hasMoreElements()) {
				direccion = ips_eth0.nextElement();////////////////////////////////////////////////////
				if (direccion instanceof Inet4Address && !direccion.isLoopbackAddress()) {
					break;
				}
				if (!ips_eth0.hasMoreElements()) {
					direccion = null;
					System.out.println("Error al intentar conseguir la ip.");
					System.exit(0);
				}
			} ///////////////////////////////////////////////////////////////
		}
		NodoPropio = new Vecino(direccion, puerto, true);
		NodoPropio.setDistancia(0);
		BufferedReader txt = null;
		try {
			txt = new BufferedReader(new FileReader("ripconf-" + direccion.getHostAddress() + ".topo"));
		} catch (FileNotFoundException e) {
			System.out.println("Error: No se ha podido abrir el txt");
			System.exit(0);
		}
		String linea = txt.readLine();
		while (linea != null) {
			String[] partes = linea.split("/");
			if (partes.length == 2) {
				subredes.put(partes[0], new Subred(InetAddress.getByName(partes[0]), Integer.parseInt(partes[1])));
			} else {
				partes = linea.split(":");
				if (partes.length == 2) {
					Vecino v = new Vecino(InetAddress.getByName(partes[0]), Integer.parseInt(partes[1]), true);
					v.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis() - 60000);
					vecinos.put(partes[0], v);
				} else {
					Vecino v = new Vecino(InetAddress.getByName(partes[0]), puerto, true);
					v.setUltima_noticia(GregorianCalendar.getInstance().getTimeInMillis() - 60000);
					vecinos.put(partes[0], v);
				}
			}
			linea = txt.readLine();
		}
		txt.close();
		HiloEnvio e = new HiloEnvio();
		e.start();
		HiloEscucha h = new HiloEscucha();
		h.start();

		while (true) {
			List<Subred> subredes_aux = new ArrayList<Subred>(subredes.values());
			List<Vecino> vecinos_aux = new ArrayList<Vecino>(vecinos.values());

			Iterator<Vecino> index = vecinos_aux.iterator();
			while (index.hasNext()) {
				ComprobarNodo(index.next());
			}
			Iterator<Subred> index2 = subredes_aux.iterator();
			while (index2.hasNext()) {
				ComprobarSubred(index2.next());
			}

			System.out.println("Destino\t\tMascara de Red\t\tG\tNextHop\t\tMetrica");

			for (Vecino n : vecinos.values()) {
				if ((n.getDistancia() == 16)
						&& (GregorianCalendar.getInstance().getTimeInMillis() - n.getUltima_noticia() >= 100000)) {
					continue;
				}
				System.out.print(n.getIp().getHostAddress() + "\t\t" + n.getMask() + "\t\t1\t");
				if (n.getNexthop() == null) {
					System.out.println("-" + "\t\t" + n.getDistancia());
				} else {
					System.out.println(n.getNexthop().getHostAddress() + "\t\t" + n.getDistancia());
				}
			}
			for (Subred s : subredes.values()) {
				int G = 1;
				System.out.print(s.getIp().getHostAddress() + "\t\t" + s.getMask() + "\t\t");
				if (s.getDistancia() == 1) {
					G = 0;
				}
				if (s.getNexthop() == null) {
					System.out.println(G + "\t-" + "\t\t" + s.getDistancia());
				} else {
					System.out.println(G + "\t" + s.getNextHop().getHostAddress() + "\t\t" + s.getDistancia());
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException x) {
				System.out.println("Hilo mal dormido");
				System.exit(0);
			}
		}
	}

	public static void ComprobarNodo(Vecino v) {
		long noticia = v.getUltima_noticia();
		/*
		 * if((v.getDistancia()==16) &&
		 * (GregorianCalendar.getInstance().getTimeInMillis()-noticia>=100000)){
		 * vecinos.remove(v.getIp().getHostAddress()); }
		 */
		if (GregorianCalendar.getInstance().getTimeInMillis() - noticia >= 60000) {
			v.setDistancia(16);
		}
		return;
	}

	public static void ComprobarSubred(Subred x) {
		long noticia = x.getUltima_noticia();

		if (x.getDistancia() == 1)
			return;

		if ((x.getDistancia() == 16) && (GregorianCalendar.getInstance().getTimeInMillis() - noticia >= 100000)) {
			subredes.remove(x.getIp().getHostAddress());
		} else if (vecinos.get(x.getNexthop().getHostAddress()).getDistancia() >= 16) {
			x.setDistancia(16);
			x.setUltima_noticia(vecinos.get(x.getNexthop().getHostAddress()).getUltima_noticia());
		}
		return;
	}
}
