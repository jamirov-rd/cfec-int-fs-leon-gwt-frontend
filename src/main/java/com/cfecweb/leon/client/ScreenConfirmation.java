package com.cfecweb.leon.client;

import java.util.ArrayList;
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
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;

/*
 * If the user gets here, they will receive a basic confirmation screen listing the permits/vessels they wish to renew/license with
 * a small text summary including total cost about to be billed. If for whatever reason their credit card does not pass for purposes 
 * of available funds, they will receive an appropriate message.
 * For valid transactions, the user to simply given an opportunity to review their pending purchase and confirm.
 */
public class ScreenConfirmation {
	InstructionsText gins = new InstructionsText();
	Grid<BaseModel> pgrid = null;
	Grid<BaseModel> vgrid = null;
	ContentPanel cp = null;
	ContentPanel cv = null;
	List<ArenewPermits> pfinal = null;
    List<ArenewVessels> vfinal = null;
    List<ArenewPermits> pclist = null;
	List<ArenewVessels> vclist = null;
	boolean halred = false;
    boolean sabred = false;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void getConfirm(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight, final FieldSet topRight, final Button startOver,
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText, final String pmtvesCount,
			final HorizontalPanel NavprogressBarPanel, final ArenewEntity entity, final List<ArenewChanges> changeList,
			final FeeTotals feeTotals, final getDataAsync service,
			final String topLeftText, final boolean firstTime, final SessionTimer timer, final ArenewPayment payment,
			final List<ArenewPermits> plist, final List<ArenewVessels> vlist, final CheckBox first, final CheckBox second, final CheckBox nop,
			final String ryear, final String reCaptchaSiteKey, final String reCaptchaAction) {
		Log.info(entity.getId().getCfecid() + " has navigated to the Confirmation Panel");
		bottomRight.removeAll();
		bottomLeftVPanel.removeAll();
		bottomLeftVPanel.add(startOver);
		bottomLeftVPanel.addText(gins.getConfirm());
	    bottomLeftVPanel.layout();
	    cfecid.disable();	 
	    DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar2").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar3").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar4").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar5").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar6").getStyle().setProperty("background", "#DCDCDC");
	    /*
	     * 	Define the NEXT Button
	     */
	    next.removeAllListeners();
        next.setEnabled(true);
        next.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                timer.timerCancel();
                timer.progReset();
                timer.setTimer(timer.getTime(), cfecid.getValue().toString());
                for (Iterator<ArenewPermits> p = plist.iterator(); p.hasNext();) {
                    ArenewPermits permit = (ArenewPermits) p.next();
                    if (permit.getStatus().equalsIgnoreCase("Available")) {
                        if (permit.getRenewed()) {
                            if (permit.getIntend()) {
                                permit.setIntends("true");
                            } else {
                                permit.setIntends("false");
                            }
                            if (permit.getNointend()) {
                                permit.setNointends("true");
                            } else {
                                permit.setNointends("false");
                            }
                            permit.setStatus("Pending");
                            permit.setNewrenew(true);
                            if (permit.getId().getFishery().equalsIgnoreCase("B 06B") || permit.getId().getFishery().equalsIgnoreCase("B 61B")) {
                                if (permit.getReducedfee()) {
                                    halred = true;
                                }
                            } else if (permit.getId().getFishery().equalsIgnoreCase("C 06B") || permit.getId().getFishery().equalsIgnoreCase("C 61B")
                                    || permit.getId().getFishery().equalsIgnoreCase("C 09B") || permit.getId().getFishery().equalsIgnoreCase("C 91B")) {
                                if (permit.getReducedfee()) {
                                    sabred = true;
                                }
                            }
                        }
                    }
                }
                for (Iterator<ArenewVessels> v = vlist.iterator(); v.hasNext();) {
                    ArenewVessels vessel = (ArenewVessels) v.next();
                    if (vessel.getStatus().equalsIgnoreCase("Available")) {
                        if (vessel.getRenewed()) {
                            vessel.setStatus("Pending");
                            vessel.setNewrenew(true);
                        }
                    }
                }
                ScreenProcessing proc = new ScreenProcessing();
                proc.getConfirm(bottomLeftVPanel, bottomRight, startOver, cfecid, next, last, statusBar, pmtvesCount, entity, changeList,
                        feeTotals, service, topLeftText, firstTime, payment, plist, vlist, pclist, vclist, ryear, halred, sabred, reCaptchaSiteKey, reCaptchaAction);
            }
        });
	    /*
	     * 	Define the LAST Button
	     */
	    last.removeAllListeners();
	    last.setEnabled(true);
	    last.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
				ScreenBilling bill = new ScreenBilling();
				bill.getBilling(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount, NavprogressBarPanel, entity, changeList, 
						feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
			}	    	
	    });	    
	    statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal final Confirmation Section</span>");
	    pfinal = new ArrayList();
	    vfinal = new ArrayList();
	    pclist = new ArrayList();
	    vclist = new ArrayList();
	    pclist.addAll(plist);
	    vclist.addAll(vlist);	    
	    Log.info(entity.getId().getCfecid() + " plist size is " + plist.size());
	    Log.info(entity.getId().getCfecid() + " vlist size is " + vlist.size());
	    for (Iterator<ArenewPermits> p = plist.iterator(); p.hasNext();) {
	    	ArenewPermits permit = (ArenewPermits) p.next();
	    	/*Log.info("permit " + permit.getId().getSerial());
	    	Log.info("permit " + permit.getStatus());
	    	Log.info("permit " + permit.isAlready());
	    	Log.info("permit " + permit.isNewrenew());
	    	Log.info(" ");*/
	    	if (permit.getStatus().equalsIgnoreCase("Available") || permit.getNewrenew()) {
	    		if (permit.getRenewed()) {
	    			/*Log.info("fishery 2 " + permit.getId().getFishery());
	    			Log.info("Halibut? 2 " + permit.isHalibut());
	    			Log.info("Sablefish? 2 " + permit.isSablefish());
	    			Log.info("reduced? 2 " + permit.isReducedfee());*/
	    			if (permit.getIntend()) {
	    				permit.setIntendString("Yes");
	    			} else {
	    				permit.setIntendString("No");
	    			}
	    			if (permit.getId().getFishery().equalsIgnoreCase("B 06B") || permit.getId().getFishery().equalsIgnoreCase("B 61B")) {
	    				if (permit.getReducedfee()) {
	    					halred = true;
	    				} 	    				
 	    			} else if (permit.getId().getFishery().equalsIgnoreCase("C 06B") || permit.getId().getFishery().equalsIgnoreCase("C 61B") 
 	    					|| permit.getId().getFishery().equalsIgnoreCase("C 09B") || permit.getId().getFishery().equalsIgnoreCase("C 91B")) {
 	    				if (permit.getReducedfee()) {
	    					sabred = true;
	    				} 
 	    			}
	    			pfinal.add(permit);
	    		}
	    	}
	    }
	    for (Iterator<ArenewVessels> v = vlist.iterator(); v.hasNext();) {
	    	ArenewVessels vessel = (ArenewVessels) v.next();
	    	if (vessel.getStatus().equalsIgnoreCase("Available") || vessel.getNewrenew()) {
	    		Log.info(entity.getId().getCfecid() + " vessel status is " + vessel.getStatus());
	    		Log.info(entity.getId().getCfecid() + " vessel isNewrenew is " + vessel.getNewrenew());
	    		Log.info(entity.getId().getCfecid() + " vessel isNewVessel is " + vessel.getNewVessel());
	    		if (vessel.getRenewed()) {
	    			vfinal.add(vessel);
	    		}
	    	}
	    }
	    if (pfinal.size() > 0) {
	    	MemoryProxy pproxy = new MemoryProxy(pfinal);
		    BeanModelReader preader = new BeanModelReader();
		    final BaseListLoader ploader = new BaseListLoader(pproxy, preader);
		    final ListStore<BaseModel> pstore = new ListStore<BaseModel>(ploader);	
		    List<ColumnConfig> pconfigs = new ArrayList<ColumnConfig>();
		    ColumnConfig p1 = new ColumnConfig("id.serial", "<center><span class='regblue12'>Serial Number</span></center>", 100);
		    p1.setAlignment(HorizontalAlignment.CENTER);
		    pconfigs.add(p1);
		    ColumnConfig p2 = new ColumnConfig("id.fishery", "<center>Fishery</center>", 100);
		    p2.setAlignment(HorizontalAlignment.CENTER);
		    pconfigs.add(p2);
		    ColumnConfig p3 = new ColumnConfig("intendString", "<center>Intend to Fish</center>", 100);
		    p3.setAlignment(HorizontalAlignment.CENTER);
		    pconfigs.add(p3);
		    //ColumnConfig p4 = new ColumnConfig("id.ryear", "<center>Renewal Year</center>", 100);
		    //p4.setAlignment(HorizontalAlignment.CENTER);
		    //pconfigs.add(p4);
		    ColumnConfig p4 = new ColumnConfig("id.pyear", "<center>Permit Year</center>", 100);
		    p4.setAlignment(HorizontalAlignment.CENTER);
		    pconfigs.add(p4);
		    ColumnConfig p5 = new ColumnConfig("adfg", "<center>ADFG Number</center>", 100);
		    p5.setAlignment(HorizontalAlignment.CENTER);
		    pconfigs.add(p5);
		    //ColumnConfig p6 = new ColumnConfig("vlicensed", "<center>Vessel Licensed</center>", 100);
		    //p6.setAlignment(HorizontalAlignment.CENTER);
		    //p6.setStyle("font: bold 12px Arial, Helvetica, Tahoma, sans-serif; color:black;");
		    //pconfigs.add(p6);
		    ColumnConfig p6 = new ColumnConfig("fee", "<center>Fee</center>", 100);
		    p6.setAlignment(HorizontalAlignment.CENTER);
		    p6.setNumberFormat(NumberFormat.getCurrencyFormat());
		    pconfigs.add(p6);
		    ColumnModel pcm = new ColumnModel(pconfigs);  
		    pgrid = new Grid<BaseModel>(pstore, pcm);
	        pgrid.setBorders(true);  
	        pgrid.setAutoHeight(true);
		    pgrid.getView().setForceFit(true);
		    pgrid.setStyleName("tableGrids");
		    pgrid.setLoadMask(true);
		    pgrid.addListener(Events.Attach, new Listener<ComponentEvent>() {
	      	  public void handleEvent(ComponentEvent be) {
	      	    DeferredCommand.addCommand(new Command() {
	      	      public void execute() {
	      	        ploader.load();
	      	      }
	      	    });  
	      	  };
	      	});
		    cp = new ContentPanel();  
		    cp.setHeadingHtml("<center><span class='regblack12'>Your Permits selected for renewal</span></center>");
		    cp.setFrame(true);
		    cp.setAutoHeight(true);
		    cp.setAutoWidth(true);
		    cp.setLayout(new FitLayout());
		    cp.setStyleAttribute("padding-bottom", "5px");
		    cp.add(pgrid);
	    }
	    if (vfinal.size() > 0) {
	    	MemoryProxy vproxy = new MemoryProxy(vfinal);
		    BeanModelReader vreader = new BeanModelReader();
		    final BaseListLoader vloader = new BaseListLoader(vproxy, vreader);
		    final ListStore<BaseModel> vstore = new ListStore<BaseModel>(vloader);	
		    List<ColumnConfig> vconfigs = new ArrayList<ColumnConfig>();
		    ColumnConfig v1 = new ColumnConfig("name", "<center><span class='regblue12'>Vessel Name</span></center>", 100);  
		    v1.setAlignment(HorizontalAlignment.CENTER);
		    vconfigs.add(v1);
		    ColumnConfig v2 = new ColumnConfig("regNum", "<center>Registration Number</center>", 100);
		    v2.setAlignment(HorizontalAlignment.CENTER);
		    vconfigs.add(v2);
		    ColumnConfig v3 = new ColumnConfig("yearBuilt", "<center>Year Built</center>", 100);
		    v3.setAlignment(HorizontalAlignment.CENTER);
		    vconfigs.add(v3);
		    ColumnConfig v4 = new ColumnConfig("id.ryear", "<center>License Year</center>", 100);
		    v4.setAlignment(HorizontalAlignment.CENTER);
		    vconfigs.add(v4);
		    ColumnConfig v5 = new ColumnConfig("id.adfg", "<center>ADFG Number</center>", 100);
		    v5.setAlignment(HorizontalAlignment.CENTER);
		    vconfigs.add(v5);
		    ColumnConfig v6 = new ColumnConfig("fee", "<center>Fee</center>", 100);
		    v6.setAlignment(HorizontalAlignment.CENTER);
		    v6.setNumberFormat(NumberFormat.getCurrencyFormat());
		    vconfigs.add(v6);
		    ColumnModel vcm = new ColumnModel(vconfigs);  
		    vgrid = new Grid<BaseModel>(vstore, vcm);
	        vgrid.setBorders(true);  
	        vgrid.setAutoHeight(true);
		    vgrid.getView().setForceFit(true);
		    vgrid.setStyleName("tableGrids");
		    vgrid.setLoadMask(true);
		    vgrid.addListener(Events.Attach, new Listener<ComponentEvent>() {
	      	  public void handleEvent(ComponentEvent be) {
	      	    DeferredCommand.addCommand(new Command() {
	      	      public void execute() {
	      	        vloader.load();
	      	      }
	      	    });  
	      	  };
	      	});
		    cv = new ContentPanel();  
		    cv.setHeadingHtml("<center><span class='regblack12'>Your Vessels selected to license</span></center>");
		    cv.setFrame(true);
		    cv.setAutoHeight(true);
		    cv.setAutoWidth(true);
		    cv.setLayout(new FitLayout());
		    cv.setStyleAttribute("padding-bottom", "5px");
		    cv.add(vgrid);
	    }
	    bottomRight.setHeadingHtml("<span class='boldblack12'>Final Confirmation Panel</span>");
	    if (pfinal.size() > 0) {
	    	bottomRight.add(cp);
	    }
	    if (vfinal.size() > 0) {
	    	bottomRight.add(cv);
	    }
	    if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
	    	entity.setDiffamount("0.0");	    	
	    } else {
	    	entity.setDiffamount(Double.toString(feeTotals.getNonresDifferential()));
	    }
        StringBuffer con = new StringBuffer("<center><span class='regblack12'>Pressing the </span><span class='regred12'>Next >></span> " +
                "<span class='regblack12'>button will redirect you to a secure hosted checkout page to complete your payment for a " +
                "total of <b>$"+payment.getTotalamount()+"</b>.<br>" +
                "If additional information is required, CFEC will NOT process this order " +
                "until all validating and/or supporting documentation<br>is received. For all other orders, " +
                "CFEC will process them within 5 business days for permits and/or vessels listed above.<br>Any additional fees, such as " +
                "shipping and/or non-resident differential cost(s), are included in the the <b>$"+payment.getTotalamount()+"</b> total fee.<br>" +
                "<br><b>PLEASE</b> ensure that the information above accurately reflects your selections before proceeding.");
        con.append("</span></center>");
        bottomRight.addText(con.toString());
		bottomRight.layout();
	}
}
