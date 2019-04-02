organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

// Disable Cassandra and Kafka
lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false

lazy val `jpa-crud-java-sbt` = (project in file("."))
  .aggregate(`jpa-crud-java-sbt-api`, `jpa-crud-java-sbt-impl`)

lazy val `jpa-crud-java-sbt-api` = (project in file("jpa-crud-java-sbt-api"))
  .settings(javaCompileSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `jpa-crud-java-sbt-impl` = (project in file("jpa-crud-java-sbt-impl"))
  .enablePlugins(LagomJava)
  .settings(javaCompileSettings: _*)
  .settings(hibernateJpaModelGenSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslTestKit,
      lombok,
      javaJpa,
      hibernateEntityManager,
      hibernateJava8,
      evolutions,
      jdbc,
      h2,
      playRepositoryJpa,
      lagomIntegrationClient,
      assertj
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`jpa-crud-java-sbt-api`)

def mkdirs(dir: File): File = { dir.mkdirs(); dir }

val hibernateVersion = "5.3.7.Final"
val lombok = "org.projectlombok" % "lombok" % "1.18.4"
val h2 = "com.h2database" % "h2" % "1.4.197"
val hibernateEntityManager = "org.hibernate" % "hibernate-entitymanager" % hibernateVersion
val hibernateJpaModelGen = "org.hibernate" % "hibernate-jpamodelgen" % hibernateVersion
val hibernateJava8 = "org.hibernate" % "hibernate-java8" % hibernateVersion
val playRepositoryJpa = "org.taymyr.play" % "play-repository-jpa-java" % "0.0.1"
val assertj = "org.assertj" % "assertj-core" % "3.11.0" % Test
val lagomIntegrationClient = "com.lightbend.lagom" %% "lagom-javadsl-integration-client" % lagomJavadslApi.revision % Test

lazy val javaCompileSettings = Seq(
  javacOptions in compile ++= Seq(
    "-encoding", "UTF-8",
    "-source", "1.8",
    "-target", "1.8",
    "-Xlint:all",
    "-parameters" // See https://github.com/FasterXML/jackson-module-parameter-names
  )
)

// Add resources to compile classpath 
// https://github.com/sbt/sbt/issues/1965
lazy val hibernateJpaModelGenSettings = Seq(
  managedClasspath in Compile := {
    val res = (resourceDirectory in Compile).value
    val old = (managedClasspath in Compile).value
    Attributed.blank(res) +: old
  },
  libraryDependencies += hibernateJpaModelGen
)