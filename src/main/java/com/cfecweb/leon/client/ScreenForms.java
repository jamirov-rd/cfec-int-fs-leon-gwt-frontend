package com.cfecweb.leon.client;

import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public class ScreenForms {
	InstructionsText gins = new InstructionsText();
	UserMessages gmsg = new UserMessages();
	ScreenAddress gadd = new ScreenAddress();
	ArenewEntity entity = null;	
	
	/*
	 * This class is the UI for our forms download section, including the persons current and previous pre-printed renweal(s). Recently added
	 * a button that will take the person from this UI directly to the Address screen without having to reset the application.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void forms(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight, final FieldSet topRight, final Button startOver, 
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText, final HorizontalPanel NavprogressBarPanel, 
			final getDataAsync service, final List<String> formlinks, final SessionTimer timer, 
			final String ryear, final boolean isAgent, final List<ArenewChanges> changeList, final ArenewPayment payment, final FeeTotals feeTotals,
			final List<ArenewPermits> plist, final List<ArenewVessels> vlist, final String agentName, final CheckBox first, final CheckBox second, 
			final CheckBox nop, final InitialOptions ginit, final String reCaptchaSiteKey, final String reCaptchaAction) {
		Log.info(cfecid.getValue().toString().toUpperCase() + " is accessing the Forms page");
		bottomRight.removeAll();
		topLeft.removeAll();
		topLeft.addText("<table width='100%' bgcolor='#FFFFCC' border='0' cellspacing ='0'><tr><td><br><span class='regblack12'><p><center>" +
		"No Permit Holder or Vessel Owner queried</center></p></span></td></tr></table>");
		topLeft.layout();
		bottomLeftVPanel.removeAll();
		bottomLeftVPanel.add(startOver);
		bottomLeftVPanel.addText(gins.getForms());
	    bottomLeftVPanel.layout();
	    cfecid.disable();	 
	    DOM.getElementById("progressBar1").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar2").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar3").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar4").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar5").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar6").getStyle().setProperty("background", "White");
	    /*
	     * 	Define the NEXT Button
	     */
	    next.removeAllListeners();
	    next.setEnabled(false);
	    next.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
	    	public void componentSelected(ButtonEvent ce) {	    		
	    	}
	    });
	    /*
	     * 	Define the LAST Button
	     */
	    last.removeAllListeners();
	    last.setEnabled(false);
	    last.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {				
			}	    	
	    });	 
	    statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Forms Section</span>");
	    
	    final Listener<BaseEvent> attachListener = new Listener<BaseEvent>() {
            @Override
            public void handleEvent(final BaseEvent be) {
                final Html html = (Html) be.getSource();
                html.el().addEventsSunk(Event.ONCLICK);
            }
        };
	    
	    VerticalPanel formPanel = new VerticalPanel();
	    formPanel.setBorders(false);
	    formPanel.setAutoHeight(true);
	    formPanel.setAutoWidth(true);
	    formPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
	    formPanel.setTableWidth("100%");
	    formPanel.add(new Html("<span class='boldblack12'><center>Your Pre-printed Renewal Form(s) - "+cfecid.getValue().toString().toUpperCase()+"</center></span>"));
	    for (int x=0;x<4;x++) {
	    	if (!(formlinks.get(x).equalsIgnoreCase("nodata"))) {
	    		final String yr = formlinks.get(x+1);
		    	Html html1 = new Html("<a href=" + formlinks.get(x)+" target='_blank'>Your "+formlinks.get(x+1)+" Pre-Printed Form(s)</a>");
				html1.setStyleName("forms");
				html1.setAutoWidth(true);
				html1.addListener(Events.Attach, attachListener);
			    html1.addListener(Events.OnClick, new Listener<BaseEvent>() {
		            public void handleEvent(final BaseEvent be) {
		            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded their " +yr+ " Pre-Printed Renewal Form");
		            }
		        });
				formPanel.add(html1);
		    }
	    	x = (x+1);
	    }
	    formPanel.add(new Html("<br>"));
	    formPanel.add(new Html("<span class='boldblack12'><center>Other CFEC forms</center></span>"));
	    Html permitApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Commerical_Fishing_Permit_Application.pdf target='_blank'>Commerical Permit Application</a>");
	    permitApp.addListener(Events.Attach, attachListener);
	    permitApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Commercial Fishing Permit Application Form");
            }
        });
	    Html povertyApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Federal_Income_Guidelines_and_Reduced_Fee_Permit_Application.pdf target='_blank'>Poverty Permit Fee Application</a>");
	    povertyApp.addListener(Events.Attach, attachListener);
	    povertyApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Reduced Fee Permit Application Form");
            }
        });
	    Html etransApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Request_for_Emergency_Transfer_of_Entry_Permit.pdf target='_blank'>Request for Emergency Transfer</a>");
	    etransApp.addListener(Events.Attach, attachListener);
	    etransApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Request for Emergency Transfer Form");
            }
        });
	    Html ptransApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Request_for_Permanent_Transfer_of_Entry_Permit.pdf target='_blank'>Request for Permanent Transfer</a>");
	    ptransApp.addListener(Events.Attach, attachListener);
	    ptransApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Request for Permanent Transfer Form");
            }
        });
	    Html itransApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Notice_of_Intent_to_Permanently_Transfer_Entry_Permit.pdf target='_blank'>Notice of Intent to Permanently Transer Entry Permit</a>");
	    itransApp.addListener(Events.Attach, attachListener);
	    itransApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Notice of Intent to Permanently Transfer Form");
            }
        });
	    Html vesselApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Commercial_Vessel_License_Application.pdf target='_blank'>Commercial Vessel Application</a>");
	    vesselApp.addListener(Events.Attach, attachListener);
	    vesselApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Commercial Vessel License Application Form");
            }
        });
	    Html ifishApp = new Html("<a href=http://www.cfec.state.ak.us/forms/Immediate_Fishing_Application.pdf target='_blank'>Immediate Fishing Application</a>");
	    ifishApp.addListener(Events.Attach, attachListener);
	    ifishApp.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Immediate Fishing Application Form");
            }
        });
	    Html ccAuth = new Html("<a href=http://www.cfec.state.ak.us/forms/Credit_Card_Authorization.pdf target='_blank'>Credit Card Authorization</a>");
	    ccAuth.addListener(Events.Attach, attachListener);
	    ccAuth.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(final BaseEvent be) {
            	Log.info(cfecid.getValue().toString().toUpperCase() + " has downloaded the Credit Card Authorization Form");
            }
        });
	   	    	    
	    formPanel.add(permitApp);
	    formPanel.add(povertyApp);
	    formPanel.add(etransApp);
	    formPanel.add(ptransApp);
	    formPanel.add(itransApp);
	    formPanel.add(vesselApp);
	    formPanel.add(ifishApp);
	    formPanel.add(ccAuth);
	    
	    Button renew = new Button("Renew your Permit/Vessles", new SelectionListener<ButtonEvent>() {
       	    public void componentSelected(ButtonEvent ce) {
       	    	gmsg.waitStart("Please Wait", "Getting CFEC Data....", "Progress", 250);
       	    	Log.info(cfecid.getValue().toString().toUpperCase() + " is attempting to login to LEON");
				/*
	    		* reset the timer
	    		*/
	    		timer.timerCancel();
		       	timer.progReset();
		       	timer.setTimer(timer.getTime(), cfecid.getValue().toString());
			   	service.getVitals(cfecid.getValue().toString().toUpperCase(), ryear, ginit.getRenewal().getValue(), false, new AsyncCallback() {
 				   public void onFailure(Throwable result) {
 					   gmsg.waitStop();
 					   statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
 					   gmsg.alert("Communication Error", gins.getTech(), 350);
 				   }
 				   public void onSuccess(Object result) {
 					   gmsg.waitStop();
 					   entity = (ArenewEntity) result;
 					   if (!(entity==null)) {
 						   if (entity.getIllegal().equalsIgnoreCase("false")) {
 							   Log.info(cfecid + " ("+entity.getXname()+") has successfully loged into LEON");
 							   Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the Renewal option from Forms");
 							   if (ginit.getAres().getValue().equals(true)) {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the Alaska Residence option");
 								  entity.setResidency("resident");
 							   } else {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the Non Alaska Residence option");
 								  entity.setResidency("nonresident");
 							   }
 							   if (ginit.getAcit().getValue().equals(true)) {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the US Citizen option");
 								  entity.setCitizen("true");
 							   } else {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the Non US Citizen option");
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " Alien Registration Number is " + ginit.getArn().getValue());
 								  entity.setCitizen("false");
 								  entity.setAlienreg(ginit.getArn().getValue());
 							   }
 							   if (ginit.getPovyes().getValue().equals(true)) {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has selected the Poverty Fees option");
 								  entity.setPoverty("true");
 							   } else {
 								  Log.info(cfecid.getValue().toString().toUpperCase() + " has NOT selected the Poverty Fees option");
 								  entity.setPoverty("false");
 							   }
 	 						   statusBar.setHTML("");
 	 						   StringBuffer topLeftText = new StringBuffer();
 	 						   topLeftText.append("<table border='0' bgcolor='#FFFFCC' width='100%' cellspacing='0'><tr><td align='center'>");
 	 						   topLeftText.append("<table border='0' width='100%' cellspacing='0'>");
 	 						   topLeftText.append("<tr><td align='center'><span class='boldblack12'>").append("Permanent mailing address:").append("</span></td></tr>");
 	 	    				   topLeftText.append("</table>");
 	 	    				   topLeftText.append("<table border='0' width='100%' cellspacing='0'><tr><td height='5'></td></tr></table>");
 	 	    				   topLeftText.append("<table border='0' width='100%' cellspacing='0'>");
 	 	    				   topLeftText.append("<tr><td align='center'><span class='regred12' id='perm_name'>").append(entity.getXname()).append("</span><br>");
 	 	    				   topLeftText.append("<span class='regred12' id='perm_address'>").append(entity.getPaddress()).append("</span><br>");
 	 	    				   topLeftText.append("<span class='regred12' id='perm_city'>").append(entity.getPcity()).append("</span>").append(",&nbsp;");
 	 	    				   topLeftText.append("<span class='regred12' id='perm_state'>").append(entity.getPstate()).append("</span>").append("&nbsp;");
 	 	    				   topLeftText.append("<span class='regred12' id='perm_zip'>").append(entity.getPzip()).append("</span>");
 	 	    				   topLeftText.append("</td></tr></table>");
 	 	    				   topLeftText.append("</td></tr></table>");
 	 	    				   if (isAgent) {
 	 	    					   entity.setAgentsub("yes");
 	 	    					   entity.setAgent(agentName);
 	 	    				   } else {
 	 	    					   entity.setAgentsub("no");
 	 	    					   entity.setAgent(agentName);
 	 	    				   }
 	 	    				   plist.clear();
  	    				       vlist.clear();
 	 	    				   for (Iterator gp = entity.getArenewPermitses().iterator(); gp.hasNext();) {
	  	    					   ArenewPermits per = (ArenewPermits) gp.next();
	  	    					   plist.add(per);
	  	    				   }
 	 	    				   for (Iterator gv = entity.getArenewVesselses().iterator(); gv.hasNext();) {
	  	    					   ArenewVessels ves = (ArenewVessels) gv.next();
	  	    					   vlist.add(ves);
	  	    				   }
 	 						   gadd.address(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, topLeftText.toString(), 
 	 								   NavprogressBarPanel, entity, service, changeList, timer, payment, feeTotals, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
 						   } else {
 							  statusBar.setHTML("<span class='regred12'>*** The CFECID number you entered cannot participate in this application ***</span>");
   						      gmsg.alert("Requirements not met", "<span class='regred12'>The CFECID number you entered cannot participate in this application</span>", 250);
   						      Log.info("Invalid CFEC ID login attempt using " + cfecid);
 				           } 						   
 					   } else {
 						   statusBar.setHTML("<span class='regred12'>*** The CFECID number you entered is invalid (6 numeric characters only), try again ***</span>");
 						   gmsg.alert("Requirements not met", "<span class='regred12'>The CFECID number you entered is invalid (6 numeric characters only), try again</span>", 250);
 						   Log.info("Invalid CFEC ID login attempt using " + cfecid);
 					   }
 				   }
 			   });
       	    }
       	});
	    renew.setStyleAttribute("margin-top", "20px");
	    
	    
	    formPanel.add(renew);
	    
	    bottomRight.setHeadingHtml("<span class='boldblack12'>Permit/Vessel Owner Forms Section</span>");
	    bottomRight.add(formPanel);
	    bottomRight.layout();
	}
}
