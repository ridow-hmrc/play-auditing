import sbt.*
import sbt.Keys.libraryDependencies

object LibDependencies {
  // we depend on http-verbs just to integrate via the AuditHooks
  // http calls are made with the underlying play-ws
  val httpVerbsVersion = "15.6.0"

  val common = Seq(
    "org.scalatest"       %% "scalatest"       % "3.2.17"   % Test,
    "com.vladsch.flexmark" % "flexmark-all"    % "0.64.8"   % Test,
    "org.scalatestplus"   %% "scalacheck-1-17" % "3.2.17.0" % Test,
    "org.scalatestplus"   %% "mockito-4-11"    % "3.2.17.0" % Test
  )

  val play30 = Seq(
    "uk.gov.hmrc"           %% "http-verbs-play-30"    % httpVerbsVersion,
    "com.networknt"          % "json-schema-validator" % "3.0.0",
    "org.playframework"     %% "play-json"             % "3.0.6",
    "io.scalaland"          %% "chimney"               % "1.8.2",
    "com.github.tomakehurst" % "wiremock"              % "3.0.0-beta-7" % Test,
    "org.slf4j"              % "slf4j-simple"          % "2.0.7"        % Test
  )
}
