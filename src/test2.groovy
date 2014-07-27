import com.eucalyptus.component.Components
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.Storage
import com.eucalyptus.scripting.Groovyness

Components.lookup(Storage.class).services().collect { Groovyness.expandoMetaClass(it) }.collect { ServiceConfiguration config ->
  Topology.disable(config);
}