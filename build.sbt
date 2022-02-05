import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "Wordersky"

version := "1.0"

scalaVersion := "3.1.1"

lazy val root =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .in(file("."))
    .settings(
      Seq(
        scalaVersion := "3.1.1",
        libraryDependencies ++= List(
          "eu.joaocosta"   %%% "minart-core" % "0.4.0-SNAPSHOT",
          "eu.joaocosta"   %%% "minart-backend" % "0.4.0-SNAPSHOT",
          "eu.joaocosta"   %%% "minart-pure" % "0.4.0-SNAPSHOT",
          "eu.joaocosta"   %%% "minart-image" % "0.4.0-SNAPSHOT",
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
    .settings(name := "Wordersky")
