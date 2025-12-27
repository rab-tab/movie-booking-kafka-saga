pipeline {
    agent any

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        DOCKER_REGISTRY = 'rabtab' // your Docker Hub username
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
            }
        }

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKER_HUB_CREDENTIALS}",
                                                 usernameVariable: 'DOCKER_USER',
                                                 passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Build & Push Booking Service') {
            steps {
                dir('booking-service') {
                    sh 'docker build -t $DOCKER_REGISTRY/booking-service:$IMAGE_TAG .'
                    sh 'docker push $DOCKER_REGISTRY/booking-service:$IMAGE_TAG'
                }
            }
        }

        stage('Build & Push Seat Inventory Service') {
            steps {
                dir('seat-inventory-service') {
                    sh 'docker build -t $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG .'
                    sh 'docker push $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG'
                }
            }
        }

        stage('Build & Push Payment Service') {
            steps {
                dir('payment-service') {
                    sh 'docker build -t $DOCKER_REGISTRY/payment-service:$IMAGE_TAG .'
                    sh 'docker push $DOCKER_REGISTRY/payment-service:$IMAGE_TAG'
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up local Docker images'
            sh 'docker logout'
        }
        success {
            echo "Pipeline completed successfully. Images pushed with tag: $IMAGE_TAG"
        }
        failure {
            echo "Pipeline failed."
        }
    }
}
