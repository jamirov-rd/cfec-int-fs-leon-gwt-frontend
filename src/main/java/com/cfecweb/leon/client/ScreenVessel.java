package com.cfecweb.leon.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.icons.ClientResources;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewChangesId;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
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
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;

import static com.cfecweb.leon.client.FeeTotalsUtil.getFeeTotals;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ScreenVessel {
	InstructionsText gins = new InstructionsText();
	UserMessages gmsg = new UserMessages();
	HorizontalPanel feetotalPanel = null;
	int vrec = 0;
	Window editVeswin = null;
	FormPanel vchar = null;
	PmtVesSelectFunctions getPmt = new PmtVesSelectFunctions();
	ArenewChanges change = null;
	ArenewChangesId changeId = null;
	ChangeRecorder cr = new ChangeRecorder();
	EditorGrid<BaseModel> grid = null;
	NewVesselBuilder nv = new NewVesselBuilder();
	String areacode = null;
	String oldreg = null;
	String oldp1 = null;
	String oldp2 = null;

	/*
	 * This is the Vessel UI screen, just a grid similar to Permits where
	 * various vessel data is displayed along with a checkbox that indicates the
	 * person wants to license that particular vessel. Also like the permit UI,
	 * the class PmtVesSelectFunctions is called after a selection is made to
	 * update fee totals.
	 */
	@SuppressWarnings("unused")
	public void getVessels(final VerticalPanel bottomLeftVPanel, final FieldSet topLeft, final FieldSet bottomRight, final FieldSet topRight, final Button startOver,
			final TextField cfecid, final Button next, final Button last, final HTML statusBar, final HTML phrdText, final String pmtvesCount,
			final HorizontalPanel NavprogressBarPanel, final ArenewEntity entity, final List<ArenewChanges> changeList, final FeeTotals feeTotals, final getDataAsync service,
			final String topLeftText, final boolean firstTime, final SessionTimer timer, final ArenewPayment payment, final List<ArenewPermits> plist,
			final List<ArenewVessels> vlist, final CheckBox first, final CheckBox second, final CheckBox nop, final String ryear, final String reCaptchaSiteKey, final String reCaptchaAction) {
		Log.info(entity.getId().getCfecid() + " has navigated to the Vessel Panel");
		/*
		 * Define the Vessel page enviroment
		 */
		bottomRight.removeAll();		
		topRight.removeAll();
		feetotalPanel = new HorizontalPanel();
		feetotalPanel.setTableWidth("100%");
		feetotalPanel.setAutoHeight(true);
		feetotalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
		feetotalPanel.setStyleAttribute("padding-bottom", "5px");
		feetotalPanel.addText(getFeeTotals(feeTotals, entity.getResidency()));
        NavprogressBarPanel.add(next);
        topRight.add(feetotalPanel);
        topRight.add(NavprogressBarPanel);
        topRight.layout();		
		bottomLeftVPanel.removeAll();
		bottomLeftVPanel.add(startOver);
		bottomLeftVPanel.addText(gins.getVessels());
		bottomLeftVPanel.layout();
		cfecid.disable();
		statusBar.setHTML("<span class='regblack12'>CFEC Online Renewal Vessel Selection Section</span>");
		DOM.getElementById("progressBar1").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar2").getStyle().setProperty("background", "#FFFFCC");
		DOM.getElementById("progressBar3").getStyle().setProperty("background", "#DCDCDC");
		DOM.getElementById("progressBar4").getStyle().setProperty("background", "White");
		DOM.getElementById("progressBar5").getStyle().setProperty("background", "White");
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
				ScreenPermit gpmt = new ScreenPermit();
				gpmt.getPermits(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, pmtvesCount.toString(), 
					NavprogressBarPanel, entity, changeList, feeTotals, service, topLeftText, firstTime, timer, payment, plist, vlist, first, second, nop, ryear, reCaptchaSiteKey, reCaptchaAction);
			}
		});
		/*
	     * 	Define the LAST Button
	     */	    
		/*
		 * if (!(firstTime)) {
	    	last.enable();
		    last.removeAllListeners();
		    last.enable();
		    last.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
				public void componentSelected(ButtonEvent ce) {
					timer.timerCancel();
		       	    timer.progReset();
		       	    timer.setTimer(timer.getTime(), cfecid.getValue().toString());
		       	    //entity.getArenewVesselses().clear();
		       	    //for (java.util.Iterator<ArenewVessels> it = vlist.iterator(); it.hasNext();) {
		    		//	ArenewVessels vessel = (ArenewVessels) it.next();
		    		//	entity.getArenewVesselses().add(vessel);
		       	    //}
					ScreenAddress gadd = new ScreenAddress();
					gadd.address(bottomLeftVPanel, topLeft, bottomRight, topRight, startOver, cfecid, next, last, statusBar, phrdText, topLeftText, 
						NavprogressBarPanel, entity, service, changeList, timer, payment, feeTotals, plist, vlist);		
				}	    	
		    });
	    } else {
	    	last.setEnabled(false);
	    } 
		*/	    
		last.setEnabled(false);
		
		/*
		 * Build the grid using our vessel list as source file
		 */
		MemoryProxy proxy = new MemoryProxy(vlist);
		BeanModelReader reader = new BeanModelReader();
		final BaseListLoader loader = new BaseListLoader(proxy, reader);
		final ListStore<BaseModel> store = new ListStore<BaseModel>(loader);
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		CheckColumnConfig v1 = new CheckColumnConfig("renewed", "<center><span class='regblue10'>License?</span></center>", 65) {
			@Override
			protected String getCheckState(ModelData model, String property, int rowIndex, int colIndex) {
				/*
				 * return "", "-on", "-disabled", "-disabled-checked",
				 * "disabled-unchecked", or "disabled-nobox"
				 */
				if (store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("completed") 
						|| store.getAt(rowIndex).get("status").toString().trim().equalsIgnoreCase("pending")) {
					return "-disabled-checked";
				} else {
					if (store.getAt(rowIndex).get("renewed").toString().equalsIgnoreCase("true")) {
						return "-on";
					} else {
						return "";
					}
				}
			}
		};
		v1.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v1);
		ColumnConfig v2 = new ColumnConfig("name", "<center><span class='regblue10'>Vessel Name</span></center>", 120);
		v2.setAlignment(HorizontalAlignment.CENTER);
		v2.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v2);
		ColumnConfig v3 = new ColumnConfig("regNum", "<center><span class='regblack10'>Reg. #</span></center>", 65);
		v3.setAlignment(HorizontalAlignment.CENTER);
		v3.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v3);
		ColumnConfig v4 = new ColumnConfig("id.ryear", "<center><span class='regblack10'>Year</span></center>", 65);
		v4.setAlignment(HorizontalAlignment.CENTER);
		v4.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v4);
		ColumnConfig v5 = new ColumnConfig("id.adfg", "<center><span class='regblack10'>ADFG #</span></center>", 65);
		v5.setAlignment(HorizontalAlignment.CENTER);
		v5.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v5);
		ColumnConfig v6 = new ColumnConfig("fee", "<center><span class='regblack10'>Fee</span></center>", 80);
		v6.setAlignment(HorizontalAlignment.CENTER);
		v6.setNumberFormat(NumberFormat.getCurrencyFormat());
		v6.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v6);
		ColumnConfig v7 = new ColumnConfig("details", "<center><span class='regblack10'>Information</span></center>", 150);
		v7.setAlignment(HorizontalAlignment.CENTER);
		v7.setStyle("font: 10px Arial, Helvetica, Tahoma, sans-serif;");
		configs.add(v7);
		ColumnConfig v8 = new ColumnConfig("newVessel", "<center>new</center>", 50);
		ColumnModel cm = new ColumnModel(configs);
		grid = new EditorGrid<BaseModel>(store, cm);
		grid.setAutoExpandColumn("details");
		grid.setBorders(true);
		grid.setAutoHeight(true);
		grid.getView().setForceFit(true);
		grid.addStyleName("tableGrids");
		grid.addPlugin(v1);
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
					public void handleEvent(GridEvent be) {
						String colName = be.getGrid().getColumnModel().getColumnId(be.getColIndex());
						String vadfg = store.getAt(be.getRowIndex()).get("id.adfg").toString();
						String newv = store.getAt(be.getRowIndex()).get("newVessel").toString();
						String vname = store.getAt(be.getRowIndex()).get("name").toString();
						if (colName.equalsIgnoreCase("renewed")) {
							if (be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("on")
									|| be.getTarget().getClassName().substring(36, 38).equalsIgnoreCase("x-")) {
								getPmt.VesselSelect(colName, feeTotals, be, store, entity, grid, vadfg, vname, newv);
							}
						}
						store.commitChanges();
						timer.timerCancel();
						timer.progReset();
						timer.setTimer(timer.getTime(), cfecid.getValue().toString());
					}
				});
		grid.addListener(Events.CellClick, new Listener<GridEvent>() {
			public void handleEvent(final GridEvent be) {
				timer.timerCancel();
				timer.progReset();
				timer.setTimer(timer.getTime(), cfecid.getValue().toString());
				String status = store.getAt(be.getRowIndex()).get("status").toString().trim();
				String renewed = store.getAt(be.getRowIndex()).get("renewed").toString();
				final String adfg = store.getAt(be.getRowIndex()).get("id.adfg").toString();
				String colName = be.getGrid().getColumnModel().getColumnId(be.getColIndex());				
				if (colName.equalsIgnoreCase("details")) {
					if (status.equalsIgnoreCase("available")) {
						if (renewed.equalsIgnoreCase("true")) {
							gmsg.waitStart("Please Wait", "Getting Vessel info....", "Progress", 300);
							/*
							 * Code Split point for selected Vessel modification. 
							 * This section will only load and run IF the cell is clicked,
							 * otherwise it remains dormat and used no client resource.
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
									int y = 0;
									for (Iterator<ArenewVessels> it = vlist.iterator(); it.hasNext();) {
										ArenewVessels vessel = (ArenewVessels) it.next();
										if (vessel.getId().getAdfg().equalsIgnoreCase(adfg)) {
											vrec = y;
											break;
										} else {
											y++;
										}
									}
									if (editVeswin == null) {
										editVeswin = new Window();
										editVeswin.setSize(800, 585);
										editVeswin.setHeadingHtml("Edit Vessel ("+ adfg + ") Characteristics");
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
										vchar = new FormPanel();
										vchar.setHeaderVisible(false);
										vchar.setBorders(false);
										vchar.setLayout(new FormLayout());
										vchar.addText("<center><b>Vessel Characteristics for ADFG " + adfg + "</b><br>The fields that are NOT allowed to be modified are automatically disabled.</center><br>");

										TextField<String> vadfg = new TextField<String>();
										vadfg.setWidth(75);
										vadfg.setValue(vlist.get(vrec).getId().getAdfg());
										vadfg.setEnabled(false);
										final TextField<String> vname = new TextField<String>();
										vname.setWidth(200);
										vname.setAllowBlank(false);
										vname.setValue(vlist.get(vrec).getName());
										vname.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vname.isValid()) {
													cr.getVesselChanges1(entity, "vesselName", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										TextField<String> vuscg = new TextField<String>();
										vuscg.setWidth(75);
										vuscg.setValue(vlist.get(vrec).getRegNum());
										vuscg.setEnabled(false);
										TextField<String> vbuilt = new TextField<String>();
										vbuilt.setWidth(75);
										vbuilt.setValue(vlist.get(vrec).getYearBuilt());
										vbuilt.setEnabled(false);
										TextField<String> vmake = new TextField<String>();
										vmake.setWidth(150);
										vmake.setValue(vlist.get(vrec).getMakeModel());
										vmake.setEnabled(false);
										TextField<String> vlen = new TextField<String>();
										vlen.setWidth(75);
										vlen.setValue(vlist.get(vrec).getLengthFeet()+"' " + vlist.get(vrec).getLengthInches());
										vlen.setEnabled(false);
										TextField<String> vfee = new TextField<String>();
										vfee.setWidth(75);
										String sfee = (vlist.get(vrec).getFee());
										vfee.setValue(sfee);
										vfee.setEnabled(false);
										final TextField<String> vgton = new TextField<String>();
										vgton.setWidth(50);
										vgton.setValue(vlist.get(vrec).getGrossTons());
										//vgton.addListener(Events.Change, new Listener<FieldEvent>() {
										//	@Override
										//	public void handleEvent(FieldEvent be) {
										//		if (vgton.isValid()) {
										//			cr.getVesselChanges1(entity, "grossTons", cfecid.getValue().toString(), be, changeList, adfg);
										//		}
										//	}
										//});
										vgton.setEnabled(false);
										final TextField<String> vnton = new TextField<String>();
										vnton.setWidth(50);
										vnton.setValue(vlist.get(vrec).getNetTons());
										//vnton.addListener(Events.Change, new Listener<FieldEvent>() {
										//	@Override
										//	public void handleEvent(FieldEvent be) {
										//		if (vnton.isValid()) {
										//			cr.getVesselChanges1(entity, "netTons", cfecid.getValue().toString(), be, changeList, adfg);
										//		}
										//	}
										//});
										vnton.setEnabled(false);
										final TextField<String> vhpc = new TextField<String>();
										vhpc.setWidth(100);
										vhpc.setAllowBlank(false);
										vhpc.setValue(vlist.get(vrec).getHomeportCity());
										vhpc.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vhpc.isValid()) {
													cr.getVesselChanges1(entity, "homeportCity", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vhps = new TextField<String>();
										vhps.setWidth(50);
										vhps.setAllowBlank(false);
										vhps.setValue(vlist.get(vrec).getHomeportState());
										vhps.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vhps.isValid()) {
													cr.getVesselChanges1(entity, "homeportState", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vengine = new TextField<String>();
										vengine.setWidth(65);
										vengine.setAllowBlank(false);
										vengine.setValue(vlist.get(vrec).getEngineType());
										vengine.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vengine.isValid()) {
													cr.getVesselChanges1(entity, "engineType", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vhp = new TextField<String>();
										vhp.setWidth(60);
										vhp.setValue(vlist.get(vrec).getHorsepower());
										vhp.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vhp.isValid()) {
													cr.getVesselChanges1(entity, "horsePower", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vvalue = new TextField<String>();
										vvalue.setWidth(65);
										vvalue.setValue(vlist.get(vrec).getEstValue());
										vvalue.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vvalue.isValid()) {
													cr.getVesselChanges1(entity, "estValue", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										TextField<String> vhtype = new TextField<String>();
										vhtype.setWidth(100);
										vhtype.setValue(vlist.get(vrec).getHullType());
										vhtype.setEnabled(false);
										TextField<String> vhid = new TextField<String>();
										vhid.setWidth(100);
										vhid.setValue(vlist.get(vrec).getHullId());
										vhid.setEnabled(false);
										final TextField<String> vfuel = new TextField<String>();
										vfuel.setWidth(45);
										vfuel.setValue(vlist.get(vrec).getFuel());
										vfuel.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vfuel.isValid()) {
													cr.getVesselChanges1(entity, "fuelCapacity", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vref = new TextField<String>();
										vref.setWidth(40);
										vref.setValue(vlist.get(vrec).getRefrigeration());
										vref.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vref.isValid()) {
													cr.getVesselChanges1(entity, "refrigeration", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vlive = new TextField<String>();
										vlive.setWidth(45);
										vlive.setValue(vlist.get(vrec).getLiveTank());
										vlive.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vlive.isValid()) {
													cr.getVesselChanges1(entity, "liveCapacity", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final TextField<String> vhold = new TextField<String>();
										vhold.setWidth(45);
										vhold.setValue(vlist.get(vrec).getHoldTank());
										vhold.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vhold.isValid()) {
													cr.getVesselChanges1(entity, "holdCapacity", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final CheckBox strhand = new CheckBox();
										strhand.setBoxLabel("Hand");
										final CheckBox strpower = new CheckBox();
										strpower.setBoxLabel("Power");
										//final RadioGroup vstr = new RadioGroup();
										//vstr.add(strhand);
										//vstr.add(strpower);
										if (!(vlist.get(vrec).getSalmontrollReg() == null)) {
											if (vlist.get(vrec).getSalmontrollReg().equalsIgnoreCase("p")) {
												strpower.setValue(true);
											} else {
												strpower.setValue(false);
											}
											if (vlist.get(vrec).getSalmontrollReg().equalsIgnoreCase("h")) {
												strhand.setValue(true);
											} else {
												strhand.setValue(false);
											}
										}
										strhand.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges4(entity, "salmonTrollReg", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg, "Hand");
												if (strhand.getValue()) {
													strpower.setValue(false);
												}
											}						
										});
										strpower.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges4(entity, "salmonTrollReg", cfecid.getValue().toString().toUpperCase(), be, changeList, adfg, "Power");
												if (strpower.getValue()) {
													strhand.setValue(false);
												}
											}						
										});												
										final DateField vstrdate = new DateField();
										vstrdate.getPropertyEditor().setFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
										Date today = new Date();
										vstrdate.setMinValue(today);
										DateTimeFormat dtformat = DateTimeFormat.getFormat("yyyy-MM-dd");
										//System.out.println(vlist.get(vrec).getSalmontrollDate());
										if (!(vlist.get(vrec).getSalmontrollDate() == null)) {
											if (!(vlist.get(vrec).getSalmontrollDate().equalsIgnoreCase("N/A"))) {
												if (dtformat.parse(vlist.get(vrec).getSalmontrollDate()).before(today)) {
													cr.getVesselChanges3(entity, "salmonTrollDate", cfecid.getValue().toString().toUpperCase(), dtformat.parse(vlist.get(vrec).getSalmontrollDate()), today, changeList, adfg);
													vstrdate.setValue(today);
												} else {
													vstrdate.setValue(dtformat.parse(vlist.get(vrec).getSalmontrollDate()));
												}
											}
										}
										vstrdate.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												if (vstrdate.isValid()) { 
													cr.getVesselChanges1(entity, "salmonTrollDate", cfecid.getValue().toString(), be, changeList, adfg);
												}
											}
										});
										final CheckBox ps = new CheckBox();
										if (vlist.get(vrec).getPurseseine().equalsIgnoreCase("x")) {
											ps.setValue(true);
										} else {
											ps.setValue(false);
										}
										ps.addListener(Events.OnClick, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "purseSeine", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox bs = new CheckBox();
										if (vlist.get(vrec).getBeachseine().equalsIgnoreCase("x")) {
											bs.setValue(true);
										} else {
											bs.setValue(false);
										}
										bs.addListener(Events.OnClick, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "beachSeine", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox dgn = new CheckBox();
										if (vlist.get(vrec).getDriftgillnet().equalsIgnoreCase("x")) {
											dgn.setValue(true);
										} else {
											dgn.setValue(false);
										}
										dgn.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "driftGillNet", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox sgn = new CheckBox();
										if (vlist.get(vrec).getSetgillnet().equalsIgnoreCase("x")) {
											sgn.setValue(true);
										} else {
											sgn.setValue(false);
										}
										sgn.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "setGillNet", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox ht = new CheckBox();
										if (vlist.get(vrec).getHandtroll().equalsIgnoreCase("x")) {
											ht.setValue(true);
										} else {
											ht.setValue(false);
										}
										ht.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "handTroll", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox ll = new CheckBox();
										if (vlist.get(vrec).getLongline().equalsIgnoreCase("x")) {
											ll.setValue(true);
										} else {
											ll.setValue(false);
										}
										ll.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "longLine", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox fw = new CheckBox();
										if (vlist.get(vrec).getFishwheel().equalsIgnoreCase("x")) {
											fw.setValue(true);
										} else {
											fw.setValue(false);
										}
										fw.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "fishWheel", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox sot = new CheckBox();
										if (vlist.get(vrec).getSingleottertrawl().equalsIgnoreCase("x")) {
											sot.setValue(true);
										} else {
											sot.setValue(false);
										}
										sot.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "singleOtterTrawl", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox p = new CheckBox();
										if (vlist.get(vrec).getPotgear().equalsIgnoreCase("x")) {
											p.setValue(true);
										} else {
											p.setValue(false);
										}
										p.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "potGear", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox rn = new CheckBox();
										if (vlist.get(vrec).getRingnet().equalsIgnoreCase("x")) {
											rn.setValue(true);
										} else {
											rn.setValue(false);
										}
										rn.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "ringNet", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox d = new CheckBox();
										if (vlist.get(vrec).getDivegear().equalsIgnoreCase("x")) {
											d.setValue(true);
										} else {
											d.setValue(false);
										}
										d.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "diveGear", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox pt = new CheckBox();
										if (vlist.get(vrec).getPowertroll().equalsIgnoreCase("x")) {
											pt.setValue(true);
										} else {
											pt.setValue(false);
										}
										pt.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "powerTroll", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox bt = new CheckBox();
										if (vlist.get(vrec).getBeamtrawl().equalsIgnoreCase("x")) {
											bt.setValue(true);
										} else {
											bt.setValue(false);
										}
										bt.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "beamTrawl", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox sd = new CheckBox();
										if (vlist.get(vrec).getDredge().equalsIgnoreCase("x")) {
											sd.setValue(true);
										} else {
											sd.setValue(false);
										}
										sd.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "dredge", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox db = new CheckBox();
										if (vlist.get(vrec).getDinglebar().equalsIgnoreCase("x")) {
											db.setValue(true);
										} else {
											db.setValue(false);
										}
										db.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "dingleBar", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox mj = new CheckBox();
										if (vlist.get(vrec).getJig().equalsIgnoreCase("x")) {
											mj.setValue(true);
										} else {
											mj.setValue(false);
										}
										mj.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "jig", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox dot = new CheckBox();
										if (vlist.get(vrec).getDoubleottertrawl().equalsIgnoreCase("x")) {
											dot.setValue(true);
										} else {
											dot.setValue(false);
										}
										dot.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "doubleOtterTrawl", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox hg = new CheckBox();
										if (vlist.get(vrec).getHearinggillnet().equalsIgnoreCase("x")) {
											hg.setValue(true);
										} else {
											hg.setValue(false);
										}
										hg.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "herringGillNet", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox ptr = new CheckBox();
										if (vlist.get(vrec).getPairtrawl().equalsIgnoreCase("x")) {
											ptr.setValue(true);
										} else {
											ptr.setValue(false);
										}
										ptr.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "pairTrawl", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox og = new CheckBox();
										if (vlist.get(vrec).getOthergear().equalsIgnoreCase("x")) {
											og.setValue(true);
										} else {
											og.setValue(false);
										}
										og.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "otherGear", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox cf = new CheckBox();
										if (vlist.get(vrec).getFishingboat().equalsIgnoreCase("x")) {
											cf.setValue(true);
										} else {
											cf.setValue(false);
										}
										cf.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "fishingBoat", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox pr = new CheckBox();
										if (vlist.get(vrec).getFreezerCanner().equalsIgnoreCase("x")) {
											pr.setValue(true);
										} else {
											pr.setValue(false);
										}
										pr.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "freezerCanner", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox tp = new CheckBox();
										if (vlist.get(vrec).getTenderPacker().equalsIgnoreCase("x")) {
											tp.setValue(true);
										} else {
											tp.setValue(false);
										}
										tp.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "tenderPacker", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final CheckBox ta = new CheckBox();
										if (vlist.get(vrec).getTransporter().equalsIgnoreCase("x")) {
											ta.setValue(true);
										} else {
											ta.setValue(false);
										}
										ta.addListener(Events.Change, new Listener<FieldEvent>() {
											@Override
											public void handleEvent(FieldEvent be) {
												cr.getVesselChanges2(entity, "transporter", cfecid.getValue().toString(), be, changeList, adfg);
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
										if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("N/A")) {
											snra1.setSimpleValue("None");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("A")) {
											snra1.setSimpleValue("Southeast (A)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("E")) {
											snra1.setSimpleValue("Prince William Sound (E)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("H")) {
											snra1.setSimpleValue("Cook Inlet (H)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("K")) {
											snra1.setSimpleValue("Kodiak (K)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("L")) {
											snra1.setSimpleValue("Chignik (L)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("M")) {
											snra1.setSimpleValue("AK Peninsula/Aleutians (M)");
										} else if (vlist.get(vrec).getSalmonregArea().equalsIgnoreCase("T")) {
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
												cr.getVesselChanges5(entity, "salmonNetRegArea", cfecid.getValue().toString(), snra1.getSimpleValue().toString(), oldreg, changeList, adfg);
											}
										});
										final TextField<String> snrp1 = new TextField<String>();
										snrp1.setWidth(75);
										if (!(vlist.get(vrec).getPermitSerial1() == null)) {
											if (!(vlist.get(vrec).getPermitSerial1().equalsIgnoreCase("N/A"))) {
												snrp1.setValue(vlist.get(vrec).getPermitSerial1());
											}
										}	
										//snrp1.setValue(vlist.get(vrec).getPermitSerial1());
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
												cr.getVesselChanges1(entity, "salmonPermit1", cfecid.getValue().toString(), be, changeList, adfg);
											}
										});
										final TextField<String> snrp2 = new TextField<String>();
										snrp2.setWidth(75);
										if (!(vlist.get(vrec).getPermitSerial2() == null)) {
											if (!(vlist.get(vrec).getPermitSerial2().equalsIgnoreCase("N/A"))) {
												snrp2.setValue(vlist.get(vrec).getPermitSerial2());
											}
										}
										//snrp2.setValue(vlist.get(vrec).getPermitSerial2());
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
												cr.getVesselChanges1(entity, "salmonPermit2", cfecid.getValue().toString(), be, changeList, adfg);
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
										//vc6.add(vstr);
										vc6.add(strhand);
										vc6.add(strpower);
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
												timer.setTimer(timer.getTime(), cfecid.getValue().toString());
												if (strhand.getValue() && (!(ht.getValue()))) {
													gmsg.alert("<span class='regred12'>Illegal selection</span>", "You have a Salmon Troll type of HAND TROLL," +
															" you must also select the corresponding Gear Type.", 300);
												} else if (strpower.getValue() && (!(pt.getValue()))) {
													gmsg.alert("<span class='regred12'>Illegal selection</span>", "You have a Salmon Troll type of POWER TROLL," +
															" you must also select the corresponding Gear Type.", 300);
												} else {
													if (vchar.isValid()) {
														if (!(vname.getValue() == null)) {
															vlist.get(vrec).setName(vname.getValue());
														} else {
															vlist.get(vrec).setName("N/A");
														}
														if (!(vgton.getValue() == null)) {
															vlist.get(vrec).setGrossTons(vgton.getValue());
														} else {
															vlist.get(vrec).setGrossTons("0");
														}
														if (!(vnton.getValue() == null)) {
															vlist.get(vrec).setNetTons(vnton.getValue());
														} else {
															vlist.get(vrec).setNetTons("0");
														}
														if (!(vhpc.getValue() == null)) {
															vlist.get(vrec).setHomeportCity(vhpc.getValue());
														} else {
															vlist.get(vrec).setHomeportCity("N/A");
														}
														if (!(vhps.getValue() == null)) {																				
															vlist.get(vrec).setHomeportState(vhps.getValue());
														} else {
															vlist.get(vrec).setHomeportState("N/A");
														}
														if (!(vengine.getValue() == null)) {
															vlist.get(vrec).setEngineType(vengine.getValue());
														} else {
															vlist.get(vrec).setEngineType("N/A");
														}
														if (!(vhp.getValue() == null)) {
															vlist.get(vrec).setHorsepower(vhp.getValue());
														} else {
															vlist.get(vrec).setHorsepower("0");
														}
														if (!(vvalue.getValue() == null)) {
															vlist.get(vrec).setEstValue(vvalue.getValue());
														} else {
															vlist.get(vrec).setEstValue("0");
														}
														if (!(vfuel.getValue() == null)) {
															vlist.get(vrec).setFuel(vfuel.getValue());
														} else {
															vlist.get(vrec).setFuel("0");
														}
														if (!(vref.getValue() == null)) {
															vlist.get(vrec).setRefrigeration(vref.getValue());
														} else {
															vlist.get(vrec).setRefrigeration("N/A");
														}
														if (!(vlive.getValue() == null)) {
															vlist.get(vrec).setLiveTank(vlive.getValue());
														} else {
															vlist.get(vrec).setLiveTank("0");
														}
														if (!(vhold.getValue() == null)) {
															vlist.get(vrec).setHoldTank(vhold.getValue());
														} else {
															vlist.get(vrec).setHoldTank("0");
														}
														if (strpower.getValue()) {
															vlist.get(vrec).setSalmontrollReg("P");
														} else if (strhand.getValue()) {
															vlist.get(vrec).setSalmontrollReg("H");
														} else {
															vlist.get(vrec).setSalmontrollReg("N/A");
														}
														if (vstrdate.getValue() != null) {
															vlist.get(vrec).setSalmontrollDate(DateTimeFormat.getFormat("yyyy-MM-dd").format(vstrdate.getValue()));
														} else {
															vlist.get(vrec).setSalmontrollDate("N/A");
														}
														if (ps.getValue()) {
															vlist.get(vrec).setPurseseine("X");
														} else {
															vlist.get(vrec).setPurseseine("N");
														}
														if (bs.getValue()) {
															vlist.get(vrec).setBeachseine("X");
														} else {
															vlist.get(vrec).setBeachseine("N");
														}
														if (dgn.getValue()) {
															vlist.get(vrec).setDriftgillnet("X");
														} else {
															vlist.get(vrec).setDriftgillnet("N");
														}
														if (sgn.getValue()) {
															vlist.get(vrec).setSetgillnet("X");
														} else {
															vlist.get(vrec).setSetgillnet("N");
														}
														if (ht.getValue()) {
															vlist.get(vrec).setHandtroll("X");
														} else {
															vlist.get(vrec).setHandtroll("N");
														}
														if (ll.getValue()) {
															vlist.get(vrec).setLongline("X");
														} else {
															vlist.get(vrec).setLongline("N");
														}
														if (fw.getValue()) {
															vlist.get(vrec).setFishwheel("X");
														} else {
															vlist.get(vrec).setFishwheel("N");
														}
														if (sot.getValue()) {
															vlist.get(vrec).setSingleottertrawl("X");
														} else {
															vlist.get(vrec).setSingleottertrawl("N");
														}
														if (p.getValue()) {
															vlist.get(vrec).setPotgear("X");
														} else {
															vlist.get(vrec).setPotgear("N");
														}
														if (rn.getValue()) {
															vlist.get(vrec).setRingnet("X");
														} else {
															vlist.get(vrec).setRingnet("N");
														}
														if (d.getValue()) {
															vlist.get(vrec).setDivegear("X");
														} else {
															vlist.get(vrec).setDivegear("N");
														}
														if (pt.getValue()) {
															vlist.get(vrec).setPowertroll("X");
														} else {
															vlist.get(vrec).setPowertroll("N");
														}
														if (bt.getValue()) {
															vlist.get(vrec).setBeamtrawl("X");
														} else {
															vlist.get(vrec).setBeamtrawl("N");
														}
														if (sd.getValue()) {
															vlist.get(vrec).setDredge("X");
														} else {
															vlist.get(vrec).setDredge("N");
														}
														if (db.getValue()) {
															vlist.get(vrec).setDinglebar("X");
														} else {
															vlist.get(vrec).setDinglebar("N");
														}
														if (mj.getValue()) {
															vlist.get(vrec).setJig("X");
														} else {
															vlist.get(vrec).setJig("N");
														}
														if (dot.getValue()) {
															vlist.get(vrec).setDoubleottertrawl("X");
														} else {
															vlist.get(vrec).setDoubleottertrawl("N");
														}
														if (hg.getValue()) {
															vlist.get(vrec).setHearinggillnet("X");
														} else {
															vlist.get(vrec).setHearinggillnet("N");
														}
														if (ptr.getValue()) {
															vlist.get(vrec).setPairtrawl("X");
														} else {
															vlist.get(vrec).setPairtrawl("N");
														}
														if (og.getValue()) {
															vlist.get(vrec).setOthergear("X");
														} else {
															vlist.get(vrec).setOthergear("N");
														}
														if (cf.getValue()) {
															vlist.get(vrec).setFishingboat("X");
														} else {
															vlist.get(vrec).setFishingboat("N");
														}
														if (pr.getValue()) {
															vlist.get(vrec).setFreezerCanner("X");
														} else {
															vlist.get(vrec).setFreezerCanner("N");
														}
														if (tp.getValue()) {
															vlist.get(vrec).setTenderPacker("X");
														} else {
															vlist.get(vrec).setTenderPacker("N");
														}
														if (ta.getValue()) {
															vlist.get(vrec).setTransporter("X");
														} else {
															vlist.get(vrec).setTransporter("N");
														}
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
															// nves.setSalmonregArea(areacode);
															vlist.get(vrec).setSalmonregArea(areacode);
														}// else
															// {
															// vlist.get(vrec).setSalmonregArea("N/A");
															// }
														if (!(snrp1.getValue() == null)) {
															vlist.get(vrec).setPermitSerial1(snrp1.getValue());
														} else {
															vlist.get(vrec).setPermitSerial1("N/A");
														}
														if (!(snrp2.getValue() == null)) {
															vlist.get(vrec).setPermitSerial2(snrp2.getValue());
														} else {
															vlist.get(vrec).setPermitSerial2("N/A");
														}
														store.getAt(be.getRowIndex()).set("details", "<span class='regblue10' style='cursor: pointer; cursor: hand;'>Click here to edit vessel</span>&nbsp;&nbsp;<span class='regred12'>* Modified *</span>");
														editVeswin.hide();
														editVeswin = null;
													}
												}												
											}
										}));
										editVeswin.addButton(new Button("Cancel and Return", new SelectionListener<ButtonEvent>() {
												public void componentSelected(ButtonEvent ce) {
													timer.timerCancel();
													timer.progReset();
													timer.setTimer(timer.getTime(), cfecid.getValue().toString());
													editVeswin.hide();
													editVeswin = null;
												}
											}));
										editVeswin.show();
									}
								}
							});
						} else {
							gmsg.alert("<span class='regred12'>Vessel not selected</span>", "Before Vessel characteristics for ADFG " + adfg
								+ " can be modified, you must first select the Vessel to license", 300);
						}
					} else {
						gmsg.alert("<span class='regred12'>Vessel already licensed</span>", "This Vessel, ADFG " + adfg
								+ ", has already been licensed for this year and cannot be modified here", 300);
					}
				}
			}
		});
		ContentPanel cp = new ContentPanel();
		cp.setHeadingHtml("<center><span class='regblack12'>Your Vessels - Select Vessels to license</span></center>");
		cp.setFrame(true);
		cp.setAutoHeight(true);
		cp.setAutoWidth(true);
		cp.setButtonAlign(HorizontalAlignment.CENTER);

		final Button newves = new Button();
		newves.setHtml("<span class='boldred10'>CLICK HERE to add a new vessel</span>");
		newves.setTitle("Click this button to add a new or currently un-licensed Vessel to your inventory");
		newves.setIcon(ClientResources.ICONS.add());
		newves.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent ce) {
				gmsg.waitStart("Please Wait", "Getting new Vessel info....", "Progress", 300);
				timer.timerCancel();
				timer.progReset();
				timer.setTimer(timer.getTime(), cfecid.getValue().toString());
				Window doesexist = nv.doesExist(gmsg, timer, cfecid, statusBar, gins, entity, vlist, grid, loader, bottomRight, 
					newves, feeTotals, service, entity.getId().getRyear(), changeList, topRight);
				gmsg.waitStop();
				doesexist.show();
			}
		});

		ButtonBar pbb = new ButtonBar();
		pbb.setAlignment(HorizontalAlignment.CENTER);
		pbb.add(newves);

		cp.add(pbb);
		cp.add(grid);
		cp.addText("<center><span class='regred12'>To edit/modify Vessel details, the Vessel MUST first be selected to license</span></center>");

		if (vlist.size() < 1) {
			bottomRight.add(cp);
			gmsg.alert("No Vessels", "<span class='regblack12'><b>You have no Vessels to license</b> - You can either.....<br><br>1.  Add a new vessel by selecting the 'CLICK HERE to add a new vessel' button, OR<br>2. Press the <span class='regred12'>Next >></span> button to proceed to permits.</span>",	300);
		} else {
			bottomRight.add(cp);
		}
		
		bottomRight.setHeadingHtml("<span class='boldblack12'>Vessel Licensing selection</b></span>");
		bottomRight.layout();
	}
}
