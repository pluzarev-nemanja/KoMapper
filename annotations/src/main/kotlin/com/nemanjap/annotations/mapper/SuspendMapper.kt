package com.nemanjap.annotations.mapper

/**
 * A generic functional interface for mapping an object of type [Input] to an object of type [Output] in a suspending context.
 *
 * This interface is intended for mapping operations that require coroutine support,
 * such as performing asynchronous or non-blocking transformations.
 *
 * It is commonly used in service or repository layers where mapping might involve
 * database queries, network calls, or other suspend functions.
 *
 * Example usage:
 * ```
 * class UserDtoToDomainSuspendMapper : SuspendMapper<UserDto, UserDomain> {
 *     override suspend fun mappingObject(input: UserDto): UserDomain {
 *         // Example: asynchronous transformation
 *         val extraInfo = api.fetchExtraInfo(input.id)
 *         return UserDomain(id = input.id, name = input.name, extra = extraInfo)
 *     }
 * }
 * ```
 *
 * Using a lambda:
 * ```
 * val mapper = SuspendMapper<UserDto, UserDomain> { dto ->
 *     UserDomain(id = dto.id, name = dto.name)
 * }
 * ```
 *
 * @param Input  The source type to map from.
 * @param Output The target type to map to.
 */
fun interface SuspendMapper<in Input, out Output> {
    /**
     * Maps the given [input] object to an instance of [Output] in a suspending context.
     *
     * @param input The source object to be transformed.
     * @return The mapped target object.
     */
    suspend fun mappingObject(input: Input): Output
}
