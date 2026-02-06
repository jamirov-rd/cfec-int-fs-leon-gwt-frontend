package com.cfecweb.leon.client;

import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

/*
 * The Address Screen is the first screen (other than perhaps the download screen) that a person would typically goto after
 * their initial options have been selected. As with all UI screens in this application, the base of the address screen is
 * out original borderlayout defined in the opening class. The four regions of that layout are simply passed to each UI
 * screen and updated with relevant content, which is of course the basis of element level modification vs. page level modification
 * that has become so popular with AJAX applications.
 * There are a number of 'rules' that have been applied to this screen, all based on our current workflow and input from the 
 * licensing folks. These are meant to validate-on-demand. Each UI screen has Last and Next buttons which are re-defined based on
 * which screen were in AND based on user status. Also, each screen gets modified content for instructions and statuses.
 * Essentially, a person CANNOT go the next screen without the main formpanel validating.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ScreenAddress extends LayoutContainer {
	InstructionsText gins = new InstructionsText();
	UserMessages gmsg = new UserMessages();
	UserWindows gwin = new UserWindows();
	FormPanel addressPanel = null;
	TextField<String> paddress = null; TextField<String> pcity = null; TextField<String> pstate = null; 
	TextField<String> pzip = null; TextField<String> paddress2 = null; TextField<String> years = null; 
	TextField<String> months = null; TextField<String> phoneArea = null; TextField<String> phonePre = null;
	TextField<String> phonePost = null; TextField<String> email = null; TextField<String> arn = null; SimpleComboBox citizen = null; 
	TextField<String> raddress = null; TextField<String> rcity = null; TextField<String> rstate = null; 
	TextField<String> rzip = null; TextField<String> raddress2 = null; TextField<String> pcountry = null; TextField<String> rcountry = null;
	TextField<String> taddress = null; TextField<String> taddress2 = null; TextField<String> tcity = null; TextField<String> tstate = null; TextField<String> tzip = null;
	boolean rfirst = false;	boolean firstTime = true; 
	ChangeRecorder cr = new ChangeRecorder(); 
	StringBuffer pmtvesCount = null;
	VerticalPanel disclaimers = null;
	HTML reschoice = new HTML();
	RadioGroup ppub = null;
	RadioGroup epub = null;
	Radio pyes = null;
	Radio pno = null;
	Radio eyes = null;
	Radio eno = null;
	VerticalPanel ppubv = null; 
	VerticalPanel epubv = null;
	RadioGroup pastyear = null; Radio pyyes = null; Radio pyno = null; TextField<String> pytext = null; 
	RadioGroup alaskaid = null; Radio akidyes = null; Radio akidno = null; TextField<String> akidtext = null; 
	//RadioGroup alaskavote = null; Radio akvoteyes = null; Radio akvoteno = null; TextField<String> othertext = null; 
	
	public void address(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight, final FieldSet topRight, final Button startOver,
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText,
			final String topLeftText, final HorizontalPanel NavprogressBarPanel, final ArenewEntity entity, final getDataAsync service,
			final List<ArenewChanges> changeList, final SessionTimer timer, final ArenewPayment payment, final FeeTotals feeTotals, final List<ArenewPermits> plist,
			final List<ArenewVessels> vlist, final CheckBox first, final CheckBox second, final CheckBox nop, final String ryear, final String reCaptchaSiteKey, final String reCaptchaAction) {
		Log.info(entity.getId().getCfecid() + " has navigated to the Address Panel");
		HTML physreq = new HTML();
		bottomRight.removeAll();
		topLeft.removeAll();
		topLeft.addText(topLeftText);
		topLeft.layout();
		bottomLeftVPanel.removeAll();
	    bottomLeftVPanel.add(startOver);
	    bottomLeftVPanel.addText(gins.getAddress());
	    bottomLeftVPanel.layout();
	    cfecid.disable();	 
	    DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar2").getStyle().setProperty("background", "#DCDCDC");
	    DOM.getElementById("progressBar3").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar4").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar5").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar6").getStyle().setProperty("background", "White");	    
	    /*
	     * disable the LAST Button in this screen, cannot go back to initial options without re-setting
	     */
	    last.setEnabled(false);
	    /*
	     *	Define the NEXT Button 
	     */
	    next.removeAllListeners();
	    next.setEnabled(true);
	    next.setTabIndex(23);
	    next.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
	    	public void componentSelected(ButtonEvent ce) {
	    		if (entity.getResidency().toString().equals("nonresident")) {
	    			raddress.setAllowBlank(true);
	    			raddress2.setAllowBlank(true);
	    			rcity.setAllowBlank(true);
	    			rstate.setAllowBlank(true);
	    			rzip.setAllowBlank(true);
	    			months.setAllowBlank(true);
	    			years.setAllowBlank(true);
	    			raddress.setValue(null);
	    			raddress2.setValue(null);
	    			rcity.setValue(null);
	    			rstate.setValue(null);
	    			rzip.setValue(null);
	    			months.setValue("0");
	    			years.setValue("0");
	    			pyyes.setValue(null);
	    			pyno.setValue(null);
	    			akidyes.setValue(null);
	    			akidno.setValue(null);
	    			//akvoteyes.setValue(null);
	    			//akvoteno.setValue(null);
	    			pytext.setValue(null);
	    			akidtext.setValue(null);
	    			//othertext.setValue(null);
	    		}
	    		if (pyes.getValue().equals(false) && pno.getValue().equals(false)) {
	    			statusBar.setHTML("<span class='regred12'>*** You MUST make a phone confidentialty selection (Public or Private) ***</span>");
	    			gmsg.alert("Requirements not met", "<span class='regred12'>You MUST make a phone confidentialty selection (Public or Private)</span>", 300);
	    		} else if (addressPanel.isValid()) {
	    			statusBar.setHTML("");
	    			gmsg.waitStart("Please Wait", "Validating Data....", "Progress", 250);	
	    			/*
	    			 * Physical Address
	    			 */
	    			entity.setRaddress(raddress.getValue());
	    			entity.setRaddress2(raddress2.getValue());
	    			entity.setRcity(rcity.getValue());
	    			entity.setRstate(rstate.getValue());
	    			entity.setRzip(rzip.getValue());	
	    			/*
	    			 * Permanent Address
	    			 */
	    			entity.setPaddress(paddress.getValue());
	    			entity.setPaddress2(paddress2.getValue());
    				entity.setPcity(pcity.getValue());
    				entity.setPstate(pstate.getValue());
    				entity.setPzip(pzip.getValue());
    				/*
    				 * Temporary Address
    				 */
    				if (nop.getValue().equals(true)) {
    					payment.setBaddress(taddress.getValue());
    					payment.setBcity(tcity.getValue());
    					payment.setBstate(tstate.getValue());
    					payment.setBzip(tzip.getValue());
    				}
    				/*
    				 * First Time boolean
    				 */
	    			if (entity.getFirsttime().equalsIgnoreCase("true")) {
	    				firstTime = true;
	    			} else {
	    				firstTime = false;
	    			}
	    			entity.setYears(years.getValue());
	    			entity.setMonths(months.getValue());
	    			/*
	    			 * Phone and email
	    			 */
	    			entity.setArea(phoneArea.getValue());
	    			if ( (!(phonePre.getValue() == null) && (!(phonePost.getValue() == null)))) {
	    				entity.setPhone(phonePre.getValue() + phonePost.getValue());
	    			} else {
	    				entity.setPhone(null);
	    			}
	    			if (pyes.getValue().equals(true)) {
	    				Log.info(entity.getId().getCfecid() + " has selected to make their phone number public");
	                	entity.setPhonepub("Yes");
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has selected to make their phone number private");
	                	entity.setPhonepub("No");
	    			}
	    			entity.setEmail(email.getValue());
	    			if (eyes.getValue().equals(true)) {
	    				Log.info(entity.getId().getCfecid() + " has selected to make their email address public");
	                	entity.setEmailpub("Yes");
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has selected to make their email address private");
	                	entity.setEmailpub("No");
	    			}	        
	    			/*
	    			 * additional residency info
	    			 */	    			
	    			// TODO
	    			// been here the past 365 days
	    			if (pyyes.getValue().equals(null) && pyno.getValue().equals(null)) {
	    				entity.setPyearabsent(null);
	    			} else if (pyyes.getValue().equals(true)) {
	    				Log.info(entity.getId().getCfecid() + " has selected YES to residing elsewhere in the previous 365 days");
	                	entity.setPyearabsent("Yes");
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has selected NO to residing elsewhere in the previous 365 days");
	                	entity.setPyearabsent("No");
	    			} 
	    			// if no, text where you have been
	    			if (!(pytext.getValue() == null)) {
	    				Log.info(entity.getId().getCfecid() + " has entered text regarding the past 365 days of resindency");
	    				entity.setPyearabsenttext(pytext.getValue());
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has NOT entered text regarding the past 365 days of resindency");
	    				entity.setPyearabsenttext(null);
	    			}
	    			// current Alaska driver's license or id
	    			if (akidyes.getValue().equals(null) && akidno.getValue().equals(null)) {
	    				entity.setAlaskaid(null);
	    			} else if (akidyes.getValue().equals(true)) {
	    				Log.info(entity.getId().getCfecid() + " has selected YES to having a current Alaska driver's license or ID");
	                	entity.setAlaskaid("Yes");
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has selected NO to having a current Alaska driver's license or ID");
	                	entity.setAlaskaid("No");
	    			} 
	    			// if yes, what is the number
	    			if (!(akidtext.getValue() == null)) {
	    				Log.info(entity.getId().getCfecid() + " has entered text regarding their Alaska ID");
	    				entity.setAlaskaidtext(akidtext.getValue());
	    			} else {
	    				Log.info(entity.getId().getCfecid() + " has NOT entered text regarding their Alaska ID");
	    				entity.setAlaskaidtext(null);
	    			}
	    			// registered to vote
	    			//if (akvoteyes.getValue().equals(null) && akvoteno.getValue().equals(null)) {
	    				entity.setAlaskavote(null);
	    			//} else if (akvoteyes.getValue().equals(true)) {
	    			//	Log.info(entity.getId().getCfecid() + " has selected YES to being a current registered voter");
	                //	entity.setAlaskavote("Yes");
	    			//} else {
	    			//	Log.info(entity.getId().getCfecid() + " has selected NO to being a current registered voter");
	                //	entity.setAlaskavote("No");
	    			//} 
	    			// other text if answered no on 2 or 3
	    			//if (!(othertext.getValue() == null)) {
	    			//	Log.info(entity.getId().getCfecid() + " has entered brief explanation for NO answers on 2");
	    			//	entity.setOthertext(othertext.getValue());
	    			//} else {
	    			//	Log.info(entity.getId().getCfecid() + " has not entered brief explanation for NO answers on 2");
	    				entity.setOthertext(null);
	    			//}
	    			//
	    			gmsg.waitStop();
	    			gmsg.waitStart("Please Wait", "Getting Permit and Vessel Data....", "Progress", 250);
	    			/*
	    			 * reset the timer
	    			 */
	    			timer.timerCancel();
		       	    timer.progReset();
		       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
		       	    gmsg.waitStop();
					statusBar.setHTML("");
					final ScreenVessel vessel = new ScreenVessel();
					pmtvesCount = new StringBuffer();
					pmtvesCount = new StringBuffer("<table bgcolor='#FFFFCC' border='0' width='100%' cellspacing='0'>");
				    pmtvesCount.append("<tr><td><span class='boldblack12'>").append("<center>"+entity.getXname()+"</center>").append("</span></td></tr>");
				    pmtvesCount.append("<tr><td><span class='boldblack12'>").append("<center>Your renewable applications:</center>").append("</span></td></tr></table>");
				    pmtvesCount.append("<table bgcolor='#FFFFCC' border='0' width='100%' cellspacing='0'><tr><td height='5'></td></tr></table>");
				    pmtvesCount.append("<table bgcolor='#FFFFCC' border='0' width='100%' cellspacing='0'>");
				    pmtvesCount.append("<tr><td align='center'><span class='regblack12'>Permits - <span id='permitsRenewalble'>").append(+entity.getArenewPermitses().size()+"</span></span></td></tr></table>");
				    pmtvesCount.append("<table bgcolor='#FFFFCC' border='0' width='100%' cellspacing='0'>");
				    pmtvesCount.append("<tr><td align='center'><span class='regblack12'>Vessels - <span id='vesselsRenewalble'>").append(+entity.getArenewVesselses().size()+"</span></span></td></tr></table></table>");
				    topLeft.removeAll();
					topLeft.addText(pmtvesCount.toString());
					topLeft.layout();
					service.sortPlist(plist, entity.getPoverty(), new AsyncCallback() {
						public void onFailure(Throwable caught) {
							gmsg.alert("Communication Error", gins.getTech(), 350);									
						}
						public void onSuccess(Object result) {
							plist.clear();
							List<ArenewPermits> p = (List<ArenewPermits>) result;
							plist.addAll(p);
							vessel.getVessels(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount.toString(), 
								NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
						}						
					});
	    		} else {
	    			statusBar.setHTML("<span class='regred12'>*** There are unmet requirements on this page. Look for the red boxes ***</span>");
	    			gmsg.alert("Requirements not met", "<span class='regred12'>There are unmet requirements on this page. Look for the red boxes</span>", 300);
	    		} 
    	    }        
        }); 
	    
	    phrdText.setHTML("<span class='regblack12'><center>Please enter your address information, then press <span class='regred12'>Next >></span> to continue</center></span>");
	    statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Address Section</span>");
	    
	    topRight.removeAll();
	    topRight.add(NavprogressBarPanel);
	    topRight.layout();
	    
	    FormData formData = new FormData("100%"); 
	    addressPanel = new FormPanel(); 
	    addressPanel.setAutoHeight(true);
	    addressPanel.setFrame(false);
	    addressPanel.setBorders(false); 
	    addressPanel.setBodyBorder(false); 
	    addressPanel.setHeaderVisible(false);
	    addressPanel.setLayout(new FlowLayout());
	    
	    final FieldSet perm = new FieldSet();
        perm.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Permanent Mailing Address</span><span class='reggreen12'>&nbsp;&nbsp;-&nbsp;&nbsp;<u>CLICK BOX TO LEFT ONLY IF YOU WANT TO CHANGE YOUR CURRENT MAILING ADDRESS</u></span>");
        perm.setCheckboxToggle(true);
        perm.setAutoHeight(true);
        perm.setStyleAttribute("margin-top", "-10px");
        perm.setStyleAttribute("margin-left", "-10px");
        perm.setStyleAttribute("margin-right", "-10px");
        perm.addStyleName("addressFieldSets");
        perm.addListener(Events.Expand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
        		perm.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Permanent Mailing Address</span><span class='regred12'>&nbsp;&nbsp;-&nbsp;&nbsp;Update your Permanent Mailing Address (where you receive mail) or un-select to cancel</span>");
        		paddress.setEnabled(true);				
				pcity.setEnabled(true);				
				pstate.setEnabled(true);
				pzip.setEnabled(true);
				paddress2.setEnabled(true);
            }
        });
        perm.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
            		perm.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Permanent Mailing Address</span><span class='reggreen12'>&nbsp;&nbsp;-&nbsp;&nbsp;<u>CLICK BOX TO LEFT ONLY IF YOU WANT TO CHANGE YOUR CURRENT MAILING ADDRESS</u></span>");
    				paddress.setEnabled(true);				
    				pcity.setEnabled(true);				
    				pstate.setEnabled(true);
    				pzip.setEnabled(true);
    				paddress2.setEnabled(true);
            }
        });   
        
        ContentPanel perm1 = new ContentPanel();
        perm1.setHeaderVisible(false);
        perm1.setBodyBorder(false);
        perm1.setBorders(false);
        perm1.setFrame(true);
        perm1.setAutoHeight(true);
        perm1.setAutoWidth(true);
        perm1.setWidth("97%");
        perm1.addStyleName("addressFieldSets");
        
        LayoutContainer main = new LayoutContainer();  
        main.setLayout(new ColumnLayout()); 
        
        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px");
        FormLayout layout = new FormLayout();  
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(59); 
        left.setLayout(layout);  
            
        LayoutContainer middle1 = new LayoutContainer();  
        middle1.setStyleAttribute("paddingRight", "10px");
        FormLayout layout2 = new FormLayout();  
        layout2.setLabelAlign(LabelAlign.LEFT);  
        layout2.setLabelWidth(36);  
        middle1.setLayout(layout2);
            
        LayoutContainer middle2 = new LayoutContainer();  
        middle2.setStyleAttribute("paddingRight", "10px");
        FormLayout layout3 = new FormLayout();  
        layout3.setLabelAlign(LabelAlign.LEFT);  
        layout3.setLabelWidth(125);  
        middle2.setLayout(layout3);
        
        LayoutContainer right = new LayoutContainer();          
        FormLayout layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(32);  
        right.setLayout(layout4);
        
        LayoutContainer fadd = new LayoutContainer();
        FormLayout layouttest = new FormLayout();  
        layouttest.setLabelAlign(LabelAlign.LEFT);
        layouttest.setLabelWidth(205); 
        fadd.setLayout(layouttest);  

        paddress = new TextField<String>();  
		paddress.setMessageTarget("tooltip");
		if (!(entity.getPaddress() == null)) {
			paddress.setValue(entity.getPaddress());
			paddress.setAllowBlank(false);
			paddress.setFieldLabel("<span class='boldred12'>*</span> Address");
		} else {
			paddress.setAllowBlank(true);
			paddress.setFieldLabel("Address");
		}		
		paddress.getMessages().setBlankText("This field cannot be blank");
        paddress.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (paddress.isValid()) {
					if (!(paddress2 == null)) {
						paddress2.setAllowBlank(true);
						paddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						paddress2.setValue(null);
						paddress.setAllowBlank(false);
						pcity.setAllowBlank(false);
						pstate.setAllowBlank(false);
						pzip.setAllowBlank(false);
						paddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						pcity.setFieldLabel("<span class='boldred12'>*</span> City");
						pstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						pzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PermMailingAddress", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });        
        paddress.setEnabled(true);
        paddress.setTabIndex(1);
        left.add(paddress, formData);  
           
        pcity = new TextField<String>();  
        if (!(entity.getPcity() == null)) {
        	pcity.setValue(entity.getPcity());
        	pcity.setAllowBlank(false);
        	pcity.setFieldLabel("<span class='boldred12'>*</span> City");
		} else {
			pcity.setAllowBlank(true);
			pcity.setFieldLabel("City");
		}	   
        pcity.setEnabled(true); 
		pcity.setMessageTarget("tooltip");
		pcity.getMessages().setBlankText("This field cannot be blank");
        pcity.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (pcity.isValid()) {
					if (!(paddress2 == null)) {
						paddress2.setAllowBlank(true);
						paddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						paddress2.setValue(null);
						paddress.setAllowBlank(false);
						pcity.setAllowBlank(false);
						pstate.setAllowBlank(false);
						pzip.setAllowBlank(false);
						paddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						pcity.setFieldLabel("<span class='boldred12'>*</span> City");
						pstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						pzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PermMailingCity", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        pcity.setTabIndex(2);
        middle1.add(pcity, formData);  
       
        pstate = new TextField<String>();  
        if (!(entity.getPstate() == null)) {
        	pstate.setValue(entity.getPstate());
        	pstate.setAllowBlank(false);
        	pstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
		} else {
			pstate.setAllowBlank(true);
			pstate.setFieldLabel("State (2 characters)");
		}	 
        pstate.setEnabled(true);
        pstate.setFieldLabel("State (2 characters)");
		pstate.setMessageTarget("tooltip");
		pstate.getMessages().setBlankText("This field cannot be blank");
		pstate.getMessages().setMaxLengthText("You have exceeded the maximum character length (2) for this field");
        pstate.setMinLength(2);
        pstate.setMaxLength(2);
        pstate.setMessageTarget("tooltip");
        pstate.setRegex("^((AL)|(AK)|(AS)|(AZ)|(AR)|(CA)|(CO)|(CT)|(DE)|(DC)|(FM)|(FL)|(GA)|(GU)|(HI)|(ID)|(IL)|(IN)|(IA)|(KS)|(KY)|(LA)|(ME)|(MH)|(MD)|(MA)|(MI)|(MN)|(MS)|(MO)|(MT)|(NE)|(NV)|(NH)|(NJ)|(NM)|(NY)|(NC)|(ND)|(MP)|(OH)|(OK)|(OR)|(PW)|(PA)|(PR)|(RI)|(SC)|(SD)|(TN)|(TX)|(UT)|(VT)|(VI)|(VA)|(WA)|(WV)|(WI)|(WY)|" +
        		       "(al)|(ak)|(as)|(az)|(ar)|(ca)|(co)|(ct)|(de)|(dc)|(fm)|(fl)|(ga)|(gu)|(hi)|(id)|(il)|(in)|(ia)|(ks)|(ky)|(la)|(me)|(mh)|(md)|(ma)|(mi)|(mn)|(ms)|(mo)|(mt)|(ne)|(nv)|(nh)|(nj)|(nm)|(ny)|(nc)|(nd)|(mp)|(oh)|(ok)|(or)|(pw)|(pa)|(pr)|(ri)|(sc)|(sd)|(tn)|(tx)|(ut)|(vt)|(vi)|(va)|(wa)|(wv)|(wi)|(wy)|" +
        		       "(Al)|(Ak)|(As)|(Az)|(Ar)|(Ca)|(Co)|(Ct)|(De)|(Dc)|(Fm)|(Fl)|(Ga)|(Gu)|(Hi)|(Id)|(Il)|(In)|(Ia)|(Ks)|(Ky)|(La)|(Me)|(Mh)|(Md)|(Ma)|(Mi)|(Mn)|(Ms)|(Mo)|(Mt)|(Ne)|(Nv)|(Nh)|(Nj)|(Nm)|(Ny)|(Nc)|(Nd)|(Mp)|(Oh)|(Ok)|(Or)|(Pw)|(Pa)|(Pr)|(Ri)|(Sc)|(Sd)|(Tn)|(Tx)|(Ut)|(Vt)|(Vi)|(Va)|(Wa)|(Wv)|(Wi)|(Wy))$");
        pstate.getMessages().setRegexText("You have enter an invalid State abbreviation");
        pstate.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(pstate.getValue() == null)) {
					if (pstate.getValue().length() == 2) {
						//zip.focus();
					}
				}
			}
        });
        pstate.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (pstate.isValid()) {
					if (!(paddress2 == null)) {
						paddress2.setAllowBlank(true);
						paddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						paddress2.setValue(null);
						paddress.setAllowBlank(false);
						pcity.setAllowBlank(false);
						pstate.setAllowBlank(false);
						pzip.setAllowBlank(false);
						paddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						pcity.setFieldLabel("<span class='boldred12'>*</span> City");
						pstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						pzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PermMailingState", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        }); 
        pstate.setTabIndex(3);
        middle2.add(pstate, formData);
         
        pzip = new TextField<String>();  
        if (!(entity.getPzip() == null)) {
        	pzip.setValue(entity.getPzip());
        	pzip.setAllowBlank(false);
        	pzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
		} else {
			pzip.setAllowBlank(true);
			pzip.setFieldLabel("Zip");
		}	 
        pzip.setEnabled(true); 
		pzip.setMessageTarget("tooltip");
		pzip.getMessages().setBlankText("This field cannot be blank");
		pzip.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
        pzip.setMinLength(5);
        pzip.setMaxLength(5);
        pzip.setMessageTarget("tooltip");
        pzip.setRegex("(\\d{5})");
        pzip.getMessages().setRegexText("Your Zip Code is invalid, must be 5 numeric characters");
        pzip.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(pzip.getValue() == null)) {
					if (pzip.getValue().length() == 5) {
						//years.focus();
					}
			    }
			}
        });
        pzip.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (pzip.isValid()) {
					if (!(paddress2 == null)) {
						paddress2.setAllowBlank(true);
						paddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						paddress2.setValue(null);
						paddress.setAllowBlank(false);
						pcity.setAllowBlank(false);
						pstate.setAllowBlank(false);
						pzip.setAllowBlank(false);
						paddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						pcity.setFieldLabel("<span class='boldred12'>*</span> City");
						pstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						pzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PermMailingZip", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        pzip.setTabIndex(4);
        right.add(pzip, formData);
        
        paddress2 = new TextField<String>();
        if (!(entity.getPaddress2() == null)) {
        	paddress2.setValue(entity.getPaddress2());
        	paddress2.setAllowBlank(false);
        	paddress2.setFieldLabel("<span class='boldred12'>*</span> <b>OR</b> Foreign Address (if applicable)");
		} else {
			paddress2.setAllowBlank(true);
			paddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)");
		}	 
        paddress2.setEnabled(true);
        paddress2.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (paddress2.isValid()) {
					if (!(paddress == null)) {
						paddress.setAllowBlank(true);
						paddress.setFieldLabel("Address"); 
						paddress.setValue(null);
						paddress2.setAllowBlank(false);
						pcity.setAllowBlank(true);
						pstate.setAllowBlank(true);
						pzip.setAllowBlank(true);
						pcity.setValue(null);
						pstate.setValue(null);
						pzip.setValue(null);
						paddress2.setFieldLabel("<span class='boldred12'>*</span> <b>OR</b> Foreign Address (if applicable)");  
						pcity.setFieldLabel("City");
						pstate.setFieldLabel("State (2 characters)");
						pzip.setFieldLabel("Zip");
					}
					cr.getAddressChanges("PermMailingAddress2", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        paddress2.setTabIndex(5);
        fadd.add(paddress2, formData);
                               
        main.add(left, new ColumnData(0.42));
        main.add(middle1, new ColumnData(0.20));
        main.add(middle2, new ColumnData(0.17));
        main.add(right, new ColumnData(0.17));
        main.add(fadd, new ColumnData(0.96));
        //main.add(newpadd, new ColumnData(1.00));
        
        perm1.add(main, new FormData("100%"));    
        
        perm.add(perm1);
        
        final FieldSet phys = new FieldSet();
        phys.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Physical Address</span><span class='reggreen12'>&nbsp;&nbsp;-&nbsp;&nbsp;Select box to change your Physical Address (where you live, NO PO boxes allowed)</span>");
        phys.setCheckboxToggle(true);
        phys.setAutoHeight(true);
        phys.setStyleAttribute("margin-left", "-10px");
        phys.setStyleAttribute("margin-right", "-10px");
        phys.addStyleName("addressFieldSets");
        if (entity.getResidency().toString().equals("resident")) {
        	phys.addListener(Events.Expand, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
            		phys.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Physical Address</span><span class='regred12'>&nbsp;&nbsp;-&nbsp;&nbsp;Please enter a Physical Address (where you live, NO PO boxes allowed)</span>");
    	    		if (raddress == null && rcity == null && rstate == null && rzip == null && raddress2 == null) {
    	    			raddress.setAllowBlank(false);
    	    			rcity.setAllowBlank(false);
    	    			rstate.setAllowBlank(false);
    	    			rzip.setAllowBlank(false);
    	    			raddress2.setAllowBlank(true);
    	    		} else if (!(raddress == null)) {
    	    			raddress.setAllowBlank(false);
    	    			rcity.setAllowBlank(false);
    	    			rstate.setAllowBlank(false);
    	    			rzip.setAllowBlank(false);
    	    			raddress2.setAllowBlank(true);
    	    		} else if (!(raddress2 == null)) {
    	    			raddress.setAllowBlank(true);
    	    			rcity.setAllowBlank(true);
    	    			rstate.setAllowBlank(true);
    	    			rzip.setAllowBlank(true);
    	    			raddress2.setAllowBlank(false);
    	    		} else {
    	    			raddress.setAllowBlank(false);
    	    			rcity.setAllowBlank(false);
    	    			rstate.setAllowBlank(false);
    	    			rzip.setAllowBlank(false);
    	    			raddress2.setAllowBlank(false);
    	    		}
                }
            });
            phys.addListener(Events.Collapse, new Listener<ComponentEvent>() {
                public void handleEvent(ComponentEvent ce) {
                	phys.expand();
                	phys.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Physical Address</span><span class='reggreen12'>&nbsp;&nbsp;-&nbsp;&nbsp;Select box to change your Physical Address (where you live, NO PO boxes allowed)</span>");
    				raddress.setEnabled(true);    				
    				raddress2.setEnabled(true);    				
    				rcity.setEnabled(true);    				
    				rstate.setEnabled(true);    				
    				rzip.setEnabled(true);   
                }
            });
        } else {        	
        	phys.setEnabled(false);
        }        
        
        ContentPanel temp1 = new ContentPanel();
        temp1.setHeaderVisible(false);
        temp1.setBodyBorder(false);
        temp1.setBorders(false);
        temp1.setFrame(true);
        temp1.setAutoHeight(true);
        temp1.setAutoWidth(true);
        temp1.setWidth("97%");
        temp1.addStyleName("addressFieldSets");
        
        LayoutContainer main2 = new LayoutContainer();  
        main2.setLayout(new ColumnLayout()); 
        
        LayoutContainer main12 = new LayoutContainer();  
        main12.setLayout(new ColumnLayout()); 
        
        left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px");
        layout = new FormLayout();  
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(59); 
        left.setLayout(layout);  
            
        middle1 = new LayoutContainer();  
        middle1.setStyleAttribute("paddingRight", "10px");
        layout2 = new FormLayout();  
        layout2.setLabelAlign(LabelAlign.LEFT);  
        layout2.setLabelWidth(36);  
        middle1.setLayout(layout2);
            
        middle2 = new LayoutContainer();  
        middle2.setStyleAttribute("paddingRight", "10px");
        layout3 = new FormLayout();  
        layout3.setLabelAlign(LabelAlign.LEFT);  
        layout3.setLabelWidth(125);  
        middle2.setLayout(layout3);
        
        right = new LayoutContainer();          
        layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(32);  
        right.setLayout(layout4);
        
        LayoutContainer tfadd = new LayoutContainer();
        tfadd.setStyleAttribute("paddingRight", "10px");
        layouttest = new FormLayout();  
        layouttest.setLabelAlign(LabelAlign.LEFT);
        layouttest.setLabelWidth(205); 
        tfadd.setLayout(layouttest);        
        
        LayoutContainer ym = new LayoutContainer();  
        ym.setStyleAttribute("paddingRight", "10px");
        layout2 = new FormLayout();  
        layout2.setLabelAlign(LabelAlign.LEFT);  
        layout2.setLabelWidth(67);  
        ym.setLayout(layout2);
        
        LayoutContainer mr = new LayoutContainer();  
        layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(71);  
        mr.setLayout(layout4);  
                
        raddress = new TextField<String>();  
        if (entity.getForeign()) {
        	raddress.setFieldLabel("Address");
        } else {
        	raddress.setFieldLabel("<span class='boldred12'>*</span> Address");
        }
        raddress.setAllowBlank(true);
        raddress.setEnabled(true);
		raddress.setMessageTarget("tooltip");
		raddress.setValue(entity.getRaddress());
        raddress.setTabIndex(6);
        /*
         * The following regular expression has been converted and modified to working in Java.
         * It 'should' pick up the following matches:
         * 	1. post/POST office/OFFICE and potential box numbers case insensitive
         *  2. post/POST code/CODE and potential box numbers case insensitive
         *  3. postal/POSTAL office/OFFICE and potential box numbers case insensitive
         *  4. postal/POSTAL code/CODE and potential box numbers case insensitive
         *  5. BOX/box CODE/code and potential box numbers case insensitive
         *  6. Any BOX/box with a corresponding number or pound sign
         *  7. any PO/po/P.O./p.o. combination with a space afterwards
         */
        raddress.addListener(Events.OnBlur, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(be.getField().getValue() == null)) {
					if (be.getField().getValue().toString().toLowerCase().matches(""
							+ "[P|p]+(OST|ost)\\.?[\\s]*[O|o]+(FFICE|ffice)\\.?[\\s]*[b|B]?[\\s]*[o|O]?[\\s]*[x|X]?[\\s]*[0-9#]*|"
							+ "\\b[P|p]+(OST|ost)\\.?[\\s]*[C|c]+(ODE|ode)\\.?[\\s]*[b|B]?[\\s]*[o|O]?[\\s]*[x|X]?[\\s]*[0-9#]*|"
							+ "\\b[P|p]+(OSTAL|ostal)\\.?[\\s]*[O|o]+(FFICE|ffice)\\.?[\\s]*[b|B]?[\\s]*[o|O]?[\\s]*[x|X]?[\\s]*[0-9#]*|"
							+ "\\b[P|p]+(OSTAL|ostal)\\.?[\\s]*[C|c]+(ODE|ode)\\.?[\\s]*[b|B]?[\\s]*[o|O]?[\\s]*[x|X]?[\\s]*[0-9#]*|"
							+ "\\b[B|b]+(OX|ox)\\.?[\\s]*[C|c]+(ODE|ode)\\.?[\\s]*[0-9#]*|"
							+ "\\b[B|b]+(OX|ox)\\.?[\\s]*[0-9#]*|"
							+ "\\b[p|P]\\.?[\\s]*[o|O]\\.?[\\s]*[b|B]?[\\s]*[o|O]?[\\s]*[x|X]?[\\s]*[0-9#]*"
					)) {
						raddress.markInvalid("test");
						raddress.setMessageTarget("tooltip");
						raddress.getMessages().setRegexText("Invalid address, contains a post office box");
						gmsg.alert("Requirements not met", "<span class='regred12'>You can't enter a PO Box as a Physical Address</span>", 250);
						raddress.setValue(null);
					}
				}
			}        	
        });
        raddress.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (raddress.isValid()) {
					if (!(raddress2 == null)) {
						raddress2.setAllowBlank(true);
						raddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						raddress2.setValue(null);
						raddress.setAllowBlank(false);
						rcity.setAllowBlank(false);
						rstate.setAllowBlank(false);
						rzip.setAllowBlank(false);
						raddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						rcity.setFieldLabel("<span class='boldred12'>*</span> City");
						rstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						rzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PhysMailingAddress", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        left.add(raddress, formData);  
           
        rcity = new TextField<String>();  
        if (entity.getForeign()) {
        	rcity.setFieldLabel("City");
        } else {
        	rcity.setFieldLabel("<span class='boldred12'>*</span> City");
        }        
        rcity.setAllowBlank(true);
        rcity.setEnabled(true);
		rcity.setMessageTarget("tooltip");
		rcity.setValue(entity.getRcity());
        rcity.setTabIndex(7);
        rcity.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (rcity.isValid()) {
					if (!(raddress2 == null)) {
						raddress2.setAllowBlank(true);
						raddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						raddress2.setValue(null);
						raddress.setAllowBlank(false);
						rcity.setAllowBlank(false);
						rstate.setAllowBlank(false);
						rzip.setAllowBlank(false);
						raddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						rcity.setFieldLabel("<span class='boldred12'>*</span> City");
						rstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						rzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PhysMailingCity", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        middle1.add(rcity, formData);  
       
        rstate = new TextField<String>();  
        if (entity.getForeign()) {
        	rstate.setFieldLabel("State (2 characters)");
        } else {
        	rstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
        }        
        rstate.setMinLength(2);
        rstate.setMaxLength(2);
        rstate.setMessageTarget("tooltip");
        rstate.getMessages().setMaxLengthText("You have exceeded the maximum character length (2) for this field");
        rstate.setRegex("^((AL)|(AK)|(AS)|(AZ)|(AR)|(CA)|(CO)|(CT)|(DE)|(DC)|(FM)|(FL)|(GA)|(GU)|(HI)|(ID)|(IL)|(IN)|(IA)|(KS)|(KY)|(LA)|(ME)|(MH)|(MD)|(MA)|(MI)|(MN)|(MS)|(MO)|(MT)|(NE)|(NV)|(NH)|(NJ)|(NM)|(NY)|(NC)|(ND)|(MP)|(OH)|(OK)|(OR)|(PW)|(PA)|(PR)|(RI)|(SC)|(SD)|(TN)|(TX)|(UT)|(VT)|(VI)|(VA)|(WA)|(WV)|(WI)|(WY)|" +
 		       "(al)|(ak)|(as)|(az)|(ar)|(ca)|(co)|(ct)|(de)|(dc)|(fm)|(fl)|(ga)|(gu)|(hi)|(id)|(il)|(in)|(ia)|(ks)|(ky)|(la)|(me)|(mh)|(md)|(ma)|(mi)|(mn)|(ms)|(mo)|(mt)|(ne)|(nv)|(nh)|(nj)|(nm)|(ny)|(nc)|(nd)|(mp)|(oh)|(ok)|(or)|(pw)|(pa)|(pr)|(ri)|(sc)|(sd)|(tn)|(tx)|(ut)|(vt)|(vi)|(va)|(wa)|(wv)|(wi)|(wy)|" +
 		       "(Al)|(Ak)|(As)|(Az)|(Ar)|(Ca)|(Co)|(Ct)|(De)|(Dc)|(Fm)|(Fl)|(Ga)|(Gu)|(Hi)|(Id)|(Il)|(In)|(Ia)|(Ks)|(Ky)|(La)|(Me)|(Mh)|(Md)|(Ma)|(Mi)|(Mn)|(Ms)|(Mo)|(Mt)|(Ne)|(Nv)|(Nh)|(Nj)|(Nm)|(Ny)|(Nc)|(Nd)|(Mp)|(Oh)|(Ok)|(Or)|(Pw)|(Pa)|(Pr)|(Ri)|(Sc)|(Sd)|(Tn)|(Tx)|(Ut)|(Vt)|(Vi)|(Va)|(Wa)|(Wv)|(Wi)|(Wy))$");
        rstate.getMessages().setRegexText("You have enter an invalid State abbreviation");
        rstate.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(rstate.getValue() == null)) {
					if (rstate.getValue().length() == 2) {
						//tzip.focus();
					}
				}
			}
        });
        rstate.setAllowBlank(true);
        rstate.setEnabled(true);
		rstate.setValue(entity.getRstate());
        rstate.setTabIndex(8);
        rstate.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (rstate.isValid()) {
					if (!(raddress2 == null)) {
						raddress2.setAllowBlank(true);
						raddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						raddress2.setValue(null);
						raddress.setAllowBlank(false);
						rcity.setAllowBlank(false);
						rstate.setAllowBlank(false);
						rzip.setAllowBlank(false);
						raddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						rcity.setFieldLabel("<span class='boldred12'>*</span> City");
						rstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						rzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PhysMailingState", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        middle2.add(rstate, formData);
         
        rzip = new TextField<String>();  
        if (entity.getForeign()) {
        	rzip.setFieldLabel("Zip");
        } else {
        	rzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
        }        
        rzip.setMinLength(5);
        rzip.setMaxLength(5);
        rzip.setMessageTarget("tooltip");
        rzip.setRegex("(\\d{5})");
        rzip.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
        rzip.getMessages().setRegexText("Your Zip Code is invalid, must be 5 numeric characters");
        rzip.setAllowBlank(true);
        rzip.setEnabled(true);
		rzip.setValue(entity.getRzip());
        rzip.setTabIndex(9);
        rzip.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (rzip.isValid()) {
					if (!(raddress2 == null)) {
						raddress2.setAllowBlank(true);
						raddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)"); 
						raddress2.setValue(null);
						raddress.setAllowBlank(false);
						rcity.setAllowBlank(false);
						rstate.setAllowBlank(false);
						rzip.setAllowBlank(false);
						raddress.setFieldLabel("<span class='boldred12'>*</span> Address");
						rcity.setFieldLabel("<span class='boldred12'>*</span> City");
						rstate.setFieldLabel("<span class='boldred12'>*</span> State (2 characters)");
						rzip.setFieldLabel("<span class='boldred12'>*</span> Zip");
					}
					cr.getAddressChanges("PhysMailingZip", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        right.add(rzip, formData);    
        
        raddress2 = new TextField<String>();
        if (entity.getForeign()) {
        	raddress2.setFieldLabel("<span class='boldred12'>*</span> <b>OR</b> Foreign Address (if applicable)");  
        } else {
        	raddress2.setFieldLabel("<b>OR</b> Foreign Address (if applicable)");  
        }        
        raddress2.setAllowBlank(true);
		raddress2.setValue(entity.getRaddress2());
        raddress2.addListener(Events.OnBlur, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(be.getField().getValue() == null)) {
					if (be.getField().getValue().toString().matches("\\b[P|p]*(OST|ost)*\\.*\\s*[O|o|0]*(ffice|FFICE)*\\.*\\s*[B|b][O|o|0][X|x]\\b")) {
						raddress2.markInvalid("test");
						raddress2.setMessageTarget("tooltip");
						raddress2.getMessages().setRegexText("Invalid address, contains a post office box");
						gmsg.alert("Requirements not met", "<span class='regred12'>You can't enter a PO Box as a Physical Address</span>", 250);
						raddress2.setValue(null);
					}
				}
			}        	
        });
        raddress2.setTabIndex(10);
        raddress2.addListener(Events.Change, new Listener<FieldEvent>() {
        	@Override
			public void handleEvent(FieldEvent be) {
				if (raddress2.isValid()) {
					if (!(raddress == null)) {
						raddress.setAllowBlank(true);
						raddress.setFieldLabel("Address"); 
						raddress.setValue(null);
						raddress2.setAllowBlank(false);
						rcity.setAllowBlank(true);
						rstate.setAllowBlank(true);
						rzip.setAllowBlank(true);
						rcity.setValue(null);
						rstate.setValue(null);
						rzip.setValue(null);
						raddress2.setFieldLabel("<span class='boldred12'>*</span> <b>OR</b> Foreign Address (if applicable)");  
						rcity.setFieldLabel("City");
						rstate.setFieldLabel("State (2 characters)");
						rzip.setFieldLabel("Zip");
					}
					cr.getAddressChanges("PhysMailingAddress2", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        raddress2.setEnabled(true);  
        tfadd.add(raddress2, formData);
        
        years = new TextField<String>();  
        years.setFieldLabel("<span class='boldred12'>*</span> Years at"); 
        years.setMinLength(1);
        years.setMaxLength(2);
        years.setTitle("Enter the number of years at this address (0 - 99), no spaces");
        years.setMessageTarget("tooltip");
        years.setRegex("^[0-9][0-9]?");
        years.getMessages().setMaxLengthText("You have exceeded the maximum character length (2) for this field");
        years.getMessages().setRegexText("You may only specifiy a number from 0 - 99, no leading zeros or spaces please");
        years.setAllowBlank(false);  
        years.setTabIndex(11);
        years.setEnabled(true);
        years.setValue(entity.getYears());
        ym.add(years, formData);  
           
        months = new TextField<String>();  
        months.setFieldLabel("<span class='boldred12'>*</span> Months at");
        months.setMinLength(1);
        months.setMaxLength(2);
        months.setTitle("Enter the number of months at this address (0 - 11), no spaces");
        months.setMessageTarget("tooltip");
        months.setRegex("^[0-9][0-1]?");
        months.getMessages().setMaxLengthText("You have exceeded the maximum character length (2) for this field");
        months.getMessages().setRegexText("You may only specifiy a number from 0 - 11, no leading zeros or spaces please");
        months.setAllowBlank(false);
        months.setTabIndex(12);
        months.setEnabled(true);
        months.setValue(entity.getMonths());
        mr.add(months, formData);  
         
        main2.add(left, new ColumnData(0.42));
        main2.add(middle1, new ColumnData(0.20));
        main2.add(middle2, new ColumnData(0.17));
        main2.add(right, new ColumnData(0.17));
        main12.add(tfadd, new ColumnData(0.58));
        main12.add(ym, new ColumnData(0.19));
        main12.add(mr, new ColumnData(0.19));
        
        temp1.add(main2, new FormData("100%"));   
        temp1.add(main12, new FormData("100%"));   
        
        phys.add(temp1);            
        
        final FieldSet aaddr = new FieldSet();
        aaddr.setHeadingHtml("<span class='boldorange12'>Additional Residency Information</span><span class='regred12'>&nbsp;&nbsp;-&nbsp;&nbsp;failure to provide requested information may result in assessment of a nonresident fee differential</span>");
        aaddr.setAutoHeight(true);
        aaddr.setAutoWidth(true);
		//temp.setStyleAttribute("margin-top", "-50px");
        aaddr.setStyleAttribute("margin-left", "-10px");
        aaddr.setStyleAttribute("margin-right", "-10px");
        aaddr.addStyleName("addressFieldSets");
		temp1 = new ContentPanel();
		temp1.setHeaderVisible(false);
		temp1.setBodyBorder(false);
		temp1.setBorders(false);
		temp1.setFrame(true);
		temp1.setAutoHeight(true);
		temp1.setAutoWidth(true);
		temp1.addStyleName("addressFieldSets");
		
		LayoutContainer main14 = new LayoutContainer();
		main14.setLayout(new ColumnLayout());
		
		LayoutContainer main15 = new LayoutContainer();
		main15.setLayout(new ColumnLayout());
		
		//LayoutContainer main16 = new LayoutContainer();
		//main16.setLayout(new ColumnLayout());
		
		left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "5px");
        left.setStyleAttribute("paddingTop", "5px");
        layout = new FormLayout();  
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(280); 
        left.setLayout(layout);  
        
        LayoutContainer left2 = new LayoutContainer();
        left2.setStyleAttribute("paddingRight", "5px");
        left2.setStyleAttribute("paddingTop", "5px");
        FormLayout layouta = new FormLayout();  
        layouta.setLabelAlign(LabelAlign.LEFT);
        layouta.setLabelWidth(280); 
        left2.setLayout(layouta);  
        
        /*LayoutContainer left3 = new LayoutContainer();
        left3.setStyleAttribute("paddingRight", "5px");
        left3.setStyleAttribute("paddingTop", "5px");
        FormLayout layoutb = new FormLayout();  
        layoutb.setLabelAlign(LabelAlign.LEFT);
        layoutb.setLabelWidth(280); 
        left3.setLayout(layoutb);*/  
        
        right = new LayoutContainer();
        right.setStyleAttribute("paddingTop", "5px");
        layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(85);  
        right.setLayout(layout4);
        
        LayoutContainer right2 = new LayoutContainer();
        right2.setStyleAttribute("paddingTop", "5px");
        FormLayout layout4a = new FormLayout();  
        layout4a.setLabelAlign(LabelAlign.LEFT);  
        layout4a.setLabelWidth(175);  
        right2.setLayout(layout4a);
        
        /*LayoutContainer right3 = new LayoutContainer();
        right3.setStyleAttribute("paddingTop", "5px");
        FormLayout layout4b = new FormLayout();  
        layout4b.setLabelAlign(LabelAlign.LEFT);  
        layout4b.setLabelWidth(175);  
        right3.setLayout(layout4b);*/
		
		pastyear = new RadioGroup();
		//pastyear.setWidth(100);
		pastyear.setStyleName("opt1radiogroup");
		pastyear.setFieldLabel("1. Within the previous 365 days, have you resided anywhere else?");
	    pyyes = new Radio();
	    pyyes.setName("pyradio");
	    pyyes.setStyleName("pyradios");
	    pyyes.setBoxLabel("- <span class='regblue12'>Yes</span>");
	    pyyes.setTitle("Select this option if you resided anywhere else in the previous 365 days.");
	    pyyes.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {            	
                pyno.setValue(false);
                //pytext.setEnabled(true);
            }
        });
	    //pyyes.setTabIndex(21);
	    pastyear.add(pyyes);
	    
	    pyno = new Radio();
	    pyno.setName("pyradio");
	    pyno.setStyleName("pyradios");
	    pyno.setBoxLabel("- <span class='regblue12'>No</span>");
	    pyno.setTitle("Select this option if you HAVE NOT resided anywhere else in the previous 365 days.");
	    pyno.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                pyyes.setValue(false);
                //pytext.setEnabled(false);
            }
        });
	    //pyno.setTabIndex(22);
	    pastyear.add(pyno);
	    
	    left.add(pastyear, formData);  
	    
	    pytext = new TextField<String>();  
	    pytext.setFieldLabel("If yes, where?");
	    pytext.setAllowBlank(true);
	    //pytext.setTabIndex(14);	        
	    //pytext.setEnabled(false);
        
	    right.add(pytext, formData);  
	    
	    main14.add(left, new ColumnData(0.42));
	    main14.add(right, new ColumnData(0.57));	    
	    
	    alaskaid = new RadioGroup();
		//pastyear.setWidth(100);
	    alaskaid.setStyleName("opt1radiogroup");
	    alaskaid.setFieldLabel("2. Do you have a current Alaska driver's license or other Alaska ID?");
	    akidyes = new Radio();
	    akidyes.setName("idradio");
	    akidyes.setStyleName("pyradios");
	    akidyes.setBoxLabel("- <span class='regblue12'>Yes</span>");
	    akidyes.setTitle("Select this option if you have a current Alaska driver's or other Alaska ID.");
	    akidyes.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {            	
            	akidno.setValue(false);
            	akidtext.setFieldLabel("If yes, please provide number:");
            	//akidtext.setEnabled(true);
            	//if (akvoteno.getValue().equals(true)) {
            		//othertext.setEnabled(true);
            	//} else {
            		//othertext.setEnabled(false);
            	//}
            }
        });
	    //pyyes.setTabIndex(21);
	    alaskaid.add(akidyes);
	    akidno = new Radio();
	    akidno.setName("idradio");
	    akidno.setStyleName("pyradios");
	    akidno.setBoxLabel("- <span class='regblue12'>No</span>");
	    akidno.setTitle("Select this option if you DO NOT have a current Alaska driver's or other Alaska ID.");
	    akidno.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
            	akidyes.setValue(false);
            	akidtext.setFieldLabel("If no, please provide explanation:");
            	//akidtext.setEnabled(false);
            	//othertext.setEnabled(true);
            }
        });
	    //pyno.setTabIndex(22);
	    alaskaid.add(akidno);
	    
	    left2.add(alaskaid, formData);  
	    
	    akidtext = new TextField<String>();  
	    akidtext.setFieldLabel("If yes, please provide number");   
	    akidtext.setAllowBlank(true);
	    //pytext.setTabIndex(14);	        
	    //akidtext.setEnabled(false);
        
	    right2.add(akidtext, formData);  
	    
	    main15.add(left2, new ColumnData(0.42));
	    main15.add(right2, new ColumnData(0.57));	    	    
	    
	    /*alaskavote = new RadioGroup();
		//pastyear.setWidth(100);
	    alaskavote.setStyleName("opt1radiogroup");
	    alaskavote.setFieldLabel("3. Are you currently registered to vote in Alaska?");
	    akvoteyes = new Radio();
	    akvoteyes.setName("vtradio");
	    akvoteyes.setStyleName("pyradios");
	    akvoteyes.setBoxLabel("- <span class='regblue12'>Yes</span>");
	    akvoteyes.setTitle("Select this option if you are currently registered to vote in Alaska.");
	    akvoteyes.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {            	
            	akvoteno.setValue(false);
            	if (akidno.getValue().equals(true)) {
            		//othertext.setEnabled(true);
            	} else {
            		//othertext.setEnabled(false);
            	}
            }
        });
	    //pyyes.setTabIndex(21);
	    alaskavote.add(akvoteyes);
	    akvoteno = new Radio();
	    akvoteno.setName("vtradio");
	    akvoteno.setStyleName("pyradios");
	    akvoteno.setBoxLabel("- <span class='regblue12'>No</span>");
	    akvoteno.setTitle("Select this option if you are NOT currently registered to vote in Alaska.");
	    akvoteno.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
            	akvoteyes.setValue(false);
            	//othertext.setEnabled(true);
            }
        });
	    //pyno.setTabIndex(22);
	    alaskavote.add(akvoteno);
	    
	    left3.add(alaskavote, formData);  
	    
	    othertext = new TextField<String>();  
	    othertext.setFieldLabel("If no to 2 or 3, please provide brief explanation");
	    othertext.setAllowBlank(true);
	    //pytext.setTabIndex(14);	        
	    //othertext.setEnabled(false);
        
	    right3.add(othertext, formData);*/
	    
	    //main16.add(left3, new ColumnData(0.42));
	    //main16.add(right3, new ColumnData(0.57));	    

        temp1.add(main14, new FormData("100%"));
        temp1.add(main15, new FormData("100%"));
        //temp1.add(main16, new FormData("100%"));
        
        // TODO
        // previous year choice
        if (!(entity.getPyearabsent()==null)) {
	    	if (entity.getPyearabsent().equalsIgnoreCase("yes")) {
		    	pyyes.setValue(true);
		    } else {
		    	pyno.setValue(true);
		    }
	    }
        // pyear explanation
        if (!(entity.getPyearabsenttext()==null)) {
	    	pytext.setValue(entity.getPyearabsenttext());
	    	//pytext.setEnabled(true);
	    } else {
	    	pytext.setValue(null);
	    	//pytext.setEnabled(false);
	    }
        // alaska ID
        if (!(entity.getAlaskaid()==null)) {
	    	if (entity.getAlaskaid().equalsIgnoreCase("yes")) {
	    		akidyes.setValue(true);
	    		akidtext.setFieldLabel("If yes, please provide number");
		    } else {
		    	akidno.setValue(true);
		    	akidtext.setFieldLabel("If no, please provide explanation:");
		    }
	    }
        if (!(entity.getAlaskaidtext()==null)) {
        	akidtext.setValue(entity.getAlaskaidtext());
	    	//pytext.setEnabled(true);
	    } else {
	    	akidtext.setValue(null);
	    	//pytext.setEnabled(false);
	    }
        // registered to vote
        /*if (!(entity.getAlaskavote()==null)) {
	    	if (entity.getAlaskavote().equalsIgnoreCase("yes")) {
	    		akvoteyes.setValue(true);
		    } else {
		    	akvoteno.setValue(true);
		    }
	    }
        // other text
        if (!(entity.getOthertext()==null)) {
        	othertext.setValue(entity.getOthertext());
	    	//pytext.setEnabled(true);
	    } else {
	    	othertext.setValue(null);
	    	//pytext.setEnabled(false);
	    }*/ 
        
        
        /*
         * disable if not a resident
         */
        if (entity.getResidency().toString().equals("resident")) {
	    	aaddr.setEnabled(true);
	    } else {
	    	aaddr.setEnabled(false);
	    }	 
        
		aaddr.add(temp1);           
        
        final FieldSet temp = new FieldSet();
		temp.setHeadingHtml("<span class='boldorange12'>Temporary Mailing Address</span>");
		temp.setAutoHeight(true);
		temp.setAutoWidth(true);
		//temp.setStyleAttribute("margin-top", "-50px");
		temp.setStyleAttribute("margin-left", "-10px");
		temp.setStyleAttribute("margin-right", "-10px");
		temp.addStyleName("addressFieldSets");
		temp1 = new ContentPanel();
		temp1.setHeaderVisible(false);
		temp1.setBodyBorder(false);
		temp1.setBorders(false);
		temp1.setFrame(true);
		temp1.setAutoHeight(true);
		temp1.setAutoWidth(true);
		temp1.addStyleName("addressFieldSets");
		
		LayoutContainer main8 = new LayoutContainer();
		main8.setLayout(new ColumnLayout());
		
		LayoutContainer main9 = new LayoutContainer();
		main9.setLayout(new ColumnLayout());
		
		LayoutContainer main10 = new LayoutContainer();
		main10.setLayout(new ColumnLayout());
		
		LayoutContainer add2 = new LayoutContainer();
		FormLayout layoutadd2 = new FormLayout();
		add2.setLayout(layoutadd2);
		
		left = new LayoutContainer();
        left.setStyleAttribute("paddingRight", "10px");
        left.setStyleAttribute("paddingTop", "5px");
        layout = new FormLayout();  
        layout.setLabelAlign(LabelAlign.LEFT);
        layout.setLabelWidth(59); 
        left.setLayout(layout);  
            
        middle1 = new LayoutContainer();  
        middle1.setStyleAttribute("paddingRight", "10px");
        middle1.setStyleAttribute("paddingTop", "5px");
        layout2 = new FormLayout();  
        layout2.setLabelAlign(LabelAlign.LEFT);  
        layout2.setLabelWidth(36);  
        middle1.setLayout(layout2);
            
        middle2 = new LayoutContainer();  
        middle2.setStyleAttribute("paddingRight", "10px");
        middle2.setStyleAttribute("paddingTop", "5px");
        layout3 = new FormLayout();  
        layout3.setLabelAlign(LabelAlign.LEFT);  
        layout3.setLabelWidth(125);  
        middle2.setLayout(layout3);
        
        right = new LayoutContainer();
        right.setStyleAttribute("paddingTop", "5px");
        layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(32);  
        right.setLayout(layout4);
        
        taddress = new TextField<String>();  
        taddress.setFieldLabel("Address");  
        taddress.setAllowBlank(true);
        taddress.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (taddress.isValid()) {
					cr.getAddressChanges("TempMailingAddress", cfecid.getValue().toString(), be, entity, changeList);
				}
			}
        });
        taddress.setTabIndex(13);
        taddress.setEnabled(false);
        left.add(taddress, formData);  
           
        tcity = new TextField<String>();  
        tcity.setFieldLabel("City");
        tcity.setAllowBlank(true);
        tcity.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (tcity.isValid()) {
					cr.getAddressChanges("TempMailingCity", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        tcity.setTabIndex(14);	        
        tcity.setEnabled(false);
        middle1.add(tcity, formData);  
       
        tstate = new TextField<String>();  
        tstate.setFieldLabel("State (2 characters)");
        tstate.setMinLength(2);
        tstate.setMaxLength(2);
        tstate.getMessages().setMaxLengthText("You have exceeded the maximum character length (2) for this field");
        tstate.setMessageTarget("tooltip");
        tstate.setRegex("^((AL)|(AK)|(AS)|(AZ)|(AR)|(CA)|(CO)|(CT)|(DE)|(DC)|(FM)|(FL)|(GA)|(GU)|(HI)|(ID)|(IL)|(IN)|(IA)|(KS)|(KY)|(LA)|(ME)|(MH)|(MD)|(MA)|(MI)|(MN)|(MS)|(MO)|(MT)|(NE)|(NV)|(NH)|(NJ)|(NM)|(NY)|(NC)|(ND)|(MP)|(OH)|(OK)|(OR)|(PW)|(PA)|(PR)|(RI)|(SC)|(SD)|(TN)|(TX)|(UT)|(VT)|(VI)|(VA)|(WA)|(WV)|(WI)|(WY)|" +
        		       "(al)|(ak)|(as)|(az)|(ar)|(ca)|(co)|(ct)|(de)|(dc)|(fm)|(fl)|(ga)|(gu)|(hi)|(id)|(il)|(in)|(ia)|(ks)|(ky)|(la)|(me)|(mh)|(md)|(ma)|(mi)|(mn)|(ms)|(mo)|(mt)|(ne)|(nv)|(nh)|(nj)|(nm)|(ny)|(nc)|(nd)|(mp)|(oh)|(ok)|(or)|(pw)|(pa)|(pr)|(ri)|(sc)|(sd)|(tn)|(tx)|(ut)|(vt)|(vi)|(va)|(wa)|(wv)|(wi)|(wy)|" +
        		       "(Al)|(Ak)|(As)|(Az)|(Ar)|(Ca)|(Co)|(Ct)|(De)|(Dc)|(Fm)|(Fl)|(Ga)|(Gu)|(Hi)|(Id)|(Il)|(In)|(Ia)|(Ks)|(Ky)|(La)|(Me)|(Mh)|(Md)|(Ma)|(Mi)|(Mn)|(Ms)|(Mo)|(Mt)|(Ne)|(Nv)|(Nh)|(Nj)|(Nm)|(Ny)|(Nc)|(Nd)|(Mp)|(Oh)|(Ok)|(Or)|(Pw)|(Pa)|(Pr)|(Ri)|(Sc)|(Sd)|(Tn)|(Tx)|(Ut)|(Vt)|(Vi)|(Va)|(Wa)|(Wv)|(Wi)|(Wy))$");
        tstate.getMessages().setRegexText("You have enter an invalid State abbreviation");
        tstate.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(tstate.getValue() == null)) {
					if (tstate.getValue().length() == 2) {
						//zip.focus();
					}
				}
			}
        });
        tstate.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (tstate.isValid()) {
					cr.getAddressChanges("TempMailingState", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        tstate.setAllowBlank(true);
        tstate.setTabIndex(15);
       	tstate.setEnabled(false);
        middle2.add(tstate, formData);
         
        tzip = new TextField<String>();  
        tzip.setFieldLabel("Zip");
        tzip.setMinLength(5);
        tzip.setMaxLength(5);
        tzip.setMessageTarget("tooltip");
        tzip.setRegex("(\\d{5})");
        tzip.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
        tzip.getMessages().setRegexText("Your Zip Code is invalid, must be 5 numeric characters");
        tzip.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(tzip.getValue() == null)) {
					if (tzip.getValue().length() == 5) {
						//years.focus();
					}
			    }
			}
        });
        tzip.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (tzip.isValid()) {
					cr.getAddressChanges("TempMailingZip", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        tzip.setAllowBlank(true);
        tzip.setTabIndex(16);
        tzip.setEnabled(false);
        right.add(tzip, formData);
        			
		nop.setName("addchoice");
		nop.setHideLabel(true);
		nop.setBoxLabel("Select to enter new Shipping address or unselect to cancel");
		nop.setWidth(460);
		nop.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
            	//final Boolean value = (Boolean) be.getValue();
            	if (entity.getCitizen().equalsIgnoreCase("false")) {
        			nop.setBoxLabel("You CAN NOT specify a Temporary Mail Address is you are a NON-US Citizen");
        			statusBar.setHTML("<span class='regred12'>*** You CAN NOT specify a Temporary Mail Address is you are a NON-US Citizen ***</span>");
        			taddress.setEnabled(false);
	            	tcity.setEnabled(false);
	            	tstate.setEnabled(false);
	            	tzip.setEnabled(false);
	            	taddress.setValue(null);
	                tcity.setValue(null);
	                tstate.setValue(null);
	                tzip.setValue(null);
	                taddress.setReadOnly(true);
	                tcity.setReadOnly(true);
	                tstate.setReadOnly(true);
	                tzip.setReadOnly(true);
        		} else if (nop.getValue().equals(true)) {
            		taddress.setEnabled(true);
	            	tcity.setEnabled(true);
	            	tstate.setEnabled(true);
	            	tzip.setEnabled(true);
	                taddress.setReadOnly(false);
	                tcity.setReadOnly(false);
	                tstate.setReadOnly(false);
	                tzip.setReadOnly(false);
	                taddress.setAllowBlank(false);
	                tcity.setAllowBlank(false);
	                tstate.setAllowBlank(false);
	                tzip.setAllowBlank(false);
            	} else {
            		taddress.setEnabled(false);
	            	tcity.setEnabled(false);
	            	tstate.setEnabled(false);
	            	tzip.setEnabled(false);
	            	taddress.setValue(null);
	                tcity.setValue(null);
	                tstate.setValue(null);
	                tzip.setValue(null);
	                taddress.setReadOnly(true);
	                tcity.setReadOnly(true);
	                tstate.setReadOnly(true);
	                tzip.setReadOnly(true);
            	}
            }
        });
		add2.add(nop, formData);
		
		main8.add(left, new ColumnData(0.42));
        main8.add(middle1, new ColumnData(0.20));
        main8.add(middle2, new ColumnData(0.17));
        main8.add(right, new ColumnData(0.17));
		main10.add(add2, new ColumnData(0.50));

        temp1.add(main8, new FormData("100%"));
        temp1.add(main10, new FormData("100%"));

		temp.add(temp1);   
	    
	    FieldSet cert = new FieldSet();
        cert.setHeadingHtml("<span class='boldorange12'>Contact Info</span>");
        cert.setAutoHeight(true);
        cert.setStyleAttribute("margin-left", "-10px");
        cert.setStyleAttribute("margin-right", "-10px");
        cert.addStyleName("addressFieldSets");
        
        ContentPanel cert1 = new ContentPanel();
        cert1.setHeaderVisible(false);
        cert1.setBodyBorder(false);
        cert1.setBorders(false);
        cert1.setFrame(true);
        cert1.setAutoHeight(true);
        cert1.setAutoWidth(true);
        cert1.setWidth("97%");
        cert1.addStyleName("addressFieldSets");
        
        ContentPanel cert3 = new ContentPanel();
        cert3.setHeaderVisible(false);
        cert3.setBodyBorder(false);
        cert3.setBorders(false);
        cert3.setFrame(true);
        cert3.setAutoHeight(true);
        cert3.setAutoWidth(true);
        cert3.setWidth("97%");
        cert3.addStyleName("addressFieldSets");
        
        ContentPanel certPhone = new ContentPanel();
        certPhone.setHeaderVisible(false);
        certPhone.setBodyBorder(false);
        certPhone.setBorders(false);
        certPhone.setFrame(false);
        certPhone.setAutoHeight(true);
        certPhone.setAutoWidth(true);
        certPhone.setWidth("97%");
        certPhone.addStyleName("addressFieldSets");
        
        ContentPanel cert4 = new ContentPanel();
        cert4.setHeaderVisible(false);
        cert4.setBodyBorder(false);
        cert4.setBorders(false);
        cert4.setFrame(false);
        cert4.setAutoHeight(true);
        cert4.setAutoWidth(true);
        cert4.setWidth("97%");
        cert4.addStyleName("addressFieldSets");
        
        LayoutContainer main3 = new LayoutContainer();  
        main3.setLayout(new ColumnLayout()); 
        
        LayoutContainer main4 = new LayoutContainer();  
        main4.setLayout(new ColumnLayout()); 
        
        LayoutContainer main5 = new LayoutContainer();  
        main5.setLayout(new ColumnLayout()); 
        
        LayoutContainer main6 = new LayoutContainer();  
        main6.setLayout(new ColumnLayout()); 
                
        if (entity.getResidency().toString().equals("resident")) {
        	reschoice.setHTML("<span class='boldred12'>Please note:</span>  Your current Alaska residency declaration is: <span class='boldblack12'>RESIDENT</span>");
        } else {
        	reschoice.setHTML("<span class='boldred12'>Please note:</span>  Your current Alaska residency declaration is: <span class='boldblack12'>NON-RESIDENT</span>");
        }
        
        left = new LayoutContainer(); 
        left.setStyleAttribute("paddingRight", "5px");
        layout = new FormLayout();  
        layout.setLabelAlign(LabelAlign.LEFT); 
        layout.setLabelWidth(52); 
        left.setLayout(layout); 
        
        middle1 = new LayoutContainer();  
        middle1.setStyleAttribute("paddingRight", "5px");
        layout2 = new FormLayout();  
        layout2.setLabelAlign(LabelAlign.LEFT);  
        layout2.setLabelWidth(5);  
        middle1.setLayout(layout2);
        
        middle2 = new LayoutContainer();  
        middle2.setStyleAttribute("paddingRight", "10px");
        layout3 = new FormLayout();  
        layout3.setLabelAlign(LabelAlign.LEFT);  
        layout3.setLabelWidth(5);  
        middle2.setLayout(layout3);
        
        right = new LayoutContainer();  
        layout4 = new FormLayout();  
        layout4.setLabelAlign(LabelAlign.LEFT);  
        layout4.setLabelWidth(92);  
        right.setLayout(layout4); 
        
        LayoutContainer alone = new LayoutContainer();  
        FormLayout layout5 = new FormLayout();  
        alone.setLayout(layout5); 
        
        phoneArea = new TextField<String>();
        phoneArea.setFieldLabel("<span class='boldred12'>*</span> Phone");
        phoneArea.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(phoneArea.getValue() == null)) {
					if (phoneArea.getValue().length() == 3) {
						phonePre.focus();
					}
				}
			}
        });
        phoneArea.setAllowBlank(false);
        phoneArea.setMinLength(3);
        phoneArea.setMaxLength(3);
        phoneArea.setTitle("Enter your area code here");
        phoneArea.setMessageTarget("tooltip");
        phoneArea.setRegex("(\\d{3})");
        phoneArea.getMessages().setRegexText("Your Phone number Area Code is invalid");
        phoneArea.setTabIndex(17);
        phoneArea.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (phoneArea.isValid()) {
					cr.getAddressChanges("phoneArea", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        if (!(entity.getArea()==null)) {
        	phoneArea.setValue(entity.getArea());
        }        
        left.add(phoneArea, formData);
        
        phonePre = new TextField<String>();
        phonePre.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(phonePre.getValue() == null)) {
					if (phonePre.getValue().length() == 3) {
						phonePost.focus();
					}
				}
			}
        });
        phonePre.setAllowBlank(false);
        phonePre.setMinLength(3);
        phonePre.setMaxLength(3);
        phonePre.setTitle("Enter the first 3 digits of your phone number here");
        phonePre.setMessageTarget("tooltip");
        phonePre.setRegex("(\\d{3})");
        phonePre.getMessages().setRegexText("Your Phone number Prefix is invalid");
        phonePre.setTabIndex(18);
        phonePre.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (phonePre.isValid()) {
					cr.getAddressChanges("phonePre", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        if (!(entity.getPhone()==null)) {
        	if (entity.getPhone().toString().length() > 3) {
            	phonePre.setValue(entity.getPhone().substring(0,3).trim());
            }
        }
        middle1.add(phonePre, formData);
        
        phonePost = new TextField<String>();
        phonePost.addListener(Events.OnKeyUp, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (!(phonePost.getValue() == null)) {
					if (phonePost.getValue().length() == 4) {
						email.focus();
					}
				}
			}
        });
        phonePost.setAllowBlank(false);
        phonePost.setMinLength(4);
        phonePost.setMaxLength(4);
        phonePost.setTitle("Enter the last 4 digits of your phone number here");
        phonePost.setMessageTarget("tooltip");
        phonePost.setRegex("(\\d{4})");
        phonePost.getMessages().setRegexText("Your Phone number Suffix is invalid");
        phonePost.setTabIndex(19);
        phonePost.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (phonePost.isValid()) {
					cr.getAddressChanges("phonePost", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        if (!(entity.getPhone()==null)) {
        	if (entity.getPhone().toString().length() == 7) {
            	phonePost.setValue(entity.getPhone().substring(3,7).trim());
            }
        }        
        middle2.add(phonePost, formData);
        
        email = new TextField<String>();  
        email.setAllowBlank(false);
        email.setFieldLabel("<span class='boldred12'>*</span> Email Address");
        email.setTitle("Enter a valid email address that you have access to");
        email.setMessageTarget("tooltip");
        email.setRegex("^([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}$");
        email.getMessages().setRegexText("Your Email address is invalid");
        email.setTabIndex(20);
        email.addListener(Events.Change, new Listener<FieldEvent>() {
			@Override
			public void handleEvent(FieldEvent be) {
				if (email.isValid()) {
					cr.getAddressChanges("email", cfecid.getValue().toString(), be, entity, changeList);
				}
			}        	
        });
        if (!(entity.getEmail()==null)) {
        	email.setValue(entity.getEmail());
        }        
        right.add(email, formData);
         
        main5.add(left, new ColumnData(0.17));
        main5.add(middle1, new ColumnData(0.14));
        main5.add(middle2, new ColumnData(0.14));
        main5.add(right, new ColumnData(0.51));
        
        cert3.add(main5, new FormData("100%"));        
        
        cert.add(cert3);
        
        ppubv = new VerticalPanel();
        ppubv.setTableWidth("100%");
        ppubv.setStyleAttribute("background", "#EEEEEE");
        ppubv.setHorizontalAlign(HorizontalAlignment.CENTER);
        ppubv.setVerticalAlign(VerticalAlignment.MIDDLE);
	    TableData ppubHeader = new TableData();
	    ppubv.add(new Html("<span class='boldred12'>*</span><span class='boldblack12'> Phone Number / EMail public or private</span> - <span class='regblack12'>Default is NO</span>"), ppubHeader);
	    ppub = new RadioGroup();
	    ppub.setWidth(1000);
	    ppub.setStyleName("opt1radiogroup");
	    pyes = new Radio();
	    pyes.setName("ppublic");
	    pyes.setStyleName("renewal");
	    pyes.setBoxLabel("- <span class='regblue12'>Yes, allow my phone to be published</span>");
	    pyes.setTitle("Select this option if you wish for CFEC to make your phone number public.");
	    pyes.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {            	
                pno.setValue(false);
            }
        });
	    pyes.setTabIndex(21);
	    ppub.add(pyes);
	    pno = new Radio();
	    pno.setName("ppublic");
	    pno.setStyleName("forms");
	    pno.setBoxLabel("- <span class='regblue12'>No, do NOT allow my phone to be published</span>");
	    pno.setTitle("Select this option if you wish for CFEC to NOT make your phone number public.");
	    pno.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                pyes.setValue(false);
            }
        });
	    pno.setTabIndex(22);
	    ppub.add(pno);
	    ppubv.add(ppub);
	    if (!(entity.getPhonepub()==null)) {
	    	if (entity.getPhonepub().equalsIgnoreCase("yes")) {
		    	pyes.setValue(true);
		    } else {
		    	pno.setValue(true);
		    }
	    } else {
	    	pno.setValue(true);
	    }
	    
	    certPhone.add(ppubv);
	    
	    epubv = new VerticalPanel();
        epubv.setTableWidth("100%");
        epubv.setStyleAttribute("background", "#EEEEEE");
        epubv.setHorizontalAlign(HorizontalAlignment.CENTER);
        epubv.setVerticalAlign(VerticalAlignment.MIDDLE);
	    
	    epub = new RadioGroup();
	    epub.setWidth(1000);
	    epub.setStyleName("opt1radiogroup");
	    eyes = new Radio();
	    eyes.setName("epublic");
	    eyes.setStyleName("renewal");
	    eyes.setBoxLabel("- <span class='regblue12'>Yes, allow my email to be published</span>");
	    eyes.setTitle("Select this option if you wish for CFEC to make your email address public.");
	    eyes.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {            	
                eno.setValue(false);
            }
        });
	    eyes.setTabIndex(21);
	    epub.add(eyes);
	    eno = new Radio();
	    eno.setName("epublic");
	    eno.setStyleName("forms");
	    eno.setBoxLabel("- <span class='regblue12'>No, do NOT allow my email to be published</span>");
	    eno.setTitle("Select this option if you wish for CFEC to NOT make your email public.");
	    eno.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
                eyes.setValue(false);
            }
        });
	    eno.setTabIndex(22);
	    epub.add(eno);
	    epubv.add(epub);
	    if (!(entity.getEmailpub()==null)) {
	    	if (entity.getEmailpub().equalsIgnoreCase("yes")) {
		    	eyes.setValue(true);
		    } else {
		    	eno.setValue(true);
		    }
	    } else {
	    	eno.setValue(true);
	    }
	    certPhone.add(epubv);
	    cert.add(certPhone);

        disclaimers = new VerticalPanel();
        disclaimers.setTableWidth("100%");
        disclaimers.setAutoHeight(true);
        disclaimers.setAutoWidth(true);
        disclaimers.setStyleName("resdis");
        disclaimers.setVerticalAlign(VerticalAlignment.MIDDLE);
        disclaimers.setHorizontalAlign(HorizontalAlignment.CENTER);        
        
        if (entity.getResidency().toString().equals("resident")) {
        	physreq.setHTML("<span class='boldred12'>Please note:</span><span class='regblack12'>  Residents ARE required to enter a physical address</span>");
        } else {
        	physreq.setHTML("<span class='boldred12'>Please note:</span><span class='regblack12'>  Non Residents are NOT required to enter a physical address</span>");
        }
        
        disclaimers.add(physreq);
        disclaimers.add(reschoice);
        alone.add(disclaimers, formData);
        
        main6.add(alone, new ColumnData(1.00));
        
        cert4.add(main6, new FormData("100%"));
        
        cert.add(cert4);
        
        addressPanel.add(perm);
        addressPanel.add(phys);
        addressPanel.add(aaddr);
        addressPanel.add(temp);
        addressPanel.add(cert);
	    
	    bottomRight.add(addressPanel);
	    bottomRight.setHeadingHtml("<span class='boldblack12'>Address and Residency Data</span>");
	    bottomRight.layout();
	    
	    if (entity.getFirsttime().equalsIgnoreCase("true")) {
	    	if (entity.getResidency() != null) {
	    		gmsg.alert("Visitor Message", "<span class='boldblack12'><center>You have previous LEON records</center></span><br>" +
						"<br><font align='left'><span class='regblack12'>I see that you, "+entity.getId().getCfecid()+", have utilized LEON for online renewals in a previous year. " +
						"please take a moment to read this helpful note.<br><br>This process will take you " +
						"through a series of screens, each relative to your particular permits and/or vessels.</span>" +
						"<span class='boldblack12'>Please DO NOT use the browsers back and forward buttons</span>.  " +
						"<span class='regblack12'>Use the previous and next buttons under the status, fee totals and navigation bar to move through " +
						"the application.  Simple instructions for each page may be found in the left column under screen " +
						"instructions. When your order is complete, please download your confirmation report and retain it for your records. " +
						"You may come to the site as often as you wish to renew other licenses or to review the status of " +
						"pending requests.<br><br><font color='red'>PLEASE REVIEW THIS PAGE and modify any value that is no longer correct</font>, prior to selecting the Next button.<br><br>If you have any questions at all, please contact us at 1-907-789-6150.</span>", 500);
		    	Log.info("First Time User " + entity.getId().getCfecid() + " - (has no existing records for current year " + entity.getId().getRyear() + "), but does have record(s) for previsous years");
	    	} else {
	    		gmsg.alert("You have previous record(s)", "<span class='boldblack12'><center>First Time with Online Renewal?</center></span><br>" +
						"<br><font align='left'><span class='regblack12'>This appears to be your first time using the CFEC Online Renewal program, " +
						"please take a moment to read this helpful note.<br><br>This process will take you " +
						"through a series of screens, each relative to your particular permits and/or vessels.</span>" +
						"<span class='boldblack12'>Please DO NOT use the browsers back and forward buttons</span>.  " +
						"<span class='regblack12'>Use the previous and next buttons under the status, fee totals and navigation bar to move through " +
						"the application.  Simple instructions for each page may be found in the left column under screen " +
						"instructions. When your order is complete, please download your confirmation report and retain it for your records. " +
						"You may come to the site as often as you wish to renew other licenses or to review the status of " +
						"pending requests.<br><br>If you have any questions at all, please contact us at 1-907-789-6150.</span>", 500);
		    	Log.info("First Time User " + entity.getId().getCfecid() + " - (has no existing records for any year " + entity.getId().getRyear() + ")");
	    	}	    	
	    	perm.collapse();
    		phys.setTitle("Establish your Physical Address");
    		phys.setHeadingHtml("<span class='boldorange12'>&nbsp;&nbsp;Physical Address</span><span class='regred12'>&nbsp;&nbsp;-&nbsp;&nbsp;Please enter/update your Physical Address (where you live, NO PO boxes allowed)</span>");
    		phys.setCheckboxToggle(false);
    		if (raddress == null && rcity == null && rstate == null && rzip == null && raddress2 == null) {
    			raddress.setAllowBlank(false);
    			rcity.setAllowBlank(false);
    			rstate.setAllowBlank(false);
    			rzip.setAllowBlank(false);
    			raddress2.setAllowBlank(true);
    		} else if (!(raddress == null)) {
    			raddress.setAllowBlank(false);
    			rcity.setAllowBlank(false);
    			rstate.setAllowBlank(false);
    			rzip.setAllowBlank(false);
    			raddress2.setAllowBlank(true);
    		} else if (!(raddress2 == null)) {
    			raddress.setAllowBlank(true);
    			rcity.setAllowBlank(true);
    			rstate.setAllowBlank(true);
    			rzip.setAllowBlank(true);
    			raddress2.setAllowBlank(false);
    		} else {
    			raddress.setAllowBlank(false);
    			rcity.setAllowBlank(false);
    			rstate.setAllowBlank(false);
    			rzip.setAllowBlank(false);
    			raddress2.setAllowBlank(false);
    		}
    		/*if (entity.getYears().toString().trim().length() > 0) {
	    		years.setValue(entity.getYears());
	    	} 
	    	if (entity.getMonths().toString().trim().length() > 0) {
	    		months.setValue(entity.getMonths());
	    	} 
    		if (!(entity.getArea()==null)) {    			
	    		if (entity.getArea().trim().length() == 3) {
	    			phoneArea.setValue(entity.getArea().trim());
		    	} else {
		    		phoneArea.setValue(null);
		    	}
	    	} else {
	    		phoneArea.setValue(null);
	    	}    		
	    	if (!(entity.getPhone()==null)) {
	    		if (entity.getPhone().trim().length() == 7) {
	    			phonePre.setValue(entity.getPhone().substring(0,3).trim());
	    			phonePost.setValue(entity.getPhone().substring(3,7).trim());
		    	} else {
		    		phonePre.setValue(null);
		    		phonePost.setValue(null);
		    	}
	    	} else {
	    		phonePre.setValue(null);
	    		phonePost.setValue(null);
	    	}
	    	Log.info(entity.getId().getCfecid() + " - has a phone publish request of " + entity.getPhonepub());
	    	if (entity.getPhonepub().equalsIgnoreCase("No")) {
	    		pno.setValue(true);
	    	} else if (entity.getPhonepub().equalsIgnoreCase("Yes")) {
	    		pyes.setValue(true);
	    	} 
	    	if (!(entity.getEmail()==null)) {
	    		email.setValue(entity.getEmail());
    		} 
    		rfirst = true;*/
	    } else {
	    	perm.collapse();
	    	//phys.collapse();
	    	//Log.info("Returning User " + entity.getId().getCfecid() + " - (has existing records for current year " + entity.getId().getRyear() + ")");
	    	firstTime = false;
	    	if (entity.getResidency().equalsIgnoreCase("resident")) {
	    		reschoice.setHTML("<span class='boldred12'>Please note:</span>  Your current Alaska residency declaration is: <span class='boldblack12'>RESIDENT</span>");
    	    } else {
    	        reschoice.setHTML("<span class='boldred12'>Please note:</span>  Your current Alaska residency declaration is: <span class='boldblack12'>NON-RESIDENT</span>");
    	    }
	    	/*if (entity.getRaddress().toString().trim().length() > 0) {
    			raddress.setValue(entity.getRaddress());
    		}
			if (entity.getRaddress2().toString().trim().length() > 0) {
				raddress2.setValue(entity.getRaddress2());			
			}
			if (entity.getRcity().toString().trim().length() > 0) {
				rcity.setValue(entity.getRcity());
			}
			if (entity.getRstate().toString().trim().length() > 0) {
				rstate.setValue(entity.getRstate());
			}
			if (entity.getRzip().toString().trim().length() > 0) {
				rzip.setValue(entity.getRzip());
			}
	    	if (entity.getYears().toString().trim().length() > 0) {
	    		years.setValue(entity.getYears());
	    	}
	    	if (entity.getMonths().toString().trim().length() > 0) {
	    		months.setValue(entity.getMonths());
	    	}
	    	
	    	 * disable ability to change Alien Registration Number, years and months of residency
	    	 
	    	//arn.disable();
	    	//years.disable();
	    	//months.disable();
	    	if (!(entity.getArea()==null)) {
	    		if (entity.getArea().trim().length() == 3) {
	    			phoneArea.setValue(entity.getArea());
		    	} else {
		    		phoneArea.setValue(null);
		    	}
	    	} else {
	    		phoneArea.setValue(null);
	    	}    		
	    	if (!(entity.getPhone()==null)) {
	    		if (entity.getPhone().trim().length() == 7) {
	    			phonePre.setValue(entity.getPhone().substring(0,3).trim());
	    			phonePost.setValue(entity.getPhone().substring(3,7).trim());
		    	} else {
		    		phonePre.setValue(null);
		    		phonePost.setValue(null);
		    	}
	    	} else {
	    		phonePre.setValue(null);
	    		phonePost.setValue(null);
	    	}
	    	//Log.info(entity.getId().getCfecid() + " - has a phone publish request of " + entity.getPhonepub());
	    	if (entity.getPhonepub().equalsIgnoreCase("No")) {
	    		pno.setValue(true);
	    	} else if (entity.getPhonepub().equalsIgnoreCase("Yes")) {
	    		pyes.setValue(true);
	    	}
    		if (!(entity.getEmail()==null)) {
    			if (entity.getEmail().trim().length() > 4) {
    	    		email.setValue(entity.getEmail());
    	    	} else {
    	    		email.setValue(null);
    	    	}
    		} else {
	    		email.setValue(null);
	    	}
    		*/
	    }
	}
	
}
