package com.cfecweb.leon.client;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.ArenewVesselsId;
import com.cfecweb.leon.client.model.FeeTotals;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

/*
 * This class is called when a user wants to license a new vessel. A new vessel can mean different things. It can be a used vessel that is new 
 * to the user, an actual new vessel or an existing vessel that was licensed by someone else the previous year. We don't show nor do we care
 * about vessel ownership, as long as someone pays for it.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class NewVesselBuilder {
	PmtVesSelectFunctions getVes = new PmtVesSelectFunctions();
	CheckBox strhand = null;
	CheckBox strpower = null;
	String areacode = null;
	ArenewVessels newvessel = null;
	Window editVeswin = null;
	ChangeRecorder cr = new ChangeRecorder();
	Button getnewVes = null;
	TextField<String> q2t = null;
	ButtonBar nvesbb = null;
	boolean found = false;
	String oldreg = null;
	String oldp1 = null;
	String oldp2 = null;
	
	/*
	 * This first method creates and displays a popup window that is first presented when a user selects the add new vessel button.
	 * It asked if they know the adfg number or do not know the adfg number. If they do know it, then a RCP is called to verify
	 * that adfg number as to it's current state and validity. If everything checks out, then a new windows is opened with that
	 * vessel's characteristics and an opportunity to make valid changes, then save to existing profile. If they don't know the adfg number, 
	 * then a new windows is opened that requests as much information as possible regarding the vessel. We then make an effort to 
	 * identify the vessel in our database. If so, show it to them. If not, then open up a new vessel windows that is all blanks and
	 * allow them to enter anything they want, except an adfg number.
	 */
	public Window doesExist(final UserMessages gmsg, final SessionTimer timer, final TextField cfecid, final HTML statusBar, final InstructionsText gins, 
			final ArenewEntity entity, final List<ArenewVessels> vlist, final EditorGrid<BaseModel> grid, final BaseListLoader loader, final FieldSet bottomRight, 
			final Button newves, final FeeTotals feeTotals, final getDataAsync service, final String ryear, final List<ArenewChanges> changeList, final FieldSet tr) {
		FormPanel pfp = new FormPanel();
		pfp.setHeaderVisible(false);
		pfp.setButtonAlign(HorizontalAlignment.CENTER);		
		final Window getadfgwin = new Window();
		getadfgwin.setSize(650, 250);
		getadfgwin.setHeadingHtml("License New Vessel");
		getadfgwin.setLayout(new FlowLayout());  
		getadfgwin.setScrollMode(Scroll.AUTO);
		getadfgwin.setFrame(true);
		getadfgwin.setBorders(true);
		getadfgwin.setIconStyle("icon-table");  
		getadfgwin.setClosable(false);
		getadfgwin.setButtonAlign(HorizontalAlignment.CENTER);
		getadfgwin.setStyleName("newPmtwin");		
		pfp.addText("<br><center><span class='regblack12'>Please enter the five digit ADF&G number<br>" +
			"you wish to license and then select the <b>NEXT</b> button.<br>" +
			"OR<br>" +
			"If you don't know the ADF&G number, please<br>" +
			"select the <b>NO ADF&G Number</b> button.</span></center><br>");		
		final TextField<String> eadfg = new TextField<String>();
		eadfg.setWidth(75);		
		eadfg.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
		eadfg.setMinLength(5);
		eadfg.setMaxLength(5);
		eadfg.setMessageTarget("tooltip");
		eadfg.setRegex("(\\d{5})");
        eadfg.getMessages().setRegexText("Your ADF&G number is invalid, must be 5 numeric characters");
		HorizontalPanel q1 = new HorizontalPanel();
        q1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
        	"<span class='regblack12'>Enter the ADF&G number of the vessel:." +
        	"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q1.add(eadfg);
        
        getnewVes = new Button("Next", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	eadfg.setAllowBlank(false);
		    	if (eadfg.isValid()) {
		    		getadfgwin.hide();
			    	gmsg.waitStart("Please Wait", "Getting new Vessel info....", "Progress", 300);
		    		timer.timerCancel();
		       	    timer.progReset();
		       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString().toUpperCase());
		       	    for (Iterator il = vlist.iterator(); il.hasNext();) {
		       	    	ArenewVessels inlist = (ArenewVessels) il.next();
		       	    	if (inlist.getId().getAdfg().equalsIgnoreCase(eadfg.getValue())) {
		       	    		found = true;
		       	    		break;
		       	    	} else {
		       	    		found = false;
		       	    	}
		       	    }
		       	    if (found) {
		       	    	found = false;
		       	    	gmsg.waitStop();
		       	    	gmsg.alert("Duplicate Vessel", "Vessel ADFG # " + eadfg.getValue() + " is already in your list.", 350);
		       	    } else {
		       	    	service.getsingleVessel(eadfg.getValue(), ryear, cfecid.getValue().toString().toUpperCase(), new AsyncCallback() {
							public void onFailure(Throwable caught) {
								gmsg.waitStop();
		 					    statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
		 					    gmsg.alert("Communication Error", gins.getTech(), 350);					
							}
							public void onSuccess(Object result) {
								gmsg.waitStop();
								final String adfg = eadfg.getValue();
		 					    newvessel = (ArenewVessels) result;	
		 					    if (newvessel.getVyear().equalsIgnoreCase("9999")) {
		 					    	gmsg.alert("Vessel already licensed", "Vessel ADFG # " + adfg + " is already licensed", 350);
		 					    } else if (newvessel.getVyear().equalsIgnoreCase("0000")) {
		 					    	gmsg.alert("Vessel not found", "Vessel ADFG # " + adfg + " does not exist.", 350);
		 					    } else {    	
		 					    	existingVessel(gmsg, statusBar, adfg, gins, entity, changeList, cfecid, timer, vlist, feeTotals, loader, tr);		    	
		 					    }			    
							}		    		
				    	});
		       	    }		    	
			    }
		    } 
		});        
        q1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        q1.add(getnewVes);
        q1.addText("<br>");        
        pfp.add(q1);        
        pfp.addText("<br>");		
		pfp.addButton(new Button("NO ADF&G number", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
	    		getadfgwin.hide();
		    	gmsg.waitStart("Please Wait", "Getting new Vessel info....", "Progress", 300);
	    		timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString().toUpperCase());
		    	/*
	    		 * 	Code Split point for new Vessel window
	    		 *  This section of code will only load and run IF the new Vessel button is pressed
	    		 */
	    		GWT.runAsync(new RunAsyncCallback() {
					@Override
					public void onFailure(Throwable reason) {
					   gmsg.waitStop();
 					   statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
 					   gmsg.alert("Technical Error", gins.getTech(), 350);		
					}
					@Override
					public void onSuccess() {
						Window newVeswin = getnewvessel(entity, vlist, grid, loader, bottomRight, newves, statusBar, timer, feeTotals, service, gins, changeList, cfecid);
						newVeswin.setClosable(true);
			    		gmsg.waitStop();
			    		newVeswin.show();					
					}		    		
	    		});
		    }
		}));	
		
		pfp.addButton(new Button("Cancel and Return", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	getadfgwin.hide();
		    }
		}));
				
		getadfgwin.add(pfp);
		
		return getadfgwin;
	}
	
	/*
	 * User told us they don't know the adfg number, so they get this new window which ask them a series of questions regarding the
	 * vessel they wish to license.
	 */
	public Window getnewvessel(final ArenewEntity entity, final List<ArenewVessels> vlist, final EditorGrid<BaseModel> grid, final BaseListLoader loader, 
			final FieldSet tr, final Button newves, final HTML statusBar, final SessionTimer timer, final FeeTotals feeTotals, final getDataAsync service, 
			final InstructionsText gins, final List<ArenewChanges> changeList, final TextField cfecid) {
		final UserMessages gmsg = new UserMessages();
		final ContentPanel vesTabs = new ContentPanel();	    	
		vesTabs.setHeaderVisible(false);
		vesTabs.setAutoHeight(true);
		final FormPanel vhist = new FormPanel();
		vhist.setAutoHeight(true);
		vhist.setHeaderVisible(false);
		vhist.addText("<center><b>Section I - Vessel History</b><br>" +
			"<span class='boldblue12'>If this is a foreign vessel, please contact our office @ 907-789-6150.</span><br>" +	
			"Please complete as much as possible in this 'History' screen, then press the 'Next' button below to continue</center><br>");	
		vhist.addStyleName("ftClass");
		final TextField<String> q1t = new TextField<String>();
        q1t.setWidth(75);
		q2t = new TextField<String>();
        q2t.setWidth(100);
        q2t.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
        q2t.setMinLength(5);
        q2t.setMaxLength(5);
        q2t.setMessageTarget("tooltip");
        q2t.setRegex("(\\d{5})");
        q2t.setAllowBlank(true);
        q2t.getMessages().setRegexText("Your ADF&G number is invalid, must be 5 numeric characters");
        final TextField<String> q3t = new TextField<String>();
        q3t.setWidth(200);
        final TextField<String> q4t = new TextField<String>();
        q4t.setWidth(200);
        final TextField<String> q5t = new TextField<String>();
        q5t.setWidth(600);
        final TextField<String> q6t = new TextField<String>();
        q6t.setWidth(75);
        final TextField<String> q7t = new TextField<String>();
        q7t.setWidth(200);
        final TextField<String> q8t = new TextField<String>();
        q8t.setWidth(75);
        final TextField<String> q9t = new TextField<String>();
        q9t.setWidth(300);
        final TextField<String> q10t = new TextField<String>();
        q10t.setWidth(75);
        final TextField<String> q11t = new TextField<String>();
        q11t.setWidth(300);        
        HorizontalPanel q1 = new HorizontalPanel();
        q1.addText("<span class='regblack12'>1.  Is there a triangular metal ADF&G number plate on the vessel?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q1.add(q1t);
        HorizontalPanel q2 = new HorizontalPanel();
        q2.addText("<span class='regblack12'>2.  If Yes, please enter the ADF&G number on the plate.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q2.add(q2t);
        HorizontalPanel q3 = new HorizontalPanel();
        q3.addText("<span class='regblack12'>3.  When did you purchase this vessel?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q3.add(q3t);	
        HorizontalPanel q4 = new HorizontalPanel();
        q4.addText("<span class='regblack12'>4.  From whom?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q4.add(q4t);	
        HorizontalPanel q5 = new HorizontalPanel();
        q5.addText("<span class='regblack12'>5.  List any vessel registration plate numbers issued to the Vessel in other states and indicate which years the Vessel fished in each state.</span>");
        HorizontalPanel q5a = new HorizontalPanel();
        q5a.add(q5t);	
        HorizontalPanel q6 = new HorizontalPanel();
        q6.addText("<span class='regblack12'>6.  Did the previous owner use the vessel for commercial fishing in Alaska?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q6.add(q6t);
        HorizontalPanel q7 = new HorizontalPanel();
        q7.addText("<span class='regblack12'>7.  if yes, please enter the years.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q7.add(q7t);
        HorizontalPanel q8 = new HorizontalPanel();
        q8.addText("<span class='regblack12'>8.  Has the vessel ever had another name?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q8.add(q8t);
        HorizontalPanel q9 = new HorizontalPanel();
        q9.addText("<span class='regblack12'>9.  if yes, please list all names you are aware of.&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q9.add(q9t);	            
        HorizontalPanel q10 = new HorizontalPanel();
        q10.addText("<span class='regblack12'>10.  Has the vessel ever been rebuilt or modified?&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
        q10.add(q10t);
        HorizontalPanel q11 = new HorizontalPanel();
        q11.addText("<span class='regblack12'>11.  if yes, please list all rebuilds you are aware of.</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        q11.add(q11t);	                    
        vhist.add(q1);
        vhist.add(q2);
        vhist.add(q3);
        vhist.add(q4);
        vhist.add(q5);
        vhist.add(q5a);
        vhist.add(q6);
        vhist.add(q7);
        vhist.add(q8);
        vhist.add(q9);
        vhist.add(q10);
        vhist.add(q11);
        vhist.addText("<span class='regred12'><center>*** Please click on the Characteristics tab above to enter " +
        		"actual Vessel data ***</center></font>");
        vhist.addText("<span class='regred12'><center>*** Please note that there is an additional $30.00 plate fee " +
        		"for NEW licensed vessels ***</center></font>");        
        final FormPanel vchar = new FormPanel();
        vchar.setAutoHeight(true);
        vchar.addStyleName("ftClass");
        vchar.setHeaderVisible(false);
		vchar.setLayout(new FormLayout());
		vchar.addText("<center><b>Section II - Vessel Characteristics</b><br>" +
				"<span class='boldblue12'>If this is a foreign vessel, please contact our office @ 907-789-6150.</span><br>" +
				"Please enter all the known information. Information in <span class='regred12'>red</span> is required.<br>" +		
				"If this vessel does not have an ADF&G number you <u>MUST</u> submit copy of your current USGS Documentation or the State registration.</center><br>");	
		final TextField<String> vadfg = new TextField<String>();
		vadfg.setAllowBlank(false);
		vadfg.setWidth(75);
		vadfg.setValue("N/A");
		vadfg.setReadOnly(true);
		vadfg.setEnabled(false);
		final TextField<String> vname = new TextField<String>();
		vname.setWidth(200);
		final TextField<String> vuscg = new TextField<String>();
		vuscg.setAllowBlank(false);
		vuscg.setRegex("[0-9a-zA-Z]{1,10}");
		vuscg.getMessages().setBlankText("The Vessel USCG# is a required field");
		vuscg.getMessages().setRegexText("The Vessel USCG# cannot exceed 10 characters. If you are not sure of this value, please leave it blank.");
		vuscg.setWidth(75);
		final TextField<String> vbuilt = new TextField<String>();
		vbuilt.setAllowBlank(false);
		vbuilt.setRegex("(\\d{4})");
		vbuilt.getMessages().setBlankText("The Vessel Year Built is a required field");
		vbuilt.getMessages().setRegexText("The Vessel Year Built field can only be 4 numeric characters (i.e., 1999)");
		vbuilt.setWidth(65);
		final TextField<String> vmake = new TextField<String>();
		vmake.setAllowBlank(false);
		vmake.getMessages().setBlankText("The Vessel Make/Model is a required field");
		vmake.setWidth(125);
		final TextField<String> vlenfeet = new TextField<String>();
		vlenfeet.setAllowBlank(false);
		vlenfeet.setRegex("(\\d{1,3})");
		vlenfeet.getMessages().setBlankText("The Vessel Length (feet) is a required field");
		vlenfeet.getMessages().setRegexText("The Vessel Length (feet) must be 1-3 numeric characters only");
		vlenfeet.setWidth(50);
		final TextField<String> vleninches = new TextField<String>();
		vleninches.setAllowBlank(false);
		vleninches.setRegex("(\\d{1,2})");
		vleninches.getMessages().setBlankText("The Vessel Length (inches) is a required field");
		vleninches.getMessages().setRegexText("The Vessel Length (inches) must be 1-2 numeric characters only");
		vleninches.setWidth(50);
		final TextField<String> vgton = new TextField<String>();
		vgton.setWidth(50);
		final TextField<String> vnton = new TextField<String>();
		vnton.setWidth(50);
		final TextField<String> vhpc = new TextField<String>();
		vhpc.setWidth(100);
		final TextField<String> vhps = new TextField<String>();
		vhps.setWidth(50);
		final TextField<String> vengine = new TextField<String>();
		vengine.setWidth(65);
		final TextField<String> vhp = new TextField<String>();
		vhp.setWidth(60);
		final TextField<String> vvalue = new TextField<String>();
		vvalue.setWidth(65);
		final SimpleComboBox vhtype = new SimpleComboBox();
		vhtype.setWidth(100);
		vhtype.setForceSelection(true);
		vhtype.removeAll();
		vhtype.add("Fiberglass");
		vhtype.add("Wood");
		vhtype.add("Steel/Alloy");
		vhtype.add("Concrete");
		vhtype.add("Aluminum");
		vhtype.add("Rubber");
		//vhtype.setSimpleValue("Fiberglass");
		vhtype.setAllowBlank(false);
		vhtype.getMessages().setBlankText("The Vessel Hull Type is a required field");
		vhtype.setTriggerAction(TriggerAction.ALL);
		final TextField<String> vhid = new TextField<String>();
		vhid.setAllowBlank(false);
		vhid.getMessages().setBlankText("The Vessel Hull ID is a required field");
		vhid.setWidth(100);
		final TextField<String> vfuel = new TextField<String>();
		vfuel.setWidth(45);
		final TextField<String> vref = new TextField<String>();
		vref.setWidth(40);
		final TextField<String> vlive = new TextField<String>();
		vlive.setWidth(45);
		final TextField<String> vhold = new TextField<String>();
		vhold.setWidth(45);
		final CheckBox strhand = new CheckBox();  
        strhand.setBoxLabel("Hand");  
        final CheckBox strpower = new CheckBox();  
        strpower.setBoxLabel("Power");
        strhand.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent fe) {
                strpower.setValue(false);
            }
        });
        strpower.addListener(Events.OnClick, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent fe) {
                strhand.setValue(false);
            }
        });
        final DateField vstrdate = new DateField();
        vstrdate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
        Date today = new Date();
        vstrdate.setMinValue(today);
        q2t.setWidth(100);
        final CheckBox ps = new CheckBox();
        final CheckBox bs = new CheckBox();
        final CheckBox dgn = new CheckBox();
        final CheckBox sgn = new CheckBox();
        final CheckBox ht = new CheckBox();
        final CheckBox ll = new CheckBox();
        final CheckBox fw = new CheckBox();	            
        final CheckBox sot = new CheckBox();
        final CheckBox p = new CheckBox();
        final CheckBox rn = new CheckBox();
        final CheckBox d = new CheckBox();
        final CheckBox pt = new CheckBox();
        final CheckBox bt = new CheckBox();
        final CheckBox sd = new CheckBox();	            
        final CheckBox db = new CheckBox();
        final CheckBox mj = new CheckBox();
        final CheckBox dot = new CheckBox();
        final CheckBox hg = new CheckBox();
        final CheckBox ptr = new CheckBox();
        final CheckBox og = new CheckBox();
        final CheckBox cf = new CheckBox();
        final CheckBox pr = new CheckBox();
        final CheckBox tp = new CheckBox();
        final CheckBox ta = new CheckBox();
        final SimpleComboBox snra1 = new SimpleComboBox();
        snra1.setWidth(200);
        snra1.setForceSelection(true);
        snra1.removeAll();
        snra1.setEditable(false);
        snra1.add("None");
        snra1.add("Southeast (A)");
        snra1.add("Prince William Sound (E)");
        snra1.add("Cook Inlet (H)");
        snra1.add("Kodiak (K)");
        snra1.add("Chignik (L)");
        snra1.add("AK Peninsula/Aleutians (M)");
        snra1.add("Bristol Bay (T)");
        snra1.setTriggerAction(TriggerAction.ALL);
        final TextField<String> snrp1 = new TextField<String>();
        snrp1.setWidth(75);
        snrp1.setAllowBlank(true);
		snrp1.setMessageTarget("tooltip");
		snrp1.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
		snrp1.setMinLength(5);
		snrp1.setMaxLength(5);
		snrp1.setRegex("(\\d{5})");
		snrp1.getMessages().setRegexText("This permit serial number is invalid, must be 5 numeric characters");
        final TextField<String> snrp2 = new TextField<String>();
        snrp2.setWidth(75);
        snrp2.setAllowBlank(true);
		snrp2.setMessageTarget("tooltip");
		snrp2.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
		snrp2.setMinLength(5);
		snrp2.setMaxLength(5);
		snrp2.setRegex("(\\d{5})");
		snrp2.getMessages().setRegexText("This permit serial number is invalid, must be 5 numeric characters");
		snra1.addListener(Events.Select, new Listener<FieldEvent>() {
			public void handleEvent(FieldEvent be) {
				if (snra1.getSimpleValue().toString().equalsIgnoreCase("None")) {
					snrp1.clear();
					snrp2.clear();
				}
			}        	
        });
		HorizontalPanel vcs1 = new HorizontalPanel();
		vcs1.setHorizontalAlign(HorizontalAlignment.CENTER);
		vcs1.addText("<center><span class='boldblack12'>Vessel Specifications</span></center>");
		HorizontalPanel vc1 = new HorizontalPanel();
		vc1.addText("<span class='regblack12'>Vessel ADF&G #:</span>&nbsp;&nbsp;&nbsp;");
		vc1.add(vadfg);
		vc1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Vessel Name:</span>&nbsp;&nbsp;&nbsp;");
		vc1.add(vname);
		vc1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regred12'>USCG #:</span>&nbsp;&nbsp;&nbsp;");
		vc1.add(vuscg);
		HorizontalPanel vc2 = new HorizontalPanel();
		vc2.addText("<span class='regred12'>Built:</span>&nbsp;&nbsp;&nbsp;");
		vc2.add(vbuilt);
		vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regred12'>Make/Model:</span>&nbsp;&nbsp;&nbsp;");
		vc2.add(vmake);
		vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regred12'>Length (feet):</span>&nbsp;&nbsp;&nbsp;");
		vc2.add(vlenfeet);
		vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regred12'>Length (inches):</span>:&nbsp;&nbsp;&nbsp;");
		vc2.add(vleninches);
		HorizontalPanel vc3 = new HorizontalPanel();
		vc3.addText("<span class='regblack12'>Gross Tons:</span>&nbsp;&nbsp;&nbsp;");
		vc3.add(vgton);
		vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Net Tons:</span>&nbsp;&nbsp;&nbsp;");
		vc3.add(vnton);
		vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Homeport City:</span>&nbsp;&nbsp;&nbsp;");
		vc3.add(vhpc);
		vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>State:</span>&nbsp;&nbsp;&nbsp;");
		vc3.add(vhps);
		HorizontalPanel vc4 = new HorizontalPanel();
		vc4.addText("<span class='regblack12'>Engine (D or G):</span>&nbsp;&nbsp;&nbsp;");
		vc4.add(vengine);
		vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Horsepower:</span>&nbsp;&nbsp;&nbsp;");
		vc4.add(vhp);
		vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Est. Value:</span>&nbsp;&nbsp;&nbsp;");
		vc4.add(vvalue);
		vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regred12'>Hull Type:</span>&nbsp;&nbsp;&nbsp;");
		vc4.add(vhtype);
		HorizontalPanel vc5 = new HorizontalPanel();
		vc5.addText("<span class='regred12'>Hull ID:</span>&nbsp;&nbsp;&nbsp;");
		vc5.add(vhid);
		vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Fuel Capacity:</span>&nbsp;&nbsp;&nbsp;");
		vc5.add(vfuel);
		vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Refrig (Y/N):</span>&nbsp;&nbsp;&nbsp;");
		vc5.add(vref);
		vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Live:</span>&nbsp;&nbsp;&nbsp;");
		vc5.add(vlive);
		vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Hold:</span>&nbsp;&nbsp;&nbsp;");
		vc5.add(vhold);
		HorizontalPanel vcs2 = new HorizontalPanel();
		vcs2.setHorizontalAlign(HorizontalAlignment.CENTER);
		vcs2.addText("<br><center><span class='boldblack12'>Salmon Troll Registration</span></center>");
		HorizontalPanel vc6 = new HorizontalPanel();
		vc6.add(strhand);
		vc6.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		vc6.add(strpower);
		vc6.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Effective Date: (MMDDYYYY)</span>&nbsp;&nbsp;&nbsp;");
		vc6.add(vstrdate);	    		
		HorizontalPanel vcs3 = new HorizontalPanel();
		vcs3.setHorizontalAlign(HorizontalAlignment.CENTER);
		vcs3.addText("<br><center><span class='boldblack12'>Types of Gear Fished</span></center>");
		HorizontalPanel vc7 = new HorizontalPanel();
		vc7.add(ps);
		vc7.addText("&nbsp;<span class='regblack12'>Purse Seine</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(bs);
		vc7.addText("&nbsp;<span class='regblack12'>Beach Seine</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(dgn);
		vc7.addText("&nbsp;<span class='regblack12'>Drift Gill Net</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(sgn);
		vc7.addText("&nbsp;<span class='regblack12'>Set Gill Net</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(ht);
		vc7.addText("&nbsp;<span class='regblack12'>Hand Troll</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(ll);
		vc7.addText("&nbsp;<span class='regblack12'>Long Line</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc7.add(fw);
		vc7.addText("&nbsp;<span class='regblack12'>Fish Wheel</span>");
		HorizontalPanel vc8 = new HorizontalPanel();
		vc8.add(sot);
		vc8.addText("&nbsp;<span class='regblack12'>SingleOtter Trawl</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(p);
		vc8.addText("&nbsp;<span class='regblack12'>Pots</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(rn);
		vc8.addText("&nbsp;<span class='regblack12'>Ring Net</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(d);
		vc8.addText("&nbsp;<span class='regblack12'>Diving</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(pt);
		vc8.addText("&nbsp;<span class='regblack12'>Power Troll</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(bt);
		vc8.addText("&nbsp;<span class='regblack12'>Beam Trawl</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc8.add(sd);
		vc8.addText("&nbsp;<span class='regblack12'>Scallop Drg</span>");
		HorizontalPanel vc9 = new HorizontalPanel();
		vc9.add(db);
		vc9.addText("&nbsp;<span class='regblack12'>Dinglebar</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc9.add(mj);
		vc9.addText("&nbsp;<span class='regblack12'>Mechanical Jig</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc9.add(dot);
		vc9.addText("&nbsp;<span class='regblack12'>Double Otter Trawl</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc9.add(hg);
		vc9.addText("&nbsp;<span class='regblack12'>Herring Gillnet</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc9.add(ptr);
		vc9.addText("&nbsp;<span class='regblack12'>Pair Trawl</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc9.add(og);
		vc9.addText("&nbsp;<span class='regblack12'>Other Gear</span>");	    		
		HorizontalPanel vcs4 = new HorizontalPanel();
		vcs4.setHorizontalAlign(HorizontalAlignment.CENTER);
		vcs4.addText("<br><center><span class='boldblack12'>Vessel Activities</span></center>");
		HorizontalPanel vc10 = new HorizontalPanel();
		vc10.add(cf);
		vc10.addText("&nbsp;<span class='regblack12'>Commercial Fishing</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc10.add(pr);
		vc10.addText("&nbsp;<span class='regblack12'>Processor</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc10.add(tp);
		vc10.addText("&nbsp;<span class='regblack12'>Tender/Packer</span>&nbsp;&nbsp;&nbsp;&nbsp;");
		vc10.add(ta);
		vc10.addText("&nbsp;<span class='regblack12'>Transporter</span>");		
		HorizontalPanel vcs5 = new HorizontalPanel();
		vcs5.setHorizontalAlign(HorizontalAlignment.CENTER);
		vcs5.addText("<br><center><span class='boldblack12'>Salmon Net Registration</span></center>");
		HorizontalPanel vc11 = new HorizontalPanel();
		vc11.addText("<span class='regblack12'>Area:</span>&nbsp;&nbsp;&nbsp;");
		vc11.add(snra1);
		vc11.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Permit 1:</span>&nbsp;&nbsp;&nbsp;");
		vc11.add(snrp1);
		vc11.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class='regblack12'>Permit 2:</span>&nbsp;&nbsp;&nbsp;");
		vc11.add(snrp2);
		
		vchar.add(vcs1);
        vchar.add(vc1);
        vchar.add(vc2);
        vchar.add(vc3);
        vchar.add(vc4);
        vchar.add(vc5);
        vchar.add(vcs2);
        vchar.add(vc6);
        vchar.add(vcs3);
        vchar.add(vc7);
        vchar.add(vc8);
        vchar.add(vc9);
        vchar.add(vcs4);
        vchar.add(vc10);
        vchar.add(vcs5);
        vchar.add(vc11);
        
		vesTabs.add(vhist);
		
		final FormPanel pfp = new FormPanel();
		pfp.setHeaderVisible(false);
		pfp.setButtonAlign(HorizontalAlignment.CENTER);
		
		final Window newVeswin = new Window();
		newVeswin.setSize(900, 600);
		newVeswin.setHeadingHtml("License New Vessel");
		newVeswin.setLayout(new FlowLayout());  
        newVeswin.setScrollMode(Scroll.AUTO);
		newVeswin.setFrame(true);
		newVeswin.setBorders(true);
		newVeswin.setIconStyle("icon-table");  
		newVeswin.setClosable(false);
		newVeswin.setButtonAlign(HorizontalAlignment.CENTER);
		newVeswin.setStyleName("newPmtwin");
					
		final Button sar = new Button("Save and Return", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString().toUpperCase());
	       	    if (strhand.getValue() && (!(ht.getValue()))) {
					gmsg.alert("<span class='regred12'>Illegal selection</span>", "You have a Salmon Troll type of HAND TROLL," +
							" you must also select the corresponding Gear Type.", 300);
				} else if (strpower.getValue() && (!(pt.getValue()))) {
					gmsg.alert("<span class='regred12'>Illegal selection</span>", "You have a Salmon Troll type of POWER TROLL," +
							" you must also select the corresponding Gear Type.", 300);
				} else {
					if (vuscg.isValid() && vbuilt.isValid() && vmake.isValid() && vlenfeet.isValid() && vleninches.isValid() && vhtype.isValid() && snrp1.isValid() && snrp2.isValid()) {
		    			newVeswin.hide();
			    		ArenewVessels nves = new ArenewVessels();
			    		ArenewVesselsId nvesId = new ArenewVesselsId();
			    		String regtype = null;
			    		regtype = "N";
		    			nves.setFoerignFlag(regtype);
		    			nves.setQ1(q1t.getValue());
			    		nves.setQ2(q2t.getValue());
			    		nves.setQ3(q3t.getValue());
			    		nves.setQ4(q4t.getValue());
			    		nves.setQ5(q5t.getValue());
			    		nves.setQ6(q6t.getValue());
			    		nves.setQ7(q7t.getValue());
			    		nves.setQ8(q8t.getValue());
			    		nves.setQ9(q9t.getValue());
			    		nves.setQ10(q10t.getValue());
			    		nves.setQ11(q11t.getValue());
			    		nvesId.setRyear(entity.getId().getRyear());
		    			nvesId.setAdfg("N/A");
		    			nves.setDetails("<span class='regred12'>If this vessel is new there may be an additional $30.00 plate fee</span>");
			    		if (!(vname.getValue() == null)) {
			    			nves.setName(vname.getValue());
			    		} else {
			    			nves.setName("N/A");
			    		}
			    		if (!(vuscg.getValue() == null)) {
			    			nves.setRegNum(vuscg.getValue());
			    		} else {
			    			nves.setRegNum("N/A");
			    		}     	
			    		nves.setVyear(vbuilt.getValue());
			    		nves.setYearBuilt(vbuilt.getValue());
			    		nves.setMakeModel(vmake.getValue().toUpperCase());
			    		nves.setLengthFeet(vlenfeet.getValue());
			    		nves.setLengthInches(vleninches.getValue());
			    		nves.setGrossTons(vgton.getValue());
			    		nves.setNetTons(vnton.getValue());
			    		if (!(vhpc.getValue() == null)) {
			    			nves.setHomeportCity(vhpc.getValue());
			    		} else {
			    			nves.setHomeportCity("N/A");
			    		}
			    		if (!(vhps.getValue() == null)) {
			    			nves.setHomeportState(vhps.getValue());
			    		} else {
			    			nves.setHomeportState("N/A");
			    		}
			    		nves.setEngineType(vengine.getValue());
			    		nves.setHorsepower(vhp.getValue());
			    		nves.setEstValue(vvalue.getValue());
			    		String htype = null;
			    		if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Fiberglass")) {
			    			htype = "F";
			    		} else if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Wood")) {
			    			htype = "W";
			    		} else if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Steel/Alloy")) {
			    			htype = "S";
			    		} else if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Concrete")) {
			    			htype = "C";
			    		} else if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Aluminum")) {
			    			htype = "A";
			    		} else if (vhtype.getSimpleValue().toString().equalsIgnoreCase("Rubber")) {
			    			htype = "R";
			    		}
			    		nves.setHullType(htype);
			    		if (!(vhid.getValue() == null)) {
			    			nves.setHullId(vhid.getValue());
			    		} else {
			    			nves.setHullId("N/A");
			    		}			    		
			    		nves.setFuel(vfuel.getValue());
			    		nves.setRefrigeration(vref.getValue());
			    		nves.setLiveTank(vlive.getValue());
			    		nves.setHoldTank(vhold.getValue());
			    		if (strhand.getValue() || strpower.getValue()) {
			    			String regarea = null;
			    			if (strhand.getValue()) {
			    				regarea = "H";
			    			} else if (strpower.getValue()) {
			    				regarea = "P";
			    			} else {
			    				regarea = "N/A";
			    			}
			    			nves.setSalmontrollReg(regarea);
			    		}
			    		if (vstrdate.getValue() != null) {
			    			nves.setSalmontrollDate(DateTimeFormat.getFormat("yyyy-MM-dd").format(vstrdate.getValue()));
			    		}
			    		if (ps.getValue()) { nves.setPurseseine("X"); } else { nves.setPurseseine("N"); }
			    		if (bs.getValue()) { nves.setBeachseine("X"); } else { nves.setBeachseine("N"); }
			    		if (dgn.getValue()) { nves.setDriftgillnet("X"); } else { nves.setDriftgillnet("N"); }
			    		if (sgn.getValue()) { nves.setSetgillnet("X"); } else { nves.setSetgillnet("N"); }
			    		if (ht.getValue()) { nves.setHandtroll("X"); } else { nves.setHandtroll("N"); }
			    		if (ll.getValue()) { nves.setLongline("X"); } else { nves.setLongline("N"); }
			    		if (fw.getValue()) { nves.setFishwheel("X"); } else { nves.setFishwheel("N"); }     			    		
			    		if (sot.getValue()) { nves.setSingleottertrawl("X"); } else { nves.setSingleottertrawl("N"); }
			    		if (p.getValue()) { nves.setPotgear("X"); } else { nves.setPotgear("N"); }
			    		if (rn.getValue()) { nves.setRingnet("X"); } else { nves.setRingnet("N"); }
			    		if (d.getValue()) { nves.setDivegear("X"); } else { nves.setDivegear("N"); }
			    		if (pt.getValue()) { nves.setPowertroll("X"); } else { nves.setPowertroll("N"); }
			    		if (bt.getValue()) { nves.setBeamtrawl("X"); } else { nves.setBeamtrawl("N"); }
			    		if (sd.getValue()) { nves.setDredge("X"); } else { nves.setDredge("N"); }     			    		
			    		if (db.getValue()) { nves.setDinglebar("X"); } else { nves.setDinglebar("N"); }
			    		if (mj.getValue()) { nves.setJig("X"); } else { nves.setJig("N"); }
			    		if (dot.getValue()) { nves.setDoubleottertrawl("X"); } else { nves.setDoubleottertrawl("N"); }
			    		if (hg.getValue()) { nves.setHearinggillnet("X"); } else { nves.setHearinggillnet("N"); }
			    		if (ptr.getValue()) { nves.setPairtrawl("X"); } else { nves.setPairtrawl("N"); }
			    		if (og.getValue()) { nves.setOthergear("X"); } else { nves.setOthergear("N"); }     			    		
			    		if (cf.getValue()) { nves.setFishingboat("X"); } else { nves.setFishingboat("N"); }
			    		if (pr.getValue()) { nves.setFreezerCanner("X"); } else { nves.setFreezerCanner("N"); }
			    		if (tp.getValue()) { nves.setTenderPacker("X"); } else { nves.setTenderPacker("N"); }
			    		if (ta.getValue()) { nves.setTransporter("X"); } else { nves.setTransporter("N"); }				    		
			    		if (!(snra1.getValue() == null)) {
			    			if (snra1.getSimpleValue().toString().equalsIgnoreCase("None")) {
			    				areacode = "N/A";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Southeast (A)")) {
			    				areacode = "A";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Prince William Sound (E)")) {
			    				areacode = "E";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Cook Inlet (H)")) {
			    				areacode = "H";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Kodiak (K)")) {
			    				areacode = "K";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Chignik (L)")) {
			    				areacode = "L";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("AK Peninsula/Aleutians (M)")) {
			    				areacode = "M";
			    			} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Bristol Bay (T)")) {
			    				areacode = "T";
			    			}
			    			nves.setSalmonregArea(areacode);
			    		}	    		
			    		nves.setPermitSerial1(snrp1.getValue());
			    		nves.setPermitSerial2(snrp2.getValue());
			    		nvesId.setCfecid(entity.getId().getCfecid().toUpperCase());
			    		nves.setStatus("Available");
			    		nves.setNewVessel(true);
			    		int vl = Integer.parseInt(vlenfeet.getValue());
			    		double vfee = 0.0;
			    		if (vl <= 25 ) {
			    			vfee = 24.00;
	 			   		} else if (vl <= 50) {
	 			   			vfee = 60.00;									
	 			   		} else if (vl <= 75) {
	 			   			vfee = 120.00;									
	 			   		} else if (vl <= 100) {
	 			   			vfee = 225.00;									
	 			   		} else if (vl <= 125) {
	 			   			vfee = 300.00;									
	 			   		} else if (vl <= 150) {
	 			   			vfee = 375.00;									
	 			   		} else if (vl <= 175) {
	 			   			vfee = 450.00;									
	 			   		} else if (vl <= 200) {
	 			   			vfee = 525.00;									
	 			   		} else if (vl <= 225) {
	 			   			vfee = 600.00;									
	 			   		} else if (vl <= 250) {
	 			   			vfee = 675.00;									
	 			   		} else if (vl <= 275) {
	 			   			vfee = 750.00;									
	 			   		} else if (vl <= 300) {
	 			   			vfee = 825.00;									
	 			   		} else {
	 			   			vfee = 900.00;									
	 			   		}
			    		if (nvesId.getAdfg().equalsIgnoreCase("N/A")) {
			    			vfee = vfee + 30.00;
			    		}
			    		nves.setFee(Double.toString(vfee));
			    		nves.setId(nvesId);
			    		vlist.add(nves);
			    		getVes.newVessel(feeTotals, entity, nves);
			    		loader.load();
			    		Log.info(entity.getId().getCfecid().toUpperCase() + " has added a new Vessel (ADFG "+nvesId.getAdfg()+", Name "+nves.getName()+") to their profile");
			    		DOM.getElementById("vesselsRenewalble").setInnerText(Integer.toString(vlist.size()));
						tr.layout();
			    	} else if (snrp1.isValid() && snrp2.isValid()) {
			    		gmsg.alert("Requirements not met", "You must supply CFEC with at least a USCG/State reg number, year built, make/model, length(feet and inches), hull type and hull ID in the Characterists tab before saving a new vessel.", 300);
			    	} else if ( !(snrp1.isValid()) || !(snrp2.isValid()) ) {
			    		gmsg.alert("Requirements not met", "If you are entering permit serial number(s) for Salmon Net Registration, they must be a 5 character numeric value only.", 300);
			    	} else {
			    		gmsg.alert("Requirements not met", "You must supply CFEC with at least a year built, make/model, length(feet and inches), hull type and hull ID in the Characterists tab before saving a new vessel.", 300);
			    	}
				}	    	
		    }     			    
		});
		
		final Button car = new Button("Cancel and Return", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), entity.getId().getCfecid().toString().toUpperCase());
		    	newVeswin.hide();
		    }
		});
		
		Button next = new Button("Next", new SelectionListener<ButtonEvent>() {
		    public void componentSelected(ButtonEvent ce) {
		    	if (!(q2t.getValue() == null)) {
					if (q2t.isValid()) {
						newVeswin.hide();
				    	gmsg.waitStart("Please Wait", "Looking for vessel info....", "Progress", 300);
			    		timer.timerCancel();
			       	    timer.progReset();
			       	    timer.setTimer(timer.getTime(), entity.getId().getCfecid().toString().toUpperCase());
			       	    for (Iterator il = vlist.iterator(); il.hasNext();) {
			       	    	ArenewVessels inlist = (ArenewVessels) il.next();
			       	    	if (inlist.getId().getAdfg().equalsIgnoreCase(q2t.getValue())) {
			       	    		found = true;
			       	    		break;
			       	    	} else {
			       	    		found = false;
			       	    	}
			       	    }
			       	    if (found) {
			       	    	found = false;
			       	    	gmsg.waitStop();
			       	    	gmsg.alert("Duplicate Vessel", "Vessel ADFG # " + q2t.getValue() + " is already in your list.", 350);
			       	    } else {
			       	    	service.getsingleVessel(q2t.getValue(), entity.getId().getRyear(), entity.getId().getCfecid().toString().toUpperCase(), new AsyncCallback() {
								public void onFailure(Throwable caught) {
									gmsg.waitStop();
	    	 					    statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
	    	 					    gmsg.alert("Communication Error", gins.getTech(), 350);						
								}
								public void onSuccess(Object result) {
									gmsg.waitStop();
	    							final String adfg = q2t.getValue();
	    	 					    newvessel = (ArenewVessels) result;	
	    	 					    if (newvessel.getVyear().equalsIgnoreCase("9999")) {
	    	 					    	gmsg.alert("Vessel already licensed", "Vessel ADFG # " + adfg + " is already licensed", 350);
	    	 					    } else if (newvessel.getVyear().equalsIgnoreCase("0000")) {
	    	 					    	gmsg.alert("Vessel not found", "Vessel ADFG # " + adfg + " does not exist.", 350);
	    	 					    } else {    	
	    	 					    	existingVessel(gmsg, statusBar, adfg, gins, entity, changeList, cfecid, timer, vlist, feeTotals, loader, tr);
	    	 					    }
								}				    		
					    	});
			       	    }			    	
					} else {
						/*
						 * Do nothing
						 */
					}
				} else {
					vesTabs.removeAll();
					pfp.removeAll();		
					newVeswin.removeAll();
					vesTabs.add(vchar);
					pfp.add(vesTabs);
					nvesbb = new ButtonBar();
					nvesbb.setAlignment(HorizontalAlignment.CENTER);
					nvesbb.add(sar);
					nvesbb.add(car);
					pfp.add(nvesbb);
					newVeswin.add(pfp);
					vesTabs.layout();
					pfp.layout();
					newVeswin.layout();
				}
		    }
		});	
		
		pfp.add(vesTabs);	
		
		nvesbb = new ButtonBar();
		nvesbb.setAlignment(HorizontalAlignment.CENTER);
		nvesbb.add(next);
		nvesbb.add(car);
		
		pfp.add(nvesbb);
		
		newVeswin.add(pfp);
		
		return newVeswin;
	}
	
	public void existingVessel(final UserMessages gmsg, final HTML statusBar, final String adfg, final InstructionsText gins, final ArenewEntity entity, 
			final List<ArenewChanges> changeList, final TextField cfecid, final SessionTimer timer, final List<ArenewVessels> vlist, final FeeTotals feeTotals, 
			final BaseListLoader loader, final FieldSet tr) {
		gmsg.waitStart("Please Wait", "Getting Vessel info....", "Progress", 300);
		/*
		 * Code Split point for selected Vessel
		 * modification This section will only load
		 * and run IF the cell is clicked
		 */
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onFailure(Throwable reason) {
				gmsg.waitStop();
				statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
				gmsg.alert("Technical Error", gins.getTech(), 350);
			}
			@Override
			public void onSuccess() {
				gmsg.waitStop();
				if (editVeswin == null) {
					editVeswin = new Window();
					editVeswin.setSize(800, 585);
					editVeswin.setHeadingHtml("<font color='red'>If this is the Vessel you wish to add, make necessary changes and press 'Save and Return', otherwise 'Cancel and Return'</font>");
					editVeswin.setLayout(new FitLayout());
					editVeswin.setScrollMode(Scroll.AUTO);
					editVeswin.setFrame(true);
					editVeswin.setBorders(true);
					editVeswin.setIconStyle("icon-table");
					editVeswin.setClosable(true);
					editVeswin.setButtonAlign(HorizontalAlignment.CENTER);
					editVeswin.addStyleName("newPmtwin");
					ContentPanel cchar = new ContentPanel();
					cchar.setHeaderVisible(false);
					cchar.setBorders(false);
					cchar.setBodyBorder(false);
					final FormPanel vchar = new FormPanel();
					vchar.setHeaderVisible(false);
					vchar.setBorders(false);
					vchar.setLayout(new FormLayout());
					vchar.addText("<center><b>Vessel Characteristics for ADFG " + adfg + "</b><br>The fields that are NOT allowed to be modified are automatically disabled.</center><br>");

					TextField<String> vadfg = new TextField<String>();
					vadfg.setWidth(75);
					vadfg.setValue(newvessel.getId().getAdfg());
					vadfg.setEnabled(false);
					final TextField<String> vname = new TextField<String>();
					vname.setWidth(200);
					vname.setAllowBlank(false);
					vname.setValue(newvessel.getName());
					vname.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vname.isValid()) {
								cr.getVesselChanges1(entity, "vesselName", entity.getId().getCfecid().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					TextField<String> vuscg = new TextField<String>();
					vuscg.setWidth(75);
					vuscg.setValue(newvessel.getRegNum());
					vuscg.setEnabled(false);
					TextField<String> vbuilt = new TextField<String>();
					vbuilt.setWidth(75);
					vbuilt.setValue(newvessel.getYearBuilt());
					vbuilt.setEnabled(false);
					TextField<String> vmake = new TextField<String>();
					vmake.setWidth(150);
					vmake.setValue(newvessel.getMakeModel());
					vmake.setEnabled(false);
					TextField<String> vlen = new TextField<String>();
					vlen.setWidth(75);
					vlen.setValue(newvessel.getLengthFeet() + "' " + newvessel.getLengthInches());
					vlen.setEnabled(false);
					TextField<String> vfee = new TextField<String>();
					vfee.setWidth(75);
					String sfee = (newvessel.getFee());
					vfee.setValue(sfee);
					vfee.setEnabled(false);
					final TextField<String> vgton = new TextField<String>();
					vgton.setWidth(50);
					vgton.setValue(newvessel.getGrossTons());
					vgton.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vgton.isValid()) {
								cr.getVesselChanges1(entity, "grossTons", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vnton = new TextField<String>();
					vnton.setWidth(50);
					vnton.setValue(newvessel.getNetTons());
					vnton.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vnton.isValid()) {
								cr.getVesselChanges1(entity, "netTons", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vhpc = new TextField<String>();
					vhpc.setWidth(100);
					vhpc.setAllowBlank(false);
					vhpc.setValue(newvessel.getHomeportCity());
					vhpc.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vhpc.isValid()) {
								cr.getVesselChanges1(entity, "homeportCity", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vhps = new TextField<String>();
					vhps.setWidth(50);
					vhps.setAllowBlank(false);
					vhps.setValue(newvessel.getHomeportState());
					vhps.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vhps.isValid()) {
								cr.getVesselChanges1(entity, "homeportState", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vengine = new TextField<String>();
					vengine.setWidth(65);
					vengine.setAllowBlank(false);
					vengine.setValue(newvessel.getEngineType());
					vengine.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vengine.isValid()) {
								cr.getVesselChanges1(entity, "engineType", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vhp = new TextField<String>();
					vhp.setWidth(60);
					vhp.setValue(newvessel.getHorsepower());
					vhp.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vhp.isValid()) {
								cr.getVesselChanges1(entity, "horsePower", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vvalue = new TextField<String>();
					vvalue.setWidth(65);
					vvalue.setValue(newvessel.getEstValue());
					vvalue.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vvalue.isValid()) {
								cr.getVesselChanges1(entity, "estValue", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					TextField<String> vhtype = new TextField<String>();
					vhtype.setWidth(100);
					vhtype.setValue(newvessel.getHullType());
					vhtype.setEnabled(false);
					TextField<String> vhid = new TextField<String>();
					vhid.setWidth(100);
					vhid.setValue(newvessel.getHullId());
					vhid.setEnabled(false);
					final TextField<String> vfuel = new TextField<String>();
					vfuel.setWidth(45);
					vfuel.setValue(newvessel.getFuel());
					vfuel.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vfuel.isValid()) {
								cr.getVesselChanges1(entity, "fuelCapacity", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vref = new TextField<String>();
					vref.setWidth(40);
					vref.setValue(newvessel.getRefrigeration());
					vref.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vref.isValid()) {
								cr.getVesselChanges1(entity, "refrigeration", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vlive = new TextField<String>();
					vlive.setWidth(45);
					vlive.setValue(newvessel.getLiveTank());
					vlive.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vlive.isValid()) {
								cr.getVesselChanges1(entity, "liveCapacity", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final TextField<String> vhold = new TextField<String>();
					vhold.setWidth(45);
					vhold.setValue(newvessel.getHoldTank());
					vhold.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vhold.isValid()) {
								cr.getVesselChanges1(entity, "holdCapacity", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final Radio strhand = new Radio();
					strhand.setBoxLabel("Hand");
					final Radio strpower = new Radio();
					strpower.setBoxLabel("Power");
					final RadioGroup vstr = new RadioGroup();
					vstr.add(strhand);
					vstr.add(strpower);					
					if (!(newvessel.getSalmontrollReg() == null)) {
						if (newvessel.getSalmontrollReg().equalsIgnoreCase("p")) {
							strpower.setValue(true);
						} else {
							strpower.setValue(false);
						}
						if (newvessel.getSalmontrollReg().equalsIgnoreCase("h")) {
							strhand.setValue(true);
						} else {
							strhand.setValue(false);
						}
					}	
					strhand.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges4(entity, "salmonTrollReg", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg, "Hand");					
						}						
					});
					strpower.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges4(entity, "salmonTrollReg", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg, "Power");					
						}						
					});				
					final DateField vstrdate = new DateField();
					vstrdate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
					Date today = new Date();
					vstrdate.setMinValue(today);
					DateTimeFormat dtformat = DateTimeFormat.getFormat("yyyy-MM-dd");
					if (!(newvessel.getSalmontrollDate() == null)) {
						//System.out.println(newvessel.getSalmontrollDate());
						if (dtformat.parse(newvessel.getSalmontrollDate()).before(today)) {
							cr.getVesselChanges3(entity, "salmonTrollDate", cfecid.getValue().toString().toUpperCase(), dtformat.parse(newvessel.getSalmontrollDate()), today, changeList, adfg);
							vstrdate.setValue(today);
						} else {
							vstrdate.setValue(dtformat.parse(newvessel.getSalmontrollDate()));
						}    												
					}
					vstrdate.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							if (vstrdate.isValid()) { 
								cr.getVesselChanges1(entity, "salmonTrollDate", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
							}
						}
					});
					final CheckBox ps = new CheckBox();
					if (newvessel.getPurseseine().equalsIgnoreCase("x")) {
						ps.setValue(true);
					} else {
						ps.setValue(false);
					}
					ps.addListener(Events.OnClick, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "purseSeine", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox bs = new CheckBox();
					if (newvessel.getBeachseine().equalsIgnoreCase("x")) {
						bs.setValue(true);
					} else {
						bs.setValue(false);
					}
					bs.addListener(Events.OnClick, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "beachSeine", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox dgn = new CheckBox();
					if (newvessel.getDriftgillnet().equalsIgnoreCase("x")) {
						dgn.setValue(true);
					} else {
						dgn.setValue(false);
					}
					dgn.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "driftGillNet", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox sgn = new CheckBox();
					if (newvessel.getSetgillnet().equalsIgnoreCase("x")) {
						sgn.setValue(true);
					} else {
						sgn.setValue(false);
					}
					sgn.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "setGillNet", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox ht = new CheckBox();
					if (newvessel.getHandtroll().equalsIgnoreCase("x")) {
						ht.setValue(true);
					} else {
						ht.setValue(false);
					}
					ht.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "handTroll", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox ll = new CheckBox();
					if (newvessel.getLongline().equalsIgnoreCase("x")) {
						ll.setValue(true);
					} else {
						ll.setValue(false);
					}
					ll.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "longLine", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox fw = new CheckBox();
					if (newvessel.getFishwheel().equalsIgnoreCase("x")) {
						fw.setValue(true);
					} else {
						fw.setValue(false);
					}
					fw.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "fishWheel", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox sot = new CheckBox();
					if (newvessel.getSingleottertrawl().equalsIgnoreCase("x")) {
						sot.setValue(true);
					} else {
						sot.setValue(false);
					}
					sot.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "singleOtterTrawl", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox p = new CheckBox();
					if (newvessel.getPotgear().equalsIgnoreCase("x")) {
						p.setValue(true);
					} else {
						p.setValue(false);
					}
					p.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "potGear", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox rn = new CheckBox();
					if (newvessel.getRingnet().equalsIgnoreCase("x")) {
						rn.setValue(true);
					} else {
						rn.setValue(false);
					}
					rn.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "ringNet", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox d = new CheckBox();
					if (newvessel.getDivegear().equalsIgnoreCase("x")) {
						d.setValue(true);
					} else {
						d.setValue(false);
					}
					d.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "diveGear", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox pt = new CheckBox();
					if (newvessel.getPowertroll().equalsIgnoreCase("x")) {
						pt.setValue(true);
					} else {
						pt.setValue(false);
					}
					pt.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "powerTroll", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox bt = new CheckBox();
					if (newvessel.getBeamtrawl().equalsIgnoreCase("x")) {
						bt.setValue(true);
					} else {
						bt.setValue(false);
					}
					bt.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "beamTrawl", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox sd = new CheckBox();
					if (newvessel.getDredge().equalsIgnoreCase("x")) {
						sd.setValue(true);
					} else {
						sd.setValue(false);
					}
					sd.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "dredge", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox db = new CheckBox();
					if (newvessel.getDinglebar().equalsIgnoreCase("x")) {
						db.setValue(true);
					} else {
						db.setValue(false);
					}
					db.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "dingleBar", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox mj = new CheckBox();
					if (newvessel.getJig().equalsIgnoreCase("x")) {
						mj.setValue(true);
					} else {
						mj.setValue(false);
					}
					mj.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "jig", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox dot = new CheckBox();
					if (newvessel.getDoubleottertrawl().equalsIgnoreCase("x")) {
						dot.setValue(true);
					} else {
						dot.setValue(false);
					}
					dot.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "doubleOtterTrawl", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox hg = new CheckBox();
					if (newvessel.getHearinggillnet().equalsIgnoreCase("x")) {
						hg.setValue(true);
					} else {
						hg.setValue(false);
					}
					hg.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "herringGillNet", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox ptr = new CheckBox();
					if (newvessel.getPairtrawl().equalsIgnoreCase("x")) {
						ptr.setValue(true);
					} else {
						ptr.setValue(false);
					}
					ptr.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "pairTrawl", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox og = new CheckBox();
					if (newvessel.getOthergear().equalsIgnoreCase("x")) {
						og.setValue(true);
					} else {
						og.setValue(false);
					}
					og.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "otherGear", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox cf = new CheckBox();
					if (newvessel.getFishingboat().equalsIgnoreCase("x")) {
						cf.setValue(true);
					} else {
						cf.setValue(false);
					}
					cf.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "fishingBoat", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox pr = new CheckBox();
					if (newvessel.getFreezerCanner().equalsIgnoreCase("x")) {
						pr.setValue(true);
					} else {
						pr.setValue(false);
					}
					pr.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "freezerCanner", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final CheckBox tp = new CheckBox();
					if (newvessel.getTenderPacker().equalsIgnoreCase("x")) {
						tp.setValue(true);
					} else {
						tp.setValue(false);
					}
					tp.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "tenderPacker", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					//	TODO figure out the Transporter value!!!!!
					final CheckBox ta = new CheckBox();
					//if (newvessel.getTransporter().equalsIgnoreCase("x")) {
					//	ta.setValue(true);
					//} else {
						ta.setValue(false);
					//}
					ta.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges2(entity, "transporter", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final SimpleComboBox snra1 = new SimpleComboBox();
					snra1.setWidth(200);
					snra1.setForceSelection(true);
					snra1.removeAll();
					snra1.setEditable(false);
					snra1.add("None");
					snra1.add("Southeast (A)");
					snra1.add("Prince William Sound (E)");
					snra1.add("Cook Inlet (H)");
					snra1.add("Kodiak (K)");
					snra1.add("Chignik (L)");
					snra1.add("AK Peninsula/Aleutians (M)");
					snra1.add("Bristol Bay (T)");
					if (newvessel.getSalmonregArea().equalsIgnoreCase("N/A")) {
						snra1.setSimpleValue("None");
	    			} else if (newvessel.getSalmonregArea().equalsIgnoreCase("A")) {
						snra1.setSimpleValue("Southeast (A)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("E")) {
						snra1.setSimpleValue("Prince William Sound (E)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("H")) {
						snra1.setSimpleValue("Cook Inlet (H)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("K")) {
						snra1.setSimpleValue("Kodiak (K)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("L")) {
						snra1.setSimpleValue("Chignik (L)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("M")) {
						snra1.setSimpleValue("AK Peninsula/Aleutians (M)");
					} else if (newvessel.getSalmonregArea().equalsIgnoreCase("T")) {
						snra1.setSimpleValue("Bristol Bay (T)");
					}
					snra1.setTriggerAction(TriggerAction.ALL);
					snra1.addListener(Events.BeforeSelect, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							oldreg = snra1.getSimpleValue().toString();
						}
					});
					snra1.addListener(Events.Select, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges5(entity, "salmonNetRegArea", cfecid.getValue().toString().toUpperCase(), snra1.getSimpleValue().toString(), oldreg, changeList, adfg);
						}
					});
					final TextField<String> snrp1 = new TextField<String>();
					snrp1.setWidth(75);
					if (!(newvessel.getPermitSerial1() == null)) {
						if (!(newvessel.getPermitSerial1().equalsIgnoreCase("N/A"))) {
							snrp1.setValue(newvessel.getPermitSerial1());
						}
					}	
					snrp1.setAllowBlank(true);
					snrp1.setMessageTarget("tooltip");
					snrp1.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
					snrp1.setMinLength(5);
					snrp1.setMaxLength(5);
					snrp1.setRegex("(\\d{5})");
					snrp1.getMessages().setRegexText("This permit serial number is invalid, must be 5 numeric characters");
					snrp1.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges1(entity, "salmonPermit1", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					final TextField<String> snrp2 = new TextField<String>();
					snrp2.setWidth(75);
					if (!(newvessel.getPermitSerial2() == null)) {
						if (!(newvessel.getPermitSerial2().equalsIgnoreCase("N/A"))) {
							snrp2.setValue(newvessel.getPermitSerial2());
						}
					}					
					snrp2.setAllowBlank(true);
					snrp2.setMessageTarget("tooltip");
					snrp2.getMessages().setMaxLengthText("You have exceeded the maximum character length (5) for this field");
					snrp2.setMinLength(5);
					snrp2.setMaxLength(5);
					snrp2.setRegex("(\\d{5})");
					snrp2.getMessages().setRegexText("This permit serial number is invalid, must be 5 numeric characters");
					snrp2.addListener(Events.Change, new Listener<FieldEvent>() {
						@Override
						public void handleEvent(FieldEvent be) {
							cr.getVesselChanges1(entity, "salmonPermit2", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg);
						}
					});
					snra1.addListener(Events.Select, new Listener<FieldEvent>() {
						public void handleEvent(FieldEvent be) {
							if (snra1.getSimpleValue().toString().equalsIgnoreCase("None")) {
								oldp1 = snrp1.getValue();
								oldp2 = snrp2.getValue();
								snrp1.clear();
								snrp2.clear();
								cr.getVesselChanges5(entity, "salmonPermit1", cfecid.getValue().toString().toUpperCase(), "N/A", oldp1, changeList, adfg);
								cr.getVesselChanges5(entity, "salmonPermit2", cfecid.getValue().toString().toUpperCase(), "N/A", oldp2, changeList, adfg);
							}
						}        	
			        });
					TextField<String> snrfpd = new TextField<String>();
					snrfpd.setWidth(100);
					snrfpd.setEnabled(false);

					HorizontalPanel vcs1 = new HorizontalPanel();
					vcs1.setHorizontalAlign(HorizontalAlignment.CENTER);
					vcs1.addText("<center><b>Vessel Specifications</b></center>");
					HorizontalPanel vc1 = new HorizontalPanel();
					vc1.addText("Vessel ADF&G #:&nbsp;&nbsp;&nbsp;");
					vc1.add(vadfg);
					vc1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Vessel Name:&nbsp;&nbsp;&nbsp;");
					vc1.add(vname);
					vc1.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;USCG #:&nbsp;&nbsp;&nbsp;");
					vc1.add(vuscg);
					HorizontalPanel vc2 = new HorizontalPanel();
					vc2.addText("Built:&nbsp;&nbsp;&nbsp;");
					vc2.add(vbuilt);
					vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Make/Model:&nbsp;&nbsp;&nbsp;");
					vc2.add(vmake);
					vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Length:&nbsp;&nbsp;&nbsp;");
					vc2.add(vlen);
					vc2.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Fee:&nbsp;&nbsp;&nbsp;");
					vc2.add(vfee);
					HorizontalPanel vc3 = new HorizontalPanel();
					vc3.addText("Gross Tons:&nbsp;&nbsp;&nbsp;");
					vc3.add(vgton);
					vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Net Tons:&nbsp;&nbsp;&nbsp;");
					vc3.add(vnton);
					vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Homeport City:&nbsp;&nbsp;&nbsp;");
					vc3.add(vhpc);
					vc3.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Homeport State:&nbsp;&nbsp;&nbsp;");
					vc3.add(vhps);
					HorizontalPanel vc4 = new HorizontalPanel();
					vc4.addText("Engine (D/G):&nbsp;&nbsp;&nbsp;");
					vc4.add(vengine);
					vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Horsepower:&nbsp;&nbsp;&nbsp;");
					vc4.add(vhp);
					vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Est. Value:&nbsp;&nbsp;&nbsp;");
					vc4.add(vvalue);
					vc4.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Hull Type:&nbsp;&nbsp;&nbsp;");
					vc4.add(vhtype);
					HorizontalPanel vc5 = new HorizontalPanel();
					vc5.addText("Hull ID:&nbsp;&nbsp;&nbsp;");
					vc5.add(vhid);
					vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Fuel Capacity:&nbsp;&nbsp;&nbsp;");
					vc5.add(vfuel);
					vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Refrig (Y/N):&nbsp;&nbsp;&nbsp;");
					vc5.add(vref);
					vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Live:&nbsp;&nbsp;&nbsp;");
					vc5.add(vlive);
					vc5.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Hold:&nbsp;&nbsp;&nbsp;");
					vc5.add(vhold);
					HorizontalPanel vcs2 = new HorizontalPanel();
					vcs2.setHorizontalAlign(HorizontalAlignment.CENTER);
					vcs2.addText("<br><center><b>Salmon Troll Registration</b></center>");
					HorizontalPanel vc6 = new HorizontalPanel();
					vc6.add(vstr);
					vc6.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Effective Date: (YYYY-MM-DD)&nbsp;&nbsp;&nbsp;");
					vc6.add(vstrdate);
					HorizontalPanel vcs3 = new HorizontalPanel();
					vcs3.setHorizontalAlign(HorizontalAlignment.CENTER);
					vcs3.addText("<br><center><b>Types of Gear Fished</b></center>");
					HorizontalPanel vc7 = new HorizontalPanel();
					vc7.add(ps);
					vc7.addText("&nbsp;Purse Seine&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(bs);
					vc7.addText("&nbsp;Beach Seine&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(dgn);
					vc7.addText("&nbsp;Drift Gill Net&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(sgn);
					vc7.addText("&nbsp;Set Gill Net&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(ht);
					vc7.addText("&nbsp;Hand Troll&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(ll);
					vc7.addText("&nbsp;Long Line&nbsp;&nbsp;&nbsp;&nbsp;");
					vc7.add(fw);
					vc7.addText("&nbsp;Fish Wheel");
					HorizontalPanel vc8 = new HorizontalPanel();
					vc8.add(sot);
					vc8.addText("&nbsp;SingleOtter Trawl&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(p);
					vc8.addText("&nbsp;Pots&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(rn);
					vc8.addText("&nbsp;Ring Net&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(d);
					vc8.addText("&nbsp;Diving&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(pt);
					vc8.addText("&nbsp;Power Troll&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(bt);
					vc8.addText("&nbsp;Beam Trawl&nbsp;&nbsp;&nbsp;&nbsp;");
					vc8.add(sd);
					vc8.addText("&nbsp;Scallop Drg");
					HorizontalPanel vc9 = new HorizontalPanel();
					vc9.add(db);
					vc9.addText("&nbsp;Dinglebar&nbsp;&nbsp;&nbsp;&nbsp;");
					vc9.add(mj);
					vc9.addText("&nbsp;Mechanical Jig&nbsp;&nbsp;&nbsp;&nbsp;");
					vc9.add(dot);
					vc9.addText("&nbsp;Double Otter Trawl&nbsp;&nbsp;&nbsp;&nbsp;");
					vc9.add(hg);
					vc9.addText("&nbsp;Herring Gillnet&nbsp;&nbsp;&nbsp;&nbsp;");
					vc9.add(ptr);
					vc9.addText("&nbsp;Pair Trawl&nbsp;&nbsp;&nbsp;&nbsp;");
					vc9.add(og);
					vc9.addText("&nbsp;Other Gear");
					HorizontalPanel vcs4 = new HorizontalPanel();
					vcs4.setHorizontalAlign(HorizontalAlignment.CENTER);
					vcs4.addText("<br><center><b>Vessel Activities</b></center>");
					HorizontalPanel vc10 = new HorizontalPanel();
					vc10.add(cf);
					vc10.addText("&nbsp;Commerical Fishing&nbsp;&nbsp;&nbsp;&nbsp;");
					vc10.add(pr);
					vc10.addText("&nbsp;Freezer/Canner&nbsp;&nbsp;&nbsp;&nbsp;");
					vc10.add(tp);
					vc10.addText("&nbsp;Tender/Packer&nbsp;&nbsp;&nbsp;&nbsp;");
					vc10.add(ta);
					vc10.addText("&nbsp;Transporter&nbsp;&nbsp;&nbsp;&nbsp;");
					HorizontalPanel vcs5 = new HorizontalPanel();
					vcs5.setHorizontalAlign(HorizontalAlignment.CENTER);
					vcs5.addText("<br><center><b>Salmon Net Registration</b></center>");
					HorizontalPanel vc11 = new HorizontalPanel();
					vc11.addText("Area:&nbsp;&nbsp;&nbsp;");
					vc11.add(snra1);
					vc11.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Permit 1:&nbsp;&nbsp;&nbsp;");
					vc11.add(snrp1);
					vc11.addText("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Permit 2:&nbsp;&nbsp;&nbsp;");
					vc11.add(snrp2);

					vchar.add(vcs1);
					vchar.add(vc1);
					vchar.add(vc2);
					vchar.add(vc3);
					vchar.add(vc4);
					vchar.add(vc5);
					vchar.add(vcs2);
					vchar.add(vc6);
					vchar.add(vcs3);
					vchar.add(vc7);
					vchar.add(vc8);
					vchar.add(vc9);
					vchar.add(vcs4);
					vchar.add(vc10);
					vchar.add(vcs5);
					vchar.add(vc11);
					cchar.add(vchar);
					editVeswin.add(cchar);
					editVeswin.addButton(new Button("Save and Return", new SelectionListener<ButtonEvent>() {
						public void componentSelected(ButtonEvent ce) {
							timer.timerCancel();
							timer.progReset();
							timer.setTimer(timer.getTime(), cfecid.getValue().toString().toUpperCase());
							if (vchar.isValid()) {
								if (!(vname.getValue() == null)) {
									newvessel.setName(vname.getValue());
								} else {
									newvessel.setName("N/A");
								}
								if (!(vgton.getValue() == null)) {
									newvessel.setGrossTons(vgton.getValue());
								} else {
									newvessel.setGrossTons("0");
								}
								if (!(vnton.getValue() == null)) {
									newvessel.setNetTons(vnton.getValue());
								} else {
									newvessel.setNetTons("0");
								}
								if (!(vhpc.getValue() == null)) {
									newvessel.setHomeportCity(vhpc.getValue());
								} else {
									newvessel.setHomeportCity("N/A");
								}
								if (!(vhps.getValue() == null)) {																				
									newvessel.setHomeportState(vhps.getValue());
								} else {
									newvessel.setHomeportState("N/A");
								}
								if (!(vengine.getValue() == null)) {
									newvessel.setEngineType(vengine.getValue());
								} else {
									newvessel.setEngineType("N/A");
								}
								if (!(vhp.getValue() == null)) {
									newvessel.setHorsepower(vhp.getValue());
								} else {
									newvessel.setHorsepower("0");
								}
								if (!(vvalue.getValue() == null)) {
									newvessel.setEstValue(vvalue.getValue());
								} else {
									newvessel.setEstValue("0");
								}
								if (!(vfuel.getValue() == null)) {
									newvessel.setFuel(vfuel.getValue());
								} else {
									newvessel.setFuel("0");
								}
								if (!(vref.getValue() == null)) {
									newvessel.setRefrigeration(vref.getValue());
								} //else {
								//	newvessel.setRefrigeration("N/A");
								//}
								if (!(vlive.getValue() == null)) {
									newvessel.setLiveTank(vlive.getValue());
								} else {
									newvessel.setLiveTank("0");
								}
								if (!(vhold.getValue() == null)) {
									newvessel.setHoldTank(vhold.getValue());
								} else {
									newvessel.setHoldTank("0");
								}
								if (strpower.getValue()) {
									newvessel.setSalmontrollReg("P");
								} else if (strhand.getValue()) {
									newvessel.setSalmontrollReg("H");
								} else {
									newvessel.setSalmontrollReg("N/A");
								}
								if (vstrdate.getValue() != null) {
									newvessel.setSalmontrollDate(DateTimeFormat.getFormat("yyyy-MM-dd").format(vstrdate.getValue()));
								} else {
									newvessel.setSalmontrollDate("N/A");
								}
								if (ps.getValue()) {
									newvessel.setPurseseine("X");
								} else {
									newvessel.setPurseseine("N");
								}
								if (bs.getValue()) {
									newvessel.setBeachseine("X");
								} else {
									newvessel.setBeachseine("N");
								}
								if (dgn.getValue()) {
									newvessel.setDriftgillnet("X");
								} else {
									newvessel.setDriftgillnet("N");
								}
								if (sgn.getValue()) {
									newvessel.setSetgillnet("X");
								} else {
									newvessel.setSetgillnet("N");
								}
								if (ht.getValue()) {
									newvessel.setHandtroll("X");
								} else {
									newvessel.setHandtroll("N");
								}
								if (ll.getValue()) {
									newvessel.setLongline("X");
								} else {
									newvessel.setLongline("N");
								}
								if (fw.getValue()) {
									newvessel.setFishwheel("X");
								} else {
									newvessel.setFishwheel("N");
								}
								if (sot.getValue()) {
									newvessel.setSingleottertrawl("X");
								} else {
									newvessel.setSingleottertrawl("N");
								}
								if (p.getValue()) {
									newvessel.setPotgear("X");
								} else {
									newvessel.setPotgear("N");
								}
								if (rn.getValue()) {
									newvessel.setRingnet("X");
								} else {
									newvessel.setRingnet("N");
								}
								if (d.getValue()) {
									newvessel.setDivegear("X");
								} else {
									newvessel.setDivegear("N");
								}
								if (pt.getValue()) {
									newvessel.setPowertroll("X");
								} else {
									newvessel.setPowertroll("N");
								}
								if (bt.getValue()) {
									newvessel.setBeamtrawl("X");
								} else {
									newvessel.setBeamtrawl("N");
								}
								if (sd.getValue()) {
									newvessel.setDredge("X");
								} else {
									newvessel.setDredge("N");
								}
								if (db.getValue()) {
									newvessel.setDinglebar("X");
								} else {
									newvessel.setDinglebar("N");
								}
								if (mj.getValue()) {
									newvessel.setJig("X");
								} else {
									newvessel.setJig("N");
								}
								if (dot.getValue()) {
									newvessel.setDoubleottertrawl("X");
								} else {
									newvessel.setDoubleottertrawl("N");
								}
								if (hg.getValue()) {
									newvessel.setHearinggillnet("X");
								} else {
									newvessel.setHearinggillnet("N");
								}
								if (ptr.getValue()) {
									newvessel.setPairtrawl("X");
								} else {
									newvessel.setPairtrawl("N");
								}
								if (og.getValue()) {
									newvessel.setOthergear("X");
								} else {
									newvessel.setOthergear("N");
								}
								if (cf.getValue()) {
									newvessel.setFishingboat("X");
								} else {
									newvessel.setFishingboat("N");
								}
								if (pr.getValue()) {
									newvessel.setFreezerCanner("X");
								} else {
									newvessel.setFreezerCanner("N");
								}
								if (tp.getValue()) {
									newvessel.setTenderPacker("X");
								} else {
									newvessel.setTenderPacker("N");
								}
								if (ta.getValue()) {
									newvessel.setTransporter("X");
								} else {
									newvessel.setTransporter("N");
								}
								if (!(snra1.getValue() == null)) {
									if (snra1.getSimpleValue().toString().equalsIgnoreCase("None")) {
					    				areacode = "N/A";
					    			} if (snra1.getSimpleValue().toString().equalsIgnoreCase("Southeast (A)")) {
										areacode = "A";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Prince William Sound (E)")) {
										areacode = "E";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Cook Inlet (H)")) {
										areacode = "H";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Kodiak (K)")) {
										areacode = "K";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Chignik (L)")) {
										areacode = "L";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("AK Peninsula/Aleutians (M)")) {
										areacode = "M";
									} else if (snra1.getSimpleValue().toString().equalsIgnoreCase("Bristol Bay (T)")) {
										areacode = "T";
									}
									newvessel.setSalmonregArea(areacode);
								}
								if (!(snrp1.getValue() == null)) {
									newvessel.setPermitSerial1(snrp1.getValue());
								} else {
									newvessel.setPermitSerial1("N/A");
								}
								if (!(snrp2.getValue() == null)) {
									newvessel.setPermitSerial2(snrp2.getValue());
								} else {
									newvessel.setPermitSerial2("N/A");
								}													
								newvessel.setNewVessel(true);													
					    		vlist.add(newvessel);
					    		getVes.newVessel(feeTotals, entity, newvessel);
					    		loader.load();
					    		Log.info(entity.getId().getCfecid().toUpperCase() + " has added a new Vessel (ADFG "+ newvessel.getId().getAdfg()+", Name "+newvessel.getName()+") to their profile");
								DOM.getElementById("vesselsRenewalble").setInnerText(Integer.toString(vlist.size()));
								tr.layout();						
								editVeswin.hide();
								editVeswin = null;
							}
						}
					}));
					editVeswin.addButton(new Button("Cancel and Return", new SelectionListener<ButtonEvent>() {
							public void componentSelected(ButtonEvent ce) {
								timer.timerCancel();
								timer.progReset();
								timer.setTimer(timer.getTime(), cfecid.getValue().toString().toUpperCase());
								editVeswin.hide();
								editVeswin = null;
							}
						}));
					editVeswin.show();
				}
			}
		});
	}
	
	public String getvview(List<ArenewVessels> vlist, String xname) {
		StringBuffer v = new StringBuffer("<table id='vesTable'><tr>");
		v.append("<th><span class='regblue10'>Vessel Name</span></th>");
    	v.append("<th><span class='regblue10'>CG Number</span></th>");
    	v.append("<th><span class='regblue10'>ADFG Number</span></th>");
    	v.append("<th><span class='regblue10'>Owner Name</span></th>");
    	v.append("<th><span class='regblue10'>Status</span></th></tr>");
    	int x = 0;
    	if (vlist.size() > 0) {
    		while (x < vlist.size()) {
    			v.append("<tr><td><span class='regblack10'>").append(vlist.get(x).getName()).append("</span></td>");
        		v.append("<td><span class='regblack10'>").append(vlist.get(x).getRegNum()).append("</span></td>");
        		v.append("<td><span class='regblack10'>").append(vlist.get(x).getId().getAdfg()).append("</span></td>");
        		v.append("<td><span class='regblack10'>").append(xname).append("</span></td>");
        		if (vlist.get(x).getStatus().equalsIgnoreCase("Pending")) {
        			v.append("<td><span class='regorange10' title='This Vessel is pending renewal as of "+vlist.get(x).getReceiveddate()+"'>").append(vlist.get(x).getStatus()).append("</span></td></tr>");
        		} else if (vlist.get(x).getStatus().equalsIgnoreCase("Completed")) {
        			v.append("<td><span class='regred10' title='This Vessel has been has been renewed, you should receive the card in the mail shortly'>").append(vlist.get(x).getStatus()).append("</span></td></tr>");
        		} else if (vlist.get(x).getStatus().equalsIgnoreCase("Available")) {
        			v.append("<td><span class='reggreen10' title='This Vessel is still available and has not been selected for renweal'>").append(vlist.get(x).getStatus()).append("</span></td></tr>");
        		} else {
        			v.append("<td><span class='regblue10' title='Please call a CFEC licensing agent on the status of this Vessel'>").append(vlist.get(x).getStatus()).append("</span></td></tr>");
        		}
	        	x++;
    		}
    	}
    	v.append("</table>");
		return v.toString();
	}
}
