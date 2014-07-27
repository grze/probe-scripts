Thread.getAllStackTraces().findAll{ t, s ->
  t?.threadGroup?.name?.contains("UDP") || t?.threadGroup?.name?.contains("DNSControl")
}.each{ t, s ->
  t.stop( )
}
