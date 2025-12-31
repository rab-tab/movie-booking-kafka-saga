pipeline {
    agent { label 'docker' } // Top-level Docker-enabled node
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

        stage('Docker Login') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_HUB_CREDENTIALS,
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                }
            }
        }

        stage('Build & Push Docker Images') {
            parallel {
                stage('Booking Service') {
                    steps {
                        sh '''
                          docker build -t $DOCKER_REGISTRY/booking-service:$IMAGE_TAG booking-service
                          docker push $DOCKER_REGISTRY/booking-service:$IMAGE_TAG
                        '''
                    }
                }

                stage('Seat Inventory Service') {
                    steps {
                        sh '''
                          docker build -t $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG seat-inventory-service
                          docker push $DOCKER_REGISTRY/seat-inventory-service:$IMAGE_TAG
                        '''
                    }
                }

                stage('Payment Service') {
                    steps {
                        sh '''
                          docker build -t $DOCKER_REGISTRY/payment-service:$IMAGE_TAG payment-service
                          docker push $DOCKER_REGISTRY/payment-service:$IMAGE_TAG
                        '''
                    }
                }
            }
        }
    }

    post {
        always { sh 'docker logout' }
    }
}
