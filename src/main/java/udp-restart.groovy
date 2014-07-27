import com.eucalyptus.bootstrap.Bootstrap
import com.eucalyptus.cloud.ws.DNSControl
import com.eucalyptus.cloud.ws.UDPHandler
import com.eucalyptus.cloud.ws.UDPListener
import com.eucalyptus.component.Faults
import com.eucalyptus.component.id.Dns
import com.eucalyptus.system.Threads
import com.eucalyptus.util.Internets
import org.apache.log4j.Logger

Logger LOG = Logger.getLogger( Faults.class )
def tp = Threads.lookup(Dns.class,UDPListener.class)

/**
 * Instrument the offending class w/ state information
 */
ExpandoMetaClass.enableGlobally()
def dealWithIt = { cb ->
  /**
   * Close old socket.
   */
  if(!DNSControl.udpListener?.socket?.isClosed()) {
    DNSControl.udpListener?.socket?.close( );
  }
  while(Bootstrap.isOperational()) {
    def socket = new DatagramSocket(53, Internets.localHostInetAddress( ))
    try {
      LOG.error( "Running a new UDPHandler for ${socket.getLocalSocketAddress( )}" );
      def fuudp = new UDPHandler((DatagramSocket)socket);
      fuudp.run();
    } catch ( InterruptedException ex ) {
      throw ex;
    } catch ( Exception ex ) {
      LOG.error( "FUUUUUDP FAILED: ${ex.getMessage()}", ex );
    } finally {
      socket?.close();
    }
  }
}
tp.submit(dealWithIt)
Thread.getAllStackTraces().findAll{ t, s ->
  t?.threadGroup?.name?.contains("UDP")
}
