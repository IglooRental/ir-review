FROM openjdk:8-jdk-alpine

MAINTAINER jm5619

RUN mkdir /app

WORKDIR /app

ADD ./target/ir-review-1.0.0-SNAPSHOT.jar /app

EXPOSE 8083

CMD ["java", "-jar", "ir-review-1.0.0-SNAPSHOT.jar"]
