sun.management.ManagementFactoryHelper.@jvm.getInternalCounters("sun.gc.*").collect {
  "\n${it.name} ${it.getValue()} ${it.getUnits()}"
}
