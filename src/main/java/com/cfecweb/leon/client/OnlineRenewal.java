package com.cfecweb.leon.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.allen_sauer.gwt.log.client.Logger;
import com.allen_sauer.gwt.log.client.RemoteLogger;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.dto.UserSessionSettings;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

import javax.annotation.Nullable;
//import com.pervasive.pscs.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
/*
 * The entry point class is the MAIN and first class called during initial application execution. By default, it is usually named
 * after the project name. There are a ton of environmental variables being established here that are passed throughout the application
 * into and out of the various screens. It also defines the main borderlayout, which gives us our 4 main containers for content. Those
 * containers are named appropriately and asynchronously updated when necessary. onModuleLoad is executed first, then automatically 
 * calls in the onRender class. This is probably the most confusing class, but there are only 2 ways out, either the person goes to the
 * download screen or they advance to the address screen. This can be found towards the bottom.
 * One of the environmental variables established is a timer that looks for and resets after certain activity. The timeout value is set
 * currently for 20 minutes. This can be defined differently in the server package. When most buttons are activated or other defined activities,
 * the timer is reset back to 20 minutes. I used to have a progressBar in the lower left corner that visually denoted remaining time, but
 * I removed it.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class OnlineRenewal extends LayoutContainer implements EntryPoint {
	/*
	 * The following container effects nearly every conceivable function of LEON and should
	 * NOT be populated with actual data. The renewal year is automatically obtained from a 
	 * source in the TBL file that gets updated during rollover. 
	 * (the initial asynchronous call to establish a session timer returns this date)
	 */
	static String ryear;
	/*
	 * System variables and classes
	 */
	int curNumber = 0;
	int oldNumber = 0;
	int bs = 2;
	HeaderText ghead = new HeaderText();
	InitialOptions ginit = new InitialOptions();
	InstructionsText gins = new InstructionsText();
	ProgressbarText gpro = new ProgressbarText();
	UserMessages gmsg = new UserMessages();
	ScreenAddress gadd = new ScreenAddress();
	FeeTotals feeTotals = null;
	VerticalPanel bottomLeftVPanel = null;
	Button startOver = null; Button next = null; Button last = null;
	FieldSet topRight = null; FieldSet bottomRight = null; 
	FieldSet topLeft = null; FieldSet bottomLeft = null;
	HTML statusBar = null; HTML phrdText = null;
	FormPanel buttonForm = null; 
	HorizontalPanel NavprogressBarPanel = null;
	String temp = null; 
	String agentName = null;
	HTML reschoice = new HTML("<span class='boldred14'>Please note:</span> You current Alaska residency declaration is: <span class='boldblack14'>UNSELECTED</span>");
	private SelectionListener<?> nextListener = null;
	List<ArenewChanges> changeList = new ArrayList();
	TextField<String> cfecid = null;	
	boolean isAgent = false;
	private int time;
	ProgressBar prog = null;
	SessionTimer timer = new SessionTimer();
	final Logger logger = new RemoteLogger();
	ArenewPayment payment = new ArenewPayment();
	ArenewEntity entity = null;
	List<ArenewPermits> plist = new ArrayList();
	List<ArenewVessels> vlist = new ArrayList();
	CheckBox first = new CheckBox();
	CheckBox second = new CheckBox();
	CheckBox nop = new CheckBox();

    // Holds the version string after the RPC returns
    private static String appVersion = "unknown";

    // Builds the footer HTML with the current version string
    private static String buildStatusBarHtml() {
        return "<span class='regblack12'>" +
                "Welcome to the CFEC Online Permit and Vessel renewal application" +
                " | Version: " + appVersion +
                "</span>";
    }

    /**
	 * Create the initial token id for browser history
	 */
	public static final String init_state = "entry";
	/**
	 * Create a remote service proxy to talk to the server-side getData service.
	 */
	private final getDataAsync service = (getDataAsync) getData.Util.getInstance();

    private String recaptchaSiteKey;
    private String recaptchaAction;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Viewport v = new Viewport();
		v.add(this);
		v.setLayout(new FitLayout());
		v.layout();
		AsyncCallback<UserSessionSettings> callback = new AsyncCallback<UserSessionSettings>() {
	       public void onSuccess(UserSessionSettings result) {
                recaptchaSiteKey = result.getRecaptchaSiteKey();
                recaptchaAction = result.getRecaptchaAction();
	    	    ryear = result.getRenewalYear().toString();
	    	    //ryear = "2022";
	    	    time = result.getUserSessionTimeoutMillis();
	            int sessionTimeMillis = time;
	            timer.setTimer(sessionTimeMillis, "Unknown");
	            RootPanel.get().add(v);

               service.getAppVersion(new AsyncCallback<String>() {
                   @Override
                   public void onFailure(Throwable caught) {
                       // keep "unknown" if it fails
                   }

                   @Override
                   public void onSuccess(String version) {
                       if (version != null && !version.trim().isEmpty()) {
                           appVersion = version.trim();
                       }
                       if (statusBar != null) {
                           statusBar.setHTML(buildStatusBarHtml());
                       }
                   }
               });

           }
	       public void onFailure(final Throwable caught) {
	    	   gmsg.alert("Login Error", gins.getTech(), 350);
	       }
	    };
	    service.getUserSessionTimeoutMillis(callback);	
	    /*
	     * GWT History listener
	     */
	    History.addValueChangeHandler(new ValueChangeHandler<String>() {
	        public void onValueChange(ValueChangeEvent<String> event) {
	          String historyToken = event.getValue();
	          /*
	           * Parse the history token
	           */
	          try {
	        	  if (historyToken.length() > 0) {
	        		  if (historyToken.substring(0, 4).equals("leon")) {
	        			//	do nothing
	  	              } else {
	  	            	//	do nothing
	  	              }
	        	 } else {
	        		 final Listener<MessageBoxEvent> l = new Listener<MessageBoxEvent>() {  
	        			 public void handleEvent(MessageBoxEvent ce) {  
	        				 Button btn = ce.getButtonClicked();  
	        				 if (btn.getHtml().toLowerCase().contains("no")) {
	        					 System.out.println("Stay in LEON");
	        					 History.forward(); 
	        				 } else {
	        					 System.out.println("Exit LEON");
	        					 History.back();
	        				 }
	        			 }  
	        		 };	        		 
	        		 MessageBox leave = new MessageBox();
	        		 leave.setButtons(MessageBox.YESNO);
	        		 leave.setIcon(MessageBox.QUESTION);
	        		 leave.getDialog().setTitle("Leave the application?");
	        		 leave.addCallback(l);  
	        		 leave.setMessage("Pressing the browsers BACK button will force you out of LEON completely. " +
	        		 		"If you are simply trying to navigate within LEON, please use the <span class='regred12'><< Last</span> " +
	        		 		"or <span class='regred12'>Next >></span> buttons. Select Yes if you actually leaving LEON or No otherwise");  
	        		 leave.show();
	        	 }
	          } catch (IndexOutOfBoundsException e) {
	            //tabPanel.selectTab(0);
	          }
	        }
	    });
    	History.newItem("leon");
    	oldNumber = 1;
	}

    public static void loadReCaptcha(final String siteKey, final Callback<Void, Exception> onLoad) {
        final String url = "https://www.google.com/recaptcha/api.js?render=" + siteKey;

        ScriptInjector.fromUrl(url)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (onLoad != null) { onLoad.onSuccess(result); }
                    }

                    @Override
                    public void onFailure(Exception exc) {
                        // log / report if desired
                        Log.error("Failed to load reCAPTCHA v3 script", exc);
                        if (onLoad != null) { onLoad.onFailure(exc); }
                    }
                })
                .inject();
    }

	@Override
	protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        loadReCaptcha(recaptchaSiteKey, null);

        setLayout(new BorderLayout());

        @Nullable String action = Window.Location.getParameter("action");
        @Nullable String ref = Window.Location.getParameter("ref");

        feeTotals = new FeeTotals();
        
        /*
         * 	North Panel Configuration
         */
        ContentPanel north = new ContentPanel();  
        north.setHeaderVisible(false);
        north.setBodyStyle("backgroundColor: #EEEEEE");
        north.add(ghead.getHeader(ryear));
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 30);
        northData.setSplit(true);
        northData.setMargins(new Margins(2, 5, 0, 5));
        
        /*
         * 	West Panel Configuration    
         */
        bottomLeft = new FieldSet();
        bottomLeft.setHeadingHtml("<span class='boldgreen12'>Screen Instructions</span>");
        bottomLeft.setAutoHeight(true);
        bottomLeft.addStyleName("topLeft");       
        bottomLeftVPanel = new VerticalPanel();
        bottomLeftVPanel.setId("bottomLeftVPanel");
        bottomLeftVPanel.setAutoWidth(true);
        bottomLeftVPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        bottomLeftVPanel.addText(gins.getInit());
        startOver = new Button("<span class='boldblack12'>Reset Application</span>", new SelectionListener<ButtonEvent>() {
       	    public void componentSelected(ButtonEvent ce) {
       	    	if (!(cfecid.getValue() == null)) {
       	    		Log.info(cfecid.getValue().toUpperCase() + " has re-initialized the application");
       	    	}
       	    	/*
       	    	 * Reset timer
       	    	 */
       	    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(time, cfecid.getValue());	       	    
	       	    /*
	       	     * Reset feeTables
	       	     */
	       	    feeTotals = new FeeTotals();	       	    
	       	    /*
	       	     * Reset payment object
	       	     */
	       	    payment = new ArenewPayment();	       	    
	       	    /*
	       	     * Reset change object
	       	     */
       	    	changeList = new ArrayList();       	    	
       	    	/*
       	    	 * Reset entity
       	    	 */
       	    	entity = null;       	  
       	    	/*
       	    	 * Reset Agent Name
       	    	 */
       	    	agentName = null;
       	    	/*
       	    	 * 	Reset StatusBar
       	    	 */
                statusBar.setHTML(buildStatusBarHtml());
                /*
       	    	 * reset reschoice
       	    	 */
       	    	reschoice = new HTML("<span class='boldred14'>Please note:</span> You current Alaska residency declaration is: <span class='boldblack14'>UNSELECTED</span>");
       	    	bottomLeft.removeAll();
       	    	bottomLeft.setAutoHeight(true);
    	    	bottomLeftVPanel.removeAll();
    	    	/*
    	    	 * 	get the initial instructions
    	    	 */
    	    	bottomLeftVPanel.addText(gins.getInit());
    	    	/*
    	    	 * 	remove all objects from BottomRight and TopRight FieldSets
    	    	 */
    	    	topRight.removeAll();
    	    	bottomRight.removeAll();
    	    	topLeft.removeAll();
    	    	topRight.setAutoHeight(true);
    	    	topLeft.setHeight(115); 
    	    	topLeft.addText("<table width='100%' bgcolor='#FFFFCC' border='0' cellspacing ='0'><tr><td><br><p><center>" +
        		"<span class='boldblack12'>No Permit Holder or Vessel Owner queried</span></center></p></td></tr></table>");
    	    	/*
    	    	 * 	redefine user message
    	    	 */
    	    	phrdText = new HTML("<span class='regblack12'><center>Please declare if you are the actual Permit Holder/Vessel Owner or Authorized Agent below</center></span>");
    	    	/*
    	    	 * 	add the new elements to BottomRight and TopRight FieldSets
    	    	 */
    	    	NavprogressBarPanel = new HorizontalPanel();
    	        NavprogressBarPanel.setTableWidth("100%");
    	        NavprogressBarPanel.setAutoHeight(true);
    	        NavprogressBarPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
    	        last = new Button("<span class='regred10'><< Last</span>");
    	        last.disable();
    	        NavprogressBarPanel.add(last);
    	        NavprogressBarPanel.addText(gpro.getPro());
    	        next = new Button("<span class='regred10'>Next >></span>");
    	        next.disable();
    	        NavprogressBarPanel.add(next);
    	    	topRight.add(phrdText);
    	    	topRight.add(buttonForm);
    	    	topRight.add(NavprogressBarPanel);
    	        /*
    	         * 	refresh the TopLeft and BottomRight FieldSets and the TopLeftVertical Panel
    	         */
    	        bottomLeft.add(bottomLeftVPanel);
    	        bottomLeft.layout();
    	        bottomLeftVPanel.layout();
    	        bottomRight.setHeadingHtml("<span class='boldblack12'>LEON Initial Options</span>");
    	        bottomRight.addText("<span class='regred12'><center>To begin, select the appropriate option above</center></span>");
     	    	bottomRight.layout();
     	    	topRight.setHeadingHtml("<span class='boldblack12'>Status, Fee Totals and Navigation</span>");
     	    	topRight.layout();
     	    	topLeft.layout();
        	    /*
        	     * 	disable and clear the initial user options
        	     */
     		    next.removeAllListeners();
     		    next.addListener(Events.Select, nextListener);
     		    next.disable();
     		    last.removeAllListeners();
     		    last.disable();
     		    ginit.getRenewal().setValue(false);
     		    ginit.getForms().setValue(false);
     		    ginit.getPrivacy().setValue(false);
     		    ginit.getAres().setValue(false);
     		    ginit.getNres().setValue(false);
     		    ginit.getAcit().setValue(false);
     		    ginit.getNcit().setValue(false);
     		    cfecid.enable();
     		    cfecid.setAllowBlank(true);
     		    cfecid.setValue(null);
     		    cfecid.setEmptyText("Enter your CFEC ID Number here"); 
     		    cfecid.setAllowBlank(false);
     		    /*
     		     * 	set the progress bar back to default
     		     */
     		    DOM.getElementById("progressBar1").getStyle().setProperty("bgcolor", "#DCDCDC");
     		    DOM.getElementById("progressBar2").getStyle().setProperty("bgcolor", "White");
     		    DOM.getElementById("progressBar3").getStyle().setProperty("bgcolor", "White");
     		    DOM.getElementById("progressBar4").getStyle().setProperty("bgcolor", "White");
     		    DOM.getElementById("progressBar5").getStyle().setProperty("bgcolor", "White");
     		    DOM.getElementById("progressBar6").getStyle().setProperty("bgcolor", "White");
     		}
        });
        startOver.setTitle("Click this button to re-initialize your session and start over. Any changes or modifications you have made will be lost");
        bottomLeft.add(bottomLeftVPanel);
        topLeft = new FieldSet();
        topLeft.setHeadingHtml("<span class='boldblack12'>Permit/Vessel Owner</span>");
        topLeft.setHeight(115); 
        topLeft.addStyleName("bottomLeft");
        topLeft.setId("blinfo");
        topLeft.addText("<table width='100%' bgcolor='#FFFFCC' border='0' cellspacing ='0'><tr><td><br><p><center>" +
        		"<span class='boldblack12'>No Permit Holder/Vessel Owner queried</span></center></p></td></tr></table>");
        ContentPanel west = new ContentPanel();
        west.setHeaderVisible(false);
        west.setScrollMode(Scroll.AUTO);
        west.setBodyStyle("backgroundColor: #EEEEEE");
        west.add(topLeft);
        west.add(bottomLeft);
        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 225);
        westData.setSplit(true);
        westData.setMargins(new Margins(2, 2, 2, 5));  
        
        /*
         * 	Center Panel Configuration
         */
        bottomRight = new FieldSet();
        bottomRight.setHeadingHtml("<span class='boldblack12'>LEON Initial Options</span>");
        //bottomRight.setAutoHeight(true);
        bottomRight.addStyleName("topRight");  
        bottomRight.setLayout(new FitLayout());
        bottomRight.setId("trinfo"); 
        bottomRight.addText("<span class='regred12'><center>To begin, select the appropriate option above</center></span>");
        topRight = new FieldSet();
        topRight.setHeadingHtml("<span class='boldblack12'>Status, Fee Totals and Navigation</span>");
        topRight.setAutoHeight(true);
        topRight.setAutoWidth(true);
        topRight.setId("brinfo");
        topRight.addStyleName("bottomRight");
        ContentPanel center = new ContentPanel();
        center.setHeaderVisible(false);
        center.setScrollMode(Scroll.AUTO);
        center.setLayout(new FitLayout());
        center.add(topRight);
        center.add(bottomRight);
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setSplit(true);
        center.setBodyStyle("backgroundColor: #EEEEEE");
        centerData.setMargins(new Margins(2, 5, 2, 0));
        
        /*
         * 	South Panel Configurations
         */
        ContentPanel south = new ContentPanel();
        south.setHeaderVisible(false);
        south.setBodyBorder(false);
        south.setFrame(false);
        
        LayoutContainer main = new LayoutContainer();  
        main.setLayout(new ColumnLayout()); 
        
        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("marginRight", "2px");
        FormLayout layout = new FormLayout();  
        left.setLayout(layout); 
        
        LayoutContainer right = new LayoutContainer();          
        FormLayout layout4 = new FormLayout();  
        right.setLayout(layout4);
        
        ContentPanel prb = new ContentPanel();
        prb.setHeaderVisible(false);
        prb.setHeight(25); 
        prog = timer.getProgbar();
        prb.add(prog);
        left.add(prb);
        
        ContentPanel sb = new ContentPanel();
        sb.setHeaderVisible(false);
        sb.setHeight(25);
        sb.setAutoWidth(true);
        sb.setBodyStyle("backgroundColor: #EEEEEE");
        statusBar = new HTML(buildStatusBarHtml());
        statusBar.setStyleName("statusBar");
        sb.add(statusBar);
        right.add(sb);    
        
        main.add(left, new ColumnData(250));
        main.add(right, new ColumnData(1));
        
        south.add(sb);    
        
        BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 25);
        southData.setSplit(true);
        southData.setMargins(new Margins(0, 5, 2, 5));
        
        /*
         * 	Add BorderLayout Panel Configurations to ViewPort
         */
        add(north, northData);  
        add(west, westData);  
        add(center, centerData);  
        add(south, southData);  
        
        /*
         * 	This HorizontalPanel defines the Last and Next Buttons, as well as the Progress Bar
         */
        NavprogressBarPanel = new HorizontalPanel();
        NavprogressBarPanel.setTableWidth("100%");
        NavprogressBarPanel.setAutoHeight(true);
        NavprogressBarPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        last = new Button("<span class='regred10'><< Last</span>");
        last.disable();
        NavprogressBarPanel.add(last);
        NavprogressBarPanel.addText(gpro.getPro());
        next = new Button("<span class='regred10'>Next >></span>");
        next.disable();
        NavprogressBarPanel.add(next);
        
        /*
         * 	This HorizontalPanel defines the user input and label for CFEC ID
         */
        final HorizontalPanel id = new HorizontalPanel();
        id.setTableWidth("100%");
        id.setAutoWidth(true);
        id.setAutoHeight(true);
        id.setHorizontalAlign(HorizontalAlignment.CENTER);
        id.addStyleName("pbar");
        cfecid = new TextField<String>();
        cfecid.setAllowBlank(false);
        cfecid.setWidth(200);
        cfecid.setEmptyText("Enter your CFEC ID Number here");       
        id.add(cfecid);

        // Show Payment Confirmation form and return, otherwise skip to main UI
        if ("confirm".equalsIgnoreCase(action)) {
            final String finalRecaptchaSiteKey = recaptchaSiteKey;
            final String finalRecaptchaAction = recaptchaAction;

            ScreenProcessing proc = new ScreenProcessing();
            proc.paymentPostProcess(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last,
                    statusBar, phrdText, NavprogressBarPanel, service, timer, first, second, nop,
                    ref, finalRecaptchaSiteKey, finalRecaptchaAction);
            return;
        }

        /*
         * 	This HTML String is the first message the user will see in the BottomLeft FieldSet
         */
        phrdText = new HTML("<span class='regblack12'><center>Please declare if you are the actual Permit Holder/Vessel Owner or Authorized Agent below</center></span>");
        
        /*
         * 	This Button Bar defines the options for Permit Holder or Authorized Agent
         */
        buttonForm = new FormPanel();
        buttonForm.setButtonAlign(HorizontalAlignment.CENTER);
        buttonForm.setFrame(false);
        buttonForm.setBorders(false);
        buttonForm.setBodyBorder(false);
        buttonForm.setHeaderVisible(false);
        buttonForm.setPadding(0);
        buttonForm.setLayout(new FlowLayout());
        Button phorvo = new Button("<span class='regred10'>I am the Permit/Vessel Owner&nbsp;</span>");
        phorvo.setStyleAttribute("padding-right", "10px");
        phorvo.setTitle("I am the actual owner of the Permits/Vessels I wish to renew");
        phorvo.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
     	    public void componentSelected(ButtonEvent ce) {
     	    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(time, cfecid.getValue());	       	    
    			isAgent = false;
       	    	agentName = "N/A";
       	    	bottomRight.removeAll();
       	    	bottomRight.add(ginit.getOption1(ryear));  
  				bottomRight.add(ginit.getOption4(ryear));  
  				bottomRight.add(ginit.getOption3(ryear));
  				bottomRight.add(ginit.getOption2(ryear));
       		    /*
       		     * 	remove the objects from the BottomRight FieldSet
       		     */
       		    topRight.removeAll();
       		    /*
       		     * 	remove the objects from the TopLeft FieldSet
       		     */
       		    bottomLeftVPanel.removeAll();
       		    /*
       		     * 	add the Start Over button at bottom of TopLeft FieldSet
       		     */
       		    bottomLeftVPanel.add(startOver);     		    
       		    /*
       		     * 	add new instructions
       		     */
       		    bottomLeftVPanel.addText(gins.getOptions());
       		    /*
       		     * 	redefine our user message
       		     */
       		    phrdText = new HTML("<span class='regblack12'><center>Please enter your CFEC ID number, select the appropriate options, then press <span class='regred12'>Next >></span> to continue</span></center>");
       		    /*
       		     * 	add the three elements to the BottomRight FieldSet
       		     */
       		    topRight.add(phrdText);
       		    topRight.add(id);
       		    topRight.add(NavprogressBarPanel);
       		    /*
       		     * 	enable the next button
       		     */
       		    next.enable();
       		    /*
       		     * 	refresh the TopLeft and BottomRight FieldSets
       		     */
       		    bottomLeft.layout();
       		    topRight.layout();
       		    bottomRight.layout();
       		    next.removeAllListeners();
       		    next.addListener(Events.Select, nextListener);
 			}
        });       
        Button aa = new Button("<span class='regred10'>I am the Authorized Agent</span>");
        aa.setTitle("I am NOT the actual owner. but an authorized representative of the Permits/Vessels I wish to renew");
        aa.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
     	    	timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(time, cfecid.getValue());
     		    final Listener<MessageBoxEvent> l2 = new Listener<MessageBoxEvent>() {
      	    		public void handleEvent(MessageBoxEvent ce) {
      	    			Button btn2 = ce.getButtonClicked();  
      	    			if (btn2.getHtml().toLowerCase().contains("ok")) {
      	    				isAgent = true;
      	    				agentName = ce.getValue().toUpperCase();
      	    				bottomRight.removeAll();
      	    				bottomRight.add(ginit.getOption1(ryear));  
      	    				bottomRight.add(ginit.getOption4(ryear));  
      	    				bottomRight.add(ginit.getOption3(ryear));
      	    				bottomRight.add(ginit.getOption2(ryear));
	      	       		    /*
	      	       		     * 	remove the objects from the BottomRight FieldSet
	      	       		     */
	      	       		    topRight.removeAll();
	      	       		    /*
	      	       		     * 	remove the objects from the TopLeft FieldSet
	      	       		     */
	      	       		    bottomLeftVPanel.removeAll();	      	       		    
	      	       		    /*
	      	       		     * 	add the Start Over button at bottom of TopLeft FieldSet
	      	       		     */
	      	       		    bottomLeftVPanel.add(startOver);	      	       		    
	      	       		    /*
	      	       		     * 	add new instructions
	      	       		     */
	      	       		    bottomLeftVPanel.addText(gins.getOptions());
	      	       		    /*
	      	       		     * 	redefine our user message
	      	       		     */
	      	       		    phrdText = new HTML("<span class='regblack12'><center>Please enter your CFEC ID number, select the appropriate options, then press <span class='regred12'>Next >></span> to continue</span></center>");
	      	       		    /*
	      	       		     * 	add the three elements to the BottomRight FieldSet
	      	       		     */
	      	       		    topRight.add(phrdText);
	      	       		    topRight.add(id);
	      	       		    topRight.add(NavprogressBarPanel);
	      	       		    /*
	      	       		     * 	enable the next button
	      	       		     */
	      	       		    next.enable();
	      	       		    /*
	      	       		     * 	refresh the TopLeft and BottomRight FieldSets
	      	       		     */
	      	       		    bottomLeft.layout();
	      	       		    topRight.layout();
	      	       		    bottomRight.layout();
	      	       		    next.removeAllListeners();
	      	       		    next.addListener(Events.Select, nextListener);
      	    			}
      	    		}  
      	    	}; 
     		    MessageBox box2 = new MessageBox();  
     		    box2.setType(MessageBoxType.PROMPT);
     		    box2.setMinWidth(600);
			    box2.setIcon(MessageBox.INFO);
			    box2.setButtons(MessageBox.OKCANCEL);
			    box2.getDialog().setTitle("Authorized Agent Agreement");
    		    box2.setMessage("<span class='boldblack12'><center>Authorized Agent Agreement" +
    		    		"CFEC Authorized Agent Agreement</center></span><br><span class='regblack12'>A permit or vessel can be renewed using the online renewal " +
    		    		"system by an authorized agent ONLY if the permit holder or vessel owner completes this " +
    		    		"<a href='http://www.cfec.state.ak.us/forms/Agent_Authorization.pdf' target='_blank'>Agent Authorization form.</a> " +
    		    		"This must be completed, signed and notarized by the permit holder or vessel owner to indicate the authorized " +
    		    		"agent. The completed form must be provided to the Entry Commission (details after order processing) or there will be delays in " +
    		    		"processing time.<br><br>If you have further questions please contact the Licensing section at 907-789-6150." +
    		    		"<br><br>By entering your name below and selecting the 'OK' button, you are verifying that you have already " +
    		    		"completed and submitted the authorized agent form for the permit holder whose records you are about to access.<br><br>" +
    		    		"<b><center>Please enter your full name</b> or Cancel to exit.</center></span>");   		    
  	    		box2.addCallback(l2); 
  	    		box2.show(); 
 		    }
        });
        buttonForm.addButton(phorvo);
        buttonForm.addButton(aa);        
        
        /*
         * 	This listener describes what happens after the initial options are selected
         */
        nextListener = new SelectionListener<ButtonEvent>() {
     	   public void componentSelected(ButtonEvent ce) {
     	       if (!(cfecid.getValue() == null)) {    
	     		   if (ginit.getRenewal().getValue().equals(false) && ginit.getForms().getValue().equals(false)) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must select either the online renewal or pre-printed form option (Item 1) ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must select either the online renewal or pre-printed form option (Item 1)</span>", 350);    			   
	     		   } else if (ginit.getPrivacy().getValue().equals(false)) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must agree to and select the Privacy Act Verification box before proceeding (Item 2) ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must agree to and select the Privacy Act Verification box before proceeding (Item 2)</span>", 350);
	     		   } else if (cfecid.getValue().toString().length() < 6 || cfecid.getValue().toString().length() > 6) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must enter a valid CFECID ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must enter a valid CFECID</span>", 250);
	     			   cfecid.focus();
	     		   } else if (ginit.getAres().getValue().equals(false) && ginit.getNres().getValue().equals(false)) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must select your status either as a Resident or Non Resident (Item 2) ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must select your status either as a Resident or Non Resident (Item 2)</span>", 350); 
	     		   } else if (ginit.getAcit().getValue().equals(false) && ginit.getNcit().getValue().equals(false)) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must select your status either as a US Citizen or Non US Citizen (Item 2) ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must select your status either as a US Citizen or Non US Citizen (Item 2)</span>", 350); 
	     		   } //else if (ginit.getAres().getValue().equals(true) && ginit.getNcit().getValue().equals(true)) {
	     			 //  statusBar.setHTML("<span class='regred12'>*** You cannot select Alaska Residency AND Non US Citizenship (Item 2) ***</span>");
	     			 //  gmsg.alert("Requirements not met", "<span class='regred12'>You cannot select Alaska Residency AND Non US Citizenship (Item 2)</span>", 350);
	     		   //} 
     	       	   else if (ginit.getNcit().getValue().equals(true) && ginit.getArn().getValue() == null) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must enter an Alien Registration Number if you select Non US Citizen (Item 2)***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must enter an Alien Registration Number if you select Non US Citizen (Item 2)</span>", 350); 
	     			   ginit.getArn().focus();
	     		   } else if (ginit.getPovyes().getValue().equals(false) && ginit.getPovno().getValue().equals(false)) {
	     			   statusBar.setHTML("<span class='regred12'>*** You must select your Poverty Fee option, Yes or No (Item 4) ***</span>");
	     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must select your Poverty Fee option, Yes or No (Item 4)</span>", 350);    			   
	     		   } 
	     		   
	     		   else {
	     			  gmsg.waitStart("Please Wait", "Getting CFEC Data....", "Progress", 250);
	     			  if (ginit.getRenewal().getValue().equals(true)) {
	     				   Log.info(cfecid.getValue().toUpperCase() + " is attempting to login to LEON");
	     				   Log.info(cfecid.getValue().toUpperCase() + " has browser type of ("+getUserAgent()+")");
	     				   timer.timerCancel();
	     	       	       timer.progReset();
	     	       	       timer.setTimer(time, cfecid.getValue());	     	       	       
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
		    						   final Listener<MessageBoxEvent> l1 = new Listener<MessageBoxEvent>() {
				     		       	    	public void handleEvent(MessageBoxEvent ce) {
				     		       	    		Button btn1 = ce.getButtonClicked();  
				     		       	    		if (btn1.getHtml().toLowerCase().contains("yes")) {
	     			    						   if (entity.getIllegal().equalsIgnoreCase("false")) {
	     			    							   Log.info(cfecid.getValue().toUpperCase() + " ("+entity.getXname()+") has successfully loged into LEON");
	     			    							   if (ginit.getRenewal().getValue().equals(true)) {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the Renewal option");
	     			    							   } else {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the download form(s) option");
	     			    							   }
	     			    							   if (ginit.getAres().getValue().equals(true)) {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the Alaska Residence option");
	     			    								  entity.setResidency("resident");
	     			    							   } else {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the Non Alaska Residence option");
	     			    								  entity.setResidency("nonresident");
	     			    							   }
	     			    							   if (ginit.getAcit().getValue().equals(true)) {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the US Citizen option");
	     			    								  entity.setCitizen("true");
	     			    							   } else {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the Non US Citizen option");
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " Alien Registration Number is " + ginit.getArn().getValue());
	     			    								  entity.setCitizen("false");
	     			    								  entity.setAlienreg(ginit.getArn().getValue());
	     			    							   }
	     			    							   if (ginit.getPovyes().getValue().equals(true)) {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has selected the Poverty Fees option");
	     			    								  entity.setPoverty("true");
	     			    							   } else {
	     			    								  Log.info(cfecid.getValue().toUpperCase() + " has NOT selected the Poverty Fees option");
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
	     				    	    				   if (!(entity.getForeign())) {
	     				    	    					   topLeftText.append("<tr><td align='center'><span class='regred12' id='perm_name'>").append(entity.getXname()).append("</span><br>");
	     					    	    				   topLeftText.append("<span class='regred12' id='perm_address'>").append(entity.getPaddress()).append("</span><br>");
	     					    	    				   topLeftText.append("<span class='regred12' id='perm_city'>").append(entity.getPcity()).append("</span>").append(",&nbsp;");
	     					    	    				   topLeftText.append("<span class='regred12' id='perm_state'>").append(entity.getPstate()).append("</span>").append("&nbsp;");
	     					    	    				   topLeftText.append("<span class='regred12' id='perm_zip'>").append(entity.getPzip()).append("</span>");
	     					    	    				   topLeftText.append("</td></tr></table>");
	     					    	    				   topLeftText.append("</td></tr></table>");			    	    				
	     				    	    				   } else {
	     				    	    					   topLeftText.append("<tr><td align='center'><span class='regred12' id='perm_name'>").append(entity.getXname()).append("</span><br>");
	     					    	    				   topLeftText.append("<span class='regred12' id='perm_address2'>").append(entity.getPaddress2()).append("</span>");
	     					    	    				   topLeftText.append("</td></tr></table>");
	     					    	    				   topLeftText.append("</td></tr></table>");
	     				    	    				   }
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
	     				    	    				   nop.setValue(false);
	     				    						   gadd.address(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, topLeftText.toString(), 
	     				    								   NavprogressBarPanel, entity, service, changeList, timer, payment, feeTotals, plist, vlist, first, second, nop, ryear, recaptchaSiteKey, recaptchaAction);
	     			    						   } else {
	     			    							   statusBar.setHTML("<span class='regred12'>*** The CFECID number you entered cannot participate in this application ***</span>");
	     				    						   gmsg.alert("Requirements not met", "<span class='regred12'>The CFECID number you entered cannot participate in this application</span>", 250);
	     				    						   Log.info("Invalid CFEC ID login attempt using " + cfecid.getValue().toUpperCase());
	     			    						   }		    
				     	      	    			} else {
				     	      	    				if (!(cfecid.getValue() == null)) {
				     	      	        	    		Log.info(cfecid.getValue().toUpperCase() + " has re-initialized the application");
				     	      	        	    	}
				     	      	        	    	/*
				     	      	        	    	 * Reset timer
				     	      	        	    	 */
				     	      	        	    	timer.timerCancel();
					     	      	 	       	    timer.progReset();
					     	      	 	       	    timer.setTimer(time, cfecid.getValue());	       	    
					     	      	 	       	    /*
					     	      	 	       	     * Reset feeTables
					     	      	 	       	     */
					     	      	 	       	    feeTotals = new FeeTotals();	       	    
					     	      	 	       	    /*
					     	      	 	       	     * Reset payment object
					     	      	 	       	     */
					     	      	 	       	    payment = new ArenewPayment();	       	    
					     	      	 	       	    /*
					     	      	 	       	     * Reset change object
					     	      	 	       	     */
				     	      	        	    	changeList = new ArrayList();       	    	
				     	      	        	    	/*
				     	      	        	    	 * Reset entity
				     	      	        	    	 */
				     	      	        	    	entity = null;       	  
				     	      	        	    	/*
				     	      	        	    	 * Reset Agent Name
				     	      	        	    	 */
				     	      	        	    	agentName = null;
				     	      	        	    	/*
				     	      	        	    	 * 	Reset StatusBar
				     	      	        	    	 */
                                                    statusBar.setHTML(buildStatusBarHtml());
                                                    /*
				     	      	        	    	 * Reset reschoice
				     	      	        	    	 */
				     	      	        	    	reschoice = new HTML("<span class='boldred14'>Please note:</span> You current Alaska residency declaration is: <span class='boldblack14'>UNSELECTED</span>");
				     	      	        	    	bottomLeft.removeAll();
				     	      	        	    	bottomLeft.setAutoHeight(true);
					     	      	     	    	bottomLeftVPanel.removeAll();
					     	      	     	    	/*
					     	      	     	    	 * 	get the initial instructions
					     	      	     	    	 */
					     	      	     	    	bottomLeftVPanel.addText(gins.getInit());
					     	      	     	    	/*
					     	      	     	    	 * 	remove all objects from BottomRight and TopRight FieldSets
					     	      	     	    	 */
					     	      	     	    	topRight.removeAll();
					     	      	     	    	bottomRight.removeAll();
					     	      	     	    	topLeft.removeAll();
					     	      	     	    	topRight.setAutoHeight(true);
					     	      	     	    	topLeft.setHeight(115); 
					     	      	     	    	bottomRight.setAutoHeight(true);
					     	      	     	    	topLeft.addText("<table width='100%' bgcolor='#FFFFCC' border='0' cellspacing ='0'><tr><td><br><p><center>" +
					     	      	         		"<span class='boldblack12'>No Permit Holder or Vessel Owner queried</span></center></p></td></tr></table>");
					     	      	     	    	/*
					     	      	     	    	 * 	redefine user message
					     	      	     	    	 */
					     	      	     	    	phrdText = new HTML("<span class='regblack12'><center>Please declare if you are the actual Permit Holder/Vessel Owner or Authorized Agent below</center></span>");
					     	      	     	    	/*
					     	      	     	    	 * 	add the new elements to BottomRight and TopRight FieldSets
					     	      	     	    	 */
					     	      	     	    	NavprogressBarPanel = new HorizontalPanel();
					     	      	     	        NavprogressBarPanel.setTableWidth("100%");
					     	      	     	        NavprogressBarPanel.setAutoHeight(true);
					     	      	     	        NavprogressBarPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
					     	      	     	        last = new Button("<span class='regred10'><< Last</span>");
					     	      	     	        last.disable();
					     	      	     	        NavprogressBarPanel.add(last);
					     	      	     	        NavprogressBarPanel.addText(gpro.getPro());
					     	      	     	        next = new Button("<span class='regred10'>Next >></span>");
					     	      	     	        next.disable();
					     	      	     	        NavprogressBarPanel.add(next);
					     	      	     	    	topRight.add(phrdText);
					     	      	     	    	topRight.add(buttonForm);
					     	      	     	    	topRight.add(NavprogressBarPanel);
					     	      	     	        /*
					     	      	     	         * 	refresh the TopLeft and BottomRight FieldSets and the TopLeftVertical Panel
					     	      	     	         */
					     	      	     	        bottomLeft.add(bottomLeftVPanel);
					     	      	     	        bottomLeft.layout();
					     	      	     	        bottomLeftVPanel.layout();
					     	      	     	        bottomRight.setHeadingHtml("<span class='boldblack12'>LEON Initial Options</span>");
					     	      	      	    	bottomRight.layout();
					     	      	      	    	topRight.setHeadingHtml("<span class='boldblack12'>Status, Fee Totals and Navigation</span>");
					     	      	      	    	topRight.layout();
					     	      	      	    	topLeft.layout();
					     	      	         	    /*
					     	      	         	     * 	disable and clear the initial user options
					     	      	         	     */
					     	      	      		    next.removeAllListeners();
					     	      	      		    next.addListener(Events.Select, nextListener);
					     	      	      		    next.disable();
					     	      	      		    last.removeAllListeners();
					     	      	      		    last.disable();
					     	      	      		    cfecid.enable();
					     	      	      		    cfecid.setAllowBlank(true);
					     	      	      		    cfecid.setValue(null);
					     	      	      		    cfecid.setEmptyText("Enter your CFEC ID Number here"); 
					     	      	      		    cfecid.setAllowBlank(false);
					     	      	      		    /*
					     	      	      		     * 	set the progress bar back to default
					     	      	      		     */
					     	      	      		    DOM.getElementById("progressBar1").getStyle().setProperty("bgcolor", "#DCDCDC");
					     	      	      		    DOM.getElementById("progressBar2").getStyle().setProperty("bgcolor", "White");
					     	      	      		    DOM.getElementById("progressBar3").getStyle().setProperty("bgcolor", "White");
					     	      	      		    DOM.getElementById("progressBar4").getStyle().setProperty("bgcolor", "White");
					     	      	      		    DOM.getElementById("progressBar5").getStyle().setProperty("bgcolor", "White");
					     	      	      		    DOM.getElementById("progressBar6").getStyle().setProperty("bgcolor", "White");
				     	      	    			}
				     		       	    	}
			    					   };		    					   
									   MessageBox box1 = new MessageBox();  					
									   if (entity.getForeign()) {
										   box1.setMinWidth(600);
										   box1.setButtons(MessageBox.YESNO);
										   box1.setIcon(MessageBox.INFO);
										   box1.getDialog().setTitle("Permit/Vessel Owner confirmation");
					     		    	   box1.setMessage("<span class='boldblack12'><center>Once you have read the instructions, please continue</center></span><br>" +
						     	  		    		"<span class='regblack12'>By clicking the 'Yes' button below, you are certifying that " +
						     	  		    		"you are the permit holder, or named agent, of the permit holder below and you swear, under penalty of perjury, that the information provided is true.<br><br>" +
						     	  		    		"For Agents,the Agent Authorization form must be completed by the permit holder and submitted to our office before the renewal can be processed." +
						     	  		    		"<font color='red'><center>"+entity.getXname()+"<br>"+entity.getPaddress2()+"</center></font><br><br>" +
						     	  		    		"If you are NOT the above named person or their authorized agent</b>, you must reset the application by clicking the 'No' button.<br><br><i><b>" +
						     	  		    		"I understand that making a false claim or submitting false documentation is a crime</b> " +
						     	  		    		"under AS 11.56.210 which is punishable by up to one year in prison and/or $5,000 fine, and may subject me to administrative fines, suspension " +
						     	  		    		"of fishing privileges and revocation of any entry permits I may hold.</i><br><br><i>The Entry Commission regulations; 20 AAC 05.560 (a) " +
						     	  		    		"Application for annual renewal of an entry permit must be made to the commission on a form provided by the commission. If the renewal form " +
						     	  		    		"is submitted by mail or facsimile, <b>the renewal form must be completed and signed by the permit holder or by an authorized agent</b> in " +
						     	  		    		"accordance with 20 AAC 05.1960. An applicant may submit an application to renew an entry permit on the online licensing system at the " +
						     	  		    		"commissions website. The total annual fee for the permit or permits being renewed must be submitted with the application.</i></span>");
										   box1.addCallback(l1); 
					     		    	   box1.show();   
									   } else {
										   box1.setMinWidth(600);
										   box1.setButtons(MessageBox.YESNO);
										   box1.setIcon(MessageBox.INFO);
										   box1.getDialog().setTitle("Permit/Vessel Owner confirmation");
					     		    	   box1.setMessage("<span class='boldblack12'><center>Once you have read the instructions, please continue</center></span><br>" +
						     	  		    		"<span class='regblack12'>By clicking the 'Yes' button below, you are certifying that " +
						     	  		    		"you are the permit holder, or named agent, of the permit holder below and you swear, under penalty of perjury, that the information provided is true.<br><br>" +
						     	  		    		"For Agents,the Agent Authorization form must be completed by the permit holder and submitted to our office before the renewal can be processed." +
						     	  		    		"<font color='red'><center>"+entity.getXname()+"<br>"+entity.getPaddress()+"<br>"+entity.getPcity()+", "+entity.getPstate()+". "+entity.getPzip()+"</center></font><br><br>" +
						     	  		    		"If you are NOT the above named person or their authorized agent</b>, you must reset the application by clicking the 'No' button.<br><br><i><b>" +
						     	  		    		"I understand that making a false claim or submitting false documentation is a crime</b> " +
						     	  		    		"under AS 11.56.210 which is punishable by up to one year in prison and/or $5,000 fine, and may subject me to administrative fines, suspension " +
						     	  		    		"of fishing privileges and revocation of any entry permits I may hold.</i><br><br><i>The Entry Commission regulations; 20 AAC 05.560 (a) " +
						     	  		    		"Application for annual renewal of an entry permit must be made to the commission on a form provided by the commission. If the renewal form " +
						     	  		    		"is submitted by mail or facsimile, <b>the renewal form must be completed and signed by the permit holder or by an authorized agent</b> in " +
						     	  		    		"accordance with 20 AAC 05.1960. An applicant may submit an application to renew an entry permit on the online licensing system at the " +
						     	  		    		"commissions website. The total annual fee for the permit or permits being renewed must be submitted with the application.</i></span>");
										   box1.addCallback(l1); 
					     		    	   box1.show();   
									   }  
		    					   } else {
		    						   statusBar.setHTML("<span class='regred12'>*** The CFECID number you entered is invalid, try again ***</span>");
		     						   gmsg.alert("Invalid CFECID number", "<span class='regred12'>The CFECID number you entered is invalid. Please try again</span>", 250);
		     						   Log.info("Invalid CFEC ID login attempt using " + cfecid);
		    					   }		    					    					   
		    				   }
		    			   });	     	       	        	     			   	   
	     			   } else {
	     				    Log.info(cfecid.getValue().toUpperCase() + " is attempting to login to LEON");
	     				    Log.info(cfecid.getValue().toUpperCase() + " has browser type of ("+getUserAgent()+")");
	     				    timer.timerCancel();
	     	       	    	timer.progReset();
	     	       	    	timer.setTimer(time, cfecid.getValue());
	     				    service.getForms(cfecid.getValue().toString().toUpperCase(), ryear, new AsyncCallback<List<String>>() {
	     					   @Override
								public void onFailure(Throwable caught) {
	     						   statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
		    					   gmsg.alert("Communication Error", gins.getTech(), 350);
								}
								@Override
								public void onSuccess(final List<String> result) {
									gmsg.waitStop();	
									if (result.get(0).equalsIgnoreCase("invalid")) {
										statusBar.setHTML("<span class='regred12'>*** The CFEC ID number you entered is invalid (6 numeric characters only) or illegal, try again ***</span>");
			    						gmsg.alert("Requirements not met", "<span class='regred12'>The CFEC ID number you entered is invalid (6 numeric characters only) or illegal, try again</span>", 250);
			    						Log.info("Invalid CFEC ID login attempt using " + cfecid.getValue().toUpperCase());
									} else {
										nop.setValue(false);
										//Log.info(cfecid.getValue().toUpperCase() + " has successfully loged into LEON");
										if (ginit.getForms().getValue().equals(true)) {
											Log.info(cfecid.getValue().toUpperCase() + " has selected the Download Forms option");
										}
										ScreenForms form = new ScreenForms();
					     				form.forms(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, NavprogressBarPanel, service, result, 
					     					timer, ryear, isAgent, changeList, payment, feeTotals, plist, vlist, agentName, first, second, nop, ginit, recaptchaSiteKey, recaptchaAction);
									}									
								}     					   
	     				   });
	     			   }
	     		   }
     	       } else {
     			   statusBar.setHTML("<span class='regred12'>*** You must enter a valid CFEC ID ***</span>");
     			   gmsg.alert("Requirements not met", "<span class='regred12'>You must enter a valid CFEC ID</span>", 250);
     			   cfecid.focus();
     		   }
     	   }        
        };  
        
        /*
         * 	Now we had the HTML, Progress Bar and Button Bar for initial screen options
         */
        topRight.add(phrdText);
        topRight.add(buttonForm); 
        topRight.add(NavprogressBarPanel);
	}
	
	/*
	 * 	Method to capture user OS/Browser information
	 */
	public native static String getUserAgent() /*-{
    	return $wnd.navigator.userAgent.toLowerCase();
    }-*/;
	
}


