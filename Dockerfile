FROM openjdk:8-jdk-alpine
COPY build/libs/TourGuide-1.0.0.jar TourGuide-1.0.0.jar
ENTRYPOINT ["java","-jar","/TourGuide-1.0.0.jar"]
