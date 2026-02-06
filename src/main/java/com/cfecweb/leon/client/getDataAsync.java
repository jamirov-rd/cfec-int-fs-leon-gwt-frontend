package com.cfecweb.leon.client;

import java.util.List;

import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.client.model.GWTfisheryTable;
import com.cfecweb.leon.client.model.ClientPaymentContext;
import com.cfecweb.leon.client.model.PaymentProcessingContextAndFields;
import com.cfecweb.leon.dto.UserSessionSettings;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("rawtypes")
public interface getDataAsync {
    //	Passes ADFG and RenewYear Strings to check if vessel is already licensed
	public void checkVessel(List<ArenewPermits> plist, String ryear, AsyncCallback<List<ArenewPermits>> callback);
	//	Passes Strings for CFECID and required initial options, returns something
	public void getVitals(String id, String ryear, boolean option, boolean poverty, AsyncCallback<ArenewEntity> callback);
	//	Passes Strings for CFECID and year for Forms section
	public void getForms(String id, String ryear, AsyncCallback<List<String>> callback);
	//	Passes CFECID and return list of FisheryTable objects
	public void getfshytable(String id, String res, String pov, String yr, List<ArenewPermits> pmt, AsyncCallback<List<GWTfisheryTable>> callback);	
    //  Forms prerequisites for order processing using Secure Acceptance Hosted Checkout (CyberSource)
    void createOrderProcessingPrerequisites(ArenewEntity ent, ArenewPayment pay, List<ArenewChanges> chg, List<ArenewPermits> plist, List<ArenewVessels> vlist,
                                            List<ArenewPermits> pclist, List<ArenewVessels> vclist,
                                            boolean halred, boolean sabred, FeeTotals feeTotals, boolean firstTime, String ryear, String pmtvesCount, String topLeftText,
                                            String captchaToken,
                                            AsyncCallback<PaymentProcessingContextAndFields> callback);
	//	Passes all completed list and returns confirmation download URL string
	public void processOrder(String ref, AsyncCallback<ClientPaymentContext> callback);
	//	Passes CFECID and Change list to record in database, return success String
	public void processChange(String id, String ryear, List<ArenewChanges> changeList, AsyncCallback<String> callback);
	//	Passes Email comments to server
	public void emailComments(String subject, String body, String to, String from, AsyncCallback<Void> callback);
	//	Gets current session time
	public void getUserSessionTimeoutMillis(AsyncCallback<UserSessionSettings> async);
	//	Kills current session
	public void killSession(AsyncCallback async);
	//	sends and receives back a sorted permit list
	public void sortPlist(List<ArenewPermits> plist, String poverty, AsyncCallback<List<ArenewPermits>> callback);
	//	sends and receives back a sorted vessel list
	public void sortVlist(List<ArenewVessels> vlist, AsyncCallback<List<ArenewVessels>> callback);
	//	passes adfg and ryear, returns a single vessel object
	public void getsingleVessel(String adfg, String ryear, String cfecid, AsyncCallback<ArenewVessels> callback);
    //  passes a payment object, returns a string representing CC validity
	public void checkCC(ArenewPayment pay, AsyncCallback<String> callback);
    // Asynchronously retrieves application version from the server.
    void getAppVersion(AsyncCallback<String> callback);
}
