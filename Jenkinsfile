pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'walker823/teedy'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_BUILDKIT = '0'
    }

    stages {
        stage('Build Maven') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                docker build \
                  -t "${DOCKER_IMAGE}:${DOCKER_TAG}" \
                  -t "${DOCKER_IMAGE}:latest" \
                  .
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    docker push "${DOCKER_IMAGE}:${DOCKER_TAG}"
                    docker push "${DOCKER_IMAGE}:latest"
                    docker logout
                    '''
                }
            }
        }

        stage('Run Containers') {
            steps {
                sh '''
                for port in 8082 8083 8084; do
                  name="teedy-container-${port}"
                  docker rm -f "$name" 2>/dev/null || true
                  docker run -d --name "$name" -p "${port}:8080" "${DOCKER_IMAGE}:${DOCKER_TAG}"
                done

                docker ps --filter "name=teedy-container"
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true, allowEmptyArchive: true
            archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true, allowEmptyArchive: true
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}
