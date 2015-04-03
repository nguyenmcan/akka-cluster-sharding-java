lazy val root = (project in file(".")).
	settings(
		name := "akka-cluster-sharding-java",
		version := "1.0",
		scalaVersion := "2.10.4",
		libraryDependencies ++= Seq(
			"com.typesafe.akka" % "akka-contrib_2.10" % "2.3.4"
		)
	) 

