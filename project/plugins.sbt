resolvers += Resolver.url("fix-sbt-plugin-releases", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns).withAllowInsecureProtocol(true)

addSbtPlugin("com.typesafe.sbt"  % "sbt-git"       % "1.0.0")
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"  % "0.15.0")
addSbtPlugin("org.scoverage"     % "sbt-scoverage" % "1.6.1")
addSbtPlugin("net.virtual-void"  % "sbt-dependency-graph" % "0.9.2")
