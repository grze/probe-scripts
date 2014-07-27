sun.management.ManagementFactoryHelper.@jvm.getInternalCounters("sun.*").collect {
  "\n${it.name} ${it.getValue()} ${it.getUnits()}"
}
