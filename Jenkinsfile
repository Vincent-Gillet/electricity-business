pipeline {
    agent any
    tools {
        nodejs 'node'
        maven 'mvn'
    }
    environment {
        SONAR_PROJECT_KEY = 'Vincent-Gillet_electricity-business'
        SONAR_ORGANIZATION = 'critik-sonar'
        SONAR_HOST_URL = 'https://sonarcloud.io'
        SONAR_LOGIN = credentials('sonar-token')

        DOCKER_REGISTRY = 'vincentgillet12'
        DOCKER_REGISTRY_IMAGE = 'docker.io/vincentgillet12'
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
                sh 'docker login docker.io -u ${DOCKER_REGISTRY} -p ${DOCKER_REGISTRY_CREDENTIALS}'
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
            echo 'Pipeline succeeded! Angular: https://electricity-business-angular-app-4eyi.onrender.com/, Spring: https://electricity-business-spring-app.onrender.com/'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}