import com.eucalyptus.scripting.Groovyness
import com.eucalyptus.vm.VmInstance
import com.eucalyptus.vm.VmInstances

"BEGIN\n" + VmInstances.list( ).collect{ Groovyness.expandoMetaClass( it ) }.collect{ VmInstance vm -> "${vm.getInstanceId( )} ${URI.create( vm.getServiceTag( ) ).getHost( )}" }.join( "\n" ) + "\nEND"