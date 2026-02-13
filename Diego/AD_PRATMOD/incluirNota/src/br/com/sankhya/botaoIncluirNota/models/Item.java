package br.com.sankhya.botaoIncluirNota.models;

import br.com.sankhya.botaoIncluirNota.includes.Queries;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Item {
    private final ArrayList<BigDecimal> ID = new ArrayList<>();
    private BigDecimal CODPROD;
    private BigDecimal VLRUNIT;
    private BigDecimal QTDNEG;

    public Item(BigDecimal ID, BigDecimal CODPROD, BigDecimal VLRUNIT, BigDecimal QTDNEG) throws Exception {
        this.ID.add(ID);
        this.CODPROD = CODPROD;
        setVLRUNIT(VLRUNIT);
        this.QTDNEG = QTDNEG;
    }

    public ArrayList<BigDecimal> getID() {
        return ID;
    }

    public void setID(BigDecimal ID) {
        this.ID.add(ID);
    }

    public BigDecimal getCODPROD() {
        return CODPROD;
    }

    public void setCODPROD(BigDecimal CODPROD) {
        this.CODPROD = CODPROD;
    }

    public BigDecimal getVLRUNIT() {
        return VLRUNIT;
    }

    public void setVLRUNIT(BigDecimal VLRUNIT) throws Exception {
        if (VLRUNIT == null || VLRUNIT.compareTo(BigDecimal.ZERO) == 0){
            Queries query = new Queries();

            NativeSql nativeSql = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

            nativeSql.setNamedParameter("CODPROD", this.CODPROD);
            nativeSql.logNamedParameters();
            ResultSet precoReposicao = nativeSql.executeQuery(query.queryRetornaUltimoCusto);

            if (precoReposicao.next()){
                this.VLRUNIT = precoReposicao.getBigDecimal(1);
            }
        } else if (VLRUNIT.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exception("Valor do item não pode ser negativo!");
        } else {
            this.VLRUNIT = VLRUNIT;
        }
    }

    public BigDecimal getQTDNEG() {
        return QTDNEG;
    }

    public void setQTDNEG(BigDecimal QTDNEG) {
        this.QTDNEG = QTDNEG;
    }

    @Override
    public String toString() {
        return "Item{" +
                "CODPROD=" + CODPROD +
                ", VLRUNIT=" + VLRUNIT +
                ", QTDNEG=" + QTDNEG +
                '}';
    }
}