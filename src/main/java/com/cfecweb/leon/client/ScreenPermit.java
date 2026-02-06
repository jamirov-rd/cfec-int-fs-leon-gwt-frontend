package com.cfecweb.leon.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.icons.ClientResources;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

@SuppressWarnings({"rawtypes"})
public class ScreenPermit {
	InstructionsText gins = new InstructionsText();
	ProgressbarText gpro = new ProgressbarText();
	PmtVesSelectFunctions getPmt = new PmtVesSelectFunctions();
	UserMessages gmsg = new UserMessages();
	Boolean cont = true;
	boolean found = false;
    boolean inList = false;
    boolean unknown = false;
    int icnt = 0;
	ChangeRecorder cr = new ChangeRecorder();
	EditorGrid<BaseModel> grid = null;
	NewPermitBuilder np = new NewPermitBuilder();
	TextField<String> padfg = null;
	TextField<String> pfishery = null;
	List<ArenewPermits> vresults = null;
	boolean vfound = false;
	
	/*
	 * This is the permit renewal UI. It is basically a grid displaying persons current permits and renew status. Options exist here
	 * to purchase new IEP permits as well. When a selection is made on the grid, the PmtVesSelectFunctions class is called to sort
	 * everything out. There are a ton of rules and conditions that need to be checked before allowing a permit to be renewed or intend
	 * to fish, such as MSNA, etc.
	 */
	@SuppressWarnings("unused")
	public void getPermits(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight, final FieldSet topRight, final Button startOver, 
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText, final String pmtvesCount, 
			final HorizontalPanel NavprogressBarPanel, final ArenewEntity entity, final List<ArenewChanges> changeList, final FeeTotals feeTotals,
			final getDataAsync service, final String topLeftText, final boolean firstTime, final SessionTimer timer, final ArenewPayment payment, 
			final List<ArenewPermits> plist, final List<ArenewVessels> vlist, final CheckBox first, final CheckBox second, final CheckBox nop, final String ryear,
            final String reCaptchaSiteKey, final String reCaptchaAction) {
		/*
		 * Define environment for permit page
		 */
		Log.info(entity.getId().getCfecid() + " has navigated to the Permit Panel");
		bottomRight.removeAll();
		bottomLeftVPanel.removeAll();
		bottomLeftVPanel.add(startOver);
		bottomLeftVPanel.addText(gins.getPermits());
	    bottomLeftVPanel.layout();
	    cfecid.disable();	 
	    statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Permit Selection Section</span>");	
	    DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar2").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar3").getStyle().setProperty("background", "#FFFFCC");
	    DOM.getElementById("progressBar4").getStyle().setProperty("background", "#DCDCDC");
	    DOM.getElementById("progressBar5").getStyle().setProperty("background", "White");
	    DOM.getElementById("progressBar6").getStyle().setProperty("background", "White");
	    /*
	     * 	Define the NEXT Button
	     */
	    next.removeAllListeners();
	    next.setEnabled(true);
	    next.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
	    	@SuppressWarnings("unchecked")
	    	public void componentSelected(ButtonEvent ce) {
	    		timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
	    		/*
	    		 * The following code iterates through the permit list checking for illegal ADFG numbers.
	    		 * It only checks those that meet the following requirements:
	    		 * 1. NOT in gear codes (04, 08, 12, 21, 02, 23, 99)
	    		 * 2. Permit IS available (hasn't already been renewed)
	    		 * 3. Permit IS currently checked for renewal
	    		 * 4. Permit IS currently checked for intent
	    		 * 5. Permit IS in the current year
	    		 * If those conditions have been met and the permit ADFG is found to be N/A, then issue
	    		 * a popup message to the user to change it and do not proceed to next screen. The editing
	    		 * box provides the necessary regular expression for validation.
	    		 */
	       	    entity.setManual(false);
	       	    icnt = 0;
	       	    unknown = false;
	       	    for (Iterator<ArenewPermits> it = plist.iterator(); it.hasNext();) {
	    			final ArenewPermits permit = (ArenewPermits) it.next();
	    			String gcode = permit.getId().getFishery().substring(2, 4);
	    			if (!( gcode.equalsIgnoreCase("04") || gcode.equalsIgnoreCase("08") || 
	    				   gcode.equalsIgnoreCase("12") || gcode.equalsIgnoreCase("21") || 
	    				   gcode.equalsIgnoreCase("02") || gcode.equalsIgnoreCase("18") || 
	    				   gcode.equalsIgnoreCase("23") || gcode.equalsIgnoreCase("99") )) {
		    			if (permit.getStatus().equalsIgnoreCase("Available")) {
		    				if (permit.getRenewed()) {
		    					if (permit.getIntend()) {
		    						icnt = icnt + 1;
			    					if (permit.getAdfg().equalsIgnoreCase("N/A")) {
			    						if (permit.getId().getPyear().equalsIgnoreCase(entity.getId().getRyear())) {
			    							cont = false;
					    					gmsg.alert("<span class='regred12'>Invalid ADFG Number</span>", "The ADFG Number for " +
					    							"fishery " + permit.getId().getFishery() + ", year " +entity.getId().getRyear()+ ", is not valid. Please edit the " +
					    							"ADFG # column for this record and enter a valid ADFG number for " +
					    							"the vessel that you will be using.", 300);
						    				break;
			    						}
			    					} else {
			    						Log.info(entity.getId().getCfecid() + " Permit "+permit.getId().getSerial()+" requires a vessel, "+permit.getAdfg()+" listed. Check local first");
			    						if (vlist.size() > 0) {
			    							for (Iterator<ArenewVessels> v = vlist.iterator(); v.hasNext();) {
				    							ArenewVessels vessel = (ArenewVessels) v.next();
				    					    	if (vessel.getId().getAdfg().equalsIgnoreCase(permit.getAdfg())) {
				    					    		if (vessel.getStatus().equalsIgnoreCase("Available") || vessel.getNewrenew()) {
				    					    			if (vessel.getRenewed()) {
					    					    			Log.info(entity.getId().getCfecid() + " vessel " + permit.getAdfg() + " exists in current inventory and is set for renewal");
					    					    			found = true;
					    					    			inList = true;
					    					    			permit.setVlicensed("YES");			
					    					    			break;
					    					    		} else {
					    					    			Log.info(entity.getId().getCfecid() + " vessel " + permit.getAdfg() + " exists in current inventory and is NOT set for renewal");
					    					    			found = false;
					    					    			inList = true;
					    					    			permit.setVlicensed("NO");
					    					    			entity.setManual(true);
					    					    			break;
					    					    		}
				    					    		} else if (vessel.getStatus().equalsIgnoreCase("Completed")) {
					    					    		Log.info(entity.getId().getCfecid() + " vessel " + permit.getAdfg() + " exists in current inventory and is already renewed");
										    			found = true;
										    			inList = true;
										    			permit.setVlicensed("YES");
										    			break;
						    					    } else if (vessel.getStatus().equalsIgnoreCase("Pending")) {
						    					    	Log.info(entity.getId().getCfecid() + " vessel " + permit.getAdfg() + " exists in current inventory and is pending renewal");
										    			found = true;
										    			inList = true;
										    			permit.setVlicensed("YES");
										    			break;
						    					    } 
				    					    	} else {
				    					    		/*
				    					    		 * didn't pick this up in the current iteration, could still be there, should continue through loop
				    					    		 */
				    					    		Log.info(entity.getId().getCfecid() + " This is not the vessel, are there more?");
									    			found = false;
									    			inList = false;
									    			permit.setVlicensed("remote");
									    			/*
									    			 * Just in case this P-ADFG number is not picked up in the list, give it a status of unknown and let the server component do the work
									    			 */
									    			unknown = true;
				    					    	}
				    						} 
			    						} else {
			    							Log.info(entity.getId().getCfecid() + " vessel " + permit.getAdfg() + " does not exist in current inventory cause there is none, don't know license status");
							    			found = false;
							    			inList = false;
							    			permit.setVlicensed("remote");
							    			entity.setManual(true);
							    			/*
							    			 * There is no permit list, check vessel validity anyway
							    			 */
							    			unknown = true;
			    						}			    						   						
			    					}			    					
		    					} else {
		    						permit.setVlicensed("nointent");
		    					}
		    				} else {
		    					permit.setVlicensed("notrenewed");
		    				}
		    			} else {
		    				permit.setVlicensed("notavailable");
		    			}
	    			} else {
	    				permit.setVlicensed("N/A");
	    				/*
	    				 * still need to check for availability, renewal status and intent
	    				 */
	    				if (permit.getStatus().equalsIgnoreCase("Available")) {
		    				if (permit.getRenewed()) {
		    					if (permit.getIntend()) {
		    						icnt = icnt + 1;
		    					}
		    				}
	    				}
	    			}
	    	    }
	       	    Log.info(entity.getId().getCfecid() + " end permit/vessel loop. Did we have permits with intent?");
	       	    if (icnt > 0) {
	       	    	Log.info(entity.getId().getCfecid() + " Yes, we found " + icnt + " permits with intent to fish");
	       	    	//Log.info(entity.getId().getCfecid() + " manual is " + entity.isManual());
		       	    //Log.info(entity.getId().getCfecid() + " found is " + found);
					//Log.info(entity.getId().getCfecid() + " inList is " + inList);
					Log.info(entity.getId().getCfecid() + " cont is " + cont);
	       	    } else {
	       	    	Log.info(entity.getId().getCfecid() + " No, we have 0 permits with intent to fish");
	       	    	//Log.info(entity.getId().getCfecid() + " manual is " + entity.isManual());
		       	    //Log.info(entity.getId().getCfecid() + " found is " + found);
					//Log.info(entity.getId().getCfecid() + " inList is " + inList);
					Log.info(entity.getId().getCfecid() + " cont is " + cont);
	       	    }
	    		/*
	    		 * 	if boolean cont is true, then proceed. If not, reset it and try again.
	    		 */
	       	    if (cont) {
	       	    	if (icnt > 0) {
		       	    	if (unknown) {
		       	    		gmsg.waitStart("Please Wait", "Verifying Current Vessel Status ....", "Progress", 250);
		       	    		/*
		       	    		 *  send a string array of permit data (serial, adfg, ryear and license status of blank
		       	    		 *  get back the same string array with status either true or false
		       	    		 *  iterate through list and set manual (pending status) as necessary
		       	    		 *  possibly annotate with vessel adfg on with permit is not renewed?
		       	    		 */
							service.checkVessel(plist, ryear, new AsyncCallback() {
								@Override
								public void onFailure(Throwable caught) {
									gmsg.waitStop();
									caught.printStackTrace();
									Log.info(entity.getId().getCfecid() + " threw error while checking for validity");
								}
								@Override
								public void onSuccess(final Object result) {
									gmsg.waitStop();
									vresults = (List<ArenewPermits>) result;
									/*
									 * Iterate through results, set the manual flag for vessels that are 
									 * not CURRENTLY licensed OR in the LEON queue(s) to be licensed. 
									 */
									for (Iterator<ArenewPermits> x = vresults.iterator(); x.hasNext();) {
						    			final ArenewPermits permit = (ArenewPermits) x.next();
						    			if (permit.getVlicensed().equalsIgnoreCase("NO")) {
						    				entity.setManual(true);
						    			}
									}
									Log.info(entity.getId().getCfecid() + " Remote Pending Vessel(s) on this order? " + entity.getManual());
									ScreenBilling billing = new ScreenBilling();
					    			billing.getBilling(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, 
					    				pmtvesCount, NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, vresults, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
								}
							});
			            } else {
			            	Log.info(entity.getId().getCfecid() + " Local Pending Vessel(s) on this order? " + entity.getManual());
			            	ScreenBilling billing = new ScreenBilling();
			    			billing.getBilling(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, 
			    				pmtvesCount, NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
			            }
	       	    	} else {
	       	    		Log.info(entity.getId().getCfecid() + " No permit intents on this order? " + entity.getManual());
	       	    		ScreenBilling billing = new ScreenBilling();
		    			billing.getBilling(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, 
		    				pmtvesCount, NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
	       	    	}
	       	    } else {
	       	    	cont = true;
	       	    }
			}
	    });
	    /*
	     * 	Define the LAST Button
	     */	    
    	last.enable();
	    last.removeAllListeners();
	    last.enable();
	    last.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
	       	    ScreenVessel gves = new ScreenVessel();
	       	    gves.getVessels(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, 
	       	    	pmtvesCount, NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
			}	    	
	    });
	    
	    /*
	     * Define and build permit grid using our permit list (plist)
	     */
		MemoryProxy proxy = new MemoryProxy(plist);
	    BeanModelReader reader = new BeanModelReader();
	    final BaseListLoader loader = new BaseListLoader(proxy, reader);
	    final ListStore<BeanModel> store = new ListStore<BeanModel>(loader);	
	    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
	    ColumnConfig p0 = new ColumnConfig("nointend", "<center>nointend</center>", 90);
	    CheckColumnConfig p1 = new CheckColumnConfig("renewed", "<center><span class='regblue10'>Do you want to renew this permit?</span></center>", 103) {
			@Override
	    	protected String getCheckState(ModelData model, String property, int rowIndex, int colIndex) {
				/*
				 * 	return "", "-on", "-disabled", "-disabled-checked", "disabled-unchecked", or "disabled-nobox"
				 */
				if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("completed") ||
						store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("pending")) {
					return "-disabled-checked";
				} else if  (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("waived")) {
					return "-disabled-unchecked";
				} else if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("action")) {
					return "-disabled-unchecked";
				} else {
					if (store.getAt(rowIndex).get("renewed").toString().equalsIgnoreCase("true")) {
						return "-on";
					} else {
						return "";
					}
				}
				//}
	        }
	    }; 
	    configs.add(p1);
	    CheckColumnConfig p2 = new CheckColumnConfig("intend", "<center><span class='regblue10'>Do you intend to fish this permit?</span></center>", 100) {
	    	@Override
	    	protected String getCheckState(ModelData model, String property, int rowIndex, int colIndex) {
				/*
				 * 	return "", "-on", "-disabled", "-disabled-checked", "disabled-unchecked", or "disabled-nobox"
				 */
				if (store.getAt(rowIndex).get("nointend").toString().equalsIgnoreCase("true")) {
					return "-disabled-unchecked";
				} else if (Integer.parseInt(store.getAt(rowIndex).get("id.pyear").toString().trim()) < Integer.parseInt(entity.getId().getRyear().toString().trim())) {
					return "-disabled-unchecked";
				} else if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("action")) {
					return "-disabled-unchecked";
				} else if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("completed") ||
						store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("pending")) {
					if (store.getAt(rowIndex).get("intend").toString().equalsIgnoreCase("true")) {
						if (store.getAt(rowIndex).get("already").toString().equalsIgnoreCase("false")) {
							if (store.getAt(rowIndex).get("nointend").toString().equalsIgnoreCase("false")) {	
								return "-on";
							} else {
								return "-disabled-checked";
							}
						} else {
							return "-disabled-checked";
						}
					} else {
						//return "";
						return "-disabled-unchecked";
					}				
				} else if  (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("waived")) {
					return "-disabled-unchecked";
				} else {
					if (store.getAt(rowIndex).get("intend").toString().equalsIgnoreCase("true")) {
						return "-on";
					} else {
						return "";
					}
				}
	        }
	    };  
	    configs.add(p2);
	    //ColumnConfig p3 = new ColumnConfig("id.fishery", "<center><span class='regblack10'>Fishery</span></center>", 60);
	    ColumnConfig p3 = new ColumnConfig("id.fishery", "<center><span class='regblack10'>Fishery</span></center>", 45);
	    p3.setAlignment(HorizontalAlignment.CENTER);
	    p3.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:black;");
	    configs.add(p3);
	    ColumnConfig p4 = new ColumnConfig("id.serial", "<center><span class='regblack10'>Serial #</span></center>", 43);
	    p4.setAlignment(HorizontalAlignment.CENTER);
	    p4.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:black;");
	    configs.add(p4);
	    ColumnConfig p5 = new ColumnConfig("id.pyear", "<center><span class='regblack10'>Year</span></center>", 37);
	    p5.setAlignment(HorizontalAlignment.CENTER);	 
	    p5.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:black;");
	    configs.add(p5);
	    ColumnConfig p6 = new ColumnConfig("adfg", "<center><span class='regblack10'>ADFG #</span></center>", 40);
	    p6.setAlignment(HorizontalAlignment.CENTER);
	    p6.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:blue;");
	    padfg = new TextField<String>();
	    padfg.setAllowBlank(false);
	    padfg.setRegex("(\\d{5}$)");
	    padfg.setMessageTarget("tooltip");
	    padfg.getMessages().setRegexText("The ADF&G field must only contain 5 numeric characters");
	    p6.setEditor(new CellEditor(padfg));
	    configs.add(p6);
	    ColumnConfig p7 = new ColumnConfig("fee", "<center><span class='regblack10'>Fee</span></center>", 40);
	    p7.setNumberFormat(NumberFormat.getCurrencyFormat());
	    p7.setAlignment(HorizontalAlignment.CENTER);
	    p7.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:black;");
	    configs.add(p7);
	    CheckColumnConfig p8 = new CheckColumnConfig("reducedfee", "<center><span class='regblue10'>Do you qualify for a Lower Fee?</span></center>", 97) {
	    	@Override
	    	protected String getCheckState(ModelData model, String property, int rowIndex, int colIndex) {
	    		/*
	    		 * 	return "", "-on", "-disabled", "-disabled-checked", "-disabled-unchecked", or "-disabled-nobox"
	    		 */
	    		entity.setAutoHALreduced(false);
				entity.setAutoSABreduced(false);
	    		if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("action")) {
	    			return "-disabled-nobox";
	    		} else if (store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 06B") ||
						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 61B")) ||
						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("C 06B")) ||
						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("C 61B")) ||
						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("C 09B")) ||
						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("C 91B"))) {
	    			//Log.info("Store fishery " + store.getAt(rowIndex).get("id.fishery").toString().trim());
	    			/*if (store.getAt(rowIndex).get("ofee").toString().equalsIgnoreCase("75")) {
	    				//Log.info("75 dollars? " + store.getAt(rowIndex).get("ofee").toString());
	    				if (store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 06B") ||
	    						(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 61B"))) {
	    					entity.setAutoHALreduced(true);
		    				return "-disabled-checked";
	    				} else {
	    					entity.setAutoSABreduced(true);
		    				return "-disabled-checked";
	    				}	 
	    			} else */if (store.getAt(rowIndex).get("status").toString().equalsIgnoreCase("completed") ||
							store.getAt(rowIndex).get("status").toString().equalsIgnoreCase("pending")) {
	    				//Log.info("status is " + store.getAt(rowIndex).get("status").toString());
						if (store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 06B") ||
								(store.getAt(rowIndex).get("id.fishery").toString().trim().equalsIgnoreCase("B 61B"))) {
							//Log.info("boolean halibut is " + store.getAt(rowIndex).get("halibut").toString());
							if (store.getAt(rowIndex).get("halibut").toString().equalsIgnoreCase("true")) {
								return "-disabled-checked";
							} else {
								return "-disabled-unchecked";
							}
						} else {
							//Log.info("boolean sablefish is " + store.getAt(rowIndex).get("sablefish").toString());
							if (store.getAt(rowIndex).get("sablefish").toString().equalsIgnoreCase("true")) {
								return "-disabled-checked";
							} else {
								return "-disabled-unchecked";
							}
						}						
					} else {
						//Log.info("boolean reducedfee " + store.getAt(rowIndex).get("reducedfee").toString());
						entity.setAutoHALreduced(false);
						entity.setAutoSABreduced(false);
						if (store.getAt(rowIndex).get("reducedfee").toString().equalsIgnoreCase("true")) {
							return "-on";
						} else {
							return "";
						}
					}
				} else {
					return "-disabled-nobox";
				}
	    	}
	    };
	    configs.add(p8);
	    ColumnConfig p9 = new ColumnConfig("notes", "<center><span class='regblack10'>Notes</span></center>", 140);
	    p9.setAlignment(HorizontalAlignment.CENTER);
	    p9.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif; color:black;");
	    configs.add(p9);
	    ColumnConfig p10 = new ColumnConfig("halibut", "<center>halibut</center>", 100);
	    ColumnConfig p11 = new ColumnConfig("sablefish", "<center>sablefish</center>", 100);
	    ColumnConfig p12 = new ColumnConfig("ofee", "<center>oFee</center>", 100);
	    ColumnConfig p13 = new ColumnConfig("msna", "<center>msna</center>", 100);
	    ColumnConfig p14 = new ColumnConfig("id.ryear", "<center>ryear</center>", 100);
	    ColumnConfig p15 = new ColumnConfig("already", "<center>already</center>", 100);
	    ColumnConfig p16 = new ColumnConfig("mpmt", "<center>mpmt</center>", 100);
	    ColumnConfig p17 = new ColumnConfig("description", "<center>description</center>", 100);
	    ColumnModel cm = new ColumnModel(configs);  
	    grid = new EditorGrid<BaseModel>(store, cm);
	    grid.setAutoExpandColumn("notes");  
        grid.setBorders(true);  
        grid.setAutoHeight(true);
	    grid.getView().setForceFit(true);
	    grid.addStyleName("tableGrids");
	    grid.addPlugin(p1);  
	    grid.addPlugin(p2);  
	    grid.addPlugin(p8); 
	    grid.setLoadMask(true);
	    /*
	     * add some grid listeners
	     */
	    grid.addListener(Events.Attach, new Listener<ComponentEvent>() {
      	  public void handleEvent(ComponentEvent be) {
      	    DeferredCommand.addCommand(new Command() {
      	      public void execute() {
      	        loader.load();
      	      }
      	    });  
      	  };
      	});
	    grid.addListener(Events.CellMouseDown, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent be) {
				String colName = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
				//Log.info("event colName " + colName);
				if (colName.equalsIgnoreCase("renewed") || colName.equalsIgnoreCase("reducedfee")) {
					//Log.info("event colName " + colName);
					//Log.info("event on?  " + be.getTarget().getClassName().substring(36, 38) );
					//Log.info("event on?  " + be.getTarget().getClassName() );
					if (be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("on") ||
							be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("x-")) {	
						getPmt.PermitSelect(colName, feeTotals, be, store, entity, grid, padfg, payment);
					}
				} else if (colName.equalsIgnoreCase("intend")) {
					if (be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("on") ||							
							be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("x-")) {	
						if (store.getAt(be.getRowIndex()).get("intend").toString().equalsIgnoreCase("false")) {
							/*
							 * 	intent is being checked but Permit is not renewed yet
							 */
							if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("true")) {
								if (store.getAt(be.getRowIndex()).get("already").toString().equalsIgnoreCase("true")) {
									cr.getPermitChanges2(store.getAt(be.getRowIndex()).get("id.serial").toString(), cfecid.getValue().toString(), 
										changeList, "false", "true", entity, store.getAt(be.getRowIndex()).get("id.fishery").toString());
								}
							}
						} else {
							if (store.getAt(be.getRowIndex()).get("already").toString().equalsIgnoreCase("true")) {
								cr.getPermitChanges2(store.getAt(be.getRowIndex()).get("id.serial").toString(), cfecid.getValue().toString(), 
									changeList, "true", "false", entity, store.getAt(be.getRowIndex()).get("id.fishery").toString());
							}
						}
						getPmt.PermitSelect(colName, feeTotals, be, store, entity, grid, padfg, payment);
					}
				}
				store.commitChanges();
				timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
			}
	    });
	    grid.addListener(Events.OnMouseOver, new Listener<GridEvent>() {
	    	@Override
			public void handleEvent(GridEvent be) {
	    		String colName = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
	    		if (colName.equalsIgnoreCase("fishery")) {
					gmsg.alert("<span class='regred12'>Fishery Code Definition</span>", (String) store.getAt(be.getRowIndex()).get("description"), 300);
				}
	    	}
	    });
	    grid.addListener(Events.BeforeEdit, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent be) {
				String colName = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
				if (colName.equalsIgnoreCase("adfg")) {
					if (store.getAt(be.getRowIndex()).get("status").toString().equalsIgnoreCase("completed") || store.getAt(be.getRowIndex()).get("status").toString().equalsIgnoreCase("pending")) {
						be.setCancelled(true);
						gmsg.alert("<span class='regred12'>Illegal Edit attempt</span>", "This permit is already renewed, you cannot edit the ADFG number online", 300);
					} else if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("false")) {
						be.setCancelled(true);
						gmsg.alert("<span class='regred12'>Illegal Edit attempt</span>", "This permit must be selected for renewal before the ADFG number can be modified", 300);
					} else {
						getPmt.PermitSelect(colName, feeTotals, be, store, entity, grid, padfg, payment);
					}
				} 
			}
	    });
	    grid.addListener(Events.AfterEdit, new Listener<GridEvent>() {
			@Override
			public void handleEvent(GridEvent be) {
				cr.getPermitChange1("pAdfg", cfecid.getValue().toString(), changeList, padfg, entity, 
					store.getAt(be.getRowIndex()).get("id.serial").toString());	
			}	    	
	    });			
	    ContentPanel cp = new ContentPanel();  
	    cp.setHeadingHtml("<center><span class='regblack12'>Your Permits - Select Permits to renew and/or intend to fish</span></center>");
	    cp.setFrame(true);
	    cp.setAutoHeight(true);
	    cp.setAutoWidth(true);
	    cp.setButtonAlign(HorizontalAlignment.CENTER);
	    
	    /*
	     * New permit button (IUP)
	     */
	    final Button newpmt = new Button();
		newpmt.setHtml("<span class='boldred10'>CLICK HERE to add a new permit</span>");
		newpmt.setTitle("Click this button to add one or more new Permits to your inventory");
		newpmt.setIcon(ClientResources.ICONS.add());
		newpmt.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
	    	public void componentSelected(ButtonEvent ce) {	
	    		gmsg.waitStart("Please Wait", "Getting new Permit info....", "Progress", 300);
	    		timer.timerCancel();
	       	    timer.progReset();
	       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
	    		/*
	    		 * 	Code Split point for new Permit window
	    		 * 	This section of code will only load and run IF the new Permit button is pressed
	    		 */
	    		GWT.runAsync(new RunAsyncCallback() {
					@Override
					public void onFailure(Throwable reason) {
					   gmsg.waitStop();
 					   statusBar.setHTML("<span class='regred12'>*** We are experiencing technical difficulties ***</span>");
 					   gmsg.alert("Login Error", gins.getTech(), 350);				
					}
					@Override
					public void onSuccess() {
						Window newPmtwin = np.getnewpermit(service, cfecid, entity, plist, bottomRight, newpmt, grid, loader, statusBar, timer, feeTotals, ryear, payment);
						newPmtwin.setClosable(true);
			    		gmsg.waitStop();
			    		newPmtwin.show();
					}	    	
	    		});
			}	    			
		});
		
		ButtonBar pbb = new ButtonBar();
		pbb.setAlignment(HorizontalAlignment.CENTER);
		pbb.add(newpmt);
		
	    cp.add(pbb);
        cp.add(grid);
        cp.addText("<center><span class='regred12'>Please note: The vessel you select for each permit MUST be licensed before we actually mail your card</span></center>");
        cp.addText("<center><span class='regred12'>Please note: You will <b>NOT</b> receive a permit card unless you intend to fish the permit</span></center>");
        
        if (entity.getCompany().equalsIgnoreCase("TRUE")) {
        	bottomRight.add(cp);
        	newpmt.setEnabled(false);
        	gmsg.alert("No Permits", "<span class='regblack12'><b>Companies can not own permits</b> - Please press the <span class='regred12'>Next >></span> button to proceed "
        		+ "to the Billing Section or the <span class='regred12'>Last >></span> button to go back to the Vessel section.</span>", 300);
        	vfound = false;
        } else if (entity.getArenewPermitses().size() < 1) {
        	bottomRight.add(cp);
        	gmsg.alert("No Permits", "<span class='regblack12'><b>You have no Permits to renew</b> - You can either.....<br><br>1. Add a new permit by selecting the 'CLICK HERE to add a new permit' button, <br>2. Press the <span class='regred12'>Next >></span> button to proceed to the Billing Section, or, <br>3. Press the <span class-'regred12'><< Last</span> button to return to the Vessel Section.</span>", 300);
        	vfound = true;
        } else {
        	bottomRight.add(cp);
        	vfound = true;
        }
	    
	    bottomRight.setHeadingHtml("<span class='boldblack12'>Permit renewal and intent selection</b></span>");
		bottomRight.layout();
		
		if (vfound) {
			if (firstTime) {
				gmsg.alert("Permit ADFG intent reminder", "If you are going to renew or purchase any permits, please remember to correctly identify " +
					"the ADFG number for vessel you intend to fish on. That number can be found under the <span class='regblue10'>ADFG #</span> column " +
				    "in the list above. To modify the existing value,<br> 1) select 'renew' for the appropriate permit.<br>2) Double click on the existing " +
				    "ADFG number.<br>3) Enter the new 5 digit ADFG number.", 300);
			}					
		} 	
	}
}
