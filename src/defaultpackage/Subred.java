import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.GregorianCalendar;

public class Subred {
	private InetAddress ip;
	private int mask;
	private InetAddress nexthop;/**/
	private int distancia = 1;/**/
	private long ultima_noticia;/**/

	public Subred(InetAddress ip, int mask) {
		this.ip = ip;
		this.mask = mask;
		this.ultima_noticia = GregorianCalendar.getInstance().getTimeInMillis();
	}

	public InetAddress getIp() {
		return ip;
	}

	public int getMask() {
		return mask;
	}

	public InetAddress getNextHop() {
		return nexthop;
	}

	public int getDistancia() {
		return distancia;
	}

	public InetAddress getNexthop() {
		return nexthop;
	}

	public void setNextHop(InetAddress salto) {
		this.nexthop = salto;
	}

	public void setDistancia(int d) {
		this.distancia = d;
	}

	public long getUltima_noticia() {
		return ultima_noticia;
	}

	public void setUltima_noticia(long x) {
		this.ultima_noticia = x;
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
		for (int i = 0; i < 32 - this.getMask(); i++) {
			aux = aux + "0";
		}
		byte[] mask = new BigInteger(aux, 2).toByteArray();
		mask = Arrays.copyOfRange(mask, 1, 5);
		return mask;
	}
}
