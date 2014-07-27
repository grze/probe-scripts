import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import com.eucalyptus.bootstrap.Databases
import com.eucalyptus.bootstrap.Host
import com.eucalyptus.bootstrap.Hosts
import com.eucalyptus.component.ServiceUris
import com.eucalyptus.component.id.Eucalyptus.Database
import com.google.common.base.Predicate
import com.google.common.collect.Iterables
import com.google.common.collect.Lists

List<String> dbNames = Lists.newArrayList();
for ( final Host h : Hosts.listActiveDatabases( ) ) {
  final String url = String.format( "jdbc:%s", ServiceUris.remote( Database.class, h.getBindAddress( ), "postgres" ) );
  try {
    final Connection conn = DriverManager.getConnection( url, Databases.getUserName( ), Databases.getPassword( ) );
    try {
      final PreparedStatement statement = conn.prepareStatement( "select datname from pg_database" );
      final ResultSet result = statement.executeQuery( );
      
      while ( result.next( ) ) {
        dbNames.add(result.getString("datname"));
      }
    } finally {
      conn.close( );
    }
  } catch ( final Exception ex ) {
    LOG.error( ex, ex );
  }
}

final List<String> finalDbNames = Lists.newArrayList();

Iterables.removeIf(dbNames, new Predicate<String>(){
      @Override
      public boolean apply(final String input) {
        if (input.startsWith("eucalyptus_") || input.equals("database_events")) {
          finalDbNames.add(input);
          return true;
        } else {
          return false;
        }
      }
    });


return finalDbNames;

