package br.com.sankhya.botaoIncluirNota.includes;

public class Queries {
    public final String queryRetornaUltimoCusto = "SELECT CUSREP"
            + " FROM TGFCUS"
            + " WHERE CODPROD = :CODPROD"
            + " ORDER BY DTATUAL DESC";
}
