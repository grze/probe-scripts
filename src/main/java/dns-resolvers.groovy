import org.xbill.DNS.Cache
import org.xbill.DNS.Credibility
import org.xbill.DNS.Lookup
import org.xbill.DNS.Name
import org.xbill.DNS.RRset
import org.xbill.DNS.Record
import org.xbill.DNS.Type

Cache cache = new Cache( );
def subdomains = { Name name ->
  def list = [name];
  def sub = name.toString( ).replaceAll("\\A[^\\.]+\\.","");
  println "${name} ${sub}"
  if ( sub.equals( "" ) || sub.equals( "." ) ) {
    list << Name.fromConstantString(".");
  } else {
    list.addAll(call(Name.fromString(sub)))
  }
  return list;
}
def nsRecords = { subs ->
  subs.collect{
    Lookup aLookup = new Lookup( it, Type.NS );
    aLookup.setCache( cache );
    aLookup.run( ).collect{ it };
  }.findAll{ !it.isEmpty() }.first()
}
def lookup = { Name name ->
  Lookup aLookup = new Lookup( name, Type.A );
  aLookup.setCache( cache );
  def lookupResults = aLookup.run( );
  lookupResults.collect{ Record r -> r.getName( ) }
  aLookup
}
[
  "google.com.",
  "www.facebook.com.",
  "repo.jenkins-ci.org."
].collect{ Name.fromConstantString( it ) }.collect{ Name name ->
  def dnsResponse = [ 'answers':[] as Set, 'authority':[] as Set, 'additional':[] as Set]
  def aLookup = lookup(name);
  def arrs = aLookup.getAnswers();
  def cnamerrs = aLookup.getAliases( ).length > 0 ? aLookup.getAliases() as List : []
  cnamerrs.each{ alias ->
    def answers = cache.lookupRecords( alias, Type.CNAME, Credibility.ANY )?.answers( );
    def records = []
    def rrs = answers.each{ RRset rrset ->
      rrset.rrs().each{ dnsResponse['answers'] += it }
    }
  }
  arrs.each{ n ->
    def answers = cache.lookupRecords( n.getName(), Type.A, Credibility.ANY )?.answers( );
    def records = []
    def rrs = answers.each{ RRset rrset ->
      rrset.rrs().each{ dnsResponse['answers'] += it }
    }
  }
  if ( !cnamerrs.isEmpty() ) {
    cnamerrs.each{ n ->
      println n
      def ns = nsRecords(subdomains(Name.fromConstantString(n.toString( ))));
      dnsResponse['authority'].addAll(ns)
      ns.each{dnsResponse['additional'].addAll(lookup(it.getTarget()).getAnswers().collect{it})}
    }
  } else {
    arrs.each{ n ->
      println n
      def ns = nsRecords(subdomains(Name.fromConstantString(n.getName().toString( ))));
      dnsResponse['authority'].addAll(ns)
      ns.each{dnsResponse['additional'].addAll(lookup(it.getTarget()).getAnswers().collect{it})}
    }
  }
  dnsResponse.each{ k,v->println "${k} =>" + v.join("\n${k} =>")}
}