import sbt._
import scala.sys.process._

lazy val oldVersion = "git describe --abbrev=0".!!.trim.replaceAll("^v", "")

lazy val commonSettings = Seq(
  organization := "com.github.plokhotnyuk.expression-evaluator",
  organizationHomepage := Some(url("https://github.com/plokhotnyuk")),
  homepage := Some(url("https://github.com/plokhotnyuk/expression-evaluator")),
  licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),
  startYear := Some(2019),
  developers := List(
    Developer(
      id = "plokhotnyuk",
      name = "Andriy Plokhotnyuk",
      email = "plokhotnyuk@gmail.com",
      url = url("https://twitter.com/aplokhotnyuk")
    )
  ),
  resolvers += Resolver.sonatypeRepo("staging"),
  scalaVersion := "2.13.6",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-feature",
    "-unchecked",
    "-Ywarn-dead-code",
    "-Xlint",
    "-Xmacro-settings:print-expr-results"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, x)) if x == 11 => Seq(
      "-Ybackend:GenBCode",
      "-Ydelambdafy:inline"
    )
    case _ => Seq()
  }),
  testOptions in Test += Tests.Argument("-oDF"),
  publishTo := sonatypePublishToBundle.value,
  sonatypeProfileName := "com.github.plokhotnyuk",
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/plokhotnyuk/expression-evaluator"),
      "scm:git@github.com:plokhotnyuk/expression-evaluator.git"
    )
  ),
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
)

lazy val publishSettings = Seq(
  packageOptions += Package.ManifestAttributes("Automatic-Module-Name" -> moduleName.value),
  mimaCheckDirection := {
    def isPatch: Boolean = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && newMinor == oldMinor
    }

    if (isPatch) "both" else "backward"
  },
  mimaPreviousArtifacts := {
    def isCheckingRequired: Boolean = {
      val Array(newMajor, newMinor, _) = version.value.split('.')
      val Array(oldMajor, oldMinor, _) = oldVersion.split('.')
      newMajor == oldMajor && (newMajor != "0" || newMinor == oldMinor)
    }

    if (isCheckingRequired) Set(organization.value %% moduleName.value % oldVersion)
    else Set()
  }
)

lazy val `expression-evaluator` = project.in(file("."))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    crossScalaVersions := Seq("2.13.6", "2.12.13", "2.13.5"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % "3.2.9" % Test
    )
  )
