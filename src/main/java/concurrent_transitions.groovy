import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import com.eucalyptus.cluster.Cluster
import com.eucalyptus.cluster.ClusterBuilder
import com.eucalyptus.component.ServiceBuilders
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.ClusterController

/**
 * Lock to use for contriving a concurrent transition.
 */
Lock lock = new ReentrantLock( true );
/**
 * Create a re-defined {@link ClusterBuilder} which holds a special lock for the purposes of testing.
 * @see groovy.lang.Script#run()
 */
ExpandoMetaClass.enableGlobally( )
def builder = ServiceBuilders.componentBuilders.get( ClusterController.class );
builder.metaClass.fireDisable = {
  
}
def oldFireDisable = ClusterBuilder.metaClass.fireDisable;
def oldFireEnable = ClusterBuilder.metaClass.fireEnable;
def oldFireCheck = ClusterBuilder.metaClass.fireCheck;
ClusterBuilder.metaClass.fireDisable = { ServiceConfiguration config ->
  
}

/**
 * This script syntheticly incudes a concurrent state transition.  
 * 
 * The important transitions which are effected by concurrency are those which modify the ENABLED services set {@link Topology#services}.
 * 1. ENABLED->ENABLED is ongoing and call Toplogy.check( config ) ==> config \in services and state is ENABLED
 * 2. DISABLED->ENABLED is ongoing and call Toplogy.check( config ) ==> config \in services and state is ENABLED
 * 2. DISABLED->ENABLED is ongoing ==> config \in services and Topology.enable( config ) should fail an leave
 */
// Get the {@link Cluster} and it's {@link ServiceConfiguration}
Cluster cluster = Topology.enabledServices( ClusterController.class ).iterator( ).next( );

/**
 * Make sure the {@link Cluster} is ENABLED
 */
Topology.enable( cluster.getConfiguration( ) ).get( );

/**
 * Acquire the {@link Cluster#gateLock} which needs to be acquired in 
 */
cluster.@gateLock.lock( );
try {
  def topologyCheckThread = { ServiceConfiguration config ->
    Topology.enable( config ).get( );
  } as Runnable;
  Topology.enable( cluster.getConfiguration( ) );
} finally {
  cluster.@gateLock.unlock( );
}