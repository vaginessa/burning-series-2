/*
 * MobileDataAlertDialog.java
 *
 * Copyright (c) 2017 Malik
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files(the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package de.m4lik.burningseries.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.ui.base.DialogBase;

/**
 * Created by Malik on 07.06.2017
 *
 * @author Malik Mann
 */

public class MobileDataAlertDialog extends DialogBase {

    public static MobileDataAlertDialog newInstace() {
        return new MobileDataAlertDialog();
    }

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return DialogBuilder.start(getActivity())
                .title("Mobile Daten")
                .content("Achtung! Du bist über mobile Daten im Internet. Willst du Fortfahren?")
                .positive("Weiter", dialog ->
                        getTargetFragment()
                                .onActivityResult(
                                        getTargetRequestCode(), Activity.RESULT_OK, null))
                .negative("Abbruch", dialog -> dismiss())
                .cancelable()
                .build();
    }
}
