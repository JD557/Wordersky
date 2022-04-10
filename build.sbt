import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "Wordersky"

version := "1.0"

scalaVersion := "3.1.1"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

lazy val root =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .in(file("."))
    .settings(
      Seq(
        scalaVersion := "3.1.1",
        libraryDependencies ++= List(
          "eu.joaocosta"   %%% "minart" % "0.4.0-RC1",
        ),
        scalafmtOnCompile := true,
        semanticdbEnabled := true,
        semanticdbVersion := scalafixSemanticdb.revision,
        scalafixOnCompile := true
      )
    )
    .jsSettings(
      Seq(
        scalaJSUseMainModuleInitializer := true
      )
    )
    .nativeSettings(
      Seq(
        nativeLinkStubs := true,
        nativeMode      := "release",
        nativeLTO       := "thin",
        nativeGC        := "immix",
        nativeConfig ~= {
          _.withEmbedResources(true)
        }
      )
    )
    .settings(name := "Wordersky")
