return "hi"
//import com.eucalyptus.cloud.ImageMetadata.Platform
//import com.eucalyptus.compute.conversion.ImportManager
//import com.eucalyptus.compute.conversion.tasks.ConversionState
//import com.eucalyptus.util.Dates
//import com.google.common.collect.Lists
//import edu.ucsb.eucalyptus.msgs.ConversionTask
//import edu.ucsb.eucalyptus.msgs.DiskImageDescription
//import edu.ucsb.eucalyptus.msgs.DiskImageVolumeDescription
//import edu.ucsb.eucalyptus.msgs.ImportInstanceTaskDetails
//import edu.ucsb.eucalyptus.msgs.ImportInstanceVolumeDetail
//import edu.ucsb.eucalyptus.msgs.ImportResourceTag
//
//ConversionTask task = new ConversionTask( );
//task.setConversionTaskId( "import-i-12345678" );
//task.setExpirationTime( Dates.hoursFromNow( ImportManager.CONVERSION_EXPIRATION_TIMEOUT ).toString( ) );
//task.setResourceTagSet( Lists.newArrayList( new ImportResourceTag( "test-key", "test-value" ), new ImportResourceTag( "another-test-key", "another-test-value" ) ) );
//task.setState( ConversionState.active.name( ) );
//task.setStatusMessage( "test message status" )
//
//DiskImageDescription image1 = new DiskImageDescription( "RAW", 12345678l, "https://mybucket.s3.eucalyptus.demo.com/test1.manifest.xml", "test1-checksum" )
//DiskImageVolumeDescription volume1 = new DiskImageVolumeDescription( 1, "emi-12345678" );
//ImportInstanceVolumeDetail volume1Detail = new ImportInstanceVolumeDetail( ConversionState.active.name( ), "test status for volume1", 1234l, "PARTI00", "description of volume1", image1, volume1 );
//
//DiskImageDescription image2 = new DiskImageDescription( "RAW", 87654321l, "https://mybucket.s3.eucalyptus.demo.com/test2.manifest.xml", "test2-checksum" )
//DiskImageVolumeDescription volume2 = new DiskImageVolumeDescription( 1, "vol-12345678" );
//ImportInstanceVolumeDetail volume2Detail = new ImportInstanceVolumeDetail( ConversionState.active.name( ), "test status for voluem2", 4321l, "PARTI00", "description of volume2", image2, volume2 );
//
//task.setImportInstance( new ImportInstanceTaskDetails( "i-12345678", Platform.linux.name( ), "import description", [ volume1Detail, volume2Detail ] ) );
//
//ImportManager.tasks.put( task.getConversionTaskId( ), task );
//ImportManager.tasks.collect{ k, v -> "${k} => ${v}" }.join("\n")