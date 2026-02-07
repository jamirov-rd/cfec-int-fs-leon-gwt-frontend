package com.cfecweb.leon.client;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.cfecweb.leon.client.model.FeeTotals;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.google.gwt.user.client.DOM;

import static com.cfecweb.leon.client.FeeTotalsUtil.diffMinusc;
import static com.cfecweb.leon.client.FeeTotalsUtil.diffMinusp;
import static com.cfecweb.leon.client.FeeTotalsUtil.diffMinusp2;
import static com.cfecweb.leon.client.FeeTotalsUtil.diffPlusc;
import static com.cfecweb.leon.client.FeeTotalsUtil.diffPlusp;
import static com.cfecweb.leon.client.FeeTotalsUtil.diffPlusp2;
import static com.cfecweb.leon.client.FeeTotalsUtil.getNonResTotal;
import static com.cfecweb.leon.client.FeeTotalsUtil.getResTotal;
import static com.cfecweb.leon.client.FeeTotalsUtil.pmtMinus;
import static com.cfecweb.leon.client.FeeTotalsUtil.pmtPlus;
import static com.cfecweb.leon.client.FeeTotalsUtil.toD;
import static com.cfecweb.leon.client.FeeTotalsUtil.toI;
import static com.cfecweb.leon.client.FeeTotalsUtil.vesMinus;
import static com.cfecweb.leon.client.FeeTotalsUtil.vesPlus;

/*
 * This class is strictly background methods for the selection of permits or vessels to renew/license.
 * Keep in mind, the feetotals block was defined in the initial class and then updated asynchronously 
 * afterwards (AJAX), in this case by accessing and updating the defined fee fields using the DOM layer
 * of the Google Web Kit.
 */
@SuppressWarnings({"unused", "rawtypes"})
public class PmtVesSelectFunctions {
	UserMessages gmsg = new UserMessages();
	
	/*
	 * If a person is defining a new vessel, this method is called to automatically renew the vessel upon save
	 * and also updates the fee totals to reflect the cost.
	 */
	public void newVessel(FeeTotals feeTotals, ArenewEntity entity, ArenewVessels nves) {
		String sfeeo = nves.getFee();
		double dfeeo = (Double.parseDouble(sfeeo));
		Log.info(entity.getId().getCfecid() + " - automatically selecting new Vessel (ADFG "+nves.getId().getAdfg()+", Name "+nves.getName()+") to license");
		/* 
		 * increment the total vessel count
		 */
        vesPlus(feeTotals);
		/*
		 * 	select the vessel to renew/license
		 */
		nves.setRenewed(true);
		if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
			feeTotals.setResVessels((toD(feeTotals.getResVessels()) + dfeeo));
			DOM.getElementById("rv").setInnerText(Double.toString(toD(feeTotals.getResVessels())));
			DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
		} else {
			feeTotals.setNonresVessels((toD(feeTotals.getNonresVessels()) + dfeeo));
			DOM.getElementById("nv").setInnerText(Double.toString(toD(feeTotals.getNonresVessels())));
	        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
		}
	}

	/*
	 * If a person selects an already existing vessel in their inventory to license, this method is called to automatically renew the vessel upon save
	 * and also updates the fee totals to reflect the cost.
	 */
	public void VesselSelect(String colName, FeeTotals feeTotals, GridEvent be, ListStore<BaseModel> store, ArenewEntity entity, 
			EditorGrid<BaseModel> grid, String vadfg, String vname, String newv) {
		String sfeeo = store.getAt(be.getRowIndex()).get("fee").toString();
		double dfeeo = (Double.parseDouble(sfeeo));
		if (colName.equalsIgnoreCase("renewed")) {
			if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("false")) {
				Log.info(entity.getId().getCfecid() + " has selected Vessel " + vadfg + " to license");
				/*
				 * 	increment to total vessel count
				 */
                vesPlus(feeTotals);
				if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
					feeTotals.setResVessels((toD(feeTotals.getResVessels()) + dfeeo));
					DOM.getElementById("rv").setInnerText(Double.toString(toD(feeTotals.getResVessels())));
					DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
				} else {
					feeTotals.setNonresVessels((toD(feeTotals.getNonresVessels()) + dfeeo));
					DOM.getElementById("nv").setInnerText(Double.toString(toD(feeTotals.getNonresVessels())));
			        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
				}
			} else {
				Log.info(entity.getId().getCfecid() + " has un-selected Vessel " + vadfg + " to license");
				/*
				 * 	decrement the total vessel count
				 */
                vesMinus(feeTotals);
				if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
					feeTotals.setResVessels((toD(feeTotals.getResVessels()) - dfeeo));
					DOM.getElementById("rv").setInnerText(Double.toString(toD(feeTotals.getResVessels())));
					DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
				} else {
					feeTotals.setNonresVessels((toD(feeTotals.getNonresVessels()) - dfeeo));
					DOM.getElementById("nv").setInnerText(Double.toString(toD(feeTotals.getNonresVessels())));
			        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
				}	
			}
		}
	}
	
	/*
	 * If any new iep is selected, then automatically renew it and select for intent.
	 */
	public void NewPermit(FeeTotals feeTotals, ArenewEntity entity, ArenewPermits npmt, ArenewPayment payment) {
		String sorig = npmt.getFee();
		double dorig = (Double.parseDouble(sorig));
		String pyear = npmt.getId().getPyear();
		String cyear = entity.getId().getRyear();
		double diffc = Double.parseDouble(entity.getDiffamountcyear());
		//double diffp = Double.parseDouble(entity.getDiffamountpyear());
		/*
		 * 	Increment the permit counter
		 */
        pmtPlus(feeTotals);
		/*
		 * 	Set the new permit as renewed AND intent to fish
		 */
		npmt.setRenewed(true);
		npmt.setIntend(true);
		/*
		 * 	Check for residency and update fee tables
		 */
		if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
			/*
			 *  	is resident
			 */
			feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) + dorig));
			DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
			DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
		} else {
			/*
			 *  	is not resident
			 */
			if (pyear.equalsIgnoreCase(cyear)) {
                diffPlusc(feeTotals);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				if (entity.getDifferentialc().equalsIgnoreCase("false")) {
					if (toI(feeTotals.getDiffc()) == 1) {
						payment.setDfee1(entity.getDiffamountcyear());
			        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffc);
						DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
			        }
				}
			} /*else {
				feeTotals.diffPlusp();
				feeTotals.setNonresFishingPermits(((feeTotals.getNonresFishingPermits()) + dorig));
				DOM.getElementById("nfp").setInnerText(Double.toString(feeTotals.getNonresFishingPermits()));
				if (entity.getDifferentialp().equalsIgnoreCase("false")) {
					if (feeTotals.getDiffp() == 1) {
			        	feeTotals.setNonresDifferential(feeTotals.getNonresDifferential() + diffp);
						DOM.getElementById("nd").setInnerText(Double.toString(feeTotals.getNonresDifferential()));	
			        }
				}
			}	*/
	        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
		}	
	}
	
	/*
	 * If any permit checkbox is selected, go here and sort things out
	 */
	public void PermitSelect(String colName, FeeTotals feeTotals, GridEvent be, ListStore<BeanModel> store, ArenewEntity entity, 
			EditorGrid<BaseModel> grid, TextField<String> padfg, ArenewPayment payment) {
		/*
		 *  Define some initial values used throughout
		 */
		String pnum = store.getAt(be.getRowIndex()).get("id.serial");
		String pfsh = store.getAt(be.getRowIndex()).get("id.fishery");
		String sorig = store.getAt(be.getRowIndex()).get("fee").toString();
		String scopy = store.getAt(be.getRowIndex()).get("ofee").toString();
		double dorig = (Double.parseDouble(sorig));
		String dcopy = scopy;
		double ddiff = (Double.parseDouble(dcopy) - 75.00);
		String dred = "75";
		double diffc = Double.parseDouble(entity.getDiffamountcyear());
		double diffp = Double.parseDouble(entity.getDiffamountpyear());
		double diffp2 = Double.parseDouble(entity.getDiffamountp2year());
		String cyear = entity.getId().getRyear();
		//	Define originally selected record metadata
		String omnsa = store.getAt(be.getRowIndex()).get("msna").toString().trim();
		String ofishery = store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().toUpperCase();
		String oyear = store.getAt(be.getRowIndex()).get("id.pyear").toString().trim();		
		String ompmt = store.getAt(be.getRowIndex()).get("mpmt").toString().trim();
		/*
		 * Selected this records Renew checkbox
		 */
		if (colName.equalsIgnoreCase("renewed")) {
			/*
			 *  is record already selected for renew?
			 */
			if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("false")) {
				Log.info(entity.getId().getCfecid() + " has selected Permit number " + pnum + ", fishery " + pfsh + " to renew");
				/*
				 *  nope, select it and total the fees
				 */
				selectRenew(feeTotals, entity, store, be, dorig, dcopy, ddiff, dred, diffc, diffp, diffp2, cyear, omnsa, ofishery, oyear, ompmt, payment);
				/*
				 *  check to see if other years exist for this record)
				 */
				selectOther(feeTotals, entity, store, be, dorig, dcopy, ddiff, dred, diffc, diffp, diffp2, cyear, omnsa, ofishery, oyear, ompmt, pnum, pfsh, sorig, scopy, grid, payment);
			} else {
				Log.info(entity.getId().getCfecid() + " has un-selected Permit number " + pnum + ", fishery " + pfsh + " to renew");
				/*
				 *  renewed is being un-checked. remove check if not older year, then check for multi and salmon net options on intent (if selected)
				 */
				unselectRenew(feeTotals, entity, store, be, dorig, dcopy, ddiff, dred, diffc, diffp, diffp2, cyear, omnsa, ofishery, oyear, ompmt, grid, payment);			
				/*
				 *  Does this record contain Multiple Salmon Net permits OR does it contain more than one permit in the same fishery?
				 */
				if (Integer.parseInt(omnsa) > 0 || ompmt.equalsIgnoreCase("Yes")) {
					BaseModel m = grid.getStore().getAt(be.getRowIndex());
					Record r = grid.getStore().getRecord(m);
					if (!(r.get("nointend").toString().equalsIgnoreCase("True"))) {
						r.set("intend", false);
						unselectIntendMSNAMultiple(feeTotals, entity, store, be, diffp, omnsa, ompmt, ofishery, oyear, grid);
					}					
				} else {
					BaseModel m = grid.getStore().getAt(be.getRowIndex());
					Record r = grid.getStore().getRecord(m);
					r.set("intend", false);
				}
			}
		/*
		 * Selected this records Intent checkbox
		 */
		} else if (colName.equalsIgnoreCase("intend")) {
			/*
			 *  is record already selected for intent?
			 */
			if (store.getAt(be.getRowIndex()).get("intend").toString().equalsIgnoreCase("false")) {
				Log.info(entity.getId().getCfecid() + " has selected Permit number " + pnum + ", fishery " + pfsh + " for intent to fish");
				/*
				 *  	nope, check to see if Renew is already selected
				 */
				if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("false")) {
					/*
					 *  	renewed is not checked, check it first
					 */
					int index = grid.getView().findRowIndex(be.getTarget());
					BaseModel m = grid.getStore().getAt(index);
					Record r = grid.getStore().getRecord(m);
					r.set("renewed", true);
					/*
					 *  	Then proceed ......
					 */
					selectRenew(feeTotals, entity, store, be, dorig, dcopy, ddiff, dred, diffc, diffp, diffp2, cyear, omnsa, ofishery, oyear, ompmt, payment);
					/*
					 *  check to see if other years exist for this record OR if this person has multiple salmon permits (same fishery, same year, different serial)
					 */
				}
				/*
				 *  Is this a salmon net permit or contain multiples? (gears 01, 03 or 04) If so, validate MSNA and multiples
				 */
				if (Integer.parseInt(omnsa) > 0 || ompmt.equalsIgnoreCase("Yes")) {
					selectIntendMSNAMultiple(store, omnsa, ompmt, ofishery, oyear, grid, be);
				}
				/*
				 * 	Are there older years for the serial number that must also be renewed? Check and handle.
				 */
				selectOther(feeTotals, entity, store, be, dorig, dcopy, ddiff, dred, diffc, diffp, diffp2, cyear, omnsa, ofishery, oyear, ompmt, pnum, pfsh, sorig, scopy, grid, payment);
			} else {
				Log.info(entity.getId().getCfecid() + " has un-selected Permit number " + pnum + ", fishery " + pfsh + " for intent to fish");
				/*
				 * 	remove this intent
				 */
				/*
				 *  Is this a salmon net permit or contain multiples? (gears 01, 03 or 04) If so, validate MSNA and multiples
				 */
				if (Integer.parseInt(omnsa) > 0 || ompmt.equalsIgnoreCase("Yes")) {
					unselectIntendMSNAMultiple(feeTotals, entity, store, be, diffp, omnsa, ompmt, ofishery, oyear, grid);
				}
			}
		/*
		 * Selected the reduced fees checkbox
		 */
		} else if (colName.equalsIgnoreCase("reducedfee")) {
			//Log.info("got here 1");
			/*
			 * 	check if the reduced fee box is already selected
			 */
			if (store.getAt(be.getRowIndex()).get("reducedfee").toString().equalsIgnoreCase("false")) {
				//Log.info("got here 2");
				/*
				 * 	nope, select it and calculate new fees
				 */
				Log.info(entity.getId().getCfecid() + " has selected reduced fees for Permit number " + pnum + ", fishery " + pfsh);
				selectReducedFeesMod(entity, store, feeTotals, dred, ddiff, be);
			} else {
				//Log.info("got here 3");
				/*
				 * 	yes, unselect it and calculate new fees
				 */
				Log.info(entity.getId().getCfecid() + " has un-selected reduced fees for Permit number " + pnum + ", fishery " + pfsh);
				unselectReducedFeesMod(entity, store, feeTotals, dred, ddiff, be, dcopy);
			}
		/*
		 * Selected the ADFG column for modification.
		 */
		} else if (colName.equalsIgnoreCase("adfg")) {
			padfg.setValue(store.getAt(be.getRowIndex()).get("adfg").toString());
		}
	}

	/*
	 * Method when permit is selected for renewal only
	 */
	public void selectRenew(FeeTotals feeTotals, ArenewEntity entity, ListStore<BeanModel> store, GridEvent be, double dorig, String dcopy, double ddiff, 
			String dred, double diffc, double diffp, double diffp2, String cyear, String omnsa, String ofishery, String oyear, String ompmt, ArenewPayment payment) {
		String cyear1 = Integer.toString(Integer.parseInt(cyear) - 1);
		String cyear2 = Integer.toString(Integer.parseInt(cyear) - 2);
        pmtPlus(feeTotals);
		if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
			/*
			 *  	is resident
			 */
			feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) + dorig));
			DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
			DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
		} else {
			/*
			 *  	is not resident
			 */
			/*
			 * Is the permit 3 years old?
			 */
			if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear2)) {
                diffPlusp2(feeTotals);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				if (entity.getDifferentialp2().equalsIgnoreCase("false")) {
					if (toI(feeTotals.getDiffp2()) == 1) {
			        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffp2);
						DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
			        }
				}
			}
			/*
			 * is the permit 2 years old?
			 */
			if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear1)) {
                diffPlusp(feeTotals);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				if (entity.getDifferentialp().equalsIgnoreCase("false")) {
					if (toI(feeTotals.getDiffp()) == 1) {
						payment.setDfee2(entity.getDiffamountpyear());
			        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffp);
						DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
			        }
				}
			}
			/*
			 * is the permit current?
			 */
			else if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear)) {
                diffPlusc(feeTotals);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				if (entity.getDifferentialc().equalsIgnoreCase("false")) {
					if (toI(feeTotals.getDiffc()) == 1) {
						payment.setDfee1(entity.getDiffamountcyear());
			        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffc);
						DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
			        }
				}
			}			
	        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
		}
	} 
	
	/*
	 * Method to discover and select prior years permit(s) with unpaid fees	
	 */
	public void selectOther(FeeTotals feeTotals, ArenewEntity entity, ListStore<BeanModel> store, GridEvent be, double dorig, 
			String dcopy, double ddiff, String dred, double diffc, double diffp, double diffp2, String cyear, String pnum, String pfsh, 
			String sorig, String scopy, String pnum2, String pfsh2, String sorig2, String scopy2, EditorGrid<BaseModel> grid, ArenewPayment payment) {
		String cyear1 = Integer.toString(Integer.parseInt(cyear) - 1);
		String cyear2 = Integer.toString(Integer.parseInt(cyear) - 2);
		/*
		 * 	execute loop through all permits
		 */		
		for (int y=0;y<store.getCount();y++) {
			/*
			 *  	not the same record ?
			 */
			if (!(store.getAt(y) == store.getAt(be.getRowIndex()))) {
				/*
				 *  	is available ?
				 */
				if (store.getAt(y).get("status").toString().equalsIgnoreCase("available")) {
					/*
					 *  	same fishery name ?
					 */
					if (store.getAt(y).get("id.fishery").toString().trim().equalsIgnoreCase(store.getAt(be.getRowIndex()).get("id.fishery").toString().trim())) {
						/*
						 *  same serial number ?
						 */
						if (store.getAt(y).get("id.serial").toString().trim().equalsIgnoreCase(store.getAt(be.getRowIndex()).get("id.serial").toString())) {
							/*
							 *  Compare the years, looking to see if the selected record is more current than the iterated record
							 */
							if (Integer.parseInt(store.getAt(be.getRowIndex()).get("id.pyear").toString().trim()) > Integer.parseInt(store.getAt(y).get("id.pyear").toString().trim())) {
							//if (Integer.parseInt(store.getAt(y).get("id.pyear").toString().trim()) < Integer.parseInt(store.getAt(be.getRowIndex()).get("id.ryear").toString().trim())) {	
								/*
								 *  	already selected ?
								 */
								if (store.getAt(y).get("renewed").toString().trim().equalsIgnoreCase("false")) {
									/*
									 *  	if not, select it for renewal because it is older and unpaid for
									 */
									sorig = store.getAt(y).get("fee").toString();
									scopy = store.getAt(y).get("ofee").toString();
									dorig = (Double.parseDouble(sorig));
									dcopy = scopy;
									ddiff = (Double.parseDouble(dcopy) - 75.00);
									dred = "75";
                                    pmtPlus(feeTotals);
									BaseModel m = grid.getStore().getAt(y);
									Record r = grid.getStore().getRecord(m);
									r.set("renewed", true);	
									if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
										/*
										 * 	is resident
										 * 
										 */
										feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) + dorig));
										DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
										DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
									} else {
										/*
										 * 	is not resident
										 */
										/*
										 * 3 year old permit?
										 */
										if (store.getAt(y).get("id.pyear").toString().equalsIgnoreCase(cyear2)) {
                                            diffPlusp2(feeTotals);
											feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
											DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
											if (entity.getDifferentialp2().equalsIgnoreCase("false")) {
												if (toI(feeTotals.getDiffp2()) == 1) {
										        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffp2);
													DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
										        }
											}
										}
										/*
										 * 2 year old permit?
										 */
										if (store.getAt(y).get("id.pyear").toString().equalsIgnoreCase(cyear1)) {
                                            diffPlusp(feeTotals);
											feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
											DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
											if (entity.getDifferentialp().equalsIgnoreCase("false")) {
												if (toI(feeTotals.getDiffp()) == 1) {
													payment.setDfee2(entity.getDiffamountpyear());
										        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffp);
													DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
										        }
											}
										} 
										/*
										 * current year permit?
										 */
										else if (store.getAt(y).get("id.pyear").toString().equalsIgnoreCase(cyear)) {
                                            diffPlusc(feeTotals);
											feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + dorig));
											DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
											if (entity.getDifferentialc().equalsIgnoreCase("false")) {
												if (toI(feeTotals.getDiffc()) == 1) {
													payment.setDfee1(entity.getDiffamountcyear());
										        	feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) + diffc);
													DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
										        }
											}
										}
								        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
									}									
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * Method to run when person is un-selecting a permit to renew
	 */
	public void unselectRenew(FeeTotals feeTotals, ArenewEntity entity, ListStore<BeanModel> store, GridEvent be, double dorig, String dcopy, double ddiff, 
			String dred, double diffc, double diffp, double diffp2, String cyear, String omnsa, String ofishery, String oyear, String ompmt, EditorGrid<BaseModel> grid, ArenewPayment payment) {
		boolean found = false;
		double sdorig = 0.0;
		String cyear1 = Integer.toString(Integer.parseInt(cyear) - 1);
		String cyear2 = Integer.toString(Integer.parseInt(cyear) - 2);
		/*
		 * 	execute loop through all permits
		 */
		for (int y=0;y<store.getCount();y++) {
			/*
			 *  same record ?
			 */
			if (!(store.getAt(y) == store.getAt(be.getRowIndex()))) {
				/*
				 *  permit is available ?
				 */
				if (store.getAt(y).get("status").toString().equalsIgnoreCase("available")) {
					/*
					 *  	fisheries match ?
					 */
					if (store.getAt(y).get("id.fishery").toString().trim().equalsIgnoreCase(store.getAt(be.getRowIndex()).get("id.fishery").toString().trim())) {
						/*
						 *  	serial match ?
						 */
						if (store.getAt(y).get("id.serial").toString().trim().equalsIgnoreCase(store.getAt(be.getRowIndex()).get("id.serial").toString())) {
							/*
							 *  	is the current iterated record year greater than selected record?
							 */
							if (Integer.parseInt(store.getAt(y).get("id.pyear").toString().trim()) > Integer.parseInt(store.getAt(be.getRowIndex()).get("id.pyear").toString().trim())) {
								/*
								 *  	If so, is it selected?
								 */
								if (store.getAt(y).get("renewed").toString().trim().equalsIgnoreCase("true")) {
									/*
									 *  	can't un-select the selected record, it is a previous years and current year is selected, must pay for both
									 */
									found = true;
									BaseModel m = grid.getStore().getAt(be.getRowIndex());
									Record r = grid.getStore().getRecord(m);
									r.set("renewed", false);	
									break;
								}
							}
						}
					}
				}
			}
		}
		/*
		 *  If found == false, there was no occurance of the same permit for a greater year, go ahead and remove the check and fees
		 *  OR if same == true, also remove the check and fees
		 */
		if (found == false) {
			/*
			 *  total the new fees
			 */
            pmtMinus(feeTotals);
			/*
			 * are they are a resident
			 */
			if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
				feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) - dorig));
				DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
				DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
			} else {				
				/*
				 * is not a resident
				 */
				/*
				 * current year permit?
				 */
			    if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear)) {
                    diffMinusc(feeTotals);
					feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) - dorig));
					DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
					if (entity.getDifferentialc().equalsIgnoreCase("false")) {
						if (toI(feeTotals.getDiffc()) == 0) {
							payment.setDfee1(null);
							feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) - diffc);
							DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
						}
					}
				}
			    /*
			     * 2 year old permit
			     */
			    else if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear1)) {
                    diffMinusp(feeTotals);
					feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) - dorig));
					DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
					if (entity.getDifferentialp().equalsIgnoreCase("false")) {
						if (toI(feeTotals.getDiffp()) == 0) {
							payment.setDfee2(null);
							feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) - diffp);
							DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
						}
					}
				}
			    /*
			     * 3 year old permit
			     */
			    else if (store.getAt(be.getRowIndex()).get("id.pyear").toString().equalsIgnoreCase(cyear2)) {
                    diffMinusp2(feeTotals);
					feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) - dorig));
					DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
					if (entity.getDifferentialp2().equalsIgnoreCase("false")) {
						if (toI(feeTotals.getDiffp2()) == 0) {
							feeTotals.setNonresDifferential(toD(feeTotals.getNonresDifferential()) - diffp2);
							DOM.getElementById("nd").setInnerText(Double.toString(toD(feeTotals.getNonresDifferential())));
						}
					}
				}
		        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
			}		
		}
	}
	
	/*
	 * Method to run if user un-selects the intent box
	 */
	public void unselectIntendMSNAMultiple(FeeTotals feeTotals, ArenewEntity entity, ListStore<BeanModel> store, GridEvent be, double diffp, String omnsa, String ompmt, String ofishery, String oyear, EditorGrid<BaseModel> grid) {
		boolean found = false;
		/*
		 * 	execute loop through all permits
		 */
		for (int y=0;y<store.getCount();y++) {
			/*
			 *  	Define selected loop record metadata
			 */
			String tmnsa = store.getAt(y).get("msna").toString();
			String tfishery = store.getAt(y).get("id.fishery").toString().trim().toUpperCase();
			String tyear = store.getAt(y).get("id.pyear").toString().trim();
			String tmpmt = store.getAt(y).get("mpmt").toString().trim();
			String oarea = store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().toUpperCase().substring(4, 5);
			String tarea = store.getAt(y).get("id.fishery").toString().trim().toUpperCase().substring(4, 5);
			/*
			 *  	check the current records primary availability
			 */
			if (store.getAt(y).get("status").toString().equalsIgnoreCase("available")) {
				/*
				 *  	check to make sure this record is salmon (mnsa greater than 0)
				 */
				if (Integer.parseInt(tmnsa) > 0) {
					/*
					 *  	check to make sure this is NOT the original record
					 */
					if (!(store.getAt(y) == store.getAt(be.getRowIndex()))) {
						if (tarea.equalsIgnoreCase(oarea)) {
							if (store.getAt(y).get("intend").toString().equalsIgnoreCase("true")) {
								found = true;
							}
						}						
					}
				}
			}
		}
		for (int x=0;x<store.getCount();x++) {
			/*
			 * 	Define selected loop record metadata
			 */
			String tmnsa = store.getAt(x).get("msna").toString();
			String tfishery = store.getAt(x).get("id.fishery").toString().trim().toUpperCase();
			String tyear = store.getAt(x).get("id.pyear").toString().trim();
			String tmpmt = store.getAt(x).get("mpmt").toString().trim();
			String oarea = store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().toUpperCase().substring(4, 5);
			String tarea = store.getAt(x).get("id.fishery").toString().trim().toUpperCase().substring(4, 5);
			/*
			 *  	check the current records primary availability
			 */
			if (store.getAt(x).get("status").toString().equalsIgnoreCase("available")) {
				/*
				 *  	check to make sure this record is salmon (mnsa greater than 0) OR contains multiples
				 */
				if (Integer.parseInt(tmnsa) > 0 || tmpmt.equalsIgnoreCase("Yes")) {
					/*
					 *  	check to make sure this is NOT the original record
					 */
					if (!(store.getAt(x) == store.getAt(be.getRowIndex()))) {
						/*
						 * 	is the msna value the same?
						 */
						if (!(tmnsa.equalsIgnoreCase(omnsa))) {
							/*
							 * 	Nope, different area. Check the year
							 */
							if (oyear.equalsIgnoreCase(tyear)) {
								/*
								 * 	current year
								 */
								if (store.getAt(x).get("nointend").toString().equalsIgnoreCase("true") && found == false) {
									/*
									 * 	disable this records intent box
									 */
									BaseModel m = grid.getStore().getAt(x);
									Record r = grid.getStore().getRecord(m);
									r.set("nointend", false);	
								}
							}							
						} else {
							/*
							 * 	same area, could be multiples, check
							 */
							if (ompmt.equalsIgnoreCase("Yes")) {
								if (tyear.equalsIgnoreCase(oyear)) {
									if ( (tfishery.toString().trim().equalsIgnoreCase("S 04D")) || 
										 (tfishery.toString().trim().equalsIgnoreCase("S 04H")) || 
										 (tfishery.toString().trim().equalsIgnoreCase("S 03H")) ) {
										/*
										 * Do nothing here, we currently allow multiple permits in these fisheries to be fished
										 */
									} else {
										/*
										 * 	disable this records intent box
										 */
										BaseModel m = grid.getStore().getAt(x);
										Record r = grid.getStore().getRecord(m);
										r.set("nointend", false);
									}
								}
							}
						}
					}
				}
			}
		}
		found = false;
	}
	
	/*
	 * Method to run if the intent being selected is a salmon permit
	 */
	public void selectIntendMSNAMultiple(ListStore<BeanModel> store, String omnsa, String ompmt, String ofishery, String oyear, 
			EditorGrid<BaseModel> grid, GridEvent be) {
		/*
		 * 	execute loop through all permits
		 */
		for (int y=0;y<store.getCount();y++) {
			/*
			 *  	Define selected loop record metadata
			 */
			String tmnsa = store.getAt(y).get("msna").toString();
			String tfishery = store.getAt(y).get("id.fishery").toString().trim().toUpperCase();
			String tyear = store.getAt(y).get("id.pyear").toString().trim();
			String tmpmt = store.getAt(y).get("mpmt").toString().trim();
			/*
			 *  	check the current records primary availability
			 */
			if (store.getAt(y).get("status").toString().equalsIgnoreCase("available")) {
				/*
				 *  	check to make sure this record is salmon (mnsa greater than 0) OR contains multiples
				 */
				if (Integer.parseInt(tmnsa) > 0 || tmpmt.equalsIgnoreCase("Yes")) { 
					/*
					 *  	check to make sure this is NOT the original record
					 */
					if (!(store.getAt(y) == store.getAt(be.getRowIndex()))) {
						/*
						 * 	is the msna value the same?
						 */
						if (!(tmnsa.equalsIgnoreCase(omnsa))) {
							/*
							 * 	Nope, different area. Check the year
							 */
							if (oyear.equalsIgnoreCase(tyear)) {
								/*
								 * 	current year
								 */
								if (store.getAt(y).get("nointend").toString().equalsIgnoreCase("false")) {
									/*
									 * 	disable this records intent box
									 */
									BaseModel m = grid.getStore().getAt(y);
									Record r = grid.getStore().getRecord(m);
									r.set("nointend", true);	
								}
							}							
						} else {
							/*
							 * 	same area, could be multiples, check
							 */
							if (ompmt.equalsIgnoreCase("Yes")) {
								if (tyear.equalsIgnoreCase(oyear)) {
									if ( (tfishery.toString().trim().equalsIgnoreCase("S 04D")) || 
											 (tfishery.toString().trim().equalsIgnoreCase("S 04H")) || 
											 (tfishery.toString().trim().equalsIgnoreCase("S 03H")) ) {
										/*
										 * Do nothing here, we currently allow multiple permits in these fisheries to be fished
										 */
									} else {
										/*
										 * 	disable this records intent box
										 */
										BaseModel m = grid.getStore().getAt(y);
										Record r = grid.getStore().getRecord(m);
										r.set("nointend", true);
									}		
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * Method to run is the user selects the Lower Fee checkbox
	 */
	public void selectReducedFeesMod(ArenewEntity entity, ListStore<BeanModel> store, FeeTotals feeTotals, String dred, double ddiff, GridEvent be) {
		String type = null;
		if (store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().equalsIgnoreCase("B 06B") || 
				(store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().equalsIgnoreCase("B 61B"))) {
			/*
			 *  	this is a halibut fishery
			 */
			type = "Halibut";
			entity.setReducedHalibut("true");
			gmsg.alert("<span class='regred12'>Halibut Lower Fee Note  -  <b>IMPORTANT!!</b></span>", "If you believe you qualify for the reduced fee, you <u>MUST</u> provide " + 
			"evidence that you either landed under 8000 lbs of halibut in the previous year OR you are a member of a Western AK CDQ halibut group. " +
			"If this is a new permit, you can still qualify by providing the CFEC evidence of IFQ allotment for this year under 8000 lbs." +		
			"Your IFQ landing report " +
			"OR statement or CDQ membership can be sent to CFEC by several options listed after your transaction has been successfully submited. Your application will processed after we receive this " +
			"supporting documentation. If you do not qualify under these conditions, please unselect the halibut lower fee box", 250);
		} else {
			/*
			 *  this is a sablefish fishery
			 */
			type = "Sablefish";
			entity.setReducedSablefish("true");
			gmsg.alert("<span class='regred12'>Sablefish Lower Fee Note  -  <b>IMPORTANT!!</b></span>", "If you believe you qualify for the reduced fee, you <u>MUST</u> provide " + 
			"evidence that you either landed under 9000 lbs of sablefish in the previous year. " +
			"If this is a new permit, you can still qualify by providing the CFEC evidence of IFQ allotment for this year under 9000 lbs." +
			"Your IFQ landing report OR statement can be sent to CFEC by several options listed after your transaction has been successfully submited. " +
			"Your application will be processed after we receive this supporting documentation. " +
			"If you do not qualify under these conditions, please unselect the sablefish lower fee box", 250);				
		}
		//Log.info("got here selectreduced 1");
		if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("true")) {
			//Log.info("got here selectreduced 2");
			if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
				//Log.info("got here selectreduced 3");
				store.getAt(be.getRowIndex()).set("fee", dred);
				feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) - ddiff));
				DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
				DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
				store.commitChanges();
				//Log.info("got here selectreduced 4");
			} else {
				//Log.info("got here selectreduced 5");
				store.getAt(be.getRowIndex()).set("fee", dred);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) - ddiff));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
				store.commitChanges();
				//Log.info("got here selectreduced 6");
			}
		} else {
			//Log.info("got here selectreduced 7");
			if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
				store.getAt(be.getRowIndex()).set("fee", dred);
				store.commitChanges();
				//Log.info("got here selectreduced 8");
			} else {
				store.getAt(be.getRowIndex()).set("fee", dred);
				store.commitChanges();
				//Log.info("got here selectreduced 9");
			}
		}
	}
	
	/*
	 * method to run is user un-selects the Lower Fee checkbox
	 */
	public void unselectReducedFeesMod(ArenewEntity entity, ListStore<BeanModel> store, FeeTotals feeTotals, String dred, double ddiff, GridEvent be, String dcopy) {
		//Log.info("got here selectunreduced 1");
		String type = null;
		if (store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().equalsIgnoreCase("B 06B") ||
				(store.getAt(be.getRowIndex()).get("id.fishery").toString().trim().equalsIgnoreCase("B 61B"))) {
			//Log.info("got here selectunreduced 2");
			/*
			 *  	this is a halibut fishery
			 */
			type = "Halibut";
			entity.setReducedHalibut("false");
			//Log.info("got here selectunreduced 3");
		} else {
			//Log.info("got here selectunreduced 4");
			/*
			 *  this is a sablefish fishery
			 */
			type = "Sablefish";
			entity.setReducedSablefish("false");
			//Log.info("got here selectunreduced 5");
		}
		if (store.getAt(be.getRowIndex()).get("renewed").toString().equalsIgnoreCase("true")) {
			if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
				store.getAt(be.getRowIndex()).set("fee", dcopy);
				feeTotals.setResFishingPermits((toD(feeTotals.getResFishingPermits()) + ddiff));
				DOM.getElementById("rfp").setInnerText(Double.toString(toD(feeTotals.getResFishingPermits())));
				DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
				//Log.info("got here selectunreduced 6");
				store.commitChanges();
				
			} else {
				store.getAt(be.getRowIndex()).set("fee", dcopy);
				feeTotals.setNonresFishingPermits((toD(feeTotals.getNonresFishingPermits()) + ddiff));
				DOM.getElementById("nfp").setInnerText(Double.toString(toD(feeTotals.getNonresFishingPermits())));
				DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
				store.commitChanges();
				//Log.info("got here selectunreduced 7");
			}
		} else {
			//Log.info("got here 6");
			if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
				store.getAt(be.getRowIndex()).set("fee", dcopy);
				store.commitChanges();
				//Log.info("got here selectunreduced 8");
			} else {
				store.getAt(be.getRowIndex()).set("fee", dcopy);
				store.commitChanges();
				//Log.info("got here selectunreduced 9");
			}
		}
	}
	
}
