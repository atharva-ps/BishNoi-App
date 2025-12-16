package com.app.bishnoi.domain.usecase.profile

import com.app.bishnoi.domain.model.Address
import com.app.bishnoi.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateAddressUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(address: Address) =
        profileRepository.updateAddress(address)
}
