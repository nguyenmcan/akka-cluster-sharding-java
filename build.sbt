lazy val root = (project in file(".")).
	settings(
		name := "akka-cluster-sharding-java",
		version := "1.0",
		scalaVersion := "2.10.4",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" % "akka-contrib_2.10" % "2.3.4",
			"io.kamon" %% "kamon-core" % "0.3.5",
			"io.kamon" %% "kamon-log-reporter" % "0.3.5",
			"io.kamon" %% "kamon-system-metrics" % "0.3.5",
			"org.aspectj" % "aspectjweaver" % "1.8.4"
		)
	) 

aspectjSettings

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

fork in run := true