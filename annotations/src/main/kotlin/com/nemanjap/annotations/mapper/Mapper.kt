package com.nemanjap.annotations.mapper

/**
 * A generic functional interface for mapping an object of type [Input] to an object of type [Output].
 *
 * This interface is intended to be implemented (or generated) by mapping classes that
 * transform data between layers (e.g., DTO → Domain, Domain → UI model).
 *
 * It can be used in manual implementations:
 * ```
 * class UserDtoToDomainMapper : Mapper<UserDto, UserDomain> {
 *     override fun mappingObject(input: UserDto): UserDomain {
 *         return UserDomain(id = input.id, name = input.name)
 *     }
 * }
 * ```
 *
 * Or with a generated mapper (e.g., via KSP + @MapTo annotation):
 * ```
 * val mapper: Mapper<UserDto, UserDomain> = GeneratedUserDtoToDomainMapper()
 * val domainUser = mapper.mappingObject(userDto)
 * ```
 *
 * Since this is a [fun interface], it can also be used with lambdas:
 * ```
 * val mapper = Mapper<UserDto, UserDomain> { dto ->
 *     UserDomain(id = dto.id, name = dto.name)
 * }
 * ```
 *
 * @param Input  The source type to map from.
 * @param Output The target type to map to.
 */
fun interface Mapper<in Input, out Output> {
    /**
     * Maps the given [input] object to an instance of [Output].
     *
     * @param input The source object to be transformed.
     * @return The mapped target object.
     */
    fun mappingObject(input: Input): Output
}