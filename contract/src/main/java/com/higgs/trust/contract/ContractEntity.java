package com.higgs.trust.contract;

public class ContractEntity {

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ContractLanguageEnum getLanguage() {
        return language;
    }

    public void setLanguage(ContractLanguageEnum language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    private String address;
    private ContractLanguageEnum language;
    private String script;
}
