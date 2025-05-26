def call(Map configMap){
    pipeline {
        agent {
            label 'AGENT-1'
        }
        options{
            timeout(time: 30, unit: 'MINUTES')
            disableConcurrentBuilds()
            //retry(1)
        }
        parameters{
            booleanParam(name: 'deploy', defaultValue: false, description: 'Select to deploy or not')
        }
        environment {
            appVersion = '' // this will become global, we can use across pipeline
            region = 'us-east-1'
            account_id = '905418111046'
            project = configMap.get("project")
            environment = 'dev'
            component = configMap.get("component")
        }

        stages {
            stage('Read the version') {
                steps {
                    script{
                        def packageJson = readJSON file: 'package.json'
                        appVersion = packageJson.version
                        echo "App version: ${appVersion}"
                    }
                }
            }
            stage('Install Dependencies') {
                steps {
                    sh 'npm install'
                }
            }
            /* stage('SonarQube analysis') {
                environment {
                    SCANNER_HOME = tool 'sonar-6.0' //scanner config
                }
            } */

            stage('Docker build'){
                steps{

                    sh """
                        aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.${region}.amazonaws.com
                        docker build -t ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion} .
                        docker push ${account_id}.dkr.ecr.${region}.amazonaws.com/${project}-${component}:${appVersion}
                    """
                }
            }

        stage('Deploy'){
            steps{
                sh """
                    aws eks update-kubeconfig --region us-east-1 --name expense-dev
                    cd helm
                    sed -i 's/IMAGE_VERSION/${appVersion}/g' values.yaml
                    helm install ${component} -n expense .
                """
            }
        }


            // stage('Verify Deployment'){
            //     steps{
            //         script{
            //             rollbackStatus = sh(script: "kubectl rollout status deployment/backend -n ${project} --timeout=1m || true", returnStdout: true).trim()
            //             if(rollbackStatus.contains('successfully rolled out')){
            //                 echo "Deployment is successfull"
            //             }
            //             else{
            //                 echo "Deployment is failed, performing rollback"
            //                 if(releaseExists.isEmpty()){
            //                     error "Deployment failed, not able to rollback, since it is first time deployment"
            //                 }
            //                 else{
            //                     sh """
            //                     aws eks update-kubeconfig --region ${region} --name ${project}-dev
            //                     helm rollback --install backend -n ${project} 0
            //                     sleep 60
            //                     """
            //                     rollbackStatus = sh(script: "kubectl rollout status deployment/backend -n expense --timeout=2m || true", returnStdout: true).trim()
            //                     if(rollbackStatus.contains('successfully rolled out')){
            //                         error "Deployment is failed, Rollback is successfull"
            //                     }
            //                     else{
            //                         error "Deployment is failed, Rollback is failed"
            //                     }
            //                 }
            //             }
            //         }
            //     }
            // }
            
            /* stage('Nexus Artifact Upload'){
                steps{
                    script{
                        nexusArtifactUploader(
                            nexusVersion: 'nexus3',
                            protocol: 'http',
                            nexusUrl: "${nexusUrl}",
                            groupId: 'com.expense',
                            version: "${appVersion}",
                            repository: "backend",
                            credentialsId: 'nexus-auth',
                            artifacts: [
                                [artifactId: "backend" ,
                                classifier: '',
                                file: "backend-" + "${appVersion}" + '.zip',
                                type: 'zip']
                            ]
                        )
                steps {
                    // sonar server injection
                    withSonarQubeEnv('sonar-6.0') {
                        sh '$SCANNER_HOME/bin/sonar-scanner'
                        //generic scanner, it automatically understands the language and provide scan results
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    timeout(time: 5, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            } */
            // stage('Docker build') {
                
            //     steps {
            //         withAWS(region: 'us-east-1', credentials: "aws-creds-${environment}") {
            //             sh """
            //             aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${account_id}.dkr.ecr.us-east-1.amazonaws.com

            //             docker build -t ${account_id}.dkr.ecr.us-east-1.amazonaws.com/${project}/${environment}/${component}:${appVersion} .

            //             docker images

            //             docker push ${account_id}.dkr.ecr.us-east-1.amazonaws.com/${project}/${environment}/${component}:${appVersion}
            //             """
            //         }
            //     }
            // }
            // stage('Deploy'){
            //     when{
            //         expression {params.deploy}
            //     }

            //     steps{
            //         build job: "../${component}-cd", parameters: [
            //             string(name: 'version', value: "$appVersion"),
            //             string(name: 'ENVIRONMENT', value: "dev"),
            //         ], wait: true
            //     }
            // }
        }

        post {
            always{
                echo "This sections runs always"
                deleteDir()
            }
            success{
                echo "This section run when pipeline success"
            }
            failure{
                echo "This section run when pipeline failure"
            }
        }
    }
}