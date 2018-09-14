//
// Configuration from command line or environment
//
//   embedded.auth=true
//     configures the build to use the authentication directory
//     in ngl instead of an external dependency.
//
//   NGL_CONF_TEST_DIR=<path>
//   ngl.test.conf.dir=<path>
//     Those definitions are added to the build classpath and the test
//     infrastructure will locate the test configuration through the
//     classpath.
//
//   ngl.test.logger.file
//   ngl.test.logger.ressource
//   logger.file
//   logger.resource
//     those definitions are used for the test logger configuration
// 

import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

import play.sbt.routes.RoutesKeys.routesGenerator
import play.routes.compiler.StaticRoutesGenerator
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesCompiler.autoImport._

object ApplicationBuild extends Build {

	import BuildSettings._	
	import Resolvers._
	import Dependencies._
	import play.sbt.Play.autoImport._
	import play.sbt.PlayImport._

	// Command line definition that allows the compilation 
	// of a linked authentication library instead of the published one.
	// It is enabled using sbt command line option -Dembedded.auth=true .
	val embeddedAuth   = System.getProperty("embedded.auth")   == "true"
	val embeddedSpring = System.getProperty("embedded.spring") == "true"
	val embeddedMongo  = System.getProperty("embedded.mongo")  == "true"
  val nglLite        = System.getProperty("ngl.lite")        == "true"
  
	// Workaround for missing properties in test vms. Build the test child
	// VM option for the logger.
	val tjl =
	  if (System.getProperty("ngl.test.logger.file") != null)
	    Seq("-Dlogger.file=" + System.getProperty("ngl.test.logger.file"))
	  else if (System.getProperty("ngl.test.logger.resource") != null)
	    Seq("-Dlogger.resource=" + System.getProperty("ngl.test.logger.resource"))
	  else if (System.getProperty("logger.file") != null)
	    Seq("-Dlogger.file=" + System.getProperty("logger.file"))
	  else if (System.getProperty("logger.resource") != null)
	    Seq("-Dlogger.resource=" + System.getProperty("logger.resource"))
	  else
	    Seq()
	    
	// val testJavaOptions = tj0 ++ tjl ++ Seq("-Dngl.in=true"/*,"-bad.stuf=zen"*/)
	val testJavaOptions = tjl;
	
	// Disable paralell test execution (hoped to fixed test failure but didn't work)
	// parallelExecution in Global := false
	
 	val appName                = "ngl"
  val scala                  = "2.12.3"
  // Dist suffix should be "-SNAPSHOT" for the master and "xxx-SNAPSHOT" for specific branches
  // so the deployed application do not land in the same directories. This could be defined 
  // in some configuration instead of being hardcoded.
  val distSuffix             = "-SNAPSHOT"
  //val appVersion             = "2.0.0"    + distSuffix
  
  val buildOrganization      = "fr.cea.ig"
	val buildVersion           = "2.1"    + distSuffix
	val nglVersion             = "2.0"    + distSuffix
	
	val sqVersion              = "2.2.2" + distSuffix	
	val biVersion              = "2.3.0" + distSuffix

	val projectsVersion        = "2.4.0"  + distSuffix
	val reagentsVersion        = "2.2.0"  + distSuffix
	val subVersion             = "2.3.0"  + distSuffix
	// val dataVersion            = "2.0.0"  + distSuffix
	val nglAssetsVersion       = "2.0.0"  + distSuffix
	val nglDataVersion         = "2.6.0"  + distSuffix
	val nglPlatesVersion       = "2.2.0"  + distSuffix
	val nglDevGuideVersion     = "2.0.0"  + distSuffix
	
	val libDatatableVersion    = "2.0.0"  + distSuffix
	val libFrameworkWebVersion = "2.0.0"  + distSuffix
	val nglCommonVersion       = "2.1.0"  + distSuffix

	// IG libraries
  val ceaAuth     = "fr.cea.ig.modules"   %% "authentication"     % "2.1.3"
  val ceaSpring   = "fr.cea.ig"           %% "play-spring-module" % "2.0.2"
  val ceaMongo    = "fr.cea.ig"           %% "mongodbplugin"      % "2.0.5"
	// The fix concerns the Query "in" constructs for collection properties
	// that should be converted roughly like :
	// Query.in(a,b)     -> Query.in(a,Arrays.asList(b))
	// Query.in(a,b,c,d) -> Query.in(a,Arrays.asList(b,c,d))
  val mongoJack   = "org.mongojack"        % "mongojack"          % "2.6.1.IG"
//      "org.mongojack" % "mongojack" % "2.6.1",
//      "org.mongojack" % "mongojack" % "2.7.0",
    // External libraries versions
	val postgresql  = "org.postgresql"       % "postgresql"         % "9.4-1206-jdbc41"  
  val commonsLang = "commons-lang"         % "commons-lang"       % "2.4"
  val jsMessages  = "org.julienrf"        %% "play-jsmessages"    % "3.0.0" 
  val fest        = "org.easytesting"      % "fest-assert"        % "1.4" % "test"
  val jtds        = "net.sourceforge.jtds" % "jtds"               % "1.3.1"

//	override def settings = super.settings ++ Seq(
//		EclipseKeys.skipParents in ThisBuild := false,
//    // Compile the project before generating Eclipse files,
//    // so that generated class files for views and routes are present
//    EclipseKeys.preTasks := Seq(compile in Compile),
//    // Java project. Don't expect Scala IDE
//    EclipseKeys.projectFlavor := com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseProjectFlavor.Java,
//    // - Avoid errors for views and routes
//    // Use .class files instead of generated .scala files for views and routes,
//    // This requires that the project is compiled using sbt to avoid unresolved symbols.
//    EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)	
//  )

	object BuildSettings {
	  
		// Probably poor scala style
    val tev0 = if (System.getProperty("ngl.test.conf.dir") != null)
	        Seq(// unmanagedResourceDirectories in Compile += file(System.getProperty("ngl.test.conf.dir")),
	            // dependencyClasspath in Compile += file(System.getProperty("ngl.test.conf.dir")),
	            dependencyClasspath in Test    += file(System.getProperty("ngl.test.conf.dir")))
	      else
	        Seq()
	  val tev1 = if (System.getProperty("NGL_CONF_TEST_DIR") != null)
	        Seq(//dependencyClasspath in Compile += file(System.getProperty("NGL_CONF_TEST_DIR"),
	            dependencyClasspath in Test    += file(System.getProperty("NGL_CONF_TEST_DIR")))
	      else
	        Seq()
	  
	  val globSettings = Seq(
	    // -- Scala compilation options are not defined as there are no scala sources
      // scalacOptions += "-deprecation",
	    // -- Java compilation options 
			javacOptions         += "-Xlint:deprecation",
			javacOptions         += "-Xlint:unchecked",
			javacOptions         += "-Xlint:-processing", // suppress annotation warning 
	    javacOptions         += "-implicit:class",
			// javacOptions         += "-verbose",
			javacOptions         += "-Xlint",
			// javacOptions         += "-Werror", // warnings as errors
			version              := buildVersion,  
			credentials          += Credentials(new File(sys.env.getOrElse("NEXUS_CREDENTIALS","") + "/nexus.credentials")),
			publishMavenStyle    := true,
			// We keep the static route generator but this should be removed
			// TODO: use dynamic route generator
			//routesGenerator     := StaticRoutesGenerator,
			routesGenerator      := InjectedRoutesGenerator,
			
			// -- jackson 2.8 series is problematic so we fall back on 2.7
			// Jackson 2.8 refuses to uses a default implementation that is not 
			// a superclass of all the other serialized types as it is impossible
			// to build a Collection<B extends A> with a default implementation
			// C extends A as we cannot put a C in a collection of B. This is a proper
			// behavior but this means that all the current default implementation
			// definitions violates the constraint. The expected fix is to change the
			// deserialization behavior to throw an exception when the type field
			// is not set.
			dependencyOverrides  += "com.fasterxml.jackson.core"     % "jackson-core"            % "2.7.3",
			dependencyOverrides  += "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.7.3",
			dependencyOverrides  += "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.7.3",
			dependencyOverrides  += "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.7.3",
			dependencyOverrides  += "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.7.3",
			
			javaOptions in Test ++= testJavaOptions,
			// fork        in Test  := true,
			fork                 := true,
			// --- javadoc configuration
			// Remove scala files from the doc process so javadoc is used. 
			sources in (Compile, doc) <<= sources in (Compile, doc) map { _.filterNot(_.getName endsWith ".scala") },
			javacOptions in (Compile,doc) ++= Seq("-notimestamp", "-linksource"),
			javacOptions in (Compile,doc)  += "-quiet", // do not show the genneration info
			// TODO: should use some defined output dir, don't know what the name is
			dependencyClasspath in (Compile, doc) += baseDirectory.value / "target" / "scala-2.12" / "classes_managed/",
			doc.in(Compile) := doc.in(Compile).dependsOn(compile.in(Compile)).value,
			//
			unmanagedResourceDirectories in Test += baseDirectory.value / "test",
			// sources in doc in Compile := Seq(),
			// Remove javadoc jar creation when packaging (building dist)
			mappings in (Compile, packageDoc) := Seq(),
			scalaVersion        := scala,
			// -- eclipse task configuration
			// Generate all project eclipse configs
			EclipseKeys.skipParents in ThisBuild := false,
			// Java project(s). Don't expect Scala IDE
			EclipseKeys.projectFlavor := com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseProjectFlavor.Java,
			// Compile the project before generating Eclipse files,
			// so that generated class files for views and routes are present
			EclipseKeys.preTasks := Seq(compile in Compile),
			// - Avoid errors for views and routes
			// Use .class files instead of generated .scala files for views and routes,
			// This requires that the project is compiled using sbt to avoid unresolved symbols.
			EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)	
		) ++ tev0 ++ tev1

		val buildSettings =  Seq (
		  organization   := buildOrganization + "." + appName
		) ++ globSettings

		val buildSettingsLib = Seq (
			organization   := buildOrganization
		) ++ globSettings

  }

  object Resolvers {        
    import BuildSettings._
    var location        = sys.env.getOrElse("NGL_LOCATION", default = "external")
    val nglgithub	      = "NGL GitHub Repo"  at "https://institut-de-genomique.github.io/NGL-Dependencies/"    
    val nexusoss        = "Sonatype OSS"     at "https://oss.sonatype.org/content/groups/public/"
    val mavencentral    = "Maven central"    at "http://central.maven.org/maven2/"
    val nexusig         = "Nexus repository" at "https://gsphere.genoscope.cns.fr/nexus/content/groups/public/" 
    val nexus           = if(location.equalsIgnoreCase("external")) Seq(nexusoss,mavencentral,nglgithub) else Seq(nexusig,nglgithub)	
    val nexusigrelease  = "releases"         at "https://gsphere.genoscope.cns.fr/nexus/content/repositories/releases"
    val nexusigsnapshot = "snapshots"        at "https://gsphere.genoscope.cns.fr/nexus/content/repositories/snapshots"
    val nexusigpublish  = if (buildVersion.endsWith("SNAPSHOT")) nexusigsnapshot else nexusigrelease				
  } 
 
  object Dependencies {
    
    // Those are the plugin defined versions but they are also 
    // defined in this project (4.3.11.REALEASE or so).
    val springVersion    = "4.1.6.RELEASE"
    val springSecVersion = "4.0.0.RELEASE"
    
    val nglTestingDependencies = Seq(
      javaCore,
      javaWs,
      guice,
      // https://mvnrepository.com/artifact/junit/junit
      "junit" % "junit" % "4.10",
      // https://mvnrepository.com/artifact/com.typesafe.play/play-test
      // Could not find any shortcut
      "com.typesafe.play" %% "play-test" % "2.6.11",
//      "org.seleniumhq.selenium" % "selenium-server"          % "3.11.0",
//      "org.seleniumhq.selenium" % "selenium-java"          % "3.11.0",
      // Requires extra installation steps
      // "org.seleniumhq.selenium" % "selenium-chrome-driver" % "3.11.0",
//      "org.seleniumhq.selenium" % "selenium-htmlunit"      % "3.11.0",

      //fest
      "org.easytesting"      % "fest-assert"        % "1.4"
      // ceaMongo
    )
    
	  val nglcommonDependencies = Seq(
		  javaCore,
		  javaJdbc, 
		  javaWs,
		  // ceaSpring,		  
		  jsMessages,
		  commonsLang,
			fest,
		  jtds,
		   guice,
		  "mysql"                % "mysql-connector-java"      % "5.1.18",
		  "net.sf.opencsv"       % "opencsv"                   % "2.0",
 		  // "commons-collections"  % "commons-collections"       % "3.2.1",
          "org.apache.commons"   % "commons-collections4"      % "4.1",
//		  "org.springframework"  % "spring-jdbc"               % "4.0.3.RELEASE",		
//		  "org.springframework"  % "spring-test"               % "4.0.3.RELEASE",
		  "org.springframework"  % "spring-jdbc"               % springVersion,		
		  "org.springframework"  % "spring-test"               % springVersion,		  
		  "javax.mail"           % "mail"                      % "1.4.2",
		  "org.codehaus.janino"  % "janino"                    % "2.5.15",
		  "de.flapdoodle.embed"  % "de.flapdoodle.embed.mongo" % "1.50.0",
		  "org.javassist"        % "javassist"                 % "3.20.0-GA",
		  "com.sybase"		       % "jdbc4"                 	   % "7.0"
		)	
			   
	  val ngldatatableDependencies = Seq(
			javaForms
	  )

	  val nglreagentsDependencies = Seq(
		  javaCore
		)
			
		val nglframeworkwebDependencies = Seq(
		  javaCore,
		  javaWs,
		  //ceaAuth,
		  // ceaMongo,
		  "javax.mail"   % "mail"            % "1.4.2",
		  "org.drools"   % "drools-core"     % "6.1.0.Final",
		  "org.drools"   % "drools-compiler" % "6.1.0.Final",
		  "org.drools"   % "knowledge-api"   % "6.1.0.Final",
		  "org.kie"      % "kie-api"         % "6.1.0.Final",
		  "org.kie"      % "kie-internal"    % "6.1.0.Final",
	      "commons-lang" % "commons-lang"    % "2.2"
		) // ++ (if (embeddedAuth) Seq() else Seq(ceaAuth))
		
		val nglbiDependencies = Seq(
			javaCore, 
			javaJdbc,
			postgresql
		)
		
		val nglsqDependencies = Seq(
		  javaCore, 
		  javaJdbc,
		  postgresql,
		  "org.assertj" % "assertj-core" % "1.7.1"
	  )
	  
    val nglplaquesDependencies = Seq(
		  javaCore, 
		  javaJdbc
		)
		
    val ngldevguideDependencies = Seq(
	    javaCore
		)

	  val ngldataDependencies = Seq(
  		javaCore, 
	  	javaJdbc,
	    postgresql
		)
		
	  val nglsubDependencies = Seq(
	    javaCore, 
	    javaJdbc,
	    "org.apache.commons" % "commons-csv" % "1.5",
	    "xerces"             % "xercesImpl"  % "2.8.0"
  	)

	  val nglprojectsDependencies = Seq(
	    javaCore, 
	    javaJdbc,
	    postgresql
	  )

	  val nglPlayMigrationDependencies = Seq(
	    // ceaMongo,
	    ehcache,
	    ws,
	    jsMessages
    )
    
    val authenticationDependencies = Seq(
      javaCore,
		  ws,
		  javaJdbc,
		  // ceaSpring,
  		"org.springframework.security"  % "spring-security-web"    % springSecVersion,
		  "org.springframework.security"  % "spring-security-ldap"   % springSecVersion,
      "org.springframework.security"  % "spring-security-config" % springSecVersion,
  		"org.springframework"           % "spring-aop"             % springVersion
    )
  
    val springPluginDependencies = Seq(
      "org.springframework"    %    "spring-context"    %    springVersion,
      "org.springframework"    %    "spring-core"       %    springVersion,
      "org.springframework"    %    "spring-beans"      %    springVersion,
		  "cglib"                  %    "cglib-nodep"       %    "3.1"
    )

    val mongoPluginDependencies = Seq (
      mongoJack,
      "org.jongo"     % "jongo"     % "1.3.0"
    )

  }

  // ----------- pseudo projects ----------------------
  // Allow the use of embbeded auth sources instead of the lib.
  // We use a virtual project that has only one dependency when using the
  // external dependency.
  val authentication = 
    if (embeddedAuth)
      Project("xt-auth",file("authentication"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies ++= authenticationDependencies,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
    else
      Project("xt-auth",file("dependency-authentication"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies  += ceaAuth,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
  
  val mongoPlugin = 
    if (embeddedMongo)
      Project("xt-mongo",file("mongoDBplugin"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies ++= mongoPluginDependencies,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
    else
      Project("xt-mongo",file("dependency-mongoplugin"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies  += ceaMongo,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
  
  val springPlugin = 
    if (embeddedSpring)
      Project("xt-spring",file("playSpringModule"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies ++= springPluginDependencies,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
    else
      Project("xt-spring",file("dependency-springplugin"),settings = buildSettingsLib)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
          libraryDependencies  += ceaSpring,
          version              := "0.0.1-SNAPSHOT",
          resolvers            := nexus
        )
           
  val nglPlayMigration =  Project("ngl-play-migration",file("lib-ngl-play-migration"),settings = buildSettings)
      .enablePlugins(play.sbt.PlayJava)
      .settings(
        libraryDependencies ++= nglPlayMigrationDependencies,   
        version              := "0.1-SNAPSHOT",
        javacOptions in (Compile,doc) := Seq("-link", "https://docs.oracle.com/javase/7/docs/api/"),
        // javacOptions in (Compile,doc) := Seq("-doctitle", "ngl-play-migration-0.1"),
        // javacOptions in (Compile,doc) := Seq("-top", "ngl-play-migration-0.1"),
        
        resolvers            := nexus
	    ).dependsOn(mongoPlugin)
	
	val nglTesting = Project("ngl-testing",file("lib-ngl-testing"),settings = buildSettings)
      .enablePlugins(play.sbt.PlayJava)
      .settings(
        libraryDependencies       ++= nglTestingDependencies, 
        version                    := "0.1-SNAPSHOT",
        resolvers                  := nexus
      ).dependsOn(mongoPlugin)    

  val ngldatatable = Project("datatable", file("lib-ngl-datatable"), settings = buildSettingsLib)
      .enablePlugins(play.sbt.Play)
      .settings(
        version                    := libDatatableVersion,
        libraryDependencies       ++= ngldatatableDependencies,
        resolvers                  := nexus,
        sbt.Keys.fork in Test      := false,
        publishTo                  := Some(nexusigpublish),
        packagedArtifacts in publishLocal := {
          val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
          val assets: java.io.File = (PlayKeys.playPackageAssets in Compile).value
          artifacts + (Artifact(moduleName.value, "asset", "jar", "assets") -> assets)
        },
        packagedArtifacts in publish := {
          val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
          val assets: java.io.File = (PlayKeys.playPackageAssets in Compile).value
          artifacts + (Artifact(moduleName.value, "asset", "jar", "assets") -> assets)
        }
      ).dependsOn(nglPlayMigration)

  val nglframeworkweb = Project("lib-frameworkweb", file("lib-ngl-frameworkweb"), settings = buildSettingsLib)
      .enablePlugins(play.sbt.Play)
      .settings(
        version                    := libFrameworkWebVersion,
        libraryDependencies       ++= nglframeworkwebDependencies,
        resolvers                  := nexus,
        sbt.Keys.fork in Test      := false,
        publishTo := Some(nexusigpublish),
        packagedArtifacts in publishLocal := {
          val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
          val assets: java.io.File = (PlayKeys.playPackageAssets in Compile).value
          artifacts + (Artifact(moduleName.value, "asset", "jar", "assets") -> assets)
        },
        packagedArtifacts in publish := {
          val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
          val assets: java.io.File = (PlayKeys.playPackageAssets in Compile).value
          artifacts + (Artifact(moduleName.value, "asset", "jar", "assets") -> assets)
        }
      ).dependsOn(ngldatatable, authentication, springPlugin)
  
  val nglcommon = Project(appName + "-common", file("lib-ngl-common"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    // version                    := appVersion,
    version                    := nglCommonVersion,
    libraryDependencies       ++= nglcommonDependencies,
    resolvers                  := nexus,
    resolvers                  += "julienrf.github.com" at "http://julienrf.github.com/repo/",
    sbt.Keys.fork in Test      := false,
    publishTo                  := Some(nexusigpublish),
    // resourceDirectory in Test <<= baseDirectory / "conftest" 
    // baseDirectory : RichFileSetting
    //   /(c: String): Def.Initialize[File]
    // trait DefinableSetting[S]
    //   <<=(app: Def.Initialize[S]): Def.Setting[S]
    // (resourceDirectory.in(Test)).<<=(baseDirectory.value./("conftest"))
    // resourceDirectory.in(Test).:=(baseDirectory.value./("conftest"))
    resourceDirectory in Test  := baseDirectory.value / "conftest"
  // ).dependsOn(nglframeworkweb % "compile->compile;test->test;doc->doc", nglPlayMigration, nglTesting % "test->test")
//  ).dependsOn(nglframeworkweb, nglPlayMigration, nglTesting % "test->test")
  ).dependsOn(nglframeworkweb, nglPlayMigration, nglTesting % "compile->test")
    
  val nglbi = Project(appName + "-bi", file("app-ngl-bi"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := biVersion,
    libraryDependencies       ++= nglbiDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish)
  ).dependsOn(nglcommon % "compile;test->test" , nglTesting % "test->test")

  val ngldata = Project(appName + "-data", file("app-ngl-data"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglDataVersion,
    libraryDependencies       ++= ngldataDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish),
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty
  ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")

  val nglsq = Project(appName + "-sq", file("app-ngl-sq"), settings = buildSettings)
                 .enablePlugins(play.sbt.PlayJava)
                 .configs( IntegrationTest )
                 .settings(Defaults.itSettings : _*)
                 .settings(                    
    version                    := sqVersion,
    libraryDependencies       ++= nglsqDependencies,
    resolvers                  := nexus,
	  //publishArtifact in (Compile, packageDoc) := false,
    //publishArtifact in packageDoc := false,
    //sources in (Compile,doc)   := Seq.empty,
    publishArtifact in makePom := false, 
    publishTo                  := Some(nexusigpublish)
  ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")

  val nglsub = Project(appName + "-sub", file("app-ngl-sub"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := subVersion,
    libraryDependencies       ++= nglsubDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish)
  ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")

  val nglassets = Project(appName + "-assets", file("app-ngl-asset"),settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
		version                    := nglAssetsVersion,
		libraryDependencies        += guice,
		resolvers                  := nexus,
		publishArtifact in makePom := false,
		publishTo                  := Some(nexusigpublish)
  )
   
  val nglplates = Project(appName + "-plates", file("app-ngl-plaques"),settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglPlatesVersion,
		libraryDependencies       ++= nglplaquesDependencies,	
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish)
  ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")

  val ngldevguide = Project(appName + "-devguide", file("app-ngl-devguide"),settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglDevGuideVersion,
		libraryDependencies       ++= ngldevguideDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish) 
  ).dependsOn(nglcommon % "test->test;compile->compile")
  
  val nglprojects = Project(appName + "-projects", file("app-ngl-projects"),settings = buildSettings)
    .enablePlugins(play.sbt.PlayJava)
    .settings(
  		version                    := projectsVersion,
	  	libraryDependencies       ++= nglprojectsDependencies,   
      resolvers                  := nexus,
      publishArtifact in makePom := false,
      publishTo                  := Some(nexusigpublish)
    ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")
	 
	val nglreagents = Project(appName + "-reagents", file("app-ngl-reagents"), settings = buildSettings)
	  .enablePlugins(play.sbt.PlayJava)
	  .settings(
		  version                    := reagentsVersion,
  		libraryDependencies       ++= nglreagentsDependencies,   
      resolvers                  := nexus,
      publishArtifact in makePom := false,
      publishTo                  := Some(nexusigpublish)
    ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")
    
  val main = Project(appName, file("."), settings = buildSettings)
        .enablePlugins(play.sbt.PlayJava)
        .settings(
	  	version                    := nglVersion,			  
      resolvers                  := nexus,
      publishArtifact in makePom := false,
      publishTo                  := Some(nexusigpublish)
    ).aggregate(
      // libs
   	  nglcommon,
     	nglframeworkweb,
      ngldatatable,
      // applications
      nglsq,       
   	  nglbi,        
   	  nglassets,   
     	nglplates,   
     	ngldata,     
   	  nglsub,      
     	nglreagents, 
     	nglprojects, 
   	  ngldevguide,
      // play migration and testing
      nglPlayMigration,
   	  nglTesting,
     	authentication,
      springPlugin,
      mongoPlugin
    )
    
    // -- Javadoc generation experiment
    
//  lazy val foo = taskKey[Seq[Seq[File]]]("foo")
//  lazy val sharedProjects = settingKey[Seq[ProjectReference]]("shared projects")
//
//  sharedProjects := Seq(
//      nglcommon,
//      nglframeworkweb,
//      ngldatatable,
//      // applications
//      nglsq,       
//   	  nglbi,        
//   	  nglassets,   
//     	nglplates,   
//     	ngldata,     
//   	  nglsub,      
//     	nglreagents, 
//     	nglprojects, 
//   	  ngldevguide,
//      // play migration and testing
//      nglPlayMigration,
//   	  nglTesting,
//     	authentication,
//      springPlugin,
//      mongoPlugin
//    )
//
//  foo := (Def.taskDyn {
//    val projects = sharedProjects.value
//    val filter = ScopeFilter(inProjects(projects: _*), inConfigurations(Compile))
//    Def.task {
//      val allSources = sources.all(filter).value
//      allSources
//    }
//  }).value

//  val projectList = Seq(
//      nglcommon,
//      nglframeworkweb,
//      ngldatatable,
//      // applications
//      nglsq,       
//   	  nglbi,        
//   	  nglassets,   
//     	nglplates,   
//     	ngldata,     
//   	  nglsub,      
//     	nglreagents, 
//     	nglprojects, 
//   	  ngldevguide,
//      // play migration and testing
//      nglPlayMigration,
//   	  nglTesting,
//     	authentication,
//      springPlugin,
//      mongoPlugin
//    )
  
//    val sqLite =
//         Project("ngl-sq-lite", file("app-ngl-sq-lite"), settings=buildSettings) 
//                .enablePlugins(play.sbt.PlayJava)
//                .dependsOn(nglsq)
                  
//  val coreSources =
//       (((sources in nglcommon)        in Compile).value)
//    ++ (((sources in nglframeworkweb)  in Compile).value)
//    ++ ((sources in ngldatatable)     in Compile).value
//    ++ ((sources in nglPlayMigration) in Compile).value
//    ++ ((sources in nglTesting)       in Compile).value
//    ++ ((sources in authentication)   in Compile).value
//    ++ ((sources in springPlugin)     in Compile).value
//    ++ ((sources in mongoPlugin)      in Compile).value
    
  // We cannot merge the docs as individual projects defines the
  // same classes in the same packages.
  // 
//  val fulldoc = Project("fulldoc", file("fulldoc"), settings=buildSettings)
//    .enablePlugins(play.sbt.PlayJava)
//    .settings(
//      version                    := "0.1",
//      // sources in Compile := foo,
//      // sources in (Compile, doc) <<= (sources in nglcommon)       in (Compile,doc),
//      (sources in Compile) :=   (((sources in nglcommon)        in Compile).value
//                              ++ ((sources in nglframeworkweb)  in Compile).value
//                              ++ ((sources in ngldatatable)     in Compile).value
//                              ++ ((sources in nglsq)            in Compile).value       
////                              ++ ((sources in nglbi)            in Compile).value        
////                              ++ ((sources in nglassets)        in Compile).value   
////                              ++ ((sources in nglplates)        in Compile).value   
////                              ++ ((sources in ngldata)          in Compile).value     
////                              ++ ((sources in nglsub)           in Compile).value  
////                              ++ ((sources in nglreagents)      in Compile).value 
////                              ++ ((sources in nglprojects)      in Compile).value 
////                              ++ ((sources in ngldevguide)      in Compile).value
//                              ++ ((sources in nglPlayMigration) in Compile).value
//   	                          ++ ((sources in nglTesting)       in Compile).value
//     	                        ++ ((sources in authentication)   in Compile).value
//                              ++ ((sources in springPlugin)     in Compile).value
//                              ++ ((sources in mongoPlugin)      in Compile).value),
////      (sources in Compile) := projectList flatMap { p => ((sources in p) in Compile) },
//      // (sources in Compile) := foo.value.flatten,
//      // sources in (Compile, doc) <<= (sources in nglframeworkweb) in (Compile, doc),
//      // sources in (Compile, doc) <<= (sources in nglframeworkweb) in (Compile, doc),
//      libraryDependencies       ++= nglcommonDependencies
//    ).dependsOn(
//      nglcommon,
//      nglframeworkweb,
//      ngldatatable,
//      // applications
//      nglsq,       
//   	  nglbi,        
//   	  nglassets,   
//     	nglplates,   
//     	ngldata,     
//   	  nglsub,      
//     	nglreagents, 
//     	nglprojects, 
//   	  ngldevguide,
//      // play migration and testing
//      nglPlayMigration,
//   	  nglTesting,
//     	authentication,
//      springPlugin,
//      mongoPlugin
//    )
     
}
