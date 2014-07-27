Thread.getAllStackTraces().findAll{ t, s -> t.threadGroup?.name?.contains("UDP") }.collect{ t, s -> t.stop(); }
