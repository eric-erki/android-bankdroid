package com.liato.bankdroid.banking.banks.swedbank;

import android.content.Context;

import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import java.io.IOException;

public class SwedbankCorporate extends AbstractSwedbank {

    private static final String TAG = "Swedbank Företag";
    private static final String NAME = "Swedbank Företag";
    private static final String NAME_SHORT = "swedbank-corporate";
    private static final int BANKTYPE_ID = IBankTypes.SWEDBANK_CORPORATE;

    private static final String APP_ID = "Our91qzclXdNmpdE";

    public SwedbankCorporate(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;

    }

    public SwedbankCorporate(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }

    @Override
    public int getImageResource() {
        return res.getIdentifier("logo_" + Swedbank.NAME_SHORT, "drawable", context.getPackageName());
    }
}
