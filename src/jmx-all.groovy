import com.eucalyptus.util.Classes
import com.eucalyptus.util.Exceptions
import com.eucalyptus.util.Mbeans

import javax.management.JMX
import java.lang.management.*


import javax.management.ObjectName
import javax.management.remote.JMXConnectorFactory as JmxFactory
import javax.management.remote.JMXServiceURL as JmxUrl
import groovy.swing.SwingBuilder
import javax.swing.WindowConstants as WC


def results=[:]
def os=ManagementFactory.operatingSystemMXBean
results["OPERATING SYSTEM"]="""
\tarchitecture=$os.arch
\tname=$os.name
\tversion=$os.version
\tprocessors=$os.availableProcessors
"""

def rt=ManagementFactory.runtimeMXBean
results["RUNTIME"]="""
\tname=$rt.name
\tspec name=$rt.specName
\tvendor=$rt.specVendor
\tspec version=$rt.specVersion
\tmanagement spec version=$rt.managementSpecVersion
"""

def cl=ManagementFactory.classLoadingMXBean
results["CLASS LOADING SYSTEM"]="""
\tisVerbose=${cl.isVerbose()}
\tloadedClassCount=$cl.loadedClassCount
\ttotalLoadedClassCount=$cl.totalLoadedClassCount
\tunloadedClassCount=$cl.unloadedClassCount
"""

def comp=ManagementFactory.compilationMXBean
results["COMPILATION"]="""
\ttotalCompilationTime=$comp.totalCompilationTime
"""

def mem=ManagementFactory.memoryMXBean
def heapUsage=mem.heapMemoryUsage
def nonHeapUsage=mem.nonHeapMemoryUsage
results["MEMORY"]="""
HEAP STORAGE:
\tcommitted=$heapUsage.committed
\tinit=$heapUsage.init
\tmax=$heapUsage.max
\tused=$heapUsage.used
NON-HEAP STORAGE:
\tcommitted=$nonHeapUsage.committed
\tinit=$nonHeapUsage.init
\tmax=$nonHeapUsage.max
\tused=$nonHeapUsage.used
"""

results["MEMORY POOLS"]=ManagementFactory.memoryPoolMXBeans.collect{ mp ->
    "\n\tname: " + mp.name +
    mp.memoryManagerNames.collect{ mmname ->
        "\n\t\tManager Name: $mmname"
    } +
    "\n\t\tmtype=$mp.type" +
    "\n\t\tUsage threshold supported=" + mp.isUsageThresholdSupported() +
    "\n\t\tUsage used=" + mp.getUsage().getUsed()/1024L + "/" + mp.getUsage().getMax()/1024L +
    (mp.getCollectionUsage()?" last-gc="+ mp.getCollectionUsage().getUsed()/1024L + "/" + mp.getCollectionUsage().getMax()/1024L:"") +
    (mp.getPeakUsage()?" peak="+ mp.getPeakUsage().getUsed()/1024L + "/" + mp.getPeakUsage().getMax()/1024L:"")
}


def td=ManagementFactory.threadMXBean
def threads=[:]
td.allThreadIds.collect { tid ->
  def info=td.getThreadInfo(tid)
  threads[info.threadId]=info
}
results["THREADS"]=threads.values().collect { ThreadInfo info ->
    "\n\tThread ${info.threadId} name=${info.threadName}, ${info.threadState} blocked-time=${info.blockedTime/1024L} lock=${info.lockInfo}" +
    (info.lockedMonitors.length == 0?"":"\n\tThread ${info.threadId} name=${info.threadName}, monitors=${info.lockedMonitors.collect{it.lockedStackFrame}}") +
    (info.lockedSynchronizers.length == 0?"":"\n\tThread ${info.threadId} name=${info.threadName}, synchronizers=${info.lockedSynchronizers.collect{it.toString()}}")
}
results["DEADLOCKED THREADS"]=td.findDeadlockedThreads().collect{ long id -> "\n\t DEADLOCKED ${threads[id].threadName}" }
results["DEADLOCKED MONITOR THREADS"]=td.findMonitorDeadlockedThreads().collect{ long id -> "\n\t DEADLOCKED ${threads[id].threadName}" }


results["GARBAGE COLLECTION"]=ManagementFactory.garbageCollectorMXBeans.collect{ gc ->
    "\n\tname=$gc.name" +
    "\n\t\tcollection count=$gc.collectionCount" +
    "\n\t\tcollection time=$gc.collectionTime" +
    gc.memoryPoolNames.collect { mpoolName ->
        "\n\t\tmpool name=$mpoolName"
    }
}

System.setProperty("com.sun.management.jmxremote",Boolean.TRUE.toString())
Mbeans.init()
def serverUrl = 'service:jmx:rmi:///jndi/rmi://localhost:1099/eucalyptus'
try {
  def server = JmxFactory.connect(new JmxUrl(serverUrl)).MBeanServerConnection
  results["JMX MBEANS"]=server.queryMBeans(null,null).collect{"\n\t\t${it.objectName}=${it.className}"}
  server.queryMBeans(null,null).each {
    try {
      inst=server.getObjectInstance(it.objectName);
      type=Classes.interfaceAncestors(Class.forName(inst.className)).get(0);
      results["JMX MBEANS: ${it.objectName}"]="\n\t\t${JMX.newMBeanProxy(server,inst.objectName, type)?.properties}"
    } catch (Exception ex) {
      results["JMX MBEANS: ${it.objectName}"]="\n\t\t${it.className}: ${Exceptions.causes(ex).collect{it.getMessage()}}"
    }
  }
} catch (Exception ex) {
  results["JMX MBEANS"]="${ex}"
}
results.collect{ k, v -> "\n============================================================\n${k}\n${v}" }
