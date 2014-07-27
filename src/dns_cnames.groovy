import org.apache.log4j.Logger
import org.xbill.DNS.ARecord
import org.xbill.DNS.Cache
import org.xbill.DNS.Credibility
import org.xbill.DNS.DClass
import org.xbill.DNS.Lookup
import org.xbill.DNS.Message
import org.xbill.DNS.NSRecord
import org.xbill.DNS.Name
import org.xbill.DNS.RRset
import org.xbill.DNS.Record
import org.xbill.DNS.SetResponse
import org.xbill.DNS.Type
import com.eucalyptus.dns.resolvers.RecursiveDnsResolver
import com.eucalyptus.util.dns.DnsResolvers
import com.eucalyptus.util.dns.DnsResolvers.DnsResponse
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists

Logger LOG = Logger.getLogger( DnsResolvers.class );


def name = Name.fromConstantString( "mail.eucalyptus.com." );
def query = new ARecord( name, DClass.IN, 60, InetAddress.getLoopbackAddress( ) )
def type = query.getType( );
def response = new Message();
def source = InetAddress.getByName( "10.211.39.46" )
response.newQuery( query );
final Cache cache = new Cache( );
Lookup aLookup = new Lookup( name, type );
aLookup.setCache( cache );
Record[] found = aLookup.run( );
List<Record> queriedrrs = Arrays.asList( found != null
    ? found : [] as Record[] );
List<Name> cnames = ( List<Name> ) ( aLookup.getAliases( ).length > 0
    ? Arrays.asList( aLookup.getAliases( ) ) : Lists.newArrayList( ) );
List<Record> answer = Lists.newArrayList( );
List<Record> authority = Lists.newArrayList( );
List<Record> additional = Lists.newArrayList( );
for ( Name cnameRec : cnames ) {
  SetResponse sr = cache.lookupRecords( cnameRec, Type.CNAME, Credibility.ANY );
  if ( sr != null && sr.isSuccessful( ) && sr.answers( ) != null ) {
    for ( RRset result : sr.answers( ) ) {
      Iterator rrs = result.rrs( false );
      if ( rrs != null ) {
        for ( Object record : ImmutableSet.copyOf( rrs ) ) {
          answer.add( ( Record ) record );
        }
      }
    }
  }
}
return answer
for ( Record queriedRec : queriedrrs ) {
  SetResponse sr = cache.lookupRecords( queriedRec.getName( ),
      queriedRec.getType( ),
      Credibility.ANY );
  if ( sr != null && sr.isSuccessful( ) && sr.answers( ) != null ) {
    for ( RRset result : sr.answers( ) ) {
      Iterator rrs = result.rrs( false );
      if ( rrs != null ) {
        for ( Object record : ImmutableSet.copyOf( rrs ) ) {
          println record
          answer.add( ( Record ) record );
        }
      }
    }
  }
}
if ( !cnames.isEmpty( ) ) {
  for ( Record aRec : queriedrrs ) {
    List<Record> nsRecs = RecursiveDnsResolver.lookupNSRecords( aRec.getName( ), cache );
    for ( Record nsRec : nsRecs ) {
      authority.add( nsRec );
      Lookup nsLookup = new Lookup( ( ( NSRecord ) nsRec ).getTarget( ), Type.A );
      nsLookup.setCache( cache );
      Record[] nsAnswers = nsLookup.run( );
      if ( nsAnswers != null ) {
        additional.addAll( Arrays.asList( nsAnswers ) );
      }
    }
  }
}
return DnsResponse.forName( name )
    .recursive( )
    .withAuthority( authority )
    .withAdditional( additional )
    .answer( answer ).dump();
