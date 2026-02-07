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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;

import static com.cfecweb.leon.client.FeeTotalsUtil.getNonResTotal;
import static com.cfecweb.leon.client.FeeTotalsUtil.getResTotal;
import static com.cfecweb.leon.client.FeeTotalsUtil.toD;
import static com.cfecweb.leon.client.FeeTotalsUtil.toI;

/*
 * The billing screen (class) contains forms to input your basic billing information. Included in this screen now is the
 * ability to specify a temporary address as well as a billing address. Even though temporary addresses are recorded in 
 * this UI, they are stored in the entity table based on original specifications. This class is much like the rest of the 
 * UI screens with a certain amount of data being required. Note the regular expressions on some of the text fields that
 * provide for client validation of certain types of data without having to check it on the server.
 * This screen allows for a few different variations when the user gets here:
 * 1. User selects 0 permits and 0 vessels to renew/license and has 0 other records to update
 * 2. User selects 0 permits and 0 vessels to renew/license but has modified some personal data (such as telephone number)
 * 3. User selects 1 or more permits and/or vessles to renew/license and possible could have personal updates
 * Based on scenarios, billing may or may not be appropriate, a simple update to personal data may be all they want to do.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ScreenBilling {
	InstructionsText gins = new InstructionsText();
	UserMessages gmsg = new UserMessages();
	FormPanel addressPanel = null;
	SimpleComboBox shipping = null;
	FieldSet ship = null;
	boolean cont = false;
	boolean tadd = true;
	boolean mail = true;
	Double tfee = null;
	boolean nopmtves = false;
	ChangeRecorder cr = new ChangeRecorder();
	LayoutContainer middle2 = null;
	LayoutContainer middle3 = null;
	LayoutContainer right = null;
	int lyear = 0;

	public void getBilling(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft,final FieldSet bottomRight, final FieldSet topRight, final Button startOver,
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText, final String pmtvesCount,
			final HorizontalPanel NavprogressBarPanel, final ArenewEntity entity, final List<ArenewChanges> changeList, final FeeTotals feeTotals,
			final getDataAsync service, final String topLeftText, final boolean firstTime, final SessionTimer timer, final ArenewPayment payment,
			final List<ArenewPermits> plist, final List<ArenewVessels> vlist, final CheckBox first, final CheckBox second, final CheckBox nop, final String ryear,
            final String reCaptchaSiteKey, final String reCaptchaAction) {
		Log.info(entity.getId().getCfecid() + " has navigated to the Billing Panel");
		lyear = (Integer.parseInt(ryear)-2);
		bottomRight.removeAll();
		bottomLeftVPanel.removeAll();
		cfecid.disable();
		DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar2").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar3").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar4").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar5").getStyle().setProperty("background", "#DCDCDC");
		DOM.getElementById("progressBar6").getStyle().setProperty("background", "White");
		/*
		 * Define the NEXT Button
		 */
		next.removeAllListeners();
		next.setEnabled(true);
		next.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				timer.timerCancel();
				timer.progReset();
				timer.setTimer(timer.getTime(), cfecid.getValue().toString());
                if (shipping.getSimpleValue().toString().equalsIgnoreCase("Express")) {
                    first.setValue(true);
                    second.setValue(true);
                    if (first.getValue().equals(true) && second.getValue().equals(true)) {
                        mail = true;
                        cont = true;
                    } else {
                        mail = false;
                        cont = false;
                    }
                } else {
                    first.setValue(false);
                    second.setValue(false);
                    cont = true;
                    mail = true;
                }
                if (cont) {
                    statusBar.setHTML("");
                    //gmsg.waitStart("Please Wait", "Validating Billing Information....", "Progress", 250);
                    if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
                        tfee = Double.parseDouble(getResTotal(feeTotals));
                    } else {
                        tfee = Double.parseDouble(getNonResTotal(feeTotals));
                    }
                    statusBar.setHTML("");

                    String st;
                    if (shipping.getSimpleValue().toString().equalsIgnoreCase("Express")) {
                        st = "em";
                    } else {
                        st = "fm";
                    }
                    payment.setShiptype(st);
                    payment.setTotalamount(Double.toString(tfee));
                    Log.info(entity.getId().getCfecid()+ " has a total payment due of " + tfee);
                    /*
                     * just a time delay test
                     */
                    ScreenConfirmation confirm = new ScreenConfirmation();
                    confirm.getConfirm(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount, NavprogressBarPanel, entity,
                        changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
                    /*
                     * original code without the RPC
                     */
                    //ScreenConfirmation confirm = new ScreenConfirmation();
                    //confirm.getConfirm(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount, NavprogressBarPanel, entity,
                    //	changeList, feeTotals, service, topLeftText, firstTime, timer, payment, results.toString(), plist, vlist, first, second, nop, ryear);
                } else {
                    if (!(tadd)) {
                        statusBar.setHTML("<span class='regred12'>Invalid Temporary Address</span>");
                        gmsg.alert("Invalid Form Data", "If you elected to enter a Temporary Address, all fields must be valid and complete. Fields outlined in red will indicate where the problems are.", 250);
                    } else if (!(mail)) {
                        statusBar.setHTML("<span class='regred12'>Must agree to shipping fees</span>");
                        gmsg.alert("Invalid Form Data", "If you selected express mail, you must agree to the additional service fee and USPS rates by selecting each box.", 250);
                    }
                }
			}
		});
		next.setTabIndex(15);
		/*
		 * Define the LAST Button
		 */
		last.removeAllListeners();
		last.setEnabled(true);
		last.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				timer.timerCancel();
				timer.progReset();
				timer.setTimer(timer.getTime(), cfecid.getValue().toString());
				ScreenPermit permit = new ScreenPermit();
				if (nopmtves) {
					permit.getPermits(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount,
						NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear,
                        reCaptchaSiteKey, reCaptchaAction);
				} else {
					/*payment.setBaddress(taddress.getValue());
					payment.setBcity(tcity.getValue());
					payment.setBstate(tstate.getValue());
					payment.setBzip(tzip.getValue());*/
					permit.getPermits(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount,
						NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear,
                        reCaptchaSiteKey, reCaptchaAction);
				}
			}
		});
		last.setTabIndex(14);

		bottomLeftVPanel.add(startOver);
		statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Billing Information Section</span>");
		if (toI(feeTotals.getVes()) == 0 && toI(feeTotals.getPmt()) == 0) {
			next.setEnabled(false);
			nopmtves = true;
			/*
			 * No permits or vessels, no action
			 */
			bottomLeftVPanel.addText(gins.getBilling5());
			bottomLeftVPanel.layout();
			bottomRight.addText("<br><center><span class='regred12'>*** You have not selected any permits to renew or vessels to license ***</span></center>");
		} else {
			next.setEnabled(true);
			nopmtves = false;

			final FormData formData = new FormData("100%");
		    addressPanel = new FormPanel();
		    addressPanel.setAutoHeight(true);
		    addressPanel.setFrame(false);
		    addressPanel.setBorders(false);
		    addressPanel.setBodyBorder(false);
		    addressPanel.setHeaderVisible(false);
		    addressPanel.setLayout(new FlowLayout());

			ship = new FieldSet();
			ship.setHeadingHtml("<span class='boldorange12'>Shipping Information - </span><span class='regred12'>The Entry Commission does not Express Mail to foreign addresses</span>");
			ship.setAutoHeight(true);
			ship.setAutoWidth(true);
			ship.addStyleName("addressFieldSets");
			ship.setStyleAttribute("margin-left", "-10px");
			ship.setStyleAttribute("margin-right", "-10px");

			ContentPanel ship1 = new ContentPanel();
			ship1.setHeaderVisible(false);
			ship1.setBodyBorder(false);
			ship1.setBorders(false);
			ship1.setFrame(true);
			ship1.setAutoHeight(true);
			ship1.setAutoWidth(true);
			ship1.addStyleName("addressFieldSets");

			LayoutContainer main = new LayoutContainer();
			main.setLayout(new ColumnLayout());

			LayoutContainer main1 = new LayoutContainer();
			main1.setLayout(new ColumnLayout());

			LayoutContainer main2 = new LayoutContainer();
			main2.setLayout(new ColumnLayout());

			LayoutContainer left = new LayoutContainer();
			left.setStyleAttribute("paddingRight", "10px");
			FormLayout layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.LEFT);
			layout.setLabelWidth(58);
			left.setLayout(layout);

			right = new LayoutContainer();
			FormLayout layout4 = new FormLayout();
			right.setLayout(layout4);

			LayoutContainer fee1 = new LayoutContainer();
			FormLayout layoutfee1 = new FormLayout();
			fee1.setLayout(layoutfee1);

			LayoutContainer fee2 = new LayoutContainer();
			FormLayout layoutfee2 = new FormLayout();
			fee2.setLayout(layoutfee2);

			shipping = new SimpleComboBox();
			shipping.setFieldLabel("Shipping");
			shipping.setEditable(false);
			shipping.add("Express");
			shipping.add("First Class");
			shipping.setForceSelection(true);
			shipping.setAllowBlank(false);
			shipping.setAutoHeight(true);
			shipping.setAutoWidth(true);
			shipping.setTriggerAction(TriggerAction.ALL);
			shipping.setTabIndex(1);
			shipping.addListener(Events.Select, new Listener<FieldEvent>() {
				public void handleEvent(FieldEvent be) {
					if (!(shipping.getSimpleValue().toString().equalsIgnoreCase("First Class"))) {
						//Log.info(entity.getId().getCfecid() + " has select express mail");
						if (!(entity.getPaddress2() == null)) {
							//Log.info(entity.getId().getCfecid() + " they have implied a foreign address");
							if (payment.getBaddress().length() > 1) {
								//Log.info(entity.getId().getCfecid() + " and, they have not chosen a US temporary address. Show popup message");
								gmsg.alert("<span class='regred12'>Invalid Express Mail Address</span>", "The Express Mail option is NOT avaialble " +
										"for Foreign Addresses<br>You can either choose to mail to a US Temporary Address (above) or specify a US " +
										"Permanent Address if applicable", 250);
										first.setValue(false);
										second.setValue(false);
										first.setEnabled(false);
										second.setEnabled(false);
										payment.setSerfee("N/A");
										payment.setShipfee("N/A");
										payment.setShiptype("fm");
										shipping.setSimpleValue("First Class");
							} else {
								//Log.info(entity.getId().getCfecid() + " has chosen a US temporary address, all is good, continue");
								first.setEnabled(true);
								second.setEnabled(true);
								payment.setShiptype("em");
								payment.setSerfee("15");
								payment.setShipfee("28.95");
								shipping.setSimpleValue("Express");
							}
						} else {
							//Log.info(entity.getId().getCfecid() + " has not entered a foreign address, continue");
							if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
								if (toD(feeTotals.getResShipping()) == 0.0) {
									double serfee = 15.00;
									double shpfee = 28.95;
									feeTotals.setResShipping((toD(feeTotals.getResShipping()) + serfee + shpfee));
									DOM.getElementById("rs").setInnerText(Double.toString(toD(feeTotals.getResShipping())));
									DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
								}
							} else {
								if (toD(feeTotals.getNonresShipping()) == 0.0) {
									double serfee = 15.00;
									double shpfee = 28.95;
									feeTotals.setNonresShipping((toD(feeTotals.getNonresShipping()) + serfee + shpfee));
									DOM.getElementById("ns").setInnerText(Double.toString(toD(feeTotals.getNonresShipping())));
							        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
								}
							}
							first.setEnabled(true);
							second.setEnabled(true);
							payment.setShiptype("em");
							payment.setSerfee("15");
							payment.setShipfee("28.95");
							shipping.setSimpleValue("Express");
						}
					} else {
						if (entity.getResidency().equalsIgnoreCase("resident") || entity.getResidency().equalsIgnoreCase("R")) {
							if (toD(feeTotals.getResShipping()) > 0.0) {
								feeTotals.setResShipping(0.0);
								DOM.getElementById("rs").setInnerText(Double.toString(toD(feeTotals.getResShipping())));
								DOM.getElementById("rt").setInnerText(getResTotal(feeTotals));
							}
						} else {
							if (toD(feeTotals.getNonresShipping()) > 0.0) {
								feeTotals.setNonresShipping(0.0);
								DOM.getElementById("ns").setInnerText(Double.toString(toD(feeTotals.getNonresShipping())));
						        DOM.getElementById("nt").setInnerText(getNonResTotal(feeTotals));
							}
						}
						first.setValue(false);
						second.setValue(false);
						first.setEnabled(false);
						second.setEnabled(false);
						payment.setSerfee("N/A");
						payment.setShipfee("N/A");
						payment.setShiptype("fm");
						shipping.setSimpleValue("First Class");
					}
				}
	        });
			left.add(shipping, formData);

			if (!(payment.getShiptype() == null)) {
				if (payment.getShiptype().equalsIgnoreCase("em")) {
					shipping.setSimpleValue("Express");
					first.setEnabled(true);
					second.setEnabled(true);
					first.setValue(true);
					second.setValue(true);
				} else {
					shipping.setSimpleValue("First Class");
					first.setEnabled(false);
					second.setEnabled(false);
					first.setValue(false);
					second.setValue(false);
				}
				shipping.setTriggerAction(TriggerAction.ALL);
			} else {
				shipping.setSimpleValue("First Class");
				shipping.setTriggerAction(TriggerAction.ALL);
				first.setEnabled(false);
				second.setEnabled(false);
				first.setValue(false);
				second.setValue(false);
			}

			Html snote = new Html(
					"<span class='regred12'>NOTE:</span><span class='regblack12'> If you select express mail, " +
							"you will be charged a $15.00 service fee AND $28.95 for the current flat rate postage.</span>");
			right.add(snote, formData);

			main.add(left, new ColumnData(0.30));
			main.add(right, new ColumnData(0.68));

			first.setName("first");
			first.setHideLabel(true);
			first.setBoxLabel("Yes, please express mail my documents. I agree to pay the $15.00 service fee.");

			second.setName("second");
			second.setHideLabel(true);
			second.setBoxLabel(
					"Yes, please express mail my documents. I agree to pay $28.95 for the current flat rate of express mail postage.");

			ship1.add(main, new FormData("100%"));

			ship.add(ship1);

			final ContentPanel cred1 = new ContentPanel();
			cred1.setHeaderVisible(false);
			cred1.setBodyBorder(false);
			cred1.setBorders(false);
			cred1.setFrame(true);
			cred1.setAutoHeight(true);
			cred1.setAutoWidth(true);
			cred1.addStyleName("addressFieldSets");

			final LayoutContainer main3 = new LayoutContainer();
			main3.setLayout(new ColumnLayout());

			left = new LayoutContainer();
			left.setStyleAttribute("paddingRight", "10px");
			layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.TOP);
			left.setLayout(layout);

			LayoutContainer middle = new LayoutContainer();
			middle.setStyleAttribute("paddingRight", "10px");
			FormLayout layout2 = new FormLayout();
			layout2.setLabelAlign(LabelAlign.TOP);
			middle.setLayout(layout2);

			final LayoutContainer ename = new LayoutContainer();
			ename.setLayout(new ColumnLayout());

			middle2 = new LayoutContainer();
	        middle2.setStyleAttribute("paddingRight", "10px");
	        FormLayout layout3 = new FormLayout();
	        layout3.setLabelAlign(LabelAlign.TOP);
	        middle2.setLayout(layout3);

	        middle3 = new LayoutContainer();
	        middle3.setStyleAttribute("paddingRight", "10px");
	        layout4 = new FormLayout();
	        layout4.setLabelAlign(LabelAlign.TOP);
	        middle3.setLayout(layout4);

			right = new LayoutContainer();
			layout4 = new FormLayout();
			layout4.setLabelAlign(LabelAlign.TOP);
			right.setLayout(layout4);

			if (!(payment.getCcowner() == null)) {
				if (payment.getCcowner().equalsIgnoreCase("Corporate")) {
					LayoutContainer middle2c = new LayoutContainer();
			        middle2c.setStyleAttribute("paddingRight", "10px");
			        FormLayout layout3c = new FormLayout();
			        layout3c.setLabelAlign(LabelAlign.TOP);
			        middle2c.setLayout(layout3c);
					ename.add(middle2c, new ColumnData(0.99));
					main3.add(left, new ColumnData(0.24));
					main3.add(middle, new ColumnData(0.15));
					main3.add(ename, new ColumnData(0.60));
				} else {
					ename.add(middle2, new ColumnData(0.32));
					ename.add(middle3, new ColumnData(0.16));
					ename.add(right, new ColumnData(0.52));

					main3.add(left, new ColumnData(0.24));
					main3.add(ename, new ColumnData(0.74));
				}
			} else {
				ename.add(middle2, new ColumnData(0.32));
				ename.add(middle3, new ColumnData(0.16));
				ename.add(right, new ColumnData(0.52));

				main3.add(left, new ColumnData(0.24));
				main3.add(ename, new ColumnData(0.74));
			}

			LayoutContainer main4 = new LayoutContainer();
			main4.setLayout(new ColumnLayout());

			LayoutContainer main6 = new LayoutContainer();
			main6.setLayout(new ColumnLayout());

			left = new LayoutContainer();
			left.setStyleAttribute("paddingRight", "10px");
			layout = new FormLayout();
			layout.setLabelAlign(LabelAlign.TOP);
			left.setLayout(layout);

			LayoutContainer middle1 = new LayoutContainer();
			middle1.setStyleAttribute("paddingRight", "10px");
			layout2 = new FormLayout();
			layout2.setLabelAlign(LabelAlign.TOP);
			middle1.setLayout(layout2);

			middle2 = new LayoutContainer();
			middle2.setStyleAttribute("paddingRight", "10px");
			layout3 = new FormLayout();
			layout3.setLabelAlign(LabelAlign.TOP);
			middle2.setLayout(layout3);

			right = new LayoutContainer();
			layout4 = new FormLayout();
			layout4.setLabelAlign(LabelAlign.TOP);
			right.setLayout(layout4);

			LayoutContainer alone = new LayoutContainer();
			FormLayout layout5 = new FormLayout();
			alone.setLayout(layout5);

			main4.add(left, new ColumnData(0.40));
			main4.add(middle1, new ColumnData(0.20));
			main4.add(middle2, new ColumnData(0.19));
			main4.add(right, new ColumnData(0.19));

			cred1.add(main3, new FormData("100%"));
			cred1.add(main4, new FormData("100%"));

			VerticalPanel disclaimers = new VerticalPanel();
			disclaimers.setTableWidth("100%");
			disclaimers.setAutoHeight(true);
			disclaimers.setAutoWidth(true);
			disclaimers.addStyleName("resdis");
			disclaimers.setVerticalAlign(VerticalAlignment.MIDDLE);
			disclaimers.setHorizontalAlign(HorizontalAlignment.CENTER);

			disclaimers.addText("<center><span class='boldred12'>*</span> - indicates a required field");
			alone.add(disclaimers, formData);

			main6.add(alone, new ColumnData(1.00));

			addressPanel.add(ship);

			bottomLeftVPanel.addText(gins.getBilling1());
			bottomLeftVPanel.layout();
			bottomRight.add(addressPanel);
		}

		bottomRight.setHeadingHtml("<span class='boldblack12'>Shipping Information</span>");
		bottomRight.layout();

		if (toI(feeTotals.getVes()) + toI(feeTotals.getPmt()) > 0) {
			ship.collapse();
		}
	}

	public static boolean isValidCC(String number) {
	    final int[][] sumTable = {{0,1,2,3,4,5,6,7,8,9},{0,2,4,6,8,1,3,5,7,9}};
	    int sum = 0, flip = 0;

	    for (int i = number.length() - 1; i >= 0; i--) {
	      sum += sumTable[flip++ & 0x1][Character.digit(number.charAt(i), 10)];
	    }
	    return sum % 10 == 0;
	}
}
