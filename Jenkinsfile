pipeline {
    agent { label 'docker' }

    environment {
        DOCKER_HUB_CREDENTIALS = 'dockerhub-creds'
        DOCKER_REGISTRY = 'rabtab'
        IMAGE_TAG = "${BUILD_NUMBER}"

        // Use a separate docker config folder to avoid macOS keychain
        DOCKER_CONFIG = "/Users/rabia/.docker-jenkins"
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
                withCredentials([usernamePassword(
                    credentialsId: DOCKER_HUB_CREDENTIALS,
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        # Remove old config to avoid macOS keychain issues
                        rm -rf $DOCKER_CONFIG
                        mkdir -p $DOCKER_CONFIG

                        # Create empty config.json so Docker does not use osxkeychain
                        echo '{"auths":{}}' > $DOCKER_CONFIG/config.json

                        # Headless login using credentials from Jenkins
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                        # Build & push each service
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
            // Logout Docker (safe even if login failed)
            sh '''
                docker logout || true
                rm -rf $DOCKER_CONFIG
            '''
        }
    }
}
