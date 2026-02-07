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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("getData")
public interface getData extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static getDataAsync instance;
		public static getDataAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(getData.class);
			}
			return instance;
		}
	}
	
    //	Passes ADFG and RenewYear Strings to check if vessel is already licensed
	public List<ArenewPermits> checkVessel(List<ArenewPermits> plist, String ryear);
	//	Passes Strings for CFECID and required initial options, returns something
	public ArenewEntity getVitals(String id, String ryear, boolean option, boolean poverty);
	//	Passes Strings for CFECID and year for Forms section
	public List<String> getForms(String id, String ryear);
	//	Passes CFECID and return list of FisheryTable objects
	public List<GWTfisheryTable> getfshytable(String id, String res, String pov, String yr, List<ArenewPermits> pmt);
    //  Forms prerequisites for order processing using Secure Acceptance Hosted Checkout (CyberSource)
    PaymentProcessingContextAndFields createOrderProcessingPrerequisites(ArenewEntity ent, ArenewPayment pay, List<ArenewChanges> chg, List<ArenewPermits> plist, List<ArenewVessels> vlist,
                                                                         List<ArenewPermits> pclist, List<ArenewVessels> vclist,
                                                                         boolean halred, boolean sabred, FeeTotals feeTotals, boolean firstTime, String ryear, String pmtvesCount, String topLeftText,
                                                                         String captchaToken);
    //	Passes all completed list and returns confirmation download URL string
	public ClientPaymentContext processOrder(String ref);
	//	Passes CFECID and Change list to record in database, return success String
	public String processChange(String id, String ryear, List<ArenewChanges> chg);
	//	Passes Email comments to server
	public void emailComments(String subject, String body, String to, String from);
	//	Gets current session time
	public UserSessionSettings getUserSessionTimeoutMillis();
	//	Kills current session
	public String killSession();
	//	sends and receives back a sorted permit list
	public List<ArenewPermits> sortPlist(List<ArenewPermits> plist, String poverty);
	//	sends and receives back a sorted vessel list
	public List<ArenewVessels> sortVlist(List<ArenewVessels> vlist);
	//	Passes adfg and ryear, returns a single vessel object
	public ArenewVessels getsingleVessel(String adfg, String ryear, String cfecid);
    //  Passes the payment object, returns a value representing CC validity
	public String checkCC(ArenewPayment pay);
    // Returns the application version string loaded from app.properties.
    String getAppVersion();
}
