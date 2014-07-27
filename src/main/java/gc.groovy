import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage


MBZ=1.0/(1024l*1024l)
MemoryPoolMXBean permGen = ManagementFactory.getMemoryPoolMXBeans( ).find{ MemoryPoolMXBean m ->
  m.getName().contains("Perm Gen")
}
MemoryPoolMXBean oldGen = ManagementFactory.getMemoryPoolMXBeans( ).find{ MemoryPoolMXBean m ->
  m.getName().contains("Old Gen")
}
GarbageCollectorMXBean cms = ManagementFactory.getGarbageCollectorMXBeans( ).find{ GarbageCollectorMXBean g ->
  g.getName( ) == "ConcurrentMarkSweep"
}
// Look at usage and peak usage collection yeild for old gen, capacity usage for perm gen
MemoryUsage gc = oldGen.getCollectionUsage( );
MemoryUsage usage = oldGen.getUsage( );
MemoryUsage perm = permGen.getUsage( );

[
      "\nog-max-ratio": "${usage.getUsed( )/usage.getMax( )} ${(int)(usage.getUsed()*MBZ)}/${(int)(usage.getMax()*MBZ)}",
      "\nperm-ratio": perm.getUsed( )/perm.getMax( ),
      "\nCMS": "${cms.getName( )} ${cms.getCollectionCount( )}/${cms.getCollectionTime( )}msec"
    ]

