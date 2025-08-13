<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<body>

<h1>KoMapper – Kotlin Mapping &amp; Koin Integration Made Simple 🚀</h1>

<p><strong>KoMapper</strong> is a Kotlin Symbol Processing (KSP) plugin that automatically generates <strong>type-safe mappers</strong> and optional <strong>Koin DI registrations</strong> for your DTOs, domain models, and more.</p>

<p>With <strong>KoMapper</strong>, you write <em>less boilerplate</em> and keep your mapping logic clean, consistent, and testable.</p>

<hr />

<h2>✨ Features</h2>
<ul>
  <li><strong>Automatic Mapper Generation</strong><br />Generate mapping classes between your DTOs and domain models with a single annotation.</li>
  <li><strong>Bidirectional Mapping</strong><br />Supports forward (<code>Dto → Domain</code>) and reverse (<code>Domain → Dto</code>) mapping.</li>
  <li><strong>Nullability Aware</strong><br />Automatically respects <code>nullable</code> and <code>non-nullable</code> fields with safe handling.</li>
  <li><strong>Suspend Functions</strong><br />Generate suspendable mapping functions for coroutine-friendly projects.</li>
  <li><strong>Extension Functions</strong><br />Adds intuitive extension functions for your models (e.g., <code>userDto.toUser()</code> or <code>user.toUserDtoBlocking()</code>).</li>
  <li><strong>Koin Integration</strong> <em>(optional)</em><br />Automatically generate a <strong>Koin module</strong> registering all your mappers for easy dependency injection.</li>
  <li><strong>Singleton or Factory Scope</strong><br />Decide via annotation whether mappers should be singletons or factories in Koin.</li>
</ul>

<hr />

<h2>📦 Installation</h2>

<pre><code>plugins {
    id("com.google.devtools.ksp") version "X.Y.Z"
}

dependencies {
    implementation("io.insert-koin:koin-core:3.5.0") // optional for DI
    ksp("com.yourname:komapper:1.0.0")
}
</code></pre>

<hr />

<h2>🚀 Usage</h2>

<h3>1️⃣ Annotate your DTO or model</h3>

<pre><code>@MapTo(
    target = User::class,
    suspendable = true,
    oneLineEnabled = true
)
@RegisterInKoin(isSingleton = true) // optional
data class UserDto(
    val userId: String,
    val userName: String
)
</code></pre>

<h3>2️⃣ Generated Mapper</h3>

<p>KoMapper generates:</p>

<pre><code>class UserDtoToUserMapper : SuspendMapper&lt;UserDto, User&gt; {
    override suspend fun mappingObject(input: UserDto): User =
        User(id = input.userId, name = input.userName)
}
</code></pre>

<p>If reverse mapping is enabled, it also generates:</p>

<pre><code>class UserToUserDtoMapper : SuspendMapper&lt;User, UserDto&gt; {
    override suspend fun mappingObject(input: User): UserDto =
        UserDto(userId = input.id, userName = input.name)
}
</code></pre>

<h3>3️⃣ Generated Extension Functions</h3>

<pre><code>suspend fun UserDto.toUser(): User =
    UserDtoToUserMapper().mappingObject(this)

fun UserDto.toUserBlocking(): User =
    runBlocking { toUser() }
</code></pre>

<h3>4️⃣ Generated Koin Module <em>(optional)</em></h3>

<p>If annotated with <code>@RegisterInKoin</code>, KoMapper creates:</p>

<pre><code>val mapperModule = module {
    single&lt;SuspendMapper&lt;UserDto, User&gt;&gt; { UserDtoToUserMapper() }
    single&lt;SuspendMapper&lt;User, UserDto&gt;&gt; { UserToUserDtoMapper() }
}
</code></pre>

<hr />

<h2>⚙️ Configuration</h2>

<table>
<thead>
<tr>
<th>Annotation</th>
<th>Parameter</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>@MapTo</code></td>
<td><code>target</code></td>
<td>Target class to map to</td>
</tr>
<tr>
<td></td>
<td><code>suspendable</code></td>
<td>Generate suspend functions</td>
</tr>
<tr>
<td></td>
<td><code>oneLineEnabled</code></td>
<td>Minimize generated code where possible</td>
</tr>
<tr>
<td></td>
<td><code>propertyMaps</code></td>
<td>Custom property mappings</td>
</tr>
<tr>
<td></td>
<td><code>sourceNullable</code></td>
<td>Allow nullable source type</td>
</tr>
<tr>
<td></td>
<td><code>targetNullable</code></td>
<td>Allow nullable target type</td>
</tr>
<tr>
<td><code>@RegisterInKoin</code></td>
<td><code>isSingleton</code></td>
<td>Register mapper as singleton or factory</td>
</tr>
</tbody>
</table>

<hr />

<h2>🛠️ Why KoMapper?</h2>

<ul>
  <li>✅ Eliminate repetitive mapping code</li>
  <li>✅ Consistent mapping rules across your project</li>
  <li>✅ Easy Koin integration without writing DI modules manually</li>
  <li>✅ Null-safety built in</li>
  <li>✅ Coroutine-friendly</li>
</ul>

<hr />

<h2>📄 License</h2>

<p>MIT License – feel free to use in your projects.</p>

</body>
</html>
