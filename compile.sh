G=graalvm
T=target
C=compile
mvn clean package
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar version
cp $C/config_backup.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
cp $C/config_no_statics.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
cp $C/config_no_statics_hard.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
cp $C/config_no_subnets.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
cp $C/config_missing_net_static.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
cp $C/config_no_subnets_hard.xml ./config.xml
java --enable-preview -agentlib:native-image-agent=config-merge-dir=$G -jar $T/Migration-jar-with-dependencies.jar graal
rm ./config.xml
rm ./new_config.xml
mvn clean -Pnative native:compile
