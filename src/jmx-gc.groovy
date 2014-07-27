java.lang.management.ManagementFactory.garbageCollectorMXBeans.collect { gc ->
    sprintf "\n%20.20s %10d %12d %s", gc.name, gc.collectionCount, gc.collectionTime, gc.memoryPoolNames.collect{"$it"}
}
