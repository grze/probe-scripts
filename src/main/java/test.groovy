import org.logicalcobwebs.proxool.ProxoolFacade;

ProxoolFacade.getConnectionPoolDefinition( "eucalyptus_cloud" ).setTrace(true)
ProxoolFacade.getConnectionPoolDefinition( "eucalyptus_cloud" ).dump( )