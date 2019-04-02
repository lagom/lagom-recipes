resolvers += Resolver.jcenterRepo

dependencyOverrides += "org.junit.jupiter" % "junit-jupiter-engine" % "5.3.1"

addSbtPlugin("com.lightbend.lagom"     % "lagom-sbt-plugin"       % "1.4.9")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin"      % "5.2.4")
addSbtPlugin("net.aichler"             % "sbt-jupiter-interface"  % "0.7.0") // JUnit 5
