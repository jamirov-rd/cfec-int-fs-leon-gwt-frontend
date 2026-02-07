package com.cfecweb.leon.server;

import com.cfecweb.leon.client.model.ClientPaymentContext;
import com.cfecweb.leon.client.model.FeeTotals;
import com.cfecweb.leon.client.model.ArenewChanges;
import com.cfecweb.leon.client.model.ArenewEntity;
import com.cfecweb.leon.client.model.ArenewPayment;
import com.cfecweb.leon.client.model.ArenewPermits;
import com.cfecweb.leon.client.model.ArenewVessels;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.annotation.Nullable;

public class StoredPaymentContext implements Serializable {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private ArenewEntity entity;
    private ArenewPayment pay;
    private List<ArenewChanges> chg;
    private List<ArenewPermits> plist;
    private List<ArenewVessels> vlist;
    private List<ArenewPermits> pclist;
    private List<ArenewVessels> vclist;
    private boolean halred;
    private boolean sabred;
    private FeeTotals feeTotals;
    private boolean firstTime;
    private String ryear;
    private @Nullable Map<String, String> postParameters;
    private String pmtvesCount;
    private String topLeftText;

    public StoredPaymentContext() {}

    public StoredPaymentContext(ArenewEntity entity, ArenewPayment pay, List<ArenewChanges> chg,
                                List<ArenewPermits> plist, List<ArenewVessels> vlist,
                                List<ArenewPermits> pclist, List<ArenewVessels> vclist,
                                boolean halred, boolean sabred, FeeTotals feeTotals, boolean firstTime, String ryear,
                                String pmtvesCount, String topLeftText) {
        this.entity = entity;
        this.pay = pay;
        this.chg = chg;
        this.plist = plist;
        this.vlist = vlist;
        this.pclist = pclist;
        this.vclist = vclist;
        this.halred = halred;
        this.sabred = sabred;
        this.feeTotals = feeTotals;
        this.firstTime = firstTime;
        this.ryear = ryear;
        this.pmtvesCount = pmtvesCount;
        this.topLeftText = topLeftText;
    }

    public ArenewEntity getEntity() {
        return entity;
    }

    public void setEntity(ArenewEntity entity) {
        this.entity = entity;
    }

    public ArenewPayment getPay() {
        return pay;
    }

    public void setPay(ArenewPayment pay) {
        this.pay = pay;
    }

    public List<ArenewChanges> getChg() {
        return chg;
    }

    public void setChg(List<ArenewChanges> chg) {
        this.chg = chg;
    }

    public List<ArenewPermits> getPlist() {
        return plist;
    }

    public void setPlist(List<ArenewPermits> plist) {
        this.plist = plist;
    }

    public List<ArenewVessels> getVlist() {
        return vlist;
    }

    public void setVlist(List<ArenewVessels> vlist) {
        this.vlist = vlist;
    }

    public List<ArenewPermits> getPclist() {
        return pclist;
    }

    public void setPclist(List<ArenewPermits> pclist) {
        this.pclist = pclist;
    }

    public List<ArenewVessels> getVclist() {
        return vclist;
    }

    public void setVclist(List<ArenewVessels> vclist) {
        this.vclist = vclist;
    }

    @Nullable
    public Map<String, String> getPostParameters() {
        return postParameters;
    }

    public void setPostParameters(@Nullable Map<String, String> postParameters) {
        this.postParameters = postParameters;
    }

    public boolean isHalred() {
        return halred;
    }

    public void setHalred(boolean halred) {
        this.halred = halred;
    }

    public boolean isSabred() {
        return sabred;
    }

    public void setSabred(boolean sabred) {
        this.sabred = sabred;
    }

    public FeeTotals getFeeTotals() {
        return feeTotals;
    }

    public void setFeeTotals(FeeTotals feeTotals) {
        this.feeTotals = feeTotals;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public String getRyear() {
        return ryear;
    }

    public void setRyear(String ryear) {
        this.ryear = ryear;
    }

    public String getPmtvesCount() {
        return pmtvesCount;
    }

    public void setPmtvesCount(String pmtvesCount) {
        this.pmtvesCount = pmtvesCount;
    }

    public String getTopLeftText() {
        return topLeftText;
    }

    public void setTopLeftText(String topLeftText) {
        this.topLeftText = topLeftText;
    }

    public static void save(File file, StoredPaymentContext ctx) throws IOException {
        // Creates or overwrites the file atomically
        MAPPER.writeValue(file, ctx);
    }

    public static StoredPaymentContext load(File file) throws IOException {
        return MAPPER.readValue(file, StoredPaymentContext.class);
    }

    public static ClientPaymentContext toClientPaymentContext(String result, StoredPaymentContext spc) {
        return new ClientPaymentContext()
                .result(result).entity(spc.getEntity()).payment(spc.getPay()).changeList(spc.getChg()).plist(spc.getPlist())
                .vlist(spc.getVlist()).halred(spc.isHalred()).sabred(spc.isSabred()).feeTotals(spc.getFeeTotals())
                .firstTime(spc.isFirstTime()).ryear(spc.getRyear()).pmtvesCount(spc.getPmtvesCount()).topLeftText(spc.getTopLeftText());
    }
}
