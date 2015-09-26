name := "JSDoc-Parser"

version := "0.0.1-RELEASE"

scalaVersion := "2.10.4"

organization := "com.github.div082"

homepage := Some(url("https://github.com/divergence082/JSDoc-Parser"))

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php"))

description := """JavaScript Documentation Parser"""

val nexusUrl = "https://oss.sonatype.org"

resolvers ++= Seq(
  "Sonatype OSS" at nexusUrl + "/content/repositories/releases/"
)

publishTo := {
  if (isSnapshot.value)
    Some("snapshots" at nexusUrl + "/content/repositories/snapshots/")
  else
    Some("releases"  at nexusUrl + "/content/repositories/releases/")
}

publishMavenStyle := true

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module"  %% "jackson-module-scala" % "2.6.0-1",
  "com.github.scopt" %% "scopt" % "3.3.0"
)

mainClass in (Compile, packageBin) := Some("com.github.div082.jsdoc.JSDocParser")
