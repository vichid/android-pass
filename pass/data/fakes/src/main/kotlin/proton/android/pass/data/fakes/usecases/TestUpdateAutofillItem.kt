package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import javax.inject.Inject

class TestUpdateAutofillItem @Inject constructor() : UpdateAutofillItem {

    override fun invoke(data: UpdateAutofillItemData) {}
}
