pipeline {

    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
                    steps {
                        git branch: 'main',
                            url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
                    }
                }

        stage('Build Maven Reactor') {
            steps {
                sh 'mvn -T 1C clean package -DskipTests'
            }
        }

        stage('Docker Login & Build/Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: DOCKER_HUB_CREDENTIALS,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        set -e

                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                        docker build -t $DOCKER_REGISTRY/booking-service:$IMAGE_TAG booking-service
                        docker push $DOCKER_REGISTRY/booking-service:$IMAGE_TAG

                        docker build -t $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG seat-inventory-service
                        docker push $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG

                        docker build -t $DOCKER_REGISTRY/payment-service:$IMAGE_TAG payment-service
                        docker push $DOCKER_REGISTRY/payment-service:$IMAGE_TAG
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
