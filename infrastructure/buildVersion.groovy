pipeline {
    agent any
    options {
        timestamps()
    }
    stages {
        stage('Build and deploy') {
            steps {
                configFileProvider([configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh("./mvnw -s ${MAVEN_SETTINGS} --no-transfer-progress -B deploy -DskipTests -DaltDeploymentRepository=${env.ALT_DEPLOYMENT_REPOSITORY_STAGING}")
                }
                withCredentials([string(credentialsId: 'github-api', variable: 'GITHUB_API_TOKEN')]) {
                    sh "./infrastructure/upload-github-release-asset.sh github_api_token=$GITHUB_API_TOKEN tag=${params.version} filename=./community/target/bonita-super-admin-application-${params.version}.bos"
                    sh "./infrastructure/upload-github-release-asset.sh github_api_token=$GITHUB_API_TOKEN tag=${params.version} filename=./subscription/target/bonita-super-admin-application-sp-${params.version}.bos"
                }
            }
        }
    }
}
