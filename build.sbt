import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.sbt.SbtGit.GitCommand
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys.assembly

import scala.sys.process.Process

////////////////////////////////////////////////////////////////////////////////////////////////
// Common settings for all sub-projects
////////////////////////////////////////////////////////////////////////////////////////////////
val htmlReportsDirectory: String = "target/test-reports"

lazy val htsjdkAndPicardExcludes = Seq(
  ExclusionRule(organization="org.apache.ant"),
  ExclusionRule(organization="gov.nih.nlm.ncbi"),
  ExclusionRule(organization="org.testng"),
  ExclusionRule(organization="com.google.cloud.genomics")
)

lazy val ToolkitVersion = {
  val date     = new SimpleDateFormat("yyyyMMdd").format(new Date())
  val hash     = Process("git rev-parse --short HEAD").lineStream.head
  val modified = Process("git status --porcelain").lineStream.nonEmpty

  date + "-" + hash + (if (modified) "-dirty" else "")
}

version in ThisBuild := ToolkitVersion

lazy val commonSettings = Seq(
  organization         := "com.editasmedicine",
  organizationName     := "Editas Medicine Inc.",
  organizationHomepage := Some(url("http://www.editasmedicine.com/")),
  homepage             := Some(url("https://github.com/EditasMedicine/digenomitas")),
  startYear            := Some(2017),
  scalaVersion         := "2.13.5",
  scalacOptions        += "-target:jvm-1.8",
  useCoursier          := false,
  autoAPIMappings      := true,
  version              := ToolkitVersion,
  testOptions in Test  += Tests.Argument(TestFrameworks.ScalaTest, "-h", Option(System.getenv("TEST_HTML_REPORTS")).getOrElse(htmlReportsDirectory)),
  testOptions in Test  += Tests.Argument("-oDF"),
  resolvers            += Resolver.jcenterRepo.withAllowInsecureProtocol(true),
  resolvers            += Resolver.sonatypeRepo("public").withAllowInsecureProtocol(true),
  resolvers            += Resolver.mavenLocal,
  resolvers            += Resolver.file("ivy-local", new File("~/.ivy2/local")),
  shellPrompt          := { state => "%s| %s> ".format(GitCommand.prompt.apply(state), version.value) },
  updateOptions        := updateOptions.value.withCachedResolution(true),
  javaOptions in Test += "-Xmx1G",
  assemblyJarName in assembly := s"${name.value}.jar"
) ++ Defaults.coreDefaultSettings

////////////////////////////////////////////////////////////////////////////////////////////////
// commons project
////////////////////////////////////////////////////////////////////////////////////////////////
lazy val commons = Project(id="commons", base=file("commons"))
  .settings(commonSettings: _*)
  .settings(description := "Utility and base classes used across projects.")
  .settings(libraryDependencies ++= Seq(
      "org.scalatest"       %% "scalatest"     % "3.2.2" % "test",
      "org.scalactic"       %% "scalactic"     % "3.2.2" % "test",
      "com.vladsch.flexmark" % "flexmark-all"  % "0.35.10" % "test",
      "com.fulcrumgenomics" %% "sopt"          % "1.1.0",
      "com.fulcrumgenomics" %% "commons"       % "1.1.0",
      "com.fulcrumgenomics" %% "fgbio"         % "1.3.0" excludeAll(htsjdkAndPicardExcludes:_*),
      "com.beachape"        %% "enumeratum"    % "1.6.1",
      "com.github.samtools"  % "htsjdk"        % "2.23.0" excludeAll(htsjdkAndPicardExcludes: _*)
      ),
  )
  .disablePlugins(AssemblyPlugin)

////////////////////////////////////////////////////////////////////////////////////////////////
// Aligner project
////////////////////////////////////////////////////////////////////////////////////////////////
lazy val aligner = Project(id="aligner", base=file("aligner"))
  .settings(commonSettings: _*)
  .settings(description := "Classes for performing guide+pam alignments.")
  .dependsOn(commons % "compile->compile;test->test")


////////////////////////////////////////////////////////////////////////////////////////////////
// digenome project
////////////////////////////////////////////////////////////////////////////////////////////////
lazy val digenome = Project(id="digenome", base=file("digenome"))
  .settings(description := "Tools for analyzing digenome-sequencing data.")
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
/*      "com.github.broadinstitute"   % "picard"        % "2.23.0" excludeAll(htsjdkAndPicardExcludes:_*)*/
  ))
  .dependsOn(commons % "compile->compile;test->test")
  .dependsOn(aligner)

// Root project
lazy val root = Project(id="root", base=file("."))
  .settings(commonSettings:_*)
  .aggregate(commons, digenome)
  .disablePlugins(AssemblyPlugin)
