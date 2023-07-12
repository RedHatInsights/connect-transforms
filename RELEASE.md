# Project release procedures

## Prerequisites

1. Ensure you are able to build the project (e.g. `mvn clean package`)
1. Ensure you have push rights to the master branch of the project
1. Ensure you have a Sonatype account that is authorized to release artifacts with the `com.redhat.insights.kafka` Group Id

   * Log in to https://oss.sonatype.org/ to validate
   * If your account is not authorized ask one of the authorized users to request the permission on your behalf: [example](https://issues.sonatype.org/browse/OSSRH-93281)
   * Put account credentials into `~/.m2/settings.xml`. Example:

      ```xml
        <settings>
            <servers>
                <server>
                    <id>ossrh</id>
                    <username>jharting</username>
                    <password>REDACTED</password>
                </server>
            </servers>
        </settings>

      ```

1. Ensure you have a GPG key for artifact signing or follow [this guide](https://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/) to create one

## Releasing a new version of connect-transforms

1. Run `mvn release:clean` to clean any previous release artifacts

1. Run `mvn release:prepare` to prepare the release.

   You'll be asked to define the release version and the next SNAPSHOT version.
   Afterwards, the release plugin will prepare two commits in the (local) master branch as well as the release tag.
   See [target docs](https://maven.apache.org/maven-release/maven-release-plugin/usage/prepare-release.html) for more information.

1. Run `mvn release:perform` to perform the release.

   The artifacts will be built and uploaded to Sonatype.
   After a short delay the new version should appear at [central.sonatype.com](https://central.sonatype.com/artifact/com.redhat.insights.kafka/connect-transforms)

   See [target docs](https://maven.apache.org/maven-release/maven-release-plugin/usage/perform-release.html) for more information.

1. Run `git push origin master && git push origin --tags` to push the release commits and the tag to github.
