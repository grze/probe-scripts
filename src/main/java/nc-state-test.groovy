import com.eucalyptus.cluster.Clusters
import com.eucalyptus.component.Components
import com.eucalyptus.component.ServiceConfiguration
import com.eucalyptus.component.ServiceConfigurations
import com.eucalyptus.component.Topology
import com.eucalyptus.component.id.ClusterController
import com.eucalyptus.empyrean.DescribeServicesType
import com.eucalyptus.empyrean.EnableServiceType
import com.eucalyptus.empyrean.StopServiceType
import com.eucalyptus.node.NodeController
import com.eucalyptus.scripting.Groovyness
import com.eucalyptus.util.async.AsyncRequests

import java.util.concurrent.TimeUnit

outNodes = [:]
def makeId = { ServiceConfiguration config ->
  ServiceConfigurations.ServiceConfigurationToServiceId.INSTANCE.apply(config);
}
def enableService = { ServiceConfiguration ccConfig, ids ->
  EnableServiceType msg = new EnableServiceType();
  msg.getServices().addAll(ids.collect { makeId(it) });
  AsyncRequests.sendSync(ccConfig, msg)
}
def stopService = { ServiceConfiguration ccConfig, ids ->
  StopServiceType msg = new StopServiceType();
  msg.getServices().addAll(ids.collect { makeId(it) });
  AsyncRequests.sendSync(ccConfig, msg)
}
def describeServices = { ServiceConfiguration ccConfig, ids ->
  DescribeServicesType msg = new DescribeServicesType();
  msg.getServices().addAll(ids.collect { makeId(it) });
  AsyncRequests.sendSync(ccConfig, msg).getServiceStatuses().collect { it.getServiceId().getName() + ":" + it.getLocalState() };
}
clusters = Components.lookup(ClusterController.class).services().collect { Groovyness.expandoMetaClass(it) }
nodes = Components.lookup(NodeController.class).services().collect { Groovyness.expandoMetaClass(it) }
nodes.each { outNodes[it] = [:] }
/**
 * Test #1
 * 1. ENABLE all NCs
 * 2. OoB StopService for each NC ==> State now shows up as out of sync
 * 3. Cluster.fireCheck() ==> Should synchronize state
 */
clusters.each { ServiceConfiguration cc ->
  nodes.each { ServiceConfiguration nc ->
    Topology.enable(nc).get();
    outNodes[nc]["#1 ENABLE NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#1 localState ENABLED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  nodes.each { ServiceConfiguration nc ->
    stopService(cc, [nc]);
    outNodes[nc]["#2 OoB StopService NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#2 localState ENABLED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  Clusters.lookup(cc).check();
  nodes.each { ServiceConfiguration nc ->
    outNodes[nc]["#3 Cluster refreshed state"] = describeServices(cc, [nc]);
    outNodes[nc]["#3 localState STOPPED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
}
/**
 * Test #2
 * 4. STOP all NCs
 * 5. OoB EnableService for each NC ==> State now shows up as out of sync
 * 6. Cluster.fireCheck() ==> Should synchronize state
 */
clusters.each { ServiceConfiguration cc ->
  nodes.each { ServiceConfiguration nc ->
    Topology.stop(nc).get();
    outNodes[nc]["#4 STOP NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#4 localState STOPPED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  nodes.each { ServiceConfiguration nc ->
    enableService(cc, [nc]);
    outNodes[nc]["#5 OoB EnableService NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#5 localState STOPPED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  Clusters.lookup(cc).check();
  nodes.each { ServiceConfiguration nc ->
    TimeUnit.SECONDS.sleep(1);
    outNodes[nc]["#6 Cluster refreshed state"] = describeServices(cc, [nc]);
    outNodes[nc]["#6 localState ENABLED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
}
/**
 * Test #3
 * 7. Remove all NCs
 * 8. Cluster.fireCheck() ==> Should recreate missing NCs
 * 9. Check topology for NCs
 */
clusters.each { ServiceConfiguration cc ->
  nodes.each { ServiceConfiguration nc ->
    Topology.enable(nc).get();
    outNodes[nc]["#7 ENABLE NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#7 localState ENABLED == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  nodes.each { ServiceConfiguration nc ->
    Topology.destroy(nc);
    outNodes[nc]["#8 Remove NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#8 localState PRIMORDIAL == "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  nodes.each { ServiceConfiguration nc ->
    enableService(cc, [nc]);
    outNodes[nc]["#9 OoB EnableService"] = describeServices(cc, [nc]);
    outNodes[nc]["#9 localState ENABLED > "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  Clusters.lookup(cc).check();
  nodes.each { ServiceConfiguration nc ->
    TimeUnit.SECONDS.sleep(1)
    outNodes[nc]["#10 Cluster refreshed state -- Cluster.fireCheck()"] = describeServices(cc, [nc]);
    outNodes[nc]["#10 localState ENABLED ==  "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
}
/**
 * Clean up
 * 1. OoB EnableService for each NC ==> State now shows up as out of sync
 * 2. Cluster.fireCheck() ==> Should synchronize state
 */
clusters.each { ServiceConfiguration cc ->
  nodes.each { ServiceConfiguration nc ->
    enableService(cc, [nc]);
    outNodes[nc]["#11 OoB EnableService NCs"] = describeServices(cc, [nc]);
    outNodes[nc]["#11 localState is whatever: "] =  "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
  Clusters.lookup(cc).check();
  nodes.each { ServiceConfiguration nc ->
    outNodes[nc]["#12 Cluster refreshed state"] = describeServices(cc, [nc]);
    outNodes[nc]["#12 localState ENABLED == "] = "${nc.lookupState()} (${describeServices(cc,[nc])}\n";
  }
}
outNodes.collect { nodeKey, nodeValue ->
  "\n\n${nodeKey} =============" + nodeValue.collect { k, v ->
    "\n${k} => ${v}" }
}
