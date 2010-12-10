package com.liato.bankdroid.banks;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class Steam extends Bank {
	private static final String TAG = "Steam";
	private static final String NAME = "Steam Wallet";
	private static final String NAME_SHORT = "steam";
	private static final String URL = "https://store.steampowered.com/login/?redir=account";
	private static final int BANKTYPE_ID = Bank.STEAM;
	private static final boolean STATIC_BALANCE = true;
	
    private Pattern reBalance = Pattern.compile("accountBalance\">\\s*<div[^>]+>([^<]+)</div>", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("(?:even|odd)\">\\s*<div\\s*class=\"transactionRowDate\">([^<]+)</div>\\s*<div.*?RowPrice\">([^<]+)</div>\\s*<div.*?RowEvent\">([^<]+)</div>.*?RowTitle\">([^<]+)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private String response = null;
	
	public Steam(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.STATIC_BALANCE = STATIC_BALANCE;
	}

	public Steam(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true);
		try {
            List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("redir", "account"));
            postData.add(new BasicNameValuePair("username", username));
            postData.add(new BasicNameValuePair("password", password));
			response = urlopen.open("https://store.steampowered.com/login/", postData);
            if (response.contains("Enter the characters above")) {
                throw new BankException("You have entered the wrong username/password too many times and Steam now requires you to enter a CAPTCHA.\nPlease wait 10 minutes before logging in again.");
            }
			if (response.contains("Incorrect login.")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		return urlopen;
	}

	@Override
	public void update() throws BankException, LoginException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		urlopen = login();
		Matcher matcher = reBalance.matcher(response);
		if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Amount            0,--&#8364;
             * 
             */
		    String amount = Html.fromHtml(matcher.group(1)).toString().trim();
		    Account account = new Account("Wallet", Helpers.parseBalance(amount), "1");
		    String currency = Helpers.parseCurrency(amount, "USD");
		    this.setCurrency(currency);
		    account.setCurrency(currency);
		    balance = balance.add(Helpers.parseBalance(amount));
		    ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		    matcher = reTransactions.matcher(response);
		    while (matcher.find()) {
	            /*
	             * Capture groups:
	             * GROUP                EXAMPLE DATA
	             * 1: Date              18 Oct 2007
	             * 2: Amount            &#36;62.44
	             * 3: Event             Purchase
	             * 4: Item              The Orange Box
	             * 
	             */
                SimpleDateFormat sdfFrom = new SimpleDateFormat("d MMM yyyy");
                SimpleDateFormat sdfTo = new SimpleDateFormat("yyyy-MM-dd");
		        Date transactionDate;
                try {
                    transactionDate = sdfFrom.parse(matcher.group(1).trim());
                    String strDate = sdfTo.format(transactionDate);
                    transactions.add(new Transaction(strDate,
                                                     Html.fromHtml(matcher.group(4)).toString().trim(),
                                                     Helpers.parseBalance(Html.fromHtml(matcher.group(2)).toString().trim()),
                                                     Helpers.parseCurrency(Html.fromHtml(matcher.group(2)).toString().trim(), "USD")));
                }
                catch (ParseException e) {
                    Log.d(TAG, "Unable to parse date: " + matcher.group(1).trim());
                }
		    }
		    account.setTransactions(transactions);
		    accounts.add(account);
		}
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }		
        super.updateComplete();
	}
}
