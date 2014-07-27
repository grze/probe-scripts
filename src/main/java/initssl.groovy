import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import com.eucalyptus.component.auth.SystemCredentials
import com.eucalyptus.component.id.Eucalyptus.Database
import com.eucalyptus.crypto.util.PEMFiles


if ( Security.getProvider( BouncyCastleProvider.PROVIDER_NAME ) == null ) {
  if ( Security.getProviders().length > 4 ) {
    Security.insertProviderAt( new BouncyCastleProvider( ), 4 ); // EUCA-5833
  } else {
    Security.addProvider( new BouncyCastleProvider( ) );
  }
}

def setOwnerReadonly = { final File file ->
  file.setReadable false, false
  file.setReadable true
  file.setWritable false, false
  file.setWritable false
  file.setExecutable false, false
  file.setExecutable false
}

//ServiceJarDiscovery.doSingleDiscovery(  new ComponentDiscovery( ) );
SystemCredentials.Credentials dbCredentials = SystemCredentials.create( new Database() );
PEMFiles.write( new File("/var/lib/eucalyptus/db/data/server.crt").getAbsolutePath(), dbCredentials.certificate )
PEMFiles.write( new File("/var/lib/eucalyptus/db/data/server.key").getAbsolutePath(), dbCredentials.keyPair )
