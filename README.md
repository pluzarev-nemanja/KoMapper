# KoMapper – Kotlin Mapping & Koin Integration Made Simple 🚀

KoMapper is a Kotlin Symbol Processing (KSP) plugin that **automatically generates type-safe mappers** and optional **Koin DI registrations** for your DTOs, domain models, and more.

With KoMapper, you write *less boilerplate* and keep your mapping logic clean, consistent, and testable.

---

## ✨ Features

- **Automatic Mapper Generation**  
  Generate mapping classes between your DTOs and domain models with a single annotation.

- **Bidirectional Mapping**  
  Supports forward (`Dto → Domain`) and reverse (`Domain → Dto`) mapping.

- **Nullability Aware**  
  Automatically respects `nullable` and `non-nullable` fields with safe handling.

- **Suspend Functions**  
  Generate suspendable mapping functions for coroutine-friendly projects.

- **Extension Functions**  
  Adds intuitive extension functions for your models (e.g., `userDto.toUser()` or `user.toUserDtoBlocking()`).

- **Koin Integration** *(optional)*  
  Automatically generate a **Koin module** registering all your mappers for easy dependency injection.

- **Singleton or Factory Scope**  
  Decide via annotation whether mappers should be singletons or factories in Koin.

---

## 📦 Installation

### Install via JitPack

KoMapper is published on [JitPack](https://jitpack.io/#pluzarev-nemanja/KoMapper/v0.1.0)!

Add the following to your `build.gradle.kts` or `build.gradle`:

<details>
<summary>build.gradle.kts (Kotlin DSL)</summary>

```kotlin
plugins {
    id("com.google.devtools.ksp") version "<KSP_VERSION>"
}
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
dependencies {
    implementation("io.insert-koin:koin-core:3.5.0") // optional for DI
    ksp("com.github.pluzarev-nemanja:KoMapper:v0.1.0")
}
```
</details>

<details>
<summary>build.gradle (Groovy DSL)</summary>

```groovy
plugins {
    id 'com.google.devtools.ksp' version '<KSP_VERSION>'
}
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'io.insert-koin:koin-core:3.5.0' // optional for DI
    ksp 'com.github.pluzarev-nemanja:KoMapper:v0.1.0'
}
```
</details>

> **Replace `<KSP_VERSION>` with the latest KSP plugin version compatible with your project.**

### Alternate: Local Dependency

If you prefer to build from source:

```kotlin
dependencies {
    ksp(files("path/to/komapper/build/libs/komapper-<version>.jar"))
}
```

---

## 🚀 Usage

### 1️⃣ Annotate your DTO or model

```kotlin
@MapTo(
    target = User::class,
    suspendable = true,
    oneLineEnabled = true
)
@RegisterInKoin(isSingleton = true) // optional
data class UserDto(
    val userId: String,
    val userName: String
)
```

### 2️⃣ Generated Mapper

KoMapper generates:

```kotlin
class UserDtoToUserMapper : SuspendMapper<UserDto, User> {
    override suspend fun mappingObject(input: UserDto): User =
        User(id = input.userId, name = input.userName)
}
```

If reverse mapping is enabled, it also generates:

```kotlin
class UserToUserDtoMapper : SuspendMapper<User, UserDto> {
    override suspend fun mappingObject(input: User): UserDto =
        UserDto(userId = input.id, userName = input.name)
}
```

### 3️⃣ Generated Extension Functions

```kotlin
suspend fun UserDto.toUser(): User =
    UserDtoToUserMapper().mappingObject(this)

fun UserDto.toUserBlocking(): User =
    runBlocking { toUser() }
```

### 4️⃣ Generated Koin Module *(optional)*

If annotated with `@RegisterInKoin`, KoMapper creates:

```kotlin
val mapperModule = module {
    single<SuspendMapper<UserDto, User>> { UserDtoToUserMapper() }
    single<SuspendMapper<User, UserDto>> { UserToUserDtoMapper() }
}
```

---

## ⚙️ Configuration

KoMapper offers fine control via annotations:

| Annotation         | Parameter         | Description                                         |
|--------------------|-------------------|-----------------------------------------------------|
| `@MapTo`           | `target`          | Target class to map to                              |
|                    | `suspendable`     | Generate suspend functions                          |
|                    | `oneLineEnabled`  | Minimize generated code where possible              |
|                    | `propertyMaps`    | Custom property mappings                            |
|                    | `sourceNullable`  | Allow nullable source type                          |
|                    | `targetNullable`  | Allow nullable target type                          |
| `@RegisterInKoin`  | `isSingleton`     | Register mapper as singleton or factory             |
|                    | `createdAtStart`  | Eager Koin definition at startup                    |
|                    | `named`           | Koin qualifier (string-based)                       |
|                    | `namedClass`      | Koin qualifier (class-based)                        |
|                    | `bindInterfaces`  | Bind generated to interfaces                        |
|                    | `useConstructorDsl`| Use Koin's constructor DSL for registration         |

---

## 🛠️ Why KoMapper?

- ✅ Eliminate repetitive mapping code
- ✅ Consistent mapping rules across your project
- ✅ Easy Koin integration without writing DI modules manually
- ✅ Null-safety built in
- ✅ Coroutine-friendly

---

## 📄 License

MIT License – feel free to use in your projects.

---

> **Note:**  
> KoMapper is now available via JitPack! For the latest instructions, see the Installation section above.