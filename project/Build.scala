import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseCreateSrc

import play.sbt.routes.RoutesKeys.routesGenerator
import play.routes.compiler.StaticRoutesGenerator
import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.routes.RoutesCompiler.autoImport._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.rjs.SbtRjs.autoImport._
import com.typesafe.sbt.uglify.SbtUglify.autoImport._
import com.typesafe.sbt.web.SbtWeb.autoImport._

object ApplicationBuild extends Build {

	import BuildSettings._	
	import Resolvers._
	import Dependencies._
	import play.sbt.Play.autoImport._
	import play.sbt.PlayImport._

	val embeddedAuth   = System.getProperty("embedded.auth")   == "true"
	val embeddedSpring = System.getProperty("embedded.spring") == "true"
	val embeddedMongo  = System.getProperty("embedded.mongo")  == "true"
	
 	val appName                = "ngl"
  val scala                  = "2.12.3"
  // Dist suffix should be "-SNAPSHOT" for the master and "xxx-SNAPSHOT" for specific branches
  // so the deployed application do not land in the same directories. This could be defined 
  // in some configuration instead of being hardcoded.
  val distSuffix             = "-SNAPSHOT"
  
  val buildOrganization      = "fr.cea.ig"
	val buildVersion           = "2.1"    + distSuffix
	val nglVersion             = "2.0"    + distSuffix

	val sqVersion              = "2.21.0" + distSuffix

	val biVersion              = "2.17.0" + distSuffix


	val projectsVersion        = "2.13.0"  + distSuffix

	val reagentsVersion       = "3.4.0"  + distSuffix

	// val dataVersion            = "2.0.0"  + distSuffix
	val nglAssetsVersion       = "2.4.0"  + distSuffix

	val nglDataVersion         = "2.30.0"  + distSuffix
	val nglPlatesVersion       = "2.4.0"  + distSuffix
	val nglDevGuideVersion     = "2.0.0"  + distSuffix

	val subVersion             = "3.9.0"  + distSuffix



	val libDatatableVersion    = "2.0.0"  + distSuffix
	val libFrameworkWebVersion = "2.0.0"  + distSuffix
	val nglCommonVersion       = "2.6.0"  + distSuffix

	// IG libraries
  val ceaAuth     = "fr.cea.ig.modules"   %% "authentication"     % "2.2.0"
  val ceaSpring   = "fr.cea.ig"           %% "play-spring-module" % "2.0.5"
  val ceaMongo    = "fr.cea.ig"           %% "mongodbplugin"      % "2.0.10"
	// The fix concerns the Query "in" constructs for collection properties
	// that should be converted roughly like :
	// Query.in(a,b)     -> Query.in(a,Arrays.asList(b))
	// Query.in(a,b,c,d) -> Query.in(a,Arrays.asList(b,c,d))
  val mongoJack   = "org.mongojack"        % "mongojack"          % "2.6.1.IG"
  // External libraries versions
	val postgresql  = "org.postgresql"       % "postgresql"         % "9.4-1206-jdbc41"  
  val commonsLang = "commons-lang"         % "commons-lang"       % "2.4"
  val jsMessages  = "org.julienrf"        %% "play-jsmessages"    % "3.0.0" 
  val fest        = "org.easytesting"      % "fest-assert"        % "1.4" % "test"
  val jtds        = "net.sourceforge.jtds" % "jtds"               % "1.3.1"
  
	object BuildSettings {
	  
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
			
			fork        in Test  := true,
			testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-q"), // -a: show line number when AssertionError is raised, -q: Suppress stdout for successful tests. 
			
			// Propagate ngl test configuration directory to test properties
			testOptions += Tests.Argument("-Dngl.test.conf.dir=" + System.getProperty("ngl.test.conf.dir")),
			// Propagate logger file to test properties
			testOptions += Tests.Argument("-Dlogger.file=" + System.getProperty("logger.file")),
			// testOptions += Tests.Argument("play.server.netty.maxInitialLineLength=16384")),
			
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
		) // ++ tev0 ++ tev1

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
      "junit" % "junit" % "4.10",
      "com.typesafe.play" %% "play-test" % "2.6.11",
      "org.easytesting"      % "fest-assert"        % "1.4"
    )
    
	  val nglcommonDependencies = Seq(
		  javaCore,
		  javaJdbc, 
		  javaWs,		  
		  jsMessages,
		  commonsLang,
			fest,
		  jtds,
		   guice,
		  "mysql"                % "mysql-connector-java"      % "5.1.18",
		  "net.sf.opencsv"       % "opencsv"                   % "2.0",
      "org.apache.commons"   % "commons-collections4"      % "4.1",
		  "org.springframework"  % "spring-jdbc"               % springVersion,		
		  "org.springframework"  % "spring-test"               % springVersion,		  
		  "javax.mail"           % "mail"                      % "1.4.2",
		  "org.codehaus.janino"  % "janino"                    % "2.5.15",
		  "de.flapdoodle.embed"  % "de.flapdoodle.embed.mongo" % "1.50.0",
		  "org.javassist"        % "javassist"                 % "3.20.0-GA",
		  "com.sybase"	  	     % "jdbc4"                 	   % "7.0",
		  "org.jsoup"            % "jsoup"                     % "1.13.1",
      "org.hsqldb"           % "hsqldb"                    % "2.4.1" % Test,
      "org.mockito" % "mockito-all" % "1.10.8" % Test,
      "org.powermock" % "powermock-api-mockito" % "1.6.1" % Test,
      "org.powermock" % "powermock-core" % "1.6.1" % Test,
      "org.powermock" % "powermock-api-mockito" % "1.6.1" % Test,
      "org.powermock" % "powermock-module-junit4" % "1.6.1" % Test
//      "junit" % "junit" % "4.11" % Test,
////      crossPaths := false,
//      "com.novocode" % "junit-interface" % "0.11" % Test
		)	

     val ngltestDependencies = Seq(
       "junit" % "junit" % "4.10",
      "com.typesafe.play" %% "play-test" % "2.6.11",
      "org.mockito" % "mockito-all" % "1.10.8" % Test,
      "org.powermock" % "powermock-api-mockito" % "1.6.1" % Test,
      "org.powermock" % "powermock-core" % "1.6.1" % Test,
      "org.powermock" % "powermock-api-mockito" % "1.6.1" % Test,
      "org.powermock" % "powermock-module-junit4" % "1.6.1" % Test
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
		  "javax.mail"   % "mail"            % "1.4.2",
		  "org.drools"   % "drools-core"     % "6.1.0.Final",
		  "org.drools"   % "drools-compiler" % "6.1.0.Final",
		  "org.drools"   % "knowledge-api"   % "6.1.0.Final",
		  "org.kie"      % "kie-api"         % "6.1.0.Final",
		  "org.kie"      % "kie-internal"    % "6.1.0.Final",
	    "commons-lang" % "commons-lang"    % "2.2"
		)
		
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
	    ehcache,
	    ws,
	    jsMessages
    )
    
    val authenticationDependencies = Seq(
      javaCore,
		  ws,
		  javaJdbc,
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
           
  // -------------------------------------------------------------------------
  // -- Regular NGL projects
        
  val nglPlayMigration =  Project("ngl-play-migration",file("lib-ngl-play-migration"),settings = buildSettings)
      .enablePlugins(play.sbt.PlayJava)
      .settings(
        libraryDependencies ++= nglPlayMigrationDependencies,   
        version              := "0.1-SNAPSHOT",
        javacOptions in (Compile,doc) := Seq("-link", "https://docs.oracle.com/javase/7/docs/api/"),
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
        pipelineStages in Assets := Seq(uglify),
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
        pipelineStages in Assets := Seq(uglify),
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
      ).dependsOn(ngldatatable, authentication, springPlugin, nglTesting)
  
  val nglcommon = Project(appName + "-common", file("lib-ngl-common"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglCommonVersion,
    libraryDependencies       ++= nglcommonDependencies,
    resolvers                  := nexus,
    resolvers                  += "julienrf.github.com" at "http://julienrf.github.com/repo/",
    sbt.Keys.fork in Test      := false,
    publishTo                  := Some(nexusigpublish),
    pipelineStages in Assets := Seq(uglify)
  ).dependsOn(nglframeworkweb, nglPlayMigration, nglTesting % "compile->test")
    
  val ngldataapi = Project(appName + "-data-api", file("lib-ngl-data"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglDataVersion,
    libraryDependencies       ++= ngldataDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish),
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty
  ).dependsOn(nglcommon % "test->test;compile->compile", nglTesting % "test->test")
  
  val nglbi = Project(appName + "-bi", file("app-ngl-bi"), settings = buildSettings).enablePlugins(play.sbt.PlayJava, SbtWeb).settings(
    version                    := biVersion,
    libraryDependencies       ++= nglbiDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish),
    pipelineStages := Seq(uglify)
  ).dependsOn(nglcommon % "compile;test->test" , ngldataapi % "compile->test", nglTesting % "test->test")

  val ngldata = Project(appName + "-data", file("app-ngl-data"), settings = buildSettings).enablePlugins(play.sbt.PlayJava).settings(
    version                    := nglDataVersion,
    resolvers                  := nexus,
    libraryDependencies       ++= ngltestDependencies,
    publishArtifact in makePom := false,
    publishTo                  := Some(nexusigpublish),
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile,doc) := Seq.empty
  ).dependsOn(ngldataapi % "compile->test")

  val nglsq = Project(appName + "-sq", file("app-ngl-sq"), settings = buildSettings)
                 .enablePlugins(play.sbt.PlayJava, SbtWeb)
                 .settings(                    
    version                    := sqVersion,
    libraryDependencies       ++= nglsqDependencies,
    libraryDependencies += filters,
    resolvers                  := nexus,
    publishArtifact in makePom := false, 
    publishTo                  := Some(nexusigpublish),
    pipelineStages := Seq(uglify)
  ).dependsOn(nglcommon % "test->test;compile->compile", ngldataapi % "compile->test", nglTesting % "compile->test")

  val nglsub = Project(appName + "-sub", file("app-ngl-sub"), settings = buildSettings).enablePlugins(play.sbt.PlayJava, SbtWeb).settings(
    version                    := subVersion,
    libraryDependencies       ++= nglsubDependencies,
    resolvers                  := nexus,
    publishArtifact in makePom := false, 
    publishTo                  := Some(nexusigpublish)
  ).dependsOn(nglcommon % "test->test;compile->compile", ngldataapi % "compile->test", nglTesting % "test->test")

  val nglassets = Project(appName + "-assets", file("app-ngl-asset"),settings = buildSettings).enablePlugins(play.sbt.PlayJava, SbtWeb).settings(
		version                    := nglAssetsVersion,
		libraryDependencies        += guice,
		resolvers                  := nexus,
		publishArtifact in makePom := false,
		publishTo                  := Some(nexusigpublish)
  )
   
  val nglplates = Project(appName + "-plates", file("app-ngl-plaques"),settings = buildSettings)
    .enablePlugins(play.sbt.PlayJava, SbtWeb)
    .settings(
      version                    := nglPlatesVersion,
		  libraryDependencies       ++= nglplaquesDependencies,	
      resolvers                  := nexus,
      publishArtifact in makePom := false,
      publishTo                  := Some(nexusigpublish)
    ).dependsOn(nglcommon % "test->test;compile->compile", ngldataapi % "compile->test", nglTesting % "test->test")

  val ngldevguide = Project(appName + "-devguide", file("app-ngl-devguide"),settings = buildSettings)
    .enablePlugins(play.sbt.PlayJava)
    .settings(
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
      publishTo                  := Some(nexusigpublish),
      pipelineStages := Seq(uglify)
    ).dependsOn(nglcommon % "test->test;compile->compile", ngldataapi % "compile->test", nglTesting % "test->test")
	 
	val nglreagents = Project(appName + "-reagents", file("app-ngl-reagents"), settings = buildSettings)
	  .enablePlugins(play.sbt.PlayJava, SbtWeb)
	  .settings(
		  version                    := reagentsVersion,
  		libraryDependencies       ++= nglreagentsDependencies,   
      resolvers                  := nexus,
      publishArtifact in makePom := false,
      publishTo                  := Some(nexusigpublish)
    ).dependsOn(nglcommon % "test->test;compile->compile", ngldataapi % "compile->test", nglTesting % "test->test")
    
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
}
