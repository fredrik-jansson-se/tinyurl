name:="tu_frontend"

mainClass := Some("se.fredriks.tu.frontend.FrontendMain")

libraryDependencies ++= Seq(
      "com.twitter" % "finagle-core_2.10" % "6.22.0"
    , "com.twitter" % "finagle-http_2.10" % "6.22.0"
    , "com.twitter" % "finagle-redis_2.10" % "6.22.0"
    , "ch.qos.logback" % "logback-classic" % "1.1.2"
    , "com.typesafe" % "config" % "1.2.1"
    , "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3"
    , "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.3"
    )

