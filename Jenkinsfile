pipeline {
    agent any  // runs checkout on any available agent

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"
        DOCKER_BUILDKIT = '1'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/rab-tab/movie-booking-kafka-saga.git'
            }
        }

        stage('Build Maven Reactor (Docker)') {
            agent { label 'docker' } // ensures this runs on Docker-enabled node
            steps {
                sh '''
                  docker run --rm \
                    -v $HOME/.m2:/root/.m2 \
                    -v $PWD:/app \
                    -w /app \
                    maven:3.9.5-eclipse-temurin-21 \
                    mvn -T 1C clean package -DskipTests
                '''
            }
        }

        stage('Docker Login') {
            agent { label 'docker' } // runs on Docker-enabled node
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
            agent { label 'docker' } // runs on Docker-enabled node
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
        always {
            agent { label 'docker' } // ensures logout runs on Docker node
            sh 'docker logout'
        }
    }
}
