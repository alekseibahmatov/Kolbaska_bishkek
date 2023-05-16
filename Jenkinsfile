pipeline {
    agent { label 'Slavik' }
    stages {
        stage('Stop stack') {
            steps {
                script {
                    try {
                        sh 'docker stop db'
                        sh 'docker stop app'
                    } catch (Exception e) {
                        echo "Error: Failed to stop the stack, but continuing the pipeline."
                        echo "Error message: ${e.getMessage()}"
                    }
                }
            }
        }
        stage('Clear docker') {
            steps {
                sh 'docker system prune -af'
            }
        }
        stage('Delete files') {
            steps {
                sh 'rm Dockerfile'
            }
        }
        stage('Copy new files') {
            steps {
                sh 'cp /var/jenkins/infra/Dockerfile_backend_dev Dockerfile'
                sh 'cp /var/jenkins/infra/.env_backend .env_backend'
                sh 'cp /var/jenkins/infra/.env_database .env_database'
            }
        }
        stage('Build and start stack') {
            steps {
                sh 'docker network create stack-network'
                sh 'docker volume create stack-db-storage'
                sh 'docker run --env-file .env_database --network stack-network -v stack-db-storage:/var/lib/mysql -p 3306:3306 --name db -d mysql:8.0.33'
                sh 'docker build . -t stack-app'
                sh 'docker run --env-file .env_backend --network stack-network -p 8080:8080 --name app -d stack-app'
                cleanWs()
            }
        }
    }

    post {
        always {
            echo "Pipeline completed successfully"
        }
    }
}
