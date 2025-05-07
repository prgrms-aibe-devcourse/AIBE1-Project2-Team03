# TEAMO !
> AIBE1 TEAM03 호식이세마리 | 팀원 준비 완료! 팀워크가 필요할 땐, Teamo

### ✅ application-dev.yml
```yml
server:
  port: 8080
spring:
  security:
    oauth2:
      client:
        registration:
          kakao:
            client-id:
            client-secret:
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/kakao"
            scope:
              - profile_nickname
              # - account_email
          github:
            client-id:
            client-secret:
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/github"
            scope:
              - read:user
              - user:email
        provider:
          kakao:
            authorization-uri:  https://kauth.kakao.com/oauth/authorize
            token-uri:          https://kauth.kakao.com/oauth/token
            user-info-uri:      https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
          github:
            authorization-uri:  https://github.com/login/oauth/authorize
            token-uri:          https://github.com/login/oauth/access_token
            user-info-uri:      https://api.github.com/user
            user-name-attribute: id
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url:
    username:
    password:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        show_sql: true
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true
jwt:
  secret:
  expiration-ms: 3600000  # 1시간
front-end:
  redirect: http://localhost:3000/oauth2/redirect
```
