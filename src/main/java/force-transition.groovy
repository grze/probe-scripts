import com.eucalyptus.component.Component
import com.eucalyptus.component.Components
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.Walrus
import com.eucalyptus.system.Threads



/**
 * Look up the register Walrus
 */
def config = Components.lookup( Walrus.class ).services().first()

/**
 * Check to see if it is in the Topology services, if so and it is ENABLED throw an error
 */
def checkForBug = {
  if(Component.State.ENABLED.apply(config)&&!Topology.isEnabled( Walrus.class )) {
    def enabledInTopology = Topology.getInstance().services.findAll{ k,v -> k.componentId == config.componentId}
    throw new RuntimeException("Failed to find ENABLED Walrus in service topology: ${config} ${enabledInTopology}")
  }
}
checkForBug()//call it once to see if it has triggered already

/**
 * If it's ENABLED, disable() it to start.
 */
if(Component.State.ENABLED.apply(config)) {
  Topology.disable( config ).get( )
}
/**
 * Create a place to store results
 */
def result = [:]
/**
 * Create a runnable which will try to ENABLE the Walrus
 */
def enableRunnable = {
  Topology.enable( config ).get( )
}
def checkRunnable = {
  10.times{ Topology.Transitions.CHECK.apply(config) }
}
def runnables = [
  enableRunnable,
  checkRunnable] as List
/**
 * start both runnables and wait for them to complete... 100 times.
 * a trail fails if the service says it is ENABLED but the Topology.services map doesn't have an entry.
 * if any of the trails fails an exception will be thrown. 
 */
10.times{i->
  runnables.collect{ runnable ->
    Threads.lookup(config).submit( runnable )
  }.collect{ future ->
    try {
      future.get()
    } catch ( Exception ex ) {
      result[i]=ex
    }
  }
  checkForBug()
}
result.collect{k,v->"\n${k}=>${v}"}
