import com.eucalyptus.component.Topology;
import com.eucalyptus.scripting.Groovyness;
System.getProperties( ).remove( "euca.noha.cloud" )
Topology.TopologyTimer.busy.set( true )
//try {
  Topology.RunChecks.INSTANCE.call( ).collect{ Groovyness.expandoMetaClass( it ) }.findAll{ it.componentId.name == "walrus" } 
//} finally {
//  Topology.TopologyTimer.busy.set( false )
//}
