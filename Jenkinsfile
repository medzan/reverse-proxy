#!groovy

pipeline {
    agent { label 'docker-centos7' }

    environment {
        JAVA_HOME = tool '1.8.0_144'
        TEST_DONE = false
        artifactory_contextUrl = "https://artifactory.groupe.pharmagest.com"
    }

    stages {
        stage("Prepare") {
            steps {
                checkout scm
                script {
                    def publish = ['develop', 'release', 'master'].contains(BRANCH_NAME.toLowerCase())
                    def release = ['release', 'master'].contains(BRANCH_NAME.toLowerCase())
                    def version = sh(script: "./gradlew properties -q -Prelease=${release} | grep \"version:\"", returnStdout: true).replace('version:', '').trim().replace('\n', '')
                    env.GROUP = sh(script: "./gradlew properties -q -Prelease=${release} | grep \"group:\"", returnStdout: true).replace('group:', '').trim().replace('\n', '')
                    env.BRANCH = BRANCH_NAME.toLowerCase()
                    env.ARTIFACT = sh(script: "./gradlew properties -q -Prelease=${release} | grep \"name:\"", returnStdout: true).replace('name:', '').trim().replace('\n', '')
                    env.VERSION = version
                    env.PUBLISH = publish
                    env.RELEASE = release
                    env.ATF_TARGET = "maven-kapelse-" + (release ? "release" : "snapshot")
                    currentBuild.result = "SUCCESS"
                    currentBuild.description = "${env.ARTIFACT}-${version}-${env.BUILD_NUMBER}"
                }
                withCredentials([usernamePassword(credentialsId: 'jenkins_ldap', usernameVariable: 'artifactory_username', passwordVariable: 'artifactory_password')]) {
                    script {
                        env.GRADLE_ARGS="-Partifactory_contextUrl=${artifactory_contextUrl} -Partifactory_username=${artifactory_username} -Partifactory_password=${artifactory_password}"
                    }
                }
            }
        }

        stage("Assemble") {
            steps {
                configFileProvider([configFile(fileId: 'init.gradle', variable: 'INIT_GRADLE')]) {
                    sh "./gradlew ${GRADLE_ARGS} --init-script ${INIT_GRADLE} clean assemble -Drelease=${env.RELEASE}"
                }
            }
        }

        stage("Test") {
            steps {
                configFileProvider([configFile(fileId: 'init.gradle', variable: 'INIT_GRADLE')]) {
                    sh "./gradlew ${GRADLE_ARGS} --init-script ${INIT_GRADLE} test -Prelease=${env.RELEASE}"
                }
            }
            post {
                always {
                    //make the junit test results available in any case (success & failure)
                    script {
                        try {
                            junit '**/build/test-results/test/*.xml'
                            env.TEST_DONE = true
                        } catch (ex) {
                            unstable('No test to run - ' + ex.getMessage())
                        }
                    }
                }
            }
        }

        stage("Build") {
            when {
                // only main branches
                expression { return env.PUBLISH }
            }
            steps {
                configFileProvider([configFile(fileId: 'init.gradle', variable: 'INIT_GRADLE')]) {
                    sh "./gradlew ${GRADLE_ARGS} --init-script ${INIT_GRADLE} build -Prelease=${env.RELEASE}"
                }
            }
        }

        stage("Sonar") {
            when {
                // only main branches
                expression { return env.PUBLISH }
            }
            steps {
                withSonarQubeEnv('SonarCom') {
                    configFileProvider([configFile(fileId: 'init.gradle', variable: 'INIT_GRADLE')]) {
                        script {
                            try {
                                def command = [
                                        "./gradlew ${GRADLE_ARGS} --init-script ${INIT_GRADLE} sonarqube -Prelease=${env.RELEASE}",
                                        "-Dsonar.java.binaries=./build/classes/java/main",
                                        "-Dsonar.java.test.binaries=./build/classes/java/test",
                                        "-Dsonar.projectKey=kapelse:${env.ARTIFACT}",
                                        "-Dsonar.projectName=kapelse/${env.ARTIFACT}",
                                        "-Dsonar.projectVersion=${env.VERSION}",
                                        "-Dsonar.branch.name=${BRANCH_NAME}",
                                        "-Dsonar.junit.reportsPath=./build/reports"
                                ]
                                command += "|| exit 0"
                                sh command.join(' ')
                            } catch (ex) {
                                unstable('Unable to run sonarqube analysis - ' + ex.getMessage())
                            }
                        }
                    }
                }
            }
        }

        stage("Publish") {
            when {
                // only main branches
                expression { return env.PUBLISH }
            }
            steps {
                script {
                    def server = Artifactory.server 'artifactory'
                    GString target = "${env.ATF_TARGET}/${env.GROUP.replaceAll('\\.', '/')}/${env.ARTIFACT}/${env.VERSION}/"
                    GString uploadSpec = """{
                        "files": [
                            {
                                "pattern": "build/libs/*${env.VERSION}.jar",
                                "target": "${target}",
                                "recursive": true,
                                "flat" : true
                            },
                            {
                                "pattern": "build/libs/*${env.VERSION}.pom",
                                "target": "${target}",
                                "recursive": true,
                                "flat" : true
                            }
                        ]
                    }"""
                    def buildInfo = server.upload uploadSpec
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', allowEmptyArchive: true
            script {
                deleteDir()
            }
        }
    }

}
