scalacOptions += "-deprecation"
// Comment to get more information during initialization
logLevel := Level.Warn
// logLevel := Level.Debug

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// dependency "analysis" tool
// addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")
// Eclipse project generation
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
// addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
// addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")
// addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.11")
dependencyOverrides += "com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4"

// bytecode enhancement has been pulled out of core play 
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")

// https://github.com/sbt/sbt-jshint
// jslint
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")
// https://github.com/sbt/sbt-rjs
// requiresjs optimizer
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")
// https://github.com/sbt/sbt-digest
// Web asset checksum
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")
// https://github.com/sbt/sbt-mocha
// SBT plugin for running mocha JavaScript unit tests on node
// addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.2")
// Twirl support maybe needed.
//addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.15")

libraryDependencies += "org.javassist" % "javassist" % "3.20.0-GA"

// addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.1")
