# SBT configuration from command line or environment

Embedded sources allow the modification of the embedded project with 
full eclipse support so there is no need to compile+deploy to test the modification
effects on NGL. External project sources must be available in the NGL
directory for those options to work.
 
  -Dembedded.auth=true
     configures the build to use the authentication source directory
     in NGL instead of an external dependency.

  -Dembedded.spring=true
     configures the build to use the spring plugin source directory
     in NGL instead of an external dependency.

  -Dembedded.mongo=true
     configures the build to use the mongo plugin source directory
     in NGL instead of an external dependency.

Test execution requires some settings:

  -Dngl.test.conf.dir=<path>
     root directory of the tests configurations (git ngl-config). This
     is required to run the tests.

  -Dngl.test.user.name=<name>
     definition of the user identity for the tests (automatic configuration
     files in the configuration users directory).

  -Dlogger.file=<file>
     definition of the test logger configuration file
  
# Running SBT on windows

!! Use the appropriate paths and files, those are just examples
 
Standard command line
  sbt "-Dngl.test.conf.dir=C:\projets\ngl-config\TEST" "-Dlogger.file=c:\projets\config\test\conf\logger.xml"

Library development command line
  sbt "-Dembedded.auth=true" "-Dembedded.spring=true" "-Dembedded.mongo=true" "-Dngl.test.conf.dir=C:\projets\ngl-config\TEST" "-Dlogger.file=c:\projets\config\test\conf\logger.xml"

Running an application (from the sbt prompt)

  ngl-sq/run -Dconfig.file=C:\projets\ngl-config\DEVLOCAL\ngl-sq.dev.vrd.conf -Dlogger.file=C:\projets\config\test\conf\logger.xml 
