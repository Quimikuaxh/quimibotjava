#!/bin/bash
mvn clean install package
cd target
java -jar ./Bot_Telegram_Example-0.0.1-SNAPSHOT-jar-with-dependencies.jar