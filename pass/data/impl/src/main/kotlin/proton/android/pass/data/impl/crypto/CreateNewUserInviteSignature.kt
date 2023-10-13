/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.key.domain.getUnarmored
import me.proton.core.key.domain.signData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.commonrust.api.NewUserInviteSignatureBodyCreator
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.pass.domain.key.ShareKey
import javax.inject.Inject
import javax.inject.Singleton

interface CreateNewUserInviteSignature {
    operator fun invoke(
        inviterUserAddress: UserAddress,
        email: String,
        vaultKey: ShareKey
    ): Result<String>
}

@Singleton
class CreateNewUserInviteSignatureImpl @Inject constructor(
    private val newUserInviteSignatureBodyCreator: NewUserInviteSignatureBodyCreator,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val context: CryptoContext
) : CreateNewUserInviteSignature {
    override fun invoke(
        inviterUserAddress: UserAddress,
        email: String,
        vaultKey: ShareKey
    ): Result<String> {
        val signatureBody = encryptionContextProvider.withEncryptionContext {
            val vaultKeyContents = decrypt(vaultKey.key)
            newUserInviteSignatureBodyCreator.create(
                email = email,
                vaultKey = vaultKeyContents
            )
        }

        val signedRawData = inviterUserAddress.useKeys(context) {
            val signature = signData(
                data = signatureBody,
                signatureContext = SignatureContext(
                    value = Constants.SIGNATURE_CONTEXT_NEW_USER,
                    isCritical = true
                )
            )
            getUnarmored(signature)
        }

        val asBase64 = Base64.encodeBase64String(signedRawData)
        return Result.success(asBase64)
    }
}
