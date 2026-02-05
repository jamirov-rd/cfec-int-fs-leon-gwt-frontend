package com.cfecweb.leon.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.cfecweb.leon.AppProperties;
import com.cfecweb.leon.client.api.AppApi;
import com.cfecweb.leon.client.api.EntitiesApi;
import com.cfecweb.leon.client.api.FisheryApi;
import com.cfecweb.leon.client.api.FormsApi;
import com.cfecweb.leon.client.api.PaymentsApi;
import com.cfecweb.leon.client.api.PermitsApi;
import com.cfecweb.leon.client.api.SessionApi;
import com.cfecweb.leon.client.api.VesselsApi;
import com.cfecweb.leon.client.invoker.ApiClient;
import com.cfecweb.leon.client.invoker.ApiException;
import com.cfecweb.leon.client.model.CheckVesselRequest;
import com.cfecweb.leon.client.model.CreateOrderProcessingPrerequisitesRequest;
import com.cfecweb.leon.client.model.EmailCommentsRequest;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.client.model.ClientPaymentContext;
import com.cfecweb.leon.client.model.GetFisheryTableRequest;
import com.cfecweb.leon.client.model.PaymentProcessingContextAndFields;
import com.cfecweb.leon.client.model.ProcessChangeRequest;
import com.cfecweb.leon.client.model.ProcessOrderRequest;
import com.cfecweb.leon.client.model.SortPlistRequest;
import com.cfecweb.leon.client.model.SortVlistRequest;
import com.cfecweb.leon.dto.UserSessionSettings;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import com.cfecweb.leon.client.getData;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.GWTfisheryTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * This is the server side remote service for RCP calls. I try to keep the methods
 * as stubs, then do the work in another class, keeps it clean that way. All RCP calls
 * from the client must go through this file first.
 */

public class getDataImpl extends RemoteServiceServlet implements getData {
    private static final Logger LOGGER = LogManager.getLogger(getDataImpl.class);

    private static final ApiClient CLIENT;
    public static final AppApi APP_API;
    public static final FisheryApi FISHERY_API;
    public static final EntitiesApi ENTITIES_API;
    public static final PaymentsApi PAYMENTS_API;
    public static final FormsApi FORMS_API;
    public static final VesselsApi VESSELS_API;
    public static final PermitsApi PERMITS_API;
    public static final SessionApi SESSION_API;

    private static final int TIME_IN_MINUTES = 20;
    private static final String LEON_PROP_LOCATION = "/home/tomcat/properties/leon.properties";
    public static final Properties LEON_PROP = new Properties();

    static {
        CLIENT = new ApiClient();
        CLIENT.setBasePath("https://api.example.com"); //TODO: get REST Service Path from settings

        APP_API = new AppApi(CLIENT);
        FISHERY_API = new FisheryApi(CLIENT);
        ENTITIES_API = new EntitiesApi(CLIENT);
        PAYMENTS_API = new PaymentsApi(CLIENT);
        FORMS_API = new FormsApi(CLIENT);
        VESSELS_API = new VesselsApi(CLIENT);
        PERMITS_API = new PermitsApi(CLIENT);
        SESSION_API = new SessionApi(CLIENT);

        try {
            LEON_PROP.load(new FileInputStream(LEON_PROP_LOCATION));
            // Printing the properties
            LOGGER.info("Printing leonprop properties:");
            for (String key : LEON_PROP.stringPropertyNames()) {
                String value = LEON_PROP.getProperty(key);
                LOGGER.info("{}: {}", key, value);
            }
        } catch (IOException e) {
            LOGGER.error("Error in loading LEON properties", e);
        }
    }

	/*
	 *  Passes CFECID and return list of FisheryTable objects(non-Javadoc)
	 * @see com.cfecweb.leon.client.getData#getfshytable(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List)
	 */
    @Override
	public List<GWTfisheryTable> getfshytable(String id, String res, String pov, String yr, List<ArenewPermits> pmt) {
        try {
            return FISHERY_API.getFisheryTable(new GetFisheryTableRequest().id(id).res(res).pov(pov).yr(yr).pmt(pmt));
        } catch (ApiException e) {
            LOGGER.error("Error in getfshytable ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 *  Passes Strings for CFECID and required initial options, returns something(non-Javadoc)
	 * @see com.cfecweb.leon.client.getData#getVitals(java.lang.String, java.lang.String, boolean, boolean)
	 */
    @Override
	public ArenewEntity getVitals(String id, String ryear, boolean option, boolean poverty) {
        try {
            return ENTITIES_API.getVitals(id, ryear, option, poverty);
        } catch (ApiException e) {
            LOGGER.error("Error in getVitals ", e);
            throw new RuntimeException(e);
        }
	}

    // Forms prerequisites for order processing using Secure Acceptance Hosted Checkout (CyberSource)
    @SuppressWarnings("unused")
    @Override
    public PaymentProcessingContextAndFields createOrderProcessingPrerequisites(ArenewEntity ent, ArenewPayment pay, List<ArenewChanges> chg, List<ArenewPermits> plist, List<ArenewVessels> vlist,
                                                                                List<ArenewPermits> pclist, List<ArenewVessels> vclist,
                                                                                boolean halred, boolean sabred, FeeTotals feeTotals, boolean firstTime, String ryear, String pmtvesCount, String topLeftText,
                                                                                String captchaToken
                                                                                ) {
        try {
            return PAYMENTS_API.createOrderProcessingPrerequisites(new CreateOrderProcessingPrerequisitesRequest()
                            .ent(ent).pay(pay).chg(chg).plist(plist).vlist(vlist).pclist(pclist).vclist(vclist)
                            .halred(halred).sabred(sabred).feeTotals(feeTotals).firstTime(firstTime)
                            .ryear(ryear).pmtvesCount(pmtvesCount).topLeftText(topLeftText).captchaToken(captchaToken)
                    );
        } catch (ApiException e) {
            LOGGER.error("Error in createOrderProcessingPrerequisites ", e);
            throw new RuntimeException(e);
        }
    }

	/*
	 * Passes various LEON objects after final selection and processing button to a method that will complete the DB work and charge the CC, if automatic.
	 * @see com.cfecweb.leon.client.getData#processOrder(com.cfecweb.leon.shared.ArenewEntity, com.cfecweb.leon.shared.ArenewPayment, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
    @Override
	public ClientPaymentContext processOrder(String ref) {
        try {
            return PAYMENTS_API.processOrder(new ProcessOrderRequest().ref(ref));
        } catch (ApiException e) {
            LOGGER.error("Error in processOrder ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Generates necessary CFEC form(s) link for user wishing to download standard and/or pre-printed forms
	 * @see com.cfecweb.leon.client.getData#getForms(java.lang.String, java.lang.String)
	 */
    @Override
	public List<String> getForms(String id, String ryear) {
        try {
            return FORMS_API.getForms(id, ryear);
        } catch (ApiException e) {
            LOGGER.error("Error in getForms ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Receives and processes any changes the user may have made to their records
	 * @see com.cfecweb.leon.client.getData#processChange(java.lang.String, java.lang.String, java.util.List)
	 */
    @Override
	public String processChange(String id, String ryear, List<ArenewChanges> chg) {
        try {
            return ENTITIES_API.processChange(new ProcessChangeRequest().id(id).ryear(ryear).chg(chg));
        } catch (ApiException e) {
            LOGGER.error("Error in processChange ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Sends an email to CFEC with user comments
	 * @see com.cfecweb.leon.client.getData#emailComments(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
	public void emailComments(String subject, String body, String to, String from) {
        try {
            APP_API.emailComments(new EmailCommentsRequest().subject(subject).body(body).to(to).from(from));
        } catch (ApiException e) {
            LOGGER.error("Error in emailComments ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Should be the first RPC call from all UIDef*.onModuleLoad()
	 * 
	 * @return java.lang.Integer (-1 if the user session has already timed out,
	 *         otherwise, the number of milliseconds)
	 */
    @Override
	public UserSessionSettings getUserSessionTimeoutMillis() {
		HttpServletRequest request = this.getThreadLocalRequest();
		HttpSession session = request.getSession();
		//HttpSession session = getThreadLocalRequest().getSession(true);
		if (session.getAttribute("active")!=null) {
            LOGGER.info("session id {} has been re-activated", session.getId());
			session.setMaxInactiveInterval(TIME_IN_MINUTES * (60 * 1000));
		} else {
			//session = getThreadLocalRequest().getSession();
			session.setAttribute("active", true);
            LOGGER.info("session id {} has been activated", session.getId());
			session.setMaxInactiveInterval(TIME_IN_MINUTES * (60 * 1000));
		}

        try {
            com.cfecweb.leon.client.model.UserSessionSettings result = SESSION_API.getUserSessionTimeoutMillis();
            return new UserSessionSettings(session.getMaxInactiveInterval(), result.getRenewalYear(),
                    result.getRecaptchaSiteKey(), result.getRecaptchaAction());
        } catch (ApiException e) {
            LOGGER.error("Error in getUserSessionTimeoutMillis ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Method to kill the current session after 20 minutes of inactivity
	 * @see com.cfecweb.leon.client.getData#killSession()
	 */
    @Override
	public String killSession() {
        try {
            HttpServletRequest request = this.getThreadLocalRequest();
            HttpSession session = request.getSession();
            LOGGER.info("session id {} has been invalidated thru inactivity", session.getId());
            session.invalidate();
            return null;
        } catch (Exception e) {
            LOGGER.error("Error in killSession ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Sorts a permit list and returns a list of objects
	 * @see com.cfecweb.leon.client.getData#sortPlist(java.util.List, java.lang.String)
	 */
    @Override
	public List<ArenewPermits> sortPlist(List<ArenewPermits> plist, String poverty) {
        try {
            return PERMITS_API.sortPlist(new SortPlistRequest().plist(plist).poverty(poverty));
        } catch (ApiException e) {
            LOGGER.error("Error in sortPlist ", e);
            throw new RuntimeException(e);
        }
	}
	
	/*
	 * Sorts a vessel list and returns a list of objects
	 * @see com.cfecweb.leon.client.getData#sortVlist(java.util.List)
	 */
    @Override
	public List<ArenewVessels> sortVlist(List<ArenewVessels> vlist) {
        try {
            return VESSELS_API.sortVlist(new SortVlistRequest().vlist(vlist));
        } catch (ApiException e) {
            LOGGER.error("Error in sortVlist ", e);
            throw new RuntimeException(e);
        }
	}

	/*
	 * Returns an object of a specific vessel by ADFG number
	 * @see com.cfecweb.leon.client.getData#getsingleVessel(java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
	public ArenewVessels getsingleVessel(String adfg, String ryear, String cfecid) {
        try {
            return VESSELS_API.getSingleVessel(adfg, ryear, cfecid);
        } catch (ApiException e) {
            LOGGER.error("Error in getsingleVessel ", e);
            throw new RuntimeException(e);
        }
	}
	
	@Override
	public List<ArenewPermits> checkVessel(List<ArenewPermits> plist, String ryear) {
        try {
            return VESSELS_API.checkVessel(new CheckVesselRequest().plist(plist).ryear(ryear));
        } catch (ApiException e) {
            LOGGER.error("Error in checkVessel ", e);
            throw new RuntimeException(e);
        }
	}
	
	@Override
	public String checkCC(ArenewPayment pay) {
        try {
            return PAYMENTS_API.checkCC(pay);
        } catch (ApiException e) {
            LOGGER.error("Error in checkCC ", e);
            throw new RuntimeException(e);
        }
	}

    /// Retrieves the application version string that was loaded from the
    /// server-side `app.properties` file.
    /// This value is injected at build time via Maven resource filtering
    /// and made available through [AppProperties].
    ///
    /// @return the application version, or `"unknown"` if the property
    ///         is not defined or cannot be read
    @Override
    public String getAppVersion() {
        return AppProperties.get(AppProperties.APP_VERSION, "unknown");
    }

}
