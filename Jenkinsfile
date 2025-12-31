pipeline {
    agent { label 'docker' } // Docker-enabled node
    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_BUILDKIT = '1'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
            }
        }

        stage('Build Maven Reactor (Once)') {
            steps {
                sh 'mvn -T 1C clean package -DskipTests'
            }
        }

        stage('Build & Push Docker Images') {
            steps {
                script {
                    // Wrap all docker builds/pushes in the Jenkins Docker Registry credentials context
                    withDockerRegistry([credentialsId: "${DOCKER_HUB_CREDENTIALS}", url: 'https://index.docker.io/v1/']) {
                        parallel(
                            'Booking Service': {
                                sh """
                                    docker build -t $DOCKER_REGISTRY/booking-service:$IMAGE_TAG booking-service
                                    docker push $DOCKER_REGISTRY/booking-service:$IMAGE_TAG
                                """
                            },
                            'Seat Inventory Service': {
                                sh """
                                    docker build -t $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG seat-inventory-service
                                    docker push $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG
                                """
                            },
                            'Payment Service': {
                                sh """
                                    docker build -t $DOCKER_REGISTRY/payment-service:$IMAGE_TAG payment-service
                                    docker push $DOCKER_REGISTRY/payment-service:$IMAGE_TAG
                                """
                            }
                        )
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout'
        }
    }
}
