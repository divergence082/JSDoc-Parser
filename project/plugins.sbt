logLevel := Level.Warn

resolvers += "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.12.0")
