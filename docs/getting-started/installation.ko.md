# 설치

## 요구사항

| 의존성       | 버전    | 비고 |
|--------------|---------|------|
| Spring Boot  | 3.2+    | 권장 3.4+. 3.0 API 기준 컴파일되지만 3.0/3.1은 EOL. CI에서 3.2, 3.3, 3.4 테스트. |
| QueryDSL     | 5.1.0+  | |
| Kotlin       | 1.7+    | |
| Java         | 17+     | |

## 의존성 추가

=== "Gradle (Kotlin DSL)"

    ```kotlin
    implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:{{ version }}")
    ```

=== "Gradle (Groovy DSL)"

    ```groovy
    implementation 'io.github.harryjhin:querydsl-ktx-spring-boot-starter:{{ version }}'
    ```

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.harryjhin</groupId>
        <artifactId>querydsl-ktx-spring-boot-starter</artifactId>
        <version>{{ version }}</version>
    </dependency>
    ```

이것으로 끝입니다. 스타터가 필요한 모든 것을 포함합니다.

---

## 모듈 선택 가이드

라이브러리는 세 개의 모듈로 구성됩니다. 필요에 따라 선택하세요:

| 모듈 | 제공하는 것 | 사용 시기 |
|--------|-----------------|-------------|
| `querydsl-ktx-spring-boot-starter` | Core + AutoConfiguration | **대부분의 프로젝트.** `JPAQueryFactory` 빈을 자동으로 등록합니다. |
| `querydsl-ktx` | 확장 인터페이스 + `QuerydslRepository` 기반 클래스 | 이미 `JPAQueryFactory` 빈을 직접 등록하고 있으며, Kotlin 확장 함수만 필요한 경우. |
| `querydsl-ktx-spring-boot` | AutoConfiguration만 | 자동 등록되는 `JPAQueryFactory`는 원하지만, 확장 인터페이스는 직접 구현할 경우. |

### Starter (권장)

```kotlin
// querydsl-ktx와 querydsl-ktx-spring-boot를 모두 포함
implementation("io.github.harryjhin:querydsl-ktx-spring-boot-starter:{{ version }}")
```

### Core만 사용

```kotlin
// 확장 인터페이스만 -- AutoConfiguration 없음
implementation("io.github.harryjhin:querydsl-ktx:{{ version }}")
```

!!! note "Core만 사용하는 경우"
    다음과 같은 경우 `querydsl-ktx`만 사용하세요:

    - 커스텀 `JPQLTemplates`로 `JPAQueryFactory`를 등록하는 경우
    - Spring이 아닌 프레임워크를 사용하는 경우
    - 자동 설정을 전혀 원하지 않는 경우

---

## AutoConfiguration

스타터는 다음 조건으로 `JPAQueryFactory` 빈을 자동 등록합니다:

| 조건 | 목적 |
|-----------|---------|
| `@ConditionalOnClass(JPAQueryFactory)` | 클래스패스에 QueryDSL이 있을 때만 활성화 |
| `@ConditionalOnMissingBean` | 직접 정의한 `JPAQueryFactory`가 있으면 자동 설정을 적용하지 않음 |
| `@AutoConfiguration(after = HibernateJpaAutoConfiguration)` | `EntityManager`가 먼저 준비되도록 보장 |

!!! tip "커스텀 JPAQueryFactory"
    직접 `JPAQueryFactory` 빈을 등록하면(예: 커스텀 `JPQLTemplates` 사용),
    자동 설정은 자동으로 비활성화됩니다. 별도의 exclusion이 필요 없습니다.

    ```kotlin
    @Configuration
    class QuerydslConfig {
        @Bean
        fun jpaQueryFactory(entityManager: EntityManager) =
            JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager)
    }
    ```

## 설정 확인

간단한 리포지토리를 만들어 모든 것이 정상 동작하는지 확인하세요:

```kotlin
@Repository
class TestRepository : QuerydslRepository<YourEntity>() {

    private val entity = QYourEntity.yourEntity

    fun findAll(): List<YourEntity> =
        selectFrom(entity).fetch()
}
```

컴파일되고 실행되면 설정이 완료된 것입니다. [빠른 시작](quick-start.md)으로 이동하여 첫 동적 쿼리를 작성하세요.

## GraalVM 네이티브 이미지

querydsl-ktx는 GraalVM 네이티브 이미지 호환을 위한 `RuntimeHintsRegistrar`를 제공합니다.
Auto-configuration이 라이브러리에 필요한 리플렉션 힌트를 자동 등록합니다.

!!! warning "QueryDSL 상위 제한사항"
    QueryDSL 5.1.0은 GraalVM 네이티브 이미지를 공식 지원하지 않습니다
    ([querydsl/querydsl#3646](https://github.com/querydsl/querydsl/issues/3646)).
    네이티브 이미지 빌드 시 QueryDSL 코어 클래스에 대한 추가 리플렉션 설정이
    필요할 수 있습니다.
