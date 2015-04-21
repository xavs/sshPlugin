grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		compile 'ch.ethz.ganymed:ganymed-ssh2:build210'
	}

	plugins {
		build ':release:3.0.1'
	}
}
