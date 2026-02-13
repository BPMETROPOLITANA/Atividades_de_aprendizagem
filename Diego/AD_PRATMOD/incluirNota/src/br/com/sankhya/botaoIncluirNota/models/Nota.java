package br.com.sankhya.botaoIncluirNota.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Nota {
    private Timestamp DTNEG;
    private String CNPJCLI;
    private BigDecimal CODTIPNEG;
    private BigDecimal CODNAT;
    private BigDecimal CODCENCUS;
    private BigDecimal CODTIPOPER;
    private BigDecimal CODEMP;
    private String OBS;

    private final List<Item> itens = new ArrayList<>();

    public Nota(Timestamp DTNEG, String CNPJCLI, BigDecimal CODTIPNEG, BigDecimal CODNAT, BigDecimal CODCENCUS, BigDecimal CODTIPOPER, BigDecimal CODEMP, String OBS) {
        this.DTNEG = DTNEG;
        this.CNPJCLI = CNPJCLI;
        this.CODTIPNEG = CODTIPNEG;
        this.CODNAT = CODNAT;
        this.CODCENCUS = CODCENCUS;
        this.CODTIPOPER = CODTIPOPER;
        this.CODEMP = CODEMP;
        this.OBS = OBS;
    }

    public Timestamp getDTNEG() {
        return DTNEG;
    }

    public void setDTNEG(Timestamp DTNEG) {
        this.DTNEG = DTNEG;
    }

    public String getCGCCPF() {
        return CNPJCLI;
    }

    public void setCNPJCLI(String CNPJCLI) {
        this.CNPJCLI = CNPJCLI;
    }

    public BigDecimal getCODTIPNEG() {
        return CODTIPNEG;
    }

    public void setCODTIPNEG(BigDecimal CODTIPNEG) {
        this.CODTIPNEG = CODTIPNEG;
    }

    public BigDecimal getCODNAT() {
        return CODNAT;
    }

    public void setCODNAT(BigDecimal CODNAT) {
        this.CODNAT = CODNAT;
    }

    public BigDecimal getCODCENCUS() {
        return CODCENCUS;
    }

    public void setCODCENCUS(BigDecimal CODCENCUS) {
        this.CODCENCUS = CODCENCUS;
    }

    public BigDecimal getCODTIPOPER() {
        return CODTIPOPER;
    }

    public void setCODTIPOPER(BigDecimal CODTIPOPER) {
        this.CODTIPOPER = CODTIPOPER;
    }

    public BigDecimal getCODEMP() {
        return CODEMP;
    }

    public void setCODEMP(BigDecimal CODEMP) {
        this.CODEMP = CODEMP;
    }

    public String getOBS() {
        return OBS;
    }

    public void setOBS(String OBS) {
        this.OBS = OBS;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void addItens(Item item) {
        if (getItens().contains(item)) {

            Item itemIgual= getItens().get(getItens().indexOf(item));

            BigDecimal quantidadeAtual = itemIgual.getQTDNEG();

            itemIgual.setQTDNEG(quantidadeAtual.add(item.getQTDNEG()));

        }else {
            this.itens.add(item);
        }
    }

    @Override
    public String toString() {
        return "Nota{" +
                "DTNEG=" + DTNEG +
                ", CNPJCLI=" + CNPJCLI +
                ", CODTIPNEG=" + CODTIPNEG +
                ", CODNAT=" + CODNAT +
                ", CODCENCUS=" + CODCENCUS +
                ", CODTIPOPER=" + CODTIPOPER +
                ", CODEMP=" + CODEMP +
                ", OBS='" + OBS + '\'' +
                ", itens=" + itens +
                '}';
    }
}
