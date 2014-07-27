[sprintf("\n%20.20s %8s %8s %8s %5s %5s %-5s %-20.20s %s", "Memory Pool", "used", "peak", "max", "used%", "peak%", "gc%", "type", "garbage collectors")] +
java.lang.management.ManagementFactory.memoryPoolMXBeans.collect { mp ->
  def usageUsed = (long) mp.getUsage().getUsed() / (1024L * 1024L)
  def max = (long) mp.getUsage().getMax() / (1024L * 1024L)
  def gcUsed = (long) mp.getCollectionUsage() ? mp.getCollectionUsage().getUsed() / (1024L * 1024L) : 0L
  def peakUsed = (long) mp.getPeakUsage() ? mp.getPeakUsage().getUsed() / (1024L * 1024L) : 0L
  sprintf("\n%20.20s %8d %8d %8d %3.3f %3.3f %3.3f %-20.20s %s",
  mp.name,
  usageUsed, peakUsed, max,
  usageUsed / max, peakUsed / max, (max - gcUsed) / max,
  mp.type, "$mp.memoryManagerNames")
}
