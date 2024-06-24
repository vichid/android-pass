/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.usecases.securelink

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.securelink.DeleteInactiveSecureLinks
import proton.android.pass.data.impl.repositories.SecureLinkRepository
import javax.inject.Inject

class DeleteInactiveSecureLinksImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val secureLinkRepository: SecureLinkRepository
) : DeleteInactiveSecureLinks {

    override suspend fun invoke() = observeCurrentUser()
        .first()
        .let { user ->
            secureLinkRepository.deleteInactiveSecureLinks(user.userId)
        }

}
