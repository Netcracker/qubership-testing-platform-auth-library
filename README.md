# Qubership Testing Platform Auth Library

## Purpose

This library is used to check user permissions, to perform users authorization and authentication for requests, received/sent by QSTP services.

## Usage Notes

### Authentication

#### 1. Add dependencies into pom.xml
- The 1st dependency is to artifact containing object models, interfaces, exceptions, annotations etc.
- The 2nd dependency is to artifact containing implementation classes.
- So, depending on usage, one needs to add the 1st dependency only (atp-auth-models) or both dependencies.

**Please note**:
- atp-auth-impl module in this repository contains **stubbed** implementation,
- So, one need to implement fully-functional atp-auth-impl module instead of it, to use it for real security checks.
```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.qubership.atp.auth</groupId>
        <artifactId>atp-auth-models</artifactId>
        <version>1.2.67</version>
    </dependency>
    <dependency>
        <groupId>org.qubership.atp.auth</groupId>
        <artifactId>atp-auth-impl</artifactId>
        <version>1.2.67</version>
    </dependency> 
    ...
</dependencies>
```

#### 2. Add Authentication properties into application.properties
```text
##==================atp-auth-spring-boot-starter=====================
spring.cache.cache-names: projects, auth_objects
spring.cache.caffeine.spec: maximumSize=100, expireAfterAccess=120s

keycloak.resource=""
keycloak.enabled=true
keycloak.bearer-only=true
keycloak.realm=${REALM_NAME}
keycloak.auth-server-url=${KEYCLOACK_AUTH_URL}

atp-auth.project_info_endpoint=${PROJECT_INFO_ENDPOINT:/api/v1/users/projects}

feign.atp.users.url=${FEIGN_ATP_USERS_URL:}
feign.atp.users.name=${FEIGN_ATP_USERS_NAME:ATP-USERS-BACKEND}
feign.atp.users.route=${FEIGN_ATP_USERS_ROUTE:/api/atp-users-backend/v1}

// if you want send service entities via kafka
kafka.enable=${KAFKA_ENABLE:false}
kafka.service.entities.topic=${KAFKA_SERVICE_ENTITIES_TOPIC:ci_service_entities}
kafka.service.entities.topic.partitions=${KAFKA_SERVICE_ENTITIES_TOPIC_PARTITIONS:1}
kafka.service.entities.topic.replicas=${KAFKA_SERVICE_ENTITIES_TOPIC_REPLICATION_FACTOR:3}
spring.kafka.producer.bootstrap-servers=${KAFKA_SERVERS:kafka:9092}
```

Please find additional information about how to integrate microservice with internal gateway in User Guide

### Authorization
#### 1. Turn ON authentication according instructions above
#### 2. Use @PreAuthorize() annotation
The annotation should be placed before each backend method, if permissions should be checked before its execution.
```java
@GetMapping(value = "/project/{projectUuid}")
@PreAuthorize("@EntityAccess.checkAccess(#projectUuid, \"READ\")")
public List<Defect> getDefectsByProjectUuid(@PathVariable("projectUuid") String projectUuid) {
    return service.getDefectsByProjectUuid(projectUuid);
}

@DeleteMapping(value = "/delete/{uuid}")
@PreAuthorize("@EntityAccess.checkAccess(@defectsService.getByUuid(#uuid).getProjectId(), \"DELETE\")")
public void delete(@PathVariable("uuid") String uuid) {
    akbRecordsService.removeDefectFromAkbRecord(uuid);
    service.delete(uuid);
}
```

In the annotation, checkAccess() method is used (EntityAccess component):
```code
@entityAccess.checkAccess(projectId, operation)

// To check access in a specific entity
// It won't work if you don't first send the service entities to the user service
@entityAccess.checkAccess(entityName, projectId, operation)
```

There is overloaded method for usability reasons:
```code
@entityAccess.checkAccess(projectId, operation)
```

Also, there is bulk method to check permissions for the action under set of projects:
```code
@entityAccess.checkAccess(projectIdSet, action)
```

5 action types are supported currently:
```text
CREATE 
READ
UPDATE
DELETE
EXECUTE
```

```code
@EntityAccess.checkAccess(#projectUuid, "CREATE")
@EntityAccess.checkAccess(#projectUuid, "READ")
@EntityAccess.checkAccess(#projectUuid, "UPDATE")
@EntityAccess.checkAccess(#projectUuid, "DELETE")
@EntityAccess.checkAccess(#projectUuid, "EXECUTE")
@EntityAccess.isAdmin()
```

Authorization is considered successful automatically, independently on `@PreAuthorize()` checks, if the user has
`ATP_ADMIN` role. This role is configured and assigned to users in Keycloak admin console.

### M2MRestTemplate
#### 1. Add Keycloak properties into application.properties
```text
keycloak.resource=${KEYCLOAK_RESOURCE}
keycloak.credentials.secret=${KEYCLOAK_SECRET}
```

#### 2. Turn ON configuration using the annotation
```code
@EnableM2MRestTemplate
```

#### 3. Add link to the bean
The link should be added to any class where you want to make requests to external resources.
```java
@Autowired
RestTemplate m2mRestTemplate;
```

#### 4. Client roles configuration
Example below shows, how one can allow a catalogue client to get information about users:
1. Open client settings
![Client Settings Edit page](doc/images/client_settings.png)
2. Switch to Service Account Roles tab
![Service Account Roles tab](doc/images/sar.png)
3. Select roles realm-management and view-users
![Select roles](doc/images/select_role.png)
4. Click 'Add selected' button

#### Remarks
If, due to some reason, you want to use m2mRestTemplate without client token getting,
you should explicitly configure it via 'atp-auth.enable-m2m' property:
```text
atp-auth.enable-m2m=false
```

Default value is true.

### RelayRestTemplate and RelayWebClient

They are used to add token of current authenticated user to all requests. RelayRestTemplate is turned on by default.

#### 1. RelayWebClient configuration can be turned ON by means of annotation
```code
@EnableTokenRelayWebClient
```

#### 2. Add link to the bean
The link should be added to any class where you want to make requests to external resources.
```java
@Autowired
RestTemplate relayRestTemplate;
```

```java
@Autowired
WebClient relayWebClient;
```

### OAuth2 Feign Client Interceptor
The interceptor uses token of the current authenticated user in requests to services.
If the current security context doesn't contain user token, service token is used (the same way as in m2m rest template).

#### 1. Feign Client Interceptor configuration can be turned ON by means of annotation
```code
@EnableOauth2FeignClientInterceptor
```

### Turning security ON and OFF by means of profiles
#### Turn ON
This is default mode. It works in case 'default' profile is active.

#### Turn OFF
This mode can be selected if:

1.'disable-security' profile is set active:

```text
spring.profiles.active=disable-security
```

2.'keycloak.enabled' is changed from true to false.
```text
keycloak.enabled=false
```

### Ssl certificate verification
By default, SSL certificate verification is turned off.
To turn if on, one needs to use 'atp-auth.ssl.certificate.verify' property, and also to set path-to-certificates-folder:
```text
atp-auth.ssl.certificate.verify=true
atp-auth.ssl.certificate.dir.path=/etc/ssl
```

- To ignore ssl certificates in feign clients, one should add properties:
```text
feign.httpclient.disableSslValidation=true
feign.httpclient.enabled=false
feign.okhttp.enabled=true
```

- To turn off metrics of feign client, one should add property:
```text
atp.feign.micrometer.enable=false
```

### RestTemplate Logging
#### 1. Add RestTemplate Logging properties into application.properties
```text
atp.logging.resttemplate.headers=${ATP_HTTP_LOGGING_HEADERS:true}
atp.logging.resttemplate.headers.ignore=${ATP_HTTP_LOGGING_HEADERS_IGNORE:}
atp.logging.feignclient.headers=${ATP_HTTP_LOGGING_HEADERS:true}
atp.logging.feignclient.headers.ignore=${ATP_HTTP_LOGGING_HEADERS_IGNORE:}
```

By default, 'atp.logging.resttemplate.headers' value is false.
* _atp.logging.resttemplate.headers_ - To log request/response headers for RelayRestTemplate and M2MRestTemplate.
* _atp.logging.resttemplate.headers.ignore_ - To ignore specified headers while logging. Tokens should be separated with spaces.
* _atp.logging.feignclient.headers_ - To ignore request/response headers for FeignClient.
* _atp.logging.feignclient.headers.ignore_ - To ignore specified headers for FeignClient. Tokens should be separated with spaces.
* Properties _atp.logging.resttemplate.headers.ignore_ and _atp.logging.feignclient.headers.ignore_ support regular expressions.

#### 2. Add configuration into logback.xml
```xml
<if condition='${ATP_HTTP_LOGGING}'>
    <then>
        <logger name="org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor" level="DEBUG" additivity="false">
            <appender-ref ref="ASYNC_GELF"/>
        </logger>
        <logger name="org.qubership.atp.catalogue.service.client.feign.DatasetFeignClient" level="DEBUG" additivity="false">
            <appender-ref ref="ASYNC_GELF"/>
        </logger>
    </then>
</if>
```

To turn logging ON at local machine, one should add options into JVM parameters:
```text
-Dlogging.level.org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor=debug
-Dlogging.level.org.qubership.atp.catalogue.service.client.feign.DatasetFeignClient=debug
```

### UI Integration

1. Turn ON 'implicit flow' option in Keycloak client settings
(Keycloak can return access token just after redirection).

2. Import `AtpHttpClientModule` into `app.module.ts` from `@..../atp-common-ui/dist/src/modules/common/http/http-client.module`

3. Turn ON login in the `atp-single-ui` component:
```html
<atp-single-ui [projectId]="_projectId$ | async"
               [loginRequired]="_loginRequired"
               [idp]="_idp"
               [navigationList]="_navigationList">
</atp-single-ui>

<ng-container *ngIf="!_loginRequired || _loginService.isAuthenticated">
    <atp-navigation-path></atp-navigation-path>

    <div class="atp-app__container">
        <main class="atp-app__main">
            <router-outlet></router-outlet>
        </main>
    </div>
</ng-container>
```

```typescript
import {AtpIdpModel} from '@..../atp-common-ui/dist/src/models/idp.model';
import {AtpRouteService} from '@..../atp-common-ui/dist/src/modules/common/services/route.service';
import {UxLoginService} from '@..../ux-ng2';

class AtpProjectComponent {
    public _idp: AtpIdpModel;
    public _loginRequired: boolean;

    constructor(
        private routeService: AtpRouteService,
        public _loginService: UxLoginService,
    ) {
        this._idp = this.routeService.getIdp();
        this._loginRequired = this.routeService.getLoginRequired();
    }
}
```

### Logging business IDs
Default list of business IDs:
```text
userId,projectId,executionRequestId,testRunId,bvTestRunId,bvTestCaseId,environmentId,
systemId,subscriberId,tsgSessionId,svpSessionId,dataSetId,dataSetListId,attributeId,
itfLiteRequestId,reportType,itfSessionId,itfContextId,callChainId
```

Property to set business IDs:
```text
atp.logging.business.keys=userId,projectId
```
