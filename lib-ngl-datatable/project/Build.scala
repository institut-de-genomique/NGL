import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "datatable"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "fr.cea.ig",
		 publishArtifact in(Compile, packageDoc) := false,
		credentials += Credentials(new File(sys.env.getOrElse("NEXUS_CREDENTIALS","") + "/nexus.credentials")),
			resolvers += "Nexus repository" at "https://gsphere.genoscope.cns.fr/nexus/content/groups/public/",
      	publishMavenStyle := true,
		publishTo <<= version { (v: String) =>
  				if (v.trim.endsWith("SNAPSHOT")) 
				    Some("snapshots" at "https://gsphere.genoscope.cns.fr/nexus/content/repositories/snapshots") 
				else
				    Some("releases"  at "https://gsphere.genoscope.cns.fr/nexus/content/repositories/releases")
		}
  )

}
