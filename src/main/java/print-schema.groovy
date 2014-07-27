import org.hibernate.ejb.Ejb3Configuration
import org.hibernate.tool.hbm2ddl.SchemaUpdate
import com.eucalyptus.bootstrap.ServiceJarDiscovery
import com.eucalyptus.component.ComponentDiscovery
import com.eucalyptus.component.Components
import com.eucalyptus.component.ServiceUris
import com.eucalyptus.component.ServiceBuilders.ServiceBuilderDiscovery
import com.eucalyptus.component.id.Eucalyptus.Database
import com.eucalyptus.entities.PersistenceContextDiscovery
import com.eucalyptus.entities.PersistenceContexts
import com.google.common.collect.ImmutableMap

[
  new ComponentDiscovery( ),
  new ServiceBuilderDiscovery( ),
  new PersistenceContextDiscovery( )
].each{
  ServiceJarDiscovery.runDiscovery( it );
}
final Map<String, String> props = ImmutableMap.builder( )
    .put( "hibernate.show_sql", "false" )
    .put( "hibernate.format_sql", "false" )
    .put( "hibernate.connection.autocommit", "false" )
    .put( "hibernate.hbm2ddl.auto", "update" )
    .put( "hibernate.generate_statistics", "false" )
    .put( "hibernate.connection.driver_class", "org.postgresql.Driver" )
    .put( "hibernate.connection.username", "" )
    .put( "hibernate.connection.password", "" )
    .put( "hibernate.bytecode.use_reflection_optimizer", "true" )
    .put( "hibernate.cglib.use_reflection_optimizer", "true" )
    .put( "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect" )
    .put( "hibernate.cache.use_second_level_cache", "false" )
    .put( "hibernate.cache.use_query_cache", "false" )
    .build( );
for ( final String ctx : PersistenceContexts.list( ) ) {
  final Properties p = new Properties( );
  p.putAll( props );
  final String ctxUrl = String.format( "jdbc:%s",
      ServiceUris.remote( Components.lookup( Database.class ), ctx ) );
  p.put( "hibernate.connection.url", ctxUrl );
  final Ejb3Configuration config = new Ejb3Configuration( );
  config.setProperties( p );
  for ( final Class c : PersistenceContexts.listEntities( ctx ) ) {
    config.addAnnotatedClass( c );
  }
  new SchemaUpdate(config.getHibernateConfiguration()).execute(true, false);
}
