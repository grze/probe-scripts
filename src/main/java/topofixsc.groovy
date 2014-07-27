import com.eucalyptus.storage.LogicalStorageManager
import com.eucalyptus.storage.StorageManagers
import com.eucalyptus.util.Classes
import com.eucalyptus.util.EucalyptusCloudException
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.collect.ComputationException

StorageManagers.managerInstances = CacheBuilder.newBuilder().build(
    new CacheLoader<String, LogicalStorageManager>() {
      @Override
      public LogicalStorageManager load( String arg0 ) {
        LogicalStorageManager bsm = Classes.newInstance( StorageManagers.lookupManager( arg0 ) );
        try {
          bsm.checkPreconditions( );
          return bsm;
        } catch ( EucalyptusCloudException ex ) {
          throw new ComputationException( ex );
        }
      }
    });