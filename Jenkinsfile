pipeline {
    agent any
    options { timestamps(); disableConcurrentBuilds() }
    environment {
        IMAGE_REPO = credentials('country-info-image-repo') // e.g. docker.io/myuser/country-info
        IMAGE_TAG = "${BUILD_NUMBER}"
        NAMESPACE = 'country-info'
    }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Build and test') { steps { sh 'mvn -B clean verify' } }
        stage('Build image') {
            steps { sh 'docker build --pull -t "$IMAGE_REPO:$IMAGE_TAG" -t "$IMAGE_REPO:latest" .' }
        }
        stage('Push image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                    sh '''set +x
                      echo "$REGISTRY_PASSWORD" | docker login -u "$REGISTRY_USER" --password-stdin
                      docker push "$IMAGE_REPO:$IMAGE_TAG"
                      docker push "$IMAGE_REPO:latest"
                      docker logout
                    '''
                }
            }
        }
        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                withCredentials([file(credentialsId: 'country-info-kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                      export KUBECONFIG="$KUBECONFIG_FILE"
                      kubectl apply -f k8s/namespace.yaml
                      kubectl apply -f k8s/configmap.yaml
                      kubectl apply -f k8s/deployment.yaml
                      kubectl -n "$NAMESPACE" set image deployment/country-info country-info="$IMAGE_REPO:$IMAGE_TAG"
                      kubectl apply -f k8s/service.yaml
                      kubectl -n "$NAMESPACE" rollout status deployment/country-info --timeout=180s
                    '''
                }
            }
        }
    }
    post {
        always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
        success { echo "Build completed. Image: ${IMAGE_REPO}:${IMAGE_TAG}" }
    }
}