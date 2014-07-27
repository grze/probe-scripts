import com.eucalyptus.component.Component
import com.eucalyptus.component.Components
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.Storage
import com.eucalyptus.scripting.Groovyness

ServiceConfiguration serviceConfig =  Groovyness.expandoMetaClass( Components.lookup( Storage.class ).getLocalServiceConfiguration( ) );

ServiceConfiguration topoConfig = Topology.instance.services.values().collect{
  Groovyness.expandoMetaClass(it)
}.find{
  it.componentId.name == "storage"
}

if ( Component.State.ENABLED.equals( serviceConfig ) && topoConfig != null ) {
  return "\n${serviceConfig}\n"
} else {
  return "\n${topoConfig}\n"
}