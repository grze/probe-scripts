import net.sf.hajdbc.sql.AbstractDatabaseCluster
import net.sf.hajdbc.sql.DriverDatabaseClusterMBean
import com.eucalyptus.bootstrap.Databases
import com.eucalyptus.util.Mbeans
import com.google.common.collect.ImmutableMap

//cronExecutor
def lookupCluster = { ctx ->
  Mbeans.lookup( Databases.jdbcJmxDomain,
      ImmutableMap.builder( ).put( "cluster", ctx ).build( ),
      DriverDatabaseClusterMBean.class );
}
AbstractDatabaseCluster db = lookupCluster("eucalyptus_cloud")
db.cronExecutor