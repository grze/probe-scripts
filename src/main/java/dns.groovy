import org.xbill.DNS.Cache
import org.xbill.DNS.Credibility
import org.xbill.DNS.Lookup
import org.xbill.DNS.NSRecord
import org.xbill.DNS.Name
import org.xbill.DNS.RRset
import org.xbill.DNS.Record
import org.xbill.DNS.Type
import org.xbill.DNS.SetResponse
import com.eucalyptus.util.dns.DnsResolvers;

def internal = Name.fromString( "eucalyptus.internal" )
def external = Name.fromString( "eucalyptus.localhost" )
def internal2 = Name.fromString( "eucalyptus.internal." )
def external2 = Name.fromString( "eucalyptus.localhost." )
def test1 = Name.fromString( "euca-192-168-40-53.eucalyptus.localhost" );
def test2 = Name.fromString( "euca-192-168-40-53.eucalyptus.localhost." );
def test3 = Name.fromString( "euca-192-168-40-53.eucalyptus.internal" );
def test4 = Name.fromString( "euca-192-168-40-53.eucalyptus.internal." );

DnsResolvers.resolversFor( queryRecord, source );
[test1, test2, test3, test4].each{ test ->
  [
    internal,
    external,
    internal2,
    external2
  ].each { domain ->    
    if ( test.subdomain( domain ) )  {
      def rel = test.relativize( domain )
      def pattern = "euca-(.+{3})-(.+{3})-(.+{3})-(.+{3})"
      def replacement = "\$1.\$2.\$3.\$4"
      println """${rel} ${rel.toString().matches(pattern)?rel.toString().replaceAll( pattern, replacement ): "fail"}  """
    }
//    println """
//${test}:${test.isAbsolute( )} ${domain}:${domain.isAbsolute( )} => ${test.subdomain( domain )} ${test.relativize( domain )}"""   
  }
}

Cache cache = new Cache();
def lookerUpper = { String dnsName, int type, int cred, Cache c ->
  SetResponse sr = null;
  Name name = Name.fromString( dnsName.endsWith(".")?dnsName:"${dnsName}." );
  def res = c.lookupRecords( name, type, Credibility.ANY );
  if ( res != null && res.isSuccessful( ) ) {
    return res;
  } else {
    Lookup lookup = new Lookup(name,Type.NS);
    lookup.setCache( c );
    Record[] nsRecords = lookup.run( );
    if ( nsRecords != null ) {
      for ( Record rns : nsRecords ) {
        NSRecord ns = (NSRecord)rns;
        Record[] queryRecords = null;
      }
      aLookup = new Lookup(name,type)
      aLookup.setCache( c )
      queryRecords = aLookup.run( );
      if ( queryRecords != null ) {
        sr = new SetResponse( SetResponse.SUCCESSFUL );
        for( Record ra : queryRecords ) {
          sr.addRRset( new RRset( ra ) );
        }
        for( Record nsr : nsRecords ) {
          sr.addRRset( new RRset( nsr ) );
        }
      }
      if ( sr != null && sr.isSuccessful() ) {
        sr?.answers( ).each{ answer -> 
            println "lookup-answer: ${answer}"
            answer.rrs( ).each{ record -> println "lookup-record: ${record.rdataToString()}" }  
          }
      }
    }
    return c.lookupRecords( name, type, cred )
  }
};

//lookerUpper(Name.fromString("google.com."),Type.A,Credibility.ANY,cache)?.answers().each{ answer -> println "cached-answer: ${answer}" }
lookerUpper("google.com",Type.A,Credibility.ANY,cache)?.answers().each{ answer -> println "cached-answer: ${answer}" }
//lookerUpper("ns1.google.com",Type.A,Credibility.ANY,cache)?.answers().each{ answer -> println "cached-answer: ${answer}" }
//lookerUpper("google.com",Type.MX,Credibility.ANY,cache)?.answers().each{ answer -> println "cached-answer: ${answer}" }
//
//cache.lookupRecords( name, Type.NS, Credibility.ANY )?.answers().each{ answer -> println "cached-answer: ${answer}" }
//cache.lookupRecords( name, Type.A, Credibility.ANY )?.answers().each{ answer -> println "cached-answer: ${answer}" }
//cache.lookupRecords( name, Type.AAAA, Credibility.ANY )?.answers().each{ answer -> println "cached-answer: ${answer}" }
