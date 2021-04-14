scalacOptions += "-deprecation"
logLevel := Level.Warn

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.13")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")
dependencyOverrides += "com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4"

addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-uglify" % "2.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")
addSbtPlugin("com.github.sbt" % "sbt-jacoco" % "3.2.0")

libraryDependencies += "org.javassist" % "javassist" % "3.20.0-GA"