import javax.persistence.EntityTransaction
import org.apache.log4j.Logger
import com.eucalyptus.auth.principal.UserFullName
import com.eucalyptus.cluster.Clusters
import com.eucalyptus.cluster.callback.VmStateCallback
import com.eucalyptus.component.Partition
import com.eucalyptus.entities.Entities
import com.eucalyptus.images.Emis.BootableSet
import com.eucalyptus.keys.SshKeyPair
import com.eucalyptus.network.NetworkGroup
import com.eucalyptus.network.PrivateNetworkIndex
import com.eucalyptus.scripting.Groovyness
import com.eucalyptus.util.async.AsyncRequests
import com.eucalyptus.vm.VmInstance
import com.eucalyptus.vm.VmInstance.RestoreAllocation
import com.eucalyptus.vmtypes.VmType
import com.eucalyptus.vmtypes.VmTypes
import com.google.common.collect.Sets
import edu.ucsb.eucalyptus.cloud.VmDescribeResponseType
import edu.ucsb.eucalyptus.cloud.VmDescribeType
import edu.ucsb.eucalyptus.cloud.VmInfo
Logger LOG = Logger.getLogger( "instance - restore - check.class" );
Clusters.getInstance().listValues().collect{
  def VmDescribeResponseType reply = AsyncRequests.sendSync(
      it.getConfiguration(),
      new VmDescribeType())
  def VmStateCallback callback = new VmStateCallback()
  callback.setSubject(it)
  def ccConfiguration = Groovyness.expandoMetaClass( callback.getSubject( ).getConfiguration( ) )
  reply.setOriginCluster( ccConfiguration.getName( ) );
  final Set<String> reportedInstances = Sets.newHashSet( );
  for ( VmInfo vmInfo : reply.getVms( ) ) {
    reportedInstances.add( vmInfo.getInstanceId( ) );
    vmInfo.setPlacement( ccConfiguration.getName( ) );
    def typeInfo = vmInfo.getInstanceType( );
    if ( typeInfo.getName( ) == null || "".equals( typeInfo.getName( ) ) ) {
      VmTypes.list( ).collect{ Groovyness.expandoMetaClass(it) }.each { VmType t ->
        if ( t.getCpu( ).equals( typeInfo.getCores( ) ) && t.getDisk( ).equals( typeInfo.getDisk( ) ) && t.getMemory( ).equals( typeInfo.getMemory( ) ) ) {
          typeInfo.setName( t.getName( ) );
        }
      }
    }
  }
  
  final Set<String> restoreInstances = Sets.newHashSet( Sets.difference( reportedInstances, callback.@initialInstances.get( ) ) );
  reply.getVms().collect{ VmInfo input ->
    if( restoreInstances.contains( input.getInstanceId( ) ) ) {
      final UserFullName userFullName = UserFullName.getInstance( input.getOwnerId( ) );
      final EntityTransaction db = Entities.get( VmInstance.class );
      boolean building = false;
      try {
        final List<NetworkGroup> networks = Groovyness.expandoMetaClass( RestoreAllocation.restoreNetworks( input, userFullName ) );
        final PrivateNetworkIndex index = Groovyness.expandoMetaClass( RestoreAllocation.restoreNetworkIndex( input, networks ) );
        final VmType vmType = Groovyness.expandoMetaClass( RestoreAllocation.restoreVmType( input ) );
        final Partition partition = Groovyness.expandoMetaClass( RestoreAllocation.restorePartition( input ) );
        final String imageId = RestoreAllocation.restoreImage( input );
        final String kernelId = RestoreAllocation.restoreKernel( input );
        final String ramdiskId = RestoreAllocation.restoreRamdisk( input );
        final BootableSet bootSet = Groovyness.expandoMetaClass( RestoreAllocation.restoreBootSet( input, imageId, kernelId, ramdiskId ) );
        final int launchIndex = RestoreAllocation.restoreLaunchIndex( input );
        final SshKeyPair keyPair = Groovyness.expandoMetaClass( RestoreAllocation.restoreSshKeyPair( input, userFullName ) );
        final byte[] userData = RestoreAllocation.restoreUserData( input );
        building = true;
        final VmInstance vmInst = Groovyness.expandoMetaClass( new VmInstance.Builder( ).owner( userFullName )
                                            .withIds( input.getInstanceId( ),
                                                      input.getReservationId( ),
                                                      null,
                                                      null,
                                                      null)
                                            .bootRecord( bootSet,
                                                         userData,
                                                         keyPair,
                                                         vmType,
                                                         Boolean.FALSE)
                                            .placement( partition, partition.getName( ) )
                                            .networking( networks, index )
                                            .build( launchIndex ) );
        vmInst.setNaturalId( input.getUuid( ) );
        RestoreAllocation.restoreAddress( input, vmInst );
        Entities.persist( vmInst );
        db.commit( );
        vmInst.getInstanceId();
      } catch ( final Exception ex ) {
        LOG.error( "Failed to restore instance " + input.getInstanceId( ) + " because of: " + ex.getMessage( ), building ? null : ex );
        LOG.info(ex,ex)
        ex;
      } finally {
        if ( db.isActive() ) db.rollback();
      }
    }
  }
}