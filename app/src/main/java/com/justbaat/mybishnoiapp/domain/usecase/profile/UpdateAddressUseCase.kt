package com.justbaat.mybishnoiapp.domain.usecase.profile

import com.justbaat.mybishnoiapp.domain.model.Address
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(address: Address) =
        profileRepository.updateAddress(address)
}
