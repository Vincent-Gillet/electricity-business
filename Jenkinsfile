pipeline {
    agent any
    tools {
        nodejs 'node'
        maven 'mvn'
    }
    environment {
        SONAR_PROJECT_KEY = credentials('sonar-project-key')
        SONAR_ORGANIZATION = credentials('sonar-organization')
        SONAR_HOST_URL = credentials('sonar-url')
        SONAR_LOGIN = credentials('sonar-token')

        DOCKER_REGISTRY = credentials('docker-hub-username')
        DOCKER_REGISTRY_IMAGE = credentials('docker-hub-imageUrl')
        DOCKER_REGISTRY_CREDENTIALS = credentials('docker-hub-credentials')

        RENDER_API_TOKEN = credentials('render-api-token')
        RENDER_SERVICE_ID_ANGULAR = credentials('render-service-id-angular')
        RENDER_SERVICE_ID_SPRING = credentials('render-service-id-spring')
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Vincent-Gillet/electricity-business.git'
            }
        }
        stage('Build Spring Boot') {
            steps {
                dir('api') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                dir('api') {
                    sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.organization=${SONAR_ORGANIZATION} \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.login=${SONAR_LOGIN}
                    '''
                }
            }
        }
        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_REGISTRY_CREDENTIALS_PSW | docker login docker.io -u $DOCKER_REGISTRY_CREDENTIALS_USR --password-stdin'
            }
        }
        stage('Build and Push Docker Images') {
            steps {
                script {
                    def services = [
                        [name: 'spring-app', path: './api', dockerfile: 'production.Dockerfile'],
                        [name: 'angular-app', path: './client', dockerfile: 'production.Dockerfile']
                    ]
                    services.each { svc ->
                        sh """
                            docker build --platform=linux/amd64 -t $DOCKER_REGISTRY/electricity-business-${svc.name}:$BUILD_NUMBER -t $DOCKER_REGISTRY/electricity-business-${svc.name}:latest -f ${svc.path}/${svc.dockerfile} ${svc.path}
                            docker push $DOCKER_REGISTRY/electricity-business-${svc.name}:$BUILD_NUMBER
                            docker push $DOCKER_REGISTRY/electricity-business-${svc.name}:latest
                        """
                    }
                }
            }
        }
        stage('Deploy to Render') {
            steps {
                script {
                    def renderServices = [
                        [name: 'angular-app', id: env.RENDER_SERVICE_ID_ANGULAR],
                        [name: 'spring-app', id: env.RENDER_SERVICE_ID_SPRING]
                    ]
                    renderServices.each { svc ->
                        sh """
                            curl --request POST \
                            --url "https://api.render.com/v1/services/${svc.id}/deploys" \
                            --header 'accept: application/json' \
                            --header "authorization: Bearer $RENDER_API_TOKEN" \
                            --header 'content-type: application/json' \
                            --data '{\"clearCache\": \"clear\", \"imageUrl\": \"$DOCKER_REGISTRY_IMAGE/electricity-business-${svc.name}:latest\"}'
                        """
                    }
                }
            }
        }
    }
    post {
        success {
            echo 'Pipeline succeeded! Angular: https://electricity-business-angular-app-4eyi.onrender.com/, Spring: https://electricity-business-spring-app-254.onrender.com/'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}