#!groovy​
import groovy.json.JsonOutput

def isMaster = (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "foobar")
def isPR = !!env.CHANGE_ID
def changeSetAuthors = []
def iosDeviceId = "2B105616-9A36-40B6-B142-A7F44DE3FF5A"//iPhone 11 Pro 14.0
def iosDeviceId2 = "50F9D4B8-8190-4535-83F3-8817E86A0BCD"//iPhone 11 Pro Max 14.0
def isRelease = false

def releaseDescription = "A build summary was not supplied"

// Used for master build deployments
def version = "0.0.0"
def buildNumber = "0"
def lastRelease
def lastReleaseTag
def lastReleaseCommit
def changeListSinceLastRelease
isUpgradeMaster =false
isUpgradeBranch=false
count =0
message =''
channel=''

red='FF0000'
yellow= 'FFFF00'
green='008000'


if(!isMaster){
    def envVariable =  env.CHANGE_BRANCH !=null ? env.CHANGE_BRANCH : ''
    isUpgradeBranch = envVariable.toLowerCase().contains("flutter_upgrade")
}

// Remove concurrent for master, since master runs simulators
if (isMaster) {
    properties([disableConcurrentBuilds()])
}

checkAuthor(isMaster,isPR)

pipeline {
    agent any
    parameters {
        booleanParam(
                name: 'Unit_Tests',
                defaultValue: true,
                description: 'Do you want to run the build with unit tests?'
        )
        booleanParam(
                name: 'Integration_Tests',
                defaultValue: true,
                description: 'Do you want to run the build with integration tests?'
        )
        booleanParam(
                name: 'Binaries',
                defaultValue: true,
                description: 'Do you want to run the build with Binaries?'
        )
        booleanParam(
                name: 'Reports',
                defaultValue: true,
                description: 'Do you want to run the build with Reports?'
        )
    }
    stages {
        stage('Environment') {
            steps {
                echo  "Unit Tests : ${params.Unit_Tests}"
                echo  "Integration Tests: ${params.Integration_Tests}"
                echo  "Binaries: ${params.Binaries}"
                echo  "Reports: ${params.Reports}"
            }
        }
    }
}


try {
    stage("Preparation") {
        printStage "Preparation"
        node {
            if (env.BRANCH_NAME.contains('PR-')) {
                withEnv(['BUILD_ID=dontKillMe', 'JENKINS_NODE_COOKIE=dontKillMe']) {
                    try {
                        def status = sh (script: "xcrun simctl boot ${iosDeviceId} && open /Applications/Xcode.app/Contents/Developer/Applications/Simulator.app/", returnStdout: true)

                        echo status
                    }catch (e) {
                        echo "Device 1 has already started :)"
                    }

                    try {
                        def status2 = sh (script: "xcrun simctl boot ${iosDeviceId2} && open /Applications/Xcode.app/Contents/Developer/Applications/Simulator.app/", returnStdout: true)

                        echo status2
                    }catch (e) {
                        echo "Device 2 has already started"
                    }
                }

                def devices = sh (script: "flutter devices", returnStdout: true)
                echo devices
            }



            sh "pwd"

            // First we ensure a clean slate :)
            deleteDir()

            // Download the code from GitHub
            checkout scm

            ansiPrint "Environment Variables: ${prettyPrint(sh(script: "env", returnStdout: true).trim())}"

            changeSetAuthors = getChangeSetAuthors()

            if (isMaster) {
                try {
                    def status = sh (script: "docker start MobSF", returnStdout: true)
                    echo status
                }catch (e) {
                    echo "MobSF starting with docker had some issue"
                }

                withEnv(['BUILD_ID=dontKillMe', 'JENKINS_NODE_COOKIE=dontKillMe']) {
                    //sh 'flutter emulators --launch Pixel_2_API_28 || echo "Failed to launch... maybe already running?"'
                    try {
                        sh "killall \"Simulator\" || echo \"No Simulators found to kill\" && xcrun simctl erase all && xcrun simctl boot ${iosDeviceId} && open /Applications/Xcode.app/Contents/Developer/Applications/Simulator.app/"
                    } catch(e) {
                        sh "killall \"Simulator\" || echo \"No Simulators found to kill\" && xcrun simctl erase all && xcrun simctl boot ${iosDeviceId} && open /Applications/Xcode.app/Contents/Developer/Applications/Simulator.app/"
                    }
                }

                ansiPrint "{lightblue} Cleaning Xcode Derived Data"
                sh "rm -rf $HOME/Library/Developer/Xcode/DerivedData"

                def fullVersion = sh(
                        script: "grep 'version:' pubspec.yaml | awk '{ print \$2 }' | tr -d '\\n'",
                        returnStdout: true
                )
                version = fullVersion.split("\\+")[0]
                buildNumber = fullVersion.split("\\+")[1]

                ansiPrint "{magenta}Current Version: ${fullVersion}, Build: ${buildNumber}"

                // We grab the sha of the last release, in order to get all changes since then
                lastRelease = githubAPICall(url: 'https://github.nwie.net/api/v3/repos/Nationwide/rp-mobile/releases/latest')
                def lastReleaseTagRef = githubAPICall(url: "https://github.nwie.net/api/v3/repos/Nationwide/rp-mobile/git/refs/tags/${lastRelease.tag_name}")
                lastReleaseTag = githubAPICall(url: lastReleaseTagRef.object.url)
                lastReleaseCommit = lastReleaseTag.sha

                // If GitHub does not have this release build...
                if ("v${fullVersion}" != lastRelease.tag_name) {
                    ansiPrint "{magenta}This version ${fullVersion} does not exist in GitHub... This build has been marked for release."
                    isRelease = true
                } else {
                    ansiPrint "{lightblue}Build already in GitHub releases."
                }

                sh "mkdir -p dist && cd dist && git log ${lastReleaseCommit}..HEAD --reverse --pretty=format:'- [%s](https://github.nwie.net/Nationwide/rp-mobile/commit/%H)' | grep -v 'ci skip' >> release.md || echo - No changes found >> release.md"
                changeListSinceLastRelease = readFile("dist/release.md")
                ansiPrint "{bgBlack}{green}Changes for the Release:\n${changeListSinceLastRelease}"
            }

            stash name: 'source'
        }

    }

    if(params.Unit_Tests) {
        stage("Analysis/Unit Tests") {
            printStage "Analysis/Unit Tests"

            parallel(
                    failFast: true,
                    "Code Analysis": {
                        node {
                            sh "pwd"
                            deleteDir()
                            unstash 'source'
                            sh "${flutter(EXECUTOR_NUMBER)} analyze"
                        }
                    },
                    "Unit Tests": {
                        node {
                            sh "pwd"
                            deleteDir()
                            unstash 'source'

                            if (isMaster || isPR) {
                                sh "${flutter(EXECUTOR_NUMBER)} clean"
                                retryUnitTests("--coverage")
                                sh "pwd"
                                sh "python3 pipeline/lib/lcov_cobertura.py coverage/lcov.info --output coverage/coverage.xml"
                                cobertura autoUpdateHealth: false, autoUpdateStability: false, classCoverageTargets: '90, 0, 0', coberturaReportFile: 'coverage/coverage.xml', conditionalCoverageTargets: '70, 0, 0', lineCoverageTargets: '80, 0, 0', maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
                            } else {
                                retryUnitTests('')
                            }
                        }
                    }
            )
        }
    }



    if ((env.BRANCH_NAME.contains('PR-') || isMaster) && params.Integration_Tests) {
        stage("Integration Tests") {
            printStage "Integration Tests"

            node {
                sh "pwd"
                deleteDir()
                unstash 'source'

                counter = 0
                testDeviceID = ""
                try {
                    while (true) {
                        def availableDevice = sh(script: "bash IntegrationTestAgent.sh", returnStdout: true)
                        if (!availableDevice.trim().equals("-1")) {
                            sh "touch ~/Desktop/lockFiles/${availableDevice.trim()}.integrationLock"
                            testDeviceID = "${availableDevice.trim()}"
                            break
                        }
                        counter++
                        if (counter > 100) {
                            message = "${env.BRANCH_NAME} was unable to run integration: devices were busy for too long."
                            sendTeamsMessage(message, channel, yellow)
                        }
                        sleep(20)
                    }

                }catch(e) {
                    throw e
                }

                def driverTests = sh(
                        script: "find ./test_driver -ipath '*_test.dart' | sed 's/^\\.\\/test_driver\\///' | tr '\\n' ',' | sed 's/,\$//'",
                        returnStdout: true
                ).split(',').collectMany { [it.replace('_test', '')] }

                ansiPrint "{lightblue}---DRIVER TESTS---\n {lightblue}${driverTests}"

                // Only place UNSTABLE or FAILURE HERE
                def unstableDriverResults = [
                        'forgot_username_password.dart'
                ]
                def skippedDrivers = [
                        'app_screenshots.dart'
                ]

                withEnv(['BUILD_ID=dontkill', 'JENKINS_NODE_COOKIE=dontkill']) {
                    attemptUnsuccesful = false
                    buildOption = ''
                    counter = 0
                    driverTests.each {
                        counter++

                        ansiPrint "{red} Test ${counter} of ${driverTests.size()}"
                        if (skippedDrivers.contains(it)) return

                        ansiPrint "{lightblue} Running integration test: ${it}"

                        if (it.contains('rebuild_')) {
                            buildOption = ''
                        }

                        attemptUnsuccesful = false

                        try {
                            sh "${flutter(EXECUTOR_NUMBER)} drive -d ${testDeviceID} ${buildOption} --target=test_driver/${it}"
                        } catch (e) {
                            //Lets give the integration test another chance
                            attemptUnsuccesful = true
                        }
                        if (attemptUnsuccesful) {
                            try {
                                ansiPrint "{bgred}{white}Retrying integration test"
                                sh "${flutter(EXECUTOR_NUMBER)} drive -d ${testDeviceID} ${buildOption} --target=test_driver/${it}"
                            } catch (e) {
                                // Temporarily always enforce unstable until we fix integration tests
                                currentBuild.result = 'UNSTABLE'
                                //currentBuild.result = unstableDriverResults.contains(it) ? 'UNSTABLE' : 'FAILURE'

                                ansiPrint "{bgblack}{lightyellow}Integration test failure: ${it}"
                                if (currentBuild.result == 'FAILURE' || currentBuild.result == 'UNSTABLE') {
                                    message = "Integration test failure.  ${it}"
                                    sendTeamsMessage(message, channel, yellow)
                                    sh "rm /Users/rpmobile/Desktop/lockFiles/${testDeviceID}.integrationLock"
                                    throw e
                                }
                            }
                        }
                        if (!it.contains('rebuild_')) {
                            buildOption = '--no-build'
                        }
                    }


                    def lockFileExists = fileExists "/Users/rpmobile/Desktop/lockFiles/${testDeviceID}.integrationLock"
                    if (lockFileExists) {
                        echo "/Users/rpmobile/Desktop/lockFiles/${testDeviceID}.integrationLock"
                        sh "rm /Users/rpmobile/Desktop/lockFiles/${testDeviceID}.integrationLock"
                        echo 'File Removed'
                    }
                    else{
                        echo 'File Does\'nt Exists'
                    }
                    message="PR Passed"
                    sendTeamsMessage(message, channel, green)

                }
            }
            node {
                try {

                    if (isUpgradeBranch) {
                        def upgrade = input(message: 'Do you wanna upgrade all executors to latest version of flutter?', ok: 'Yes',
                                parameters: [booleanParam(defaultValue: false,
                                        description: 'Abort if you don\'t wanna continue', name: 'Yes?')])

                        echo "Upgrade Triggered?:" + upgrade

                        isUpgradeMaster = true
                        ansiPrint "Executor number upgrading: 0"
                        sh "${flutter(0)} analyze"
                        ansiPrint "Upgrade completed successfully"
                    }

                } catch (abortedException) {
                    currentBuild.result = "ABORTED"
                    throw (abortedException)
                }
            }

        }


    }

    if (isMaster && params.Binaries){
        stage("Binaries") {
            printStage "Binaries"

            node {
                sh "pwd"
//                deleteDir()
//                unstash 'source'
//
//                sh "${flutter(EXECUTOR_NUMBER)} build apk --release --verbose"
//                if (isRelease) {
//                    sh "mkdir -p dist"
//                    sh "cp build/app/outputs/apk/release/app-release.apk dist/app-android-release.apk"
//                }
//
//                sh "${flutter(EXECUTOR_NUMBER)} clean"
//
//                sh "${flutter(EXECUTOR_NUMBER)} build ios --release --no-codesign"

                if (isRelease) {
//                    sh "cd ios && CI=true fastlane build"
//                    sh "mv ios/Runner.ipa dist/app-ios-release.ipa"
//                    sh "mv ios/Runner.app.dSYM.zip dist/app-ios-release.app.dSYM.zip"

                    // ================================================
                    // Release input :)
                    // ================================================

                    ansiPrint "{bgblack}{magenta}Please provide a summary for this release"
                    message= "In order to proceed please provide details for the release of version ${version}+${buildNumber} here:\n${BUILD_URL}input"
                    sendTeamsMessage(message, channel, green)
                    try {
                        releaseDescription = input(
                                id: 'userInput',
                                message: 'Please provide the Whats New Text for this release (This is sent to Apple/Google store). Confirm/receive text with Business.',
                                parameters: [[
                                                     $class: 'TextParameterDefinition',
                                                     name: 'Whats New Text',
                                                     description: "Changes since last release:\n${changeListSinceLastRelease}"
                                             ]]
                        )
                    } catch (abortedException) {
                        currentBuild.result = "ABORTED"
                        throw (abortedException)
                    }

                    // We dont really need nexus atm... Github is good enough :) But here is the command for the future
                    // sh "cd dist && zip -r ../rp-mobile-release.zip *"
                    // sh "mvn deploy:deploy-file -DgroupId=com.nationwide -DartifactId=rp-mobile -Dversion=${version} -DgeneratePom=true -Dpackaging=zip -DrepositoryId=nexus -Durl=http://repo.nwie.net/nexus/repository/maven-internal -Dfile=rp-mobile-release.zip"

                    // This call creates the release on GitHub
                    def releaseHeader = """Release created from Jenkins build [${BUILD_ID}](${BUILD_URL})\n\n### Release Summary\n${releaseDescription}\n### Changes\n"""

                    def newRelease = githubAPICall(
                            url: 'https://github.nwie.net/api/v3/repos/Nationwide/rp-mobile/releases',
                            httpMode: 'POST',
                            requestBody: JsonOutput.toJson([
                                    tag_name: "v${version}+${buildNumber}",
                                    target_commitish: "master",
                                    name: "v${version}+${buildNumber}",
                                    body: releaseHeader + changeListSinceLastRelease,
                                    draft: false,
                                    prerelease: false
                            ])
                    )

                    // Upload the IPA and APK to the release
//                    uploadGithubReleaseAsset(newRelease.id, 'app-android-release.apk', 'dist')
//                    uploadGithubReleaseAsset(newRelease.id, 'app-ios-release.ipa', 'dist')
//                    uploadGithubReleaseAsset(newRelease.id, 'app-ios-release.app.dSYM.zip', 'dist')

                    // Grab the build logs and toss em :)
                    httpRequest(
                            url: "${BUILD_URL}consoleText",
                            outputFile: "dist/build-log.txt"
                    )
                    uploadGithubReleaseAsset(newRelease.id, 'build-log.txt', 'dist')
                }
            }
        }
    }


    if (isMaster && params.Reports) {
        node {
            stage("Reports") {
                printStage "Generating Reports"
                try {
                    sh (script: "curl -H \"Authorization: Bearer ${NOWSECURE}\" -X POST https://lab-api.nowsecure.com/build/?group=c6b46c0f-a09e-4f37-9d98-0751193e2953 --data-binary @/Users/rpmobile/Dev/rp-mobile/ios/Runner.ipa")
                } catch (err){
                    print(err)
                }
//                def machineAddress = 'localhost:8888'
//                ansiPrint "{magenta}MobSF is analyzing at ${machineAddress}"
//                def uriForMobSF = "http://${machineAddress}/api/v1"
//                ansiPrint "{magenta}Curling at ${uriForMobSF}..."
//
//
//                ansiPrint "{magenta}Uploading packages..."
//                def dockerKey = sh (script: "python3 ~/Documents/getMobSFAPIKey.py http://${machineAddress}", returnStdout: true)
//                ansiPrint "{magenta}${uriForMobSF}/upload"
//                dockerKey = dockerKey.replace("\n", "")
//
//                sh (script: "curl -F 'file=@/Users/rpmobile/Dev/rp-mobile/build/app/outputs/apk/release/app-release.apk' ${uriForMobSF}/upload -H \"Authorization:${dockerKey}\"")
//                sh (script: "curl -F 'file=@/Users/rpmobile/Dev/rp-mobile/ios/Runner.ipa' ${uriForMobSF.replace("\n", "")}/upload -H \"Authorization:${dockerKey}\"")
//                try {
//                    ansiPrint "{magenta}Getting report hashes..."
//                    String scans = sh (script: "curl --url ${uriForMobSF.replace("\n", "")}/scans -H \"Authorization:${dockerKey}\"", returnStdout: true)
//                    println(scans)
//                    ansiPrint "{red} PARSE TIME"
//
//                    def scansJson = readJSON text: scans
//
//                    def reportOneHash = "${scansJson.content[0].MD5}".replace("\n", "")
//                    def reportTwoHash = "${scansJson.content[1].MD5}".replace("\n", "")
//
//                    sh (script: "curl -X POST --url \"${uriForMobSF}/download_pdf\" --data \"hash=${reportOneHash}\" -H \"Authorization:${dockerKey}\" > /Users/rpmobile/Dev/rp-mobile/reports/iOSReport.pdf", returnStdout: true)
//                    sh (script: "curl -X POST --url \"${uriForMobSF}/download_pdf\" --data \"hash=${reportTwoHash}\" -H \"Authorization:${dockerKey}\" > /Users/rpmobile/Dev/rp-mobile/reports/AndroidReport.pdf", returnStdout: true)
//                } catch (err){
//                    print(err)
//                }
            }
        }
    }

    // Package and wrap things up
    // we have node here for the final cleanup :)
    node {
        sh "pwd"
        if (isMaster) {
            deleteDir()

            def previousBuildResult = getPreviousNonAbortedBuild(currentBuild).result
            ansiPrint "{lightblue}Previous Result: ${previousBuildResult}, Current Result: ${currentBuild?.result}"

            // SUCCESS/FAILURE to UNSTABLE
            if (currentBuild?.result == 'UNSTABLE' && ['SUCCESS', 'FAILURE'].contains(previousBuildResult)) {

                message = previousBuildResult == 'FAILURE' ?
                        'Build improved from failure! Although, I am still feeling a little unstable... Who can check integration tests and rebuild me?' :
                        'Oh no! I am feeling a little unstable... Who can check integration tests and rebuild me?'

                sendTeamsMessage(message, channel, red)


            }

            // FAILURE/UNSTABLE to SUCCESS
            if (currentBuild?.result == null && ['UNSTABLE', 'FAILURE'].contains(previousBuildResult)) {
                message='Great job team, I am healthy and green again! :fireworks: :fireworks: :fireworks: :fireworks: :fireworks: :fireworks: '
                sendTeamsMessage(message, channel, green)
            }
        }

        cleanWs()
    }

} catch (err) {

    ansiPrint "{red}${currentBuild.result}"

    // If we didn't abort it, send a message to the peoples in Microsoft Teams!
    if (currentBuild.result != "ABORTED") {

        def message = "Your changes for branch <https://github.nwie.net/Nationwide/rp-mobile/tree/${env.BRANCH_NAME}|${env.BRANCH_NAME}> has caused a failure! Job: ${env.JOB_NAME}, Build: ${env.BUILD_NUMBER} => [Go to build](${BUILD_URL})"

        if (isMaster) {
            message = "The build for master has failed! Job: ${env.JOB_NAME}, Build: ${env.BUILD_NUMBER} => [Go to build](${BUILD_URL})"
        } else if (isPR) {
            message = "<${env.CHANGE_URL}|Pull Request ${env.CHANGE_ID}> has failed. Please check it now! => [Go to build](${BUILD_URL})"
        }

        if (['SUCCESS', 'UNSTABLE'].contains(getPreviousNonAbortedBuild(currentBuild).result)) {
            sendTeamsMessage(message, channel,red)
        }
    }

    ansiPrint "{bgred}{white}BUILD FAILURE ${err}"
    throw (err)
}


def retryUnitTests(coverage){
    try {
        sh "${flutter(EXECUTOR_NUMBER)} test ${coverage}"
    } catch(e) {
        ansiPrint "{bgred}{white}Unit test(s) failed. Retrying..."
        try {
            sh "${flutter(EXECUTOR_NUMBER)} test ${coverage}"
        }
        catch (retryException){
            message = "Unit tests failure.  ${retryException}"
            sendTeamsMessage(message, channel, red)
            throw retryException
        }
    }
}


def checkAuthor(isMaster,isPR){
    channel = changeSetAuthors.collect { "@${it}" }.join(',')
    if (isMaster) {
        channel= '@channel'
    } else if (isPR) {
        channel = "@${env.CHANGE_AUTHOR}"
    }
    else{
        channel = "@${env.CHANGE_AUTHOR}"
    }
}


def sendTeamsMessage(message, channel, style){

    office365ConnectorSend color: style, message: message + ' ' + channel, webhookUrl: 'https://outlook.office.com/webhook/59c99f53-e3e5-49b9-a1d7-063c5a769965@22140e4c-d390-45c2-b297-a26c516dc461/IncomingWebhook/05e5527de19e4918a2e45ca31c1ffb5f/1901ff29-aee3-412f-8015-bcc46d73063b'
}


/*******************************************
 HELPER FUNCTIONS
 ********************************************/

def flutter(executor){

    def branchName = env.CHANGE_BRANCH

    echo branchName == null ? "": branchName
    println isUpgradeMaster.toString()
    echo executor.toString()

//latest version flutter upgrade to: 1.22.5
    def flutter_dir = isUpgradeBranch && !isUpgradeMaster ? "/usr/local/flutter-jenkins/executor-flutterUpgrade" :
            "/usr/local/flutter-jenkins/executor-${executor}"
    def flutter_exec = "${flutter_dir}/bin/flutter"
    if (!fileExists(flutter_dir)) {
        println "Creating flutter ${flutter_exec}"
        sh "cp -R /usr/local/flutter/flutter/. ${flutter_dir}"
        sh "${flutter_exec} --version"
        sh "${flutter_exec} packages get"
    }
    else if ((isUpgradeBranch && count==0) || isUpgradeMaster){
        if (isUpgradeMaster) {
            int i
            if (flutter_exec.contains("executor-0")) {
                for (i = 0; i <= 3; i++) {
                    sh "rm -rf /usr/local/flutter-jenkins/backupExecutors/executor-${i}"
                    sh "cp -R /usr/local/flutter-jenkins/executor-${i} /usr/local/flutter-jenkins/backupExecutors/"
                }
            }
            try {
                //Upgrade first executor
                sh "${flutter_exec} upgrade --force"
                sh "${flutter_exec} --version"
                sh "${flutter_exec} packages get"

                //Make copies of upgraded executor 0 and override old one's
                for (i = 1; i <= 3; i++) {
                    def sourceDir = "/usr/local/flutter-jenkins/executor-0"
                    def destinationDir = "/usr/local/flutter-jenkins/executor-${i}"
                    sh "rm -rf ${destinationDir}"
                    sh "cp -R ${sourceDir} ${destinationDir}"
                    sh "/usr/local/flutter-jenkins/executor-${i}/bin/flutter doctor"
                }

            } catch (e) {
                ansiPrint "Flutter Upgrade failed"
                def sourceDir = "/usr/local/flutter-jenkins/backupExecutors/executor-0"
                def destinationDir = "/usr/local/flutter-jenkins/executor-0"
                sh "rm -rf ${destinationDir}"
                sh "cp -R ${sourceDir} ${destinationDir}"
                ansiPrint "Copy completed from OLD executor"
            }
        } else {
            sh "${flutter_exec} upgrade --force"
            sh "${flutter_exec} --version"
            sh "${flutter_exec} packages get"
            count++

        }
        echo count.toString()
    } else {

        sh "${flutter_exec} --version"
        sh "${flutter_exec} packages get"
    }
    return flutter_exec
}

def githubAPICall(params = [:]) {
    def options = [
            'url': '',
            'httpMode': 'GET',
            'requestBody' : null
    ] + params
    def httpResponse = httpRequest(
            consoleLogResponseBody: true,
            httpMode: options.httpMode,
            ignoreSslErrors: true,
            authentication: 'rp-mobile-git',
            url: options.url,
            responseHandle: 'NONE',
            requestBody: options.requestBody
    )
    ansiPrint "{lightgray}${httpResponse}"
    return readJSON(text: httpResponse.content)
}

def uploadGithubReleaseAsset(releaseId, fileName, fileDir = ".") {
    def releaseAssetHttpResponse = httpRequest(
            consoleLogResponseBody: true,
            httpMode: 'POST',
            ignoreSslErrors: true,
            authentication: 'rp-mobile-git',
            url: "https://github.nwie.net/api/uploads/repos/Nationwide/rp-mobile/releases/${releaseId}/assets?name=${fileName}",
            uploadFile: "${fileDir}/${fileName}",
            multipartName: fileName
    )
    ansiPrint "{lightgray}${releaseAssetHttpResponse}"
}

def printStage(name) {
    def padChar = "="
    def totalChars = 80
    def message = " Stage: ${name} "
    def padCount = (totalChars - message.length())/2
    message = "".padLeft(padCount, " ") + message + "".padLeft(padCount, " ")
    if (message.length() < totalChars) message = message + padChar
    ansiPrint "{bglightblue}{white} " + "".padLeft(totalChars,padChar) + "\n ${message}\n " + "".padLeft(totalChars,padChar)
}

def ansiPrint(message) {
    message = ansiFormatColors(message)
    ansiColor('xterm') {
        if (message == "DEBUG") {
            echo ansiFormatColors(debugAnsi())
        }
        echo message + "\u001B[0m"
    }
}


@NonCPS
def getChangeSetAuthors() {
    def result = []
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            result.push("${entry.author}".toLowerCase())
        }
    }
    return result.unique()
}

@NonCPS
def getPreviousNonAbortedBuild(build) {
    def previousBuild = build.getPreviousBuild();
    while (previousBuild != null) {
        if (previousBuild.result == null || previousBuild.result == 'ABORTED') {
            previousBuild = previousBuild.getPreviousBuild()
        } else {
            return previousBuild
        }
    }
    return previousBuild
}

@NonCPS
def prettyPrint(envText) {
    return "{${envText.split("\r?\n").join(', ')}}"
}

// http://misc.flogisoft.com/bash/tip_colors_and_formatting

@NonCPS
def getAnsiMap() {
    return [
            // FORMATS
            'bold' : '1',
            'di' : '2',
            'underline': '4',
            'blink': '5',
            'inverted': '7',

            // RESETS
            'reset' : '0',
            'resetweight' : '21',
            'resetdim' : '22',
            'resetunderline' : '24',
            'resetblink' : '25',

            // FOREGROUND COLORS
            'black': '30',
            'red': '31',
            'green': '32',
            'yellow': '33',
            'blue': '34',
            'magenta': '35',
            'cyan': '36',
            'lightgrey': '37',
            'lightgray': '37',
            'darkgrey': '90',
            'lightred': '91',
            'lightgreen': '92',
            'lightyellow': '93',
            'lightblue': '94',
            'lightmagenta': '95',
            'lightcyan': '96',
            'white': '97',
            'orange': '209',

            // BACKGROUND COLORS
            'bgdefault': '49',
            'bgblack': '40',
            'bgred': '41',
            'bggreen': '42',
            'bgyellow': '43',
            'bgblue': '44',
            'bgmagenta': '45',
            'bgcyan': '46',
            'bglightgray': '47',
            'bgdarkgray': '100',
            'bglightred': '101',
            'bglightgreen': '102',
            'bglightyellow': '103',
            'bglightblue': '104',
            'bglightmagenta': '105',
            'bglightcyan': '106',
            'bgwhite': '107'
    ]
}

@NonCPS
def convertToAnsi(message, format, ansiCode) {
    message = message.replaceAll("(?i)\\{${format}\\}", "\u001B[${ansiCode}m")
    return message.replaceAll(/\{([0-9]+)\}/,  '\u001B[$1m')
}

@NonCPS
def ansiFormatColors(message) {
    getAnsiMap().each { key, value ->
        message = convertToAnsi(message, key, value)
    }
    return message
}