import com.eucalyptus.component.Component
import com.eucalyptus.component.Components
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.Walrus
import com.eucalyptus.scripting.Groovyness

ServiceConfiguration serviceConfig =  Groovyness.expandoMetaClass( Components.lookup( Walrus.class ).getLocalServiceConfiguration( ) );

ServiceConfiguration topoConfig = Topology.instance.services.values().collect{
  Groovyness.expandoMetaClass(it)
}.find{
  it.componentId.name == "walrus"
}

if ( Component.State.ENABLED.equals( serviceConfig ) && topoConfig != null ) {
  return "\n${serviceConfig}\n"
} else {
  return "\n${topoConfig}\n"
}