pipeline {
    agent any

    environment {
        SERVICE_NAME = "reporting-v2"
        BUILD_IMAGE_LINK = "gcr.io/qordoba-devel/qordoba-build"
        ARTIFACTORY_DEPLOYER_CONFIG = credentials('qordobaArtifactoryDeployerCredentials')
        ARTIFACTORY_READER_CONFIG = credentials('qordobaArtifactoryReaderCredentials')
        GIT_EMAIL = "jenkins.reporting-sql-progress@qordoba.com"
        GIT_CREDENTIALS = "not needed cause no Q libs"
    }

    stages {

        stage('update to latest lib versions for this build') {
            when {
                allOf {branch "develop"; branch "ENG-1126-velocity-v2"; expression { return false } }
            }

            steps {
                // develop must auto-magically stay on latest library versions
                // ... will only 'commit' these version back to the development integration branch if this test build succeeds
                // Not all builds needs this; but consistency is easier
                sh("cp /var/lib/jenkins/workspace/Develop/library.utils/project/release.version-minor             \$(pwd)/project/utils.version-minor")
                sh("cp /var/lib/jenkins/workspace/Develop/library.utilstm/project/release.version-minor           \$(pwd)/project/saas-tm.version-minor")
                sh("cp /var/lib/jenkins/workspace/Develop/library.utilspubsub/project/release.version-minor       \$(pwd)/project/pubsub.version-minor")
                sh("cp /var/lib/jenkins/workspace/Develop/service.reporting/project/release.version-minor         \$(pwd)/project/reporting.version-minor")
            }
        }

        stage('build and push image') {

            when {
                anyOf { branch "develop"; branch 'test'; branch 'master' }
            }

            steps {

                checkout scm

                //http://jpetazzo.github.io/2015/09/03/do-not-use-docker-in-docker-for-ci/
                //This looks like Docker-in-Docker, feels like Docker-in-Docker, but it’s not Docker-in-Docker: (which is a good thing)

                sh '''
                 echo "Retrieving artifactory credential files"
                 cp "${ARTIFACTORY_DEPLOYER_CONFIG}" "/var/lib/jenkins/.ivy2/.qordobaArtifactoryDeployerCredentials"
                 chmod 600 /var/lib/jenkins/.ivy2/.qordobaArtifactoryDeployerCredentials
                 cp "${ARTIFACTORY_READER_CONFIG}" "/var/lib/jenkins/.ivy2/.qordobaArtifactoryReaderCredentials"
                 chmod 600 /var/lib/jenkins/.ivy2/.qordobaArtifactoryReaderCredentials

                 docker run                                    \
                  --rm                                         \
                   -v $(pwd):/code                             \
                   -v /var/lib/jenkins:/root                   \
                   -v /var/run/docker.sock:/var/run/docker.sock -i ${BUILD_IMAGE_LINK}

                 echo "Pushing to google container registry"
               '''

                script {
                    def PROJECT = ""
                    if (env.BRANCH_NAME == "develop" || env.BRANCH_NAME ==  "ENG-1126-velocity-v2") {
                        PROJECT =  "qordoba-devel"
                    } else if (env.BRANCH_NAME == "test") {
                        PROJECT =  "strange-cosmos-822"
                    } else if (env.BRANCH_NAME == "master") {
                        PROJECT =  "qordoba-prod"
                    }


                    def GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    def SHORT_COMMIT = GIT_COMMIT.take(6)

                    //Some build versions are read from a file which increase after the build process
                    //In which case this is replaced with a line of script; not a static number


                    def VERSION = "2.0"

                    echo "PROJECT: ${PROJECT}"

                    // Was getting errors with variables in ''' multi-line strings
                    sh("docker tag ${SERVICE_NAME}:${VERSION} gcr.io/${PROJECT}/${SERVICE_NAME}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}_${SHORT_COMMIT}")

                    sh("gcloud docker -- push gcr.io/${PROJECT}/${SERVICE_NAME}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}_${SHORT_COMMIT}")

                    sh("docker rmi ${SERVICE_NAME}:${VERSION}")
                    sh("docker rmi gcr.io/${PROJECT}/${SERVICE_NAME}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}_${SHORT_COMMIT}")
                }
            }
        }

        stage('push back to repo to update all future builds to latest libs') {
            when {
                allOf {branch "develop"; expression { return false }}
            }

            steps {

                sh '''echo "Commit versions"
                  git config push.default simple
                  git config user.name "Jenkins"
                  git config user.email "${GIT_EMAIL}"
                  git add project/*.version-minor
                  git commit -m 'Version update'
                '''

                script {
                    println "Committing version update"
                    sshagent([env.GIT_CREDENTIALS]) {
                        sh "git push origin HEAD:refs/heads/${BRANCH_NAME}"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Removing workspace"
            deleteDir()
            // Remove dangling images till we stop producing them
            sh("docker image prune -f")
        }
        success {
            echo 'This will run only if succeeded'
        }
        failure {
            echo 'This will run only if failed'
        }
        unstable {
            echo 'This will run only if the run was marked as unstable'
        }
        changed {
            echo 'This will run only if the state of the Pipeline has changed'
            echo 'For example, if the Pipeline was previously failing but is now successful'
        }
    }
}