package com.cfecweb.leon.client;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.AppProperties;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.client.model.ClientPaymentContext;
import com.cfecweb.leon.client.model.PaymentProcessingContextAndFields;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ScreenProcessing {
	InstructionsText gins = new InstructionsText();
	UserMessages gmsg = new UserMessages();
	ChangeRecorder cr = new ChangeRecorder();
	String cn = null;	
	boolean isnewVes = false;
	boolean isnewPmt = false;
	StringBuffer newvesb = new StringBuffer();
	StringBuffer newpmtb = new StringBuffer();
	String newpmts = null;
	String newvess = null;

    public static native void fetchToken(String siteKey, String action, TokenHandler handler) /*-{
        if (!$wnd.getRecaptchaToken) {
            $wnd.getRecaptchaToken = function(siteKey, action, cb, errCb) {
                if (!$wnd.grecaptcha || !$wnd.grecaptcha.ready) {
                    if (errCb) errCb("grecaptcha not available");
                    return;
                }
                $wnd.grecaptcha.ready(function() {
                    try {
                        $wnd.grecaptcha.execute(siteKey, { action: action }).then(function(token) {
                            cb(token);
                        }, function(reason) {
                            if (errCb) errCb("execute rejected: " + reason);
                        });
                    } catch (e) {
                        if (errCb) errCb("execute threw: " + e);
                    }
                });
            };
        }

        $wnd.getRecaptchaToken(siteKey, action,
            function(token) {
                handler.@com.cfecweb.leon.client.TokenHandler::onToken(Ljava/lang/String;)(token);
            },
            function(msg) {
                handler.@com.cfecweb.leon.client.TokenHandler::onError(Ljava/lang/String;)(msg);
            });
    }-*/;

	/*
	 * This class represents the final UI in the process, which basically appears after final confirmation. It presents the user with the
	 * final total, a confirmation number and the option to download a confirmation report. If the person entered an email address ealier
	 * in the process (Address Screen), the confirmation report will automatically be emailed to them.
	 */
	public void getConfirm(final VerticalPanel bottomLeftVPanel, final FieldSet bottomRight,
			final Button startOver, final TextField cfecid, final Button next, final Button last, final HTML statusBar, String pmtvesCount,
			final ArenewEntity entity, final List<ArenewChanges> changeList,
			final FeeTotals feeTotals, final getDataAsync service, String topLeftText, final boolean firstTime,
			final ArenewPayment payment, final List<ArenewPermits> plist, final List<ArenewVessels> vlist, List<ArenewPermits> pclist,
			List<ArenewVessels> vclist, final String ryear, final boolean halred, final boolean sabred, final String reCaptchaSiteKey, final String reCaptchaAction) {
		/*
		 * Define the processesing screen page enviroment
		 */
		bottomRight.removeAll();
		bottomLeftVPanel.removeAll();
		startOver.setEnabled(false);
		bottomLeftVPanel.addText(gins.getProcess());
	    bottomLeftVPanel.layout();
	    cfecid.disable();
	    statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Order Processing Section</span>");	    
	    bottomRight.setHeadingHtml("<span class='boldblack12'>Order Processing Panel</span>");
	    //bottomRight.layout();
	    DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar2").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar3").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar4").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar5").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar6").getStyle().setProperty("background", "#FFFFCC");
	    /*
	     * 	Define the NEXT Button
	     */
	    next.removeAllListeners();
	    next.setEnabled(false);
	    /*
	     * 	Define the LAST Button
	     */
	    last.removeAllListeners();
	    last.setEnabled(false);	    	    
	    gmsg.waitStart("Processing Order", "Redirecting to secure checkout, please wait", "redirecting....", 250);
	    
	    /*
	     * This next iterator checks each vessel that was renewed for troll date
	     * compliance. If there troll date is prior to todays date, then make
	     * the troll date effective today.
	     */
	    Date today = new Date();
	    DateTimeFormat dtformat = DateTimeFormat.getFormat("yyyy-MM-dd");
	    for (Iterator vt = vlist.iterator(); vt.hasNext();) {
	    	ArenewVessels tc = (ArenewVessels) vt.next();
	    	if (tc.getNewrenew() || tc.getNewVessel()) {
	    		if (!(tc.getSalmontrollDate() == null)) {
	    			if (!(tc.getSalmontrollDate().equalsIgnoreCase("N/A"))) {
	    				if (dtformat.parse(tc.getSalmontrollDate()).before(today)) {
	    					cr.getVesselChanges3(entity, "salmonTrollDate", cfecid.getValue().toString().toUpperCase(), dtformat.parse(tc.getSalmontrollDate()), today, changeList, tc.getId().getAdfg());
			    			tc.setSalmontrollDate(DateTimeFormat.getFormat("yyyy-MM-dd").format(today));
			    		}
	    			}		    			
	    		}		    		
	    	}
	    }
	    
	    /*
	     * Next, take the final plist and vlist objects and set them as entity permits and vessels
	     */
	    entity.getArenewPermitses().clear();
	    entity.getArenewVesselses().clear();
	    isnewPmt = false;
	    for (Iterator fp = plist.iterator(); fp.hasNext();) {
	    	ArenewPermits per = (ArenewPermits) fp.next();
	    	if (per.getId().getSerial().equalsIgnoreCase("Not Issued")) {
	    		if (per.getNewpermit()) {
		    		newpmtb.append(per.getId().getFishery() + "/");
		    		isnewPmt = true;
		    	}
	    	}	    	
	    	entity.getArenewPermitses().add(per);
	    }
	    
	    isnewVes = false;
	    for (Iterator fv = vlist.iterator(); fv.hasNext();) {
	    	ArenewVessels ves = (ArenewVessels) fv.next();
	    	if (ves.getId().getAdfg().equalsIgnoreCase("N/A")) {
				if (ves.getNewrenew()) {
					if (!(ves.getName()==null)) {
						newvesb.append(ves.getName() + "/");
					} else {
						newvesb.append("NoName/");
					}
					isnewVes = true;
				}
	    	}
	    	if (ves.getId().getAdfg().equalsIgnoreCase("N/A")) {
				if (ves.getNewVessel()) {
					if (!(ves.getName()==null)) {
						newvesb.append(ves.getName() + "/");
					} else {
						newvesb.append("NoName/");
					}
					isnewVes = true;
				}
	    	}
	    	entity.getArenewVesselses().add(ves);
	    }
	    if (newpmtb.length() > 1) {
	    	newpmts = newpmtb.toString().substring(0,newpmtb.toString().length()-1);
	    }
	    if (newvesb.length() > 1) {
	    	newvess = newvesb.toString().substring(0,newvesb.toString().length()-1);
	    }
	    
	    /*
	     * then attach the entire change object for this session
	     */
	    entity.getArenewChangeses().clear();
	    entity.getArenewChangeses().addAll(changeList);
	    if (firstTime) {
	    	entity.setFirsttime("false");
	    }

        final List<ArenewPermits> finalPclist = pclist;
        final List<ArenewVessels> finalVclist = vclist;
        final String finalPmtvesCount = pmtvesCount;
        final String finalTopLeftText = topLeftText;

        fetchToken(reCaptchaSiteKey, reCaptchaAction, new TokenHandler() {
            @Override
            public void onToken(String captchaToken) {

                // Send a request to the server to get signed fields and signature
                service.createOrderProcessingPrerequisites(entity, payment, changeList, plist, vlist, finalPclist, finalVclist,
                        halred, sabred, feeTotals, firstTime, ryear, finalPmtvesCount, finalTopLeftText,
                        captchaToken,
                    new AsyncCallback<PaymentProcessingContextAndFields>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            gmsg.waitStop();
                            bottomRight.removeAll();
                            bottomRight.addText("<br><center><span class='regblack12'>There has been a technical error with your order and it HAS NOT been processed. " +
                                    "CFEC's information Technology department has been informed and will be working on the problem. If you have any " +
                                    "questions please contact us at 1-907-789-6160 (option #4).</span></center><br>");
                            ButtonBar resetBB = new ButtonBar();
                            resetBB.setAlignment(HorizontalAlignment.CENTER);
                            Button reset = startOver;
                            reset.setText("Reset Application");
                            reset.setEnabled(true);
                            resetBB.add(reset);
                            bottomRight.add(new HTML("<br>"));
                            bottomRight.add(resetBB);
                            bottomRight.layout();
                        }

                        @Override
                        public void onSuccess(PaymentProcessingContextAndFields context) {
                            redirectToHostedPayment(context.getFields(), context.getRedirectUrl());
                        }

                        private void redirectToHostedPayment(Map<String, String> fields, String redirectUrl) {
                            // 1) Create form
                            com.google.gwt.user.client.ui.FormPanel form = new com.google.gwt.user.client.ui.FormPanel();
                            form.setAction(redirectUrl);
                            form.setMethod(com.google.gwt.user.client.ui.FormPanel.METHOD_POST);
                            // Keep in same tab:
                            form.getElement().setAttribute("target", "_self");

                            // 2) Add hidden inputs
                            com.google.gwt.user.client.ui.FlowPanel container = new com.google.gwt.user.client.ui.FlowPanel();
                            for (Map.Entry<String, String> e : fields.entrySet()) {
                                container.add(new com.google.gwt.user.client.ui.Hidden(e.getKey(), e.getValue() == null ? "" : e.getValue()));
                            }
                            form.setWidget(container);

                            // 3) Attach, submit, and optionally clean up
                            com.google.gwt.user.client.ui.RootPanel.get().add(form);
                            form.submit();
                        }
                    });
            }

            @Override
            public void onError(String message) {
                Log.error("ReCaptcha Token retrieval error: " + message);

                gmsg.waitStop();
                bottomRight.removeAll();
                bottomRight.addText("<br><center><span class='regblack12'>There has been a technical error with your order and it HAS NOT been processed. " +
                        "CFEC's information Technology department has been informed and will be working on the problem. If you have any " +
                        "questions please contact us at 1-907-789-6160 (option #4).</span></center><br>");
                ButtonBar resetBB = new ButtonBar();
                resetBB.setAlignment(HorizontalAlignment.CENTER);
                Button reset = startOver;
                reset.setText("Reset Application");
                reset.setEnabled(true);
                resetBB.add(reset);
                bottomRight.add(new HTML("<br>"));
                bottomRight.add(resetBB);
                bottomRight.layout();
            }
        });
    }

    public static void reopenBasePage() {
        // Base pieces
        String protocol = Window.Location.getProtocol();
        String host = Window.Location.getHost();

        // Current path, without any path parameters (e.g., ;jsessionid)
        String path = Window.Location.getPath();
        int semicolon = path.indexOf(';');
        if (semicolon != -1) {
            path = path.substring(0, semicolon);
        }

        // Build base URL
        StringBuilder base = new StringBuilder();
        base.append(protocol).append("//").append(host);
        base.append(path);

        // Navigate to the clean URL (same tab, adds to history)
        Window.Location.assign(base.toString());
    }

    public void paymentPostProcess(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight,
                                   final FieldSet topRight, final Button startOver, final TextField cfecid, final Button next,
                                   final Button last, final HTML statusBar, final HTML phrdText,
                                   final HorizontalPanel NavprogressBarPanel, final getDataAsync service,
                                   final SessionTimer timer, final CheckBox first, final CheckBox second, final CheckBox nop,
                                   String ref, final String reCaptchaSiteKey, final String reCaptchaAction
    ) {
        gmsg.waitStart("Finalizing Order", "Finalizing order, please wait", "finalizing....", 250);

        /*
         * Define the processesing screen page enviroment
         */
        bottomRight.removeAll();
        bottomLeftVPanel.removeAll();
        startOver.setEnabled(false);
        bottomLeftVPanel.addText(gins.getProcess());
        bottomLeftVPanel.layout();
        cfecid.disable();
        statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Order Processing Section</span>");
        bottomRight.setHeadingHtml("<span class='boldblack12'>Order Processing Panel</span>");
        //bottomRight.layout();

        /*
         * 	Define the NEXT Button
         */
        next.removeAllListeners();
        next.setEnabled(false);
        /*
         * 	Define the LAST Button
         */
        last.removeAllListeners();
        last.setEnabled(false);

	    service.processOrder(ref, new AsyncCallback<ClientPaymentContext>() {
			@Override
			public void onFailure(Throwable caught) {
				gmsg.waitStop();
				bottomRight.removeAll();
			    bottomRight.addText("<br><center><span class='regblack12'>There has been a technical error with your order and it HAS NOT been processed. " +
			    	"CFEC's information Technology department has been informed and will be working on the problem. If you have any " +
			    	"questions please contact us at 1-907-789-6160 (option #4).</span></center><br>");
			    ButtonBar resetBB = new ButtonBar();
			    resetBB.setAlignment(HorizontalAlignment.CENTER);
			    Button reset = startOver;
			    reset.setText("Reset Application");
			    reset.setEnabled(true);
                reset.removeAllListeners();
                reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) { reopenBasePage(); }
                });
			    resetBB.add(reset);
			    bottomRight.add(new HTML("<br>"));
			    bottomRight.add(resetBB);
			    bottomRight.layout();
			}
			@Override
			public void onSuccess(ClientPaymentContext clientPaymentContext) {
                final String result = clientPaymentContext.getResult();

                final ArenewEntity entity = clientPaymentContext.getEntity();
                final ArenewPayment payment = clientPaymentContext.getPayment();
                final List<ArenewChanges> changeList = clientPaymentContext.getChangeList();
                final List<ArenewPermits> plist = clientPaymentContext.getPlist();
                final List<ArenewVessels> vlist = clientPaymentContext.getVlist();

                final boolean halred = clientPaymentContext.getHalred();
                final boolean sabred = clientPaymentContext.getSabred();
                final FeeTotals feeTotals = clientPaymentContext.getFeeTotals();
                final boolean firstTime = clientPaymentContext.getFirstTime();
                final String ryear = clientPaymentContext.getRyear();
                final String pmtvesCount = clientPaymentContext.getPmtvesCount();
                final String topLeftText = clientPaymentContext.getTopLeftText();

				gmsg.waitStop();
				bottomRight.removeAll();
				cn = result.toString();				
				Log.info(entity.getId().getCfecid() + " object result is " + result.toString());
				StringBuffer con = new StringBuffer("");
				if (result.toString().equalsIgnoreCase("error")) {
					con.append("<br><center><span class='regblack12'>There has been a technical error with your order and it HAS NOT been processed. " +
				    		"CFEC's information Technology department has been informed and will be working on the problem. " +
				    		"If you have any questions please contact us at 907-789-6160 (option #4).</span></center><br>");
					bottomRight.addText(con.toString());
				} else if (result.toString().equalsIgnoreCase("declined")) {
					con.append("<br><center><span class='regblack12'>We apologize, but your credit card has been declined.<br>" +
				    		"If you have any questions please contact your bank, try another card<br> or check that the card information is correct<br>" +
				    		"by pressing the '<span class='boldblack12'>Go back to Payment Section</span>' button.<br>" +
				    		"You can also reset the application to the beginning by pressing the '<span class='boldblack12'>Reset Application</span>' button.</span></center><br>");
					bottomRight.addText(con.toString());
				} else {
					con.append("<div><center><span class='regblack12'>Your order has been successfully processed. Here is your confirmation code <b>" + result +".</b><br>" +
			    			"Your confirmation report has been emailed to you. If you have any problems, please call us at 907-789-6150 during normal business hours.</span></center></div><br><br>");
			    	boolean attach = false;
			    	if (entity.getAgentsub().equalsIgnoreCase("yes")) {
			    		con.append("<div><span class='regblack12'>You have specified that your are an Agent representing a permit/vessel owner.<br>You <u>MUST</u> provide " + 
				    		"a completed and notarized Agent Authorization Form to the Commission</span><br> " + 
				    		"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
				    		//"<br><span class='regblack12'>This application will be processed <b>AFTER</b> we receive and/or verify the supporting documentation.</span></div><br>");
				    	attach = true;
			    	}
			    	if (halred) {
			    		con.append("<div><span class='regblack12'>You have specified the REDUCED FEE option for a HALIBUT permit.<br>You <u>MUST</u> provide " + 
			    			"evidence that you either landed under 8000 lbs of halibut in the previous year OR you are a member of a Western AK CDQ halibut group.<br> " + 
			    			"If you did NOT fish this permit last year, then you must provide CFEC with supporting IFQ evidence of less than 8000 lbs of halibut this year.</span><br> " +
			    			"<span class='regred12'>Please submit your IFQ landing report OR statement of CDQ participation OR this years IFQ allotment to the Commission by one of the options below.</span>" +
			    			"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
			    			//"<br><span class='regblack12'>Your application will be processed <b>AFTER</b> we receive and/or verify this supporting documentation.</span></div><br>");
			    		attach = true;
			    	}
			    	if (sabred) {
			    		con.append("<div><span class='regblack12'>You have specified the REDUCED FEE option for a SABLEFISH permit.<br>You <u>MUST</u> provide " + 
			    			"evidence that you landed under 9000 lbs of sablefish in the previous year.<br>" +
			    			"If you did NOT fish this permit last year, then you must provide CFEC with supporting IFQ evidence of less than 9000 lbs of sablefish this year.</span><br> " +
			    			"<span class='regred12'>Please submit your IFQ landing report OR this years IFQ allotment to the Commission by one of the options below.<span><br>" +
			    			"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
			    			//"<span class='regblack12'>Your application will be processed <b>AFTER</b> we receive and/or verify this supporting documentation.</span></div><br>");
			    		attach = true;
			    	}
			    	if (entity.getCitizen().equalsIgnoreCase("false")) {
			    		con.append("<div><span class='regblack12'>You have specified that you are a not a US CITIZEN.</span><br>" + 
			    		    "<span class='regred12'>You must submit a copy of your green card to the Commission by one of the options below.</span><br>" +
			    		    "<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
			    			//"<span class='regblack12'>Your application will be processed <b>AFTER</b> we receive and/or verify this supporting documentation.</span></div><br>");
			    		attach = true;
					}
					if (entity.getPoverty().equalsIgnoreCase("true")) {
						con.append("<div><span class='regblack12'>You have specified the POVERTY FEE option for reduced permit fees.</span><br>" +
							"<span class='regred12'>You must provide a copy of your entire 2017 or 2018 tax return or,<br>" +
							"if you qualify for public assistance the most recent determination letter or copy of your Quest card.</span><br>" + 
							"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
				    		//"<span class='regblack12'>Your application will processed <b>AFTER</b> we receive and/or verify this supporting documentation.</span></div><br>");
						attach = true;
					} 
					if (isnewVes) {
						con.append("<div><span class='regblack12'>You have licensed new Vessel(s) ("+newvess+").</span><br>" +
							"<span class='regred12'>You must provide a copy of the vessel's USCG or AK documentation.<br>" +
							"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
					    	//"<span class='regblack12'>Your application will processed <b>AFTER</b> we received and/or this supporting documentation.</span></div><br>");
						attach = true;
					}
					//if (isnewPmt) {
					//	con.append("<div><span class='regblack12'>You have licensed a new Vessel.</span><br>" +
					//		"<span class='regred12'>You must provide a copy of the vessel's USCG or AK documentation.<br>" +
					//		"<span class='regred12'>If you have already provided this documentation, then no further action is required.</span></div><br>");
					    	//"<span class='regblack12'>Your application will processed <b>AFTER</b> we received and/or this supporting documentation.</span></div><br>");
					//	attach = true;
					//}
					if (attach) {
						con.append("<div>");
						con.append("<span class='regred12'>Your transaction is pending until said documentation is received and/or verified.<br>");
						con.append("<span class='regblack12'>EMAIL as attachment to <a href='mailto:DFG.CFEC.myinfo@alaska.gov" +
								"?subject=Documentation for CFECID "+entity.getId().getCfecid()+"&body=Please add your attached supporting documentation to this email " +
								"and send to CFEC. Please DO NOT attach or send any credit card information. PLEASE send ALL documentation required for this transaction " +
								"to expedite your transaction. If you have already provided CFEC with the supporting documentation, then we will verify and process " +
								"your order as quickly as possible.'>this email address</a></span><br>");
						con.append("<span class='regblack12'>Fax documentation to 907-789-6170</span><br>");
						con.append("<span class='regblack12'>Mail documentation to CFEC @ PO Box 110302, Juneau, AK 99811-0302</span>");
						con.append("</div>");
					}		    	
				   	ButtonBar dload = new ButtonBar();
			    	dload.setAlignment(HorizontalAlignment.CENTER);
			    	Button dform = new Button("<span class='regblue12'>Download your CFEC Confirmation Report here</span>", new SelectionListener<ButtonEvent>() {
			    		public void componentSelected(ButtonEvent ce) {
			    			com.google.gwt.user.client.Window.open("http://www.cfec.state.ak.us/leonOut/" +
			    				"confirmations/confirm_"+result+".pdf", "string1", "string2");
			    			Log.info(entity.getId().getCfecid() + " has downloaded their confirmation report confirm_"+cn+".pdf");
			    		}
			    	});
			    	dload.add(dform);
			    	bottomRight.addText(con.toString());
			    	//bottomRight.add(dload);			    
				}
				ButtonBar resetBB = new ButtonBar();
			    resetBB.setAlignment(HorizontalAlignment.CENTER);
			    Button reset = startOver;
			    Button rebill = new Button("<span class='boldblack12'>Go back to Payment Section</span>");
			    rebill.setEnabled(true);
			    reset.setText("Reset Application");
			    reset.setEnabled(true);
                reset.removeAllListeners();
                reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) { reopenBasePage(); }
                });
			    resetBB.add(reset);
			    rebill.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
					public void componentSelected(ButtonEvent ce) {
						timer.timerCancel();
			       	    timer.progReset();
			       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
			       	    ScreenBilling billing = new ScreenBilling();
		    			billing.getBilling(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, 
		    				pmtvesCount, NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, 
		    				vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
					}	    	
			    });
			    if (result.toString().equalsIgnoreCase("declined")) {
			    	resetBB.add(rebill);
			    }			    
			    bottomRight.add(new HTML("<br>"));
			    bottomRight.add(resetBB);
			    Log.info(bottomRight.toString());
			    bottomRight.layout();
			}	    	
	    });
	}
}
