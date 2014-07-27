import org.apache.log4j.Logger;
import org.xbill.DNS.Name
import org.xbill.DNS.Record
import com.eucalyptus.component.Component
import com.eucalyptus.component.ComponentId
import com.eucalyptus.component.Components
import com.eucalyptus.component.Partition
import com.eucalyptus.component.Partitions
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.ServiceConfigurations
import com.eucalyptus.component.Topology
import com.eucalyptus.component.TopologyDnsResolver
import com.eucalyptus.component.Topology.ServiceKey;
import com.eucalyptus.component.TopologyDnsResolver.ResolverSupport
import com.eucalyptus.util.dns.DnsResolvers
import com.eucalyptus.util.dns.DomainNameRecords
import com.eucalyptus.util.dns.DnsResolvers.DnsResolver
import com.eucalyptus.util.dns.DnsResolvers.DnsResponse
import com.eucalyptus.util.dns.DnsResolvers.RequestType
import com.google.common.collect.Iterables
import com.google.common.collect.Lists


/**
 * A simple DnsResolver plugin which extends the TopologyDnsResolver to support aliasing of internal IP addresses to a public IP address at DNS A record resolution time.
 */
public class PublicTopologyDnsResolver extends TopologyDnsResolver {
  
  /**
   * Map from an internally used IP address to one which is routable from the outside.
   */
  def hostMap = [
    "192.168.249.19" : "173.205.188.38",
    "192.168.249.20" : "173.205.188.39",
  ]
  
  /**
   * Override the behaviour of lookupRecords() to alias IP address responses in A records based on the above hostMap.
   * 
   * @see com.eucalyptus.component.TopologyDnsResolver#lookupRecords(org.xbill.DNS.Record)
   */
  public DnsResponse lookupRecords( Record query ) {
    final Name name = query.getName( );
    if ( ResolverSupport.COMPONENT.apply( name ) ) {
      Class<? extends ComponentId> compIdType = ResolverSupport.COMPONENT_FUNCTION.apply( name );
      Component comp = Components.lookup( compIdType );
      List<ServiceConfiguration> configs = Lists.newArrayList( );
      final ComponentId componentId = comp.getComponentId( );
      if ( componentId.isPartitioned( ) ) {
        String partitionName = name.getLabelString( 1 );
        Partition partition = Partitions.lookupByName( partitionName );
        if ( componentId.isManyToOnePartition( ) ) {
          for ( ServiceConfiguration conf : Iterables.filter( Components.lookup( compIdType ).services( ),
          ServiceConfigurations.filterByPartition( partition ) ) ) {
            configs.add( conf );
          }
          Collections.shuffle( configs );
        } else {
          configs.add( Topology.lookup( compIdType, partition ) );
        }
      } else {
        if ( componentId.isManyToOnePartition( ) ) {
          for ( ServiceConfiguration conf : Components.lookup( compIdType ).services( ) ) {
            configs.add( conf );
          }
          Collections.shuffle( configs );
        } else {
          try {
            configs.add( Topology.lookup( compIdType ) );
          } catch( Exception ex ) {
            configs.addAll( Topology.enabledServices( compIdType ) );
          }
        }
      }
      List<Record> answers = Lists.newArrayList( );
      for ( ServiceConfiguration config : configs ) {
        InetAddress addr = config.getInetAddress( );
        if ( hostMap.containsKey( addr.getHostAddress( ) ) ) {
          addr = InetAddress.getByName( hostMap.get( addr.getHostAddress( ) ) );
        }
        Record aRecord = DomainNameRecords.addressRecord( name, addr );
        answers.add( aRecord );
      }
      return DnsResponse.forName( query.getName( ) )
      .answer( RequestType.AAAA.apply( query ) ? null : answers );
    }
    return super.lookupRecords( query );
  }
}

/** 
 * Replace the old TopologyDnsResolver with the new PublicTopologyDnsResolver
 */
DnsResolvers.resolvers.putInstance( TopologyDnsResolver.class, new PublicTopologyDnsResolver( ) );
DnsResolvers.resolvers.collect{ "\n${it}" }