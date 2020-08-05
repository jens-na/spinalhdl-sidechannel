name := "spinalhdl-sidechannel"
version := "0.1"
scalaVersion := "2.11.12"

lazy val externalLibs = project in file("lib")

libraryDependencies ++= Seq(
  "com.github.spinalhdl" % "spinalhdl-core_2.11" % "1.4.0",
  "com.github.spinalhdl" % "spinalhdl-lib_2.11" % "1.4.0",
  "com.github.spinalhdl" % "spinalhdl-asg_2.11" % "0.1",
  compilerPlugin("com.github.spinalhdl" % "spinalhdl-idsl-plugin_2.11" % "1.4.0")
)

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys ++= Seq[BuildInfoKey](
      BuildInfoKey.map(name) { case (k, v) => "project" + k.capitalize -> v.capitalize },
      "externalLibs" -> s"${baseDirectory.in(externalLibs).value.getAbsolutePath}"
    ),
    buildInfoPackage := "spinal.lib.sidechannel"
  )

fork := true
