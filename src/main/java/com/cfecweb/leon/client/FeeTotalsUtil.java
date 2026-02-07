package com.cfecweb.leon.client;

import com.cfecweb.leon.client.model.FeeTotals;

/** Contains special methods from removed FeeTotals class */
public class FeeTotalsUtil {
    public static int toI(Integer i) {
        if (i == null) { return 0; } else { return i; }
    }

    public static double toD(Double d) {
        if (d == null) { return 0.0; } else { return d; }
    }

    public static boolean toB(Boolean b) {
        if (b == null) { return false; } else { return b; }
    }

    public static void vesPlus(FeeTotals feeTotals) {
        feeTotals.ves(toI(feeTotals.getVes()) + 1);
    }

    public static void vesMinus(FeeTotals feeTotals) {
        feeTotals.ves(toI(feeTotals.getVes()) - 1);
    }

    public static void pmtPlus(FeeTotals feeTotals) {
        feeTotals.pmt(toI(feeTotals.getPmt()) + 1);
    }

    public static void pmtMinus(FeeTotals feeTotals) {
        feeTotals.pmt(toI(feeTotals.getPmt()) - 1);
    }

    public static void diffPlusc(FeeTotals feeTotals) {
        feeTotals.diffc(toI(feeTotals.getDiffc()) + 1);
    }

    public static void diffMinusc(FeeTotals feeTotals) {
        feeTotals.diffc(toI(feeTotals.getDiffc()) - 1);
    }

    public static void diffPlusp(FeeTotals feeTotals) {
        feeTotals.diffp2(toI(feeTotals.getDiffp()) + 1);
    }

    public static void diffMinusp(FeeTotals feeTotals) {
        feeTotals.diffp(toI(feeTotals.getDiffp()) - 1);
    }

    public static void diffPlusp2(FeeTotals feeTotals) {
        feeTotals.diffp2(toI(feeTotals.getDiffp2()) + 1);
    }

    public static void diffMinusp2(FeeTotals feeTotals) {
        feeTotals.diffp2(toI(feeTotals.getDiffp2()) - 1);
    }

    public static String round(String tot) {
        String newtotal = null;
        if (tot.indexOf(".") != -1) {
            newtotal = null;
            String remain = tot.substring(tot.indexOf(".")+1, tot.length());
            String begin = tot.substring(0, tot.indexOf("."));
            int total = remain.length();
            if (remain.trim().length() == 1) {
                newtotal = tot + "0";
            } else if (remain.trim().length() == 2) {
                newtotal = tot;
            } else {
                int firstI = Integer.parseInt(remain.substring(0, 1));
                int secondI = Integer.parseInt(remain.substring(1, 2));
                int thirdI = Integer.parseInt(remain.substring(2, 3));
                if (total > 2) {
                    if (thirdI >= 5) {
                        newtotal = begin+"."+Integer.toString(firstI)+Integer.toString(secondI+1);
                    } else {
                        newtotal = begin+"."+Integer.toString(firstI)+Integer.toString(secondI);
                    }
                } else {
                    newtotal = tot;
                }
            }
        } else {
            newtotal = tot;
        }
        return newtotal;
    }

    public static String getResTotal(FeeTotals feeTotals) {
        double restot = (toD(feeTotals.getResFishingPermits()) + toD(feeTotals.getResVessels()) + toD(feeTotals.getResShipping()));
        String rtot = Double.toString(restot);
        if (rtot.indexOf(".") != -1) {
            rtot = round(rtot);
        }
        return rtot;
    }

    public static String getNonResTotal(FeeTotals feeTotals) {
        double nonrestot = (toD(feeTotals.getNonresFishingPermits()) + toD(feeTotals.getNonresDifferential())
                + toD(feeTotals.getNonresVessels()) + toD(feeTotals.getNonresShipping()));
        String ntot = Double.toString(nonrestot);
        if (ntot.indexOf(".") != -1) {
            ntot = round(ntot);
        }
        return ntot;
    }

    public static String getFeeTotals(FeeTotals feeTotals, String res) {
        StringBuffer ft = new StringBuffer();
        if (res.equalsIgnoreCase("resident") || res.equalsIgnoreCase("R")) {
            double restot = (toD(feeTotals.getResFishingPermits()) + toD(feeTotals.getResDifferential())
                    + toD(feeTotals.getResVessels()) + toD(feeTotals.getResShipping()));
            String rtot = Double.toString(restot);
            ft.append("<table width='100%' border='0' cellspacing='0' align='center'>");
            ft.append("<tr><td class='boldblack12' width='15%'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Alaska Resident</td>" +
                    "<td align='center' class='regblack12' width='22%'>Fishing Permits:&nbsp;&nbsp$<span id='rfp'><i>"+toD(feeTotals.getResFishingPermits())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='16%'>Non-Res Differential:&nbsp;&nbsp;$<span id='rd'><i>"+toD(feeTotals.getResDifferential())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='14%'>Vessels:&nbsp;&nbsp;$<span id='rv'><i>"+toD(feeTotals.getResVessels())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='18%'>Shipping:&nbsp;&nbsp;$<span id ='rs'><i>"+toD(feeTotals.getResShipping())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='15%'>Total:&nbsp;&nbsp;$<span id='rt'><i>"+rtot+"</i></span></td></tr></table>");
        } else {
            double nonrestot = (toD(feeTotals.getNonresFishingPermits()) + toD(feeTotals.getNonresDifferential())
                    + toD(feeTotals.getNonresVessels()) + toD(feeTotals.getNonresShipping()));
            String ntot = Double.toString(nonrestot);
            ft.append("<table width='100%' border='0' cellspacing='0' align='center'>");
            ft.append("<tr><td class='boldblack12' width='15%'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nonresident</td>" +
                    "<td align='center' class='regblack12' width='22%'>Fishing Permits:&nbsp;&nbsp$<span id='nfp'><i>"+toD(feeTotals.getNonresFishingPermits())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='16%'>Non-Res Differential:&nbsp;&nbsp;$<span id='nd'><i>"+toD(feeTotals.getNonresDifferential())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='14%'>Vessels:&nbsp;&nbsp;$<span id='nv'><i>"+toD(feeTotals.getNonresVessels())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='18%'>Shipping:&nbsp;&nbsp;$<span id ='ns'><i>"+toD(feeTotals.getNonresShipping())+"</i></span></td>" +
                    "<td align='center' class='regblack12' width='15%'>Total:&nbsp;&nbsp;$<span id='nt'><i>"+ntot+"</i></span></td></tr></table>");
        }
        return ft.toString();
    }
}
