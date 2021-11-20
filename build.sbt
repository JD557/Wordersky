import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "Space Game"

version := "1.0"

scalaVersion := "2.13.6"

lazy val root =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .in(file("."))
    .settings(
      Seq(
        scalaVersion := "2.13.6",
        libraryDependencies ++= List(
          "eu.joaocosta"   %%% "minart-core" % "0.3.1",
          "eu.joaocosta"   %%% "minart-pure" % "0.3.1",
          "eu.joaocosta"   %%% "minart-extra" % "0.3.1-SNAPSHOT",
        )
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
        nativeGC        := "immix"
      )
    )
    .settings(name := "Space Game Root")
