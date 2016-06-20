import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class Vecino {
	private InetAddress ip;
	private InetAddress nexthop;
	private int puerto = 5512;
	private final int mask = 32;
	private int distancia;
	private long ultima_noticia;
	private boolean adyacente;

	public boolean isAdyacente() {
		return adyacente;
	}

	public Vecino(InetAddress ip, int puerto, boolean adyacente) {
		this.ip = ip;
		this.puerto = puerto;
		distancia = 16;
		this.adyacente = adyacente;
		this.ultima_noticia = GregorianCalendar.getInstance().getTimeInMillis();
	}

	public Vecino(InetAddress ip, int distancia) {
		this.ip = ip;
		this.distancia = distancia;
		this.ultima_noticia = GregorianCalendar.getInstance().getTimeInMillis();
	}

	public int getDistancia() {
		return distancia;
	}

	public InetAddress getIp() {
		return ip;
	}

	public int getPuerto() {
		return puerto;
	}

	public int getMask() {
		return mask;
	}

	public void setDistancia(int distancia) {
		this.distancia = distancia;
	}

	public long getUltima_noticia() {
		return ultima_noticia;
	}

	public void setUltima_noticia(long x) {
		this.ultima_noticia = x;
	}

	public InetAddress getNexthop() {
		return nexthop;
	}

	public void setNextHop(InetAddress x) {
		this.nexthop = x;
	}

	public void setAdyacente(boolean g) {
		this.adyacente = g;
	}

	public byte[] getMessage(InetAddress split_pois) throws IOException {
		short cero = 0;
		short idfamily = 2;
		byte[] mensaje = new byte[20];
		byte[] idfamilia = ByteBuffer.allocate(2).putShort(idfamily).array();
		byte[] routertag = ByteBuffer.allocate(2).putShort(cero).array();
		byte[] ip = this.ip.getAddress();
		byte[] mask = ByteMask();
		byte[] nexthop = ByteBuffer.allocate(4).putInt(0).array();
		byte[] distancia = ByteBuffer.allocate(4).putInt(this.distancia).array();
		ByteArrayOutputStream concatenar = new ByteArrayOutputStream();
		concatenar.write(idfamilia);
		concatenar.write(routertag);
		concatenar.write(ip);
		concatenar.write(mask);
		concatenar.write(nexthop);
		concatenar.write(distancia);
		mensaje = concatenar.toByteArray();
		concatenar.close();
		return mensaje;
	}

	public byte[] ByteMask() {
		String unos = "11111111111111111111111111111111";
		String aux = unos.substring(0, this.getMask());
		for (int i = 0; i < (32 - this.getMask()); i++) {
			aux = aux + "0";
		}
		byte[] mask = new BigInteger(aux, 2).toByteArray();
		mask = Arrays.copyOfRange(mask, 1, 5);
		return mask;
	}
}
