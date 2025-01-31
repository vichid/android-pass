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

package proton.android.pass.data.impl.usecases

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.Vault
import proton.android.pass.test.domain.TestVault
import java.util.Calendar
import java.util.Date

class ObserveUsableVaultsImplTest {

    private lateinit var instance: ObserveUsableVaultsImpl

    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        getUserPlan = TestGetUserPlan()
        observeVaults = TestObserveVaults()

        instance = ObserveUsableVaultsImpl(
            getUserPlan = getUserPlan,
            observeVaults = observeVaults
        )
    }

    @Test
    fun `only writeable vaults if plan is free`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Free("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(2)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2)
        assertThat(res.map { it.role }).containsExactly(ShareRole.Write, ShareRole.Write)
    }

    @Test
    fun `owned vaults and first N shared vaults if plan is free`() = runTest {
        // Setup plan with 2 owned vault limit
        val plan = Plan(
            planType = PlanType.Free("", ""),
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(2),
            aliasLimit = PlanLimit.Limited(1),
            totpLimit = PlanLimit.Limited(1),
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(userId = DEFAULT_USER_ID, value = Result.success(plan))

        // 3 vault setup, one owned, two not owned
        // shared1 is Read to show that the order does not care about the role
        val now = Date()
        val otherUserId = UserId("OtherUserID")
        val ownedVault1 = TestVault.create(
            shareId = ShareId("owned1"),
            userId = DEFAULT_USER_ID,
            role = ShareRole.Write,
            name = "unused",
            createTime = now
        )
        val sharedVault1 = TestVault.create(
            shareId = ShareId("shared1"),
            userId = otherUserId,
            isOwned = false,
            role = ShareRole.Read,
            name = "unused",
            createTime = now.plusHours(1)
        )
        val sharedVault2 = TestVault.create(
            shareId = ShareId("shared2"),
            userId = otherUserId,
            isOwned = false,
            role = ShareRole.Write,
            name = "unused",
            createTime = now.plusHours(2)
        )
        observeVaults.sendResult(Result.success(listOf(ownedVault1, sharedVault1, sharedVault2)))

        val res = instance().first()
        assertThat(res).hasSize(2)
        assertThat(res).isEqualTo(listOf(ownedVault1, sharedVault1))
    }

    @Test
    fun `only writeable vaults if plan is unknown`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Unknown("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(2)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2)
        assertThat(res.map { it.role }).containsExactly(ShareRole.Write, ShareRole.Write)

    }

    @Test
    fun `all vaults if plan is plus`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Paid.Plus("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(3)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2, vault3)
        assertThat(res.map { it.role }).containsExactly(
            ShareRole.Write,
            ShareRole.Write,
            ShareRole.Read
        )
    }

    @Test
    fun `all vaults if plan is business`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Paid.Business("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(3)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2, vault3)
        assertThat(res.map { it.role }).containsExactly(
            ShareRole.Write,
            ShareRole.Write,
            ShareRole.Read
        )
    }

    @Test
    fun `all vaults if plan is trial`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Trial("", "", remainingDays = 1))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).hasSize(3)
        assertThat(res.map { it.shareId }).containsExactly(vault1, vault2, vault3)
        assertThat(res.map { it.role }).containsExactly(
            ShareRole.Write,
            ShareRole.Write,
            ShareRole.Read
        )
    }

    private fun setVaults(vaults: Map<ShareId, ShareRole>) {
        val vaultInstances: List<Vault> = vaults.map { (shareId, role) ->
            TestVault.create(
                shareId = shareId,
                userId = DEFAULT_USER_ID,
                role = role,
                name = "unused"
            )
        }

        observeVaults.sendResult(Result.success(vaultInstances))
    }

    private fun setPlan(planType: PlanType) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(1),
            aliasLimit = PlanLimit.Limited(1),
            totpLimit = PlanLimit.Limited(1),
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(userId = DEFAULT_USER_ID, value = Result.success(plan))
    }

    private fun Date.plusHours(hours: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.HOUR, hours)
        return calendar.time
    }

    companion object {
        val DEFAULT_USER_ID = UserId("default-user-id")
    }

}
